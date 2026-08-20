package com.example.data.repository

import com.example.data.local.UserDao
import com.example.data.local.UserEntity
import kotlinx.coroutines.flow.Flow

sealed class AuthResult {
    data class Success(val user: UserEntity) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class UserRepository(private val userDao: UserDao) {

    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()

    suspend fun seedDefaultAccounts() {
        val admin = userDao.getUserByEmail("admin@suraimusic.com")
        if (admin == null) {
            userDao.insertUser(
                UserEntity(
                    email = "admin@suraimusic.com",
                    passwordHash = "admin123",
                    fullName = "Sur AI Admin",
                    role = "ADMIN",
                    tokenBalance = 99999,
                    isBanned = false
                )
            )
        }

        val defaultUser = userDao.getUserByEmail("user@suraimusic.com")
        if (defaultUser == null) {
            userDao.insertUser(
                UserEntity(
                    email = "user@suraimusic.com",
                    passwordHash = "user123",
                    fullName = "Pro Studio Creator",
                    role = "USER",
                    tokenBalance = 500,
                    isBanned = false
                )
            )
        }
    }

    suspend fun registerUser(
        email: String,
        password: String,
        fullName: String,
        role: String = "USER"
    ): AuthResult {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            return AuthResult.Error("Please enter a valid email address.")
        }
        if (password.length < 4) {
            return AuthResult.Error("Password must be at least 4 characters.")
        }

        val existing = userDao.getUserByEmail(cleanEmail)
        if (existing != null) {
            return AuthResult.Error("An account with this email already exists.")
        }

        val newUser = UserEntity(
            email = cleanEmail,
            passwordHash = password,
            fullName = fullName.ifBlank { "Sur AI Creator" },
            role = role,
            tokenBalance = if (role == "ADMIN") 99999 else 200,
            isBanned = false
        )

        userDao.insertUser(newUser)
        return AuthResult.Success(newUser)
    }

    suspend fun loginUser(email: String, password: String): AuthResult {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank()) {
            return AuthResult.Error("Email field cannot be empty.")
        }

        val user = userDao.getUserByEmail(cleanEmail)
            ?: return AuthResult.Error("No account found with this email. Please Sign Up.")

        if (user.isBanned) {
            return AuthResult.Error("This account has been suspended by an Administrator.")
        }

        if (user.passwordHash != password) {
            return AuthResult.Error("Incorrect password. Please try again.")
        }

        return AuthResult.Success(user)
    }

    suspend fun getUserByEmail(email: String): UserEntity? {
        return userDao.getUserByEmail(email.trim().lowercase())
    }

    suspend fun insertOrUpdateUser(user: UserEntity) {
        userDao.insertOrUpdateUser(user)
    }

    suspend fun updateUserRole(email: String, newRole: String) {
        userDao.updateUserRole(email.trim().lowercase(), newRole)
    }

    suspend fun updateUserBanned(email: String, isBanned: Boolean) {
        userDao.updateUserBanned(email.trim().lowercase(), isBanned)
    }

    suspend fun updateUserTokenBalance(email: String, tokenBalance: Int) {
        userDao.updateUserTokenBalance(email.trim().lowercase(), tokenBalance)
    }

    suspend fun deleteUser(email: String) {
        userDao.deleteUser(email.trim().lowercase())
    }
}
