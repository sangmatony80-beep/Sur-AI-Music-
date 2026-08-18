package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SunoLyricsGeneratorDialog(
    initialPrompt: String = "",
    onDismiss: () -> Unit,
    onApplyLyrics: (generatedLyrics: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var topicPrompt by remember { mutableStateOf(initialPrompt.ifBlank { "শরতের মেঘ ও নদীর গান" }) }
    var selectedLanguage by remember { mutableStateOf("Bangla") }
    val languages = listOf("Bangla", "English", "Hindi", "Spanish")

    var selectedGenre by remember { mutableStateOf("বাউল ফিউশন (Baul Folk)") }
    val genres = listOf(
        "বাউল ফিউশন (Baul Folk)",
        "মডার্ন পপ মেলোডি (Pop Melody)",
        "সিন্থওয়েভ রক (Synthwave Rock)",
        "ক্লাসিক্যাল গজল (Classical Ghazal)",
        "হিপ-হপ র‍্যাপ (Hip-Hop / Trap)"
    )

    var songStructure by remember { mutableStateOf("Verse - Chorus - Verse - Bridge - Outro") }
    val structures = listOf(
        "Verse - Chorus - Verse - Bridge - Outro",
        "A-B-A-B Strophic Ballad",
        "Verse - Pre-Chorus - Drop - Chorus"
    )

    var isGenerating by remember { mutableStateOf(false) }
    var generatedResult by remember { mutableStateOf("") }

    // Quick Suno Prompt Generator
    fun generateSunoLyrics() {
        scope.launch {
            isGenerating = true
            delay(1000)
            isGenerating = false

            generatedResult = when (selectedLanguage.lowercase()) {
                "bangla" -> buildString {
                    append("[Intro - $selectedGenre • Soft Melodic Pads]\n")
                    append("(হালকা একতারা ও জলতরঙ্গের ধ্বনি...)\n\n")
                    append("[Verse 1]\n")
                    append("নীল আকাশে জমছে মেঘের মেলা\n")
                    append("নদীর জলে চলছে রোদের খেলা\n")
                    append("$topicPrompt নিয়ে মনে বাজে গান\n")
                    append("ডিজিটাল এই ছন্দে জুড়ায় প্রাণ\n\n")
                    append("[Pre-Chorus]\n")
                    append("সময় তো থামে না কারো তরে...\n")
                    append("চল ভেসে যাই রূপকথার চরে...\n\n")
                    append("[Chorus]\n")
                    append("ও মন রে... সুরের ডানা মেলে দে\n")
                    append("অচিন সুরে নিজেকে আজ হারিয়ে ফেলে দে\n")
                    append("প্রতিটি বিটে নতুন আশার আলো জ্বলে\n")
                    append("সঙ্গীতের মহাকাব্যে সবাই মিলে!\n\n")
                    append("[Verse 2]\n")
                    append("শহরের কোলাহল পেরিয়ে বহুদূর\n")
                    append("পাখির ডানায় খুঁজে ফিরি চেনা সুর\n\n")
                    append("[Bridge - Guitar Solo & High Energy]\n")
                    append("(ইলেকট্রিক গিটারের জাদুকরী সুর ও ড্রামের বিট...)\n\n")
                    append("[Outro]\n")
                    append("সুরের মায়ায়... চিরন্তন ঠিকানা।\n")
                    append("[Fade Out]")
                }
                "hindi" -> buildString {
                    append("[Intro - $selectedGenre • Acoustic Strings]\n\n")
                    append("[Verse 1]\n")
                    append("आसमान में छाए हैं बादल प्यार के\n")
                    append("दिल में छिपे हैं अफसाना $topicPrompt के\n\n")
                    append("[Chorus]\n")
                    append("ओ सनम... सुरों की इस दुनिया में खो जा\n")
                    append("हर एक धड़कन में नया सा जादू हो जा!\n\n")
                    append("[Verse 2]\n")
                    append("रात की खामोशी में गूंजे तराना\n")
                    append("यही है हम सबका प्यारा ठिकाना\n\n")
                    append("[Outro]\n")
                    append("संगीत का सफर... हमेशा जारी रहे।")
                }
                else -> buildString {
                    append("[Intro - $selectedGenre • Ambient Synth & Beat]\n\n")
                    append("[Verse 1]\n")
                    append("Neon lights and quiet midnight air\n")
                    append("Finding whispers of $topicPrompt everywhere\n\n")
                    append("[Chorus]\n")
                    append("Let the rhythm take control tonight\n")
                    append("Melodies shining in the fading light\n")
                    append("We are limitless, we are alive\n")
                    append("Through the music, we forever thrive!\n\n")
                    append("[Verse 2]\n")
                    append("Chasing shadows down the digital stream\n")
                    append("Waking up inside a lossless dream\n\n")
                    append("[Outro]\n")
                    append("Fading out into the cosmic sound.\n")
                    append("[End]")
                }
            }
            Toast.makeText(context, "✨ Suno AI Lyrics Generated Successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("সুনো স্টাইল এআই লিরিক্স জেনারেটর", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("সুনো এআই (Suno v4) এর মতো প্রফেশনাল স্ট্রাকচারসহ (Verse, Chorus, Bridge, Solo) আকর্ষণীয় লিরিক্স তৈরি করুন।", fontSize = 12.sp, color = Color.Gray)

                OutlinedTextField(
                    value = topicPrompt,
                    onValueChange = { topicPrompt = it },
                    label = { Text("গান বা লিরিক্সের বিষয়বস্তু (Topic / Theme)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Language selection
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("🌐 ভাষা (Language)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        languages.forEach { lang ->
                            FilterChip(
                                selected = selectedLanguage == lang,
                                onClick = { selectedLanguage = lang },
                                label = { Text(lang, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Genre selection
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("🎵 জেনার ও স্টাইল (Style)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        genres.forEach { g ->
                            FilterChip(
                                selected = selectedGenre == g,
                                onClick = { selectedGenre = g },
                                label = { Text(g, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                Button(
                    onClick = { generateSunoLyrics() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isGenerating) "Generating Suno Lyrics..." else "Generate Suno Lyrics (লিরিক্স তৈরি করুন)", fontWeight = FontWeight.Bold)
                }

                if (generatedResult.isNotBlank()) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("✨ জেনারেটেড লিরিক্স প্রিভিউ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFA78BFA))
                            Text(generatedResult, fontSize = 12.sp, color = Color.White, lineHeight = 18.sp)
                        }
                    }
                }

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
                            if (generatedResult.isNotBlank()) {
                                onApplyLyrics(generatedResult)
                                Toast.makeText(context, "লিরিক্স স্টুডিও এডিটরে এপ্লাই করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            } else {
                                Toast.makeText(context, "আগে লিরিক্স জেনারেট করুন!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = generatedResult.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("এডিটরে ব্যবহার করুন")
                    }
                }
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
