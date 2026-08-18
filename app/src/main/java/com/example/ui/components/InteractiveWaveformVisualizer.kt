package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class WaveformVisualizerMode(val title: String, val titleBn: String) {
    NEON_SPECTRUM("Neon Spectrum", "নিয়ন স্পেকট্রাম"),
    WAVE_SCRUBBER("Waveform Scrubber", "ওয়েভফর্ম স্ক্রাবার"),
    BEAT_PULSE("Pulse Ripples", "বিটের তরঙ্গ")
}

/**
 * Interactive Audio Waveform Visualizer with real-time frequency bars,
 * canvas curve path spectrum, interactive drag/tap scrubbing, and dynamic reactive pulses.
 */
@Composable
fun InteractiveWaveformVisualizer(
    progress: Float,
    isPlaying: Boolean,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    barCount: Int = 48,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    accentColor: Color = Color(0xFF38BDF8)
) {
    var selectedMode by remember { mutableStateOf(WaveformVisualizerMode.NEON_SPECTRUM) }

    // Generate static baseline waveform heights with pseudo-frequency peaks
    val baseHeights = remember(barCount) {
        val rand = Random(1337)
        List(barCount) { idx ->
            val centerBias = 1f - kotlin.math.abs((idx - barCount / 2f) / (barCount / 2f)) * 0.35f
            val harmonic = sin(idx * 0.3f).toFloat() * 0.2f
            ((0.2f + rand.nextFloat() * 0.6f + harmonic) * centerBias).coerceIn(0.15f, 0.95f)
        }
    }

    // Dynamic wave animation when playing
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    // Glowing pulse size for playback indicator
    val pulseGlowRadius by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        // Visualizer mode switcher row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = activeColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Live Audio Visualizer",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                WaveformVisualizerMode.values().forEach { mode ->
                    val isModeSelected = selectedMode == mode
                    Surface(
                        onClick = { selectedMode = mode },
                        shape = RoundedCornerShape(6.dp),
                        color = if (isModeSelected) activeColor.copy(alpha = 0.25f) else Color.Transparent,
                        border = if (isModeSelected) androidx.compose.foundation.BorderStroke(1.dp, activeColor.copy(alpha = 0.5f)) else null
                    ) {
                        Text(
                            text = mode.title,
                            fontSize = 9.sp,
                            fontWeight = if (isModeSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isModeSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
                .testTag("interactive_waveform_canvas"),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.75f),
            border = androidx.compose.foundation.BorderStroke(1.dp, activeColor.copy(alpha = 0.2f))
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                            onSeek(newProgress)
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val newProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                            onSeek(newProgress)
                        }
                    }
            ) {
                val totalWidth = size.width
                val totalHeight = size.height
                val barWidth = (totalWidth / barCount) * 0.62f
                val spacing = (totalWidth / barCount) * 0.38f

                val currentPlayedIndex = (progress.coerceIn(0f, 1f) * (barCount - 1)).toInt()
                val playedX = progress.coerceIn(0f, 1f) * totalWidth

                when (selectedMode) {
                    WaveformVisualizerMode.NEON_SPECTRUM -> {
                        // Draw underlying smooth frequency spectrum path
                        val waveformPath = Path()
                        var startedPath = false

                        for (i in 0 until barCount) {
                            val x = i * (barWidth + spacing) + barWidth / 2
                            val dynamicMultiplier = if (isPlaying) {
                                1f + (sin(wavePhase + i * 0.4f).toFloat() * 0.28f)
                            } else 1f

                            val barHeightRatio = (baseHeights[i] * dynamicMultiplier).coerceIn(0.1f, 1.0f)
                            val barHeight = totalHeight * barHeightRatio
                            val y = (totalHeight - barHeight) / 2

                            if (!startedPath) {
                                waveformPath.moveTo(x, y)
                                startedPath = true
                            } else {
                                waveformPath.lineTo(x, y)
                            }
                        }

                        // Draw subtle smooth audio spectrum contour line
                        drawPath(
                            path = waveformPath,
                            color = activeColor.copy(alpha = 0.35f),
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Draw waveform bars with neon gradient
                        for (i in 0 until barCount) {
                            val x = i * (barWidth + spacing) + spacing / 2
                            val dynamicMultiplier = if (isPlaying) {
                                1f + (sin(wavePhase + i * 0.45f).toFloat() * 0.32f)
                            } else 1f

                            val barHeightRatio = (baseHeights[i] * dynamicMultiplier).coerceIn(0.12f, 1.0f)
                            val barHeight = totalHeight * barHeightRatio
                            val y = (totalHeight - barHeight) / 2

                            val isPlayed = i <= currentPlayedIndex
                            val brush = if (isPlayed) {
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFFEC4899), accentColor, activeColor),
                                    startY = y,
                                    endY = y + barHeight
                                )
                            } else {
                                Brush.verticalGradient(
                                    colors = listOf(inactiveColor.copy(alpha = 0.6f), inactiveColor.copy(alpha = 0.2f)),
                                    startY = y,
                                    endY = y + barHeight
                                )
                            }

                            drawRoundRect(
                                brush = brush,
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                            )
                        }
                    }

                    WaveformVisualizerMode.WAVE_SCRUBBER -> {
                        for (i in 0 until barCount) {
                            val x = i * (barWidth + spacing) + spacing / 2
                            val dynamicMultiplier = if (isPlaying) {
                                1f + (sin(wavePhase + i * 0.3f).toFloat() * 0.2f)
                            } else 1f

                            val barHeightRatio = (baseHeights[i] * dynamicMultiplier).coerceIn(0.15f, 1.0f)
                            val barHeight = totalHeight * barHeightRatio
                            val y = (totalHeight - barHeight) / 2

                            val isPlayed = i <= currentPlayedIndex
                            val barColor = if (isPlayed) activeColor else inactiveColor.copy(alpha = 0.4f)

                            drawRoundRect(
                                color = barColor,
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                            )
                        }
                    }

                    WaveformVisualizerMode.BEAT_PULSE -> {
                        // Circular pulse ripples from playhead
                        val centerX = playedX
                        val centerY = totalHeight / 2

                        if (isPlaying) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFFEC4899).copy(alpha = 0.5f), Color.Transparent),
                                    center = Offset(centerX, centerY),
                                    radius = totalHeight * 0.8f * pulseGlowRadius
                                ),
                                radius = totalHeight * 0.8f * pulseGlowRadius,
                                center = Offset(centerX, centerY)
                            )
                        }

                        // Sine wave curves representing acoustic waves
                        val wavePath = Path()
                        for (x in 0..totalWidth.toInt() step 4) {
                            val xPos = x.toFloat()
                            val angle = (xPos / totalWidth) * 4 * Math.PI.toFloat() + (if (isPlaying) wavePhase else 0f)
                            val waveY = centerY + (sin(angle) * (totalHeight * 0.35f)).toFloat()
                            if (x == 0) wavePath.moveTo(xPos, waveY) else wavePath.lineTo(xPos, waveY)
                        }
                        drawPath(
                            path = wavePath,
                            brush = Brush.horizontalGradient(listOf(activeColor, accentColor, Color(0xFFEC4899))),
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }

                // Draw scrub head indicator on all modes
                val headX = playedX.coerceIn(0f, totalWidth)
                val headY = totalHeight / 2

                if (isPlaying) {
                    drawCircle(
                        color = accentColor.copy(alpha = 0.35f),
                        radius = 12.dp.toPx() * pulseGlowRadius,
                        center = Offset(headX, headY)
                    )
                }

                drawCircle(
                    color = Color.White,
                    radius = 6.dp.toPx(),
                    center = Offset(headX, headY)
                )
                drawCircle(
                    color = activeColor,
                    radius = 3.5.dp.toPx(),
                    center = Offset(headX, headY)
                )

                // Draw progress baseline highlight
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(accentColor, activeColor, Color.Transparent),
                        startX = 0f,
                        endX = totalWidth
                    ),
                    start = Offset(0f, totalHeight - 2.dp.toPx()),
                    end = Offset(playedX, totalHeight - 2.dp.toPx()),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}


