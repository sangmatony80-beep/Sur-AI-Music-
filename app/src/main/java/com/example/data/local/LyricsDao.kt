package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LyricsDao {
    @Query("SELECT * FROM lyrics_history ORDER BY timestamp DESC")
    fun getAllLyricsHistory(): Flow<List<LyricsHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLyrics(lyrics: LyricsHistoryEntity): Long

    @Query("DELETE FROM lyrics_history WHERE id = :id")
    suspend fun deleteLyrics(id: Long)

    @Query("SELECT * FROM cloned_voices ORDER BY timestamp DESC")
    fun getAllClonedVoices(): Flow<List<ClonedVoiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClonedVoice(voice: ClonedVoiceEntity)

    @Query("DELETE FROM cloned_voices WHERE id = :id")
    suspend fun deleteClonedVoice(id: Long)
}
