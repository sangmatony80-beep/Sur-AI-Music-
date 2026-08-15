package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.random.Random

/**
 * Interactive Audio Waveform Visualizer with real-time frequency bars,
 * interactive scrubbing, and dynamic reactive pulses when playing.
 */
@Composable
fun InteractiveWaveformVisualizer(
    progress: Float,
    isPlaying: Boolean,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    barCount: Int = 42,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    accentColor: Color = Color(0xFF38BDF8)
) {
    // Generate static baseline waveform heights
    val baseHeights = remember(barCount) {
        val rand = Random(42)
        List(barCount) {
            val centerBias = 1f - kotlin.math.abs((it - barCount / 2f) / (barCount / 2f)) * 0.4f
            (0.25f + rand.nextFloat() * 0.75f) * centerBias
        }
    }

    // Dynamic wave animation when playing
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.6f)
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
        ) {
            val totalWidth = size.width
            val totalHeight = size.height
            val barWidth = (totalWidth / barCount) * 0.65f
            val spacing = (totalWidth / barCount) * 0.35f

            val currentPlayedIndex = (progress * barCount).toInt()

            for (i in 0 until barCount) {
                val x = i * (barWidth + spacing) + spacing / 2

                // If playing, add subtle sine-wave pulsing
                val dynamicMultiplier = if (isPlaying) {
                    val sinFactor = kotlin.math.sin(wavePhase + i * 0.45f).toFloat()
                    1f + (sinFactor * 0.25f)
                } else 1f

                val barHeightRatio = (baseHeights[i] * dynamicMultiplier).coerceIn(0.12f, 1.0f)
                val barHeight = totalHeight * barHeightRatio
                val y = (totalHeight - barHeight) / 2

                val isPlayed = i <= currentPlayedIndex

                val brush = if (isPlayed) {
                    Brush.verticalGradient(
                        colors = listOf(accentColor, activeColor),
                        startY = y,
                        endY = y + barHeight
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(inactiveColor.copy(alpha = 0.8f), inactiveColor.copy(alpha = 0.3f)),
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

                // Draw scrub head indicator on currently active bar
                if (i == currentPlayedIndex) {
                    drawCircle(
                        color = Color.White,
                        radius = barWidth * 0.9f,
                        center = Offset(x + barWidth / 2, y + barHeight / 2)
                    )
                    drawCircle(
                        color = activeColor,
                        radius = barWidth * 0.5f,
                        center = Offset(x + barWidth / 2, y + barHeight / 2)
                    )
                }
            }
        }
    }
}
