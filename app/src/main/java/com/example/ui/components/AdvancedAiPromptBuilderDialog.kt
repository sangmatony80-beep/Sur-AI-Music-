package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdvancedAiPromptBuilderDialog(
    onDismiss: () -> Unit,
    onApplyPrompt: (constructedPrompt: String, genre: String, vibe: String, bpm: Int, instrumentation: String) -> Unit
) {
    val context = LocalContext.current

    var selectedGenre by remember { mutableStateOf("বাউল ফিউশন (Baul Fusion)") }
    val genres = listOf(
        "বাউল ফিউশন (Baul Fusion)",
        "সিন্থওয়েভ পপ (Synthwave Pop)",
        "ক্লাসিক্যাল গজল (Classical Ghazal)",
        "লো-ফাই হিপ-হপ (Lo-Fi Hip-Hop)",
        "মডার্ন রক (Modern Rock)",
        "সুফি ফোক (Sufi Folk)",
        "সিনেমাটিক অর্কেস্ট্রাল (Cinematic)"
    )

    var selectedMood by remember { mutableStateOf("নস্টালজিক ও আবেগময় (Nostalgic)") }
    val moods = listOf(
        "নস্টালজিক ও আবেগময় (Nostalgic)",
        "শক্তিশালী ও উৎসাহী (Energetic)",
        "শান্ত ও ধ্যানমগ্ন (Spiritual Calm)",
        "রোমান্টিক ও মেলোডিয়াস (Romantic)",
        "ঘুমপাড়ানি ও রিলাক্সিং (Chill Lofi)"
    )

    var bpm by remember { mutableFloatStateOf(110f) }

    var selectedInstrumentation by remember { mutableStateOf("একতারা, একাউস্টিক গিটার ও সফট প্যাড") }
    val instrumentations = listOf(
        "একতারা, একাউস্টিক গিটার ও সফট প্যাড",
        "সিন্থ লিড, 808 বেস ড্রাম ও রিভার্ব কিক",
        "সেতার, তবলা ও ক্লাসিক্যাল হারমোনিয়াম",
        "ভিনাইল ক্র্যাকল পিয়ানো ও ব্রাশ ড্রামস",
        "হেভি ডিস্টোরশন ইলেকট্রিক গিটার ও রক ড্রামস"
    )

    var selectedVocalStyle by remember { mutableStateOf("উষ্ণ লোকসংগীত কণ্ঠ (Warm Folk Vocal)") }
    val vocalStyles = listOf(
        "উষ্ণ লোকসংগীত কণ্ঠ (Warm Folk Vocal)",
        "ক্রিস্টাল ক্লিয়ার পপ ডিভা (Crystal Pop Diva)",
        "গভীর মেলোডি বারী টোন (Deep Baritone)",
        "রেশমি গজল কুহেলি (Silk Ghazal Tone)"
    )

    val constructedPrompt = buildString {
        append("Genre: $selectedGenre. ")
        append("Mood: $selectedMood. ")
        append("Tempo: ${bpm.toInt()} BPM. ")
        append("Instrumentation: $selectedInstrumentation. ")
        append("Vocal Style: $selectedVocalStyle. ")
        append("Mastered for 44.1kHz Hi-Fi streaming with rich spatial separation.")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("অ্যাডভান্সড এআই প্রম্পট বিল্ডার", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("আপনার পছন্দের মিউজিক্যাল জেনার, মুড, BPM এবং ইনস্ট্রুমেন্টেশন স্টাইল বেছে নিন। এআই এই প্যারামিটারগুলোর ওপর ভিত্তি করে নিখুঁত গান তৈরি করবে।", fontSize = 13.sp, color = Color.Gray)

                // 1. Genre Selection
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("🎵 মিউজিক জেনার (Genre)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        genres.forEach { g ->
                            FilterChip(
                                selected = selectedGenre == g,
                                onClick = { selectedGenre = g },
                                label = { Text(g, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // 2. Mood & Vibe
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("✨ মুড ও ভাইব (Mood / Vibe)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        moods.forEach { m ->
                            FilterChip(
                                selected = selectedMood == m,
                                onClick = { selectedMood = m },
                                label = { Text(m, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // 3. BPM Slider
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("⏱️ টেম্পো (BPM): ${bpm.toInt()} BPM", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        val tempoName = when {
                            bpm < 80 -> "Largo (ধীর লয়)"
                            bpm < 110 -> "Andante (মধ্যম লয়)"
                            bpm < 140 -> "Allegro (দ্রুত লয়)"
                            else -> "Presto (অত্যন্ত দ্রুত)"
                        }
                        Text(tempoName, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = bpm,
                        onValueChange = { bpm = it },
                        valueRange = 60f..180f,
                        steps = 24
                    )
                }

                // 4. Instrumentation Style
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("🎸 ইনস্ট্রুমেন্টেশন স্টাইল (Instrumentation)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    instrumentations.forEach { inst ->
                        Surface(
                            onClick = { selectedInstrumentation = inst },
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedInstrumentation == inst) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (selectedInstrumentation == inst) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(inst, fontSize = 12.sp, modifier = Modifier.padding(12.dp), fontWeight = if (selectedInstrumentation == inst) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }

                // 5. Vocal Style
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("🎙️ ভোকাল স্টাইল (Vocal Tone)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        vocalStyles.forEach { v ->
                            FilterChip(
                                selected = selectedVocalStyle == v,
                                onClick = { selectedVocalStyle = v },
                                label = { Text(v, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Constructed Prompt Live Preview Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("📜 জেনারেটেড এআই প্রম্পট প্রিভিউ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFA78BFA))
                        Text(constructedPrompt, fontSize = 11.sp, color = Color.White)
                    }
                }

                // Action Buttons
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("বাতিল")
                    }
                    Button(
                        onClick = {
                            onApplyPrompt(constructedPrompt, selectedGenre, selectedMood, bpm.toInt(), selectedInstrumentation)
                            Toast.makeText(context, "অ্যাডভান্সড প্রম্পট সফলভাবে এপ্লাই করা হয়েছে!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("প্রম্পট ব্যবহার করুন")
                    }
                }
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
