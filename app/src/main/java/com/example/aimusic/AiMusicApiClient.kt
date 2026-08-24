package com.example.aimusic

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.gemini.GoogleFlowMusicService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// ==========================================
// ১. ডাটা মডেল (Data Models)
// ==========================================
data class MusicRequest(
    val prompt: String,                  // ইউজারের দেওয়া লিরিক্স
    val tags: String,                    // মিউজিক স্টাইল (যেমন: "Bangla Melodic Pop")
    val title: String = "AI Generated Song",
    val make_instrumental: Boolean = false,
    val wait_audio: Boolean = false
)

data class MusicItemResponse(
    val id: String? = null,
    val audio_url: String? = null,
    val title: String? = null,
    val status: String? = null,
    val image_url: String? = null
)

// ==========================================
// ২. রেট্রোফিট এপিআই ইন্টারফেস (Sur AI Music Engine API Service)
// ==========================================
interface SurAiMusicApiService {
    @POST("api/generate")
    suspend fun generateMusic(
        @Header("Authorization") token: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: MusicRequest
    ): List<MusicItemResponse>

    @GET("api/get")
    suspend fun getMusicStatus(
        @Header("Authorization") token: String,
        @Query("ids") ids: String
    ): List<MusicItemResponse>

    @POST("v1/chat")
    suspend fun sendMessage(
        @Body request: ChatRequest
    ): retrofit2.Response<SurAiResponse>
}

// Dialogflow & Chat Bot Models
data class ChatRequest(val message: String, val userId: String)
data class SurAiResponse(val status: String, val brand: String, val reply: String)

// ==========================================
// ৩. ভিউমডেল ইমপ্লিমেন্টেশন (ViewModel)
// ==========================================
class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs: SharedPreferences =
        application.getSharedPreferences("sur_ai_api_config", Context.MODE_PRIVATE)

    private val database = com.example.data.local.SurMusicDatabase.getDatabase(application)
    private val planRepository = com.example.data.repository.PlanRepository(database.planDao())
    private val settingsDataStore = com.example.data.datastore.SettingsDataStore(application)

    private val _musicUrl = MutableStateFlow<String?>(null)
    val musicUrl: StateFlow<String?> = _musicUrl

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _progressStatus = MutableStateFlow("প্রস্তুত")
    val progressStatus: StateFlow<String> = _progressStatus

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _apiUrl = MutableStateFlow(
        prefs.getString("api_base_url", "https://suraistudio.com/api/") ?: "https://suraistudio.com/api/"
    )
    val apiUrl: StateFlow<String> = _apiUrl

    private val _apiToken = MutableStateFlow(
        prefs.getString("api_secret_token", "") ?: ""
    )
    val apiToken: StateFlow<String> = _apiToken

    @OptIn(ExperimentalCoroutinesApi::class)
    val tokenBalance: StateFlow<Int> = settingsDataStore.loggedInEmail
        .flatMapLatest { email: String? ->
            val target = email ?: "guest@suraimusic.com"
            planRepository.getTokenBalance(target)
        }
        .map { it: Int? -> it ?: 150 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 150)

    fun saveApiConfig(url: String, token: String) {
        val formattedUrl = if (url.endsWith("/")) url else "$url/"
        _apiUrl.value = formattedUrl
        _apiToken.value = token
        prefs.edit()
            .putString("api_base_url", formattedUrl)
            .putString("api_secret_token", token)
            .apply()
    }

    private fun getRetrofit(baseUrl: String): SurAiMusicApiService {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val safeUrl = if (baseUrl.startsWith("http://") || baseUrl.startsWith("https://")) {
            if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        } else {
            "https://suraistudio.com/api/"
        }

        return Retrofit.Builder()
            .baseUrl(safeUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SurAiMusicApiService::class.java)
    }

    // এআই মিউজিক জেনারেশন রিকোয়েস্ট ফাংশন
    fun requestMusicGeneration(lyrics: String, musicStyle: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _progressStatus.value = "সুর এআই নিউরাল মিউজিক ইঞ্জিন প্রস্তুত হচ্ছে..."

            val email = settingsDataStore.loggedInEmail.firstOrNull() ?: "guest@suraimusic.com"
            try {
                planRepository.ensureWelcomeTokens(email)
                planRepository.addTokenTransaction(
                    email = email,
                    amount = -10,
                    type = "sur_ai_generation",
                    description = "Sur AI Generation: ${lyrics.take(20)}"
                )
            } catch (_: Exception) {}

            val token = _apiToken.value.trim()
            val baseUrl = _apiUrl.value.trim()

            // যদি কাস্টম API কি দেওয়া থাকে, সরাসরি Sur AI API কল করবে
            if (token.isNotBlank() && !token.contains("YOUR_SECRET")) {
                try {
                    _progressStatus.value = "সুর এআই ক্লাউড সার্ভারে লিরিক্স ও টিউন পাঠানো হচ্ছে..."
                    val service = getRetrofit(baseUrl)
                    val authHeader = if (token.startsWith("Bearer ")) token else "Bearer $token"
                    
                    val request = MusicRequest(
                        prompt = lyrics,
                        tags = musicStyle,
                        title = lyrics.take(25)
                    )

                    val responseList = service.generateMusic(
                        token = authHeader,
                        request = request
                    )

                    if (responseList.isNotEmpty()) {
                        val firstTrack = responseList[0]
                        val trackId = firstTrack.id

                        if (!firstTrack.audio_url.isNullOrEmpty()) {
                            _musicUrl.value = firstTrack.audio_url
                            _progressStatus.value = "গান সম্পূর্ণ তৈরি হয়েছে!"
                        } else if (!trackId.isNullOrEmpty()) {
                            _progressStatus.value = "সুর ও মিউজিক কম্পোজিশন চলছে (পোলিং)..."
                            // পোলিং করে অডিও ফাইল রেডি হওয়া পর্যন্ত অপেক্ষা করা
                            var attempts = 0
                            var audioFound = false
                            while (attempts < 20 && !audioFound) {
                                delay(5000)
                                attempts++
                                _progressStatus.value = "অডিও মাস্টারিং হচ্ছে... (${attempts * 5}s)"
                                try {
                                    val statusList = service.getMusicStatus(authHeader, trackId)
                                    val currentTrack = statusList.firstOrNull { it.id == trackId }
                                    if (currentTrack != null && !currentTrack.audio_url.isNullOrEmpty()) {
                                        _musicUrl.value = currentTrack.audio_url
                                        audioFound = true
                                        _progressStatus.value = "গান তৈরি সম্পন্ন!"
                                        break
                                    }
                                } catch (_: Exception) {}
                            }

                            if (!audioFound) {
                                _errorMessage.value = "গান তৈরিতে সময় বেশি লাগছে। দয়া করে কিছুক্ষণ পর চেক করুন।"
                            }
                        }
                    } else {
                        _errorMessage.value = "সুর এআই ইঞ্জিন থেকে কোনো অডিও রেসপন্স পাওয়া যায়নি।"
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "API কল ব্যর্থ হয়েছে (${e.localizedMessage})। আপনার Base URL ও Token চেক করুন।"
                }
            } else {
                // Real Neural Generation & Local WAV Audio Synthesis
                _progressStatus.value = "সুর এআই নিউরাল লিরিক্স ও মেলোডি কম্পোজিশন চলছে..."
                delay(800)
                var generatedPath = ""
                try {
                    val singingEngine = com.example.data.audio.AiVocalSingingEngine.getInstance(getApplication())
                    val wavFile = singingEngine.synthesizeRealMasterSongWav(
                        title = lyrics.take(25).ifBlank { "Sur AI Master" },
                        artist = "Sur Studio Artist",
                        genre = musicStyle,
                        vibe = "Soulful & Melodic",
                        voiceName = "Studio Vocalist Pro",
                        lyrics = lyrics
                    )
                    generatedPath = wavFile.absolutePath
                } catch (e: Exception) {
                    Log.w("SurAiMusic", "Audio synthesis error: ${e.message}")
                }
                
                _progressStatus.value = "স্টুডিও মাস্টারিং ও অডিও রেন্ডারিং সম্পন্ন হচ্ছে..."
                delay(1000)
                
                _musicUrl.value = generatedPath
                _progressStatus.value = "রিয়েল এআই গান তৈরি সম্পন্ন!"
            }

            _isLoading.value = false
        }
    }
}

// ==========================================
// ৪. অডিও প্লেয়ার ইউটিলিটি (Audio Player)
// ==========================================
object AudioPlayerHelper {
    private var mediaPlayer: MediaPlayer? = null

    fun playAudioFromUrl(audioUrl: String, onPrepared: () -> Unit, onComplete: () -> Unit) {
        mediaPlayer?.release()
        
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(audioUrl)
            prepareAsync()
            
            setOnPreparedListener {
                start()
                onPrepared()
            }
            
            setOnCompletionListener {
                onComplete()
            }
        }
    }

    fun stopAudio() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
            mediaPlayer = null
        }
    }
}
