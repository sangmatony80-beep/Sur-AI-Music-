package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.data.audio.RealAudioPlaybackManager
import com.example.data.local.SongEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VoiceCorrectionScreen(
    appLanguage: String = "bn",
    onNavigateToCreateSong: (vocalTone: String, scale: String) -> Unit = { _, _ -> },
    onSaveToDownloads: (title: String, audioUrl: String) -> Unit = { _, _ -> },
    onSaveVoiceRecord: ((title: String, scale: String, tone: String, retuneSpeed: Float, pitchShift: Float) -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isBangla = appLanguage == "bn"

    // Audio Input States
    var selectedInputMode by rememberSaveable { mutableStateOf("mic") } // "mic", "file", "sample"
    var isRecording by rememberSaveable { mutableStateOf(false) }
    var isRecordingPaused by rememberSaveable { mutableStateOf(false) }
    var recordingDurationSeconds by rememberSaveable { mutableStateOf(0) }
    var uploadedFileName by rememberSaveable { mutableStateOf("raw_vocal_take_01.wav") }
    var uploadedFileSize by rememberSaveable { mutableStateOf("2.4 MB (24-bit / 44.1kHz)") }
    var hasAudioSource by rememberSaveable { mutableStateOf(true) }

    // Pitch & Tuning Parameters
    var selectedScale by rememberSaveable { mutableStateOf("C Major (পপ ও আধুনিক)") }
    val scaleOptions = listOf(
        "C Major (পপ ও আধুনিক)",
        "A Minor (মেলানকলিক)",
        "Raag Bhairavi (রাগ ভৈরবী)",
        "Raag Yaman (রাগ ইমন)",
        "Raag Bilaval (রাগ বিলাবল)",
        "Auto-Detect Scale (এআই সনাক্তকরণ)",
        "Chromatic (সব নোট)"
    )

    var retuneSpeed by rememberSaveable { mutableStateOf(0.85f) } // 0.0 (Natural) to 1.0 (Hard Auto-Tune)
    var pitchShiftSemitones by rememberSaveable { mutableStateOf(0f) } // -12 to +12 semitones
    var pitchDriftCorrection by rememberSaveable { mutableStateOf(0.70f) } // 0.0 to 1.0

    // Vocal Tone & FX Presets
    var selectedVocalTone by rememberSaveable { mutableStateOf("সুরের জাদু (Melodious Sweetness)") }
    val vocalToneOptions = listOf(
        "সুরের জাদু (Melodious Sweetness)",
        "উষ্ণ বাউল ও ফোক (Warm Baul Folk)",
        "রেশমি গজল ও খেয়াল (Silk Ghazal)",
        "ক্রিস্টাল স্টুдио পপ (Crystal Pop)",
        "গভীর ক্লাসিক্যাল (Deep Classical)",
        "ডুয়াল অক্টেভ হারমোনি (Dual Octave)"
    )

    var reverbLevel by rememberSaveable { mutableStateOf(0.55f) }
    var spatialEchoLevel by rememberSaveable { mutableStateOf(0.30f) }
    var enable3DHarmonizer by rememberSaveable { mutableStateOf(true) }
    var enableDeEsser by rememberSaveable { mutableStateOf(true) }
    var enableNoiseSuppression by rememberSaveable { mutableStateOf(true) }

    // Transformation Processing State
    var isProcessing by rememberSaveable { mutableStateOf(false) }
    var processingProgress by rememberSaveable { mutableStateOf(0f) }
    var processingStepText by rememberSaveable { mutableStateOf("") }
    var isTuningCompleted by rememberSaveable { mutableStateOf(false) }

    // Playback Manager for A/B Comparison
    var isPlayingOriginal by rememberSaveable { mutableStateOf(false) }
    var isPlayingTuned by rememberSaveable { mutableStateOf(false) }
    val audioPlaybackManager = remember { RealAudioPlaybackManager(context) }

    DisposableEffect(Unit) {
        onDispose {
            audioPlaybackManager.stop()
        }
    }

    // Timer logic for mic recording
    LaunchedEffect(isRecording, isRecordingPaused) {
        if (isRecording && !isRecordingPaused) {
            while (isRecording && !isRecordingPaused && recordingDurationSeconds < 60) {
                delay(1000)
                recordingDurationSeconds++
            }
            if (recordingDurationSeconds >= 60) {
                isRecording = false
                Toast.makeText(context, if (isBangla) "৬০ সেকেন্ড রেকর্ড সম্পন্ন হয়েছে!" else "60s Recording Complete!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // 1. Hero Title Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = Color.White.copy(alpha = 0.25f),
                                    shape = CircleShape
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .size(28.dp),
                                        tint = Color.White
                                    )
                                }

                                Surface(
                                    color = Color.Black.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = if (isBangla) "AI Pitch Engine 2.0" else "AI Pitch Engine 2.0",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = if (isBangla) "ভয়েস কারেকশন ও অটো-টিউন স্টুডিও" else "Voice Correction & Auto-Tune Studio",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Text(
                                text = if (isBangla) "যে কোনো সাধারণ বা বেসুরো গলার রেকর্ডিং আপলোড করুন বা সরাসরি গাওয়ান—মুহূর্তেই নিখুঁত মেলোডিয়াস সুরে পরিবর্তন করুন ✨"
                                else "Upload or record raw vocals and instantly quantize pitch, fix off-key notes, and apply studio-grade tone polish ✨",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            // 2. Audio Input Selection (Mic / File Upload / Sample)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = if (isBangla) "১. অডিও ইনপুট বা রেকর্ড নির্বাচন করুন" else "1. Select or Record Audio Source",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Segmented Tab Buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val tabs = listOf(
                                "mic" to (if (isBangla) "মাইক রেকর্ড" else "Mic Record"),
                                "file" to (if (isBangla) "ফাইল আপলোড" else "Upload File"),
                                "sample" to (if (isBangla) "নমুনা ভয়েস" else "Sample Voice")
                            )

                            tabs.forEach { (mode, label) ->
                                val selected = selectedInputMode == mode
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedInputMode = mode },
                                    color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // Content for Selected Input Mode
                        when (selectedInputMode) {
                            "mic" -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Live Waveform Visualizer
                                    LivePitchWaveformCanvas(
                                        isRecording = isRecording && !isRecordingPaused,
                                        isTuned = isTuningCompleted
                                    )

                                    Text(
                                        text = if (isRecording) {
                                            if (isRecordingPaused) "রেকর্ডিং সাময়িক পজ আছে (${recordingDurationSeconds}s)"
                                            else "মাইক রেকর্ডিং চলছে: ${recordingDurationSeconds}s"
                                        } else "রেকর্ড করতে নিচের লাল মাইক বাটনে চাপুন",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = {
                                                if (isRecording) {
                                                    isRecording = false
                                                    isRecordingPaused = false
                                                    hasAudioSource = true
                                                    Toast.makeText(context, if (isBangla) "রেকর্ডিং সম্পন্ন!" else "Recording Saved!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    isRecording = true
                                                    isRecordingPaused = false
                                                    recordingDurationSeconds = 0
                                                    isTuningCompleted = false
                                                    hasAudioSource = true
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                                contentDescription = null
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isRecording) (if (isBangla) "রেকর্ড থামান" else "Stop Record")
                                                else (if (isBangla) "রেকর্ড শুরু করুন" else "Start Mic"),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        if (isRecording) {
                                            OutlinedButton(
                                                onClick = { isRecordingPaused = !isRecordingPaused },
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isRecordingPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                                    contentDescription = null
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = if (isRecordingPaused) "চালিয়ে যান" else "পজ")
                                            }
                                        }
                                    }
                                }
                            }

                            "file" -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                            .clickable {
                                                uploadedFileName = "my_vocal_track_${System.currentTimeMillis() % 1000}.wav"
                                                uploadedFileSize = "3.8 MB (24-bit 44.1kHz)"
                                                hasAudioSource = true
                                                isTuningCompleted = false
                                                Toast.makeText(context, if (isBangla) "অডিও ফাইল লোড হয়েছে: $uploadedFileName" else "Loaded: $uploadedFileName", Toast.LENGTH_SHORT).show()
                                            },
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.UploadFile,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(28.dp)
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = uploadedFileName,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                                Text(
                                                    text = uploadedFileSize,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Text(
                                                text = if (isBangla) "ব্রাউজ করুন" else "Browse",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            "sample" -> {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val samples = listOf(
                                        "বেসুরো বাউল গান (Off-pitch Baul Vocal)",
                                        "কাঁচা পপ সোলো (Raw Pop Scratch)",
                                        "খেয়াল তান নমুনা (Classical Kheyal Tan)"
                                    )
                                    samples.forEach { sampleName ->
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    uploadedFileName = sampleName
                                                    uploadedFileSize = "Sample Audio Track"
                                                    hasAudioSource = true
                                                    isTuningCompleted = false
                                                    Toast.makeText(context, if (isBangla) "নমুনা ভয়েস নির্বাচন করা হয়েছে!" else "Sample Loaded!", Toast.LENGTH_SHORT).show()
                                                },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (uploadedFileName == sampleName) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                            border = if (uploadedFileName == sampleName) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AudioFile,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text(
                                                    text = sampleName,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Pitch & Tuning Controls (Scale / Retune Speed / Pitch Shift / Vibrato)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = if (isBangla) "২. স্কেল কোয়ান্টাইজার ও পিচ টিউনিং" else "2. Pitch Quantizer & Scale Tuning",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Scale Selector Chips
                        Text(
                            text = if (isBangla) "টার্গেট স্কেল বা রাগ লকিং:" else "Target Scale or Raga Lock:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(scaleOptions) { scale ->
                                FilterChip(
                                    selected = selectedScale == scale,
                                    onClick = { selectedScale = scale },
                                    label = { Text(scale, fontSize = 11.sp) }
                                )
                            }
                        }

                        // Retune Speed Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isBangla) "অটো-টিউন কারেকশন স্পিড:" else "Retune Speed:",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${(retuneSpeed * 100).toInt()}% (${if (retuneSpeed > 0.8f) (if (isBangla) "নিখুঁত সুরেল" else "Hard Pitch Lock") else (if (isBangla) "প্রাকৃতিক" else "Soft Natural")})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = retuneSpeed,
                                onValueChange = { retuneSpeed = it },
                                valueRange = 0.1f..1.0f
                            )
                        }

                        // Pitch Shift / Formant Shifting Slider (-12 to +12)
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isBangla) "পিচ শিফটার (Formant/Octave):" else "Pitch Shift (Formant/Octave):",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                val pitchText = when {
                                    pitchShiftSemitones.toInt() == 0 -> if (isBangla) "নিউট্রাল (Original)" else "Neutral (0 ST)"
                                    pitchShiftSemitones > 0 -> "+${pitchShiftSemitones.toInt()} ST (${if (isBangla) "উঁচু মেয়েলি/সোপ্রানো" else "High Female/Soprano"})"
                                    else -> "${pitchShiftSemitones.toInt()} ST (${if (isBangla) "গভীর পুরুষালি/বাস" else "Deep Male/Bass"})"
                                }
                                Text(
                                    text = pitchText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = pitchShiftSemitones,
                                onValueChange = { pitchShiftSemitones = it },
                                valueRange = -12f..12f,
                                steps = 23
                            )
                        }

                        // Pitch Drift & Vibrato Smoother
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isBangla) "কম্পন ও ড্রিফট স্ট্যাবিলাইজার:" else "Pitch Drift & Vibrato Smoother:",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${(pitchDriftCorrection * 100).toInt()}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = pitchDriftCorrection,
                                onValueChange = { pitchDriftCorrection = it },
                                valueRange = 0.2f..1.0f
                            )
                        }
                    }
                }
            }

            // 4. Vocal Tone & FX Sweeteners
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = if (isBangla) "৩. মিষ্টি কন্ঠের মেজাজ ও স্টুডিও ইফেক্ট" else "3. Vocal Tone & Studio Effects",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Vocal Tone Chips
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(vocalToneOptions) { tone ->
                                SuggestionChip(
                                    onClick = { selectedVocalTone = tone },
                                    label = {
                                        Text(
                                            text = tone,
                                            fontSize = 11.sp,
                                            fontWeight = if (selectedVocalTone == tone) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    border = if (selectedVocalTone == tone) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                                )
                            }
                        }

                        // Reverb Level
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = if (isBangla) "স্টুডিও রিভার্ব:" else "Studio Reverb:", fontSize = 12.sp)
                                Text(text = "${(reverbLevel * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Slider(value = reverbLevel, onValueChange = { reverbLevel = it })
                        }

                        // Spatial Echo
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = if (isBangla) "স্পেশিয়াল ইকো (Delay):" else "Spatial Delay Echo:", fontSize = 12.sp)
                                Text(text = "${(spatialEchoLevel * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Slider(value = spatialEchoLevel, onValueChange = { spatialEchoLevel = it })
                        }

                        // Toggles Row (3D Chorus / De-Esser / Noise Suppression)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            FilterChip(
                                selected = enable3DHarmonizer,
                                onClick = { enable3DHarmonizer = !enable3DHarmonizer },
                                label = { Text(if (isBangla) "৩ডি কোরাস" else "3D Chorus", fontSize = 11.sp) },
                                leadingIcon = {
                                    if (enable3DHarmonizer) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            )

                            FilterChip(
                                selected = enableDeEsser,
                                onClick = { enableDeEsser = !enableDeEsser },
                                label = { Text(if (isBangla) "ডি-এসার" else "De-Esser", fontSize = 11.sp) },
                                leadingIcon = {
                                    if (enableDeEsser) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            )

                            FilterChip(
                                selected = enableNoiseSuppression,
                                onClick = { enableNoiseSuppression = !enableNoiseSuppression },
                                label = { Text(if (isBangla) "নয়েজ ক্লিন" else "Noise Clean", fontSize = 11.sp) },
                                leadingIcon = {
                                    if (enableNoiseSuppression) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            )
                        }
                    }
                }
            }

            // 5. Interactive Pitch Curve Graph (Raw vs Quantized Curve)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isBangla) "৪. পিচ কার্ভ ও স্পেকট্রাম বিশ্লেষণ" else "4. Pitch Curve & Spectrum Graph",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(modifier = Modifier.size(8.dp).background(Color(0xFFFF5252), CircleShape))
                                    Text(text = if (isBangla) "কাঁচা অফ-পিচ" else "Raw Pitch", fontSize = 10.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF1DB954), CircleShape))
                                    Text(text = if (isBangla) "অটো-টিউনড" else "Auto-Tuned", fontSize = 10.sp)
                                }
                            }
                        }

                        PitchComparisonGraphCanvas(isTuned = isTuningCompleted)
                    }
                }
            }

            // 6. Processing Actions & A/B Comparison Player
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (isProcessing) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                LinearProgressIndicator(
                                    progress = { processingProgress },
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = processingStepText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else if (!isTuningCompleted) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        isProcessing = true
                                        processingProgress = 0.2f
                                        processingStepText = if (isBangla) "১/৩: অফ-পিচ ও ফ্রিকোয়েন্সি স্পেকট্রাম স্ক্যান করা হচ্ছে..." else "1/3: Scanning raw pitch frequency spectrum..."
                                        delay(600)
                                        processingProgress = 0.6f
                                        processingStepText = if (isBangla) "২/৩: $selectedScale স্কেলে নোট কোয়ান্টাইজেশন হচ্ছে..." else "2/3: Quantizing notes to $selectedScale..."
                                        delay(700)
                                        processingProgress = 0.95f
                                        processingStepText = if (isBangla) "৩/৩: $selectedVocalTone ও স্টুডিও রিভার্ব ইফেক্ট রেন্ডার হচ্ছে..." else "3/3: Applying $selectedVocalTone & studio reverb..."
                                        delay(500)
                                        isProcessing = false
                                        isTuningCompleted = true
                                        onSaveVoiceRecord?.invoke(uploadedFileName, selectedScale, selectedVocalTone, retuneSpeed, pitchShiftSemitones)
                                        Toast.makeText(context, if (isBangla) "কন্ঠ নিখুঁত মেলোডিয়াস করা সফল হয়েছে!" else "Vocal Auto-Tuned Successfully!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBangla) "কন্ঠ শ্রুতিমধুর করুন (Apply Auto-Tune)" else "Apply Pitch Correction & Auto-Tune",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            // A/B Comparison Playback Controls
                            Text(
                                text = if (isBangla) "✓ কন্ঠ সফলভাবে শ্রুতিমধুর করা হয়েছে! তুলনামূলকভাবে শুনুন:" else "✓ Voice Auto-Tuned Successfully! Compare A/B:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Original Raw Audio Playback
                                OutlinedButton(
                                    onClick = {
                                        if (isPlayingOriginal) {
                                            audioPlaybackManager.stop()
                                            isPlayingOriginal = false
                                        } else {
                                            isPlayingTuned = false
                                            isPlayingOriginal = true
                                            audioPlaybackManager.play(
                                                SongEntity(
                                                    id = 881,
                                                    title = "Original Raw Vocal",
                                                    artist = "User Vocal Take",
                                                    genre = "Raw",
                                                    audioUrl = "http://raw.vocal.demo",
                                                    duration = "0:20",
                                                    imageUrl = "",
                                                    lyrics = "[Raw Off-Pitch Recording]"
                                                )
                                            )
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPlayingOriginal) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = if (isBangla) "আসল বেসুরো" else "Play Raw", fontSize = 11.sp)
                                }

                                // Tuned Melodious Audio Playback
                                Button(
                                    onClick = {
                                        if (isPlayingTuned) {
                                            audioPlaybackManager.stop()
                                            isPlayingTuned = false
                                        } else {
                                            isPlayingOriginal = false
                                            isPlayingTuned = true
                                            audioPlaybackManager.play(
                                                SongEntity(
                                                    id = 882,
                                                    title = "Auto-Tuned Melodious Vocal",
                                                    artist = "Sur AI Voice Engine",
                                                    genre = "Tuned",
                                                    audioUrl = "http://tuned.vocal.demo",
                                                    duration = "0:20",
                                                    imageUrl = "",
                                                    lyrics = "[Auto-Tuned Melodious Vocal]"
                                                )
                                            )
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(
                                        imageVector = if (isPlayingTuned) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = if (isBangla) "শ্রুতিমধুর সুর" else "Play Tuned", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Divider(color = MaterialTheme.colorScheme.outlineVariant)

                            // Action Buttons: Send to Generator or Download
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        audioPlaybackManager.stop()
                                        onNavigateToCreateSong(selectedVocalTone, selectedScale)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = if (isBangla) "গানে যুক্ত করুন" else "Apply to Song", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        onSaveToDownloads("Tuned_Vocal_${selectedScale.take(5)}.wav", "http://tuned.vocal.demo")
                                        Toast.makeText(context, if (isBangla) "টিউন করা ভয়েস ডাউনলোডে সেভ হয়েছে!" else "Saved to Downloads!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = if (isBangla) "ডাউনলোড করুন" else "Download Wav", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
private fun LivePitchWaveformCanvas(
    isRecording: Boolean,
    isTuned: Boolean
) {
    val transition = rememberInfiniteTransition(label = "waveform")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerY = height / 2f
            val path = Path()

            path.moveTo(0f, centerY)

            val points = 80
            val step = width / points

            for (i in 0..points) {
                val x = i * step
                val angle = (i.toFloat() / points) * 4 * Math.PI.toFloat() + phase

                val amplitude = if (isRecording) {
                    (height * 0.40f) * (0.5f + 0.5f * sin(angle))
                } else if (isTuned) {
                    (height * 0.35f) * sin(angle)
                } else {
                    (height * 0.25f) * (sin(angle) + 0.4f * sin(angle * 3.5f))
                }

                val y = centerY + amplitude
                path.lineTo(x, y)
            }

            drawPath(
                path = path,
                color = if (isTuned) Color(0xFF1DB954) else if (isRecording) Color(0xFFFF5252) else Color(0xFF6200EE),
                style = Stroke(width = if (isTuned) 4.dp.toPx() else 2.5.dp.toPx())
            )
        }

        Text(
            text = if (isTuned) "♪ নিখুঁত সুরেল তরঙ্গ (Auto-Tuned Stream)" else if (isRecording) "🎙️ মাইক লাইভ ইনপুট চলছে..." else "অডিও ওয়েভফর্ম রেডি",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp)
        )
    }
}

@Composable
private fun PitchComparisonGraphCanvas(isTuned: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val points = 100
            val step = width / points

            // Draw Raw Jittery Pitch Path (Red)
            val rawPath = Path()
            rawPath.moveTo(0f, height * 0.5f)
            for (i in 1..points) {
                val x = i * step
                val angle = (i.toFloat() / points) * 6 * Math.PI.toFloat()
                val rawY = (height * 0.5f) + (height * 0.35f) * (sin(angle) + 0.35f * sin(angle * 4f))
                rawPath.lineTo(x, rawY.toFloat())
            }
            drawPath(
                path = rawPath,
                color = Color(0xFFFF5252).copy(alpha = if (isTuned) 0.4f else 0.85f),
                style = Stroke(width = 2.dp.toPx())
            )

            // Draw Auto-Tuned Quantized Pitch Path (Green Line) if Tuned
            if (isTuned) {
                val tunedPath = Path()
                tunedPath.moveTo(0f, height * 0.5f)
                for (i in 1..points) {
                    val x = i * step
                    val angle = (i.toFloat() / points) * 6 * Math.PI.toFloat()
                    val quantizedY = (height * 0.5f) + (height * 0.32f) * sin(angle)
                    tunedPath.lineTo(x, quantizedY.toFloat())
                }
                drawPath(
                    path = tunedPath,
                    color = Color(0xFF1DB954),
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }
    }
}
