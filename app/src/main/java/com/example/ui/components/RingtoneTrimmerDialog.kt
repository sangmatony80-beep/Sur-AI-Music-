package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SongEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.random.Random

@Composable
fun RingtoneTrimmerDialog(
    song: SongEntity,
    onDismiss: () -> Unit,
    onSetRingtoneSuccess: (String) -> Unit = {}
) {
    var startSeconds by remember { mutableFloatStateOf(0f) }
    val clipDuration = 30f // Standard ringtone length: 30 seconds
    val maxTrackDuration = 180f
    
    var isPreviewPlaying by remember { mutableStateOf(false) }
    var currentPreviewPosition by remember { mutableFloatStateOf(0f) }
    var isFadeIn by remember { mutableStateOf(true) }
    var isFadeOut by remember { mutableStateOf(true) }
    var ringtoneType by remember { mutableStateOf("Phone Call Ringtone") }
    var showSuccessToast by remember { mutableStateOf(false) }

    // Simulated waveform bars
    val waveformBars = remember(song.id) {
        val rand = Random(song.id.toInt().coerceAtLeast(1))
        List(40) { rand.nextFloat().coerceIn(0.15f, 1f) }
    }

    // Preview playback loop
    LaunchedEffect(isPreviewPlaying, startSeconds) {
        if (isPreviewPlaying) {
            currentPreviewPosition = startSeconds
            while (isActive && isPreviewPlaying) {
                delay(100L)
                currentPreviewPosition += 0.1f
                if (currentPreviewPosition >= startSeconds + clipDuration) {
                    currentPreviewPosition = startSeconds
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = {
            isPreviewPlaying = false
            onDismiss()
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCut,
                        contentDescription = "Trim",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Ringtone Maker & Trimmer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = {
                    isPreviewPlaying = false
                    onDismiss()
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Song details header
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "${song.artist} • ${song.genre}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Waveform Trimming Scrubber
                Text(
                    text = "Trim 30s Audio Segment:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
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
                        // Waveform visualizer bars
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

                // Audio Fade & Type Options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isFadeIn,
                        onClick = { isFadeIn = !isFadeIn },
                        label = { Text("Fade In 2s") },
                        leadingIcon = {
                            if (isFadeIn) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = isFadeOut,
                        onClick = { isFadeOut = !isFadeOut },
                        label = { Text("Fade Out 2s") },
                        leadingIcon = {
                            if (isFadeOut) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Ringtone Target Selector
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

                // Preview Player Button
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
                                text = "Successfully applied as $ringtoneType! Audio clip saved.",
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
            Button(
                onClick = {
                    isPreviewPlaying = false
                    showSuccessToast = true
                    onSetRingtoneSuccess("${song.title} set as $ringtoneType")
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.RingVolume, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Set as $ringtoneType")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                isPreviewPlaying = false
                onDismiss()
            }) {
                Text("Cancel")
            }
        }
    )
}
