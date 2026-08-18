package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.BottomNavBar
import com.example.ui.components.GlobalErrorBanner
import com.example.ui.components.LeftDrawerContent
import com.example.ui.components.MiniPlayer
import com.example.ui.screens.*
import com.example.ui.theme.SurMusicTheme
import com.example.ui.theme.ThemeColorPreset
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val themeColorName by viewModel.themeColor.collectAsStateWithLifecycle()

            val themePreset = remember(themeColorName) {
                ThemeColorPreset.values().find { it.name == themeColorName } ?: ThemeColorPreset.NeonPurple
            }

            SurMusicTheme(
                themeMode = themeMode,
                themeColorPreset = themePreset
            ) {
                SurMusicApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurMusicApp(viewModel: MainViewModel) {
    var currentScreen by rememberSaveable { mutableStateOf("splash") }
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val songs by viewModel.allSongs.collectAsStateWithLifecycle()
    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val userRole by viewModel.userRole.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val realUsersList by viewModel.allRegisteredUsers.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val themeColor by viewModel.themeColor.collectAsStateWithLifecycle()
    val tokenBalance by viewModel.tokenBalance.collectAsStateWithLifecycle()
    val activeSubscription by viewModel.activeSubscription.collectAsStateWithLifecycle()
    val plans by viewModel.allPlans.collectAsStateWithLifecycle()
    val lyricsHistory by viewModel.allLyricsHistory.collectAsStateWithLifecycle()
    val clonedVoices by viewModel.allClonedVoices.collectAsStateWithLifecycle()

    // Settings & Toggle states
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val autoPlay by viewModel.autoPlay.collectAsStateWithLifecycle()
    val hqAudio by viewModel.hqAudio.collectAsStateWithLifecycle()
    val studioFx by viewModel.studioFx.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()

    // Global Error & Network State
    val globalError by viewModel.globalError.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val isFetchingSupabaseFeed by viewModel.isFetchingSupabaseFeed.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Streaming Audio Playback & Progress state
    val playbackProgress by viewModel.playbackProgress.collectAsStateWithLifecycle()
    val playbackDurationSeconds by viewModel.playbackDurationSeconds.collectAsStateWithLifecycle()
    val isAudioBuffering by viewModel.isAudioBuffering.collectAsStateWithLifecycle()
    val bufferedProgress by viewModel.bufferedProgress.collectAsStateWithLifecycle()

    var showFullscreenPlayer by rememberSaveable { mutableStateOf(false) }
    var showTokenDialog by rememberSaveable { mutableStateOf(false) }
    val showGoProDialog by viewModel.showGoProDialog.collectAsStateWithLifecycle()

    val activePlanTitle = activeSubscription?.planId?.uppercase() ?: "FREE PLAN"

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn && (currentScreen == "auth" || currentScreen == "onboarding")) {
            currentScreen = "home"
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (currentScreen) {
                "splash" -> {
                    SplashScreen(onFinished = {
                        currentScreen = "onboarding"
                    })
                }
                "onboarding" -> {
                    OnboardingScreen(onCompleted = {
                        currentScreen = if (isLoggedIn) "home" else "auth"
                    })
                }
                "auth" -> {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    AuthScreen(
                        onLogin = { email, pass ->
                            val res = viewModel.loginWithCredentials(email, pass)
                            if (res is com.example.data.repository.AuthResult.Error) {
                                viewModel.showGlobalError(
                                    title = if (appLanguage == "bn") "লগইন ব্যর্থ হয়েছে" else "Authentication Failed",
                                    message = res.message,
                                    category = com.example.ui.components.ErrorCategory.AUTHENTICATION,
                                    severity = com.example.ui.components.ErrorSeverity.ERROR,
                                    autoDismissMillis = 6000L
                                )
                            }
                            res
                        },
                        onRegister = { email, pass, name, role ->
                            val res = viewModel.registerUser(email, pass, name, role)
                            if (res is com.example.data.repository.AuthResult.Error) {
                                viewModel.showGlobalError(
                                    title = if (appLanguage == "bn") "রেজিস্ট্রেশন ব্যর্থ হয়েছে" else "Registration Failed",
                                    message = res.message,
                                    category = com.example.ui.components.ErrorCategory.AUTHENTICATION,
                                    severity = com.example.ui.components.ErrorSeverity.ERROR,
                                    autoDismissMillis = 6000L
                                )
                            }
                            res
                        },
                        onGoogleSignIn = {
                            val res = viewModel.loginWithGoogle(context)
                            if (res is com.example.data.repository.AuthResult.Error) {
                                viewModel.showGlobalError(
                                    title = if (appLanguage == "bn") "গুগল লগইন ব্যর্থ হয়েছে" else "Google Sign-In Failed",
                                    message = res.message,
                                    category = com.example.ui.components.ErrorCategory.AUTHENTICATION,
                                    severity = com.example.ui.components.ErrorSeverity.ERROR,
                                    autoDismissMillis = 6000L
                                )
                            }
                            res
                        },
                        onGuestMode = {
                            viewModel.loginGuest()
                            currentScreen = "home"
                        }
                    )
                }
                "pricing" -> {
                    PricingScreen(
                        plans = plans,
                        activeSubscription = activeSubscription,
                        onSubscribe = { planId, billingCycle ->
                            viewModel.subscribeToPlan(planId, billingCycle)
                            currentScreen = "profile"
                        },
                        onBack = { currentScreen = "profile" }
                    )
                }
                else -> {
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            LeftDrawerContent(
                                currentRoute = currentScreen,
                                appLanguage = appLanguage,
                                userEmail = userEmail,
                                userRole = userRole,
                                tokenBalance = tokenBalance,
                                activePlanName = activePlanTitle,
                                onNavigate = { route -> currentScreen = route },
                                onCloseDrawer = { scope.launch { drawerState.close() } },
                                onLanguageToggle = {
                                    val nextLang = if (appLanguage == "bn") "en" else "bn"
                                    viewModel.setAppLanguage(nextLang)
                                }
                            )
                        }
                    ) {
                        Scaffold(
                            contentWindowInsets = WindowInsets.systemBars,
                            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                            topBar = {
                            TopAppBar(
                                title = {
                                    val isBangla = appLanguage == "bn"
                                    val titleText = when (currentScreen) {
                                        "home" -> if (isBangla) "সুর এআই হোম" else "Sur AI Home"
                                        "downloads" -> if (isBangla) "ডাউনলোড ও অফলাইন সেন্টার" else "Downloads & Offline Center"
                                        "create", "create_gen" -> if (isBangla) "এআই গান জেনারেটর" else "AI Studio Generator"
                                        "create_lyrics" -> if (isBangla) "লিরিক্স স্টুডিও" else "Lyrics Studio"
                                        "create_adv" -> if (isBangla) "এডভান্সড এআই ১০ ফিচার" else "Advanced AI Tech"
                                        "create_stems" -> if (isBangla) "প্রো স্টেম ও ডিজে মিক্সার" else "Pro Stems & Mixer"
                                        "voice_correction" -> if (isBangla) "ভয়েস কারেকশন ও টিউনিং" else "Voice Correction & Tuning"
                                        "video_visual" -> if (isBangla) "ভিডিও ও ভিজ্যুয়াল স্টুডিও" else "Video & Visual Studio"
                                        "feed" -> if (isBangla) "মিউজিক ফিড" else "Music Feed"
                                        "marketplace" -> if (isBangla) "এআই মার্কেটপ্লেস" else "AI Marketplace"
                                        "profile" -> if (isBangla) "মাই প্রোফাইল" else "User Profile"
                                        "settings" -> if (isBangla) "ইউজার সেটিংস ও টগল" else "User Settings"
                                        else -> "Sur AI Studio"
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = titleText,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                },
                                navigationIcon = {
                                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                        Icon(
                                            imageVector = Icons.Default.Menu,
                                            contentDescription = "Open Left Menu Bar",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                actions = {
                                    IconButton(onClick = {
                                        val nextTheme = when (themeMode) {
                                            "dark" -> "amoled"
                                            "amoled" -> "light"
                                            else -> "dark"
                                        }
                                        viewModel.setThemeMode(nextTheme)
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Theme: $themeMode",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(onClick = { currentScreen = "settings" }) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "Settings",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        },
                        bottomBar = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .navigationBarsPadding()
                            ) {
                                MiniPlayer(
                                    song = currentSong,
                                    isPlaying = isPlaying,
                                    playbackProgress = playbackProgress,
                                    durationSeconds = playbackDurationSeconds,
                                    isBuffering = isAudioBuffering,
                                    bufferedProgress = bufferedProgress,
                                    onPlayPauseClick = { viewModel.togglePlayPause() },
                                    onSeekChange = { newProgress -> viewModel.seekToProgress(newProgress) },
                                    onFavoriteClick = { currentSong?.let { viewModel.toggleFavorite(it) } },
                                    onOpenPlayer = { showFullscreenPlayer = true },
                                    onSkipNext = { viewModel.skipToNext() },
                                    onSkipPrevious = { viewModel.skipToPrevious() },
                                    onClose = { viewModel.closeMiniPlayer() }
                                )
                                BottomNavBar(
                                    currentRoute = when (currentScreen) {
                                        "create_gen", "create_lyrics", "create_adv", "create_stems" -> "create"
                                        else -> currentScreen
                                    },
                                    onNavigate = { route -> currentScreen = route }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentScreen) {
                                "home" -> HomeScreen(
                                    songs = songs,
                                    tokenBalance = tokenBalance,
                                    appLanguage = appLanguage,
                                    isOnline = isOnline,
                                    isFetchingSupabase = isFetchingSupabaseFeed,
                                    onRefreshFeed = { viewModel.refreshSupabaseFeed() },
                                    currentUserArtistName = currentUser?.fullName ?: (userEmail?.substringBefore("@") ?: "Sur AI Artist"),
                                    viewModel = viewModel,
                                    onSongClick = { song -> viewModel.playSong(song) },
                                    onFavoriteClick = { song -> viewModel.toggleFavorite(song) },
                                    onNavigateCreate = { currentScreen = "create_gen" },
                                    onOpenTokenPacks = { showTokenDialog = true },
                                    onRewardClaimed = { tokens, reason ->
                                        viewModel.claimDailyReward(tokens, reason)
                                    },
                                    onPaymentSuccess = { tokens, cost, trxId ->
                                        viewModel.purchaseTokens(tokens, cost)
                                    },
                                    onUploadTrack = { title, artist, genre, prompt, duration, fileName, bytes, coverUrl, isPublic ->
                                        viewModel.uploadAudioToSupabase(
                                            title = title,
                                            artist = artist,
                                            genre = genre,
                                            prompt = prompt,
                                            duration = duration,
                                            fileName = fileName,
                                            audioBytes = bytes,
                                            imageUrl = coverUrl,
                                            isPublic = isPublic
                                        )
                                    },
                                    onPostgrestSearch = { query ->
                                        viewModel.searchSongsViaPostgrest(query)
                                    }
                                )
                                "downloads" -> DownloadsScreen(
                                    appLanguage = appLanguage,
                                    allSongs = songs,
                                    onPlaySong = { song -> viewModel.playSong(song) }
                                )
                                "create", "create_gen" -> CreateSongScreen(
                                    lyricsHistory = lyricsHistory,
                                    clonedVoices = clonedVoices,
                                    onGenerateSong = { prompt, genre, vibe, lyrics ->
                                        viewModel.generateAiSong(prompt, genre, vibe, lyrics)
                                        currentScreen = "home"
                                    },
                                    onGenerateLyrics = { prompt, lang, genre, vibe ->
                                        viewModel.generateAiLyrics(prompt, lang, genre, vibe)
                                    },
                                    onCheckCopyright = { lyrics -> viewModel.checkCopyright(lyrics) },
                                    onSaveLyrics = { title, lang, lyrics, isClean ->
                                        viewModel.saveLyricsHistory(title, lang, lyrics, isClean)
                                    },
                                    onDeleteLyrics = { id ->
                                        viewModel.deleteLyricsHistory(id)
                                        id
                                    },
                                    onSaveVoice = { name, desc -> viewModel.saveClonedVoice(name, desc) },
                                    onDeleteVoice = { id -> viewModel.deleteClonedVoice(id) },
                                    getChords = { genre -> viewModel.getChordProgressions(genre) },
                                    getBeats = { genre -> viewModel.getBeatSuggestions(genre) },
                                    getMelody = { key, scale -> viewModel.generateMelodyNotes(key, scale) },
                                    onAnalyzeMood = { lyrics -> viewModel.analyzeLyricsMood(lyrics) },
                                    onAutoFinishLyrics = { lyrics, scheme -> viewModel.autoFinishLyrics(lyrics, scheme) },
                                    onTransformGenre = { lyrics, genre -> viewModel.transformLyricsGenre(lyrics, genre) },
                                    onAnalyzeVocal = { viewModel.analyzeVocalPerformance() },
                                    initialTab = 0,
                                    onGeneratePreviewSong = { prompt, genre, vibe, lyrics ->
                                        viewModel.generateSongForPreview(prompt, genre, vibe, lyrics)
                                    },
                                    isPlaying = isPlaying,
                                    playbackProgress = playbackProgress,
                                    playbackDurationSeconds = playbackDurationSeconds,
                                    isBuffering = isAudioBuffering,
                                    onPlayPause = { viewModel.togglePlayPause() },
                                    onSeek = { pos -> viewModel.seekToProgress(pos) },
                                    onNavigateToHome = { currentScreen = "home" }
                                )
                                "create_lyrics" -> CreateSongScreen(
                                    lyricsHistory = lyricsHistory,
                                    clonedVoices = clonedVoices,
                                    onGenerateSong = { prompt, genre, vibe, lyrics ->
                                        viewModel.generateAiSong(prompt, genre, vibe, lyrics)
                                        currentScreen = "home"
                                    },
                                    onGenerateLyrics = { prompt, lang, genre, vibe ->
                                        viewModel.generateAiLyrics(prompt, lang, genre, vibe)
                                    },
                                    onCheckCopyright = { lyrics -> viewModel.checkCopyright(lyrics) },
                                    onSaveLyrics = { title, lang, lyrics, isClean ->
                                        viewModel.saveLyricsHistory(title, lang, lyrics, isClean)
                                    },
                                    onDeleteLyrics = { id ->
                                        viewModel.deleteLyricsHistory(id)
                                        id
                                    },
                                    onSaveVoice = { name, desc -> viewModel.saveClonedVoice(name, desc) },
                                    onDeleteVoice = { id -> viewModel.deleteClonedVoice(id) },
                                    getChords = { genre -> viewModel.getChordProgressions(genre) },
                                    getBeats = { genre -> viewModel.getBeatSuggestions(genre) },
                                    getMelody = { key, scale -> viewModel.generateMelodyNotes(key, scale) },
                                    onAnalyzeMood = { lyrics -> viewModel.analyzeLyricsMood(lyrics) },
                                    onAutoFinishLyrics = { lyrics, scheme -> viewModel.autoFinishLyrics(lyrics, scheme) },
                                    onTransformGenre = { lyrics, genre -> viewModel.transformLyricsGenre(lyrics, genre) },
                                    onAnalyzeVocal = { viewModel.analyzeVocalPerformance() },
                                    initialTab = 1,
                                    onGeneratePreviewSong = { prompt, genre, vibe, lyrics ->
                                        viewModel.generateSongForPreview(prompt, genre, vibe, lyrics)
                                    },
                                    isPlaying = isPlaying,
                                    playbackProgress = playbackProgress,
                                    playbackDurationSeconds = playbackDurationSeconds,
                                    isBuffering = isAudioBuffering,
                                    onPlayPause = { viewModel.togglePlayPause() },
                                    onSeek = { pos -> viewModel.seekToProgress(pos) },
                                    onNavigateToHome = { currentScreen = "home" }
                                )
                                "create_adv" -> CreateSongScreen(
                                    lyricsHistory = lyricsHistory,
                                    clonedVoices = clonedVoices,
                                    onGenerateSong = { prompt, genre, vibe, lyrics ->
                                        viewModel.generateAiSong(prompt, genre, vibe, lyrics)
                                        currentScreen = "home"
                                    },
                                    onGenerateLyrics = { prompt, lang, genre, vibe ->
                                        viewModel.generateAiLyrics(prompt, lang, genre, vibe)
                                    },
                                    onCheckCopyright = { lyrics -> viewModel.checkCopyright(lyrics) },
                                    onSaveLyrics = { title, lang, lyrics, isClean ->
                                        viewModel.saveLyricsHistory(title, lang, lyrics, isClean)
                                    },
                                    onDeleteLyrics = { id ->
                                        viewModel.deleteLyricsHistory(id)
                                        id
                                    },
                                    onSaveVoice = { name, desc -> viewModel.saveClonedVoice(name, desc) },
                                    onDeleteVoice = { id -> viewModel.deleteClonedVoice(id) },
                                    getChords = { genre -> viewModel.getChordProgressions(genre) },
                                    getBeats = { genre -> viewModel.getBeatSuggestions(genre) },
                                    getMelody = { key, scale -> viewModel.generateMelodyNotes(key, scale) },
                                    onAnalyzeMood = { lyrics -> viewModel.analyzeLyricsMood(lyrics) },
                                    onAutoFinishLyrics = { lyrics, scheme -> viewModel.autoFinishLyrics(lyrics, scheme) },
                                    onTransformGenre = { lyrics, genre -> viewModel.transformLyricsGenre(lyrics, genre) },
                                    onAnalyzeVocal = { viewModel.analyzeVocalPerformance() },
                                    initialTab = 2,
                                    onGeneratePreviewSong = { prompt, genre, vibe, lyrics ->
                                        viewModel.generateSongForPreview(prompt, genre, vibe, lyrics)
                                    },
                                    isPlaying = isPlaying,
                                    playbackProgress = playbackProgress,
                                    playbackDurationSeconds = playbackDurationSeconds,
                                    isBuffering = isAudioBuffering,
                                    onPlayPause = { viewModel.togglePlayPause() },
                                    onSeek = { pos -> viewModel.seekToProgress(pos) },
                                    onNavigateToHome = { currentScreen = "home" }
                                )
                                "create_stems" -> CreateSongScreen(
                                    lyricsHistory = lyricsHistory,
                                    clonedVoices = clonedVoices,
                                    onGenerateSong = { prompt, genre, vibe, lyrics ->
                                        viewModel.generateAiSong(prompt, genre, vibe, lyrics)
                                        currentScreen = "home"
                                    },
                                    onGenerateLyrics = { prompt, lang, genre, vibe ->
                                        viewModel.generateAiLyrics(prompt, lang, genre, vibe)
                                    },
                                    onCheckCopyright = { lyrics -> viewModel.checkCopyright(lyrics) },
                                    onSaveLyrics = { title, lang, lyrics, isClean ->
                                        viewModel.saveLyricsHistory(title, lang, lyrics, isClean)
                                    },
                                    onDeleteLyrics = { id ->
                                        viewModel.deleteLyricsHistory(id)
                                        id
                                    },
                                    onSaveVoice = { name, desc -> viewModel.saveClonedVoice(name, desc) },
                                    onDeleteVoice = { id -> viewModel.deleteClonedVoice(id) },
                                    getChords = { genre -> viewModel.getChordProgressions(genre) },
                                    getBeats = { genre -> viewModel.getBeatSuggestions(genre) },
                                    getMelody = { key, scale -> viewModel.generateMelodyNotes(key, scale) },
                                    onAnalyzeMood = { lyrics -> viewModel.analyzeLyricsMood(lyrics) },
                                    onAutoFinishLyrics = { lyrics, scheme -> viewModel.autoFinishLyrics(lyrics, scheme) },
                                    onTransformGenre = { lyrics, genre -> viewModel.transformLyricsGenre(lyrics, genre) },
                                    onAnalyzeVocal = { viewModel.analyzeVocalPerformance() },
                                    initialTab = 3,
                                    onGeneratePreviewSong = { prompt, genre, vibe, lyrics ->
                                        viewModel.generateSongForPreview(prompt, genre, vibe, lyrics)
                                    },
                                    isPlaying = isPlaying,
                                    playbackProgress = playbackProgress,
                                    playbackDurationSeconds = playbackDurationSeconds,
                                    isBuffering = isAudioBuffering,
                                    onPlayPause = { viewModel.togglePlayPause() },
                                    onSeek = { pos -> viewModel.seekToProgress(pos) },
                                    onNavigateToHome = { currentScreen = "home" }
                                )
                                "video_visual" -> VideoVisualScreen(
                                    appLanguage = appLanguage,
                                    templates = viewModel.getLyricsVideoTemplates(),
                                    animationPresets = viewModel.getLyricAnimationPresets(),
                                    subtitleLanguages = viewModel.getSubtitleLanguages(),
                                    onGenerateCoverArt = { prompt, style -> viewModel.generateAiCoverArt(prompt, style) },
                                    onGenerateStoryboard = { title, lyrics -> viewModel.generateAiStoryboard(title, lyrics) },
                                    onGenerateSubtitles = { lyrics, lang -> viewModel.generateSubtitles(lyrics, lang) }
                                )
                                "voice_correction" -> VoiceCorrectionScreen(
                                    appLanguage = appLanguage,
                                    onNavigateToCreateSong = { _, _ ->
                                        currentScreen = "create_gen"
                                    },
                                    onSaveToDownloads = { title, _ ->
                                        scope.launch {
                                            viewModel.uploadAudioToSupabase(
                                                title = title,
                                                artist = "Sur AI Voice Corrector",
                                                genre = "Tuned Vocal",
                                                prompt = "Auto-tuned voice sample",
                                                duration = "0:30",
                                                fileName = "$title.wav",
                                                audioBytes = ByteArray(1024),
                                                imageUrl = "",
                                                isPublic = false
                                            )
                                        }
                                    },
                                    onSaveVoiceRecord = { title, scale, tone, speed, pitch ->
                                        viewModel.saveVoiceRecord(
                                            title = title,
                                            targetScale = scale,
                                            vocalTone = tone,
                                            retuneSpeed = speed,
                                            pitchShiftSemitones = pitch
                                        )
                                    }
                                )
                                "feed", "community_feed" -> FeedScreen(
                                    songs = songs,
                                    onSongClick = { song -> viewModel.playSong(song) },
                                    onFavoriteClick = { song -> viewModel.toggleFavorite(song) },
                                    isLoading = isFetchingSupabaseFeed,
                                    onRefresh = { viewModel.refreshSupabaseFeed() }
                                )
                                "social_collab" -> SocialCollabScreen(
                                    appLanguage = appLanguage,
                                    posts = viewModel.getTikTokFeedPosts(),
                                    trending = viewModel.getTrendingPageData(),
                                    profile = viewModel.getUserProfileData(),
                                    collabSessions = viewModel.getSupabaseCollabSessions(),
                                    jamRooms = viewModel.getLiveJamRooms(),
                                    battleState = viewModel.getLyricsBattleState(),
                                    duets = viewModel.getDuetChallenges(),
                                    remixes = viewModel.getRemixFeed(),
                                    playlists = viewModel.getUserPlaylists(),
                                    fanTiers = viewModel.getFanClubTiers(),
                                    liveStream = viewModel.getLiveStreamData(),
                                    songRequests = viewModel.getSongRequests(),
                                    gigs = viewModel.getMarketplaceGigs(),
                                    contest = viewModel.getCoverContestInfo()
                                )
                                "global_lang" -> GlobalLanguageScreen(
                                    appLanguage = appLanguage,
                                    translateState = viewModel.getRealtimeTranslateState(),
                                    accentState = viewModel.getAccentChangerState(),
                                    dialectData = viewModel.getRegionalDialectData(),
                                    signLangData = viewModel.getSignLanguageVideoData(),
                                    brailleData = viewModel.getBrailleLyricsData(),
                                    duetState = viewModel.getMultiLangDuetState()
                                )
                                "pro_legal" -> ProfessionalLegalScreen(
                                    appLanguage = appLanguage,
                                    royaltySplit = viewModel.getRoyaltySplitData(),
                                    isrcData = viewModel.getIsrcGeneratorData(),
                                    contractData = viewModel.getMusicContractMakerData(),
                                    arManager = viewModel.getAiArManagerData(),
                                    trademarkData = viewModel.getTrademarkSearchData(),
                                    syncOpps = viewModel.getSyncLicensingFinderData(),
                                    invoiceData = viewModel.getInvoiceGeneratorData(),
                                    expenseData = viewModel.getExpenseTrackerData(),
                                    taxReport = viewModel.getTaxReportData(),
                                    rhythmData = viewModel.getRhythmTrainingData()
                                )
                                "experience_ui" -> ExperienceUiScreen(
                                    appLanguage = appLanguage,
                                    authState = viewModel.getSupabaseAuthState(),
                                    uiConfig = viewModel.getExperienceUiConfig(),
                                    analytics = viewModel.getAnalyticsDashboardData(),
                                    widgetCast = viewModel.getWidgetAndCastData(),
                                    themeMode = themeMode,
                                    themeColor = themeColor,
                                    onThemeModeChange = { mode -> viewModel.setThemeMode(mode) },
                                    onThemeColorChange = { color -> viewModel.setThemeColor(color) }
                                )
                                "payment_sub" -> PaymentSubscriptionScreen(
                                    appLanguage = appLanguage,
                                    mfsData = viewModel.getMfsPaymentNumbers(),
                                    tokenPacks = viewModel.getTokenPacks(),
                                    coupons = viewModel.getCouponCodes(),
                                    subInfo = viewModel.getSubscriptionInfo()
                                )
                                "admin_panel" -> AdminPanelScreen(
                                    appLanguage = appLanguage,
                                    currentUserRole = userRole,
                                    pendingPayments = viewModel.getPendingPayments(),
                                    paymentLogs = viewModel.getPaymentLogs(),
                                    refunds = viewModel.getRefundRequests(),
                                    usersList = viewModel.getAdminUsersList(),
                                    realUsersList = realUsersList,
                                    incomeData = viewModel.getIncomeDashboardData(),
                                    userStats = viewModel.getUserStatsData(),
                                    apiCostData = viewModel.getApiCostTrackerData(),
                                    moderationList = viewModel.getContentModerationData(),
                                    userReports = viewModel.getUserReportsData(),
                                    featureToggles = viewModel.getFeatureTogglesList(),
                                    sysConfig = viewModel.getAdminSystemConfig(),
                                    onUpdateRole = { email, role -> viewModel.updateUserRole(email, role) },
                                    onUpdateBanned = { email, isBanned -> viewModel.updateUserBanned(email, isBanned) },
                                    onUpdateTokens = { email, tokens -> viewModel.updateUserTokenBalance(email, tokens) },
                                    onAccessDeniedClose = { currentScreen = "home" }
                                 )
                                 "tech_ai" -> TechnicalAndAiIntegrationScreen(
                                     appLanguage = appLanguage,
                                     supabaseConfig = viewModel.getSupabaseBackendConfig(),
                                     openAiData = viewModel.getOpenAiApiData(),
                                     securityConfig = viewModel.getSecurityConfig(),
                                     contentFilter = viewModel.getAiContentFilter(),
                                     cacheCdn = viewModel.getCacheCdnConfig(),
                                     lazyLoading = viewModel.getLazyLoadingConfig(),
                                     backupRestore = viewModel.getBackupRestoreData(),
                                     crashlytics = viewModel.getCrashlyticsStatus(),
                                     nftGallery = viewModel.getMusicNftGallery(),
                                     vrHall = viewModel.getVrConcertHallData(),
                                     aiAudience = viewModel.getAiAudienceData()
                                 )
                                 "automation" -> AutomationScreen(
                                     appLanguage = appLanguage,
                                     autoPosts = viewModel.getAutoPostSchedules(),
                                     captions = viewModel.getAiCaptionTemplates(),
                                     trends = viewModel.getTrendAnalyzerData(),
                                     autoReplies = viewModel.getAutoReplyRules(),
                                     backupStatus = viewModel.getSmartBackupStatus(),
                                     crashState = viewModel.getCrashRecoveryState()
                                 )
                                "marketplace" -> MarketplaceScreen(
                                    appLanguage = appLanguage,
                                    beats = viewModel.getMarketplaceBeats(),
                                    lyrics = viewModel.getMarketplaceLyrics(),
                                    nftState = viewModel.getPolygonNftMintState(),
                                    courses = viewModel.getMasterclassCourses(),
                                    whiteLabel = viewModel.getWhiteLabelConfig(),
                                    apiDashboard = viewModel.getSurAiApiDashboard(),
                                    commercialLicense = viewModel.getCommercialLicenseData(),
                                    spotifyDist = viewModel.getSpotifyDistributionData(),
                                    referralData = viewModel.getReferralProgramData(),
                                    affiliateData = viewModel.getAffiliateProgramData(),
                                    sponsorships = viewModel.getSponsorshipDeals(),
                                    tipJar = viewModel.getTipJarState()
                                )
                                "profile" -> ProfileScreen(
                                    userEmail = userEmail,
                                    tokenBalance = tokenBalance,
                                    activeSubscription = activeSubscription,
                                    themeMode = themeMode,
                                    themeColor = themeColor,
                                    onThemeModeChange = { mode -> viewModel.setThemeMode(mode) },
                                    onThemeColorChange = { color -> viewModel.setThemeColor(color) },
                                    onNavigatePricing = { currentScreen = "pricing" },
                                    onOpenTokenPacks = { showTokenDialog = true },
                                    onLogout = {
                                        viewModel.logout()
                                        currentScreen = "auth"
                                    }
                                )
                                "settings" -> SettingsScreen(
                                    appLanguage = appLanguage,
                                    themeMode = themeMode,
                                    themeColor = themeColor,
                                    autoPlay = autoPlay,
                                    hqAudio = hqAudio,
                                    studioFx = studioFx,
                                    notificationsEnabled = notificationsEnabled,
                                    isOnline = isOnline,
                                    onLanguageChange = { lang -> viewModel.setAppLanguage(lang) },
                                    onThemeModeChange = { mode -> viewModel.setThemeMode(mode) },
                                    onThemeColorChange = { color -> viewModel.setThemeColor(color) },
                                    onAutoPlayChange = { enabled -> viewModel.setAutoPlay(enabled) },
                                    onHqAudioChange = { enabled -> viewModel.setHqAudio(enabled) },
                                    onStudioFxChange = { enabled -> viewModel.setStudioFx(enabled) },
                                    onNotificationsChange = { enabled -> viewModel.setNotificationsEnabled(enabled) },
                                    onTestConnectivity = { viewModel.retryConnectivityCheck() },
                                    onTriggerTestError = { title, msg ->
                                        viewModel.showGlobalError(
                                            title = title,
                                            message = msg,
                                            category = com.example.ui.components.ErrorCategory.AUTHENTICATION,
                                            severity = com.example.ui.components.ErrorSeverity.ERROR,
                                            actionLabel = "Re-login",
                                            onAction = { currentScreen = "auth" },
                                            autoDismissMillis = 8000L
                                        )
                                    }
                                )
                                "voice_access" -> VoiceAndAccessibilityScreen(
                                    appLanguage = appLanguage,
                                    voiceConfig = viewModel.getVoiceCommandConfig(),
                                    arKaraoke = viewModel.getArKaraokeData(),
                                    noiseData = viewModel.getNoiseCancellationData(),
                                    accessData = viewModel.getAccessibilitySettingsData()
                                )
                                "pro_power" -> ProPowerFeaturesScreen(
                                    appLanguage = appLanguage,
                                    proData = viewModel.getProPowerFeaturesData(),
                                    supabaseSchema = viewModel.getSupabaseSqlSchema()
                                )
                            }
                        }
                    }
                }

                if (showFullscreenPlayer && currentSong != null) {
                    PlayerScreen(
                        song = currentSong!!,
                        isPlaying = isPlaying,
                        onPlayPauseClick = { viewModel.togglePlayPause() },
                        onFavoriteClick = { viewModel.toggleFavorite(currentSong!!) },
                        onClose = { showFullscreenPlayer = false },
                        onSkipNext = { viewModel.skipToNext() },
                        onSkipPrevious = { viewModel.skipToPrevious() },
                        playbackProgress = playbackProgress,
                        playbackDurationSeconds = playbackDurationSeconds,
                        onSeekToProgress = { pos -> viewModel.seekToProgress(pos) }
                    )
                }

                if (showTokenDialog) {
                    com.example.ui.components.TokenPackDialog(
                        currentBalance = tokenBalance,
                        onDismiss = { showTokenDialog = false },
                        onBuyTokens = { tokens, cost, desc -> viewModel.buyTokens(tokens, cost, desc) },
                        onGiftTokens = { recipient, tokens, desc -> viewModel.giftTokens(recipient, tokens, desc) }
                    )
                }

                if (showGoProDialog) {
                    com.example.ui.components.GoProDialog(
                        isBangla = (appLanguage == "bn"),
                        onDismiss = { viewModel.setShowGoProDialog(false) },
                        onUpgradeClick = { currentScreen = "pricing" },
                        onTopUpClick = { showTokenDialog = true }
                    )
                }
            }
        }

        // Global persistent / floating error banner for authentication, connectivity, and system events
        GlobalErrorBanner(
            errorInfo = globalError,
            onDismiss = { viewModel.clearGlobalError() },
            appLanguage = appLanguage,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 4.dp)
        )
    }
}
}
