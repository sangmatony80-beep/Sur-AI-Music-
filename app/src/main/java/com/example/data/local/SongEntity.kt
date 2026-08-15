package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val artist: String,
    val genre: String,
    val audioUrl: String,
    val imageUrl: String,
    val lyrics: String,
    val duration: String,
    val isFavorite: Boolean = false,
    val isGenerated: Boolean = false,
    val cloudId: String = "",
    val lastViewedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
