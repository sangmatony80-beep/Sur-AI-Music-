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
import com.example.data.repository.LyricRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SurAiLyricsGeneratorDialog(
    initialPrompt: String = "",
    onDismiss: () -> Unit,
    onApplyLyrics: (generatedLyrics: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lyricRepository = remember { LyricRepository(context) }

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

    var isGenerating by remember { mutableStateOf(false) }
    var generatedResult by remember { mutableStateOf("") }

    fun generateSurLyrics() {
        scope.launch {
            isGenerating = true
            try {
                val result = lyricRepository.fetchLyrics(
                    theme = topicPrompt,
                    genre = selectedGenre,
                    vibe = "Emotional & Uplifting",
                    language = selectedLanguage
                )
                generatedResult = result
                Toast.makeText(context, "✨ গুগল জেমিনাই লিরিক্স সফলভাবে তৈরি হয়েছে!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                isGenerating = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("গুগল লিরিক্স জেনারেটর (Google Lyrics)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                Text("গুগল জেমিনাই এআই মডেল ব্যবহার করে প্রফেশনাল ও অরিজিনাল লিরিক্স জেনারেট করুন।", fontSize = 12.sp, color = Color.Gray)

                OutlinedTextField(
                    value = topicPrompt,
                    onValueChange = { topicPrompt = it },
                    label = { Text("গান বা লিরিক্সের বিষয়বস্তু (Topic / Theme)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Text("ভাষা (Language):", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    languages.forEach { lang ->
                        FilterChip(
                            selected = selectedLanguage == lang,
                            onClick = { selectedLanguage = lang },
                            label = { Text(lang, fontSize = 12.sp) }
                        )
                    }
                }

                Text("জনরা / স্টাইল (Genre):", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    genres.take(3).forEach { g ->
                        FilterChip(
                            selected = selectedGenre == g,
                            onClick = { selectedGenre = g },
                            label = { Text(g, fontSize = 12.sp) }
                        )
                    }
                }

                Button(
                    onClick = { generateSurLyrics() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isGenerating,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("সুর এআই লিরিক্স তৈরি হচ্ছে...")
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("জেনারেট করুন (Sur AI Studio)")
                    }
                }

                if (isGenerating) {
                    Spacer(modifier = Modifier.height(4.dp))
                    AiGenerationShimmerCard()
                }

                AnimatedVisibility(visible = generatedResult.isNotBlank()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("তৈরিকৃত লিরিক্স প্রিভিউ:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = generatedResult,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = onDismiss) {
                                Text("বাতিল")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    onApplyLyrics(generatedResult)
                                    onDismiss()
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("লিরিক্স ব্যবহার করুন")
                            }
                        }
                    }
                }
            }
        },
        dismissButton = {
            if (generatedResult.isBlank()) {
                TextButton(onClick = onDismiss) {
                    Text("বন্ধ করুন")
                }
            }
        }
    )
}
