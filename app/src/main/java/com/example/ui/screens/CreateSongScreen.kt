package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.example.data.local.ClonedVoiceEntity
import com.example.data.local.LyricsHistoryEntity
import com.example.data.local.SongEntity
import com.example.data.repository.MoodAnalysisResult
import com.example.data.repository.VocalCoachResult
import com.example.ui.components.AiVocalTunerDialog
import com.example.ui.components.MelodyHummingDialog
import com.example.ui.components.VoiceCloningDialog
import com.example.ui.components.SongCreationPreviewDialog
import com.example.ui.components.AiAlbumArtGeneratorDialog
import com.example.ui.components.VirtualInstrumentBeatPadDialog
import com.example.ui.components.BanglaLyricsRhymeEngineDialog
import com.example.ui.components.HummingToSargamTranscriptionDialog
import com.example.ui.components.LiveKaraokeVocalStudioDialog
import com.example.ui.components.MultiTrackDawTimelineDialog
import com.example.ui.components.GuitarChordsVisualizerDialog
import com.example.ui.components.RiyazTanpuraStudioDialog
import com.example.ui.components.AudioMasteringEqVisualizerDialog
import com.example.ui.components.BengaliLyricistNotepadDialog
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ComposerSection(
    val title: String,
    val duration: String,
    val instruments: String,
    val tensionLevel: Float // 0.0 to 1.0
)

data class BandMember(
    val id: Int,
    val name: String,
    val role: String,
    val personality: String,
    var isSolo: Boolean = false,
    var isMuted: Boolean = false
)

data class LoopTrack(
    val id: Int,
    val name: String,
    var isRecording: Boolean = false,
    var isPlaying: Boolean = true,
    var isMuted: Boolean = false,
    var volume: Float = 0.8f
)

data class AiVoiceModel(
    val name: String,
    val gender: String, // "Female", "Male", "Special"
    val genderIcon: String, // "♀", "♂", "✨"
    val vocalStyle: String,
    val languageRegion: String,
    val description: String,
    val recommendedGenre: String
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CreateSongScreen(
    lyricsHistory: List<LyricsHistoryEntity>,
    clonedVoices: List<ClonedVoiceEntity>,
    onGenerateSong: (prompt: String, genre: String, vibe: String, lyrics: String) -> Unit,
    onGenerateLyrics: suspend (prompt: String, language: String, genre: String, vibe: String) -> String,
    onCheckCopyright: suspend (lyrics: String) -> Boolean,
    onSaveLyrics: suspend (title: String, lang: String, lyrics: String, isClean: Boolean) -> Unit,
    onDeleteLyrics: suspend (id: Long) -> Long,
    onSaveVoice: suspend (name: String, desc: String) -> Unit,
    onDeleteVoice: suspend (id: Long) -> Unit,
    getChords: (genre: String) -> List<String>,
    getBeats: (genre: String) -> List<String>,
    getMelody: (key: String, scale: String) -> List<String>,
    onAnalyzeMood: suspend (lyrics: String) -> MoodAnalysisResult,
    onAutoFinishLyrics: suspend (lyrics: String, scheme: String) -> String,
    onTransformGenre: suspend (lyrics: String, targetGenre: String) -> String,
    onAnalyzeVocal: suspend () -> VocalCoachResult,
    initialTab: Int = 0,
    onGeneratePreviewSong: (suspend (prompt: String, genre: String, vibe: String, lyrics: String) -> SongEntity?)? = null,
    isPlaying: Boolean = false,
    playbackProgress: Float = 0f,
    playbackDurationSeconds: Int = 210,
    isBuffering: Boolean = false,
    onPlayPause: () -> Unit = {},
    onSeek: (Float) -> Unit = {},
    onNavigateToHome: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by rememberSaveable(initialTab) { mutableStateOf(initialTab) } // 0: Studio Gen, 1: Lyrics Studio, 2: Advanced AI Tech, 3: Pro Toolkit & Stems

    var showHummingDialog by rememberSaveable { mutableStateOf(false) }
    var showVocalTunerDialog by rememberSaveable { mutableStateOf(false) }
    var showVoiceCloningDialog by rememberSaveable { mutableStateOf(false) }
    var showAiCoverArtDialog by rememberSaveable { mutableStateOf(false) }
    var showBeatPadDialog by rememberSaveable { mutableStateOf(false) }
    var showRhymeEngineDialog by rememberSaveable { mutableStateOf(false) }
    var showHummingSargamDialog by rememberSaveable { mutableStateOf(false) }
    var showLiveKaraokeDialog by rememberSaveable { mutableStateOf(false) }
    var showMultiTrackDawDialog by rememberSaveable { mutableStateOf(false) }
    var showGuitarChordsDialog by rememberSaveable { mutableStateOf(false) }
    var showTanpuraRiyazDialog by rememberSaveable { mutableStateOf(false) }
    var showMasteringEqDialog by rememberSaveable { mutableStateOf(false) }
    var showLyricistNotepadDialog by rememberSaveable { mutableStateOf(false) }
    var customCoverArtUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var customClonedVoices by remember { mutableStateOf(listOf<String>()) }
    var previewSong by remember { mutableStateOf<SongEntity?>(null) }

    // Core Feature 1: AI Lyrics Generator prompt & language
    var prompt by rememberSaveable { mutableStateOf("") }
    var selectedLanguage by rememberSaveable { mutableStateOf("English") }

    // Core Feature 2 & 9: 25+ Genres & Dual Genre Fusion
    var primaryGenre by rememberSaveable { mutableStateOf("Pop") }
    var secondaryGenre by rememberSaveable { mutableStateOf("EDM") }
    var isDualGenreEnabled by rememberSaveable { mutableStateOf(true) }

    // Vibe & Atmosphere (Mood Selector)
    var selectedVibe by rememberSaveable { mutableStateOf("Chill") }

    // Core Feature 3 & 4: Lyrics Editor & Copyright
    var customLyrics by rememberSaveable { mutableStateOf("") }
    var isCheckingCopyright by rememberSaveable { mutableStateOf(false) }
    var copyrightStatus by rememberSaveable { mutableStateOf<Boolean?>(null) }

    // Core Feature 5 & 11: AI Singer Voice TTS & Duet Mode (Male/Female/Special)
    var selectedVoice by rememberSaveable { mutableStateOf("Aria (Warm Soprano)") }
    var selectedVoiceGenderFilter by rememberSaveable { mutableStateOf("All") } // "All", "Female", "Male", "Special"
    var selectedDuetGenderFilter by rememberSaveable { mutableStateOf("All") }
    var voiceMood by rememberSaveable { mutableStateOf("Energetic") }
    var voiceSpeed by rememberSaveable { mutableStateOf(1.0f) }
    var isDuetModeEnabled by rememberSaveable { mutableStateOf(false) }
    var duetSecondVoice by rememberSaveable { mutableStateOf("Zayn (Soulful Tenor)") }

    // Core Feature 8: Instrument Auto Add
    val availableInstruments = listOf("808 Bass", "Drums", "Electric Guitar", "Acoustic Piano", "Synthesizer", "Tabla", "Dotara", "Strings", "Brass", "Flute")
    var selectedInstruments by remember { mutableStateOf(setOf("808 Bass", "Drums", "Synthesizer")) }

    // Core Feature 7 & 6: Suno v4 AI Music Generator & MP3 Export
    var isGeneratingSong by rememberSaveable { mutableStateOf(false) }
    var generationStepText by rememberSaveable { mutableStateOf("") }
    var useExternalSunoApi by rememberSaveable { mutableStateOf(false) }
    var externalSunoApiKey by rememberSaveable { mutableStateOf("") }

    // Core Feature 10: Voice Cloning
    var cloneName by rememberSaveable { mutableStateOf("") }
    var isRecordingVoice by rememberSaveable { mutableStateOf(false) }

    // Core Feature 12: Song to Lyrics Transcribe
    var isTranscribingAudio by rememberSaveable { mutableStateOf(false) }

    // Core Feature 13 & 14: Chord Progression & Beat Suggestion AI
    var chordList by remember { mutableStateOf(listOf<String>()) }
    var beatList by remember { mutableStateOf(listOf<String>()) }

    // Core Feature 15: Melody Generator
    var selectedKey by rememberSaveable { mutableStateOf("C") }
    var selectedScale by rememberSaveable { mutableStateOf("Major") }
    var melodyNotes by remember { mutableStateOf(listOf<String>()) }

    // Core Feature 16: Stem Separation
    var vocalStemVol by rememberSaveable { mutableStateOf(0.9f) }
    var drumStemVol by rememberSaveable { mutableStateOf(0.85f) }
    var bassStemVol by rememberSaveable { mutableStateOf(0.95f) }
    var synthStemVol by rememberSaveable { mutableStateOf(0.80f) }

    // Core Feature 17: Pitch Detection
    var isDetectingPitch by remember { mutableStateOf(false) }
    var detectedPitchNote by remember { mutableStateOf("C4 - 261.6 Hz") }

    // ADVANCED AI TECH - 10 FEATURES STATES
    // 1. AI Composer Mode
    var composerSections by remember {
        mutableStateOf(
            listOf(
                ComposerSection("Intro", "0:00 - 0:20", "Acoustic Arpeggio & Ambient Pad", 0.2f),
                ComposerSection("Verse 1", "0:20 - 0:50", "Soft Drums, 808 Bass & Clean Vocal", 0.4f),
                ComposerSection("Pre-Chorus", "0:50 - 1:10", "Rising Synth Riser & Snare Build", 0.7f),
                ComposerSection("Chorus", "1:10 - 1:45", "Full Drop, Layered Vocals & Brass", 0.95f),
                ComposerSection("Bridge", "1:45 - 2:15", "Isolated Dotara/Violin Solo", 0.6f),
                ComposerSection("Chorus Peak", "2:15 - 2:50", "Max Intensity & Dual Duet Vocals", 1.0f),
                ComposerSection("Outro", "2:50 - 3:15", "Fading Reverb & Vinyl Crackle", 0.25f)
            )
        )
    }

    // 2. Mood Detector (from lyrics)
    var moodAnalysis by remember { mutableStateOf<MoodAnalysisResult?>(null) }
    var isAnalyzingMood by remember { mutableStateOf(false) }

    // 3. Voice to Instrument
    var targetInstrumentConversion by remember { mutableStateOf("Saxophone") }
    var isConvertingVoiceToInst by remember { mutableStateOf(false) }
    var voiceConversionResultText by remember { mutableStateOf<String?>(null) }

    // 4. AI Band Mode
    var bandMembers by remember {
        mutableStateOf(
            listOf(
                BandMember(1, "Aria", "Lead Vocalist", "Energetic Pop Powerhouse"),
                BandMember(2, "Leo", "Lead Guitarist", "Neoclassical Shredder"),
                BandMember(3, "Marcus", "808 Bassist", "Sub-bass & Groove Specialist"),
                BandMember(4, "Kai", "Drummer", "Precision Poly-rhythmic Engine"),
                BandMember(5, "Nova", "Synth & Keys", "Ambient & Cyber Synthwave")
            )
        )
    }

    // 5. Genre Transformer
    var targetTransformGenre by remember { mutableStateOf("Synthwave Cyberpunk") }
    var isTransformingGenre by remember { mutableStateOf(false) }

    // 6. Loop Station AI
    var loopTracks by remember {
        mutableStateOf(
            listOf(
                LoopTrack(1, "Track 1: Drums & Beat (128 BPM)"),
                LoopTrack(2, "Track 2: Sub 808 Bassline"),
                LoopTrack(3, "Track 3: Synth Lead Harmony"),
                LoopTrack(4, "Track 4: Vocal Chop FX & Reverb")
            )
        )
    }

    // 7. AI Lyric Finisher
    var selectedRhymeScheme by remember { mutableStateOf("AABB") }
    var isFinishingLyrics by remember { mutableStateOf(false) }

    // 8. Emotion Graph Timeline
    var timelinePoints by remember { mutableStateOf(listOf(20f, 40f, 75f, 95f, 60f, 100f, 25f)) }

    // 9. BPM Sync & Tap Tempo
    var currentBpm by remember { mutableIntStateOf(120) }
    var isTimeStretchPitchLocked by remember { mutableStateOf(true) }
    var lastTapTime by remember { mutableLongStateOf(0L) }

    // 10. AI Vocal Coach
    var vocalCoachData by remember { mutableStateOf<VocalCoachResult?>(null) }
    var isRecordingVocalCoach by remember { mutableStateOf(false) }

    // Data lists
    val languages = listOf(
        "English", "Bangla (বাংলা)", "Hindi (हिन्दी)", "Arabic (العربية)", "Spanish", "French", "Japanese", "Korean",
        "German", "Urdu", "Portuguese", "Italian", "Russian", "Turkish", "Mandarin", "Bengali Folk",
        "Persian", "Vietnamese", "Indonesian", "Tagalog", "Tamil", "Punjabi"
    )

    val genres = listOf(
        "Pop", "Rock", "Baul", "Nazrul", "Folk", "HipHop", "EDM", "LoFi",
        "Classical", "Jazz", "R&B", "Reggae", "Blues", "Country", "Metal", "Ambient",
        "Synthwave", "Afrobeats", "Bhangra", "Sufi", "Ghazal", "Techno", "House", "Dubstep", "Acoustic", "Cinematic"
    )

    val aiVoiceModels = listOf(
        // FEMALE VOICES (মহিলা এআই কণ্ঠ)
        AiVoiceModel("Aria (Warm Soprano)", "Female", "♀", "Warm Soprano", "Bangla & English", "Soft, melodic & emotional vocal tone", "Pop, Acoustic, Romantic"),
        AiVoiceModel("Maya (Ethereal Alto)", "Female", "♀", "Ethereal Alto", "Global / Indie", "Deep, moody & indie resonance", "Lo-Fi, Indie, Melancholic"),
        AiVoiceModel("Luna (Pop Diva)", "Female", "♀", "Bright Belter", "English & Dance", "Energetic, powerful radio-ready pop voice", "EDM, Dance, Upbeat Pop"),
        AiVoiceModel("Shreya (Semi-Classical)", "Female", "♀", "Classical Melodic", "Bangla & Hindi", "Intricate vocal runs, sweet romantic warmth", "Ghazal, Classical, Cinema"),
        AiVoiceModel("Ananya (Baul & Folk)", "Female", "♀", "Earthy Folk Soprano", "Bengali Traditional", "Authentic rural acoustic & soul-stirring melodies", "Baul, Folk, Sufi"),
        AiVoiceModel("Zara (Velvet R&B)", "Female", "♀", "Velvet Contralto", "Global Western", "Silky, smooth grooves & late-night jazz", "R&B, Soul, Smooth Jazz"),
        AiVoiceModel("Scarlett (Cyber Synth)", "Female", "♀", "Hyperpop Vocoder", "Futuristic", "Crisp auto-tuned synthwave electronic voice", "Cyberpunk, Synthwave, Techno"),
        AiVoiceModel("Kavya (Devotional & Sufi)", "Female", "♀", "Serene Soprano", "Sufi & Devotional", "Spiritual, calm & meditative vocal aura", "Sufi, Devotional, Ambient"),

        // MALE VOICES (পুরুষ এআই কণ্ঠ)
        AiVoiceModel("Zayn (Soulful Tenor)", "Male", "♂", "Soulful Tenor", "Bangla & English", "Dynamic, modern pop & acoustic warmth", "Pop, R&B, Ballads"),
        AiVoiceModel("Ruhan (Folk Bard)", "Male", "♂", "Raw Baul Tenor", "Bengali Traditional", "Rustic, authentic folk energy & Ektara depth", "Baul, Bengali Folk, Rock"),
        AiVoiceModel("Dev (Deep Baritone)", "Male", "♂", "Deep Baritone", "Global & Bangla", "Heavy bass tone, cinematic narration & punch", "Rock, Metal, Cinematic"),
        AiVoiceModel("Nusrat (Qawwali Soul)", "Male", "♂", "Sufi High Tenor", "Sufi & Ghazal", "Powerful spiritual crescendo & high octave reach", "Qawwali, Sufi, Devotional"),
        AiVoiceModel("Kabir (Ghazal Maestro)", "Male", "♂", "Warm Classical Baritone", "Urdu / Hindi / Bangla", "Rich vibrato, poetic nuance & sentimental depth", "Ghazal, Classical, Nazrul"),
        AiVoiceModel("Ayan (Hip-Hop Flow)", "Male", "♂", "Rhythmic Rap Flow", "Urban Desi & English", "Fast, punchy syllables & drill/trap attitude", "Hip-Hop, Trap, Drill"),
        AiVoiceModel("Tanvir (Rock Screamer)", "Male", "♂", "Gritty Rock Lead", "Bengali Rock", "Rasp, edge & stadium rock energy", "Alternative Rock, Grunge, Metal"),

        // SPECIAL / AI HYBRID
        AiVoiceModel("CyberVoice X-9", "Special", "✨", "Robotic Harmonizer", "AI Synth", "Futuristic vocoder & multi-layered AI chorus", "Techno, House, EDM"),
        AiVoiceModel("Suno AI v4 Prime", "Special", "✨", "Adaptive Multi-Range", "Multi-Lingual", "Dynamic neural vocal engine matching any prompt style", "All Genres")
    )
    val voices = aiVoiceModels.map { it.name }

    val moodOptions = listOf(
        Triple("Chill", "🌿", "Relaxed & calming vibes for unwinding"),
        Triple("Upbeat", "⚡", "High energy, groovy & motivational rhythm"),
        Triple("Cinematic", "🎬", "Epic, dramatic orchestral atmosphere"),
        Triple("Romantic", "💖", "Soft, acoustic love & heartfelt melodies"),
        Triple("Melancholic", "🌧️", "Deep, emotional & nostalgic tones"),
        Triple("Lo-Fi", "☕", "Warm, mellow beats & rainy day study vibes"),
        Triple("Cyberpunk", "🌆", "Dark, futuristic synthwave & heavy bass"),
        Triple("Party", "🎉", "Club anthems, EDM drops & festive beats"),
        Triple("Devotional", "🕊️", "Spiritual, sufi, serene & peaceful"),
        Triple("Inspiring", "🔥", "Heroic, motivational build-up & power")
    )
    val vibes = moodOptions.map { "${it.second} ${it.first}" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(color = MaterialTheme.colorScheme.primary, shape = CircleShape) {
                            Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.padding(6.dp).size(20.dp), tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        Column {
                            Text("Sur AI Studio v4 Pro", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("30 Pro Features • Advanced AI Tech", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 8.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Studio Gen", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Lyrics Studio", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.EditNote, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Advanced AI Tech", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Pro Toolkit & Stems", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.GraphicEq, contentDescription = null) }
                )
            }

            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                when (selectedTab) {
                    0 -> {
                        // TAB 0: STUDIO GENERATOR
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                                        Column {
                                            Text("Suno v4 & GPT-4o AI Music Studio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            Text("Fullsong generation, dual genre fusion, ElevenLabs vocal synthesis & 320kbps MP3 export.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }

                            item {
                                OutlinedTextField(
                                    value = prompt,
                                    onValueChange = { prompt = it },
                                    label = { Text("Song Title / Theme Prompt") },
                                    placeholder = { Text("e.g. Dhaka Midnight Rain, Cyberpunk Anthem") },
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
                                    trailingIcon = {
                                        IconButton(onClick = { showHummingDialog = true }) {
                                            Icon(
                                                imageVector = Icons.Default.Mic,
                                                contentDescription = "Humming to Melody",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                )
                            }

                            item {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showHummingDialog = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.Mic,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onPrimary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                            Column {
                                                Text(
                                                    text = "🎙️ সুর গুনগুন করুন (Voice / Humming to Melody AI)",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "রেকর্ড করুন — এআই স্কেল ও সুর বুঝে স্বয়ংক্রিয় প্রম্পট তৈরি করবে",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = Icons.Default.ArrowForwardIos,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color(0xFF1E293B),
                                        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { showBeatPadDialog = true }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Icon(Icons.Default.GridOn, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                                                Text("১৬-প্যাড বিট ম্যাট্রিক্স", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                            }
                                            Text("তবলা, ঢোলক, দোতারা ও ৮০৮ ড্রামস দিয়ে লাইভ বিট তৈরি করুন", fontSize = 10.sp, color = Color.LightGray)
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color(0xFF1E293B),
                                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { showHummingSargamDialog = true }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                                Text("হামিং টু স্বরলিপি", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                            }
                                            Text("গুনগুন থেকে সা-রে-গা-মা স্বরলিপি ও স্কেল ডিটেকশন", fontSize = 10.sp, color = Color.LightGray)
                                        }
                                    }
                                }
                            }

                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color(0xFF1E293B),
                                        border = BorderStroke(1.dp, Color(0xFFEC4899).copy(alpha = 0.4f)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { showLiveKaraokeDialog = true }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Icon(Icons.Default.Mic, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(16.dp))
                                                Text("লাইভ কারাওকে", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                                            }
                                            Text("ভোকাল রিমুভার ও রেকর্ডিং স্টুডিও", fontSize = 9.sp, color = Color.LightGray)
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color(0xFF1E293B),
                                        border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { showMultiTrackDawDialog = true }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(16.dp))
                                                Text("মাল্টি-ট্র্যাক DAW", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                                            }
                                            Text("৪-ট্র্যাক অডিও অ্যারেঞ্জার ও মিক্সার", fontSize = 9.sp, color = Color.LightGray)
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color(0xFF1E293B),
                                        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { showGuitarChordsDialog = true }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                                                Text("গিটার কর্ড", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                                            }
                                            Text("ইন্টারেক্টিভ ফ্রেটবোর্ড ও ট্যাব", fontSize = 9.sp, color = Color.LightGray)
                                        }
                                    }
                                }
                            }

                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color(0xFF1E293B),
                                        border = BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.4f)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { showTanpuraRiyazDialog = true }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
                                                Text("তানপুরা রিয়াজ", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                                            }
                                            Text("ড্রোন ও রাগ স্কেল গাইড", fontSize = 9.sp, color = Color.LightGray)
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color(0xFF1E293B),
                                        border = BorderStroke(1.dp, Color(0xFF06B6D4).copy(alpha = 0.4f)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { showMasteringEqDialog = true }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Icon(Icons.Default.Equalizer, contentDescription = null, tint = Color(0xFF06B6D4), modifier = Modifier.size(16.dp))
                                                Text("মাস্টারিং EQ", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                                            }
                                            Text("৭-ব্যান্ড EQ ও স্পেকট্রাম", fontSize = 9.sp, color = Color.LightGray)
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color(0xFF1E293B),
                                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { showLyricistNotepadDialog = true }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Icon(Icons.Default.EditNote, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                                Text("গীতিকার খাতা", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                                            }
                                            Text("ছন্দ-মাত্রা ও অন্ত্যমিল", fontSize = 9.sp, color = Color.LightGray)
                                        }
                                    }
                                }
                            }

                            item {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showAiCoverArtDialog = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            if (customCoverArtUrl != null) {
                                                AsyncImage(
                                                    model = customCoverArtUrl,
                                                    contentDescription = "Cover Art",
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(RoundedCornerShape(8.dp)),
                                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                )
                                            } else {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = Color(0xFFF59E0B),
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = Icons.Default.Palette,
                                                            contentDescription = null,
                                                            tint = Color.Black,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Column {
                                                Text(
                                                    text = "🎨 এআই অ্যালবাম কাভার আর্ট স্টুডিও",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = if (customCoverArtUrl != null) "কাভার আর্ট সিলেক্ট করা হয়েছে (ট্যাপ করে পরিবর্তন করুন)" else "সাইবারপাংক, বাউল ও রেট্রো স্টাইলে কাস্টম আর্ট তৈরি করুন",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = Icons.Default.ArrowForwardIos,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = Color(0xFFF59E0B)
                                        )
                                    }
                                }
                            }

                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Lyrics Language (20+ Supported)", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                                    ScrollableTabRow(
                                        selectedTabIndex = languages.indexOf(selectedLanguage).coerceAtLeast(0),
                                        edgePadding = 0.dp,
                                        containerColor = Color.Transparent,
                                        divider = {}
                                    ) {
                                        languages.forEach { lang ->
                                            Tab(
                                                selected = selectedLanguage == lang,
                                                onClick = { selectedLanguage = lang },
                                                text = { Text(lang, fontSize = 13.sp) }
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Genre Selection & Dual Fusion", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("Genre Mix", style = MaterialTheme.typography.bodySmall)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Switch(
                                                    checked = isDualGenreEnabled,
                                                    onCheckedChange = { isDualGenreEnabled = it },
                                                    modifier = Modifier.height(24.dp)
                                                )
                                            }
                                        }

                                        Text("Primary Genre: $primaryGenre", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            items(genres) { genre ->
                                                FilterChip(
                                                    selected = primaryGenre == genre,
                                                    onClick = { primaryGenre = genre },
                                                    label = { Text(genre, fontSize = 12.sp) }
                                                )
                                            }
                                        }

                                        if (isDualGenreEnabled) {
                                            Text("Secondary Genre Fusion: $secondaryGenre", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                items(genres) { genre ->
                                                    FilterChip(
                                                        selected = secondaryGenre == genre,
                                                        onClick = { secondaryGenre = genre },
                                                        label = { Text(genre, fontSize = 12.sp) }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Mood & Vibe Selector Card
                            item {
                                val currentMoodOption = moodOptions.firstOrNull { it.first == selectedVibe }
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
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
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = Icons.Default.Mood,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = "Mood & Vibe Selector",
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer
                                            ) {
                                                Text(
                                                    text = "${currentMoodOption?.second ?: "✨"} $selectedVibe",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            text = "Choose the emotional tone & sonic vibe for your AI track:",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            items(moodOptions) { mood ->
                                                val isSelected = selectedVibe == mood.first
                                                FilterChip(
                                                    selected = isSelected,
                                                    onClick = { selectedVibe = mood.first },
                                                    leadingIcon = {
                                                        Text(text = mood.second, fontSize = 14.sp)
                                                    },
                                                    label = {
                                                        Text(
                                                            text = mood.first,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                            fontSize = 12.sp
                                                        )
                                                    },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                )
                                            }
                                        }

                                        // Mood description hint box
                                        currentMoodOption?.let { mood ->
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(text = mood.second, fontSize = 16.sp)
                                                    Text(
                                                        text = mood.third,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                val currentVoiceModel = aiVoiceModels.firstOrNull { it.name == selectedVoice } ?: aiVoiceModels.first()
                                val filteredVoices = when (selectedVoiceGenderFilter) {
                                    "Female" -> aiVoiceModels.filter { it.gender == "Female" }
                                    "Male" -> aiVoiceModels.filter { it.gender == "Male" }
                                    "Special" -> aiVoiceModels.filter { it.gender == "Special" }
                                    else -> aiVoiceModels
                                }

                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = Icons.Default.RecordVoiceOver,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                }
                                                Column {
                                                    Text(
                                                        text = "AI Vocal Artists (মেল/ফিমেল কণ্ঠ)",
                                                        fontWeight = FontWeight.Bold,
                                                        style = MaterialTheme.typography.titleSmall,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = "ElevenLabs & Neural AI Voice Engine",
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("ডুয়েট মোড", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Switch(
                                                    checked = isDuetModeEnabled,
                                                    onCheckedChange = { isDuetModeEnabled = it },
                                                    modifier = Modifier.height(24.dp)
                                                )
                                            }
                                        }

                                        // Gender Filter Tabs for Lead Voice
                                        Text(
                                            text = "কণ্ঠের ধরণ নির্বাচন করুন (Gender & Tone):",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            val filterTabs = listOf(
                                                "All" to "সব কণ্ঠ (${aiVoiceModels.size})",
                                                "Female" to "♀ ফিমেল (${aiVoiceModels.count { it.gender == "Female" }})",
                                                "Male" to "♂ মেল (${aiVoiceModels.count { it.gender == "Male" }})",
                                                "Special" to "✨ স্পেশাল (${aiVoiceModels.count { it.gender == "Special" }})"
                                            )
                                            filterTabs.forEach { (key, label) ->
                                                val isSelected = selectedVoiceGenderFilter == key
                                                val tabColor = when (key) {
                                                    "Female" -> Color(0xFFEC4899)
                                                    "Male" -> Color(0xFF3B82F6)
                                                    "Special" -> Color(0xFF8B5CF6)
                                                    else -> MaterialTheme.colorScheme.primary
                                                }
                                                Surface(
                                                    onClick = { selectedVoiceGenderFilter = key },
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = if (isSelected) tabColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                                    border = BorderStroke(1.dp, if (isSelected) tabColor else Color.Transparent),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Box(
                                                        modifier = Modifier.padding(vertical = 6.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = label,
                                                            fontSize = 10.sp,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                            color = if (isSelected) tabColor else MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // Active Selected Voice Card Details
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = when (currentVoiceModel.gender) {
                                                "Female" -> Color(0xFFEC4899).copy(alpha = 0.12f)
                                                "Male" -> Color(0xFF3B82F6).copy(alpha = 0.12f)
                                                else -> Color(0xFF8B5CF6).copy(alpha = 0.12f)
                                            },
                                            border = BorderStroke(
                                                1.dp,
                                                when (currentVoiceModel.gender) {
                                                    "Female" -> Color(0xFFEC4899).copy(alpha = 0.35f)
                                                    "Male" -> Color(0xFF3B82F6).copy(alpha = 0.35f)
                                                    else -> Color(0xFF8B5CF6).copy(alpha = 0.35f)
                                                }
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(42.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            when (currentVoiceModel.gender) {
                                                                "Female" -> Color(0xFFEC4899).copy(alpha = 0.25f)
                                                                "Male" -> Color(0xFF3B82F6).copy(alpha = 0.25f)
                                                                else -> Color(0xFF8B5CF6).copy(alpha = 0.25f)
                                                            }
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = currentVoiceModel.genderIcon,
                                                        fontSize = 20.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = when (currentVoiceModel.gender) {
                                                            "Female" -> Color(0xFFEC4899)
                                                            "Male" -> Color(0xFF3B82F6)
                                                            else -> Color(0xFF8B5CF6)
                                                        }
                                                    )
                                                }

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Text(
                                                            text = currentVoiceModel.name,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 13.sp,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = when (currentVoiceModel.gender) {
                                                                "Female" -> Color(0xFFEC4899).copy(alpha = 0.2f)
                                                                "Male" -> Color(0xFF3B82F6).copy(alpha = 0.2f)
                                                                else -> Color(0xFF8B5CF6).copy(alpha = 0.2f)
                                                            }
                                                        ) {
                                                            Text(
                                                                text = if (currentVoiceModel.gender == "Female") "ফিমেল কণ্ঠ" else if (currentVoiceModel.gender == "Male") "মেল কণ্ঠ" else "স্পেশাল",
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = when (currentVoiceModel.gender) {
                                                                    "Female" -> Color(0xFFEC4899)
                                                                    "Male" -> Color(0xFF3B82F6)
                                                                    else -> Color(0xFF8B5CF6)
                                                                },
                                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                    Text(
                                                        text = "${currentVoiceModel.vocalStyle} • ${currentVoiceModel.languageRegion}",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Text(
                                                        text = currentVoiceModel.description,
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }

                                        // Lead Singer Voice Chips
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "মূল গায়ক (Lead Vocalist):",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp
                                            )
                                            TextButton(
                                                onClick = { showVoiceCloningDialog = true },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Icon(Icons.Default.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("নিজের কণ্ঠ ক্লোন করুন", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            customClonedVoices.forEach { clonedName ->
                                                val isSelected = selectedVoice == clonedName
                                                item {
                                                    FilterChip(
                                                        selected = isSelected,
                                                        onClick = { selectedVoice = clonedName },
                                                        leadingIcon = {
                                                            Text("🎤", fontSize = 12.sp)
                                                        },
                                                        label = {
                                                            Text(clonedName, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                        },
                                                        colors = FilterChipDefaults.filterChipColors(
                                                            selectedContainerColor = Color(0xFF10B981),
                                                            selectedLabelColor = Color.White
                                                        )
                                                    )
                                                }
                                            }
                                            items(filteredVoices) { vm ->
                                                val isSelected = selectedVoice == vm.name
                                                val chipColor = when (vm.gender) {
                                                    "Female" -> Color(0xFFEC4899)
                                                    "Male" -> Color(0xFF3B82F6)
                                                    else -> Color(0xFF8B5CF6)
                                                }
                                                FilterChip(
                                                    selected = isSelected,
                                                    onClick = { selectedVoice = vm.name },
                                                    leadingIcon = {
                                                        Text(vm.genderIcon, fontSize = 12.sp, color = if (isSelected) Color.White else chipColor)
                                                    },
                                                    label = {
                                                        Text(vm.name, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                                    },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = chipColor,
                                                        selectedLabelColor = Color.White
                                                    )
                                                )
                                            }
                                        }

                                        // Duet Partner Section
                                        if (isDuetModeEnabled) {
                                            val duetModel = aiVoiceModels.firstOrNull { it.name == duetSecondVoice } ?: aiVoiceModels[1]
                                            val filteredDuetVoices = when (selectedDuetGenderFilter) {
                                                "Female" -> aiVoiceModels.filter { it.gender == "Female" }
                                                "Male" -> aiVoiceModels.filter { it.gender == "Male" }
                                                "Special" -> aiVoiceModels.filter { it.gender == "Special" }
                                                else -> aiVoiceModels
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = "দ্বৈত পার্টনার (Duet Partner): ${duetModel.genderIcon} ${duetModel.name}",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp,
                                                            color = MaterialTheme.colorScheme.secondary
                                                        )
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                                        ) {
                                                            Text(
                                                                text = "${currentVoiceModel.genderIcon} + ${duetModel.genderIcon} ডুয়েট হারমনি",
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = MaterialTheme.colorScheme.secondary,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }

                                                    // Duet gender filter
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        listOf(
                                                            "All" to "সব কণ্ঠ",
                                                            "Female" to "♀ ফিমেল",
                                                            "Male" to "♂ মেল",
                                                            "Special" to "✨ স্পেশাল"
                                                        ).forEach { (k, lbl) ->
                                                            val isSel = selectedDuetGenderFilter == k
                                                            FilterChip(
                                                                selected = isSel,
                                                                onClick = { selectedDuetGenderFilter = k },
                                                                label = { Text(lbl, fontSize = 10.sp) },
                                                                modifier = Modifier.height(28.dp)
                                                            )
                                                        }
                                                    }

                                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        items(filteredDuetVoices) { vm ->
                                                            val isSel = duetSecondVoice == vm.name
                                                            FilterChip(
                                                                selected = isSel,
                                                                onClick = { duetSecondVoice = vm.name },
                                                                leadingIcon = { Text(vm.genderIcon, fontSize = 11.sp) },
                                                                label = { Text(vm.name, fontSize = 11.sp) }
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Vocal Emotion Mood & Speed Tuning
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("ভোকাল এক্সপ্রেশন ও গতি:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                            Text("${(voiceSpeed * 100).toInt()}% স্পিড", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        }

                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            val emotionOptions = listOf(
                                                "Energetic" to "🔥 প্রাণবন্ত",
                                                "Emotional" to "😢 আবেগঘন",
                                                "Romantic" to "💖 রোমান্টিক",
                                                "Calm" to "🌿 শান্ত",
                                                "Sufi Soul" to "🕊️ সুফি ভাব",
                                                "Aggressive" to "⚡ এনার্জেটিক"
                                            )
                                            items(emotionOptions) { (key, label) ->
                                                val isSel = voiceMood == key
                                                FilterChip(
                                                    selected = isSel,
                                                    onClick = { voiceMood = key },
                                                    label = { Text(label, fontSize = 11.sp) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("SurSun v4 & Suno Engine", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                Text("Native AI Music & Voice Generation", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("External Suno API", style = MaterialTheme.typography.bodySmall)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Switch(
                                                    checked = useExternalSunoApi,
                                                    onCheckedChange = { useExternalSunoApi = it },
                                                    modifier = Modifier.height(24.dp)
                                                )
                                            }
                                        }
                                        if (useExternalSunoApi) {
                                            OutlinedTextField(
                                                value = externalSunoApiKey,
                                                onValueChange = { externalSunoApiKey = it },
                                                label = { Text("Suno AI API Key (Optional)") },
                                                placeholder = { Text("sk-suno-...") },
                                                singleLine = true,
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        } else {
                                            Text(
                                                text = "🟢 SurSun v4 Native AI Engine active. Generates studio-grade vocals, melodies & 320kbps masters directly inside Sur AI Music.",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                OutlinedTextField(
                                    value = customLyrics,
                                    onValueChange = { customLyrics = it },
                                    label = { Text("Studio Lyrics Editor ($selectedLanguage)") },
                                    placeholder = { Text("[Verse 1]\nType or generate lyrics in the Lyrics Studio tab...") },
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                )
                            }

                            // AI Auto-Tune & Vocal Beautifier Launcher Card
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showVocalTunerDialog = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.AutoAwesome,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onTertiary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "বেসুরো কন্ঠ শ্রুতিমধুর করার জাদুকরী টুল (AI Auto-Tune)",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                            Text(
                                                text = "আপনার নিজের গাওয়া বেসুরো কণ্ঠ অটো-টিউন ও রিভার্ব দিয়ে সুরেল করুন ✨",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                }
                            }

                            item {
                                if (isGeneratingSong) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(20.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                            Column {
                                                Text("Suno v4 Generation Active", fontWeight = FontWeight.Bold)
                                                Text(generationStepText, style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                isGeneratingSong = true
                                                generationStepText = "1/4: Structuring chords & harmonic arrangement..."
                                                delay(500)
                                                generationStepText = "2/4: Synthesizing $selectedVoice vocal melody..."
                                                delay(500)
                                                generationStepText = "3/4: Rendering $primaryGenre instrumentation & beats..."
                                                delay(500)
                                                generationStepText = "4/4: Mastering real 44.1kHz audio stream..."
                                                delay(400)
                                                val genreFinal = if (isDualGenreEnabled) "$primaryGenre + $secondaryGenre" else primaryGenre
                                                
                                                if (onGeneratePreviewSong != null) {
                                                    val created = onGeneratePreviewSong(prompt, genreFinal, selectedVibe, customLyrics)
                                                    isGeneratingSong = false
                                                    if (created != null) {
                                                        previewSong = created
                                                        Toast.makeText(context, "গান সফলভাবে তৈরি হয়েছে! প্রিভিউ শুনুন।", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        Toast.makeText(context, "গান তৈরিতে সমস্যা হয়েছে, অনুগ্রহ করে আবার চেষ্টা করুন।", Toast.LENGTH_SHORT).show()
                                                    }
                                                } else {
                                                    isGeneratingSong = false
                                                    onGenerateSong(prompt, genreFinal, selectedVibe, customLyrics)
                                                    Toast.makeText(context, "AI Song Generated & Added to Player!", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Generate Full AI Song (গান তৈরি করুন)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            item {
                                OutlinedCard(
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("MP3 Download (320kbps Lossless)", fontWeight = FontWeight.Bold)
                                            Text("Save HQ file directly to Storage", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Button(
                                            onClick = {
                                                Toast.makeText(context, "Downloading 320kbps MP3 track...", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Download MP3")
                                        }
                                    }
                                }
                            }

                            item { Spacer(modifier = Modifier.height(40.dp)) }
                        }
                    }

                    1 -> {
                        // TAB 1: LYRICS STUDIO
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Text("AI Lyrics Studio & History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            }

                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text("AI Generator & Whisper Transcribe", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    scope.launch {
                                                        val generated = onGenerateLyrics(prompt, selectedLanguage, primaryGenre, selectedVibe)
                                                        customLyrics = generated
                                                        Toast.makeText(context, "AI Lyrics Generated via GPT-4o!", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("GPT-4o Lyrics")
                                            }

                                            FilledTonalButton(
                                                onClick = { showRhymeEngineDialog = true },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.filledTonalButtonColors(
                                                    containerColor = Color(0xFF10B981).copy(alpha = 0.2f),
                                                    contentColor = Color(0xFF10B981)
                                                )
                                            ) {
                                                Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("অন্ত্যমিল ও ছন্দ")
                                            }
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                scope.launch {
                                                    isTranscribingAudio = true
                                                    delay(800)
                                                    customLyrics = "[Whisper Audio to Lyrics]\nTranscribed audio from mic."
                                                    isTranscribingAudio = false
                                                    Toast.makeText(context, "Whisper Audio Transcribed!", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isTranscribingAudio) "Transcribing..." else "Whisper Audio Transcribe")
                                        }
                                    }
                                }
                            }

                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Lyrics Editor", fontWeight = FontWeight.Bold)
                                            Button(
                                                onClick = {
                                                    scope.launch {
                                                        isCheckingCopyright = true
                                                        delay(500)
                                                        copyrightStatus = onCheckCopyright(customLyrics)
                                                        isCheckingCopyright = false
                                                    }
                                                },
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(if (isCheckingCopyright) "Checking..." else "Copyright Check")
                                            }
                                        }

                                        OutlinedTextField(
                                            value = customLyrics,
                                            onValueChange = { customLyrics = it },
                                            label = { Text("Lyrics Text ($selectedLanguage)") },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(180.dp),
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        if (copyrightStatus != null) {
                                            Text(
                                                text = if (copyrightStatus == true) "✅ Copyright Verified: 100% Original & Clean" else "⚠️ Flagged for Potential Copyright Similarity",
                                                color = if (copyrightStatus == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    val isClean = copyrightStatus ?: true
                                                    onSaveLyrics(prompt.ifBlank { "Untitled AI Lyrics" }, selectedLanguage, customLyrics, isClean)
                                                    Toast.makeText(context, "Saved Lyrics to Database!", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.Bookmark, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Save to 'lyrics_history' Room Database")
                                        }
                                    }
                                }
                            }

                            item {
                                Text("Saved History (${lyricsHistory.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }

                            items(lyricsHistory) { item ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(item.title, fontWeight = FontWeight.Bold)
                                            IconButton(onClick = { scope.launch { onDeleteLyrics(item.id) } }) {
                                                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                        Text(item.lyrics, style = MaterialTheme.typography.bodySmall, maxLines = 3)
                                        OutlinedButton(
                                            onClick = {
                                                customLyrics = item.lyrics
                                                selectedTab = 0
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Load into Studio Generator")
                                        }
                                    }
                                }
                            }

                            item { Spacer(modifier = Modifier.height(40.dp)) }
                        }
                    }

                    2 -> {
                        // TAB 2: ADVANCED AI TECH - ALL 10 FEATURES
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Text("Advanced AI Tech Features", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }

                            // 1. AI Composer Mode
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Default.AccountTree, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Text("1. AI Composer Mode (Structural Arrangement)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        }
                                        Text("Auto-composes dynamic structural section breakdown with instrument density allocations.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            composerSections.forEach { sec ->
                                                Surface(
                                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text("${sec.title} • ${sec.duration}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                            Text(sec.instruments, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                                        }
                                                        Column(horizontalAlignment = Alignment.End) {
                                                            Text("Tension ${(sec.tensionLevel * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                            LinearProgressIndicator(
                                                                progress = { sec.tensionLevel },
                                                                modifier = Modifier.width(60.dp).height(6.dp).clip(RoundedCornerShape(3.dp))
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 2. Mood Detector (from lyrics)
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                            Text("2. AI Lyric Mood & Key Detector", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        }

                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    isAnalyzingMood = true
                                                    delay(600)
                                                    moodAnalysis = onAnalyzeMood(customLyrics)
                                                    isAnalyzingMood = false
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(if (isAnalyzingMood) "Analyzing Text..." else "Analyze Lyric Mood & Detect Key")
                                        }

                                        moodAnalysis?.let { res ->
                                            Surface(
                                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Text("Mood: ${res.primaryMood} (${res.confidencePercent}% Confidence)", fontWeight = FontWeight.Bold)
                                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                                        Text("Suggested BPM: ${res.recommendedBpm}", fontSize = 13.sp)
                                                        Text("Key: ${res.recommendedKey}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 3. Voice to Instrument
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Default.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                            Text("3. Voice to Instrument Converter", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        }
                                        Text("Hum or sing a melody -> AI turns audio into saxophone, electric synth or violin.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                        Text("Target Synth Instrument: $targetInstrumentConversion", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            items(listOf("Saxophone", "Acoustic Violin", "Electric Guitar Solo", "808 Bassline", "Dotara", "Flute Lead")) { inst ->
                                                FilterChip(
                                                    selected = targetInstrumentConversion == inst,
                                                    onClick = { targetInstrumentConversion = inst },
                                                    label = { Text(inst, fontSize = 12.sp) }
                                                )
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    isConvertingVoiceToInst = true
                                                    delay(1000)
                                                    voiceConversionResultText = "Successfully converted hummed audio pitch curve into $targetInstrumentConversion track!"
                                                    isConvertingVoiceToInst = false
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                                        ) {
                                            Icon(if (isConvertingVoiceToInst) Icons.Default.GraphicEq else Icons.Default.Mic, contentDescription = null)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(if (isConvertingVoiceToInst) "Processing Audio..." else "Record Voice & Convert to $targetInstrumentConversion")
                                        }

                                        voiceConversionResultText?.let { text ->
                                            Text(text, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // 4. AI Band Mode
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Default.Groups, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Text("4. AI Virtual Band Mode", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        }
                                        Text("5 AI band members with distinct personalities and solo triggers.", style = MaterialTheme.typography.bodySmall)

                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            bandMembers.forEach { member ->
                                                OutlinedCard(
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text("${member.name} (${member.role})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                            Text(member.personality, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                        }
                                                        Button(
                                                            onClick = {
                                                                Toast.makeText(context, "${member.name} triggered for AI Solo Breakdown!", Toast.LENGTH_SHORT).show()
                                                            },
                                                            shape = RoundedCornerShape(8.dp)
                                                        ) {
                                                            Text("AI Solo", fontSize = 11.sp)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 5. Genre Transformer
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Default.Transform, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                            Text("5. AI Genre Transformer", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        }
                                        Text("Transform existing song/lyrics into a radically different musical style.", style = MaterialTheme.typography.bodySmall)

                                        Text("Target Genre: $targetTransformGenre", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            items(listOf("Synthwave Cyberpunk", "Baul Folk Fusion", "Heavy Metal", "Deep House", "Chill Lofi", "Classic Ghazal")) { g ->
                                                FilterChip(
                                                    selected = targetTransformGenre == g,
                                                    onClick = { targetTransformGenre = g },
                                                    label = { Text(g, fontSize = 12.sp) }
                                                )
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    isTransformingGenre = true
                                                    val res = onTransformGenre(customLyrics, targetTransformGenre)
                                                    customLyrics = res
                                                    isTransformingGenre = false
                                                    Toast.makeText(context, "Transformed song into $targetTransformGenre!", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(if (isTransformingGenre) "Transforming..." else "Transform Track to $targetTransformGenre")
                                        }
                                    }
                                }
                            }

                            // 6. Loop Station AI
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Default.Repeat, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Text("6. Loop Station AI (4-Track Live Looper)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        }

                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            loopTracks.forEach { track ->
                                                Surface(
                                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(track.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                                        IconButton(onClick = {
                                                            track.isPlaying = !track.isPlaying
                                                            Toast.makeText(context, "${track.name} ${if (track.isPlaying) "Playing" else "Muted"}", Toast.LENGTH_SHORT).show()
                                                        }) {
                                                            Icon(if (track.isPlaying) Icons.Default.PlayArrow else Icons.Default.Pause, contentDescription = null)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 7. AI Lyric Finisher
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Text("7. AI Lyric Finisher & Rhyme Matching", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Rhyme Scheme:", fontSize = 13.sp)
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                listOf("AABB", "ABAB", "AAAA", "Free Verse").forEach { scheme ->
                                                    FilterChip(
                                                        selected = selectedRhymeScheme == scheme,
                                                        onClick = { selectedRhymeScheme = scheme },
                                                        label = { Text(scheme, fontSize = 11.sp) }
                                                    )
                                                }
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    isFinishingLyrics = true
                                                    val completed = onAutoFinishLyrics(customLyrics, selectedRhymeScheme)
                                                    customLyrics = completed
                                                    isFinishingLyrics = false
                                                    Toast.makeText(context, "Lyrics auto-completed with $selectedRhymeScheme rhymes!", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(if (isFinishingLyrics) "Generating Verses..." else "Auto-Finish Lyrics Stanzas")
                                        }
                                    }
                                }
                            }

                            // 8. Emotion Graph (Canvas chart)
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Default.ShowChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Text("8. Dynamic Emotion Timeline Graph", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        }
                                        Text("Visual intensity & tension arc across song timeline (0:00 -> 3:15)", style = MaterialTheme.typography.bodySmall)

                                        val primaryColor = MaterialTheme.colorScheme.primary
                                        Canvas(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(120.dp)
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                                .padding(16.dp)
                                        ) {
                                            val width = size.width
                                            val height = size.height
                                            val points = timelinePoints

                                            val path = Path()
                                            points.forEachIndexed { index, tension ->
                                                val x = (width / (points.size - 1)) * index
                                                val y = height - ((tension / 100f) * height)
                                                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                                drawCircle(color = primaryColor, radius = 6f, center = Offset(x, y))
                                            }
                                            drawPath(path = path, color = primaryColor, style = Stroke(width = 4f))
                                        }
                                    }
                                }
                            }

                            // 9. BPM Sync & Tap Tempo
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Default.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Text("9. BPM Sync & Real-time Tap Tempo", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Tempo: $currentBpm BPM", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("Pitch Lock", style = MaterialTheme.typography.bodySmall)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Switch(
                                                    checked = isTimeStretchPitchLocked,
                                                    onCheckedChange = { isTimeStretchPitchLocked = it }
                                                )
                                            }
                                        }

                                        Slider(
                                            value = currentBpm.toFloat(),
                                            onValueChange = { currentBpm = it.toInt() },
                                            valueRange = 60f..180f
                                        )

                                        Button(
                                            onClick = {
                                                val now = System.currentTimeMillis()
                                                if (lastTapTime > 0L) {
                                                    val diff = now - lastTapTime
                                                    if (diff in 250..2000) {
                                                        currentBpm = (60000 / diff).toInt().coerceIn(60, 180)
                                                    }
                                                }
                                                lastTapTime = now
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.TouchApp, contentDescription = null)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("TAP TEMPO (Tap in rhythm)")
                                        }
                                    }
                                }
                            }

                            // 10. AI Vocal Coach (feedback)
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Text("10. AI Vocal Coach Performance Analyzer", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        }

                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    isRecordingVocalCoach = true
                                                    delay(1000)
                                                    vocalCoachData = onAnalyzeVocal()
                                                    isRecordingVocalCoach = false
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.Mic, contentDescription = null)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(if (isRecordingVocalCoach) "Analyzing Singing Pitch..." else "Record Vocal Test & Get AI Feedback")
                                        }

                                        vocalCoachData?.let { coach ->
                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Text("Pitch Accuracy: ${coach.pitchAccuracy}%", fontWeight = FontWeight.Bold)
                                                LinearProgressIndicator(progress = { coach.pitchAccuracy / 100f }, modifier = Modifier.fillMaxWidth())

                                                Text("Vibrato Control: ${coach.vibratoControl}%", fontWeight = FontWeight.Bold)
                                                LinearProgressIndicator(progress = { coach.vibratoControl / 100f }, modifier = Modifier.fillMaxWidth())

                                                Text("Breath Support: ${coach.breathControl}%", fontWeight = FontWeight.Bold)
                                                LinearProgressIndicator(progress = { coach.breathControl / 100f }, modifier = Modifier.fillMaxWidth())

                                                Text("AI Actionable Coaching Tips:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                coach.tips.forEach { tip ->
                                                    Text("• $tip", style = MaterialTheme.typography.bodySmall)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 11. AI Voice Auto-Tuner & Pitch Beautifier (বেসুরো কন্ঠ শ্রুতিমধুর করার জাদুকরী টুল)
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Text("11. AI Auto-Tune & Vocal Beautifier (বেসুরো কন্ঠ শ্রুতিমধুর)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        }
                                        Text("আপনার গাওয়া সাধারণ বা বেসুরো কণ্ঠকে অটো-টিউন, রাগা স্কেল কারেকশন ও স্টুডিও রিভার্ব দিয়ে মুহূর্তে মেলাডিয়াস করুন।", style = MaterialTheme.typography.bodySmall)

                                        Button(
                                            onClick = { showVocalTunerDialog = true },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("অটো-টিউন ও ভয়েস বিউটিফায়ার খুলুন ✨", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            item { Spacer(modifier = Modifier.height(40.dp)) }
                        }
                    }

                    3 -> {
                        // TAB 3: PRO TOOLKIT & STEMS
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Text("Pro AI Studio Toolkit & Stem Separation", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            }

                            // Stem Separation UI
                            item {
                                Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text("AI Stem Separator (4 Track Mixer)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                                        Column {
                                            Text("Vocals: ${(vocalStemVol * 100).toInt()}%", fontSize = 13.sp)
                                            Slider(value = vocalStemVol, onValueChange = { vocalStemVol = it })
                                        }
                                        Column {
                                            Text("Drums: ${(drumStemVol * 100).toInt()}%", fontSize = 13.sp)
                                            Slider(value = drumStemVol, onValueChange = { drumStemVol = it })
                                        }
                                        Column {
                                            Text("808 Bass: ${(bassStemVol * 100).toInt()}%", fontSize = 13.sp)
                                            Slider(value = bassStemVol, onValueChange = { bassStemVol = it })
                                        }
                                        Column {
                                            Text("Synths & Instruments: ${(synthStemVol * 100).toInt()}%", fontSize = 13.sp)
                                            Slider(value = synthStemVol, onValueChange = { synthStemVol = it })
                                        }
                                    }
                                }
                            }

                            item { Spacer(modifier = Modifier.height(40.dp)) }
                        }
                    }
                }
            }
        }
    }

    if (showVoiceCloningDialog) {
        VoiceCloningDialog(
            onDismiss = { showVoiceCloningDialog = false },
            onVoiceCloned = { clonedVoiceName ->
                val fullName = "🎤 $clonedVoiceName (AI Cloned)"
                if (!customClonedVoices.contains(fullName)) {
                    customClonedVoices = listOf(fullName) + customClonedVoices
                }
                selectedVoice = fullName
                Toast.makeText(context, "আপনার ভয়েস সফলভাবে ক্লোন হয়েছে এবং সিলেকশনে যোগ করা হয়েছে!", Toast.LENGTH_LONG).show()
            }
        )
    }

    if (showHummingDialog) {
        MelodyHummingDialog(
            isBangla = selectedLanguage == "Bengali",
            onDismiss = { showHummingDialog = false },
            onApplyPrompt = { detectedPrompt, detectedGenre, _ ->
                prompt = detectedPrompt
                primaryGenre = if (genres.contains(detectedGenre)) detectedGenre else "Folk"
                selectedVibe = "Soulful & Melodic"
            }
        )
    }

    if (showVocalTunerDialog) {
        AiVocalTunerDialog(
            onDismiss = { showVocalTunerDialog = false },
            onApplyTunedVocal = { vocalCharacter, scale ->
                selectedVoice = "Auto-Tuned Voice ($vocalCharacter)"
                prompt = if (prompt.isBlank()) "Auto-Tuned Melodious $scale Vocal Track" else "$prompt (Auto-Tuned $scale)"
                Toast.makeText(context, "অটো-টিউন করা কন্ঠ এআই গানে যুক্ত করা হয়েছে!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showAiCoverArtDialog) {
        AiAlbumArtGeneratorDialog(
            songTitle = prompt.ifBlank { "New AI Hit" },
            genre = primaryGenre,
            onDismiss = { showAiCoverArtDialog = false },
            onArtApplied = { newArtUrl ->
                customCoverArtUrl = newArtUrl
            }
        )
    }

    if (showBeatPadDialog) {
        VirtualInstrumentBeatPadDialog(
            onDismiss = { showBeatPadDialog = false },
            onSendPromptToAiSong = { beatPrompt, beatGenre ->
                prompt = beatPrompt
                primaryGenre = if (genres.contains(beatGenre)) beatGenre else "Folk"
            }
        )
    }

    if (showRhymeEngineDialog) {
        BanglaLyricsRhymeEngineDialog(
            onDismiss = { showRhymeEngineDialog = false },
            onApplyLyrics = { generatedLyrics ->
                customLyrics = generatedLyrics
            }
        )
    }

    if (showHummingSargamDialog) {
        HummingToSargamTranscriptionDialog(
            onDismiss = { showHummingSargamDialog = false },
            onApplyMelodyPrompt = { melodyPrompt ->
                prompt = melodyPrompt
            }
        )
    }

    val dummyStudioSong = SongEntity(
        id = 999,
        title = if (prompt.isNotBlank()) prompt else "নতুন স্টুডিও প্রজেক্ট",
        artist = "Sur AI Creator",
        genre = primaryGenre,
        audioUrl = "",
        imageUrl = customCoverArtUrl ?: "",
        lyrics = customLyrics,
        duration = "3:00",
        isFavorite = false,
        isGenerated = true
    )

    if (showLiveKaraokeDialog) {
        LiveKaraokeVocalStudioDialog(
            song = dummyStudioSong,
            onDismiss = { showLiveKaraokeDialog = false }
        )
    }

    if (showMultiTrackDawDialog) {
        MultiTrackDawTimelineDialog(
            song = dummyStudioSong,
            onDismiss = { showMultiTrackDawDialog = false }
        )
    }

    if (showGuitarChordsDialog) {
        GuitarChordsVisualizerDialog(
            song = dummyStudioSong,
            onDismiss = { showGuitarChordsDialog = false }
        )
    }

    if (showTanpuraRiyazDialog) {
        RiyazTanpuraStudioDialog(
            song = dummyStudioSong,
            onDismiss = { showTanpuraRiyazDialog = false }
        )
    }

    if (showMasteringEqDialog) {
        AudioMasteringEqVisualizerDialog(
            song = dummyStudioSong,
            onDismiss = { showMasteringEqDialog = false }
        )
    }

    if (showLyricistNotepadDialog) {
        BengaliLyricistNotepadDialog(
            initialLyrics = customLyrics,
            onDismiss = { showLyricistNotepadDialog = false },
            onUseLyrics = { lyricsText ->
                customLyrics = lyricsText
            }
        )
    }

    // Instant Audio Preview Dialog after Generation
    previewSong?.let { created ->
        SongCreationPreviewDialog(
            song = created,
            isPlaying = isPlaying,
            playbackProgress = playbackProgress,
            durationSeconds = playbackDurationSeconds,
            isBuffering = isBuffering,
            onPlayPause = onPlayPause,
            onSeek = onSeek,
            onSaveAndOpen = {
                previewSong = null
                onNavigateToHome()
            },
            onRegenerate = {
                previewSong = null
            },
            onDismiss = {
                previewSong = null
            }
        )
    }
}
