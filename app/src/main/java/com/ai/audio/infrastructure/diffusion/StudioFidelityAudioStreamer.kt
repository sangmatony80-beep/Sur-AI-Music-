package com.ai.audio.infrastructure.diffusion

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.io.InputStream

class StudioFidelityAudioStreamer {

    private var audioTrack: AudioTrack? = null
    @Volatile private var isPlaying = false

    val audioSessionId: Int?
        get() = audioTrack?.audioSessionId

    // 320kbps স্টুডিও ফিডেলিটি অডিওর জন্য AudioTrack কনফিগারেশন (48kHz, Stereo)
    fun preparePlayback() {
        val sampleRate = 48000 
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
    }

    // ডিফিউশন মডেল জেনারেটেড অডিও স্ট্রিম প্লে করা
    fun streamDiffusionOutput(inputStream: InputStream) {
        if (audioTrack == null) preparePlayback()
        
        isPlaying = true
        audioTrack?.play()

        Thread {
            val buffer = ByteArray(4096)
            var bytesRead: Int
            try {
                while (inputStream.read(buffer).also { bytesRead = it } != -1 && isPlaying) {
                    audioTrack?.write(buffer, 0, bytesRead)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                stopPlayback()
            }
        }.start()
    }

    fun stopPlayback() {
        isPlaying = false
        audioTrack?.apply {
            if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                stop()
            }
            release()
        }
        audioTrack = null
    }
}
