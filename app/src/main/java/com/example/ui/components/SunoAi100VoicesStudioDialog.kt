package com.example.ui.components

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AiSingerVoice(
    val id: Int,
    val name: String,
    val category: String, // "Female", "Male", "Child", "Rock", "Jazz", "Folk", "Special"
    val icon: String,
    val style: String,
    val language: String,
    val description: String,
    val sampleSongTag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SunoAi100VoicesStudioDialog(
    onDismiss: () -> Unit,
    onGenerateWithVoice: (voiceName: String, lyrics: String, prompt: String, genre: String, mood: String) -> Unit,
    onGenerateLyrics: suspend (prompt: String, lang: String, genre: String, vibe: String) -> String
) {
    val scope = rememberCoroutineScope()
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedVoice by remember { mutableStateOf<AiSingerVoice?>(null) }

    var promptInput by remember { mutableStateOf("") }
    var lyricsInput by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf("Pop") }
    var selectedMood by remember { mutableStateOf("Upbeat") }
    var selectedLang by remember { mutableStateOf("English") }
    var isGeneratingLyrics by remember { mutableStateOf(false) }
    var isGeneratingSong by remember { mutableStateOf(false) }
    var generationStatus by remember { mutableStateOf("") }

    // 100 Comprehensive AI Singer Voices
    val voicesList = remember {
        val list = mutableListOf<AiSingerVoice>()
        val categories = listOf("Female", "Male", "Child", "Rock", "Jazz", "Folk", "Special")
        
        // 1-25: Female Pop & Soul
        for (i in 1..25) {
            val names = listOf("Aria", "Maya", "Luna", "Shreya", "Ananya", "Zara", "Scarlett", "Kavya", "Stella", "Nisha", "Chloe", "Priya", "Elena", "Zoe", "Zara", "Mira", "Rhea", "Nabila", "Tanvi", "Sanjana", "Kiara", "Audrey", "Celeste", "Ruby", "Aurora")
            val name = "${names[(i - 1) % names.size]} (Female Pro $i)"
            list.add(AiSingerVoice(i, name, "Female", "♀", "Soprano & Belting", "English / Bangla", "Warm melodic soprano with professional studio vocal resonance", "Pop Hit #$i"))
        }

        // 26-50: Male Baritones & Tenors
        for (i in 26..50) {
            val names = listOf("Zayn", "Ruhan", "Dev", "Nusrat", "Kabir", "Ayan", "Tanvir", "Mehedi", "Arjun", "Farhan", "Julian", "Liam", "Mateo", "Kian", "Samir", "Arman", "Ronnie", "Vikram", "Kabir", "Zuber", "Tariq", "Imran", "Adnan", "Sohrab", "Orion")
            val name = "${names[(i - 26) % names.size]} (Male Pro ${i - 25})"
            list.add(AiSingerVoice(i, name, "Male", "♂", "Tenor & Baritone", "Global & Multi-lingual", "Deep emotional baritone and powerful high tenor resonance", "Acoustic Ballad #$i"))
        }

        // 51-65: Children & Kids
        for (i in 51..65) {
            val names = listOf("Robi", "Tuli", "Timmy", "Mimi", "Piku", "Joy", "Sunny", "Daisy", "Bably", "Chinu", "Rinku", "Benu", "Tinku", "Panna", "Kona")
            val name = "${names[(i - 51) % names.size]} (Kids Voice ${i - 50})"
            list.add(AiSingerVoice(i, name, "Child", "🧒", "Child Soprano", "Bangla & English", "Pure joyful innocent child vocals for nursery rhymes and cheerful songs", "Kids Rhyme #$i"))
        }

        // 66-75: Rock, Metal & Grunge
        for (i in 66..75) {
            val names = listOf("Stark", "Blaze", "Vortex", "Rage", "Cobra", "Titan", "Viper", "Thor", "Diesel", "Nexus")
            val name = "${names[(i - 66) % names.size]} (Rock Star ${i - 65})"
            list.add(AiSingerVoice(i, name, "Rock", "🎸", "Gritty Rock Rasp", "English & Rock", "Stadium rock grit, raspy power vocals and aggressive distortion capability", "Heavy Anthem #$i"))
        }

        // 76-85: Jazz, Blues & R&B
        for (i in 76..85) {
            val names = listOf("Velvet", "Smokey", "Blue", "Hazel", "Django", "Miles", "Etta", "Nina", "Ray", "Bessie")
            val name = "${names[(i - 76) % names.size]} (Jazz Vocalist ${i - 75})"
            list.add(AiSingerVoice(i, name, "Jazz", "🎷", "Smooth Contralto & Jazz", "English & Blues", "Smoky late-night jazz club vibrato and silky soul micro-tones", "Midnight Blues #$i"))
        }

        // 86-95: Folk, Baul & Traditional
        for (i in 86..95) {
            val names = listOf("Lalon", "Hason", "Pagla", "Kuddus", "Radha", "Benu", "Gour", "Padma", "Meghna", "Tista")
            val name = "${names[(i - 86) % names.size]} (Folk Bard ${i - 85})"
            list.add(AiSingerVoice(i, name, "Folk", "🌾", "Earthy Folk Resonance", "Bengali Folk & Sufi", "Authentic rural acoustic soul, ektara undertones and meditative folk chants", "Baul Melody #$i"))
        }

        // 96-100: Cyberpunk, Robotic & Choir
        for (i in 96..100) {
            val names = listOf("CyberX", "Vocodex", "Celestial Choir", "Matrix Alpha", "Synthetica")
            val name = "${names[(i - 96) % names.size]} (AI Special ${i - 95})"
            list.add(AiSingerVoice(i, name, "Special", "✨", "Neural Synth Voice", "Futuristic AI", "Multi-layered robotic vocoder, choir cathedral harmonics and AI synthesis", "Cyber EDM #$i"))
        }

        list
    }

    val filteredVoices = voicesList.filter { voice ->
        (selectedCategory == "All" || voice.category == selectedCategory) &&
                (searchQuery.isBlank() || voice.name.contains(searchQuery, ignoreCase = true) || voice.style.contains(searchQuery, ignoreCase = true))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.95f),
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(color = MaterialTheme.colorScheme.primary, shape = CircleShape) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(8.dp).size(22.dp))
                    }
                    Column {
                        Text("Suno.ai 100+ Voice Studio", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Select from 100 Pro AI Singer Voices & Generate Songs", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Search & Filter
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search 100 AI singer voices (e.g. Aria, Zayn, Robi, Rock)...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Category Chips
                val categories = listOf("All", "Female", "Male", "Child", "Rock", "Jazz", "Folk", "Special")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        val isSel = selectedCategory == cat
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                HorizontalDivider()

                // If voice not selected, show Voice Browser
                if (selectedVoice == null) {
                    Text("Select an AI Singer Voice (${filteredVoices.size} available):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(filteredVoices) { voice ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    modifier = Modifier.fillMaxWidth().clickable { selectedVoice = voice }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                shape = CircleShape,
                                                modifier = Modifier.size(40.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(voice.icon, fontSize = 18.sp)
                                                }
                                            }
                                            Column {
                                                Text(voice.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text("${voice.style} • ${voice.language}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                                Text(voice.description, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                            }
                                        }
                                        Button(
                                            onClick = { selectedVoice = voice },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("Use Voice", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Selected Voice Config & Song Generation Studio
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(selectedVoice!!.icon, fontSize = 24.sp)
                                Column {
                                    Text("Selected AI Singer: ${selectedVoice!!.name}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Text(selectedVoice!!.style, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                            TextButton(onClick = { selectedVoice = null }) {
                                Text("Change Voice", fontSize = 11.sp)
                            }
                        }
                    }

                    // Suno AI Lyric & Prompt Form
                    Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                Text("1. Song Prompt / Idea (Sun.ai style):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                OutlinedTextField(
                                    value = promptInput,
                                    onValueChange = { promptInput = it },
                                    placeholder = { Text("e.g. An emotional acoustic song about rainy evening in Dhaka...", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    maxLines = 2
                                )
                            }
                            item {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("2. Lyrics (Auto-Generate or Custom):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                isGeneratingLyrics = true
                                                try {
                                                    val generated = onGenerateLyrics(
                                                        promptInput.ifBlank { "Emotional melody about love and rain" },
                                                        selectedLang,
                                                        selectedGenre,
                                                        selectedMood
                                                    )
                                                    lyricsInput = generated
                                                } catch (e: Exception) {
                                                    lyricsInput = "[Verse 1]\nRaindrops falling on the glass\nMoments that will never pass\n\n[Chorus]\nSinging in this AI tune\nDancing underneath the moon"
                                                }
                                                isGeneratingLyrics = false
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        if (isGeneratingLyrics) {
                                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("AI Write Lyrics", fontSize = 11.sp)
                                    }
                                }
                                OutlinedTextField(
                                    value = lyricsInput,
                                    onValueChange = { lyricsInput = it },
                                    placeholder = { Text("Enter lyrics or click AI Write Lyrics...", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth().height(120.dp),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                            item {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Genre", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        OutlinedTextField(value = selectedGenre, onValueChange = { selectedGenre = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(8.dp))
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Mood", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        OutlinedTextField(value = selectedMood, onValueChange = { selectedMood = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(8.dp))
                                    }
                                }
                            }
                            if (isGeneratingSong) {
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                            Column {
                                                Text("Synthesizing AI Song with ${selectedVoice!!.name}...", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                Text(generationStatus, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (selectedVoice != null) {
                Button(
                    onClick = {
                        scope.launch {
                            isGeneratingSong = true
                            generationStatus = "Analyzing vocal timbre and harmonic frequencies..."
                            kotlinx.coroutines.delay(800)
                            generationStatus = "Generating musical arrangement & drum groove..."
                            kotlinx.coroutines.delay(800)
                            generationStatus = "Synthesizing AI singer voice (${selectedVoice!!.name})..."
                            kotlinx.coroutines.delay(1000)
                            generationStatus = "Mastering broadcast track (-14 LUFS)..."
                            kotlinx.coroutines.delay(600)

                            onGenerateWithVoice(
                                selectedVoice!!.name,
                                lyricsInput.ifBlank { "[Verse]\nAI Generated Song by ${selectedVoice!!.name}" },
                                promptInput.ifBlank { "Suno AI Song" },
                                selectedGenre,
                                selectedMood
                            )
                            isGeneratingSong = false
                            onDismiss()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isGeneratingSong
                ) {
                    Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Generate AI Song with ${selectedVoice!!.name}")
                }
            } else {
                Button(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                    Text("Close")
                }
            }
        }
    )
}
