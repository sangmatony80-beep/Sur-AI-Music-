package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    suspend fun getAllUsersList(): List<UserEntity>

    @Query("UPDATE users SET role = :role WHERE email = :email")
    suspend fun updateUserRole(email: String, role: String)

    @Query("UPDATE users SET isBanned = :isBanned WHERE email = :email")
    suspend fun updateUserBanned(email: String, isBanned: Boolean)

    @Query("UPDATE users SET tokenBalance = :tokenBalance WHERE email = :email")
    suspend fun updateUserTokenBalance(email: String, tokenBalance: Int)

    @Query("DELETE FROM users WHERE email = :email")
    suspend fun deleteUser(email: String)
}
