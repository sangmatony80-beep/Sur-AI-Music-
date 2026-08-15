package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Daily Lucky Spin & Streak Reward Dialog.
 * Rewards users with free creation tokens every 24 hours to foster retention and engagement.
 */
@Composable
fun DailyRewardSpinDialog(
    onDismiss: () -> Unit,
    onRewardClaimed: (Int, String) -> Unit,
    currentStreakDays: Int = 3
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isSpinning by remember { mutableStateOf(false) }
    var selectedReward by remember { mutableStateOf<Int?>(null) }
    var rewardClaimed by remember { mutableStateOf(false) }

    // Wheel rotation animatable
    val wheelRotation = remember { Animatable(0f) }

    val wheelSlices = listOf(
        WheelSlice(amount = 25, label = "25 🪙", color = Color(0xFF6366F1)),
        WheelSlice(amount = 50, label = "50 🪙", color = Color(0xFFEC4899)),
        WheelSlice(amount = 100, label = "100 🪙", color = Color(0xFF10B981)),
        WheelSlice(amount = 20, label = "20 🪙", color = Color(0xFFF59E0B)),
        WheelSlice(amount = 250, label = "250 🌟", color = Color(0xFF8B5CF6)),
        WheelSlice(amount = 75, label = "75 🪙", color = Color(0xFF3B82F6)),
        WheelSlice(amount = 500, label = "JACKPOT 🎉", color = Color(0xFFEF4444)),
        WheelSlice(amount = 35, label = "35 🪙", color = Color(0xFF14B8A6))
    )

    Dialog(onDismissRequest = { if (!isSpinning) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF0F172A),
            tonalElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Streak Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Stars, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(24.dp))
                        Text(
                            text = "Daily Lucky Spin",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                    IconButton(onClick = onDismiss, enabled = !isSpinning) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                // 7-Day Streak Row
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("🔥 Daily Streak", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                            Text("Day $currentStreakDays of 7 (Bonus +$${currentStreakDays * 10}%)", fontSize = 11.sp, color = Color(0xFF38BDF8))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (day in 1..7) {
                                val isDone = day <= currentStreakDays
                                val isToday = day == currentStreakDays
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isToday -> Color(0xFFF59E0B)
                                                    isDone -> Color(0xFF10B981)
                                                    else -> Color(0xFF334155)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isDone) "✓" else "D$day",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // The Spinning Wheel Canvas
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(220.dp)
                ) {
                    // Outer Ring
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(wheelRotation.value)
                    ) {
                        val radius = size.minDimension / 2f
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val sliceAngle = 360f / wheelSlices.size

                        for (i in wheelSlices.indices) {
                            val startAngle = i * sliceAngle
                            drawArc(
                                color = wheelSlices[i].color,
                                startAngle = startAngle,
                                sweepAngle = sliceAngle,
                                useCenter = true,
                                topLeft = Offset(center.x - radius, center.y - radius),
                                size = Size(radius * 2, radius * 2),
                                style = Fill
                            )

                            drawArc(
                                color = Color.White.copy(alpha = 0.2f),
                                startAngle = startAngle,
                                sweepAngle = sliceAngle,
                                useCenter = true,
                                topLeft = Offset(center.x - radius, center.y - radius),
                                size = Size(radius * 2, radius * 2),
                                style = Stroke(width = 2f)
                            )
                        }

                        // Center hub
                        drawCircle(
                            color = Color(0xFF0F172A),
                            radius = radius * 0.28f,
                            center = center
                        )
                        drawCircle(
                            color = Color(0xFFF59E0B),
                            radius = radius * 0.16f,
                            center = center
                        )
                    }

                    // Center Hub Label
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("SUR", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White)
                        Text("AI", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                    }

                    // Top Pointer Needle
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Pointer",
                        tint = Color.White,
                        modifier = Modifier
                            .size(42.dp)
                            .align(Alignment.TopCenter)
                            .offset(y = (-6).dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Result display or Spin trigger button
                if (selectedReward != null && !isSpinning) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🎉 CONGRATULATIONS!", fontWeight = FontWeight.Black, color = Color(0xFF10B981), fontSize = 14.sp)
                            Text(
                                text = "You won +$selectedReward Sur AI Creation Tokens!",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            rewardClaimed = true
                            onRewardClaimed(selectedReward!!, "Daily Lucky Wheel Reward")
                            Toast.makeText(context, "Added +$selectedReward Tokens to your balance! 🎵", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Claim & Add to Balance", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            if (!isSpinning) {
                                scope.launch {
                                    isSpinning = true
                                    selectedReward = null

                                    // Pick random slice
                                    val targetIndex = Random.nextInt(wheelSlices.size)
                                    val sliceAngle = 360f / wheelSlices.size
                                    // Calculate target degrees: 5 full rotations + specific slice offset aligning to top (270 degrees)
                                    val targetDegrees = (360f * 5) + (360f - (targetIndex * sliceAngle + sliceAngle / 2f) + 270f) % 360f

                                    wheelRotation.animateTo(
                                        targetValue = targetDegrees,
                                        animationSpec = tween(
                                            durationMillis = 3200,
                                            easing = FastOutSlowInEasing
                                        )
                                    )

                                    selectedReward = wheelSlices[targetIndex].amount
                                    isSpinning = false
                                }
                            }
                        },
                        enabled = !isSpinning,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        if (isSpinning) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Spinning...", color = Color.Black, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.RotateRight, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SPIN FOR FREE TOKENS", color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

data class WheelSlice(
    val amount: Int,
    val label: String,
    val color: Color
)
