package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Live AI Duet & Collaboration Studio
 * Allows singing or generating multi-artist collaborative tracks with real-time AI harmony.
 */
@Composable
fun LiveDuetStudioDialog(
    onDismiss: () -> Unit,
    onStartDuet: (partnerVoice: String, harmonyStyle: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedVoice by remember { mutableStateOf("Shreya AI (Melodic Classical)") }
    var selectedHarmony by remember { mutableStateOf("3rd Interval Higher Harmony") }
    var autoPitchCorrection by remember { mutableStateOf(true) }
    var isGeneratingSession by remember { mutableStateOf(false) }

    val virtualPartners = listOf(
        Pair("Shreya AI (Melodic Classical)", "Sweet lyrical voice, perfect for romantic and semi-classical melodies"),
        Pair("Arijit AI (Soulful Acoustic)", "Rich emotive texture with breathy acoustic delivery"),
        Pair("Folk Baul AI (Traditional)", "High-octave authentic Bengali folk and mystic baul cadence"),
        Pair("Cyber Trap Vocalist AI", "Autotuned punchy rap cadence with energetic adlibs")
    )

    val harmonyStyles = listOf(
        "3rd Interval Higher Harmony",
        "Octave Lower Sub-Double",
        "5th Harmonic Chorus Expansion",
        "Unison Double Tracking (Wide Stereo)"
    )

    Dialog(onDismissRequest = { if (!isGeneratingSession) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0F172A),
            tonalElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.GroupAdd, contentDescription = null, tint = Color(0xFFEC4899))
                        Column {
                            Text("Live AI Duet Studio", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                            Text("Multi-Artist AI Collaboration Engine", fontSize = 11.sp, color = Color.LightGray)
                        }
                    }
                    IconButton(onClick = onDismiss, enabled = !isGeneratingSession) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Select AI Duet Partner
                Text("Select Virtual Duet Partner:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    virtualPartners.forEach { (name, desc) ->
                        val isSel = selectedVoice == name
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedVoice = name },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) Color(0xFFEC4899).copy(alpha = 0.2f) else Color(0xFF1E293B),
                            border = if (isSel) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEC4899)) else null
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(
                                    imageVector = if (isSel) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isSel) Color(0xFFEC4899) else Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                    Text(desc, fontSize = 10.sp, color = Color.LightGray)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Harmony & Auto-Tune Settings
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("AI Harmony Arrangement:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedHarmony, fontSize = 11.sp, color = Color.White)
                            TextButton(onClick = {
                                val nextIndex = (harmonyStyles.indexOf(selectedHarmony) + 1) % harmonyStyles.size
                                selectedHarmony = harmonyStyles[nextIndex]
                            }) {
                                Text("Change", fontSize = 11.sp, color = Color(0xFF38BDF8))
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFF334155))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Real-Time Pitch Snap (Auto-Tune)", fontSize = 11.sp, color = Color.LightGray)
                            Switch(
                                checked = autoPitchCorrection,
                                onCheckedChange = { autoPitchCorrection = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF10B981))
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Launch Duet Session
                Button(
                    onClick = {
                        isGeneratingSession = true
                        scope.launch {
                            delay(1000)
                            isGeneratingSession = false
                            onStartDuet(selectedVoice, selectedHarmony)
                            Toast.makeText(context, "AI Duet session created with $selectedVoice!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    },
                    enabled = !isGeneratingSession,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    if (isGeneratingSession) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Connecting Duet Studio...", fontSize = 12.sp)
                    } else {
                        Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Live Duet Recording", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
