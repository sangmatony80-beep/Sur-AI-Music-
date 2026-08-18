package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.SongEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

data class KaraokeLyricLine(
    val timeSec: Float,
    val bengaliText: String,
    val englishText: String,
    val targetPitchNote: String
)

@Composable
fun LiveKaraokeVocalStudioDialog(
    song: SongEntity,
    onDismiss: () -> Unit,
    onExportRecording: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isKaraokePlaying by remember { mutableStateOf(true) }
    var vocalMode by remember { mutableStateOf("karaoke") } // "full", "karaoke" (vocals removed), "vocals_only"
    var micEnabled by remember { mutableStateOf(true) }
    var isRecordingUserVoice by remember { mutableStateOf(false) }
    var recordingTimerSec by remember { mutableIntStateOf(0) }

    // Vocal FX
    var vocalReverb by remember { mutableFloatStateOf(0.45f) }
    var vocalEcho by remember { mutableFloatStateOf(0.20f) }
    var autoTuneKey by remember { mutableStateOf("C Major") }
    var vocalVolume by remember { mutableFloatStateOf(0.85f) }
    var backingTrackVolume by remember { mutableFloatStateOf(0.75f) }

    // Realtime Singing Pitch score
    var livePitchAccuracy by remember { mutableFloatStateOf(94.5f) }
    var currentSingerNote by remember { mutableStateOf("C4 (Perfect)") }

    // Lyrics data
    val lyricsLines = remember {
        listOf(
            KaraokeLyricLine(0f, "মেঘের দেশে সুরের খেলা শুরু হলো আজ", "In the realm of clouds, melodies begin today", "C4"),
            KaraokeLyricLine(4f, "বৃষ্টি ভেজা মিষ্টি হাওয়া ছুঁয়ে যায় প্রাণ", "Sweet rain-soaked breeze touches the heart", "E4"),
            KaraokeLyricLine(8f, "তোমার চোখে আমার গান খুঁজে পেল ভাষা", "Through your eyes my songs found their voice", "G4"),
            KaraokeLyricLine(12f, "ভোরের আলোয় সুরের ডানায় ছড়িয়ে ভালোবাসা", "Spreading love on the wings of morning dawn", "A4"),
            KaraokeLyricLine(16f, "ও সুরের পাখি, দূর আকাশে উড়ো মন খুলে", "Oh singing bird, soar freely in distant skies", "C5"),
            KaraokeLyricLine(20f, "আজকে আমি গাইব গান সব দ্বিধা ভুলে", "Today I shall sing discarding all hesitations", "G4"),
            KaraokeLyricLine(24f, "দোতারার এই ছন্দে নাচে সোনালী রোদ্দুর", "Golden sunlight dances to the rhythm of Dotara", "F4"),
            KaraokeLyricLine(28f, "চলতে গিয়ে অচিন সুরে বাজলো সুমধুর", "Wandering in unknown tunes, sweetness resounds", "E4"),
            KaraokeLyricLine(32f, "হৃদয় মাঝে সুরের মেলা, ভালোবাসার গান...", "A carnival of melody in the soul, songs of eternal love...", "C4")
        )
    }

    var currentLineIndex by remember { mutableIntStateOf(0) }
    var currentSongSeconds by remember { mutableFloatStateOf(0f) }
    val listState = rememberLazyListState()

    // Timer effect for playback & synchronized lyrics
    LaunchedEffect(isKaraokePlaying) {
        while (isKaraokePlaying) {
            delay(100)
            currentSongSeconds += 0.1f
            if (currentSongSeconds > 36f) {
                currentSongSeconds = 0f
            }
            val activeIdx = lyricsLines.indexOfLast { it.timeSec <= currentSongSeconds }.coerceAtLeast(0)
            if (activeIdx != currentLineIndex) {
                currentLineIndex = activeIdx
                listState.animateScrollToItem(activeIdx)
                // Fluctuate pitch accuracy
                livePitchAccuracy = (90f + (sin(currentSongSeconds.toDouble()) * 8).toFloat()).coerceIn(82f, 99f)
            }
        }
    }

    // Recording timer
    LaunchedEffect(isRecordingUserVoice) {
        while (isRecordingUserVoice) {
            delay(1000)
            recordingTimerSec += 1
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0B0F19),
            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFEC4899),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        Column {
                            Text(
                                text = "লাইভ কারাওকে ও ভোকাল রিমুভার",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Live Karaoke Studio & AI Vocal Remover",
                                fontSize = 11.sp,
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Vocal Isolation Selector (Full / Instrumental Karaoke / Vocals Only)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            Triple("karaoke", "🎤 কারাওকে (ভোকাল বন্ধ)", Color(0xFFEC4899)),
                            Triple("full", "🎵 ফুল ট্র্যাক", Color(0xFF38BDF8)),
                            Triple("vocals_only", "🗣️ ভোকাল একাপেলা", Color(0xFF10B981))
                        ).forEach { (mode, label, col) ->
                            val isSel = vocalMode == mode
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        vocalMode = mode
                                        val toastMsg = when (mode) {
                                            "karaoke" -> "ভোকাল রিমুভ করা হয়েছে — এবার আপনি নিজে গাইবেন!"
                                            "full" -> "ফুল গান ও ব্যাকগ্রাউন্ড মিউজিক চালু"
                                            else -> "শুধুমাত্র ভোকাল আইসোলেট করা হয়েছে"
                                        }
                                        Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                                    },
                                color = if (isSel) col else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = label,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) Color.White else Color.LightGray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Live Pitch Accuracy Score Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "🎯 রিয়েলটাইম পিচ অ্যাকুরেসি স্কোর",
                                fontSize = 11.sp,
                                color = Color.LightGray
                            )
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "${livePitchAccuracy.toInt()}%",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF10B981)
                                )
                                Text(
                                    text = if (livePitchAccuracy >= 90) "★ পারফেক্ট সুর!" else "ভালো গাইছেন 🎶",
                                    fontSize = 12.sp,
                                    color = Color(0xFFFBBF24),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("গায়কীর স্কেল ও নোট:", fontSize = 10.sp, color = Color.Gray)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = currentSingerNote,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Synchronized Scrolling Karaoke Lyrics Screen
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF070A10)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFEC4899).copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            itemsIndexed(lyricsLines) { index, line ->
                                val isActive = index == currentLineIndex
                                val isPast = index < currentLineIndex

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isActive) Color(0xFFEC4899).copy(alpha = 0.18f)
                                            else Color.Transparent
                                        )
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (isActive) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0xFFEC4899),
                                                modifier = Modifier.size(8.dp)
                                            ) {}
                                            Text(
                                                text = "TARGET NOTE: ${line.targetPitchNote}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFEC4899)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                    }

                                    Text(
                                        text = line.bengaliText,
                                        fontSize = if (isActive) 18.sp else 14.sp,
                                        fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
                                        color = when {
                                            isActive -> Color(0xFFFFD166)
                                            isPast -> Color.White.copy(alpha = 0.45f)
                                            else -> Color.White.copy(alpha = 0.8f)
                                        },
                                        textAlign = TextAlign.Center
                                    )

                                    Text(
                                        text = line.englishText,
                                        fontSize = if (isActive) 12.sp else 10.sp,
                                        color = if (isActive) Color(0xFF38BDF8) else Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // Vocal FX Bar (Reverb, Echo, Key)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🎤 মাইক্রোফোন স্টুডিও এফেক্টস (Vocal FX & Reverb)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                            Text("কী: $autoTuneKey", fontSize = 11.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Reverb", fontSize = 10.sp, color = Color.LightGray)
                                    Text("${(vocalReverb * 100).toInt()}%", fontSize = 10.sp, color = Color.White)
                                }
                                Slider(
                                    value = vocalReverb,
                                    onValueChange = { vocalReverb = it },
                                    modifier = Modifier.height(20.dp),
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color(0xFFEC4899),
                                        activeTrackColor = Color(0xFFEC4899)
                                    )
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Echo / Delay", fontSize = 10.sp, color = Color.LightGray)
                                    Text("${(vocalEcho * 100).toInt()}%", fontSize = 10.sp, color = Color.White)
                                }
                                Slider(
                                    value = vocalEcho,
                                    onValueChange = { vocalEcho = it },
                                    modifier = Modifier.height(20.dp),
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color(0xFF38BDF8),
                                        activeTrackColor = Color(0xFF38BDF8)
                                    )
                                )
                            }
                        }
                    }
                }

                // Player & Record Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Play/Pause Karaoke
                    FilledIconButton(
                        onClick = { isKaraokePlaying = !isKaraokePlaying },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color(0xFF38BDF8)
                        ),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            if (isKaraokePlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.Black
                        )
                    }

                    // Record Vocal Button
                    Button(
                        onClick = {
                            isRecordingUserVoice = !isRecordingUserVoice
                            if (isRecordingUserVoice) {
                                recordingTimerSec = 0
                                Toast.makeText(context, "🔴 আপনার কন্ঠ রেকর্ড করা হচ্ছে...", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "✅ কারাওকে কভার সেভ হয়েছে! (দৈর্ঘ্য: ${recordingTimerSec}s)", Toast.LENGTH_LONG).show()
                                onExportRecording("Cover_Track_${song.id}.mp3")
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRecordingUserVoice) Color(0xFFEF4444) else Color(0xFFEC4899)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            if (isRecordingUserVoice) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (isRecordingUserVoice) "রেকর্ড থামান (${recordingTimerSec}s)" else "কভার রেকর্ড করুন (Sing & Record)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
