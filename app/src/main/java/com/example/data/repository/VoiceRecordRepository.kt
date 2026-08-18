package com.example.data.repository

import android.util.Log
import com.example.data.local.VoiceRecordDao
import com.example.data.local.VoiceRecordEntity
import com.example.data.supabase.RemoteVoiceRecordItem
import com.example.data.supabase.SupabaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class VoiceRecordRepository(
    private val voiceRecordDao: VoiceRecordDao,
    private val supabaseRepository: SupabaseRepository = SupabaseRepository()
) {

    private val TAG = "VoiceRecordRepository"

    val allVoiceRecords: Flow<List<VoiceRecordEntity>> = voiceRecordDao.getAllVoiceRecords()

    fun getVoiceRecordsByStatus(isCorrected: Boolean): Flow<List<VoiceRecordEntity>> {
        return voiceRecordDao.getVoiceRecordsByStatus(isCorrected)
    }

    suspend fun getVoiceRecordById(id: Long): VoiceRecordEntity? {
        return voiceRecordDao.getVoiceRecordById(id)
    }

    /**
     * Inserts voice record into local Room database and synchronizes audio metadata with Supabase PostgreSQL.
     */
    suspend fun insertVoiceRecord(record: VoiceRecordEntity): Long {
        val insertedId = voiceRecordDao.insertVoiceRecord(record)
        syncRecordToSupabase(record.copy(id = insertedId))
        return insertedId
    }

    /**
     * Publishes a single local voice record to Supabase PostgreSQL table.
     */
    private suspend fun syncRecordToSupabase(record: VoiceRecordEntity) {
        val remoteItem = RemoteVoiceRecordItem(
            id = record.id.toString(),
            title = record.title,
            artist = record.artist,
            duration = record.duration,
            localFilePath = record.localFilePath,
            audioUrl = record.audioUrl,
            targetScale = record.targetScale,
            vocalTone = record.vocalTone,
            retuneSpeed = record.retuneSpeed,
            pitchShiftSemitones = record.pitchShiftSemitones,
            isCorrected = record.isCorrected,
            createdAt = record.createdAt
        )
        supabaseRepository.publishVoiceRecord(remoteItem)
    }

    /**
     * Synchronizes Room database with Supabase PostgreSQL table to pull cross-device audio tracks.
     */
    suspend fun syncWithSupabase(): Result<Int> {
        return try {
            val remoteResult = supabaseRepository.fetchVoiceRecords()
            val remoteRecords = remoteResult.getOrNull() ?: emptyList()
            var addedCount = 0

            val currentLocalRecords = voiceRecordDao.getAllVoiceRecords().firstOrNull() ?: emptyList()
            val localTitles = currentLocalRecords.map { it.title }.toSet()

            for (remote in remoteRecords) {
                if (!localTitles.contains(remote.title)) {
                    val localEntity = VoiceRecordEntity(
                        title = remote.title,
                        artist = remote.artist,
                        duration = remote.duration,
                        localFilePath = remote.localFilePath,
                        audioUrl = remote.audioUrl,
                        targetScale = remote.targetScale,
                        vocalTone = remote.vocalTone,
                        retuneSpeed = remote.retuneSpeed,
                        pitchShiftSemitones = remote.pitchShiftSemitones,
                        isCorrected = remote.isCorrected,
                        createdAt = remote.createdAt
                    )
                    voiceRecordDao.insertVoiceRecord(localEntity)
                    addedCount++
                }
            }

            Log.i(TAG, "Successfully synced $addedCount cross-device tracks from Supabase PostgreSQL")
            Result.success(addedCount)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing voice records with Supabase: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteVoiceRecordById(id: Long) {
        voiceRecordDao.deleteVoiceRecordById(id)
    }

    suspend fun clearAllVoiceRecords() {
        voiceRecordDao.clearAllVoiceRecords()
    }
}
