package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Mega Pro Studio Suite Hub (60 Real Professional Features across 6 Elite Categories)
 * Architecture: Senior Android Clean Modular Composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MegaProStudioSuiteHub(
    onDismiss: () -> Unit,
    onExecuteAction: suspend (featureName: String) -> String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedCategoryIndex by remember { mutableStateOf(0) }
    var activeExecutingFeature by remember { mutableStateOf<String?>(null) }
    var executionResultText by remember { mutableStateOf<String?>(null) }

    val categories = listOf(
        "🎛️ DSP & FX Rack (1-10)",
        "🤖 AI Composition (11-20)",
        "🎧 Hardware & Live (21-30)",
        "🥁 Beat & Synth Lab (31-40)",
        "📊 Master & Broadcast (41-50)",
        "☁️ Cloud & Collab (51-60)"
    )

    // 60 Detailed Feature Definitions
    val featuresMap = mapOf(
        0 to listOf(
            "1. Parametric 10-Band Graphic Equalizer",
            "2. Convolution Reverb Impulse Response Processor",
            "3. Stereo Widener & Haas Effect Processor",
            "4. Transient Shaper (Attack & Sustain Boost)",
            "5. Tube Saturation & Harmonic Distortion Unit",
            "6. Auto-Wah & Envelope Filter Modulator",
            "7. Chorus & Ensemble Multi-Voice Doubler",
            "8. Flanger & Jet Sweep Modulation FX",
            "9. Phaser & Frequency Notch Sweeper",
            "10. Bitcrusher & Sample Rate Downsampler"
        ),
        1 to listOf(
            "11. AI Melody Generator via Gemini",
            "12. AI Bassline Groove Arranger",
            "13. AI Drum Pattern Groove Quantizer",
            "14. AI Vocal Harmony Stacking Generator",
            "15. AI Counterpoint & Counter-Melody Creator",
            "16. AI Song Structure Formatter (Verse-Chorus-Bridge)",
            "17. AI Lyric Sentiment Analyzer & Mood Tuner",
            "18. AI Genre Fusion Synthesizer",
            "19. AI Acoustic Instrument Modeling Assistant",
            "20. AI Master Mixing Advisor & EQ Suggestion Engine"
        ),
        2 to listOf(
            "21. Live MIDI Controller Input Listener",
            "22. Bluetooth BLE Audio Device Pairing & Routing",
            "23. Headphone Latency & Buffer Size Optimizer",
            "24. Low-Latency Audio Stream Configuration",
            "25. Real-Time Audio VU Meter & Peak Clipper",
            "26. Live Monitor Direct Feed Toggle",
            "27. Stage Performance Setlist Manager",
            "28. Live Lyrics Teleprompter Scroll Mode",
            "29. USB Audio Interface Direct Input Handler",
            "30. Hardware Footswitch MIDI Mapping"
        ),
        3 to listOf(
            "31. 808 Sub-Bass Frequency Synthesizer",
            "32. Analog Subtractive Synth Lead Designer",
            "33. FM Synthesis Operator Matrix",
            "34. Wavetable Morphing Synthesizer Pad",
            "35. Arpeggiator Pattern Generator (Up/Down/Random)",
            "36. Swing & Groove Timing Quantization",
            "37. Drum Roll & Roller Repeater Effect",
            "38. Percussion One-Shot Pitch Envelope",
            "39. Modular Patch Cable Routing Simulator",
            "40. Custom Drum Kit Sample Mapper"
        ),
        4 to listOf(
            "41. Loudness LUFS Target Metering (-14 LUFS Spotify)",
            "42. True Peak Limiter & Intersample Clipper Preventer",
            "43. Multi-Band Crossover Frequency Splitter",
            "44. Mid/Side (M/S) Stereo Matrix Mastering",
            "45. Spectral Balance & Tonal Equalizer Corrector",
            "46. Harmonic Exciter & Air Band Enhancer",
            "47. Stereo Field Correlator & Phase Meter",
            "48. Dither & Bit Depth Reduction Engine (24 to 16-bit)",
            "49. Broadcast Radio Compression Preset Suite",
            "50. Vinyl Warmth & Tape Saturation Emulator"
        ),
        5 to listOf(
            "51. Cloud Project Version History & Rollback",
            "52. Stem Stash Cloud Storage & Versioning",
            "53. Artist Profile & Portfolio Showcase Card",
            "54. Royalty & Copyright Split Sheet Calculator",
            "55. Project License & Creative Commons Tagging",
            "56. Session Activity Audit Log & Timeline",
            "57. Secure Peer-to-Peer Project Export Bundle (.zip)",
            "58. Automated Backup Scheduler (Daily/Weekly)",
            "59. Cloud Stems Shared Workspace Permission Manager",
            "60. Studio Master Analytics & Listening Stats Dashboard"
        )
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.95f),
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(color = MaterialTheme.colorScheme.primary, shape = CircleShape) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(8.dp).size(20.dp))
                    }
                    Text("Mega Pro Studio Suite (60 Features)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Select from 60 professional real-world production, DSP, AI, and hardware features:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                // Category Selector Tabs
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories.size) { idx ->
                        val isSel = selectedCategoryIndex == idx
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedCategoryIndex = idx },
                            label = { Text(categories[idx], fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                HorizontalDivider()

                // Feature List for Selected Category
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    val currentFeatures = featuresMap[selectedCategoryIndex] ?: emptyList()
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(currentFeatures) { featureName ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.fillMaxWidth().clickable {
                                    scope.launch {
                                        activeExecutingFeature = featureName
                                        executionResultText = "Executing $featureName..."
                                        val res = withContext(Dispatchers.IO) {
                                            onExecuteAction(featureName)
                                        }
                                        executionResultText = res
                                        activeExecutingFeature = null
                                    }
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(featureName, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                if (executionResultText != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Execution Result / Status:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(executionResultText!!, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                Text("Close Suite")
            }
        }
    )
}
