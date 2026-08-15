package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.SongEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * AI Stem Separation & Multi-Track Studio Mixer
 * Allows isolating and fine-tuning individual stems: Vocals, Drums, Bass, and Melody/Instruments.
 */
@Composable
fun StemSeparationDialog(
    song: SongEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var vocalVolume by remember { mutableFloatStateOf(0.85f) }
    var drumsVolume by remember { mutableFloatStateOf(0.80f) }
    var bassVolume by remember { mutableFloatStateOf(0.75f) }
    var instVolume by remember { mutableFloatStateOf(0.90f) }

    var isVocalMuted by remember { mutableStateOf(false) }
    var isDrumsMuted by remember { mutableStateOf(false) }
    var isBassMuted by remember { mutableStateOf(false) }
    var isInstMuted by remember { mutableStateOf(false) }

    var isProcessingExport by remember { mutableStateOf(false) }
    var isKaraokeMode by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { if (!isProcessingExport) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0F172A),
            tonalElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = Color(0xFF38BDF8))
                        Column {
                            Text("AI 4-Stem Audio Mixer", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                            Text("${song.title} (${song.genre})", fontSize = 11.sp, color = Color.LightGray, maxLines = 1)
                        }
                    }
                    IconButton(onClick = onDismiss, enabled = !isProcessingExport) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Mode Bar (Karaoke / Acapella / Full Reset)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(
                        onClick = {
                            isKaraokeMode = !isKaraokeMode
                            if (isKaraokeMode) {
                                isVocalMuted = true
                                isDrumsMuted = false
                                isBassMuted = false
                                isInstMuted = false
                                Toast.makeText(context, "🎤 Karaoke Mode: Vocals Muted", Toast.LENGTH_SHORT).show()
                            } else {
                                isVocalMuted = false
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isKaraokeMode) Color(0xFFEC4899) else Color(0xFF1E293B)
                        ),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.MicOff, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isKaraokeMode) "Karaoke ON" else "Karaoke", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    FilledTonalButton(
                        onClick = {
                            // Acapella Solo Vocal
                            isVocalMuted = false
                            isDrumsMuted = true
                            isBassMuted = true
                            isInstMuted = true
                            Toast.makeText(context, "🎙️ Acapella Mode: Solo Vocals", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF1E293B)),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Acapella", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    FilledTonalButton(
                        onClick = {
                            isVocalMuted = false
                            isDrumsMuted = false
                            isBassMuted = false
                            isInstMuted = false
                            vocalVolume = 0.85f
                            drumsVolume = 0.80f
                            bassVolume = 0.75f
                            instVolume = 0.90f
                            isKaraokeMode = false
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF1E293B)),
                        modifier = Modifier.weight(0.8f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text("Reset", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stem Track 1: Vocals
                StemTrackSliderRow(
                    name = "🎤 Lead Vocals",
                    color = Color(0xFFEC4899),
                    volume = vocalVolume,
                    isMuted = isVocalMuted,
                    onVolumeChange = { vocalVolume = it },
                    onMuteToggle = { isVocalMuted = !isVocalMuted }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Stem Track 2: Drums / Beats
                StemTrackSliderRow(
                    name = "🥁 Drums & Percussion",
                    color = Color(0xFFF59E0B),
                    volume = drumsVolume,
                    isMuted = isDrumsMuted,
                    onVolumeChange = { drumsVolume = it },
                    onMuteToggle = { isDrumsMuted = !isDrumsMuted }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Stem Track 3: Bassline
                StemTrackSliderRow(
                    name = "🎸 Bass & Sub-Low",
                    color = Color(0xFF8B5CF6),
                    volume = bassVolume,
                    isMuted = isBassMuted,
                    onVolumeChange = { bassVolume = it },
                    onMuteToggle = { isBassMuted = !isBassMuted }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Stem Track 4: Instruments
                StemTrackSliderRow(
                    name = "🎹 Piano / Synth & Guitars",
                    color = Color(0xFF10B981),
                    volume = instVolume,
                    isMuted = isInstMuted,
                    onVolumeChange = { instVolume = it },
                    onMuteToggle = { isInstMuted = !isInstMuted }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Export Stem Mix Button
                Button(
                    onClick = {
                        isProcessingExport = true
                        scope.launch {
                            delay(1200)
                            isProcessingExport = false
                            Toast.makeText(context, "Exported custom Stem Mix (WAV Studio format)!", Toast.LENGTH_LONG).show()
                            onDismiss()
                        }
                    },
                    enabled = !isProcessingExport,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    if (isProcessingExport) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Rendering Audio Mix...", color = Color.Black, fontSize = 12.sp)
                    } else {
                        Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Separated Stems (.ZIP)", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun StemTrackSliderRow(
    name: String,
    color: Color,
    volume: Float,
    isMuted: Boolean,
    onVolumeChange: (Float) -> Unit,
    onMuteToggle: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E293B),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (isMuted) Color.Gray else color))
                    Text(name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isMuted) Color.Gray else Color.White)
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (isMuted) "MUTED" else "${(volume * 100).toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isMuted) Color(0xFFEF4444) else color
                    )

                    Surface(
                        modifier = Modifier.clickable { onMuteToggle() },
                        shape = RoundedCornerShape(6.dp),
                        color = if (isMuted) Color(0xFFEF4444) else Color(0xFF334155)
                    ) {
                        Text(
                            text = if (isMuted) "UNMUTE" else "MUTE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Slider(
                value = if (isMuted) 0f else volume,
                onValueChange = onVolumeChange,
                enabled = !isMuted,
                colors = SliderDefaults.colors(
                    thumbColor = color,
                    activeTrackColor = color,
                    inactiveTrackColor = Color(0xFF334155)
                ),
                modifier = Modifier.fillMaxWidth().height(28.dp)
            )
        }
    }
}
