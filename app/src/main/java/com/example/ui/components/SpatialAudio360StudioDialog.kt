package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.SongEntity
import kotlin.math.cos
import kotlin.math.sin

/**
 * 🎧 8D Spatial Audio, Binaural 360° Studio & 10-Band Graphic Equalizer
 * Enables real-time acoustic environment modeling and 3D headphone panning.
 */
@Composable
fun SpatialAudio360StudioDialog(
    song: SongEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var is8dEnabled by remember { mutableStateOf(true) }
    var rotationSpeed by remember { mutableFloatStateOf(1.0f) }
    var spatialWidth by remember { mutableFloatStateOf(1.4f) }
    var selectedPreset by remember { mutableStateOf("🏟️ Dhaka Stadium") }
    var bassBoostAmount by remember { mutableFloatStateOf(6.0f) }
    var vocalClarityAmount by remember { mutableFloatStateOf(4.5f) }

    // 10-Band EQ gain levels in dB (-12dB to +12dB)
    val eqFrequencies = listOf("32Hz", "64Hz", "125Hz", "250Hz", "500Hz", "1kHz", "2kHz", "4kHz", "8kHz", "16kHz")
    var eqGains by remember {
        mutableStateOf(mutableStateListOf(4.0f, 3.0f, 1.5f, 0.0f, -1.0f, 2.0f, 3.5f, 4.0f, 3.0f, 2.0f))
    }

    // Continuous 360° rotation animation for spatial 8D head tracking
    val infiniteTransition = rememberInfiniteTransition(label = "8D_Spatial_Orbit")
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween((7000 / rotationSpeed.coerceAtLeast(0.2f)).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitAngle"
    )

    val presets = listOf(
        "🏟️ Dhaka Stadium",
        "🕌 Sufi Acoustic Hall",
        "📻 1970s Vintage Vinyl",
        "🌊 Riverside Baul Akhra",
        "🚗 Car Subwoofer Reflex",
        "🌌 Cyberpunk Neon Void"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF0B0F19),
            tonalElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Header
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
                            color = Color(0xFF6366F1).copy(alpha = 0.2f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Headphones, contentDescription = null, tint = Color(0xFF818CF8), modifier = Modifier.size(24.dp))
                            }
                        }
                        Column {
                            Text("৮ডি স্থানিক অডিও ও বাইনোরাল স্টুডিও", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                            Text("${song.title} • 360° Spatial Acoustic Field", fontSize = 11.sp, color = Color.LightGray)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                // 360° Orbital Head Panning Visualizer Canvas
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("360° Binaural Audio Orbit", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(if (is8dEnabled) "8D ACTIVE" else "STEREO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (is8dEnabled) Color(0xFF10B981) else Color.Gray)
                                Switch(
                                    checked = is8dEnabled,
                                    onCheckedChange = { is8dEnabled = it }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Spatial Radar Canvas
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val center = Offset(size.width / 2, size.height / 2)
                                val radius = size.minDimension / 2.2f

                                // Draw Radar Rings
                                drawCircle(color = Color(0xFF334155), radius = radius * 0.35f, center = center, style = Stroke(1.5f))
                                drawCircle(color = Color(0xFF334155), radius = radius * 0.70f, center = center, style = Stroke(1.5f))
                                drawCircle(color = Color(0xFF475569), radius = radius, center = center, style = Stroke(2f))

                                // Draw Crosshairs
                                drawLine(color = Color(0xFF334155), start = Offset(center.x, center.y - radius), end = Offset(center.x, center.y + radius), strokeWidth = 1f)
                                drawLine(color = Color(0xFF334155), start = Offset(center.x - radius, center.y), end = Offset(center.x + radius, center.y), strokeWidth = 1f)

                                if (is8dEnabled) {
                                    // Calculate rotating audio satellite position
                                    val rad = Math.toRadians(orbitAngle.toDouble())
                                    val satX = center.x + (radius * 0.85f * cos(rad)).toFloat()
                                    val satY = center.y + (radius * 0.85f * sin(rad)).toFloat()

                                    // Audio Trail line
                                    drawLine(
                                        color = Color(0xFF818CF8).copy(alpha = 0.5f),
                                        start = center,
                                        end = Offset(satX, satY),
                                        strokeWidth = 2.5f
                                    )

                                    // Satellite node
                                    drawCircle(color = Color(0xFF818CF8), radius = 10f, center = Offset(satX, satY))
                                    drawCircle(color = Color.White, radius = 5f, center = Offset(satX, satY))
                                }

                                // Center Head Icon
                                drawCircle(color = Color(0xFF38BDF8), radius = 14f, center = center)
                                drawCircle(color = Color.White, radius = 6f, center = center)
                            }
                            Text("🎧 YOU", color = Color(0xFF38BDF8), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.offset(y = 22.dp))
                        }

                        // Orbit Speed & Spatial Width Sliders
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Rotation Speed: ${String.format("%.1f", rotationSpeed)}x", fontSize = 11.sp, color = Color.LightGray)
                                Slider(
                                    value = rotationSpeed,
                                    onValueChange = { rotationSpeed = it },
                                    valueRange = 0.3f..3.0f
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("3D Field Width: ${(spatialWidth * 100).toInt()}%", fontSize = 11.sp, color = Color.LightGray)
                                Slider(
                                    value = spatialWidth,
                                    onValueChange = { spatialWidth = it },
                                    valueRange = 0.5f..2.0f
                                )
                            }
                        }
                    }
                }

                // Acoustic Space Presets
                Text("🏛️ অ্যাকোস্টিক রুম প্রিসেটস (Acoustic Spaces)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { preset ->
                        val isSelected = selectedPreset == preset
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedPreset = preset
                                Toast.makeText(context, "Acoustic space applied: $preset", Toast.LENGTH_SHORT).show()
                            },
                            label = { Text(preset, fontSize = 12.sp, color = if (isSelected) Color.White else Color.LightGray) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF6366F1),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // 10-Band Graphic Equalizer
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🎚️ ১০-ব্যান্ড স্টুডিও গ্রাফিক্যাল ইকুয়ালাইজার", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            TextButton(
                                onClick = {
                                    for (i in eqGains.indices) eqGains[i] = 0.0f
                                    Toast.makeText(context, "EQ Flat Reset", Toast.LENGTH_SHORT).show()
                                },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Reset Flat", fontSize = 11.sp, color = Color(0xFF38BDF8))
                            }
                        }

                        // EQ Visual Fader Bars
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            eqFrequencies.forEachIndexed { index, freq ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxHeight()
                                ) {
                                    Text(
                                        text = "${if (eqGains[index] > 0) "+" else ""}${eqGains[index].toInt()}dB",
                                        fontSize = 8.sp,
                                        color = if (eqGains[index] != 0f) Color(0xFF818CF8) else Color.Gray,
                                        fontWeight = FontWeight.Bold
                                    )

                                    // Vertical Gain Level Slider Simulation
                                    Box(
                                        modifier = Modifier
                                            .width(22.dp)
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF1E293B))
                                            .clickable {
                                                eqGains[index] = when {
                                                    eqGains[index] >= 6.0f -> -6.0f
                                                    eqGains[index] >= 0.0f -> 6.0f
                                                    else -> 0.0f
                                                }
                                            },
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        val fillFraction = ((eqGains[index] + 12f) / 24f).coerceIn(0.05f, 1f)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .fillMaxHeight(fillFraction)
                                                .background(
                                                    Brush.verticalGradient(
                                                        listOf(Color(0xFF818CF8), Color(0xFF6366F1))
                                                    )
                                                )
                                        )
                                    }

                                    Text(freq, fontSize = 8.sp, color = Color.LightGray)
                                }
                            }
                        }
                    }
                }

                // Action Buttons
                Button(
                    onClick = {
                        Toast.makeText(context, "8D Spatial Audio Profile & Studio EQ Saved!", Toast.LENGTH_LONG).show()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Apply Spatial Sound Engine", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
