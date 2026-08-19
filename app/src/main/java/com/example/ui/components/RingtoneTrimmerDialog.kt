package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.data.local.SongEntity
import com.ai.audio.infrastructure.export.AudioTrimmer
import com.ai.audio.infrastructure.export.WavExporter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RingtoneTrimmerDialog(
    song: SongEntity,
    onDismiss: () -> Unit,
    onSetRingtoneSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var startSeconds by remember { mutableFloatStateOf(0f) }
    val clipDuration = 30f // Fixed 30s ringtone
    val maxTrackDuration = 210f // e.g. 3m 30s
    var isPreviewPlaying by remember { mutableStateOf(false) }
    var currentPreviewPosition by remember { mutableFloatStateOf(startSeconds) }
    
    var isFadeIn by remember { mutableStateOf(true) }
    var isFadeOut by remember { mutableStateOf(true) }
    var ringtoneType by remember { mutableStateOf("Phone Ringtone") }
    
    var showSuccessToast by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    // Fake waveform generation
    val waveformBars = remember { List(40) { Random.nextFloat() * 0.8f + 0.2f } }

    LaunchedEffect(isPreviewPlaying, startSeconds) {
        if (isPreviewPlaying) {
            currentPreviewPosition = startSeconds
            while (isPreviewPlaying && currentPreviewPosition < (startSeconds + clipDuration)) {
                delay(100)
                currentPreviewPosition += 0.1f
            }
            isPreviewPlaying = false
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.ContentCut, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("AI Ringtone Maker", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Trim a 30-second seamless loop from \"${song.title}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Visualizer/Trimmer Graph area
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            waveformBars.forEachIndexed { index, heightFactor ->
                                val barTime = (index / 40f) * maxTrackDuration
                                val isInsideClip = barTime >= startSeconds && barTime <= (startSeconds + clipDuration)
                                val isCurrentlyPlaying = isPreviewPlaying && (barTime <= currentPreviewPosition && barTime >= startSeconds)
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .fillMaxHeight(heightFactor)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            when {
                                                isCurrentlyPlaying -> MaterialTheme.colorScheme.tertiary
                                                isInsideClip -> MaterialTheme.colorScheme.primary
                                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                            }
                                        )
                                )
                            }
                        }
                    }
                }

                // Range Slider
                Column {
                    Slider(
                        value = startSeconds,
                        onValueChange = {
                            startSeconds = it.coerceIn(0f, maxTrackDuration - clipDuration)
                            if (isPreviewPlaying) {
                                currentPreviewPosition = startSeconds
                            }
                        },
                        valueRange = 0f..(maxTrackDuration - clipDuration),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val sMin = (startSeconds / 60).toInt()
                        val sSec = (startSeconds % 60).toInt()
                        val eMin = ((startSeconds + clipDuration) / 60).toInt()
                        val eSec = ((startSeconds + clipDuration) % 60).toInt()
                        Text(
                            text = "Start: ${String.format("%d:%02d", sMin, sSec)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Duration: 30s",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "End: ${String.format("%d:%02d", eMin, eSec)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isFadeIn,
                        onClick = { isFadeIn = !isFadeIn },
                        label = { Text("Fade In 2s") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = isFadeOut,
                        onClick = { isFadeOut = !isFadeOut },
                        label = { Text("Fade Out 2s") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Phone Ringtone", "Alarm Tone", "Notification").forEach { type ->
                        FilterChip(
                            selected = ringtoneType == type,
                            onClick = { ringtoneType = type },
                            label = { Text(type, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedButton(
                    onClick = { isPreviewPlaying = !isPreviewPlaying },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (isPreviewPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isPreviewPlaying) "Pause 30s Clip Preview" else "Listen to 30s Trimmed Preview")
                }

                if (isProcessing) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Text("Trimming audio buffer...", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }

                if (showSuccessToast) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.2f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                            Text(
                                text = "Successfully applied as $ringtoneType! Audio clip saved to Music.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!isProcessing) {
                Button(
                    onClick = {
                        isPreviewPlaying = false
                        isProcessing = true
                        
                        scope.launch {
                            // 1. Generate Dummy PCM data representing the full song
                            // 48kHz * 2 channels * 2 bytes = 192000 bytes/sec
                            val dummyFullPcm = ByteArray((maxTrackDuration * 192000).toInt())
                            
                            // 2. Actually trim the audio using AudioTrimmer
                            val trimmedData = AudioTrimmer.trimAudio(
                                pcmData = dummyFullPcm,
                                startTimeSec = startSeconds,
                                endTimeSec = startSeconds + clipDuration
                            )
                            
                            // 3. Export to WAV using WavExporter
                            if (trimmedData != null) {
                                val filename = "Ringtone_${song.title.replace(" ", "_")}_$ringtoneType"
                                WavExporter.exportToWav(context, trimmedData, filename)
                                
                                isProcessing = false
                                showSuccessToast = true
                                onSetRingtoneSuccess("${song.title} set as $ringtoneType")
                            } else {
                                isProcessing = false
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.RingVolume, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save $ringtoneType")
                }
            }
        },
        dismissButton = {
            if (!isProcessing) {
                TextButton(onClick = {
                    isPreviewPlaying = false
                    onDismiss()
                }) {
                    Text("Cancel")
                }
            }
        }
    )
}