package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {
    @Query("SELECT * FROM songs ORDER BY lastViewedAt DESC, createdAt DESC")
    fun getAllSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE isGenerated = 1 ORDER BY createdAt DESC")
    fun getGeneratedSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE id = :songId LIMIT 1")
    suspend fun getSongById(songId: Long): SongEntity?

    @Query("SELECT * FROM songs WHERE cloudId = :cloudId LIMIT 1")
    suspend fun getSongByCloudId(cloudId: String): SongEntity?

    @Query("SELECT * FROM songs WHERE LOWER(title) = LOWER(:title) AND LOWER(artist) = LOWER(:artist) LIMIT 1")
    suspend fun getSongByTitleAndArtist(title: String, artist: String): SongEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>): List<Long>

    @Query("UPDATE songs SET lastViewedAt = :timestamp WHERE id = :songId")
    suspend fun updateLastViewedAt(songId: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE songs SET lastViewedAt = :timestamp WHERE cloudId = :cloudId")
    suspend fun updateLastViewedByCloudId(cloudId: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE songs SET isFavorite = :isFavorite WHERE id = :songId")
    suspend fun updateFavorite(songId: Long, isFavorite: Boolean)

    @Query("DELETE FROM songs WHERE id = :songId")
    suspend fun deleteSong(songId: Long)

    @Query("DELETE FROM songs")
    suspend fun clearAllSongs()
}
