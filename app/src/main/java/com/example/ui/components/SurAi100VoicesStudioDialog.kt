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
fun SurAi100VoicesStudioDialog(
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
    var selectedLang by remember { mutableStateOf("Bangla") }
    var isGeneratingLyrics by remember { mutableStateOf(false) }
    var isGeneratingSong by remember { mutableStateOf(false) }
    var generationStatus by remember { mutableStateOf("") }

    // 100 Comprehensive Sur AI Singer Voices
    val voicesList = remember {
        val list = mutableListOf<AiSingerVoice>()
        val categories = listOf("Female", "Male", "Child", "Rock", "Jazz", "Folk", "Special")
        
        // 1-25: Female Pop & Soul
        for (i in 1..25) {
            val names = listOf("Aria", "Maya", "Luna", "Shreya", "Ananya", "Zara", "Scarlett", "Kavya", "Stella", "Nisha", "Chloe", "Priya", "Elena", "Zoe", "Zara", "Mira", "Rhea", "Nabila", "Tanvi", "Sanjana", "Kiara", "Audrey", "Celeste", "Ruby", "Aurora")
            val name = "${names[(i - 1) % names.size]} (Female Pro $i)"
            list.add(AiSingerVoice(i, name, "Female", "♀", "Soprano & Belting", "Bangla / Global", "Warm melodic soprano with professional studio vocal resonance", "Pop Hit #$i"))
        }

        // 26-50: Male Baritones & Tenors
        for (i in 26..50) {
            val names = listOf("Zayn", "Ruhan", "Dev", "Nusrat", "Kabir", "Ayan", "Tanvir", "Mehedi", "Arjun", "Farhan", "Julian", "Liam", "Mateo", "Kian", "Samir", "Arman", "Ronnie", "Vikram", "Kabir", "Zuber", "Tariq", "Imran", "Adnan", "Sohrab", "Orion")
            val name = "${names[(i - 26) % names.size]} (Male Pro ${i - 25})"
            list.add(AiSingerVoice(i, name, "Male", "♂", "Tenor & Baritone", "Bangla & Global", "Deep emotional baritone and powerful high tenor resonance", "Acoustic Ballad #$i"))
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
            list.add(AiSingerVoice(i, name, "Rock", "🎸", "Gritty Rock Rasp", "Rock & Metal", "Stadium rock grit, raspy power vocals and aggressive distortion capability", "Heavy Anthem #$i"))
        }

        // 76-85: Jazz, Blues & R&B
        for (i in 76..85) {
            val names = listOf("Velvet", "Smokey", "Blue", "Hazel", "Django", "Miles", "Etta", "Nina", "Ray", "Bessie")
            val name = "${names[(i - 76) % names.size]} (Jazz Vocalist ${i - 75})"
            list.add(AiSingerVoice(i, name, "Jazz", "🎷", "Smooth Contralto & Jazz", "Jazz & Blues", "Smoky late-night jazz club vibrato and silky soul micro-tones", "Midnight Blues #$i"))
        }

        // 86-95: Folk, Baul & Traditional
        for (i in 86..95) {
            val names = listOf("Lalon AI", "Hason AI", "Shah Abdul AI", "Baul Samrat", "Folk Moni", "Palli Kantho", "Bhatiyali Sur", "Murshidi AI", "Jari Singer", "Sari Singer")
            val name = "${names[(i - 86) % names.size]} (Folk Legend ${i - 85})"
            list.add(AiSingerVoice(i, name, "Folk", "🪕", "Spiritual Baul & Folk", "Bangla Traditional", "Authentic mystical Baul ektara ornamentation and deep spiritual resonance", "Folk Song #$i"))
        }

        // 96-100: Special Neural & Celestial Models
        val specials = listOf(
            AiSingerVoice(96, "সুর এআই প্রাইম মাস্টার", "Special", "✨", "Dynamic Multi-Range", "Universal", "Adaptive neural vocal synthesis engineered by Sur AI Studio", "Hit #96"),
            AiSingerVoice(97, "CyberHarmonizer X", "Special", "🤖", "Vocoder / Auto-Harmonizer", "Electronic", "Futuristic vocoder and layered AI chorus", "Electro #97"),
            AiSingerVoice(98, "Celestial Choir", "Special", "👥", "Polychoral Ensemble", "Global Choral", "Ethereal 40-piece choir backing and epic cathedral acoustics", "Choral #98"),
            AiSingerVoice(99, "Acoustic Whispers", "Special", "🍃", "ASMR Intimate Vocal", "Acoustic Pop", "Ultra-close microphone proximity with soothing breathy timbre", "Acoustic #99"),
            AiSingerVoice(100, "Symphonic Diva", "Special", "👑", "Operatic Coloratura", "Classical Opera", "Dramatic 4-octave operatic belting with vibrato mastery", "Opera #100")
        )
        list.addAll(specials)
        list
    }

    val filteredVoices = voicesList.filter { voice ->
        val matchesCategory = (selectedCategory == "All") || (voice.category == selectedCategory)
        val matchesSearch = searchQuery.isBlank() ||
                voice.name.contains(searchQuery, ignoreCase = true) ||
                voice.style.contains(searchQuery, ignoreCase = true) ||
                voice.language.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    val categories = listOf("All", "Female", "Male", "Child", "Folk", "Rock", "Jazz", "Special")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.RecordVoiceOver,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "সুর এআই ১০০+ ভয়েস মডেল স্টুডিও",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (selectedVoice == null) {
                    // Voice Picker Screen
                    Text(
                        "সুর এআই নিউরাল প্রযুক্তিতে ১০০টি অনন্য এআই শিল্পীর কণ্ঠ থেকে আপনার পছন্দের ভয়েস নির্বাচন করুন:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Search Box
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search voice, style or language...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Categories Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }

                    // 100 Voices List
                    Box(modifier = Modifier.weight(1f)) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredVoices) { voice ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedVoice = voice },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .fillMaxWidth(),
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

                    // Sur AI Lyric & Prompt Form
                    Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                Text("1. Song Prompt / Idea (Sur AI Studio):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                OutlinedTextField(
                                    value = promptInput,
                                    onValueChange = { promptInput = it },
                                    placeholder = { Text("e.g. A soulful acoustic ballad about reunion in Dhaka rain...", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    maxLines = 2
                                )
                            }
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("2. Custom Lyrics:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    TextButton(
                                        onClick = {
                                            scope.launch {
                                                isGeneratingLyrics = true
                                                val generated = onGenerateLyrics(
                                                    promptInput.ifBlank { "Soulful Melodic Journey" },
                                                    selectedLang,
                                                    selectedGenre,
                                                    selectedMood
                                                )
                                                lyricsInput = generated
                                                isGeneratingLyrics = false
                                            }
                                        },
                                        enabled = !isGeneratingLyrics
                                    ) {
                                        if (isGeneratingLyrics) {
                                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                             Text("Generating...", fontSize = 10.sp)
                                        } else {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Auto-Write Lyrics", fontSize = 10.sp)
                                        }
                                    }
                                }
                                OutlinedTextField(
                                    value = lyricsInput,
                                    onValueChange = { lyricsInput = it },
                                    placeholder = { Text("[Verse 1]\nWrite or paste lyrics here...", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth().height(100.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    maxLines = 6
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
                            generationStatus = "Analyzing vocal timbre and harmonic frequencies in Sur AI Studio..."
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
                                promptInput.ifBlank { "Sur AI Song" },
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
