package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceRecordDao {
    @Query("SELECT * FROM voice_records ORDER BY createdAt DESC")
    fun getAllVoiceRecords(): Flow<List<VoiceRecordEntity>>

    @Query("SELECT * FROM voice_records WHERE isCorrected = :isCorrected ORDER BY createdAt DESC")
    fun getVoiceRecordsByStatus(isCorrected: Boolean): Flow<List<VoiceRecordEntity>>

    @Query("SELECT * FROM voice_records WHERE id = :id LIMIT 1")
    suspend fun getVoiceRecordById(id: Long): VoiceRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoiceRecord(record: VoiceRecordEntity): Long

    @Query("DELETE FROM voice_records WHERE id = :id")
    suspend fun deleteVoiceRecordById(id: Long)

    @Query("DELETE FROM voice_records")
    suspend fun clearAllVoiceRecords()
}
