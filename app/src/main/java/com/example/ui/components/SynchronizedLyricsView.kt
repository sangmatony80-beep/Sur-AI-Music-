package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.audio.LyricsCue
import com.example.data.audio.SynchronizedLyricsService
import com.example.data.audio.SynchronizedLyricsState
import com.example.data.local.SongEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SynchronizedLyricsView(
    song: SongEntity,
    playbackProgress: Float,
    durationSeconds: Int,
    isPlaying: Boolean,
    onSeekToProgress: (Float) -> Unit,
    modifier: Modifier = Modifier,
    onClose: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lyricsService = remember { SynchronizedLyricsService() }

    var lyricsState by remember { mutableStateOf<SynchronizedLyricsState?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showTranslations by remember { mutableStateOf(true) }
    var karaokeMode by remember { mutableStateOf(true) }
    var fontSizeModifier by remember { mutableFloatStateOf(0f) }

    val listState = rememberLazyListState()

    // Calculate current playback in ms
    val totalDurationMs = (durationSeconds * 1000L).coerceAtLeast(30000L)
    val currentPositionMs = (playbackProgress * totalDurationMs).toLong()

    // Load lyrics from Supabase metadata or aligned service
    LaunchedEffect(song.id, song.cloudId) {
        isLoading = true
        val result = lyricsService.getSynchronizedLyrics(song, totalDurationMs)
        lyricsState = result
        isLoading = false
    }

    val cues = lyricsState?.cues ?: emptyList()
    val activeIndex = remember(cues, currentPositionMs) {
        lyricsService.getActiveCueIndex(cues, currentPositionMs)
    }

    // Auto-scroll to keep active lyric line centered
    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0 && activeIndex < cues.size) {
            val targetScrollIndex = (activeIndex - 2).coerceAtLeast(0)
            listState.animateScrollToItem(targetScrollIndex)
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFF0B0F19)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Ambient glowing background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                Color(0xFF0F172A),
                                Color(0xFF050811)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF10B981).copy(alpha = 0.2f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Synced Lyrics & Karaoke",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            val sourceLabel = when (lyricsState?.source) {
                                "SUPABASE_METADATA" -> "☁️ Supabase Cloud Metadata"
                                "SUPABASE_LRC" -> "🎵 Supabase Synced Stream"
                                "LOCAL_LRC" -> "📄 Embedded LRC"
                                else -> "✨ AI Real-Time Synchronized"
                            }
                            Text(
                                text = sourceLabel,
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Translation toggle
                        IconButton(onClick = { showTranslations = !showTranslations }) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "Toggle Translation",
                                tint = if (showTranslations) Color(0xFF38BDF8) else Color.Gray,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Copy Lyrics
                        IconButton(onClick = {
                            val fullLyrics = cues.joinToString("\n") { it.text }
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Lyrics", "${song.title} - ${song.artist}\n\n$fullLyrics")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "লিরিক্স ক্লিপবোর্ডে কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Lyrics",
                                tint = Color.LightGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Close button if modal
                        if (onClose != null) {
                            IconButton(onClick = onClose) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Track Mini Banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.05f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White,
                                maxLines = 1
                            )
                            Text(
                                text = "${song.artist} • ট্যাপ করে লাইনে যান",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8),
                                maxLines = 1
                            )
                        }

                        // Karaoke Glow indicator
                        if (isPlaying) {
                            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                            val glowAlpha by infiniteTransition.animateFloat(
                                initialValue = 0.4f,
                                targetValue = 1.0f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(800, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "glow"
                            )
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFFEC4899).copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEC4899).copy(alpha = glowAlpha))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEC4899))
                                    )
                                    Text(
                                        text = "LIVE SYNC",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFEC4899)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Lyrics Content List
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "Supabase থেকে সিঙ্কড লিরিক্স লোড হচ্ছে...",
                                fontSize = 13.sp,
                                color = Color.LightGray
                            )
                        }
                    }
                } else if (cues.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "কোনো সিঙ্কড লিরিক্স পাওয়া যায়নি",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                        contentPadding = PaddingValues(vertical = 40.dp)
                    ) {
                        itemsIndexed(cues) { index, cue ->
                            val isActive = index == activeIndex
                            val isPast = index < activeIndex

                            // Active line progress
                            val cueProgress = if (isActive) {
                                lyricsService.getCueProgress(cue, currentPositionMs)
                            } else 0f

                            SynchronizedLyricItem(
                                cue = cue,
                                isActive = isActive,
                                isPast = isPast,
                                cueProgress = cueProgress,
                                showTranslations = showTranslations,
                                fontSizeModifier = fontSizeModifier,
                                onClick = {
                                    val targetProgress = (cue.timeMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
                                    onSeekToProgress(targetProgress)
                                    Toast.makeText(context, "⏩ Jumped to [${formatMs(cue.timeMs)}]", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Karaoke Controls (Font scale & Seeking bar)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatMs(currentPositionMs),
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = { fontSizeModifier = (fontSizeModifier - 2f).coerceAtLeast(-4f) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text("A-", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(
                            onClick = { fontSizeModifier = (fontSizeModifier + 2f).coerceAtMost(8f) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text("A+", color = Color.LightGray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = formatMs(totalDurationMs),
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun SynchronizedLyricItem(
    cue: LyricsCue,
    isActive: Boolean,
    isPast: Boolean,
    cueProgress: Float,
    showTranslations: Boolean,
    fontSizeModifier: Float,
    onClick: () -> Unit
) {
    val targetScale = if (isActive) 1.05f else 1.0f
    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    val textColor = when {
        isActive -> Color.White
        isPast -> Color(0xFF64748B)
        else -> Color(0xFF475569)
    }

    val baseFontSize = if (cue.isChorus) (20f + fontSizeModifier) else (17f + fontSizeModifier)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .background(
                if (isActive) Color.White.copy(alpha = 0.08f) else Color.Transparent
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = if (cue.isChorus) Alignment.CenterHorizontally else Alignment.Start
    ) {
        // Tag for Chorus
        if (cue.isChorus) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isActive) Color(0xFFF59E0B).copy(alpha = 0.3f) else Color.DarkGray.copy(alpha = 0.3f),
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Text(
                    text = "CHORUS / স্থায়ী",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) Color(0xFFFBBF24) else Color.Gray,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // Main Lyric Line with active karaoke highlight
        Text(
            text = cue.text,
            fontSize = baseFontSize.sp,
            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
            color = textColor,
            textAlign = if (cue.isChorus) TextAlign.Center else TextAlign.Start,
            lineHeight = (baseFontSize + 8).sp,
            modifier = Modifier.fillMaxWidth()
        )

        // Progress bar inside active cue
        if (isActive) {
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(cueProgress)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF38BDF8), Color(0xFFEC4899))
                            )
                        )
                )
            }
        }

        // Phonetic or Translation line
        if (showTranslations && cue.translationBn.isNotEmpty() && !cue.text.startsWith("🎵") && !cue.text.startsWith("✨")) {
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = cue.translationBn,
                fontSize = (baseFontSize - 5).coerceAtLeast(10f).sp,
                color = if (isActive) Color(0xFF38BDF8).copy(alpha = 0.9f) else Color(0xFF64748B),
                textAlign = if (cue.isChorus) TextAlign.Center else TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
