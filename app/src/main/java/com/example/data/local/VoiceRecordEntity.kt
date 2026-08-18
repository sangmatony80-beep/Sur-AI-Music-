package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voice_records")
data class VoiceRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val artist: String = "User Voice",
    val duration: String = "0:30",
    val localFilePath: String,
    val audioUrl: String = "",
    val targetScale: String = "C Major",
    val vocalTone: String = "Melodious Sweetness",
    val retuneSpeed: Float = 0.85f,
    val pitchShiftSemitones: Float = 0f,
    val isCorrected: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
