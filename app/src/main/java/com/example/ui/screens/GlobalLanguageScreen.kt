package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.repository.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalLanguageScreen(
    appLanguage: String = "en",
    translateState: RealtimeTranslateSingingState,
    accentState: AccentChangerState,
    dialectData: RegionalDialectData,
    signLangData: SignLanguageVideoData,
    brailleData: BrailleLyricsData,
    duetState: MultiLangDuetState
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }

    val categories = listOf(
        "🌐 Live Translate",
        "🗣️ Accent Changer",
        "🗣️ Regional Dialects",
        "🤟 Sign Language Video",
        "⠠⠃ Braille Export",
        "🎤 Multi-Lang Duet"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (appLanguage == "bn") "গ্লোবাল ও ভাষা স্টুডিও" else "Global & Language Studio",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (appLanguage == "bn") "৬টি গ্লোবাল এআই ও এক্সেসিবিলিটি ফিচার" else "6 Global AI Voice & Accessibility Features",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                        Text("Multilingual AI", modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(categories) { idx, catName ->
                        FilterChip(
                            selected = selectedTab == idx,
                            onClick = { selectedTab = idx },
                            label = { Text(catName, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> LiveTranslateSubView(translateState)
                1 -> AccentChangerSubView(accentState)
                2 -> RegionalDialectsSubView(dialectData)
                3 -> SignLanguageSubView(signLangData)
                4 -> BrailleExportSubView(brailleData)
                5 -> MultiLangDuetSubView(duetState)
            }
        }
    }
}

// 1. Real-time Translate Singing
@Composable
private fun LiveTranslateSubView(state: RealtimeTranslateSingingState) {
    val context = LocalContext.current
    var sourceLang by remember { mutableStateOf(state.sourceLanguage) }
    var targetLang by remember { mutableStateOf(state.targetLanguage) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🌐 Real-time Translate Singing", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Translates singing voice while matching pitch, melody & vocal formants", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = {}) { Text("Source: $sourceLang") }
                    Icon(Icons.Default.SwapHoriz, contentDescription = "Swap")
                    OutlinedButton(onClick = {}) { Text("Target: $targetLang") }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Original Vocals ($sourceLang):", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(state.sourceLyricsSnippet, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Translated Vocals ($targetLang):", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        Text(state.translatedLyricsSnippet, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Pitch Accuracy: ${state.pitchMatchAccuracyPercent}%", fontSize = 12.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    Text("Formant Preserved: ${if (state.isFormantPreserved) "Yes ✓" else "No"}", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { Toast.makeText(context, "Real-time Live Translated Audio Stream Active!", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Live Translate Singing")
                }
            }
        }
    }
}

// 2. Accent Changer
@Composable
private fun AccentChangerSubView(state: AccentChangerState) {
    val context = LocalContext.current
    var selectedAccent by remember { mutableStateOf(state.currentAccent) }
    var intensity by remember { mutableStateOf(state.pronunciationIntensityPercent.toFloat()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🗣️ Vocal Accent Changer & Formant Shifter", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Transform singing voice pronunciation accent without affecting melody", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(16.dp))

                Text("Select Target Vocal Accent:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.availableAccents) { acc ->
                        FilterChip(
                            selected = selectedAccent == acc,
                            onClick = { selectedAccent = acc },
                            label = { Text(acc, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Accent Pronunciation Intensity: ${intensity.toInt()}%", fontSize = 12.sp)
                Slider(
                    value = intensity,
                    onValueChange = { intensity = it },
                    valueRange = 0f..100f
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { Toast.makeText(context, "Applied $selectedAccent at ${intensity.toInt()}% Intensity!", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Apply Accent Transformation")
                }
            }
        }
    }
}

// 3. Regional Dialects (Bangla Regional Dialects)
@Composable
private fun RegionalDialectsSubView(data: RegionalDialectData) {
    val context = LocalContext.current
    var inputLyrics by remember { mutableStateOf(data.standardLyrics) }
    var selectedDialect by remember { mutableStateOf(data.selectedDialect) }
    var outputLyrics by remember { mutableStateOf(data.convertedLyrics) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🗣️ Regional Dialect Transformer (Bangla Dialects)", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Convert standard Bangla lyrics into Chatgaya, Sylheti, Noakhali & Dhakaiya", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = inputLyrics,
                    onValueChange = { inputLyrics = it },
                    label = { Text("Standard Bangla Lyrics") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Select Regional Dialect:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(data.availableDialects) { d ->
                        FilterChip(
                            selected = selectedDialect == d,
                            onClick = {
                                selectedDialect = d
                                outputLyrics = when {
                                    d.contains("Chatgaya") -> "আই তুঁয়ারে বহুৎ পেয়ার গরি, তুঁই আঁর লগে আইবা নে?"
                                    d.contains("Sylheti") -> "আমি তোমার লাগি ব্লা ব্লা টান অনুভব করিরাম!"
                                    d.contains("Noakhali") -> "আই তোমারে হেক্কল চেয়ে বেশি হালাবাষি!"
                                    d.contains("Dhakaiya") -> "আরে মামা তুমি লগে চলো, পুরা আগুন গান বাঁধমু!"
                                    else -> "আমি তোমাকে অনেক ভালোবাসি..."
                                }
                            },
                            label = { Text(d, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("$selectedDialect Lyrics Result:", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text(outputLyrics, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { Toast.makeText(context, "Synthesized $selectedDialect Voice Track!", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate $selectedDialect Vocal Track")
                }
            }
        }
    }
}

// 4. Sign Language Video
@Composable
private fun SignLanguageSubView(data: SignLanguageVideoData) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🤟 Sign Language Video Generator", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("${data.signLanguageStandard} • Synced 3D Avatar for deaf & hard-of-hearing music lovers", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(
                        model = data.videoPreviewUrl,
                        contentDescription = "Sign Language Video",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth()
                    ) {
                        Text("Previewing ${data.avatarStyle} @ ${data.frameFps} FPS", color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { Toast.makeText(context, "Rendering BdSL Sign Language Video...", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Videocam, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Sign Language Video")
                }
            }
        }
    }
}

// 5. Braille Lyrics Export
@Composable
private fun BrailleExportSubView(data: BrailleLyricsData) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("⠠⠃ Braille Lyrics Unicode Export", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("${data.brailleGrade} • Formatted for tactile refreshable Braille displays", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Original Text:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(data.originalLyrics, fontSize = 13.sp)

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Braille Unicode Notation:", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text(data.brailleUnicodeOutput, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(data.brailleUnicodeOutput))
                        Toast.makeText(context, "Braille Unicode Copied to Clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copy Braille Unicode Text")
                }
            }
        }
    }
}

// 6. Multi-Lang Duet
@Composable
private fun MultiLangDuetSubView(duet: MultiLangDuetState) {
    val context = LocalContext.current
    var blend by remember { mutableStateOf(duet.harmonyBlendPercent.toFloat()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🎤 Multi-Lang Cross-Language Duet Studio", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("${duet.partnerA} 🤝 ${duet.partnerB}", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)

                Spacer(modifier = Modifier.height(16.dp))

                Text("Bilingual Synced Lyrics Flow:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                duet.lyricsLines.forEach { (speaker, line) ->
                    ListItem(
                        headlineContent = { Text(line, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                        supportingContent = { Text("Singer: $speaker", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    )
                    Divider()
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Language Voice Harmony Blend: ${blend.toInt()}%", fontSize = 12.sp)
                Slider(value = blend, onValueChange = { blend = it }, valueRange = 0f..100f)

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { Toast.makeText(context, "Multi-Lang Duet Track Rendered!", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Generate Cross-Language Duet Track")
                }
            }
        }
    }
}
