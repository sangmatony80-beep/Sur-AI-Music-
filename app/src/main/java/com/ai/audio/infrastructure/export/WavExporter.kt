package com.ai.audio.infrastructure.export

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream

object WavExporter {
    private fun writeWavHeader(out: OutputStream, audioDataLength: Int, sampleRate: Int, channels: Int, bitRate: Int) {
        val totalDataLen = audioDataLength + 36
        val byteRate = sampleRate * channels * bitRate / 8
        val header = ByteArray(44)

        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16 // 4 bytes: size of 'fmt ' chunk
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // format = 1 (PCM)
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = (channels * bitRate / 8).toByte() // block align
        header[33] = 0
        header[34] = bitRate.toByte() // bits per sample
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (audioDataLength and 0xff).toByte()
        header[41] = ((audioDataLength shr 8) and 0xff).toByte()
        header[42] = ((audioDataLength shr 16) and 0xff).toByte()
        header[43] = ((audioDataLength shr 24) and 0xff).toByte()

        out.write(header, 0, 44)
    }

    /**
     * Converts raw PCM byte array to a playable WAV file and saves it to the device's Music folder.
     */
    suspend fun exportToWav(
        context: Context, 
        pcmData: ByteArray, 
        filename: String, 
        sampleRate: Int = 48000, 
        channels: Int = 2, 
        bitRate: Int = 16
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "$filename.wav")
                    put(MediaStore.MediaColumns.MIME_TYPE, "audio/wav")
                    // Store in the Music/SurSun folder
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + "/SurSun")
                }

                val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    resolver.openOutputStream(it)?.use { outputStream ->
                        writeWavHeader(outputStream, pcmData.size, sampleRate, channels, bitRate)
                        outputStream.write(pcmData)
                        outputStream.flush()
                    }
                    return@withContext true
                }
                return@withContext false
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext false
            }
        }
    }
}
