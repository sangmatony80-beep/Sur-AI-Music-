package com.ai.audio.infrastructure.synth

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * High performance PCM synthesizer engine for generating real instrument and drum audio on device.
 */
object RealtimeAudioSynthEngine {

    private const val SAMPLE_RATE = 44100
    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * Plays synthesized PCM audio buffer directly via Android AudioTrack in MODE_STATIC.
     */
    fun playPcmBuffer(pcm16Data: ShortArray) {
        scope.launch {
            try {
                val bufferSizeInBytes = pcm16Data.size * 2
                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSizeInBytes)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(pcm16Data, 0, pcm16Data.size)
                audioTrack.play()

                // AudioTrack automatically finishes static playback; release after duration
                val durationMs = (pcm16Data.size.toFloat() / SAMPLE_RATE * 1000).toLong() + 100
                kotlinx.coroutines.delay(durationMs)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Triggers sound for a specific pad ID from the virtual beat matrix.
     */
    fun triggerPad(padId: String) {
        val buffer = when (padId) {
            // Row 1: Bangla Traditional Percussion
            "p1" -> generateTablaDha()
            "p2" -> generateTablaNa()
            "p3" -> generateDholakThump()
            "p4" -> generateKhamakZap()

            // Row 2: Bengali Acoustic Folk
            "p5" -> generatePluckedString(baseFreq = 220.0, durationSec = 0.8f, brightness = 0.6f) // Ektara
            "p6" -> generateDotaraStrum()
            "p7" -> generateFlute(freq = 523.25, durationSec = 0.9f) // Bansuri C5
            "p8" -> generateMandiraBell()

            // Row 3: Modern 808 Studio Beats
            "p9" -> generate808Kick()
            "p10" -> generateSnare()
            "p11" -> generateTrapHiHat()
            "p12" -> generateOpenCymbal()

            // Row 4: Melodic Chords
            "p13" -> generateChord(listOf(261.63, 329.63, 392.00)) // C Major (C4, E4, G4)
            "p14" -> generateChord(listOf(196.00, 246.94, 293.66)) // G Major (G3, B3, D4)
            "p15" -> generateChord(listOf(220.00, 261.63, 329.63)) // A Minor (A3, C4, E4)
            "p16" -> generateChord(listOf(174.61, 220.00, 261.63)) // F Major (F3, A3, C4)

            else -> generateSineTone(440.0, 0.3f)
        }
        playPcmBuffer(buffer)
    }

    // --- Sound Synthesis Algorithms ---

    /**
     * 808 Sub-Bass Kick: Fast pitch envelope drop from 160Hz down to 45Hz with exponential decay.
     */
    fun generate808Kick(durationSec: Float = 0.55f): ShortArray {
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            // Pitch drop envelope
            val freq = 45.0 + 130.0 * exp(-t * 22.0)
            phase += 2.0 * PI * freq / SAMPLE_RATE
            // Amplitude envelope
            val amp = exp(-t * 5.5) * 0.95
            val sample = (sin(phase) * amp * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            buffer[i] = sample.toShort()
        }
        return buffer
    }

    /**
     * Snare Drum: 180Hz sine body + White Noise burst with fast decay.
     */
    fun generateSnare(durationSec: Float = 0.3f): ShortArray {
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val toneFreq = 185.0 * exp(-t * 12.0)
            phase += 2.0 * PI * toneFreq / SAMPLE_RATE

            val toneAmp = exp(-t * 15.0) * 0.4
            val noiseAmp = exp(-t * 18.0) * 0.6
            val noise = (Random.nextFloat() * 2.0 - 1.0) * noiseAmp
            val tone = sin(phase) * toneAmp

            val sample = ((tone + noise) * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            buffer[i] = sample.toShort()
        }
        return buffer
    }

    /**
     * Trap Hi-Hat: Crisp, short bandpass white noise burst (40ms).
     */
    fun generateTrapHiHat(durationSec: Float = 0.06f): ShortArray {
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        var lastNoise = 0.0

        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val noise = (Random.nextFloat() * 2.0 - 1.0)
            // High-pass filter emulation
            val highPassNoise = noise - lastNoise * 0.85
            lastNoise = noise

            val amp = exp(-t * 80.0) * 0.75
            val sample = (highPassNoise * amp * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            buffer[i] = sample.toShort()
        }
        return buffer
    }

    /**
     * Open Cymbal: Metallic shimmer with long release.
     */
    fun generateOpenCymbal(durationSec: Float = 0.75f): ShortArray {
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val noise = (Random.nextFloat() * 2.0 - 1.0)
            val ring1 = sin(2.0 * PI * 3820.0 * t) * 0.2
            val ring2 = sin(2.0 * PI * 5420.0 * t) * 0.2
            val amp = exp(-t * 6.0) * 0.6
            val sample = ((noise * 0.6 + ring1 + ring2) * amp * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            buffer[i] = sample.toShort()
        }
        return buffer
    }

    /**
     * Tabla Dha (Bass bayan): Low pitch bending resonance (90Hz -> 65Hz) with acoustic ring.
     */
    fun generateTablaDha(durationSec: Float = 0.65f): ShortArray {
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        var phase1 = 0.0
        var phase2 = 0.0

        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val freq = 65.0 + 35.0 * exp(-t * 8.0)
            phase1 += 2.0 * PI * freq / SAMPLE_RATE
            phase2 += 2.0 * PI * (freq * 2.01) / SAMPLE_RATE

            val amp = exp(-t * 6.5) * 0.85
            val harmonic = sin(phase1) * 0.75 + sin(phase2) * 0.25
            val sample = (harmonic * amp * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            buffer[i] = sample.toShort()
        }
        return buffer
    }

    /**
     * Tabla Na (Dayan rim sound): Crisp metallic ringing tone at 360Hz + 720Hz overtone.
     */
    fun generateTablaNa(durationSec: Float = 0.45f): ShortArray {
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val f1 = 360.0
            val f2 = 720.0
            val f3 = 1080.0
            val ring = sin(2.0 * PI * f1 * t) * 0.6 + sin(2.0 * PI * f2 * t) * 0.3 + sin(2.0 * PI * f3 * t) * 0.1
            val amp = exp(-t * 9.0) * 0.8
            val sample = (ring * amp * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            buffer[i] = sample.toShort()
        }
        return buffer
    }

    /**
     * Dholak Thump: Warm wooden low strike.
     */
    fun generateDholakThump(durationSec: Float = 0.5f): ShortArray {
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val freq = 90.0 + 80.0 * exp(-t * 20.0)
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val noise = (Random.nextFloat() * 2.0 - 1.0) * exp(-t * 40.0) * 0.2
            val amp = exp(-t * 8.0) * 0.85
            val sample = ((sin(phase) + noise) * amp * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            buffer[i] = sample.toShort()
        }
        return buffer
    }

    /**
     * Khamak Zap: Bengali Anandalahari swooping string tension sound.
     */
    fun generateKhamakZap(durationSec: Float = 0.4f): ShortArray {
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            // Swooping pitch upwards then settling
            val freq = 120.0 + 260.0 * sin(PI * (t / durationSec))
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val amp = exp(-t * 7.0) * 0.8
            val sample = (sin(phase) * amp * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            buffer[i] = sample.toShort()
        }
        return buffer
    }

    /**
     * Plucked String (Ektara / Dotara): Harmonic rich pluck with natural body resonance.
     */
    fun generatePluckedString(baseFreq: Double, durationSec: Float = 0.8f, brightness: Float = 0.6f): ShortArray {
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val h1 = sin(2.0 * PI * baseFreq * t)
            val h2 = sin(2.0 * PI * (baseFreq * 2) * t) * (brightness * 0.6)
            val h3 = sin(2.0 * PI * (baseFreq * 3) * t) * (brightness * 0.35)
            val h4 = sin(2.0 * PI * (baseFreq * 4) * t) * (brightness * 0.15)

            val amp = exp(-t * 5.0) * 0.8
            val combined = (h1 + h2 + h3 + h4) * amp
            val sample = (combined * Short.MAX_VALUE * 0.7).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            buffer[i] = sample.toShort()
        }
        return buffer
    }

    /**
     * Dotara Strum: Double-string folk acoustic strum.
     */
    fun generateDotaraStrum(): ShortArray {
        val durationSec = 0.7f
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val s1 = sin(2.0 * PI * 293.66 * t) * exp(-t * 6.0) // D4
            val s2 = sin(2.0 * PI * 440.00 * (t - 0.02).coerceAtLeast(0.0)) * exp(-t * 5.5) // A4 strum delay
            val s3 = sin(2.0 * PI * 587.33 * (t - 0.04).coerceAtLeast(0.0)) * exp(-t * 5.0) // D5 strum delay

            val combined = (s1 * 0.5 + s2 * 0.35 + s3 * 0.25)
            val sample = (combined * Short.MAX_VALUE * 0.8).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            buffer[i] = sample.toShort()
        }
        return buffer
    }

    /**
     * Bansuri Flute: Bamboo flute with gentle vibrato and breath air.
     */
    fun generateFlute(freq: Double, durationSec: Float = 0.9f): ShortArray {
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val vibrato = sin(2.0 * PI * 5.5 * t) * 4.0 // 5.5Hz vibrato
            val currentFreq = freq + vibrato
            phase += 2.0 * PI * currentFreq / SAMPLE_RATE

            // Soft attack envelope
            val attack = (t / 0.08f).coerceAtMost(1f)
            val release = exp(-(t - 0.5f).coerceAtLeast(0f) * 4.0)
            val env = attack * release * 0.75

            val breath = (Random.nextFloat() * 2.0 - 1.0) * 0.05
            val harmonic2 = sin(phase * 2.0) * 0.15
            val tone = (sin(phase) + harmonic2 + breath) * env

            val sample = (tone * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            buffer[i] = sample.toShort()
        }
        return buffer
    }

    /**
     * Mandira Bell: Pure high metallic dual chime.
     */
    fun generateMandiraBell(durationSec: Float = 1.0f): ShortArray {
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val bell1 = sin(2.0 * PI * 1760.0 * t) * 0.6
            val bell2 = sin(2.0 * PI * 3520.0 * t) * 0.3
            val bell3 = sin(2.0 * PI * 5280.0 * t) * 0.1
            val amp = exp(-t * 4.0) * 0.75

            val sample = ((bell1 + bell2 + bell3) * amp * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            buffer[i] = sample.toShort()
        }
        return buffer
    }

    /**
     * Polyphonic Chord Generator: Synthesizes multiple note frequencies simultaneously.
     */
    fun generateChord(frequencies: List<Double>, durationSec: Float = 0.85f): ShortArray {
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            var chordSum = 0.0
            for (f in frequencies) {
                // Soft warm organ harmonics
                chordSum += sin(2.0 * PI * f * t) * 0.5 + sin(2.0 * PI * (f * 2) * t) * 0.2
            }
            chordSum /= frequencies.size.toDouble()

            // ADSR Envelope
            val attack = (t / 0.04f).coerceAtMost(1f)
            val decay = exp(-t * 2.8)
            val env = attack * decay * 0.85

            val sample = (chordSum * env * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            buffer[i] = sample.toShort()
        }
        return buffer
    }

    /**
     * Simple Sine Tone Generator for tuning or preview.
     */
    fun generateSineTone(freq: Double, durationSec: Float = 0.3f): ShortArray {
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val sample = (sin(2.0 * PI * freq * t) * exp(-t * 4.0) * Short.MAX_VALUE * 0.7).toInt()
            buffer[i] = sample.toShort()
        }
        return buffer
    }
}
