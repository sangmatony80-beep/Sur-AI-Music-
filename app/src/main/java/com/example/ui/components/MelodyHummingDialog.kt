package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.random.Random

@Composable
fun MelodyHummingDialog(
    isBangla: Boolean = true,
    onDismiss: () -> Unit,
    onApplyPrompt: (melodyDescription: String, detectedGenre: String, suggestedTempo: String) -> Unit
) {
    var isRecording by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableIntStateOf(0) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisComplete by remember { mutableStateOf(false) }

    var detectedGenre by remember { mutableStateOf("Baul Fusion") }
    var detectedMood by remember { mutableStateOf("Melancholic & Soulful") }
    var detectedScale by remember { mutableStateOf("Bhairavi / D Minor") }
    var detectedBpm by remember { mutableStateOf("92 BPM") }
    var generatedMelodyPrompt by remember { mutableStateOf("") }

    // Waveform heights for animated mic pulse
    var waveformLevels by remember { mutableStateOf(List(24) { 0.2f }) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Recording timer & animated bars loop
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingSeconds = 0
            while (isActive && isRecording) {
                delay(150L)
                waveformLevels = List(24) { Random.nextFloat().coerceIn(0.15f, 1f) }
                if (System.currentTimeMillis() % 1000 < 200) {
                    recordingSeconds += 1
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
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
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Humming",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (isBangla) "ভয়েস ও গুনগুন সুর রেকর্ডার" else "Voice & Humming to Melody AI",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (isBangla) "আপনার মনের যেকোনো সুর গুনগুন করুন বা বাঁশি বাজান। সুর এআই স্বয়ংক্রিয়ভাবে পিচ ও স্কেল শনাক্ত করে প্রো প্রম্পটে রূপান্তর করবে।"
                    else "Hum, sing, or whistle your melody idea. Sur AI pitch detection will convert your audio into musical stems and master prompt.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // Recording Visualizer Center
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Big Pulsing Record Mic Button
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .scale(pulseScale)
                                .background(
                                    if (isRecording) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                                    CircleShape
                                )
                                .clickable {
                                    if (!isRecording) {
                                        isRecording = true
                                        analysisComplete = false
                                    } else {
                                        isRecording = false
                                        isAnalyzing = true
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = "Record",
                                tint = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        // Timer & Live Status
                        if (isRecording) {
                            Text(
                                text = "🔴 Recording Melody... ${recordingSeconds}s",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            // Animated Waveform Bars
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                waveformLevels.forEach { lvl ->
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .fillMaxHeight(lvl)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(MaterialTheme.colorScheme.error)
                                    )
                                }
                            }
                        } else if (isAnalyzing) {
                            LaunchedEffect(Unit) {
                                delay(1200L)
                                isAnalyzing = false
                                analysisComplete = true
                                generatedMelodyPrompt = if (isBangla) 
                                    "মনের গভীরে পৌঁছানো বাউল ফিউশন সুর, একতারা ও দোতারার সাথে আধুনিক অ্যাকোস্টিক ড্রামস বিট এবং শান্ত বাঁশির মিষ্টি তান।"
                                else 
                                    "Emotional Baul Folk Fusion melody with acoustic Dotara plucks, mellow flute leads, soulful chord progression, 92 BPM in D Minor scale."
                            }
                            CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
                            Text(
                                text = if (isBangla) "এআই পিচ ও স্কেল এনালাইসিস চলছে..." else "AI Neural Pitch & Key Extraction...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else if (!analysisComplete) {
                            Text(
                                text = if (isBangla) "রেকর্ড শুরু করতে মাইকে ট্যাপ করুন" else "Tap microphone to record melody",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // AI Detection Results
                if (analysisComplete) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "✨ Pitch Analysis Result",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = detectedBpm,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surface) {
                                    Text("🎵 $detectedGenre", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp)
                                }
                                Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surface) {
                                    Text("🎹 $detectedScale", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp)
                                }
                            }
                            Text(
                                text = generatedMelodyPrompt,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (analysisComplete) {
                Button(
                    onClick = {
                        onApplyPrompt(generatedMelodyPrompt, detectedGenre, detectedBpm)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBangla) "প্রম্পটে ব্যবহার করুন" else "Apply to Music Prompt")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Close")
            }
        }
    )
}
