package com.ai.audio.infrastructure.diffusion

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.util.Log

class AudioFXManager {
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null

    fun attachToAudioSession(audioSessionId: Int) {
        try {
            // Equalizer
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = true
            }
            
            // Bass Boost
            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = true
                setStrength(0.toShort()) // Initial
            }
            
            // Stereo Widener (Virtualizer)
            virtualizer = Virtualizer(0, audioSessionId).apply {
                enabled = true
                setStrength(0.toShort())
            }
            Log.d("AudioFXManager", "Audio FX successfully attached to session $audioSessionId")
        } catch (e: Exception) {
            Log.e("AudioFXManager", "Failed to attach Audio FX: ${e.message}")
        }
    }

    fun setBassBoost(strengthProgress: Float) { // 0.0 to 1.0
        val strength = (strengthProgress * 1000).toInt().toShort()
        bassBoost?.setStrength(strength)
    }

    fun setVirtualizer(strengthProgress: Float) { // 0.0 to 1.0
        val strength = (strengthProgress * 1000).toInt().toShort()
        virtualizer?.setStrength(strength)
    }

    fun setEqualizerBand(bandIndex: Short, gainDb: Float) {
        try {
            // Eq gain in millibels. 1 dB = 100 mB.
            // Android Eq ranges usually from -1500 to +1500
            val maxDb = 15f
            val normalizedGainDb = gainDb.coerceIn(-maxDb, maxDb)
            val millibels = (normalizedGainDb * 100).toInt().toShort()
            equalizer?.setBandLevel(bandIndex, millibels)
        } catch (e: Exception) {
            Log.e("AudioFXManager", "Failed to set EQ band: ${e.message}")
        }
    }

    fun release() {
        equalizer?.release()
        bassBoost?.release()
        virtualizer?.release()
        equalizer = null
        bassBoost = null
        virtualizer = null
    }
}