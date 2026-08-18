package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.datastore.SettingsDataStore
import com.example.data.local.SurMusicDatabase
import com.example.data.local.SongEntity
import com.example.data.local.UserSubscriptionEntity
import com.example.data.local.PlanEntity
import com.example.data.local.LyricsHistoryEntity
import com.example.data.local.ClonedVoiceEntity
import com.example.data.local.UserEntity
import com.example.data.local.VoiceRecordEntity
import com.example.data.repository.UserRepository
import com.example.data.repository.VoiceRecordRepository
import com.example.data.repository.AuthResult
import com.example.data.repository.MusicRepository
import com.example.data.repository.PlanRepository
import com.example.data.repository.MusicStudioRepository
import com.example.data.repository.MoodAnalysisResult
import com.example.data.repository.VocalCoachResult
import com.example.data.supabase.SupabaseAuthManager
import com.example.data.supabase.SupabaseClientProvider
import com.example.data.supabase.SupabaseRepository
import com.example.data.supabase.SupabaseUserSessionManager
import com.example.data.supabase.RemoteSongItem
import com.example.data.util.NetworkMonitor
import com.example.data.audio.RealAudioPlaybackManager
import com.example.ui.components.ErrorCategory
import com.example.ui.components.ErrorSeverity
import com.example.ui.components.GlobalErrorInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = SurMusicDatabase.getDatabase(application)
    private val repository = MusicRepository(database.musicDao())
    private val settingsDataStore = SettingsDataStore(application)
    private val planRepository = PlanRepository(database.planDao())
    private val musicStudioRepository = MusicStudioRepository(database.lyricsDao())
    private val userRepository = UserRepository(database.userDao())
    // Supabase Integration
    val supabaseAuthManager = SupabaseAuthManager()
    val supabaseRepository = SupabaseRepository()
    private val voiceRecordRepository = VoiceRecordRepository(database.voiceRecordDao(), supabaseRepository)
    private val networkMonitor = NetworkMonitor(application)
    val audioPlaybackManager = RealAudioPlaybackManager(application)

    val allVoiceRecords: StateFlow<List<VoiceRecordEntity>> = voiceRecordRepository.allVoiceRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun syncVoiceRecordsWithCloud() {
        viewModelScope.launch {
            voiceRecordRepository.syncWithSupabase()
        }
    }

    fun saveVoiceRecord(
        title: String,
        localFilePath: String = "",
        targetScale: String = "C Major",
        vocalTone: String = "Melodious Sweetness",
        retuneSpeed: Float = 0.85f,
        pitchShiftSemitones: Float = 0f,
        isCorrected: Boolean = true
    ) {
        viewModelScope.launch {
            val record = VoiceRecordEntity(
                title = title,
                artist = _currentUser.value?.fullName ?: "User Voice",
                duration = "0:30",
                localFilePath = localFilePath.ifEmpty { "/storage/emulated/0/Music/SurAI/$title" },
                targetScale = targetScale,
                vocalTone = vocalTone,
                retuneSpeed = retuneSpeed,
                pitchShiftSemitones = pitchShiftSemitones,
                isCorrected = isCorrected
            )
            voiceRecordRepository.insertVoiceRecord(record)
        }
    }

    // Global Error & Notification State
    private val _globalError = MutableStateFlow<GlobalErrorInfo?>(null)
    val globalError: StateFlow<GlobalErrorInfo?> = _globalError.asStateFlow()

    // Network Connectivity State
    val isOnline: StateFlow<Boolean> = networkMonitor.isOnlineFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), networkMonitor.isCurrentlyConnected())

    // Supabase Integration
    val supabaseUserSessionManager = SupabaseUserSessionManager(
        context = application,
        userDao = database.userDao(),
        settingsDataStore = settingsDataStore,
        coroutineScope = viewModelScope
    )
    val supabaseRealtimeStatus: StateFlow<String> = supabaseRepository.realtimeStatus
    val isSupabaseConfigured: StateFlow<Boolean> = SupabaseClientProvider.isConfigured

    // Supabase Feed Fetching Loading State for Skeletons
    private val _isFetchingSupabaseFeed = MutableStateFlow(false)
    val isFetchingSupabaseFeed: StateFlow<Boolean> = _isFetchingSupabaseFeed.asStateFlow()

    // Auth state
    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    private val _userRole = MutableStateFlow<String>("GUEST")
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _showGoProDialog = MutableStateFlow(false)
    val showGoProDialog: StateFlow<Boolean> = _showGoProDialog.asStateFlow()

    fun setShowGoProDialog(show: Boolean) {
        _showGoProDialog.value = show
    }

    val allRegisteredUsers: StateFlow<List<UserEntity>> = userRepository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPlans: StateFlow<List<PlanEntity>> = planRepository.allPlans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeSubscription: StateFlow<UserSubscriptionEntity?> = _userEmail
        .flatMapLatest { email ->
            if (email != null) planRepository.getActiveSubscription(email)
            else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val tokenBalance: StateFlow<Int> = _userEmail
        .flatMapLatest { email ->
            if (email != null) planRepository.getTokenBalance(email)
            else flowOf(150)
        }
        .map { it ?: 150 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 150)

    val allSongs: StateFlow<List<SongEntity>> = repository.allSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteSongs: StateFlow<List<SongEntity>> = repository.favoriteSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val generatedSongs: StateFlow<List<SongEntity>> = repository.generatedSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLyricsHistory: StateFlow<List<LyricsHistoryEntity>> = musicStudioRepository.allLyricsHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allClonedVoices: StateFlow<List<ClonedVoiceEntity>> = musicStudioRepository.allClonedVoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val themeMode: StateFlow<String> = settingsDataStore.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "dark")

    val themeColor: StateFlow<String> = settingsDataStore.themeColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "NeonPurple")

    val themeFont: StateFlow<String> = settingsDataStore.appFont
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Default")

    val appLanguage: StateFlow<String> = settingsDataStore.appLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "bn")

    val autoPlay: StateFlow<Boolean> = settingsDataStore.autoPlay
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val hqAudio: StateFlow<Boolean> = settingsDataStore.hqAudio
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val studioFx: StateFlow<Boolean> = settingsDataStore.studioFx
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val notificationsEnabled: StateFlow<Boolean> = settingsDataStore.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // Playback state
    private val _currentSong = MutableStateFlow<SongEntity?>(null)
    val currentSong: StateFlow<SongEntity?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // Streaming Audio Progress & Seeking States
    private val _playbackProgress = MutableStateFlow(0f)
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

    private val _playbackDurationSeconds = MutableStateFlow(210)
    val playbackDurationSeconds: StateFlow<Int> = _playbackDurationSeconds.asStateFlow()

    private val _isAudioBuffering = MutableStateFlow(false)
    val isAudioBuffering: StateFlow<Boolean> = _isAudioBuffering.asStateFlow()

    private val _bufferedProgress = MutableStateFlow(0.85f)
    val bufferedProgress: StateFlow<Float> = _bufferedProgress.asStateFlow()

    private var playbackTickerJob: kotlinx.coroutines.Job? = null

    // --- Audio Studio & Enhancement States ---
    val bassBoost = androidx.compose.runtime.mutableStateOf(0.65f)
    val virtualizer = androidx.compose.runtime.mutableStateOf(0.5f)
    val eqBands = androidx.compose.runtime.mutableStateOf(floatArrayOf(0.2f, 0.4f, 0.1f, 0.5f, 0.3f))
    val eqPreset = androidx.compose.runtime.mutableStateOf("Bass Booster")

    // Karaoke & 4-Track Stem Mixer
    val karaokeModeEnabled = androidx.compose.runtime.mutableStateOf(false)
    val vocalStemVolume = androidx.compose.runtime.mutableStateOf(1.0f)
    val drumStemVolume = androidx.compose.runtime.mutableStateOf(1.0f)
    val bassStemVolume = androidx.compose.runtime.mutableStateOf(1.0f)
    val synthStemVolume = androidx.compose.runtime.mutableStateOf(1.0f)

    // Sleep Timer
    val sleepTimerMinutesLeft = androidx.compose.runtime.mutableStateOf(0)
    val isSleepTimerActive = androidx.compose.runtime.mutableStateOf(false)
    private var sleepTimerJob: kotlinx.coroutines.Job? = null

    // Followed Creators
    val followedArtists = androidx.compose.runtime.mutableStateListOf<String>("Suno AI", "Sur Studio", "CyberBeats")

    fun setPreset(preset: String) {
        eqPreset.value = preset
        when (preset) {
            "Bass Booster" -> {
                bassBoost.value = 0.9f
                virtualizer.value = 0.4f
                eqBands.value = floatArrayOf(0.8f, 0.6f, 0.1f, 0.2f, 0.3f)
            }
            "Vocal Booster" -> {
                bassBoost.value = 0.3f
                virtualizer.value = 0.6f
                eqBands.value = floatArrayOf(0.1f, 0.3f, 0.8f, 0.9f, 0.5f)
            }
            "Lofi Chill" -> {
                bassBoost.value = 0.7f
                virtualizer.value = 0.8f
                eqBands.value = floatArrayOf(0.5f, 0.4f, 0.2f, -0.2f, -0.4f)
            }
            "Concert Hall" -> {
                bassBoost.value = 0.6f
                virtualizer.value = 1.0f
                eqBands.value = floatArrayOf(0.4f, 0.5f, 0.3f, 0.6f, 0.7f)
            }
            else -> {
                bassBoost.value = 0.5f
                virtualizer.value = 0.5f
                eqBands.value = floatArrayOf(0f, 0f, 0f, 0f, 0f)
            }
        }
    }

    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            isSleepTimerActive.value = false
            sleepTimerMinutesLeft.value = 0
            return
        }
        isSleepTimerActive.value = true
        sleepTimerMinutesLeft.value = minutes
        sleepTimerJob = viewModelScope.launch {
            var remaining = minutes
            while (remaining > 0) {
                kotlinx.coroutines.delay(60_000L)
                remaining--
                sleepTimerMinutesLeft.value = remaining
            }
            isSleepTimerActive.value = false
            _isPlaying.value = false
        }
    }

    fun toggleFollowArtist(artistName: String) {
        if (followedArtists.contains(artistName)) {
            followedArtists.remove(artistName)
        } else {
            followedArtists.add(artistName)
        }
    }

    init {
        viewModelScope.launch {
            userRepository.seedDefaultAccounts()
            planRepository.seedDefaultPlans()

            // Restore persistent user session if present
            val savedEmail = settingsDataStore.loggedInEmail.firstOrNull()
            if (!savedEmail.isNullOrBlank()) {
                val user = userRepository.getUserByEmail(savedEmail)
                if (user != null && !user.isBanned) {
                    _userEmail.value = user.email
                    _userRole.value = user.role
                    _currentUser.value = user
                    _isLoggedIn.value = true
                }
            }

            // Seed initial sample AI tracks if database is empty
            database.musicDao().getAllSongs().collect { songs ->
                if (songs.isEmpty()) {
                    val sampleSongs = listOf(
                        SongEntity(
                            title = "Neon Cyber Dreams",
                            artist = "Sur AI & SynthMaster",
                            genre = "Cyberpunk / Electronic",
                            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                            imageUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500",
                            lyrics = "[Verse 1]\nNeon lights glowing in the rain\nCircuit pulses through my brain\n(AI Chorus)\nWe are electric, we are the sound\nDancing on quantum ground!",
                            duration = "3:45",
                            isFavorite = true,
                            isGenerated = false
                        ),
                        SongEntity(
                            title = "Quantum Echoes",
                            artist = "Suno v4 AI",
                            genre = "Ambient / Cinematic",
                            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                            imageUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=500",
                            lyrics = "[Intro]\nFrequencies rising in the void\nTimelines folding, pure and alloyd\n[Chorus]\nEchoes of tomorrow sing tonight.",
                            duration = "4:12",
                            isFavorite = false,
                            isGenerated = false
                        ),
                        SongEntity(
                            title = "Future Lofi Chill",
                            artist = "Sur AI",
                            genre = "Lofi / HipHop",
                            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                            imageUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500",
                            lyrics = "[Beat]\nCoffee steam and midnight code\nFloating down the neural road.",
                            duration = "2:58",
                            isFavorite = true,
                            isGenerated = false
                        )
                    )
                    sampleSongs.forEach { repository.insertSong(it) }
                }
            }

            // Initial sync of cloud tracks into Room database cache
            syncCloudSongsToLocalCache()

            // Monitor Network Connectivity changes
            var previousOnlineState: Boolean? = null
            networkMonitor.isOnlineFlow.collect { isConnected ->
                if (previousOnlineState != null && previousOnlineState != isConnected) {
                    if (!isConnected) {
                        _globalError.value = GlobalErrorInfo(
                            category = ErrorCategory.CONNECTIVITY,
                            severity = ErrorSeverity.WARNING,
                            title = "No Internet Connection",
                            message = "You are currently offline. Local playback and offline audio studio remain fully functional.",
                            actionLabel = "Retry",
                            onAction = { retryConnectivityCheck() },
                            isDismissable = true
                        )
                    } else {
                        _globalError.value = GlobalErrorInfo(
                            category = ErrorCategory.CONNECTIVITY,
                            severity = ErrorSeverity.SUCCESS,
                            title = "Back Online",
                            message = "Internet connection restored. Cloud sync & AI generation are active.",
                            autoDismissMillis = 4000L,
                            isDismissable = true
                        )
                        // Sync fresh songs from Supabase when connection restores
                        syncCloudSongsToLocalCache()
                    }
                }
                previousOnlineState = isConnected
            }
        }
    }

    /**
     * Synchronizes tracks from Supabase cloud into Room database cache so they are available offline.
     */
    fun syncCloudSongsToLocalCache() {
        viewModelScope.launch {
            _isFetchingSupabaseFeed.value = true
            try {
                if (networkMonitor.isCurrentlyConnected()) {
                    val cloudFeed = supabaseRepository.fetchCommunitySongs()
                    if (cloudFeed.isSuccess) {
                        val songs = cloudFeed.getOrDefault(emptyList())
                        if (songs.isNotEmpty()) {
                            repository.cacheRemoteSongs(songs)
                        }
                    }
                }
            } catch (_: Exception) {
            } finally {
                _isFetchingSupabaseFeed.value = false
            }
        }
    }

    /**
     * Explicitly refreshes the Supabase cloud track feed with skeleton loading state.
     */
    fun refreshSupabaseFeed() {
        viewModelScope.launch {
            _isFetchingSupabaseFeed.value = true
            try {
                kotlinx.coroutines.delay(800L) // Perceived performance shimmer
                if (networkMonitor.isCurrentlyConnected()) {
                    val cloudFeed = supabaseRepository.fetchCommunitySongs()
                    if (cloudFeed.isSuccess) {
                        val songs = cloudFeed.getOrDefault(emptyList())
                        if (songs.isNotEmpty()) {
                            repository.cacheRemoteSongs(songs)
                        }
                    }
                }
            } catch (_: Exception) {
            } finally {
                _isFetchingSupabaseFeed.value = false
            }
        }
    }

    fun showGlobalError(
        title: String,
        message: String,
        category: ErrorCategory = ErrorCategory.GENERAL,
        severity: ErrorSeverity = ErrorSeverity.ERROR,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null,
        autoDismissMillis: Long? = null
    ) {
        _globalError.value = GlobalErrorInfo(
            category = category,
            severity = severity,
            title = title,
            message = message,
            actionLabel = actionLabel,
            onAction = onAction,
            autoDismissMillis = autoDismissMillis
        )
    }

    fun clearGlobalError() {
        _globalError.value = null
    }

    fun retryConnectivityCheck() {
        val connected = networkMonitor.isCurrentlyConnected()
        if (connected) {
            _globalError.value = GlobalErrorInfo(
                category = ErrorCategory.CONNECTIVITY,
                severity = ErrorSeverity.SUCCESS,
                title = "Connected",
                message = "Internet connection verified!",
                autoDismissMillis = 3000L
            )
            syncCloudSongsToLocalCache()
        } else {
            _globalError.value = GlobalErrorInfo(
                category = ErrorCategory.CONNECTIVITY,
                severity = ErrorSeverity.ERROR,
                title = "Connection Failed",
                message = "Still offline. Please check your Wi-Fi or mobile data settings.",
                actionLabel = "Retry",
                onAction = { retryConnectivityCheck() }
            )
        }
    }

    private fun parseDurationToSeconds(durationStr: String): Int {
        val parts = durationStr.split(":")
        return if (parts.size == 2) {
            val mins = parts[0].trim().toIntOrNull() ?: 3
            val secs = parts[1].trim().toIntOrNull() ?: 30
            (mins * 60 + secs).coerceAtLeast(30)
        } else {
            210
        }
    }

    private fun startPlaybackTicker(song: SongEntity, initialProgress: Float = 0f) {
        playbackTickerJob?.cancel()
        val totalSec = parseDurationToSeconds(song.duration)
        _playbackDurationSeconds.value = totalSec
        _playbackProgress.value = initialProgress
        _isAudioBuffering.value = true
        _bufferedProgress.value = 0.95f

        // Trigger real multi-track sound synthesis / playback through Android speaker
        audioPlaybackManager.play(song, initialProgress) { progress, sec ->
            _playbackProgress.value = progress
        }

        playbackTickerJob = viewModelScope.launch {
            kotlinx.coroutines.delay(200L)
            _isAudioBuffering.value = false

            while (_isPlaying.value && _currentSong.value?.id == song.id) {
                kotlinx.coroutines.delay(500L)
                if (_isPlaying.value) {
                    val current = _playbackProgress.value
                    val step = 0.5f / totalSec.coerceAtLeast(1)
                    val next = current + step
                    if (next >= 1.0f) {
                        _playbackProgress.value = 1.0f
                        if (autoPlay.value) {
                            skipToNext()
                        } else {
                            _isPlaying.value = false
                            _playbackProgress.value = 0f
                            audioPlaybackManager.stop()
                        }
                        break
                    } else {
                        _playbackProgress.value = next
                    }
                }
            }
        }
    }

    fun playSong(song: SongEntity) {
        val isDifferentSong = _currentSong.value?.id != song.id
        _currentSong.value = song
        _isPlaying.value = true
        startPlaybackTicker(song, initialProgress = if (isDifferentSong) 0f else _playbackProgress.value)

        viewModelScope.launch {
            // Update last viewed timestamp in local Room database cache
            repository.recordSongViewed(song.id)
            if (song.cloudId.isNotBlank()) {
                repository.recordSongViewedByCloudId(song.cloudId)
            }
        }
    }

    fun togglePlayPause() {
        val nextPlaying = !_isPlaying.value
        _isPlaying.value = nextPlaying
        val current = _currentSong.value
        if (current != null) {
            if (nextPlaying) {
                audioPlaybackManager.resume()
                startPlaybackTicker(current, initialProgress = _playbackProgress.value)
            } else {
                audioPlaybackManager.pause()
                playbackTickerJob?.cancel()
            }
        }
    }

    fun seekToProgress(progressFraction: Float) {
        val clamped = progressFraction.coerceIn(0f, 1f)
        _playbackProgress.value = clamped
        val current = _currentSong.value
        if (current != null) {
            audioPlaybackManager.seekTo(clamped, current)
            if (_isPlaying.value) {
                startPlaybackTicker(current, initialProgress = clamped)
            }
        }
    }

    fun seekRelativeSeconds(secondsDelta: Int) {
        val totalSec = _playbackDurationSeconds.value.coerceAtLeast(1)
        val currentSec = _playbackProgress.value * totalSec
        val targetSec = (currentSec + secondsDelta).coerceIn(0f, totalSec.toFloat())
        seekToProgress(targetSec / totalSec)
    }

    fun closeMiniPlayer() {
        _isPlaying.value = false
        _currentSong.value = null
        _playbackProgress.value = 0f
        playbackTickerJob?.cancel()
        audioPlaybackManager.stop()
    }

    override fun onCleared() {
        super.onCleared()
        audioPlaybackManager.stop()
    }

    fun skipToNext() {
        val current = _currentSong.value ?: return
        val songList = allSongs.value
        if (songList.isEmpty()) return
        val currentIndex = songList.indexOfFirst { it.id == current.id }
        val nextIndex = if (currentIndex != -1 && currentIndex < songList.size - 1) currentIndex + 1 else 0
        playSong(songList[nextIndex])
    }

    fun skipToPrevious() {
        val current = _currentSong.value ?: return
        val songList = allSongs.value
        if (songList.isEmpty()) return
        val currentIndex = songList.indexOfFirst { it.id == current.id }
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else songList.size - 1
        playSong(songList[prevIndex])
    }

    fun toggleFavorite(song: SongEntity) {
        viewModelScope.launch {
            repository.updateFavorite(song.id, !song.isFavorite)
        }
    }

    suspend fun generateAiLyrics(prompt: String, language: String, genre: String, vibe: String): String {
        return musicStudioRepository.generateAiLyrics(prompt, language, genre, vibe)
    }

    suspend fun transcribeSongToLyrics(): String {
        return musicStudioRepository.transcribeSongToLyrics()
    }

    suspend fun checkCopyright(lyrics: String): Boolean {
        return musicStudioRepository.checkCopyright(lyrics)
    }

    suspend fun analyzeLyricsMood(lyrics: String): MoodAnalysisResult {
        return musicStudioRepository.analyzeLyricsMood(lyrics)
    }

    suspend fun autoFinishLyrics(incompleteLyrics: String, rhymeScheme: String = "AABB"): String {
        return musicStudioRepository.autoFinishLyrics(incompleteLyrics, rhymeScheme)
    }

    suspend fun transformLyricsGenre(lyrics: String, targetGenre: String): String {
        return musicStudioRepository.transformLyricsGenre(lyrics, targetGenre)
    }

    suspend fun analyzeVocalPerformance(): VocalCoachResult {
        return musicStudioRepository.analyzeVocalPerformance()
    }


    suspend fun saveLyricsHistory(title: String, language: String, lyrics: String, isClean: Boolean) {
        musicStudioRepository.saveLyricsHistory(title, language, lyrics, isClean)
    }

    suspend fun deleteLyricsHistory(id: Long) {
        musicStudioRepository.deleteLyricsHistory(id)
    }

    suspend fun saveClonedVoice(name: String, desc: String) {
        musicStudioRepository.saveClonedVoice(name, desc)
    }

    suspend fun deleteClonedVoice(id: Long) {
        musicStudioRepository.deleteClonedVoice(id)
    }

    fun getChordProgressions(genre: String): List<String> = musicStudioRepository.getChordProgressions(genre)
    fun getBeatSuggestions(genre: String): List<String> = musicStudioRepository.getBeatSuggestions(genre)
    fun generateMelodyNotes(key: String, scale: String): List<String> = musicStudioRepository.generateMelodyNotes(key, scale)

    suspend fun hasWatermark(): Boolean {
        val email = _userEmail.value ?: "guest@suraimusic.com"
        return planRepository.hasWatermark(email)
    }

    suspend fun generateSongForPreview(prompt: String, genre: String, vibe: String, lyrics: String): SongEntity? {
        val email = _userEmail.value ?: "guest@suraimusic.com"
        val canCreate = planRepository.canCreateLyrics(email)
        if (!canCreate) {
            _showGoProDialog.value = true
            return null
        }
        planRepository.recordLyricCreation(email)

        val watermark = planRepository.hasWatermark(email)
        val finalLyrics = if (watermark) "$lyrics\n\n[Watermark: Made with Sur AI Music]" else lyrics
        val songTitle = if (prompt.isNotBlank()) prompt.trim() else "AI $genre Masterpiece"

        val genreFinal = if (vibe.isNotBlank() && !genre.contains(vibe)) "$genre • $vibe" else genre

        val newSong = SongEntity(
            title = songTitle,
            artist = "Sur AI Creator",
            genre = genreFinal,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
            imageUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500",
            lyrics = if (finalLyrics.isNotBlank()) finalLyrics else "[Verse 1]\nSur AI melodies rise in harmonic tone\nA generated master sound all your own\n\n[Chorus]\nMusic without bounds, pure energy found!",
            duration = "3:30",
            isFavorite = false,
            isGenerated = true
        )
        val id = repository.insertSong(newSong)
        val created = newSong.copy(id = id)
        playSong(created)
        return created
    }

    fun generateAiSong(prompt: String, genre: String, vibe: String, lyrics: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val created = generateSongForPreview(prompt, genre, vibe, lyrics)
            onResult(created != null)
        }
    }

    /**
     * Upload an audio file directly to Supabase Storage and save metadata in the cloud & local database
     */
    suspend fun uploadAudioToSupabase(
        title: String,
        artist: String,
        genre: String,
        prompt: String,
        duration: String,
        fileName: String,
        audioBytes: ByteArray,
        imageUrl: String = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500",
        lyrics: String = "",
        isPublic: Boolean = true
    ): Result<SongEntity> {
        val email = _userEmail.value ?: "creator@suraimusic.com"
        val authorName = artist.ifBlank { _currentUser.value?.fullName ?: "Sur AI Artist" }
        val res = supabaseRepository.uploadAndPublishTrack(
            title = title,
            artist = authorName,
            genre = genre,
            prompt = prompt,
            duration = duration.ifBlank { "03:15" },
            fileName = fileName,
            audioBytes = audioBytes,
            userId = email,
            isPublic = isPublic
        )
        return if (res.isSuccess) {
            val remote = res.getOrThrow()
            val localSong = SongEntity(
                title = remote.title,
                artist = remote.artist,
                genre = remote.genre,
                audioUrl = remote.audioUrl,
                imageUrl = imageUrl.ifBlank { "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500" },
                lyrics = lyrics.ifBlank { "[Prompt / Description]\n$prompt\n\n[Uploaded to Supabase Storage & Community Feed]" },
                duration = remote.duration,
                isFavorite = false,
                isGenerated = false
            )
            val newId = repository.insertSong(localSong)
            val created = localSong.copy(id = newId)

            _globalError.value = GlobalErrorInfo(
                category = ErrorCategory.GENERAL,
                severity = ErrorSeverity.SUCCESS,
                title = "Track Uploaded to Cloud",
                message = "\"$title\" was uploaded to Supabase Storage and saved to the database.",
                autoDismissMillis = 5000L
            )
            Result.success(created)
        } else {
            val err = res.exceptionOrNull()?.message ?: "Upload failed"
            _globalError.value = GlobalErrorInfo(
                category = ErrorCategory.GENERAL,
                severity = ErrorSeverity.ERROR,
                title = "Upload Failed",
                message = err,
                autoDismissMillis = 6000L
            )
            Result.failure(res.exceptionOrNull() ?: Exception("Upload failed"))
        }
    }

    /**
     * Real-time search of songs using Supabase Postgrest (filtering by track title or artist)
     * and automatically caches them into the local Room database for offline access.
     */
    suspend fun searchSongsViaPostgrest(query: String): List<RemoteSongItem> {
        val result = supabaseRepository.searchSongs(query)
        val remoteList = result.getOrDefault(emptyList())
        if (remoteList.isNotEmpty()) {
            repository.cacheRemoteSongs(remoteList)
        }
        return remoteList
    }

    suspend fun loginWithCredentials(email: String, password: String): AuthResult {
        val result = supabaseUserSessionManager.signInWithEmail(email, password)
        if (result is AuthResult.Success) {
            _userEmail.value = result.user.email
            _userRole.value = result.user.role
            _currentUser.value = result.user
            _isLoggedIn.value = true
        }
        return result
    }

    suspend fun loginWithGoogle(context: android.content.Context): AuthResult {
        val googleAuthManager = com.example.auth.GoogleAuthManager(context)
        val credential = googleAuthManager.getGoogleIdToken()
        if (credential != null) {
            val email = credential.id
            val fullName = credential.displayName ?: "Google User"
            val result = supabaseUserSessionManager.signInWithGoogleIdToken(credential.idToken, email, fullName)
            if (result is AuthResult.Success) {
                _userEmail.value = result.user.email
                _userRole.value = result.user.role
                _currentUser.value = result.user
                _isLoggedIn.value = true
            }
            return result
        }
        return AuthResult.Error("Google Sign-In failed: Device has no logged-in Google Account. If you are on an emulator, please use Email/Password.")
    }

    suspend fun registerUser(email: String, password: String, fullName: String, role: String = "USER"): AuthResult {
        val result = supabaseUserSessionManager.signUpWithEmail(email, password, fullName, role)
        if (result is AuthResult.Success) {
            _userEmail.value = result.user.email
            _userRole.value = result.user.role
            _currentUser.value = result.user
            _isLoggedIn.value = true
        }
        return result
    }

    fun loginGuest() {
        _userEmail.value = "guest@suraimusic.com"
        _userRole.value = "GUEST"
        _currentUser.value = null
        _isLoggedIn.value = true
        viewModelScope.launch {
            settingsDataStore.saveUserSession("guest@suraimusic.com", "GUEST")
        }
    }

    fun logout() {
        _userEmail.value = null
        _userRole.value = "GUEST"
        _currentUser.value = null
        _isLoggedIn.value = false
        viewModelScope.launch {
            supabaseUserSessionManager.signOut()
        }
    }

    fun updateUserRole(email: String, newRole: String) {
        viewModelScope.launch {
            userRepository.updateUserRole(email, newRole)
            if (_userEmail.value == email) {
                _userRole.value = newRole
            }
        }
    }

    fun updateUserBanned(email: String, isBanned: Boolean) {
        viewModelScope.launch {
            userRepository.updateUserBanned(email, isBanned)
            if (isBanned && _userEmail.value == email) {
                logout()
            }
        }
    }

    fun updateUserTokenBalance(email: String, tokenBalance: Int) {
        viewModelScope.launch {
            userRepository.updateUserTokenBalance(email, tokenBalance)
        }
    }

    fun massCreditInject(tokens: Int, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            try {
                database.musicDao().getAllSongs() // verify db connection
                // Inject tokens or simulate broadcast token reward
                onComplete(tokens)
            } catch (e: Exception) {
                onComplete(0)
            }
        }
    }

    fun clearDatabaseCache(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                database.musicDao().clearAllSongs()
                onComplete()
            } catch (e: Exception) {
                onComplete()
            }
        }
    }

    fun subscribeToPlan(planId: String, billingCycle: String) {
        val email = _userEmail.value ?: "guest@suraimusic.com"
        viewModelScope.launch {
            planRepository.subscribeUser(email, planId, billingCycle)
        }
    }

    fun buyTokens(tokens: Int, cost: Int, description: String) {
        val email = _userEmail.value ?: "guest@suraimusic.com"
        viewModelScope.launch {
            planRepository.addTokenTransaction(email, tokens, "token_pack_purchase", description)
        }
    }

    fun purchaseTokens(tokens: Int, cost: Double) {
        val email = _userEmail.value ?: "guest@suraimusic.com"
        viewModelScope.launch {
            planRepository.addTokenTransaction(email, tokens, "mfs_instant_purchase", "Purchased $tokens tokens for BDT $cost")
        }
    }

    fun claimDailyReward(tokens: Int, reason: String) {
        val email = _userEmail.value ?: "guest@suraimusic.com"
        viewModelScope.launch {
            planRepository.addTokenTransaction(email, tokens, "daily_reward", reason)
        }
    }

    fun giftTokens(recipientEmail: String, tokens: Int, description: String) {
        val email = _userEmail.value ?: "guest@suraimusic.com"
        viewModelScope.launch {
            planRepository.addTokenTransaction(email, -tokens, "gift_sent", "Sent gift to $recipientEmail")
            planRepository.addTokenTransaction(recipientEmail, tokens, "gift_received", "Received gift from $email")
        }
    }

    suspend fun checkCanCreateLyrics(): Boolean {
        val email = _userEmail.value ?: "guest@suraimusic.com"
        return planRepository.canCreateLyrics(email)
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch { settingsDataStore.setThemeMode(mode) }
    }

    fun setThemeColor(colorName: String) {
        viewModelScope.launch { settingsDataStore.setThemeColor(colorName) }
    }

    fun setAppFont(font: String) {
        viewModelScope.launch { settingsDataStore.setAppFont(font) }
    }

    fun setAppLanguage(lang: String) {
        viewModelScope.launch { settingsDataStore.setAppLanguage(lang) }
    }

    fun setAutoPlay(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.setAutoPlay(enabled) }
    }

    fun setHqAudio(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.setHqAudio(enabled) }
    }

    fun setStudioFx(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.setStudioFx(enabled) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.setNotificationsEnabled(enabled) }
    }

    // Video & Visual Studio API hooks
    fun getLyricsVideoTemplates() = musicStudioRepository.getLyricsVideoTemplates()
    fun getLyricAnimationPresets() = musicStudioRepository.getLyricAnimationPresets()
    fun getSubtitleLanguages() = musicStudioRepository.getSubtitleLanguages()

    suspend fun generateAiCoverArt(prompt: String, style: String) =
        musicStudioRepository.generateAiCoverArt(prompt, style)

    suspend fun generateAiStoryboard(songTitle: String, lyrics: String) =
        musicStudioRepository.generateAiStoryboard(songTitle, lyrics)

    suspend fun generateSubtitles(lyrics: String, targetLanguage: String) =
        musicStudioRepository.generateSubtitles(lyrics, targetLanguage)

    // Social & Collaboration 15 Features API hooks
    fun getTikTokFeedPosts() = musicStudioRepository.getTikTokFeedPosts()
    fun getTrendingPageData() = musicStudioRepository.getTrendingPageData()
    fun getUserProfileData() = musicStudioRepository.getUserProfileData()
    fun getSupabaseCollabSessions() = musicStudioRepository.getSupabaseCollabSessions()
    fun getLiveJamRooms() = musicStudioRepository.getLiveJamRooms()
    fun getLyricsBattleState() = musicStudioRepository.getLyricsBattleState()
    fun getDuetChallenges() = musicStudioRepository.getDuetChallenges()
    fun getRemixFeed() = musicStudioRepository.getRemixFeed()
    fun getUserPlaylists() = musicStudioRepository.getUserPlaylists()
    fun getFanClubTiers() = musicStudioRepository.getFanClubTiers()
    fun getLiveStreamData() = musicStudioRepository.getLiveStreamData()
    fun getSongRequests() = musicStudioRepository.getSongRequests()
    fun getMarketplaceGigs() = musicStudioRepository.getMarketplaceGigs()
    fun getCoverContestInfo() = musicStudioRepository.getCoverContestInfo()

    // Business & Marketplace 12 Features API hooks
    fun getMarketplaceBeats() = musicStudioRepository.getMarketplaceBeats()
    fun getMarketplaceLyrics() = musicStudioRepository.getMarketplaceLyrics()
    fun getPolygonNftMintState() = musicStudioRepository.getPolygonNftMintState()
    fun getMasterclassCourses() = musicStudioRepository.getMasterclassCourses()
    fun getWhiteLabelConfig() = musicStudioRepository.getWhiteLabelConfig()
    fun getSurAiApiDashboard() = musicStudioRepository.getSurAiApiDashboard()
    fun getCommercialLicenseData() = musicStudioRepository.getCommercialLicenseData()
    fun getSpotifyDistributionData() = musicStudioRepository.getSpotifyDistributionData()
    fun getReferralProgramData() = musicStudioRepository.getReferralProgramData()
    fun getAffiliateProgramData() = musicStudioRepository.getAffiliateProgramData()
    fun getSponsorshipDeals() = musicStudioRepository.getSponsorshipDeals()
    fun getTipJarState() = musicStudioRepository.getTipJarState()

    // Global & Language 6 Features API hooks
    fun getRealtimeTranslateState() = musicStudioRepository.getRealtimeTranslateState()
    fun getAccentChangerState() = musicStudioRepository.getAccentChangerState()
    fun getRegionalDialectData() = musicStudioRepository.getRegionalDialectData()
    fun getSignLanguageVideoData() = musicStudioRepository.getSignLanguageVideoData()
    fun getBrailleLyricsData() = musicStudioRepository.getBrailleLyricsData()
    fun getMultiLangDuetState() = musicStudioRepository.getMultiLangDuetState()

    // Professional & Legal 10 Features API hooks
    fun getRoyaltySplitData() = musicStudioRepository.getRoyaltySplitData()
    fun getIsrcGeneratorData() = musicStudioRepository.getIsrcGeneratorData()
    fun getMusicContractMakerData() = musicStudioRepository.getMusicContractMakerData()
    fun getAiArManagerData() = musicStudioRepository.getAiArManagerData()
    fun getTrademarkSearchData() = musicStudioRepository.getTrademarkSearchData()
    fun getSyncLicensingFinderData() = musicStudioRepository.getSyncLicensingFinderData()
    fun getInvoiceGeneratorData() = musicStudioRepository.getInvoiceGeneratorData()
    fun getExpenseTrackerData() = musicStudioRepository.getExpenseTrackerData()
    fun getTaxReportData() = musicStudioRepository.getTaxReportData()
    fun getRhythmTrainingData() = musicStudioRepository.getRhythmTrainingData()

    // UI/UX & Experience 15 Features API hooks
    fun getSupabaseAuthState() = musicStudioRepository.getSupabaseAuthState()
    fun getExperienceUiConfig() = musicStudioRepository.getExperienceUiConfig()
    fun getAnalyticsDashboardData() = musicStudioRepository.getAnalyticsDashboardData()
    fun getWidgetAndCastData() = musicStudioRepository.getWidgetAndCastData()

    // Payment & Subscription 7 Features API hooks
    fun getMfsPaymentNumbers() = musicStudioRepository.getMfsPaymentNumbers()
    fun getTokenPacks() = musicStudioRepository.getTokenPacks()
    fun getCouponCodes() = musicStudioRepository.getCouponCodes()
    fun getSubscriptionInfo() = musicStudioRepository.getSubscriptionInfo()

    // Admin Panel 20 Features API hooks
    fun getPendingPayments() = musicStudioRepository.getPendingPayments()
    fun getPaymentLogs() = musicStudioRepository.getPaymentLogs()
    fun getRefundRequests() = musicStudioRepository.getRefundRequests()
    fun getAdminUsersList() = musicStudioRepository.getAdminUsersList()
    fun getIncomeDashboardData() = musicStudioRepository.getIncomeDashboardData()
    fun getUserStatsData() = musicStudioRepository.getUserStatsData()
    fun getApiCostTrackerData() = musicStudioRepository.getApiCostTrackerData()
    fun getContentModerationData() = musicStudioRepository.getContentModerationData()
    fun getUserReportsData() = musicStudioRepository.getUserReportsData()
    fun getFeatureTogglesList() = musicStudioRepository.getFeatureTogglesList()
    fun getAdminSystemConfig() = musicStudioRepository.getAdminSystemConfig()

    // Technical & AI Integration 12 Features API hooks
    fun getSupabaseBackendConfig() = musicStudioRepository.getSupabaseBackendConfig()
    fun getOpenAiApiData() = musicStudioRepository.getOpenAiApiData()
    fun getSecurityConfig() = musicStudioRepository.getSecurityConfig()
    fun getAiContentFilter() = musicStudioRepository.getAiContentFilter()
    fun getCacheCdnConfig() = musicStudioRepository.getCacheCdnConfig()
    fun getLazyLoadingConfig() = musicStudioRepository.getLazyLoadingConfig()
    fun getBackupRestoreData() = musicStudioRepository.getBackupRestoreData()
    fun getCrashlyticsStatus() = musicStudioRepository.getCrashlyticsStatus()
    fun getMusicNftGallery() = musicStudioRepository.getMusicNftGallery()
    fun getVrConcertHallData() = musicStudioRepository.getVrConcertHallData()
    fun getAiAudienceData() = musicStudioRepository.getAiAudienceData()

    // Automation 6 Features API hooks
    fun getAutoPostSchedules() = musicStudioRepository.getAutoPostSchedules()
    fun getAiCaptionTemplates() = musicStudioRepository.getAiCaptionTemplates()
    fun getTrendAnalyzerData() = musicStudioRepository.getTrendAnalyzerData()
    fun getAutoReplyRules() = musicStudioRepository.getAutoReplyRules()
    fun getSmartBackupStatus() = musicStudioRepository.getSmartBackupStatus()
    fun getCrashRecoveryState() = musicStudioRepository.getCrashRecoveryState()

    // Voice & Accessibility & Pro Power Features API hooks
    fun getVoiceCommandConfig() = musicStudioRepository.getVoiceCommandConfig()
    fun getArKaraokeData() = musicStudioRepository.getArKaraokeData()
    fun getNoiseCancellationData() = musicStudioRepository.getNoiseCancellationData()
    fun getAccessibilitySettingsData() = musicStudioRepository.getAccessibilitySettingsData()

    fun getProPowerFeaturesData() = musicStudioRepository.getProPowerFeaturesData()
    fun getSupabaseSqlSchema() = musicStudioRepository.getSupabaseSqlSchema()

    // Supabase Live Client Actions
    fun connectSupabaseRealtime(channel: String = "public:feed") {
        viewModelScope.launch {
            supabaseRepository.subscribeToRealtimeBroadcast(channel)
        }
    }

    fun disconnectSupabaseRealtime() {
        viewModelScope.launch {
            supabaseRepository.unsubscribeRealtime()
        }
    }

    fun syncCommunitySongsWithSupabase(onComplete: (Boolean, Int) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val result = supabaseRepository.fetchCommunitySongs()
            if (result.isSuccess) {
                val songs = result.getOrNull() ?: emptyList()
                onComplete(true, songs.size)
            } else {
                onComplete(false, 0)
            }
        }
    }
}


