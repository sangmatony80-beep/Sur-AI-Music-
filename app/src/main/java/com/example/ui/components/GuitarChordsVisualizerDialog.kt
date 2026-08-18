package com.example.ui.components

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.widget.Toast
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
import androidx.compose.ui.graphics.Brush
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
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

data class ChordPosition(
    val chordName: String,
    val bengaliName: String,
    // Finger positions on 6 strings (E, A, D, G, B, e): 0=Open, -1=Muted, 1..4=Fret number
    val stringsFrets: List<Int>,
    val rootFrequencies: List<Float>
)

@Composable
fun GuitarChordsVisualizerDialog(
    song: SongEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedInstrument by remember { mutableStateOf("Guitar") } // "Guitar", "Ukulele", "Dotara"
    var capoFret by remember { mutableIntStateOf(0) }
    var currentChordIndex by remember { mutableIntStateOf(0) }
    var isAutoPlayStrum by remember { mutableStateOf(true) }
    var strumBpm by remember { mutableIntStateOf(110) }

    val chordList = remember {
        listOf(
            ChordPosition("C Major", "সা-গা-পা (C)", listOf(-1, 3, 2, 0, 1, 0), listOf(130.81f, 164.81f, 196.00f, 261.63f, 329.63f)),
            ChordPosition("G Major", "পা-নি-রে (G)", listOf(3, 2, 0, 0, 0, 3), listOf(98.00f, 123.47f, 146.83f, 196.00f, 246.94f, 392.00f)),
            ChordPosition("A Minor", "ধা-সা-গা (Am)", listOf(-1, 0, 2, 2, 1, 0), listOf(110.00f, 164.81f, 220.00f, 261.63f, 329.63f)),
            ChordPosition("F Major", "মা-ধা-সা (F)", listOf(1, 3, 3, 2, 1, 1), listOf(87.31f, 130.81f, 174.61f, 220.00f, 261.63f, 349.23f)),
            ChordPosition("E Minor", "গা-পা-নি (Em)", listOf(0, 2, 2, 0, 0, 0), listOf(82.41f, 123.47f, 164.81f, 196.00f, 246.94f, 329.63f)),
            ChordPosition("D Minor", "রে-মা-ধা (Dm)", listOf(-1, -1, 0, 2, 3, 1), listOf(146.83f, 220.00f, 293.66f, 349.23f)),
            ChordPosition("D Major", "রে-তীব্র মা-পা (D)", listOf(-1, -1, 0, 2, 3, 2), listOf(146.83f, 220.00f, 293.66f, 369.99f))
        )
    }

    val currentChord = chordList[currentChordIndex]

    // Real audio synthesizer for chord strum
    fun playStrumSynth(frequencies: List<Float>) {
        scope.launch(Dispatchers.IO) {
            try {
                val sampleRate = 44100
                val durationMs = 500
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
                    var sampleSum = 0.0

                    frequencies.forEachIndexed { strIdx, freq ->
                        val stringDelaySec = strIdx * 0.025
                        if (t >= stringDelaySec) {
                            val strTime = t - stringDelaySec
                            val env = exp(-strTime * 5.0)
                            sampleSum += sin(2.0 * PI * freq * strTime) * env
                        }
                    }

                    val sampleNorm = (sampleSum / (frequencies.size.coerceAtLeast(1)))
                        .coerceIn(-1.0, 1.0)
                    buffer[i] = (sampleNorm * 30000).toInt().toShort()
                }

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
                delay(600)
                audioTrack.release()
            } catch (_: Exception) {}
        }
    }

    // Auto chord sequence progression
    LaunchedEffect(isAutoPlayStrum) {
        while (isAutoPlayStrum) {
            playStrumSynth(currentChord.rootFrequencies)
            delay((60000 / strumBpm).toLong() * 2)
            currentChordIndex = (currentChordIndex + 1) % chordList.size
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0B0F19),
            border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            color = Color(0xFFF59E0B),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                            }
                        }
                        Column {
                            Text(
                                text = "লাইভ গিটার ও দোতারা কর্ড চার্ট",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Interactive Fretboard & Strumming Guide",
                                fontSize = 11.sp,
                                color = Color(0xFFF59E0B)
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

                // Instrument & Capo Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Instrument Selector
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Guitar", "Ukulele", "Dotara").forEach { inst ->
                            val isSel = selectedInstrument == inst
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedInstrument = inst },
                                label = { Text(inst, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFF59E0B),
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }

                    // Capo Selector
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Capo:", fontSize = 11.sp, color = Color.LightGray)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier.clickable {
                                capoFret = (capoFret + 1) % 6
                                Toast.makeText(context, "Capo on Fret $capoFret", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text(
                                text = if (capoFret == 0) "None" else "Fret $capoFret",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Current Active Chord Banner & Strum Button
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("বর্তমান বাজছে:", fontSize = 10.sp, color = Color.Gray)
                            Text(
                                text = currentChord.chordName,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFF59E0B)
                            )
                            Text(
                                text = currentChord.bengaliName,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        Button(
                            onClick = { playStrumSynth(currentChord.rootFrequencies) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Strum Cord", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Interactive Fretboard Visualizer Canvas (6 Strings & 5 Frets)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF070A10)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        val width = size.width
                        val height = size.height
                        val stringsCount = 6
                        val fretsCount = 5

                        val stringSpacing = height / (stringsCount - 1)
                        val fretSpacing = width / fretsCount

                        // Draw Frets (Vertical lines)
                        for (f in 0..fretsCount) {
                            val x = f * fretSpacing
                            drawLine(
                                color = if (f == 0) Color(0xFFF59E0B) else Color.Gray.copy(alpha = 0.5f),
                                start = Offset(x, 0f),
                                end = Offset(x, height),
                                strokeWidth = if (f == 0) 6f else 2f
                            )
                        }

                        // Draw Strings (Horizontal lines)
                        for (s in 0 until stringsCount) {
                            val y = s * stringSpacing
                            val stringThickness = (stringsCount - s) * 0.8f + 1f
                            drawLine(
                                color = Color.White.copy(alpha = 0.7f),
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = stringThickness
                            )
                        }

                        // Draw Finger position dots
                        currentChord.stringsFrets.forEachIndexed { stringIdx, fret ->
                            val y = stringIdx * stringSpacing
                            if (fret > 0) {
                                val x = (fret - 0.5f) * fretSpacing
                                drawCircle(
                                    color = Color(0xFF10B981),
                                    radius = 11f,
                                    center = Offset(x, y)
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 4f,
                                    center = Offset(x, y)
                                )
                            }
                        }
                    }
                }

                // Strumming Pattern & Metronome Guide
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🎸 স্ট্রামিং প্যাটার্ন: ↓ ↓ ↑ ↑ ↓ ↑ (D-D-U-U-D-U)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                            Text("$strumBpm BPM", fontSize = 11.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("↓ Down", "↓ Down", "↑ Up", "↑ Up", "↓ Down", "↑ Up").forEachIndexed { idx, dir ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (idx % 2 == 0) Color(0xFFF59E0B).copy(alpha = 0.2f) else Color(0xFF38BDF8).copy(alpha = 0.2f)
                                ) {
                                    Text(dir, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = Color.White)
                                }
                            }
                        }
                    }
                }

                // Quick Chord Progression Selector Bar
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chordList.indices.toList()) { index ->
                        val ch = chordList[index]
                        val isSelected = index == currentChordIndex
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFFF59E0B) else Color(0xFF1E293B),
                            border = BorderStroke(1.dp, if (isSelected) Color.White else Color.Transparent),
                            modifier = Modifier
                                .clickable {
                                    currentChordIndex = index
                                    playStrumSynth(ch.rootFrequencies)
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(ch.chordName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isSelected) Color.Black else Color.White)
                                Text(ch.bengaliName, fontSize = 9.sp, color = if (isSelected) Color.Black.copy(alpha = 0.8f) else Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}
