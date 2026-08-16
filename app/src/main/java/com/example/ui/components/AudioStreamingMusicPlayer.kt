package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.SongEntity
import kotlinx.coroutines.delay

/**
 * Repeat modes for audio stream playback.
 */
enum class AudioRepeatMode {
    OFF, ALL, ONE
}

/**
 * Comprehensive Audio Streaming Music Player UI Component built with Jetpack Compose.
 *
 * Includes:
 * - Play, Pause with buffering & streaming indicator
 * - Skip Next and Skip Previous controls
 * - Fast-forward (+10s) and Rewind (-10s) seek actions
 * - Interactive Audio Streaming Progress Bar with buffered cache line & dynamic timestamps
 * - Real-time reactive Audio Spectrum / Waveform Visualizer
 * - Streaming bitrate / audio quality badge
 * - Volume controls with mute toggle
 * - Shuffle & Loop toggles
 * - Accessible 48dp touch targets and Compose testTags for automated testing
 */
@Composable
fun AudioStreamingMusicPlayer(
    song: SongEntity?,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    modifier: Modifier = Modifier,
    isBuffering: Boolean = false,
    bufferedProgress: Float = 0.85f,
    onFavoriteClick: (() -> Unit)? = null,
    onShareClick: (() -> Unit)? = null,
    onCloseClick: (() -> Unit)? = null
) {
    if (song == null) {
        EmptyPlayerPlaceholder(modifier = modifier)
        return
    }

    // Local state for smooth seeking & streaming playback time calculation
    var progress by remember(song.id) { mutableFloatStateOf(0.15f) }
    var isUserDraggingSlider by remember { mutableStateOf(false) }
    var isShuffleEnabled by remember { mutableStateOf(false) }
    var repeatMode by remember { mutableStateOf(AudioRepeatMode.OFF) }
    var volume by remember { mutableFloatStateOf(0.85f) }
    var isMuted by remember { mutableStateOf(false) }
    var showVolumeControl by remember { mutableStateOf(false) }

    // Parse total duration in seconds from song duration string (e.g. "3:45")
    val totalSeconds = remember(song.duration) {
        val parts = song.duration.split(":")
        if (parts.size == 2) {
            (parts[0].toIntOrNull() ?: 3) * 60 + (parts[1].toIntOrNull() ?: 30)
        } else 210
    }

    // Auto-advance stream playback progress when playing and not user-dragging
    LaunchedEffect(isPlaying, isBuffering, isUserDraggingSlider, song.id) {
        if (isPlaying && !isBuffering && !isUserDraggingSlider) {
            while (true) {
                delay(1000L)
                val step = 1.0f / totalSeconds.coerceAtLeast(1)
                val next = progress + step
                if (next >= 1.0f) {
                    when (repeatMode) {
                        AudioRepeatMode.ONE -> progress = 0f
                        AudioRepeatMode.ALL -> {
                            progress = 0f
                            onSkipNext()
                        }
                        AudioRepeatMode.OFF -> {
                            progress = 1.0f
                            onSkipNext()
                        }
                    }
                } else {
                    progress = next
                }
            }
        }
    }

    val currentSeconds = (progress * totalSeconds).toInt()
    val formattedCurrentTime = String.format("%02d:%02d", currentSeconds / 60, currentSeconds % 60)
    val formattedTotalTime = song.duration.ifBlank {
        String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60)
    }

    // Animated artwork pulse when playing
    val infiniteTransition = rememberInfiniteTransition(label = "player_art_anim")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("audio_streaming_player_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A).copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                            Color(0xFF0F172A).copy(alpha = 0.85f),
                            Color(0xFF090D16)
                        )
                    )
                )
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Streaming Status & Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Streaming Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isPlaying) Color(0xFF10B981).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isPlaying) Color(0xFF10B981).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isPlaying) Color(0xFF10B981) else Color(0xFF94A3B8))
                        )
                        Text(
                            text = if (isBuffering) "BUFFERING..." else if (isPlaying) "LIVE HQ AUDIO" else "STREAM READY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isPlaying) Color(0xFF10B981) else Color(0xFF94A3B8),
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Bitrate / Quality Tag
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ) {
                    Text(
                        text = "320 kbps • Dolby AI",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Action icons: Favorite / Share
                Row {
                    if (onFavoriteClick != null) {
                        IconButton(
                            onClick = onFavoriteClick,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("favorite_button")
                        ) {
                            Icon(
                                imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (song.isFavorite) Color(0xFFEC4899) else Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    if (onShareClick != null) {
                        IconButton(
                            onClick = onShareClick,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("share_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    if (onCloseClick != null) {
                        IconButton(
                            onClick = onCloseClick,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Album Artwork with dynamic glowing aura
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .shadow(
                        elevation = if (isPlaying) 16.dp else 4.dp,
                        shape = RoundedCornerShape(24.dp),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = if (isPlaying) glowAlpha else 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = song.imageUrl,
                    contentDescription = song.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Crop
                )

                // Buffering Overlay Spinner
                if (isBuffering) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.55f))
                            .clip(RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Track Title & Artist Metadata
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${song.artist} • ${song.genre}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Real-Time Waveform Visualizer
            InteractiveWaveformVisualizer(
                progress = progress,
                isPlaying = isPlaying && !isBuffering,
                onSeek = { newProgress ->
                    progress = newProgress
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Audio Streaming Progress Bar & Buffer Track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("audio_progress_bar_container")
            ) {
                // Background Buffered Stream Track
                Slider(
                    value = progress,
                    onValueChange = {
                        isUserDraggingSlider = true
                        progress = it
                    },
                    onValueChangeFinished = {
                        isUserDraggingSlider = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("audio_progress_bar"),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                    )
                )
            }

            // Timestamp indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formattedCurrentTime,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.testTag("current_time_text")
                )
                Text(
                    text = formattedTotalTime,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.testTag("total_time_text")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Playback Controls (Shuffle, Previous, 10s Rewind, Play/Pause, 10s Forward, Next, Repeat)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle Toggle
                IconButton(
                    onClick = { isShuffleEnabled = !isShuffleEnabled },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("shuffle_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffleEnabled) MaterialTheme.colorScheme.primary else Color(0xFF64748B),
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Skip Previous Track
                IconButton(
                    onClick = {
                        progress = 0f
                        onSkipPrevious()
                    },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("skip_previous_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Track",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Quick Rewind 10s
                IconButton(
                    onClick = {
                        val step = 10f / totalSeconds.coerceAtLeast(1)
                        progress = (progress - step).coerceIn(0f, 1f)
                    },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("rewind_10s_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay10,
                        contentDescription = "Rewind 10 seconds",
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Main Play / Pause Button with Floating Style
                FloatingActionButton(
                    onClick = onPlayPauseClick,
                    modifier = Modifier
                        .size(64.dp)
                        .testTag("play_pause_button"),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                ) {
                    if (isBuffering) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 3.dp
                        )
                    } else {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                // Quick Forward 10s
                IconButton(
                    onClick = {
                        val step = 10f / totalSeconds.coerceAtLeast(1)
                        progress = (progress + step).coerceIn(0f, 1f)
                    },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("forward_10s_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Forward10,
                        contentDescription = "Forward 10 seconds",
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Skip Next Track
                IconButton(
                    onClick = {
                        progress = 0f
                        onSkipNext()
                    },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("skip_next_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Track",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Repeat Mode Toggle (OFF -> ALL -> ONE)
                IconButton(
                    onClick = {
                        repeatMode = when (repeatMode) {
                            AudioRepeatMode.OFF -> AudioRepeatMode.ALL
                            AudioRepeatMode.ALL -> AudioRepeatMode.ONE
                            AudioRepeatMode.ONE -> AudioRepeatMode.OFF
                        }
                    },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("repeat_button")
                ) {
                    Icon(
                        imageVector = when (repeatMode) {
                            AudioRepeatMode.ONE -> Icons.Default.RepeatOne
                            AudioRepeatMode.ALL -> Icons.Default.Repeat
                            AudioRepeatMode.OFF -> Icons.Default.Repeat
                        },
                        contentDescription = "Repeat Mode",
                        tint = if (repeatMode != AudioRepeatMode.OFF) MaterialTheme.colorScheme.primary else Color(0xFF64748B),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Volume Controls Bar (Expandable or inline)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Mute / Volume Icon
                IconButton(
                    onClick = { isMuted = !isMuted },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("volume_button")
                ) {
                    Icon(
                        imageVector = when {
                            isMuted || volume == 0f -> Icons.Default.VolumeOff
                            volume < 0.5f -> Icons.Default.VolumeDown
                            else -> Icons.Default.VolumeUp
                        },
                        contentDescription = "Volume",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Volume slider
                Slider(
                    value = if (isMuted) 0f else volume,
                    onValueChange = {
                        volume = it
                        if (isMuted && it > 0f) isMuted = false
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .testTag("volume_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF94A3B8),
                        activeTrackColor = Color(0xFF38BDF8),
                        inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                    )
                )

                Text(
                    text = "${((if (isMuted) 0f else volume) * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.width(36.dp),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

/**
 * Compact version of the Streaming Player component for embedding in list screens or headers.
 */
@Composable
fun CompactAudioStreamingPlayer(
    song: SongEntity?,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onExpandPlayer: () -> Unit,
    modifier: Modifier = Modifier,
    isBuffering: Boolean = false
) {
    if (song == null) return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onExpandPlayer() }
            .testTag("compact_audio_streaming_player"),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.96f),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album Art
                AsyncImage(
                    model = song.imageUrl,
                    contentDescription = song.title,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Title & Artist
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isPlaying) Color(0xFF10B981) else Color(0xFF94A3B8))
                        )
                    }
                    Text(
                        text = "${song.artist} • 320 kbps Stream",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Skip Prev
                IconButton(
                    onClick = onSkipPrevious,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("compact_skip_prev")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Play / Pause
                FilledIconButton(
                    onClick = onPlayPauseClick,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("compact_play_pause"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isBuffering) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Skip Next
                IconButton(
                    onClick = onSkipNext,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("compact_skip_next")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Bottom subtle live progress indicator
            LinearProgressIndicator(
                progress = { 0.35f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.White.copy(alpha = 0.1f)
            )
        }
    }
}

@Composable
private fun EmptyPlayerPlaceholder(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No Track Selected",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Select an AI song or audio stream to start playback",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
