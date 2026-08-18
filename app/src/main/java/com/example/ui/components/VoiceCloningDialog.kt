package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VoiceCloningDialog(
    onDismiss: () -> Unit,
    onVoiceCloned: (voiceName: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var voiceName by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var recordingProgress by remember { mutableFloatStateOf(0f) }
    var isSuccess by remember { mutableStateOf(false) }

    // Pulse animation for recording
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingProgress = 0f
            while (recordingProgress < 1f) {
                delay(100) // 10 seconds total to record
                recordingProgress += 0.01f
            }
            isRecording = false
            isAnalyzing = true
            delay(3000) // Simulate AI training
            isAnalyzing = false
            isSuccess = true
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI Voice Cloning", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Record a 10-second sample of your voice. Our AI will learn your vocal timbre, pitch, and accent so you can sing any song in your own voice!",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = voiceName,
                    onValueChange = { voiceName = it },
                    placeholder = { Text("Name this voice (e.g., My Voice)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    singleLine = true,
                    enabled = !isRecording && !isAnalyzing && !isSuccess
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (isSuccess) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Voice Successfully Cloned!", fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            onVoiceCloned(if (voiceName.isBlank()) "My Custom Voice" else voiceName)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Use This Voice")
                    }
                } else if (isAnalyzing) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("AI is mapping your vocal characteristics...", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                } else {
                    // Record Button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(100.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(if (isRecording) Color(0xFFEF4444).copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .border(2.dp, if (isRecording) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary, CircleShape)
                            .clickable(enabled = voiceName.isNotBlank() && !isRecording) {
                                isRecording = true
                            }
                    ) {
                        Icon(
                            imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = "Record",
                            tint = if (isRecording) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isRecording) {
                        LinearProgressIndicator(
                            progress = { recordingProgress },
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFEF4444),
                            trackColor = Color(0xFFEF4444).copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Reading sample... Please speak clearly.", fontSize = 12.sp, color = Color(0xFFEF4444))
                    } else {
                        Text(
                            text = if (voiceName.isBlank()) "Enter a name to start recording" else "Tap to start 10s recording",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
