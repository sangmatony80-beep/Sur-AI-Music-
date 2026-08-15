package com.example.data.supabase

import android.util.Log
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Authentication Manager using Supabase Auth SDK
 */
class SupabaseAuthManager {

    private val auth = SupabaseClientProvider.auth
    private val TAG = "SupabaseAuthManager"

    /**
     * Observable session status
     */
    val sessionStatus: Flow<SessionStatus> = auth.sessionStatus

    /**
     * Check if user is currently authenticated
     */
    val isAuthenticated: Flow<Boolean> = auth.sessionStatus.map { status ->
        status is SessionStatus.Authenticated
    }

    /**
     * Get currently logged-in user or null
     */
    val currentUser: UserInfo?
        get() = auth.currentUserOrNull()

    /**
     * Sign Up with Email and Password
     */
    suspend fun signUpWithEmail(emailInput: String, passwordInput: String): Result<UserInfo> {
        if (!SupabaseClientProvider.hasValidCredentials()) {
            return Result.failure(Exception("Supabase cloud endpoint not configured. Offline mode active."))
        }
        return try {
            auth.signUpWith(Email) {
                email = emailInput
                password = passwordInput
            }
            val user = auth.currentUserOrNull()
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Signup initiated. Verification email may be required."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Signup error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Sign In with Email and Password
     */
    suspend fun signInWithEmail(emailInput: String, passwordInput: String): Result<UserInfo> {
        if (!SupabaseClientProvider.hasValidCredentials()) {
            return Result.failure(Exception("Supabase cloud endpoint not configured. Offline mode active."))
        }
        return try {
            auth.signInWith(Email) {
                email = emailInput
                password = passwordInput
            }
            val user = auth.currentUserOrNull()
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Failed to retrieve user session after sign in."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "SignIn error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Sign Out and clear session tokens
     */
    suspend fun signOut(): Result<Unit> {
        if (!SupabaseClientProvider.hasValidCredentials()) {
            return Result.success(Unit)
        }
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "SignOut error: ${e.message}", e)
            Result.failure(e)
        }
    }
}
