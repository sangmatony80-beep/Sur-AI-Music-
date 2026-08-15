package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Reusable LoadingScreen composable with modern animated spinner, pulsing glow, and customizable status text.
 * Suitable for asynchronous operations like fetching music feeds, authenticating users, or generating AI songs.
 */
@Composable
fun LoadingScreen(
    modifier: Modifier = Modifier,
    message: String = "Loading, please wait...",
    subtitle: String? = "Connecting to Sur AI Studio",
    isTransparentOverlay: Boolean = false
) {
    // Pulse animation for the glowing ambient aura
    val infiniteTransition = rememberInfiniteTransition(label = "loading_transition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_angle"
    )

    val backgroundModifier = if (isTransparentOverlay) {
        modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f))
    } else {
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    }

    Box(
        modifier = backgroundModifier.testTag("loading_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Container card
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = if (isTransparentOverlay) Color(0xFF1E293B).copy(alpha = 0.95f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shadowElevation = 8.dp,
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 380.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 32.dp, vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Animated Glowing Spinner Aura
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(90.dp)
                ) {
                    // Pulsing background glow ring
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .scale(pulseScale)
                            .alpha(pulseAlpha)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        Color(0xFF8B5CF6),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Secondary rotating accent spinner
                    CircularProgressIndicator(
                        progress = { 0.75f },
                        modifier = Modifier
                            .size(68.dp)
                            .rotate(rotationAngle),
                        color = Color(0xFF38BDF8).copy(alpha = 0.4f),
                        strokeWidth = 3.dp,
                        strokeCap = StrokeCap.Round
                    )

                    // Main animated CircularProgressIndicator
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("loading_spinner"),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        strokeWidth = 4.dp,
                        strokeCap = StrokeCap.Round
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Primary animated text message
                Text(
                    text = message,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("loading_message")
                )

                // Subtitle if provided
                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
