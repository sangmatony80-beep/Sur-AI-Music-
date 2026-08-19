package com.ai.audio.infrastructure.export

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

object AudioTrimmer {

    /**
     * Trims a PCM byte array given a start and end time (in seconds).
     * Assumes 16-bit PCM, 48kHz, Stereo.
     */
    suspend fun trimAudio(
        pcmData: ByteArray,
        startTimeSec: Float,
        endTimeSec: Float,
        sampleRate: Int = 48000,
        channels: Int = 2
    ): ByteArray? = withContext(Dispatchers.Default) {
        if (startTimeSec >= endTimeSec) return@withContext null
        
        val bytesPerSecond = sampleRate * channels * 2 // 16-bit = 2 bytes
        val startByteIndex = (startTimeSec * bytesPerSecond).toInt()
        val endByteIndex = (endTimeSec * bytesPerSecond).toInt()
        
        val safeStart = max(0, startByteIndex)
        // Ensure we are aligning to frame boundary (4 bytes per frame for 16-bit stereo)
        val alignedStart = safeStart - (safeStart % 4)
        
        val safeEnd = min(pcmData.size, endByteIndex)
        val alignedEnd = safeEnd - (safeEnd % 4)

        if (alignedStart >= alignedEnd) return@withContext null

        val trimmedSize = alignedEnd - alignedStart
        val trimmedData = ByteArray(trimmedSize)
        
        System.arraycopy(pcmData, alignedStart, trimmedData, 0, trimmedSize)
        
        return@withContext trimmedData
    }

    /**
     * Reads PCM InputStream to ByteArray. Useful if we want to trim directly from a stream.
     */
    fun readFully(inputStream: InputStream): ByteArray {
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(4096)
        var read: Int
        while (inputStream.read(buffer).also { read = it } != -1) {
            out.write(buffer, 0, read)
        }
        return out.toByteArray()
    }
}
