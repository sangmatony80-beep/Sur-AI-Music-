package com.example.data.supabase

import android.content.Context
import android.util.Log
import com.example.data.datastore.SettingsDataStore
import com.example.data.local.UserDao
import com.example.data.local.UserEntity
import com.example.data.repository.AuthResult
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * User Session Manager using Supabase Auth that:
 * 1. Persists session state across app launches (DataStore & Room synchronization)
 * 2. Handles Sign-Up and Sign-In with Email and Password via Supabase Auth
 * 3. Gracefully bridges local SQLite persistence with remote Supabase cloud authentication
 */
class SupabaseUserSessionManager(
    private val context: Context,
    private val userDao: UserDao,
    private val settingsDataStore: SettingsDataStore,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val TAG = "SupabaseUserSessionMgr"
    private val auth = SupabaseClientProvider.auth

    // Observable session status from Supabase
    val sessionStatus: Flow<SessionStatus> = auth.sessionStatus

    val isSupabaseAuthenticated: Flow<Boolean> = auth.sessionStatus.map { status ->
        status is SessionStatus.Authenticated
    }

    private val _currentUserProfile = MutableStateFlow<UserEntity?>(null)
    val currentUserProfile: StateFlow<UserEntity?> = _currentUserProfile.asStateFlow()

    private val _isSessionLoaded = MutableStateFlow(false)
    val isSessionLoaded: StateFlow<Boolean> = _isSessionLoaded.asStateFlow()

    init {
        // Observe Supabase session status changes only if valid active project credentials are provided
        if (SupabaseClientProvider.hasValidCredentials()) {
            coroutineScope.launch {
                try {
                    auth.sessionStatus.collect { status ->
                        when (status) {
                            is SessionStatus.Authenticated -> {
                                val user = auth.currentUserOrNull()
                                val email = user?.email
                                if (!email.isNullOrBlank()) {
                                    syncLocalSession(email, role = "USER")
                                }
                            }
                            is SessionStatus.NotAuthenticated -> {
                                Log.d(TAG, "Supabase session status: NotAuthenticated")
                            }
                            else -> Unit
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Supabase session collection stopped: ${e.message}")
                }
            }
        }

        // Restore saved session on startup
        coroutineScope.launch {
            restoreSavedSession()
        }
    }

    /**
     * Restore saved session from persistent DataStore & Local Room DB
     */
    suspend fun restoreSavedSession(): UserEntity? {
        val savedEmail = settingsDataStore.loggedInEmail.firstOrNull()
        val savedRole = settingsDataStore.loggedInRole.firstOrNull() ?: "USER"

        if (!savedEmail.isNullOrBlank()) {
            var localUser = userDao.getUserByEmail(savedEmail)
            if (localUser == null) {
                // Auto-provision local record if authenticated remotely
                val newUser = UserEntity(
                    email = savedEmail,
                    passwordHash = "",
                    fullName = savedEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                    role = savedRole,
                    tokenBalance = if (savedRole == "ADMIN") 99999 else 250,
                    isBanned = false
                )
                userDao.insertUser(newUser)
                localUser = newUser
            }

            if (!localUser.isBanned) {
                _currentUserProfile.value = localUser
                _isSessionLoaded.value = true
                return localUser
            }
        }
        _isSessionLoaded.value = true
        return null
    }

    /**
     * Sign Up with Email and Password using Supabase Auth
     */
    suspend fun signUpWithEmail(
        emailInput: String,
        passwordInput: String,
        fullNameInput: String,
        roleInput: String = "USER"
    ): AuthResult {
        val email = emailInput.trim().lowercase()
        val password = passwordInput.trim()
        val fullName = fullNameInput.trim()

        if (email.isBlank() || !email.contains("@")) {
            return AuthResult.Error("Please enter a valid email address.")
        }
        if (password.length < 6) {
            return AuthResult.Error("Password must be at least 6 characters.")
        }

        return try {
            // Attempt Supabase Auth Sign Up if valid credentials configured
            var remoteUserId = ""
            if (SupabaseClientProvider.hasValidCredentials()) {
                try {
                    auth.signUpWith(Email) {
                        this.email = email
                        this.password = password
                    }
                    val remoteUser = auth.currentUserOrNull()
                    remoteUserId = remoteUser?.id ?: ""
                } catch (e: Exception) {
                    Log.w(TAG, "Supabase remote signup fallback (offline/custom domain): ${e.message}")
                }
            }

            // Sync or create local Room user record
            val existing = userDao.getUserByEmail(email)
            if (existing != null) {
                return AuthResult.Error("An account with this email already exists.")
            }

            val newUser = UserEntity(
                email = email,
                passwordHash = password, // In production stored securely/hashed
                fullName = if (fullName.isNotBlank()) fullName else email.substringBefore("@"),
                role = roleInput,
                tokenBalance = if (roleInput == "ADMIN") 99999 else 250,
                isBanned = false
            )
            userDao.insertUser(newUser)

            // Persist session to DataStore
            settingsDataStore.saveUserSession(email, roleInput)
            _currentUserProfile.value = newUser

            AuthResult.Success(newUser)
        } catch (e: Exception) {
            Log.e(TAG, "Sign up failure: ${e.message}", e)
            AuthResult.Error("Registration failed: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    /**
     * Sign In with Email and Password using Supabase Auth
     */
    suspend fun signInWithEmail(
        emailInput: String,
        passwordInput: String
    ): AuthResult {
        val email = emailInput.trim().lowercase()
        val password = passwordInput.trim()

        if (email.isBlank() || !email.contains("@")) {
            return AuthResult.Error("Please enter a valid email address.")
        }
        if (password.isBlank()) {
            return AuthResult.Error("Please enter your password.")
        }

        return try {
            // Attempt remote Supabase authentication only if valid credentials configured
            var remoteAuthSuccess = false
            if (SupabaseClientProvider.hasValidCredentials()) {
                try {
                    auth.signInWith(Email) {
                        this.email = email
                        this.password = password
                    }
                    remoteAuthSuccess = auth.currentUserOrNull() != null
                } catch (e: Exception) {
                    Log.w(TAG, "Supabase remote signin fallback: ${e.message}")
                }
            }

            // Check local Room user record
            val localUser = userDao.getUserByEmail(email)

            if (localUser != null) {
                if (localUser.isBanned) {
                    return AuthResult.Error("This account has been suspended by system administrators.")
                }
                if (!remoteAuthSuccess && localUser.passwordHash.isNotEmpty() && localUser.passwordHash != password) {
                    return AuthResult.Error("Invalid email or password.")
                }

                // Persist session
                settingsDataStore.saveUserSession(localUser.email, localUser.role)
                _currentUserProfile.value = localUser
                return AuthResult.Success(localUser)
            } else if (remoteAuthSuccess) {
                // User exists on Supabase Cloud, provision into local database
                val newUser = UserEntity(
                    email = email,
                    passwordHash = password,
                    fullName = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                    role = "USER",
                    tokenBalance = 250,
                    isBanned = false
                )
                userDao.insertUser(newUser)
                settingsDataStore.saveUserSession(newUser.email, newUser.role)
                _currentUserProfile.value = newUser
                return AuthResult.Success(newUser)
            } else {
                return AuthResult.Error("No account found with this email. Please sign up.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sign in failure: ${e.message}", e)
            AuthResult.Error("Sign in failed: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    /**
     * Sign Out and clear both Supabase session tokens and local DataStore persistence
     */
    suspend fun signOut(): Result<Unit> {
        return try {
            if (SupabaseClientProvider.hasValidCredentials()) {
                try {
                    auth.signOut()
                } catch (e: Exception) {
                    Log.w(TAG, "Supabase remote signOut: ${e.message}")
                }
            }
            settingsDataStore.clearUserSession()
            _currentUserProfile.value = null
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Sign out error: ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun syncLocalSession(email: String, role: String) {
        val user = userDao.getUserByEmail(email) ?: run {
            val newUser = UserEntity(
                email = email,
                passwordHash = "",
                fullName = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                role = role,
                tokenBalance = 250,
                isBanned = false
            )
            userDao.insertUser(newUser)
            newUser
        }
        settingsDataStore.saveUserSession(user.email, user.role)
        _currentUserProfile.value = user
    }
}
