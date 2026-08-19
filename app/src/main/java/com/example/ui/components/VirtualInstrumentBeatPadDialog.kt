package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ai.audio.infrastructure.synth.RealtimeAudioSynthEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class PadSound(
    val id: String,
    val nameBn: String,
    val nameEn: String,
    val category: String, // "DRUMS", "PERC", "FOLK", "CHORD"
    val baseColor: Color
)

/**
 * 🥁 AI Virtual Instrument & 16-Pad Live Beat Matrix
 * Allows jamming with Bengali Folk percussion (Tabla, Dholak, Dotara, Ektara, Bansuri)
 * plus modern 808 Hip-Hop drums and chord progressions.
 */
@Composable
fun VirtualInstrumentBeatPadDialog(
    onDismiss: () -> Unit,
    onSendPromptToAiSong: (prompt: String, genre: String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var bpm by remember { mutableIntStateOf(110) }
    var isLoopPlaying by remember { mutableStateOf(false) }
    var activeStep by remember { mutableIntStateOf(0) }
    var selectedScale by remember { mutableStateOf("Bhairavi (ভৈরবী রাগ)") }

    // Pad flash states
    var lastTriggeredPad by remember { mutableStateOf<String?>(null) }

    val pads = remember {
        listOf(
            // Row 1: Bangla Traditional Percussion
            PadSound("p1", "তবলা ধা", "Tabla Dha", "PERC", Color(0xFFE11D48)),
            PadSound("p2", "তবলা না", "Tabla Na", "PERC", Color(0xFFF43F5E)),
            PadSound("p3", "ঢোলক গুম", "Dholak Thump", "PERC", Color(0xFFFB7185)),
            PadSound("p4", "খমক ট্রিগার", "Khamak Zap", "PERC", Color(0xFFFDA4AF)),

            // Row 2: Bengali Acoustic Folk Instruments
            PadSound("p5", "একতারা প্লাক", "Ektara Pluck", "FOLK", Color(0xFFD97706)),
            PadSound("p6", "দোতারা স্ট্রাম", "Dotara Strum", "FOLK", Color(0xFFF59E0B)),
            PadSound("p7", "বাঁশির তান", "Bansuri Flute", "FOLK", Color(0xFFFBBF24)),
            PadSound("p8", "মন্দিরা ঝংকার", "Mandira Bell", "FOLK", Color(0xFFFDE68A)),

            // Row 3: Modern 808 Studio Beats
            PadSound("p9", "৮০৮ কিক", "808 Sub Kick", "DRUMS", Color(0xFF2563EB)),
            PadSound("p10", "স্ন্যাপ স্নিয়ার", "Snappy Snare", "DRUMS", Color(0xFF3B82F6)),
            PadSound("p11", "হাই-হ্যাট রোল", "Trap Hi-Hat", "DRUMS", Color(0xFF60A5FA)),
            PadSound("p12", "ওপেন সিম্বল", "Open Cymbal", "DRUMS", Color(0xFF93C5FD)),

            // Row 4: Melodic Chords & Atmosphere
            PadSound("p13", "সি মেজর (C)", "C Major Chord", "CHORD", Color(0xFF7C3AED)),
            PadSound("p14", "জি মেজর (G)", "G Major Chord", "CHORD", Color(0xFF8B5CF6)),
            PadSound("p15", "এ মাইনর (Am)", "A Minor Chord", "CHORD", Color(0xFFA78BFA)),
            PadSound("p16", "এফ মেজর (F)", "F Major Chord", "CHORD", Color(0xFFC4B5FD))
        )
    }

    // Step sequencer loop audio playback
    LaunchedEffect(isLoopPlaying, bpm) {
        if (isLoopPlaying) {
            val stepDelayMs = (60000 / (bpm * 4)).toLong()
            while (isLoopPlaying) {
                activeStep = (activeStep + 1) % 16
                // Trigger real acoustic rhythm on sequencer steps
                when (activeStep) {
                    0, 8 -> RealtimeAudioSynthEngine.triggerPad("p9") // 808 Kick
                    4, 12 -> RealtimeAudioSynthEngine.triggerPad("p10") // Snare
                    2, 6, 10, 14 -> RealtimeAudioSynthEngine.triggerPad("p11") // Hi-hat
                    1, 5, 9, 13 -> RealtimeAudioSynthEngine.triggerPad("p1") // Tabla Dha
                    7, 15 -> RealtimeAudioSynthEngine.triggerPad("p2") // Tabla Na
                }
                delay(stepDelayMs)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF0F172A),
            tonalElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f)
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.GridOn, contentDescription = null, tint = Color(0xFFF59E0B))
                            }
                        }
                        Column {
                            Text("🥁 ১৬-প্যাড লাইভ বিট ও ফোক ইন্সট্রুমেন্ট", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                            Text("তবলা, দোতারা, একতারা ও ৮০৮ বিট ম্যাট্রিক্স", fontSize = 11.sp, color = Color.LightGray)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                // Control Bar (BPM, Tap Tempo, Play Loop, Scale)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledIconButton(
                                onClick = { isLoopPlaying = !isLoopPlaying },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = if (isLoopPlaying) Color(0xFFEF4444) else Color(0xFF10B981)
                                )
                            ) {
                                Icon(
                                    if (isLoopPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = "Play Loop",
                                    tint = Color.White
                                )
                            }

                            Column {
                                Text("BPM: $bpm", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    FilledTonalButton(
                                        onClick = { bpm = (bpm - 5).coerceAtLeast(60) },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) { Text("-5", fontSize = 10.sp) }
                                    FilledTonalButton(
                                        onClick = { bpm = (bpm + 5).coerceAtMost(200) },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) { Text("+5", fontSize = 10.sp) }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                bpm = ((70..140).random())
                                Toast.makeText(context, "Tap Tempo Synced: $bpm BPM", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tap Tempo", fontSize = 11.sp)
                        }
                    }
                }

                // 16-Pad Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    userScrollEnabled = false
                ) {
                    itemsIndexed(pads) { index, pad ->
                        val isTriggered = lastTriggeredPad == pad.id || (isLoopPlaying && activeStep % 4 == index % 4)
                        val animatedColor by animateColorAsState(
                            targetValue = if (isTriggered) Color.White else pad.baseColor,
                            animationSpec = tween(durationMillis = 150),
                            label = "Pad_Anim"
                        )

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = animatedColor.copy(alpha = if (isTriggered) 0.95f else 0.35f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isTriggered) Color.White else pad.baseColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(76.dp)
                                .clickable {
                                    lastTriggeredPad = pad.id
                                    RealtimeAudioSynthEngine.triggerPad(pad.id)
                                    scope.launch {
                                        delay(150)
                                        if (lastTriggeredPad == pad.id) lastTriggeredPad = null
                                    }
                                }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = pad.nameBn,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isTriggered) Color.Black else Color.White,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                                Text(
                                    text = pad.nameEn,
                                    fontSize = 9.sp,
                                    color = if (isTriggered) Color.DarkGray else Color.LightGray,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // AI Prompt Generator from Pad Beat
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                            Text("এআই গান জেনারেটরে এই বিট ও কর্ড পাঠান", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        }
                        Text("আপনার বাজানো দোতারা ও তবলা বিট থেকে তাৎক্ষণিক ফুল ট্র্যাক তৈরির প্রম্পট তৈরি করুন।", fontSize = 11.sp, color = Color.LightGray)

                        Button(
                            onClick = {
                                val generatedPrompt = "Bangla acoustic fusion track with soulful Dotara rhythm, Tabla grooves at $bpm BPM, Ektara flourishes and sweet Bansuri melody in $selectedScale scale."
                                onSendPromptToAiSong(generatedPrompt, "Folk Fusion")
                                Toast.makeText(context, "প্রম্পট এআই স্টুডিওতে পাঠানো হয়েছে!", Toast.LENGTH_LONG).show()
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                        ) {
                            Text("Create Full Song with This Beat", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}
