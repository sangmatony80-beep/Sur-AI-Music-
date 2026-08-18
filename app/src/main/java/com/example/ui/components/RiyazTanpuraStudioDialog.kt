package com.example.ui.components

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.SongEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

data class ClassicalRaga(
    val englishName: String,
    val bengaliName: String,
    val thaat: String,
    val aarohan: String, // আরোহী
    val avarohan: String, // অবরোহী
    val vadi: String, // বাদী স্বর
    val samvadi: String, // সমবাদী স্বর
    val prahar: String, // গাওয়ার সময়
    val mood: String, // ভাব/রস
    val keyScale: String = "C"
)

data class SwaraNote(
    val bengaliName: String,
    val englishName: String,
    val freqOffsetMultiplier: Float, // Relative to Root Sa
    val isKomalOrTeevra: Boolean = false
)

@Composable
fun RiyazTanpuraStudioDialog(
    song: SongEntity? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Base Pitch selection (Root Sa)
    var selectedRootPitch by remember { mutableStateOf("C") } // C, C#, D, D#, E, F, F#, G, G#, A, A#, B
    val pitchFrequencies = mapOf(
        "A" to 220.00f,
        "A#" to 233.08f,
        "B" to 246.94f,
        "C" to 261.63f,
        "C#" to 277.18f,
        "D" to 293.66f,
        "D#" to 311.13f,
        "E" to 329.63f,
        "F" to 349.23f,
        "F#" to 369.99f,
        "G" to 392.00f,
        "G#" to 415.30f
    )

    // Tanpura First String Tuning (Pa, Ma, or Ni)
    var tanpuraTuningMode by remember { mutableStateOf("Pa") } // "Pa" (প), "Ma" (ম), "Ni" (নি)
    var isTanpuraPlaying by remember { mutableStateOf(false) }
    var tanpuraTempoBpm by remember { mutableIntStateOf(60) }
    var tanpuraVolume by remember { mutableFloatStateOf(0.85f) }

    // Riyaz Session Timer
    var riyazSecondsElapsed by remember { mutableIntStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(false) }

    // Active Raga selection
    val ragaList = remember {
        listOf(
            ClassicalRaga("Yaman (ইমন)", "রাগ ইমন", "কল্যাণ ঠাট", "নি্ রে গা মাঁ ধা নি র্সা", "র্সা নি ধা পা মাঁ গা রে সা", "গা (Gandhar)", "নি (Nishad)", "সন্ধ্যার প্রথম প্রহর (Evening)", "শান্ত ও ভক্তিভাব"),
            ClassicalRaga("Bhairav (ভৈরব)", "রাগ ভৈরব", "ভৈরব ঠাট", "সা রেঁ গা মা পা ধাঁ নি র্সা", "র্সা নি ধাঁ পা মা গা রেঁ সা", "ধাঁ (Komal Dha)", "রেঁ (Komal Re)", "ভোরবেলা (Dawn / Morning)", "গম্ভীর ও ধ্যানমগ্ন"),
            ClassicalRaga("Bhairavi (ভৈরবী)", "রাগ ভৈরবী", "ভৈরবী ঠাট", "সা রেঁ গ্যা মা পা ধাঁ নী র্সা", "র্সা নী ধাঁ পা মা গ্যা রেঁ সা", "মা (Madhyam)", "সা (Shadja)", "প্রভাত ও সমাপনী (All-time)", "বিরহ ও করুণ রস"),
            ClassicalRaga("Kafi (কাফি)", "রাগ কাফি", "কাফি ঠাট", "সা রে গ্যা মা পা ধা নী র্সা", "র্সা নী ধা পা মা গ্যা রে সা", "পা (Pancham)", "সা (Shadja)", "মধ্যরাত / বসন্ত (Spring/Night)", "শৃঙ্গার ও লোকসঙ্গীতের ভাব"),
            ClassicalRaga("Bilawal (বিলাবল)", "রাগ বিলাবল", "বিলাবল ঠাট", "সা রে গা মা পা ধা নি র্সা", "র্সা নি ধা পা মা গা রে সা", "ধা (Dhaivat)", "গা (Gandhar)", "সকাল (Morning)", "আনন্দ ও প্রফুল্লতা"),
            ClassicalRaga("Khamaj (খাম্বাজ)", "রাগ খাম্বাজ", "খাম্বাজ ঠাট", "সা গা মা পা ধা নি র্সা", "র্সা নী ধা পা মা গা রে সা", "গা (Gandhar)", "নি (Nishad)", "রাত্রির দ্বিতীয় প্রহর (Night)", "চটুল ও প্রেমভাব"),
            ClassicalRaga("Asavari (আশাবরী)", "রাগ আশাবরী", "আশাবরী ঠাট", "সা রে মা পা ধাঁ র্সা", "র্সা নী ধাঁ পা মা গ্যা রে সা", "ধাঁ (Komal Dha)", "গ্যা (Komal Ga)", "দিনের দ্বিতীয় প্রহর (Late Morning)", "করুণ ও বৈরাগ্য"),
            ClassicalRaga("Malkauns (মালকোষ)", "রাগ মালকোষ", "ভৈরবী ঠাট", "সা গ্যা মা ধাঁ নী র্সা", "র্সা নী ধাঁ মা গ্যা সা", "মা (Madhyam)", "সা (Shadja)", "গভীর রাত (Midnight)", "শান্ত, গম্ভীর ও ঐশ্বরিক")
        )
    }

    var selectedRaga by remember { mutableStateOf(ragaList[0]) }

    // 12 Swaras with Indian Scale frequency multipliers
    val swaras = remember {
        listOf(
            SwaraNote("সা", "S", 1.000f),
            SwaraNote("রেঁ", "r", 1.066f, true),
            SwaraNote("রে", "R", 1.125f),
            SwaraNote("গ্যা", "g", 1.200f, true),
            SwaraNote("গা", "G", 1.250f),
            SwaraNote("মা", "M", 1.333f),
            SwaraNote("মাঁ", "M'", 1.406f, true),
            SwaraNote("পা", "P", 1.500f),
            SwaraNote("ধাঁ", "d", 1.600f, true),
            SwaraNote("ধা", "D", 1.666f),
            SwaraNote("নী", "n", 1.800f, true),
            SwaraNote("নি", "N", 1.875f),
            SwaraNote("র্সা", "S'", 2.000f)
        )
    }

    var lastPlayedSwara by remember { mutableStateOf<String?>(null) }

    // Audio synthesizer helper for single Swara note
    fun playSwaraSynth(freq: Float) {
        lastPlayedSwara = "${freq.toInt()} Hz"
        scope.launch(Dispatchers.IO) {
            try {
                val sampleRate = 44100
                val durationMs = 600
                val totalSamples = (sampleRate * durationMs) / 1000
                val buffer = ShortArray(totalSamples)

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    val env = exp(-t * 3.5)
                    // Harmonics for rich Indian acoustic flute/harmonium tone
                    val fundamental = sin(2.0 * PI * freq * t)
                    val h2 = 0.5 * sin(2.0 * PI * freq * 2.0 * t)
                    val h3 = 0.25 * sin(2.0 * PI * freq * 3.0 * t)
                    val sample = ((fundamental + h2 + h3) / 1.75) * env
                    buffer[i] = (sample.coerceIn(-1.0, 1.0) * 30000).toInt().toShort()
                }

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
                delay(700)
                audioTrack.release()
            } catch (_: Exception) {}
        }
    }

    // Tanpura 4-String Drone Loop Synthesizer
    LaunchedEffect(isTanpuraPlaying, selectedRootPitch, tanpuraTuningMode, tanpuraTempoBpm) {
        if (!isTanpuraPlaying) return@LaunchedEffect

        val rootSa = pitchFrequencies[selectedRootPitch] ?: 261.63f
        val firstStringFreq = when (tanpuraTuningMode) {
            "Pa" -> rootSa * 0.75f // Mandra Pancham
            "Ma" -> rootSa * 0.666f // Mandra Madhyam
            else -> rootSa * 0.937f // Mandra Nishad
        }
        val secondStringFreq = rootSa // Madhya Sa
        val thirdStringFreq = rootSa // Madhya Sa
        val fourthStringFreq = rootSa * 0.5f // Mandra Sa (Low Base)

        val tanpuraSequence = listOf(firstStringFreq, secondStringFreq, thirdStringFreq, fourthStringFreq)
        val pluckIntervalMs = (60000L / tanpuraTempoBpm).coerceIn(400L, 2000L)

        while (isTanpuraPlaying && isActive) {
            for (freq in tanpuraSequence) {
                if (!isTanpuraPlaying) break
                // Play acoustic tanpura string resonance
                scope.launch(Dispatchers.IO) {
                    try {
                        val sampleRate = 44100
                        val durationMs = 1800
                        val totalSamples = (sampleRate * durationMs) / 1000
                        val buffer = ShortArray(totalSamples)

                        val track = AudioTrack.Builder()
                            .setAudioAttributes(
                                AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build()
                            )
                            .setAudioFormat(
                                AudioFormat.Builder()
                                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                    .setSampleRate(sampleRate)
                                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                    .build()
                            )
                            .setBufferSizeInBytes(buffer.size * 2)
                            .setTransferMode(AudioTrack.MODE_STATIC)
                            .build()

                        for (i in 0 until totalSamples) {
                            val t = i.toDouble() / sampleRate
                            val env = exp(-t * 1.8)
                            // Jawari buzzing harmonics (Tanpura signature resonance)
                            val f1 = sin(2.0 * PI * freq * t)
                            val f2 = 0.6 * sin(2.0 * PI * (freq * 2.0 + 0.5) * t)
                            val f3 = 0.35 * sin(2.0 * PI * (freq * 3.0 + 1.0) * t)
                            val f4 = 0.2 * sin(2.0 * PI * (freq * 4.0 + 1.5) * t)
                            val s = ((f1 + f2 + f3 + f4) / 2.15) * env * tanpuraVolume
                            buffer[i] = (s.coerceIn(-1.0, 1.0) * 28000).toInt().toShort()
                        }
                        track.write(buffer, 0, buffer.size)
                        track.play()
                        delay(1900)
                        track.release()
                    } catch (_: Exception) {}
                }
                delay(pluckIntervalMs)
            }
        }
    }

    // Riyaz Timer
    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning) {
            delay(1000)
            riyazSecondsElapsed += 1
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
            color = Color(0xFF0D1117),
            border = BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Top Bar
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
                            color = Color(0xFFD97706),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.Black, modifier = Modifier.size(22.dp))
                            }
                        }
                        Column {
                            Text(
                                text = "তানপুরা ড্রোন ও রিয়াজ স্টুডিও",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Classical Tanpura Drone, Ragas & Swara Riyaz",
                                fontSize = 11.sp,
                                color = Color(0xFFFBBF24)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Tanpura Drone Master Controller Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1917)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("🪕 তানপুরা ড্রোন:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isTanpuraPlaying) Color(0xFF10B981).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f)
                                ) {
                                    Text(
                                        text = if (isTanpuraPlaying) "● বাজছে (Active)" else "বন্ধ (Stopped)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isTanpuraPlaying) Color(0xFF10B981) else Color.Gray,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // Start/Stop Tanpura Button
                            Button(
                                onClick = {
                                    isTanpuraPlaying = !isTanpuraPlaying
                                    if (isTanpuraPlaying && !isTimerRunning) {
                                        isTimerRunning = true
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isTanpuraPlaying) Color(0xFFEF4444) else Color(0xFFD97706)
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(if (isTanpuraPlaying) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isTanpuraPlaying) "ড্রোন বন্ধ করুন" else "ড্রোন চালু করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Root Pitch Selector (সা স্কেল)
                        Text("মূল স্কেল নির্বাচন (Root Pitch / সা):", fontSize = 10.sp, color = Color.LightGray)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(pitchFrequencies.keys.toList()) { pitch ->
                                val isSel = selectedRootPitch == pitch
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSel) Color(0xFFD97706) else Color.White.copy(alpha = 0.08f),
                                    modifier = Modifier.clickable {
                                        selectedRootPitch = pitch
                                        Toast.makeText(context, "স্কেল পরিবর্তন: $pitch", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Text(
                                        text = pitch,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) Color.Black else Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        // 1st String Tuning Mode & Tempo
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Tuning mode
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("১ম তার:", fontSize = 10.sp, color = Color.Gray)
                                listOf("Pa" to "পা-সা-সা-সা", "Ma" to "মা-সা-সা-সা", "Ni" to "নি-সা-সা-সা").forEach { (mode, label) ->
                                    val isSel = tanpuraTuningMode == mode
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSel) Color(0xFFFBBF24).copy(alpha = 0.25f) else Color.Transparent,
                                        border = BorderStroke(1.dp, if (isSel) Color(0xFFFBBF24) else Color.White.copy(alpha = 0.1f)),
                                        modifier = Modifier.clickable { tanpuraTuningMode = mode }
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 9.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSel) Color(0xFFFBBF24) else Color.LightGray,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            // Riyaz Stopwatch Timer
                            val rMin = riyazSecondsElapsed / 60
                            val rSec = riyazSecondsElapsed % 60
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                                Text(
                                    text = String.format("%02d:%02d", rMin, rSec),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                        }
                    }
                }

                // Interactive Sargam Riyaz Practice Keyboard (সা, রে, গা, মা...)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🎹 সরগম রিয়াজ কিবোর্ড (ট্যাপ করে সুর শুনুন ও গাইবেন):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                            lastPlayedSwara?.let {
                                Text(it, fontSize = 10.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                            }
                        }

                        val baseSaFreq = pitchFrequencies[selectedRootPitch] ?: 261.63f

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(swaras) { swara ->
                                val noteFreq = baseSaFreq * swara.freqOffsetMultiplier
                                val isKomal = swara.isKomalOrTeevra

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isKomal) Color(0xFF0F172A) else Color(0xFF334155),
                                    border = BorderStroke(1.dp, if (isKomal) Color(0xFFF59E0B) else Color.White.copy(alpha = 0.2f)),
                                    modifier = Modifier
                                        .width(44.dp)
                                        .height(64.dp)
                                        .clickable {
                                            playSwaraSynth(noteFreq)
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize().padding(4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = swara.englishName,
                                            fontSize = 9.sp,
                                            color = if (isKomal) Color(0xFFF59E0B) else Color.LightGray
                                        )
                                        Text(
                                            text = swara.bengaliName,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (isKomal) Color(0xFFFBBF24) else Color.White
                                        )
                                        Text(
                                            text = "${noteFreq.toInt()}",
                                            fontSize = 8.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Classical Raga Knowledge & Guide Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🎼 ভারতীয় ও বাংলা ক্লাসিক্যাল রাগ নির্দেশিকা:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA78BFA))

                        // Raga Selector Chips
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(ragaList) { raga ->
                                val isSel = selectedRaga.englishName == raga.englishName
                                FilterChip(
                                    selected = isSel,
                                    onClick = { selectedRaga = raga },
                                    label = { Text(raga.englishName, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF8B5CF6),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        // Detailed Selected Raga Info
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF27272A),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(selectedRaga.bengaliName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFFBBF24))
                                            Text("ঠাট: ${selectedRaga.thaat}", fontSize = 11.sp, color = Color.LightGray)
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("🔼 আরোহী (Aarohan):", fontSize = 10.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                        Text(selectedRaga.aarohan, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)

                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("🔽 অবরোহী (Avarohan):", fontSize = 10.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                                        Text(selectedRaga.avarohan, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)

                                        Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("বাদী: ${selectedRaga.vadi}", fontSize = 10.sp, color = Color(0xFF38BDF8))
                                            Text("সমবাদী: ${selectedRaga.samvadi}", fontSize = 10.sp, color = Color(0xFF38BDF8))
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("সময়: ${selectedRaga.prahar}", fontSize = 10.sp, color = Color.Gray)
                                            Text("ভাব: ${selectedRaga.mood}", fontSize = 10.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
