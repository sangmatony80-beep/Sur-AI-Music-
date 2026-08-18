package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.audio.RealAudioPlaybackManager
import com.example.data.local.SongEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AiVocalTunerDialog(
    onDismiss: () -> Unit,
    onApplyTunedVocal: (vocalName: String, lyricsPrompt: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isRecording by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableIntStateOf(0) }
    var hasRecordedAudio by remember { mutableStateOf(false) }

    // Auto-Tune Parameters
    var selectedScale by remember { mutableStateOf("C Major (পপ ও আধুনিক)") }
    val scaleOptions = listOf(
        "C Major (পপ ও আধুনিক)",
        "A Minor (মেলানকলিক)",
        "Raag Bhairavi (রাগ ভৈরবী)",
        "Raag Yaman (রাগ ইমন)",
        "Raag Bilaval (রাগ বিলাবল)",
        "Chromatic Auto-Tune (সব স্কেল)"
    )

    var autoTuneSpeed by remember { mutableFloatStateOf(0.85f) } // 0.0 to 1.0
    var selectedVocalCharacter by remember { mutableStateOf("সুরের জাদু (Melodious Sweetness)") }
    val vocalCharacters = listOf(
        "সুরের জাদু (Melodious Sweetness)",
        "উষ্ণ বাউল ও ফোক (Warm Baul Folk)",
        "রেশমি গজল ও খেয়াল (Silk Ghazal)",
        "ক্রিস্টাল স্টুдио পপ (Studio Crystal Pop)",
        "গভীর ক্লাসিক্যাল (Deep Classical)"
    )

    var reverbLevel by remember { mutableFloatStateOf(0.60f) }
    var enable3DHarmonizer by remember { mutableStateOf(true) }

    // Processing State
    var isProcessing by remember { mutableStateOf(false) }
    var processingStep by remember { mutableStateOf("") }
    var isTunedSuccess by remember { mutableStateOf(false) }

    // Preview Playback State
    var isPlayingOriginal by remember { mutableStateOf(false) }
    var isPlayingTuned by remember { mutableStateOf(false) }

    val audioPlaybackManager = remember { RealAudioPlaybackManager(context) }

    DisposableEffect(Unit) {
        onDispose {
            audioPlaybackManager.stop()
        }
    }

    // Timer for voice recording simulation
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingDuration = 0
            while (isRecording && recordingDuration < 15) {
                delay(1000)
                recordingDuration++
            }
            if (recordingDuration >= 15) {
                isRecording = false
                hasRecordedAudio = true
                Toast.makeText(context, "১৫ সেকেন্ড রেকর্ড সম্পন্ন হয়েছে!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    AlertDialog(
        onDismissRequest = {
            audioPlaybackManager.stop()
            onDismiss()
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clip(RoundedCornerShape(24.dp))
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title Header
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
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "বেসুরো কন্ঠ শ্রুতিমধুর করার জাদু",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "AI Pitch Auto-Tune & Studio Voice Beautifier",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = {
                        audioPlaybackManager.stop()
                        onDismiss()
                    }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "বন্ধ করুন")
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                // Main Content Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp)
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Live Voice Recording or Demo Voice
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (hasRecordedAudio) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "১. আপনার কন্ঠ রেকর্ড করুন বা নমুনা নির্বাচন করুন",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (hasRecordedAudio) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = "রেকর্ড রেডি ✓",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            fontSize = 11.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (isRecording) {
                                            isRecording = false
                                            hasRecordedAudio = true
                                        } else {
                                            isRecording = true
                                            hasRecordedAudio = false
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isRecording) "রেকর্ডিং থামান (${recordingDuration}s)" else "মাইক দিয়ে রেকর্ড",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        hasRecordedAudio = true
                                        isRecording = false
                                        Toast.makeText(context, "নমুনা বেসুরো ফাইল নির্বাচন করা হয়েছে", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AudioFile,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "নমুনা ফাইল", fontSize = 12.sp)
                                }
                            }

                            // Visualizer Waveform Animation
                            if (isRecording || hasRecordedAudio) {
                                LivePitchWaveformVisualizer(
                                    isRecording = isRecording,
                                    isTuned = isTunedSuccess
                                )
                            }
                        }
                    }

                    // 2. Scale & Auto-Tune Controls
                    Text(
                        text = "২. স্কেল নির্বাচন ও অটো-টিউন স্পিড",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Scale Selector Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(scaleOptions) { scale ->
                            FilterChip(
                                selected = selectedScale == scale,
                                onClick = { selectedScale = scale },
                                label = { Text(scale, fontSize = 11.sp) }
                            )
                        }
                    }

                    // Retune Speed Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "অটো-টিউন কারেকশন পাওয়ার:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "${(autoTuneSpeed * 100).toInt()}% (${if (autoTuneSpeed > 0.8f) "নিখুঁত সুরেল" else "প্রাকৃতিক"})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = autoTuneSpeed,
                            onValueChange = { autoTuneSpeed = it },
                            valueRange = 0.2f..1.0f
                        )
                    }

                    // 3. Vocal Tone & Reverb Sweetener
                    Text(
                        text = "৩. মিষ্টি কন্ঠের মেজাজ ও রিভার্ব ইফেক্ট",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(vocalCharacters) { char ->
                            SuggestionChip(
                                onClick = { selectedVocalCharacter = char },
                                label = {
                                    Text(
                                        text = char,
                                        fontSize = 11.sp,
                                        fontWeight = if (selectedVocalCharacter == char) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                border = if (selectedVocalCharacter == char) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                            )
                        }
                    }

                    // Reverb Level & Harmonizer Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "স্টুডিও রিভার্ব: ${(reverbLevel * 100).toInt()}%", fontSize = 11.sp)
                            Slider(
                                value = reverbLevel,
                                onValueChange = { reverbLevel = it }
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { enable3DHarmonizer = !enable3DHarmonizer }
                        ) {
                            Checkbox(
                                checked = enable3DHarmonizer,
                                onCheckedChange = { enable3DHarmonizer = it }
                            )
                            Text(text = "৩ডি কোরাস", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    // Processing Loading or Success Actions
                    if (isProcessing) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                                Column {
                                    Text(text = "কন্ঠ সুরেল করা হচ্ছে...", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = processingStep, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }

                    // Before vs After Comparison Card
                    if (isTunedSuccess && !isProcessing) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    Text(text = "কন্ঠ নিখুঁত শ্রুতিমধুর করা হয়েছে!", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Original Raw Audio Playback
                                    OutlinedButton(
                                        onClick = {
                                            if (isPlayingOriginal) {
                                                audioPlaybackManager.stop()
                                                isPlayingOriginal = false
                                            } else {
                                                isPlayingTuned = false
                                                isPlayingOriginal = true
                                                audioPlaybackManager.play(
                                                    SongEntity(
                                                        id = 991,
                                                        title = "Original Voice",
                                                        artist = "User Voice",
                                                        genre = "Raw",
                                                        audioUrl = "http://raw.demo",
                                                        duration = "0:15",
                                                        imageUrl = "",
                                                        lyrics = "[Raw Voice Recording]"
                                                    )
                                                )
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlayingOriginal) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "আসল বেসুরো", fontSize = 11.sp)
                                    }

                                    // Tuned Melodious Audio Playback
                                    Button(
                                        onClick = {
                                            if (isPlayingTuned) {
                                                audioPlaybackManager.stop()
                                                isPlayingTuned = false
                                            } else {
                                                isPlayingOriginal = false
                                                isPlayingTuned = true
                                                audioPlaybackManager.play(
                                                    SongEntity(
                                                        id = 992,
                                                        title = "Auto-Tuned Melodious Voice",
                                                        artist = "Sur AI Beautifier",
                                                        genre = "Baul • Folk",
                                                        audioUrl = "http://tuned.demo",
                                                        duration = "0:15",
                                                        imageUrl = "",
                                                        lyrics = "[Auto-Tuned Melodious Vocal]"
                                                    )
                                                )
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlayingTuned) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "শ্রুতিমধুর সুর", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!isTunedSuccess) {
                        Button(
                            onClick = {
                                if (!hasRecordedAudio) {
                                    hasRecordedAudio = true
                                }
                                scope.launch {
                                    isProcessing = true
                                    processingStep = "১/৩: বেসুরো পিচ এবং ফ্রিকোয়েন্সি স্ক্যান হচ্ছে..."
                                    delay(600)
                                    processingStep = "২/৩: $selectedScale স্কেলে অটো-টিউন সিঙ্ক করা হচ্ছে..."
                                    delay(700)
                                    processingStep = "৩/৩: $selectedVocalCharacter এবং স্টুডিও রিভার্ব যোগ করা হচ্ছে..."
                                    delay(600)
                                    isProcessing = false
                                    isTunedSuccess = true
                                    Toast.makeText(context, "কন্ঠ নিখুঁত ও শ্রুতিমধুর করা সফল হয়েছে!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            enabled = !isProcessing
                        ) {
                            Icon(imageVector = Icons.Default.GraphicEq, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "কন্ঠ শ্রুতিমধুর করুন (Auto-Tune Vocal)", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                audioPlaybackManager.stop()
                                onApplyTunedVocal(selectedVocalCharacter, selectedScale)
                                onDismiss()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "এআই গানে প্রয়োগ করুন (Apply to Song)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LivePitchWaveformVisualizer(
    isRecording: Boolean,
    isTuned: Boolean
) {
    val transition = rememberInfiniteTransition(label = "waveform")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerY = height / 2f
            val path = Path()

            path.moveTo(0f, centerY)

            val points = 60
            val step = width / points

            for (i in 0..points) {
                val x = i * step
                val angle = (i.toFloat() / points) * 4 * Math.PI.toFloat() + phase
                // If isTuned is true, the waveform is a smooth sinusoidal melodious curve.
                // If raw off-pitch, it has jittery pitch noise!
                val amplitude = if (isRecording) {
                    (height * 0.35f) * (0.5f + 0.5f * kotlin.math.sin(angle))
                } else if (isTuned) {
                    (height * 0.30f) * kotlin.math.sin(angle) // Perfectly smooth melodious pitch curve!
                } else {
                    // Off pitch jittery line
                    (height * 0.25f) * (kotlin.math.sin(angle) + 0.4f * kotlin.math.sin(angle * 3.5f))
                }

                val y = centerY + amplitude
                path.lineTo(x, y)
            }

            drawPath(
                path = path,
                color = if (isTuned) Color(0xFF1DB954) else if (isRecording) Color(0xFFFF5252) else Color(0xFF6200EE),
                style = Stroke(width = if (isTuned) 4.dp.toPx() else 2.5.dp.toPx())
            )
        }

        Text(
            text = if (isTuned) "♪ নিখুঁত সুরেল তরঙ্গ (Auto-Tuned Pitch Stream)" else if (isRecording) "🎙️ রেকর্ডিং চলছে..." else "বেসুরো তরঙ্গ রেকর্ড রেডি",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp)
        )
    }
}
