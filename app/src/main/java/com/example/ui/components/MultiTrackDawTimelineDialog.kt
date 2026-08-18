package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.SongEntity
import kotlinx.coroutines.delay
import kotlin.math.sin

data class DawTrack(
    val id: Int,
    val name: String,
    val bengaliName: String,
    val iconName: String,
    val color: Color,
    var volume: Float = 0.85f,
    var pan: Float = 0.0f, // -1.0 Left to +1.0 Right
    var isMuted: Boolean = false,
    var isSolo: Boolean = false,
    var fxReverb: Float = 0.3f,
    var isArmed: Boolean = false
)

@Composable
fun MultiTrackDawTimelineDialog(
    song: SongEntity,
    onDismiss: () -> Unit,
    onExportMix: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var isDawPlaying by remember { mutableStateOf(true) }
    var currentPlayheadSec by remember { mutableFloatStateOf(15.0f) }
    val totalLengthSec = 180f // 3:00 min

    var isLoopingAB by remember { mutableStateOf(false) }
    var loopStartSec by remember { mutableFloatStateOf(10f) }
    var loopEndSec by remember { mutableFloatStateOf(45f) }

    var masterVolume by remember { mutableFloatStateOf(0.90f) }
    var masterLimiterActive by remember { mutableStateOf(true) }
    var selectedTool by remember { mutableStateOf("pointer") } // "pointer", "cut", "duplicate", "mute_region"

    var tracks by remember {
        mutableStateOf(
            listOf(
                DawTrack(1, "Track 1: Lead Vocals", "🎤 প্রধান কন্ঠ (Aria)", "Mic", Color(0xFFEC4899), volume = 0.9f, pan = 0.0f),
                DawTrack(2, "Track 2: Dotara & Guitar", "🎸 দোতারা ও গিটার", "Guitar", Color(0xFFF59E0B), volume = 0.8f, pan = -0.25f),
                DawTrack(3, "Track 3: Tabla & 808 Drums", "🥁 তবলা ও ড্রাম বিট", "Drums", Color(0xFF10B981), volume = 0.85f, pan = 0.0f),
                DawTrack(4, "Track 4: Synth Pad & Flute", "🎹 সিন্থ ও বাঁশি অ্যাম্বিয়েন্স", "Synth", Color(0xFF38BDF8), volume = 0.7f, pan = 0.35f)
            )
        )
    }

    // Playhead animation
    LaunchedEffect(isDawPlaying) {
        while (isDawPlaying) {
            delay(100)
            currentPlayheadSec += 0.2f
            if (isLoopingAB && currentPlayheadSec > loopEndSec) {
                currentPlayheadSec = loopStartSec
            } else if (currentPlayheadSec > totalLengthSec) {
                currentPlayheadSec = 0f
            }
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
            border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            color = Color(0xFF8B5CF6),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        Column {
                            Text(
                                text = "মাল্টি-ট্র্যাক অডিও DAW ও টাইমলাইন মিক্সার",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Professional Multi-Stem Track Arranger & Master Bus",
                                fontSize = 11.sp,
                                color = Color(0xFFA78BFA)
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

                // Top DAW Toolbar (Transport + Tools + Loop + Time)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Play/Pause & Stop
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { isDawPlaying = !isDawPlaying },
                                modifier = Modifier.size(36.dp).background(Color(0xFF8B5CF6), CircleShape)
                            ) {
                                Icon(if (isDawPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            IconButton(
                                onClick = {
                                    isDawPlaying = false
                                    currentPlayheadSec = 0f
                                },
                                modifier = Modifier.size(36.dp).background(Color.White.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }

                        // Timecode Display
                        val curMin = (currentPlayheadSec / 60).toInt()
                        val curSec = (currentPlayheadSec % 60).toInt()
                        val curMillis = ((currentPlayheadSec - curMin * 60 - curSec) * 10).toInt()
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F172A),
                            border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = String.format("%02d:%02d.%01d / 03:00", curMin, curSec, curMillis),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        // Loop A-B button
                        FilterChip(
                            selected = isLoopingAB,
                            onClick = { isLoopingAB = !isLoopingAB },
                            label = { Text("A-B Loop", fontSize = 10.sp) },
                            leadingIcon = { Icon(Icons.Default.Repeat, contentDescription = null, modifier = Modifier.size(12.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFF59E0B),
                                selectedLabelColor = Color.Black
                            ),
                            modifier = Modifier.height(30.dp)
                        )

                        // Tools selector (Cut / Duplicate / Pointer)
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            listOf(
                                "pointer" to Icons.Default.TouchApp,
                                "cut" to Icons.Default.ContentCut,
                                "duplicate" to Icons.Default.ContentCopy
                            ).forEach { (tool, icon) ->
                                val isSel = selectedTool == tool
                                IconButton(
                                    onClick = {
                                        selectedTool = tool
                                        Toast.makeText(context, "Tool: $tool active", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(if (isSel) Color(0xFF8B5CF6) else Color.Transparent, RoundedCornerShape(6.dp))
                                ) {
                                    Icon(icon, contentDescription = tool, tint = if (isSel) Color.White else Color.Gray, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                // Global Timeline Scrub Bar Canvas
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF131D31),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                                currentPlayheadSec = fraction * totalLengthSec
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height

                        // Draw time ticks
                        for (i in 0..12) {
                            val x = (i / 12f) * width
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.3f),
                                start = Offset(x, 0f),
                                end = Offset(x, height),
                                strokeWidth = 1f
                            )
                        }

                        // Draw Loop region if active
                        if (isLoopingAB) {
                            val loopStartX = (loopStartSec / totalLengthSec) * width
                            val loopEndX = (loopEndSec / totalLengthSec) * width
                            drawRect(
                                color = Color(0xFFF59E0B).copy(alpha = 0.25f),
                                topLeft = Offset(loopStartX, 0f),
                                size = Size(loopEndX - loopStartX, height)
                            )
                        }

                        // Draw Playhead
                        val playheadX = (currentPlayheadSec / totalLengthSec) * width
                        drawLine(
                            color = Color(0xFFEF4444),
                            start = Offset(playheadX, 0f),
                            end = Offset(playheadX, height),
                            strokeWidth = 3f
                        )
                        drawCircle(
                            color = Color(0xFFEF4444),
                            radius = 6f,
                            center = Offset(playheadX, height / 2)
                        )
                    }
                }

                // 4 Interactive Multi-Track Strips
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tracks) { track ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF131D31),
                            border = BorderStroke(1.dp, track.color.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                // Track Info Header & Mute/Solo
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(shape = CircleShape, color = track.color, modifier = Modifier.size(10.dp)) {}
                                        Text(track.bengaliName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                    }

                                    // Mute, Solo, Arm Buttons
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        // Mute
                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable {
                                                    tracks = tracks.map {
                                                        if (it.id == track.id) it.copy(isMuted = !it.isMuted) else it
                                                    }
                                                },
                                            color = if (track.isMuted) Color(0xFFEF4444) else Color.White.copy(alpha = 0.1f)
                                        ) {
                                            Text(
                                                "M",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                color = if (track.isMuted) Color.White else Color.Gray,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        // Solo
                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable {
                                                    tracks = tracks.map {
                                                        if (it.id == track.id) it.copy(isSolo = !it.isSolo) else it
                                                    }
                                                },
                                            color = if (track.isSolo) Color(0xFFF59E0B) else Color.White.copy(alpha = 0.1f)
                                        ) {
                                            Text(
                                                "S",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                color = if (track.isSolo) Color.Black else Color.Gray,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                // Interactive Waveform Canvas Block for Track
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF0B0F19),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp)
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val width = size.width
                                        val height = size.height
                                        val barCount = 60
                                        val barWidth = width / barCount

                                        for (i in 0 until barCount) {
                                            val x = i * barWidth
                                            val factor = sin((i * 0.35 + track.id).toDouble()).toFloat().coerceIn(0.1f, 1f)
                                            val barH = height * 0.7f * factor * (if (track.isMuted) 0.15f else track.volume)
                                            drawRect(
                                                color = if (track.isMuted) Color.Gray.copy(alpha = 0.3f) else track.color.copy(alpha = 0.75f),
                                                topLeft = Offset(x, (height - barH) / 2),
                                                size = Size(barWidth * 0.7f, barH)
                                            )
                                        }

                                        // Track Playhead line
                                        val playheadX = (currentPlayheadSec / totalLengthSec) * width
                                        drawLine(
                                            color = Color.White.copy(alpha = 0.8f),
                                            start = Offset(playheadX, 0f),
                                            end = Offset(playheadX, height),
                                            strokeWidth = 2f
                                        )
                                    }
                                }

                                // Volume & Pan Controls Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Volume Slider
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                                        Slider(
                                            value = track.volume,
                                            onValueChange = { newVol ->
                                                tracks = tracks.map {
                                                    if (it.id == track.id) it.copy(volume = newVol) else it
                                                }
                                            },
                                            modifier = Modifier.height(18.dp),
                                            colors = SliderDefaults.colors(
                                                thumbColor = track.color,
                                                activeTrackColor = track.color
                                            )
                                        )
                                    }

                                    // Pan Indicator
                                    val panText = when {
                                        track.pan < -0.1f -> "L ${(-track.pan * 50).toInt()}"
                                        track.pan > 0.1f -> "R ${(track.pan * 50).toInt()}"
                                        else -> "C"
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color.White.copy(alpha = 0.08f)
                                    ) {
                                        Text("Pan: $panText", fontSize = 9.sp, color = Color.LightGray, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Master Bus & Export Section
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🎚️ মাস্টার বাস (Master Bus 320kbps Limiter)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6))
                            Text("${(masterVolume * 100).toInt()}% Gain", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Slider(
                                value = masterVolume,
                                onValueChange = { masterVolume = it },
                                modifier = Modifier.weight(1f).height(20.dp),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF8B5CF6),
                                    activeTrackColor = Color(0xFF8B5CF6)
                                )
                            )

                            Button(
                                onClick = {
                                    Toast.makeText(context, "স্টুডিও মাস্টার মিক্স ৩২0kbps MP3 ও Lossless WAV হিসেবে এক্সপোর্ট হচ্ছে...", Toast.LENGTH_LONG).show()
                                    onExportMix("Master_Mix_${song.id}.wav")
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                            ) {
                                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Export Master Mix", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
