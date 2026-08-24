package com.example.data.audio

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.data.local.SongEntity
import kotlinx.coroutines.*

class RealAudioPlaybackManager(private val context: Context) {

    private var exoPlayer: ExoPlayer? = null
    private var playbackJob: Job? = null
    private val audioScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    @Volatile
    private var isCurrentlyPlaying = false

    @Volatile
    private var isPaused = false

    @Volatile
    private var currentSongId: Long = -1

    @Volatile
    private var playbackPositionSeconds: Float = 0f

    @Volatile
    private var totalDurationSeconds: Float = 210f

    private var currentVolume: Float = 1.0f
    
    private var progressCallback: ((Float, Float) -> Unit)? = null

    init {
        initExoPlayer()
    }

    private fun initExoPlayer() {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_READY -> {
                                totalDurationSeconds = (duration.toFloat() / 1000f).coerceAtLeast(10f)
                            }
                            Player.STATE_ENDED -> {
                                isCurrentlyPlaying = false
                                isPaused = false
                                progressCallback?.invoke(1f, totalDurationSeconds)
                                stopProgressTracking()
                            }
                            else -> {}
                        }
                    }

                    override fun onIsPlayingChanged(isPlayingChanged: Boolean) {
                        isCurrentlyPlaying = isPlayingChanged
                        if (isCurrentlyPlaying) {
                            isPaused = false
                            startProgressTracking()
                        } else {
                            if (playbackState != Player.STATE_ENDED) {
                                isPaused = true
                            }
                            stopProgressTracking()
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        Log.e("RealAudioPlayback", "ExoPlayer error: ${error.message}", error)
                        isCurrentlyPlaying = false
                        stopProgressTracking()
                    }
                })
            }
        }
        exoPlayer?.volume = currentVolume
    }

    private fun parseDuration(duration: String): Float {
        val parts = duration.split(":")
        return if (parts.size == 2) {
            val m = parts[0].toIntOrNull() ?: 3
            val s = parts[1].toIntOrNull() ?: 30
            (m * 60 + s).toFloat().coerceAtLeast(30f)
        } else 210f
    }

    fun play(song: SongEntity, initialProgress: Float = 0f, onProgressUpdate: ((Float, Float) -> Unit)? = null) {
        if (exoPlayer == null) {
            initExoPlayer()
        }
        
        progressCallback = onProgressUpdate
        currentSongId = song.id
        totalDurationSeconds = parseDuration(song.duration)
        playbackPositionSeconds = (initialProgress * totalDurationSeconds).coerceIn(0f, totalDurationSeconds)
        
        // Play guaranteed synthetic musical chord / vocal sound immediately
        playSyntheticMelody()

        var url = song.audioUrl.trim()
        val isLocalFile = url.startsWith("/") || url.startsWith("file://")
        
        // Ensure demo streams and local synthetic fallbacks map to real locally synthesized WAV files
        if (!isLocalFile && (url.isBlank() || url.contains("demo") || url.contains("raw.vocal") || url.contains("tuned.vocal") || url.contains("example") || url.contains("soundhelix"))) {
            try {
                url = kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) {
                    AiVocalSingingEngine.getInstance(context).synthesizeRealMasterSongWav(
                        title = song.title.ifBlank { "Sur AI Track" },
                        artist = song.artist.ifBlank { "Sur AI" },
                        genre = song.genre.ifBlank { "Pop" },
                        vibe = "Melodious",
                        voiceName = song.artist,
                        lyrics = song.lyrics
                    ).absolutePath
                }
            } catch (_: Exception) {}
        }

        try {
            val uri = if (isLocalFile) {
                if (url.startsWith("file://")) android.net.Uri.parse(url)
                else android.net.Uri.fromFile(java.io.File(url))
            } else {
                android.net.Uri.parse(url)
            }
            val mediaItem = MediaItem.fromUri(uri)
            exoPlayer?.apply {
                setMediaItem(mediaItem)
                prepare()
                seekTo((playbackPositionSeconds * 1000L).toLong())
                playWhenReady = true
            }

            // Sing lyrics in AI voice if song has lyrics
            if (song.lyrics.isNotBlank() && song.isGenerated) {
                val voiceModel = when {
                    song.artist.isNotBlank() && !song.artist.contains("Creator Studio", ignoreCase = true) -> song.artist
                    song.genre.isNotBlank() -> song.genre
                    else -> "Aria"
                }
                AiVocalSingingEngine.getInstance(context).singSongLyricsLive(
                    lyrics = song.lyrics,
                    voiceName = voiceModel
                )
            }
        } catch (e: Exception) {
            Log.e("RealAudioPlayback", "Failed to play URL: $url", e)
        }
    }

    private fun playSyntheticMelody() {
        audioScope.launch(Dispatchers.IO) {
            var audioTrack: android.media.AudioTrack? = null
            try {
                val sampleRate = 44100
                val numSamples = sampleRate * 2 // 2 seconds of clean chord
                val sample = ByteArray(numSamples * 2)
                val freqs = doubleArrayOf(261.63, 329.63, 392.00, 523.25) // C major chord
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    var valSample = 0.0
                    for (f in freqs) {
                        valSample += kotlin.math.sin(2.0 * kotlin.math.PI * f * t) * 0.25
                    }
                    val envelope = if (i > numSamples - 11025) (numSamples - i).toDouble() / 11025.0 else 1.0
                    val sampleVal = (valSample * envelope * 32767.0).toInt().coerceIn(-32768, 32767)
                    sample[2 * i] = (sampleVal and 0xff).toByte()
                    sample[2 * i + 1] = ((sampleVal shr 8) and 0xff).toByte()
                }
                audioTrack = android.media.AudioTrack.Builder()
                    .setAudioAttributes(
                        android.media.AudioAttributes.Builder()
                            .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        android.media.AudioFormat.Builder()
                            .setEncoding(android.media.AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(android.media.AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(sample.size)
                    .setTransferMode(android.media.AudioTrack.MODE_STATIC)
                    .build()
                audioTrack.write(sample, 0, sample.size)
                audioTrack.play()
                delay(2200L)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                Log.e("RealAudioPlayback", "Synthetic audio error", e)
                try {
                    audioTrack?.release()
                } catch (_: Exception) {}
            }
        }
    }

    private fun startProgressTracking() {
        playbackJob?.cancel()
        playbackJob = audioScope.launch {
            while (isActive) {
                exoPlayer?.let { player ->
                    if (player.isPlaying) {
                        val currentPosMs = player.currentPosition
                        val currentSec = currentPosMs / 1000f
                        playbackPositionSeconds = currentSec
                        val durationSec = (player.duration / 1000f).coerceAtLeast(1f)
                        val progress = (currentSec / durationSec).coerceIn(0f, 1f)
                        progressCallback?.invoke(progress, currentSec)
                    }
                }
                delay(250)
            }
        }
    }

    private fun stopProgressTracking() {
        playbackJob?.cancel()
        playbackJob = null
    }

    fun pause() {
        isPaused = true
        exoPlayer?.pause()
    }

    fun resume() {
        isPaused = false
        exoPlayer?.play()
    }

    fun seekTo(fraction: Float, song: SongEntity? = null, onProgressUpdate: ((Float, Float) -> Unit)? = null) {
        progressCallback = onProgressUpdate ?: progressCallback
        if (exoPlayer != null) {
            val durationMs = exoPlayer?.duration?.takeIf { it > 0 } ?: (totalDurationSeconds * 1000f).toLong()
            val targetMs = (fraction * durationMs).toLong()
            exoPlayer?.seekTo(targetMs)
            playbackPositionSeconds = targetMs / 1000f
        } else if (song != null) {
            play(song, initialProgress = fraction, onProgressUpdate = onProgressUpdate)
        }
    }

    fun setVolume(volume: Float) {
        currentVolume = volume.coerceIn(0f, 1f)
        exoPlayer?.volume = currentVolume
    }

    fun stop() {
        stopProgressTracking()
        exoPlayer?.stop()
        exoPlayer?.clearMediaItems()
        isCurrentlyPlaying = false
        isPaused = false
    }
    
    fun release() {
        stop()
        exoPlayer?.release()
        exoPlayer = null
        audioScope.cancel()
    }
}
