package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lyrics_history")
data class LyricsHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val language: String,
    val lyrics: String,
    val isCopyrightClean: Boolean,
    val timestamp: Long
)

@Entity(tableName = "cloned_voices")
data class ClonedVoiceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val sampleDescription: String,
    val timestamp: Long
)
