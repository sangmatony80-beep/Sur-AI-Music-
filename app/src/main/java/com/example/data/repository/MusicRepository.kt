package com.example.data.repository

import com.example.data.local.MusicDao
import com.example.data.local.SongEntity
import com.example.data.supabase.RemoteSongItem
import kotlinx.coroutines.flow.Flow

class MusicRepository(private val musicDao: MusicDao) {
    val allSongs: Flow<List<SongEntity>> = musicDao.getAllSongs()
    val favoriteSongs: Flow<List<SongEntity>> = musicDao.getFavoriteSongs()
    val generatedSongs: Flow<List<SongEntity>> = musicDao.getGeneratedSongs()

    suspend fun insertSong(song: SongEntity): Long {
        return musicDao.insertSong(song)
    }

    suspend fun insertSongs(songs: List<SongEntity>): List<Long> {
        return musicDao.insertSongs(songs)
    }

    suspend fun updateFavorite(songId: Long, isFavorite: Boolean) {
        musicDao.updateFavorite(songId, isFavorite)
    }

    suspend fun deleteSong(songId: Long) {
        musicDao.deleteSong(songId)
    }

    suspend fun recordSongViewed(songId: Long) {
        musicDao.updateLastViewedAt(songId, System.currentTimeMillis())
    }

    suspend fun recordSongViewedByCloudId(cloudId: String) {
        musicDao.updateLastViewedByCloudId(cloudId, System.currentTimeMillis())
    }

    suspend fun getSongById(songId: Long): SongEntity? {
        return musicDao.getSongById(songId)
    }

    /**
     * Caches remote songs fetched from Supabase directly into Room database.
     * Prevents duplicate rows if already cached, and updates lastViewed timestamp.
     */
    suspend fun cacheRemoteSongs(remoteSongs: List<RemoteSongItem>) {
        val now = System.currentTimeMillis()
        val entitiesToCache = mutableListOf<SongEntity>()

        for ((index, remote) in remoteSongs.withIndex()) {
            val existing = if (remote.id.isNotBlank()) {
                musicDao.getSongByCloudId(remote.id)
            } else null ?: musicDao.getSongByTitleAndArtist(remote.title, remote.artist)

            if (existing != null) {
                // Update timestamp so last viewed list retains freshness
                musicDao.updateLastViewedAt(existing.id, now - (index * 100))
            } else {
                entitiesToCache.add(
                    SongEntity(
                        title = remote.title,
                        artist = remote.artist,
                        genre = remote.genre,
                        audioUrl = remote.audioUrl,
                        imageUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500",
                        lyrics = remote.prompt,
                        duration = remote.duration,
                        isFavorite = false,
                        isGenerated = true,
                        cloudId = remote.id,
                        lastViewedAt = now - (index * 100),
                        createdAt = now
                    )
                )
            }
        }

        if (entitiesToCache.isNotEmpty()) {
            musicDao.insertSongs(entitiesToCache)
        }
    }
}
