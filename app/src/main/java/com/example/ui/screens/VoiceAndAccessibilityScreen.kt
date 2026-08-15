package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceAndAccessibilityScreen(
    appLanguage: String = "en",
    voiceConfig: VoiceCommandConfigData,
    arKaraoke: ArKaraokeData,
    noiseData: NoiseCancellationData,
    accessData: AccessibilitySettingsData
) {
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(voiceConfig.isListening) }
    var recognizedText by remember { mutableStateOf(voiceConfig.recognizedText) }
    var isRnNoisePro by remember { mutableStateOf(noiseData.isRnNoiseProEnabled) }
    var isVoiceGuide by remember { mutableStateOf(accessData.isVoiceGuidanceEnabled) }
    var isHapticBass by remember { mutableStateOf(accessData.isHapticBassVibrationEnabled) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Banner
        Surface(
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = Color(0xFF06B6D4))
                        Text(
                            text = if (appLanguage == "bn") "ভয়েস ও অ্যাক্সেসিবিলিটি" else "Voice Commands & Accessibility",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = if (appLanguage == "bn") "৬টি ভয়েস ও অ্যাক্সেসিবিলিটি কন্ট্রোল ফিচার" else "6 Voice Control, AR Karaoke & RNNoise Neural Denoise Features",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )
                }
                Badge(containerColor = Color(0xFF06B6D4)) {
                    Text("MODULE 14", modifier = Modifier.padding(2.dp), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 1. Voice Commands (Speech-To-Text)
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🗣️ Voice Command Control (Speech-to-Text)", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Language: ${voiceConfig.speechToTextLanguage}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = recognizedText,
                    onValueChange = { recognizedText = it },
                    label = { Text("Recognized Voice Command") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Last Executed: ${voiceConfig.lastCommandExecuted}", fontSize = 11.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        isListening = !isListening
                        Toast.makeText(context, if (isListening) "Listening for Bangla voice commands..." else "Voice Command Executed!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isListening) Color.Red else MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(if (isListening) Icons.Default.Stop else Icons.Default.Mic, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isListening) "Stop Listening" else "Speak Voice Command")
                }
            }
        }

        // 2. AR Karaoke (ARCore)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Camera, contentDescription = null, tint = Color(0xFFEC4899))
                    Text("🎤 AR Karaoke (ARCore 3D Lyrics)", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Song: ${arKaraoke.activeSongTitle}", fontSize = 13.sp, color = Color.LightGray)
                Text("AR Engine: ${arKaraoke.arCoreStatus}", fontSize = 12.sp, color = Color(0xFFEC4899))
                Text("Filter: ${arKaraoke.filterPreset}", fontSize = 11.sp, color = Color.Cyan)

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { Toast.makeText(context, "ARCore Camera Opened! 3D Lyrics Overlay Ready", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Videocam, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Launch AR Karaoke Camera", color = Color.White)
                }
            }
        }

        // 3. Background Noise Remove & Pro RNNoise
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🎙️ Background Noise Suppression (RNNoise Neural)", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Text("• Standard Denoise Suppression: ${noiseData.standardDenoiseDb} dB", fontSize = 12.sp)
                Text("• RNNoise Pro Neural Denoise: ${noiseData.rnNoiseProNeuralDenoiseDb} dB", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                Text("• Sample Rate: ${noiseData.sampleRateHz} Hz Studio Quality", fontSize = 11.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable RNNoise Pro Neural Denoise", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Switch(
                        checked = isRnNoisePro,
                        onCheckedChange = {
                            isRnNoisePro = it
                            Toast.makeText(context, "RNNoise Pro Denoise ${if (it) "ENABLED (-32dB)" else "DISABLED"}!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        // 4. Accessibility Options
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("♿ Accessibility & Haptic Feedback", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Screen Reader Voice Guidance", fontSize = 13.sp)
                    Switch(checked = isVoiceGuide, onCheckedChange = { isVoiceGuide = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Dynamic Haptic Bass Vibration", fontSize = 13.sp)
                    Switch(checked = isHapticBass, onCheckedChange = { isHapticBass = it })
                }
            }
        }
    }
}
