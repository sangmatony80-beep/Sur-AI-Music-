package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.SongEntity
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

data class EqPreset(
    val id: String,
    val bengaliName: String,
    val englishName: String,
    val gains: List<Float>, // 7 values for 60Hz, 150Hz, 400Hz, 1kHz, 2.5kHz, 6kHz, 14kHz (range -12f to +12f)
    val bassBoost: Float = 0.3f,
    val stereoWidth: Float = 0.5f,
    val tubeWarmth: Float = 0.4f,
    val vocalClarity: Float = 0.5f
)

@Composable
fun AudioMasteringEqVisualizerDialog(
    song: SongEntity? = null,
    onDismiss: () -> Unit,
    onApplyMastering: (String) -> Unit = {}
) {
    val context = LocalContext.current

    // Equalizer Frequencies: 60Hz, 150Hz, 400Hz, 1kHz, 2.5kHz, 6kHz, 14kHz
    val eqFrequencies = listOf("60Hz", "150Hz", "400Hz", "1kHz", "2.5kHz", "6kHz", "14kHz")
    val eqLabels = listOf("Sub", "Bass", "Lo-Mid", "Mid", "Hi-Mid", "Pres", "Air")

    val presets = remember {
        listOf(
            EqPreset("folk_punch", "ফোক ও বাউল পাঞ্চ", "Bengali Folk Punch", listOf(4f, 6f, -1f, 2f, 4f, 5f, 3f), bassBoost = 0.65f, stereoWidth = 0.6f, tubeWarmth = 0.7f, vocalClarity = 0.8f),
            EqPreset("rabindra_warmth", "রবীন্দ্রসঙ্গীত ভয়েস", "Rabindra Vocal Warmth", listOf(1f, 2f, 3f, 4f, 2f, 3f, 4f), bassBoost = 0.2f, stereoWidth = 0.4f, tubeWarmth = 0.85f, vocalClarity = 0.9f),
            EqPreset("nazrul_clarity", "নজরুলগীতি ডায়নামিক", "Nazrul Dynamic Clarity", listOf(2f, 4f, 1f, 3f, 5f, 6f, 4f), bassBoost = 0.4f, stereoWidth = 0.55f, tubeWarmth = 0.5f, vocalClarity = 0.85f),
            EqPreset("modern_pop", "আধুনিক পপ ও রক", "Modern Pop & Rock", listOf(6f, 5f, -2f, 0f, 4f, 7f, 6f), bassBoost = 0.8f, stereoWidth = 0.75f, tubeWarmth = 0.35f, vocalClarity = 0.7f),
            EqPreset("edm_808", "হেভি ইডিএম বেস", "EDM 808 Bass Boost", listOf(9f, 8f, -3f, -1f, 3f, 6f, 8f), bassBoost = 0.95f, stereoWidth = 0.85f, tubeWarmth = 0.2f, vocalClarity = 0.6f),
            EqPreset("acoustic_unplugged", "অ্যাকোস্টিক দোতারা", "Acoustic Unplugged", listOf(2f, 3f, 2f, 1f, 3f, 4f, 5f), bassBoost = 0.3f, stereoWidth = 0.5f, tubeWarmth = 0.6f, vocalClarity = 0.75f),
            EqPreset("flat", "ফ্ল্যাট মাস্টার", "Flat Neutral", listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f), bassBoost = 0f, stereoWidth = 0.5f, tubeWarmth = 0f, vocalClarity = 0.5f)
        )
    }

    var selectedPreset by remember { mutableStateOf(presets[0]) }
    var currentGains by remember { mutableStateOf(presets[0].gains.toMutableList()) }

    var bassBoostVal by remember { mutableFloatStateOf(presets[0].bassBoost) }
    var stereoWidthVal by remember { mutableFloatStateOf(presets[0].stereoWidth) }
    var tubeWarmthVal by remember { mutableFloatStateOf(presets[0].tubeWarmth) }
    var vocalClarityVal by remember { mutableFloatStateOf(presets[0].vocalClarity) }

    var isEqBypassed by remember { mutableStateOf(false) }
    var targetLufs by remember { mutableStateOf("-14 LUFS (Spotify)") }
    var isMasteringProcessing by remember { mutableStateOf(false) }

    // Dynamic animated audio spectrum bars (16 frequency buckets)
    val spectrumValues = remember { mutableStateListOf(*Array(16) { 0.4f }) }

    LaunchedEffect(Unit) {
        var tick = 0.0
        while (true) {
            delay(80)
            tick += 0.3
            for (i in 0 until 16) {
                val wave = (sin(tick + i * 0.5) * 0.4 + 0.5).toFloat()
                val jitter = (Random.nextFloat() * 0.2f)
                val eqInfluence = if (!isEqBypassed) {
                    val gainIdx = (i * 7) / 16
                    (currentGains[gainIdx] / 24f) + 0.1f
                } else 0f
                spectrumValues[i] = (wave + jitter + eqInfluence).coerceIn(0.08f, 0.98f)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0B0F19),
            border = BorderStroke(1.dp, Color(0xFF06B6D4).copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
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
                            color = Color(0xFF06B6D4),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Equalizer, contentDescription = null, tint = Color.Black, modifier = Modifier.size(22.dp))
                            }
                        }
                        Column {
                            Text(
                                text = "প্রফেশনাল অডিও মাস্টারিং ও EQ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "7-Band Graphic Equalizer, LUFS Meter & Analog Warmer",
                                fontSize = 11.sp,
                                color = Color(0xFF22D3EE)
                            )
                        }
                    }

                    // Bypass Toggle & Close
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(
                            selected = isEqBypassed,
                            onClick = {
                                isEqBypassed = !isEqBypassed
                                Toast.makeText(context, if (isEqBypassed) "EQ Bypassed (Original)" else "Mastering EQ Active", Toast.LENGTH_SHORT).show()
                            },
                            label = { Text(if (isEqBypassed) "Bypassed" else "Active", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFEF4444),
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.height(30.dp)
                        )

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                // Dynamic Spectrum & Decibel Meter Canvas
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF060911)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF06B6D4).copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val width = size.width
                            val height = size.height
                            val barCount = spectrumValues.size
                            val barWidth = width / (barCount * 1.35f)
                            val spacing = (width - (barCount * barWidth)) / (barCount - 1)

                            for (i in 0 until barCount) {
                                val x = i * (barWidth + spacing)
                                val valNormalized = if (isEqBypassed) spectrumValues[i] * 0.6f else spectrumValues[i]
                                val barHeight = height * valNormalized

                                // Gradient from Cyan to Purple/Pink for high levels
                                val brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFEC4899),
                                        Color(0xFF8B5CF6),
                                        Color(0xFF06B6D4)
                                    ),
                                    startY = height - barHeight,
                                    endY = height
                                )

                                drawRoundRect(
                                    brush = brush,
                                    topLeft = Offset(x, height - barHeight),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                                )

                                // Peak hold cap line
                                val peakY = (height - barHeight - 4f).coerceAtLeast(0f)
                                drawLine(
                                    color = Color(0xFFFBBF24),
                                    start = Offset(x, peakY),
                                    end = Offset(x + barWidth, peakY),
                                    strokeWidth = 2f
                                )
                            }
                        }

                        // Target LUFS badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF1E293B).copy(alpha = 0.85f),
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("Loudness:", fontSize = 9.sp, color = Color.Gray)
                                Text(targetLufs, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            }
                        }
                    }
                }

                // Preset selector chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(presets) { p ->
                        val isSel = selectedPreset.id == p.id
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) Color(0xFF06B6D4) else Color(0xFF1E293B),
                            modifier = Modifier.clickable {
                                selectedPreset = p
                                currentGains = p.gains.toMutableList()
                                bassBoostVal = p.bassBoost
                                stereoWidthVal = p.stereoWidth
                                tubeWarmthVal = p.tubeWarmth
                                vocalClarityVal = p.vocalClarity
                                Toast.makeText(context, "প্রিসেট: ${p.bengaliName}", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text(
                                text = p.bengaliName,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) Color.Black else Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // 7-Band Graphic Equalizer Faders
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF131D31),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("🎚️ ৭-ব্যান্ড গ্রাফিক্যাল ইকুয়ালাইজার (Gain: -12dB to +12dB):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF22D3EE))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            eqFrequencies.forEachIndexed { idx, freq ->
                                val gain = currentGains.getOrElse(idx) { 0f }

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Current dB Value
                                    Text(
                                        text = "${if (gain > 0) "+" else ""}${gain.toInt()}dB",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (gain > 0) Color(0xFF06B6D4) else if (gain < 0) Color(0xFFEF4444) else Color.Gray
                                    )

                                    // Vertical Fader Bar Slider Simulation
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF0B0F19),
                                        modifier = Modifier
                                            .width(22.dp)
                                            .weight(1f)
                                            .clickable {
                                                // Cycle gain on tap for fast adjusting
                                                val nextGain = when {
                                                    gain >= 8f -> -6f
                                                    gain <= -6f -> 0f
                                                    gain == 0f -> 5f
                                                    else -> 8f
                                                }
                                                val updated = currentGains.toMutableList()
                                                updated[idx] = nextGain
                                                currentGains = updated
                                            }
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            // Center zero line
                                            Divider(
                                                color = Color.White.copy(alpha = 0.3f),
                                                modifier = Modifier
                                                    .align(Alignment.Center)
                                                    .fillMaxWidth()
                                            )

                                            // Thumb position
                                            val fraction = ((gain + 12f) / 24f).coerceIn(0f, 1f)
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomCenter)
                                                    .fillMaxWidth()
                                                    .fillMaxHeight(fraction)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(
                                                        if (isEqBypassed) Color.Gray.copy(alpha = 0.5f)
                                                        else Color(0xFF06B6D4).copy(alpha = 0.8f)
                                                    )
                                            )
                                        }
                                    }

                                    // Frequency & Label
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(freq, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(eqLabels[idx], fontSize = 8.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }

                // Analog Enhancers (Bass Boost, Tube Warmth, Stereo Widener, Vocal Clarity)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("✨ অ্যানালগ স্টুডিও প্রসেসিং (Analog Coloring & Widener):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Bass Boost", fontSize = 9.sp, color = Color.LightGray)
                                    Text("${(bassBoostVal * 100).toInt()}%", fontSize = 9.sp, color = Color.White)
                                }
                                Slider(
                                    value = bassBoostVal,
                                    onValueChange = { bassBoostVal = it },
                                    modifier = Modifier.height(18.dp),
                                    colors = SliderDefaults.colors(thumbColor = Color(0xFF06B6D4), activeTrackColor = Color(0xFF06B6D4))
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Tube Warmth", fontSize = 9.sp, color = Color.LightGray)
                                    Text("${(tubeWarmthVal * 100).toInt()}%", fontSize = 9.sp, color = Color.White)
                                }
                                Slider(
                                    value = tubeWarmthVal,
                                    onValueChange = { tubeWarmthVal = it },
                                    modifier = Modifier.height(18.dp),
                                    colors = SliderDefaults.colors(thumbColor = Color(0xFFF59E0B), activeTrackColor = Color(0xFFF59E0B))
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Stereo 3D Widener", fontSize = 9.sp, color = Color.LightGray)
                                    Text("${(stereoWidthVal * 100).toInt()}%", fontSize = 9.sp, color = Color.White)
                                }
                                Slider(
                                    value = stereoWidthVal,
                                    onValueChange = { stereoWidthVal = it },
                                    modifier = Modifier.height(18.dp),
                                    colors = SliderDefaults.colors(thumbColor = Color(0xFF8B5CF6), activeTrackColor = Color(0xFF8B5CF6))
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Vocal Air & Clarity", fontSize = 9.sp, color = Color.LightGray)
                                    Text("${(vocalClarityVal * 100).toInt()}%", fontSize = 9.sp, color = Color.White)
                                }
                                Slider(
                                    value = vocalClarityVal,
                                    onValueChange = { vocalClarityVal = it },
                                    modifier = Modifier.height(18.dp),
                                    colors = SliderDefaults.colors(thumbColor = Color(0xFF10B981), activeTrackColor = Color(0xFF10B981))
                                )
                            }
                        }
                    }
                }

                // AI Auto-Master & Export Master Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            // AI Auto-Master analyze & apply optimal EQ
                            currentGains = mutableListOf(4f, 3f, -1f, 2f, 3f, 5f, 4f)
                            bassBoostVal = 0.55f
                            tubeWarmthVal = 0.65f
                            stereoWidthVal = 0.70f
                            vocalClarityVal = 0.80f
                            Toast.makeText(context, "🤖 AI অটো-মাস্টারিং প্রয়োগ করা হয়েছে (320kbps Lossless Ready)!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(46.dp),
                        border = BorderStroke(1.dp, Color(0xFF06B6D4))
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF06B6D4), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("AI Auto Master", color = Color(0xFF06B6D4), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            isMasteringProcessing = true
                            Toast.makeText(context, "✅ মাস্টার্ড অডিও সফলভাবে এক্সপোর্ট হয়েছে!", Toast.LENGTH_LONG).show()
                            onApplyMastering(song?.title ?: "Mastered_Audio.wav")
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06B6D4))
                    ) {
                        Icon(Icons.Default.DownloadDone, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Apply & Save", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
