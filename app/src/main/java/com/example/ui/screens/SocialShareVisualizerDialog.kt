package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SongEntity
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

// Re-using the Song model from PlayerScreen
@Composable
fun SocialShareVisualizerDialog(
    song: SongEntity,
    onDismiss: () -> Unit
) {
    var isExporting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var exportProgress by remember { mutableFloatStateOf(0f) }

    // Animation for visualizer
    val infiniteTransition = rememberInfiniteTransition(label = "visualizer")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    AlertDialog(
        onDismissRequest = { if (!isExporting) onDismiss() },
        title = { Text("Share to Social Media", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Generate a dynamic audio visualizer video optimized for Instagram Reels and TikTok.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Visualizer Preview Box
                Box(
                    modifier = Modifier
                        .size(200.dp, 300.dp) // 9:16 aspect ratio preview
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Draw Circular Visualizer
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val radius = size.width / 4

                        val barCount = 40
                        val angleStep = (2 * Math.PI) / barCount

                        for (i in 0 until barCount) {
                            val angle = i * angleStep
                            
                            // Simulate dynamic audio bars
                            val noise = sin(phase * 3 + i) * cos(phase * 2 - i)
                            val barHeight = 10f + (Math.abs(noise) * 40f)

                            val startX = center.x + radius * cos(angle).toFloat()
                            val startY = center.y + radius * sin(angle).toFloat()

                            val endX = center.x + (radius + barHeight) * cos(angle).toFloat()
                            val endY = center.y + (radius + barHeight) * sin(angle).toFloat()

                            drawLine(
                                color = Color(0xFF38BDF8),
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = 6f,
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    // Song Title Overlay
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(song.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(song.artist, color = Color.Gray, fontSize = 12.sp)
                    }
                }

                if (isExporting) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { exportProgress },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Rendering Video: ${(exportProgress * 100).toInt()}%", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            if (!isExporting) {
                Button(onClick = {
                    isExporting = true
                    // Simulate Video Rendering
                    scope.launch {
                        for (i in 1..100) {
                            exportProgress = i / 100f
                            delay(30)
                        }
                        onDismiss()
                    }
                }) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export MP4")
                }
            }
        },
        dismissButton = {
            if (!isExporting) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
