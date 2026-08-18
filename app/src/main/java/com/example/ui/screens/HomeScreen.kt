package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.SongEntity
import com.example.data.util.TrackMoodHelper
import com.example.ui.components.AiVocalTunerDialog
import com.example.ui.components.BanglaLyricsRhymeEngineDialog
import com.example.ui.components.MelodyHummingDialog
import com.example.ui.components.VoiceCloningDialog
import com.example.ui.components.DailyRewardSpinDialog
import com.example.ui.components.EqualizerBottomSheet
import com.example.ui.components.StemMixerBottomSheet
import com.example.ui.components.SleepTimerBottomSheet
import com.example.ui.components.RingtoneTrimmerBottomSheet
import com.example.ui.components.ReferralEarningDialog
import com.example.ui.components.WatchAdForCreditsDialog
import com.example.ui.components.CreatorRoyaltyCashoutDialog
import com.example.ui.components.InstantMfsPaymentDialog
import com.example.ui.components.LiveDuetStudioDialog
import com.example.ui.components.SongItemSkeletonCard
import com.example.ui.components.UploadAudioDialog
import com.example.ui.components.LiveKaraokeVocalStudioDialog
import com.example.ui.components.MultiTrackDawTimelineDialog
import com.example.ui.components.GuitarChordsVisualizerDialog
import com.example.ui.components.RiyazTanpuraStudioDialog
import com.example.ui.components.AudioMasteringEqVisualizerDialog
import com.example.ui.components.BengaliLyricistNotepadDialog
import com.example.ui.components.rememberShimmerBrush
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    songs: List<SongEntity>,
    tokenBalance: Int,
    onSongClick: (SongEntity) -> Unit,
    onFavoriteClick: (SongEntity) -> Unit,
    onNavigateCreate: () -> Unit,
    onOpenTokenPacks: () -> Unit,
    appLanguage: String = "en",
    isOnline: Boolean = true,
    isFetchingSupabase: Boolean = false,
    onRefreshFeed: (() -> Unit)? = null,
    currentUserArtistName: String = "Sur AI Artist",
    viewModel: MainViewModel? = null,
    onRewardClaimed: (Int, String) -> Unit = { _, _ -> },
    onPaymentSuccess: (Int, Double, String) -> Unit = { _, _, _ -> },
    onUploadTrack: (suspend (String, String, String, String, String, String, ByteArray, String, Boolean) -> Result<Any>)? = null,
    onPostgrestSearch: (suspend (String) -> List<com.example.data.supabase.RemoteSongItem>)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isBangla = appLanguage == "bn"
    var searchQuery by remember { mutableStateOf("") }
    var selectedSongForDownload by remember { mutableStateOf<SongEntity?>(null) }
    var isDownloading by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    // Supabase Postgrest remote query search state
    var isSearchingSupabase by remember { mutableStateOf(false) }
    var supabaseSearchResults by remember { mutableStateOf<List<SongEntity>?>(null) }

    // Dialog trigger states
    var showDailySpinDialog by remember { mutableStateOf(false) }
    var showInstantMfsDialog by remember { mutableStateOf(false) }
    var showVoiceCloneDialog by remember { mutableStateOf(false) }
    var showHumToMusicDialog by remember { mutableStateOf(false) }
    var showVocalTunerDialog by remember { mutableStateOf(false) }
    var showLiveDuetDialog by remember { mutableStateOf(false) }
    var showBanglaRhymeDialog by remember { mutableStateOf(false) }
    var showUploadAudioDialog by remember { mutableStateOf(false) }
    var showEqDialog by remember { mutableStateOf(false) }
    var showStemDialog by remember { mutableStateOf(false) }
    var showSleepDialog by remember { mutableStateOf(false) }
    var showTrimmerDialog by remember { mutableStateOf(false) }
    var trimmerSongTarget by remember { mutableStateOf<SongEntity?>(null) }
    var showReferralDialog by remember { mutableStateOf(false) }
    var showWatchAdDialog by remember { mutableStateOf(false) }
    var showRoyaltyCashoutDialog by remember { mutableStateOf(false) }
    var showLiveKaraokeDialog by remember { mutableStateOf(false) }
    var showMultiTrackDawDialog by remember { mutableStateOf(false) }
    var showGuitarChordsDialog by remember { mutableStateOf(false) }
    var showTanpuraRiyazDialog by remember { mutableStateOf(false) }
    var showMasteringEqDialog by remember { mutableStateOf(false) }
    var showLyricistNotepadDialog by remember { mutableStateOf(false) }
    var studioSelectedSong by remember { mutableStateOf<SongEntity?>(null) }

    // Real-time debounce effect to query Supabase Postgrest when search query changes
    LaunchedEffect(searchQuery) {
        val trimmed = searchQuery.trim()
        if (trimmed.isEmpty()) {
            supabaseSearchResults = null
            isSearchingSupabase = false
        } else {
            isSearchingSupabase = true
            delay(250) // Debounce rapid keystrokes
            if (onPostgrestSearch != null) {
                try {
                    val remoteItems = onPostgrestSearch(trimmed)
                    val converted = remoteItems.map { remote ->
                        SongEntity(
                            id = remote.id.hashCode().toLong(),
                            title = remote.title,
                            artist = remote.artist,
                            genre = remote.genre,
                            audioUrl = remote.audioUrl,
                            imageUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500",
                            lyrics = remote.prompt,
                            duration = remote.duration,
                            isFavorite = false,
                            isGenerated = true
                        )
                    }
                    supabaseSearchResults = converted
                } catch (e: Exception) {
                    supabaseSearchResults = null
                }
            }
            isSearchingSupabase = false
        }
    }

    // Combine local dataset filtering with Supabase Postgrest results
    val filteredSongs = remember(searchQuery, songs, supabaseSearchResults) {
        val trimmed = searchQuery.trim()
        if (trimmed.isEmpty()) {
            songs
        } else {
            val localMatches = songs.filter {
                it.title.contains(trimmed, ignoreCase = true) ||
                        it.artist.contains(trimmed, ignoreCase = true) ||
                        it.genre.contains(trimmed, ignoreCase = true)
            }
            val remoteMatches = supabaseSearchResults ?: emptyList()
            // Merge unique by title & artist
            (localMatches + remoteMatches).distinctBy { "${it.title.lowercase()}_${it.artist.lowercase()}" }
        }
    }

    val genres = listOf("All", "Cyberpunk", "Ambient", "Lofi", "Synthwave", "Pop AI", "Cinematic")
    var selectedGenre by remember { mutableStateOf("All") }

    val genreFiltered = if (selectedGenre == "All") filteredSongs else filteredSongs.filter { it.genre.contains(selectedGenre, ignoreCase = true) }

    PullToRefreshBox(
        isRefreshing = isFetchingSupabase,
        onRefresh = { onRefreshFeed?.invoke() },
        modifier = Modifier.fillMaxSize(),
        state = pullToRefreshState,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullToRefreshState,
                isRefreshing = isFetchingSupabase,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                color = MaterialTheme.colorScheme.primary
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Sur AI Music",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Discover neural-generated masterpieces",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    com.example.ui.components.TokenBalanceBadge(
                        tokenBalance = tokenBalance,
                        onClick = onOpenTokenPacks
                    )
                    IconButton(
                        onClick = onNavigateCreate,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                            .size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Create",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Search & Real-time Filter via Supabase Postgrest
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        if (isBangla) "গান বা শিল্পীর নাম দিয়ে খুঁজুন (সুপাবেস)..."
                        else "Search songs or artists via Supabase Postgrest..."
                    )
                },
                leadingIcon = {
                    if (isSearchingSupabase) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear Search")
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }

        // Offline Mode / Cached Room Database Notification
        if (!isOnline) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.85f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = "Offline Cache",
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBangla) "অফলাইন ক্যাশ মোড সক্রিয়" else "Offline Cache Active",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                text = if (isBangla) "ইন্টারনেট নেই — পূর্বে দেখা গানের তালিকা ও মেটাডাটা লোকাল রুম ডাটাবেজ (Room Cache) থেকে দেখানো হচ্ছে।"
                                else "No connection — displaying last viewed songs and metadata cached securely in local Room database.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }

        // Trial & Token Limit Banner for Free Users
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                Icons.Default.WorkspacePremium,
                                contentDescription = "Trial Limit",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (isBangla) "🌟 ফ্রি ট্রায়াল ও টোকেন লিমিট" else "🌟 Free Trial & Token Limit",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Text(
                            text = if (isBangla) 
                                "ফ্রি ইউজারদের জন্য প্রতিদিন সীমিত টোকেন ও এআই জেনারেশন প্রযোজ্য। সীমাহীন জেনারেশনের জন্য প্রো তে যান।"
                                else "Free users have daily generation & token limits. Top up or upgrade for unlimited access.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onOpenTokenPacks,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(if (isBangla) "টপ-আপ" else "Top Up", fontSize = 12.sp)
                    }
                }
            }
        }

        // Hero Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
                    .clickable { onNavigateCreate() },
                contentAlignment = Alignment.BottomStart
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "FEATURED AI ENGINE v4",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Create Tracks with Suno v4",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap to generate custom vocals & lyrics instantly",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Quick Feature Launcher Strip
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Daily Spin Action
                Surface(
                    onClick = { showDailySpinDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Stars, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                        Column {
                            Text("Daily Lucky Spin", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            Text("Win Free Tokens", fontSize = 10.sp, color = Color(0xFFF59E0B))
                        }
                    }
                }

                // Instant bKash / Nagad
                Surface(
                    onClick = { showInstantMfsDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFE2136E).copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2136E).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.FlashOn, contentDescription = null, tint = Color(0xFFE2136E), modifier = Modifier.size(20.dp))
                        Column {
                            Text("Instant bKash/Nagad", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            Text("1-Click Top-Up", fontSize = 10.sp, color = Color(0xFFE2136E))
                        }
                    }
                }

                // AI Voice Cloning
                Surface(
                    onClick = { showVoiceCloneDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF059669).copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF059669).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                        Column {
                            Text("AI Voice Clone", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            Text("নিজের কণ্ঠ ক্লোন করুন", fontSize = 10.sp, color = Color(0xFF10B981))
                        }
                    }
                }

                // Hum to Music
                Surface(
                    onClick = { showHumToMusicDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFD97706).copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                        Column {
                            Text("Hum to Music", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            Text("সুর গুনগুন করে গান", fontSize = 10.sp, color = Color(0xFFF59E0B))
                        }
                    }
                }

                // AI Vocal Tuner & Mastering
                Surface(
                    onClick = { showVocalTunerDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF7C3AED).copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7C3AED).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFA78BFA), modifier = Modifier.size(20.dp))
                        Column {
                            Text("1-Click Mastering", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            Text("অটো-টিউন ও ফিক্স", fontSize = 10.sp, color = Color(0xFFA78BFA))
                        }
                    }
                }

                // Live AI Duet
                Surface(
                    onClick = { showLiveDuetDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFEC4899).copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEC4899).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.GroupAdd, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(20.dp))
                        Column {
                            Text("Live AI Duet", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            Text("Virtual Co-Singer", fontSize = 10.sp, color = Color(0xFFEC4899))
                        }
                    }
                }

                // Bangla Rhyme Engine
                Surface(
                    onClick = { showBanglaRhymeDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                        Column {
                            Text("বাংলা অন্ত্যমিল", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            Text("Rhyme Dictionary", fontSize = 10.sp, color = Color(0xFF10B981))
                        }
                    }
                }

                // Graphic Equalizer & Sound FX
                Surface(
                    onClick = { showEqDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF6366F1).copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(20.dp))
                        Column {
                            Text("Graphic Equalizer", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            Text("Bass Boost & FX", fontSize = 10.sp, color = Color(0xFF6366F1))
                        }
                    }
                }

                // 4-Track Stem Mixer & Karaoke Vocal Cut
                Surface(
                    onClick = { showStemDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF8B5CF6).copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(20.dp))
                        Column {
                            Text("4-Track Stem Mixer", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            Text("Karaoke Vocal Cut", fontSize = 10.sp, color = Color(0xFF8B5CF6))
                        }
                    }
                }

                // Sleep Timer
                Surface(
                    onClick = { showSleepDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF3B82F6).copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Bedtime, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
                        Column {
                            Text("Sleep Timer", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            Text("Auto-Stop Audio", fontSize = 10.sp, color = Color(0xFF3B82F6))
                        }
                    }
                }

                // Ringtone Cutter & Audio Trimmer
                Surface(
                    onClick = {
                        trimmerSongTarget = songs.firstOrNull()
                        showTrimmerDialog = true
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF14B8A6).copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF14B8A6).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.ContentCut, contentDescription = null, tint = Color(0xFF14B8A6), modifier = Modifier.size(20.dp))
                        Column {
                            Text("Ringtone Trimmer", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            Text("30s Cut & Export", fontSize = 10.sp, color = Color(0xFF14B8A6))
                        }
                    }
                }

                // Referral & Earn (রেফার করে আয়)
                Surface(
                    onClick = { showReferralDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                        Column {
                            Text("রেফারেল ও আয়", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            Text("৳৫০ প্রতি রেফারে", fontSize = 10.sp, color = Color(0xFF10B981))
                        }
                    }
                }

                // Watch Ads for Free Credits (বিজ্ঞাপন দেখে ফ্রি কয়েন)
                Surface(
                    onClick = { showWatchAdDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                        Column {
                            Text("বিজ্ঞাপন দেখে কয়েন", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            Text("+৫ ফ্রি কয়েন", fontSize = 10.sp, color = Color(0xFFF59E0B))
                        }
                    }
                }

                // Creator Royalty Cashout (ক্রিয়েটর ক্যাশআউট)
                Surface(
                    onClick = { showRoyaltyCashoutDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFEC4899).copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEC4899).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(20.dp))
                        Column {
                            Text("রয়্যালটি ক্যাশআউট", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            Text("বিকাশ/নগদে উইথড্র", fontSize = 10.sp, color = Color(0xFFEC4899))
                        }
                    }
                }

                // Live Karaoke & Vocal Remover
                Surface(
                    onClick = {
                        studioSelectedSong = songs.firstOrNull()
                        showLiveKaraokeDialog = true
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFEC4899).copy(alpha = 0.18f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEC4899).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(20.dp))
                        Column {
                            Text("লাইভ কারাওকে", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            Text("ভোকাল রিমুভার ও রেকর্ডিং", fontSize = 10.sp, color = Color(0xFFEC4899))
                        }
                    }
                }

                // Multi-Track DAW Timeline
                Surface(
                    onClick = {
                        studioSelectedSong = songs.firstOrNull()
                        showMultiTrackDawDialog = true
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF8B5CF6).copy(alpha = 0.18f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(20.dp))
                        Column {
                            Text("মাল্টি-ট্র্যাক DAW", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            Text("অডিও স্টেম ও টাইমলাইন", fontSize = 10.sp, color = Color(0xFFA78BFA))
                        }
                    }
                }

                // Guitar Chords & Fretboard Visualizer
                Surface(
                    onClick = {
                        studioSelectedSong = songs.firstOrNull()
                        showGuitarChordsDialog = true
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF59E0B).copy(alpha = 0.18f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                        Column {
                            Text("গিটার কর্ড ও ট্যাব", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            Text("Fretboard & Strumming", fontSize = 10.sp, color = Color(0xFFF59E0B))
                        }
                    }
                }

                // Tanpura Drone & Classical Riyaz Studio
                Surface(
                    onClick = {
                        studioSelectedSong = songs.firstOrNull()
                        showTanpuraRiyazDialog = true
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFD97706).copy(alpha = 0.18f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(20.dp))
                        Column {
                            Text("তানপুরা ও রিয়াজ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            Text("ড্রোন ও রাগ স্কেল গাইড", fontSize = 10.sp, color = Color(0xFFFBBF24))
                        }
                    }
                }

                // Pro Audio Mastering & 7-Band Graphic EQ
                Surface(
                    onClick = {
                        studioSelectedSong = songs.firstOrNull()
                        showMasteringEqDialog = true
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF06B6D4).copy(alpha = 0.18f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF06B6D4).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Equalizer, contentDescription = null, tint = Color(0xFF06B6D4), modifier = Modifier.size(20.dp))
                        Column {
                            Text("মাস্টারিং ও EQ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            Text("7-Band EQ ও স্পেকট্রাম", fontSize = 10.sp, color = Color(0xFF22D3EE))
                        }
                    }
                }

                // Bengali Lyricist Pad & Rhyme Engine
                Surface(
                    onClick = {
                        showLyricistNotepadDialog = true
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.18f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.EditNote, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                        Column {
                            Text("বাংলা গীতিকার খাতা", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            Text("ছন্দ-মাত্রা ও অন্ত্যমিল", fontSize = 10.sp, color = Color(0xFF34D399))
                        }
                    }
                }
            }
        }

        // Genre Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                genres.forEach { genre ->
                    FilterChip(
                        selected = selectedGenre == genre,
                        onClick = { selectedGenre = genre },
                        label = { Text(genre) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }

        item {
            Text(
                text = "Trending AI Hits",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (isFetchingSupabase || isSearchingSupabase) {
            items(4) {
                SongItemSkeletonCard(shimmerBrush = rememberShimmerBrush())
            }
        } else {
            items(genreFiltered) { song ->
                SongItemCard(
                    song = song,
                    onClick = { onSongClick(song) },
                    onFavoriteClick = { onFavoriteClick(song) },
                    onDownloadClick = { selectedSongForDownload = song },
                    onShareClick = {
                        val sendIntent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            putExtra(android.content.Intent.EXTRA_SUBJECT, "Check out this song: ${song.title}")
                            putExtra(android.content.Intent.EXTRA_TEXT, "Listen to \"${song.title}\" by ${song.artist} (${song.genre}) generated on Sur AI Music!\n\nAudio Stream: ${song.audioUrl}")
                            type = "text/plain"
                        }
                        val shareIntent = android.content.Intent.createChooser(sendIntent, "Share Track via")
                        context.startActivity(shareIntent)
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Daily Lucky Spin Dialog
    if (showDailySpinDialog) {
        DailyRewardSpinDialog(
            onDismiss = { showDailySpinDialog = false },
            onRewardClaimed = { tokens, reason ->
                onRewardClaimed(tokens, reason)
            }
        )
    }

    // Instant MFS Dialog
    if (showInstantMfsDialog) {
        InstantMfsPaymentDialog(
            packName = "Popular Studio Pack",
            tokenAmount = 500,
            priceBdt = 450,
            onDismiss = { showInstantMfsDialog = false },
            onPaymentSuccess = { tokens, cost, trxId ->
                onPaymentSuccess(tokens, cost, trxId)
                Toast.makeText(context, "Deposited +$tokens Tokens via Instant MFS (TrxID: $trxId)", Toast.LENGTH_LONG).show()
            }
        )
    }

    // Live AI Duet Dialog
    if (showLiveDuetDialog) {
        LiveDuetStudioDialog(
            onDismiss = { showLiveDuetDialog = false },
            onStartDuet = { partner, harmony ->
                onNavigateCreate()
            }
        )
    }

    // Bangla Rhyme Engine Dialog
    if (showBanglaRhymeDialog) {
        BanglaLyricsRhymeEngineDialog(
            onDismiss = { showBanglaRhymeDialog = false },
            onApplyLyrics = { lyrics ->
                onNavigateCreate()
            }
        )
    }

    // Voice Cloning Dialog
    if (showVoiceCloneDialog) {
        VoiceCloningDialog(
            onDismiss = { showVoiceCloneDialog = false },
            onVoiceCloned = { voiceName ->
                Toast.makeText(context, "ভয়েস ক্লোন সম্পন্ন হয়েছে: $voiceName! এবার গান তৈরি করুন।", Toast.LENGTH_LONG).show()
                onNavigateCreate()
            }
        )
    }

    // Hum to Music Dialog
    if (showHumToMusicDialog) {
        MelodyHummingDialog(
            isBangla = isBangla,
            onDismiss = { showHumToMusicDialog = false },
            onApplyPrompt = { detectedPrompt, detectedGenre, _ ->
                Toast.makeText(context, "সুর বিশ্লেষণ সম্পন্ন! স্টুডিওতে লোড করা হচ্ছে...", Toast.LENGTH_SHORT).show()
                onNavigateCreate()
            }
        )
    }

    // AI Vocal Tuner / Mastering Dialog
    if (showVocalTunerDialog) {
        AiVocalTunerDialog(
            onDismiss = { showVocalTunerDialog = false },
            onApplyTunedVocal = { vocalChar, scale ->
                Toast.makeText(context, "ভোকাল টিউন সম্পন্ন! এবার গান তৈরি করুন।", Toast.LENGTH_SHORT).show()
                onNavigateCreate()
            }
        )
    }

    // Audio Tools Bottom Sheets
    if (showEqDialog && viewModel != null) {
        EqualizerBottomSheet(
            viewModel = viewModel,
            onDismiss = { showEqDialog = false }
        )
    }

    if (showStemDialog && viewModel != null) {
        val targetSong = songs.firstOrNull() ?: SongEntity(
            title = "Demo AI Track",
            artist = "Sur AI",
            genre = "Pop",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            imageUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500",
            lyrics = "Demo AI Studio Lyrics",
            duration = "03:15"
        )
        StemMixerBottomSheet(
            viewModel = viewModel,
            song = targetSong,
            onDismiss = { showStemDialog = false }
        )
    }

    if (showSleepDialog && viewModel != null) {
        SleepTimerBottomSheet(
            viewModel = viewModel,
            onDismiss = { showSleepDialog = false }
        )
    }

    if (showTrimmerDialog) {
        val targetSong = trimmerSongTarget ?: songs.firstOrNull() ?: SongEntity(
            title = "Demo AI Track",
            artist = "Sur AI",
            genre = "Pop",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            imageUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500",
            lyrics = "Demo AI Studio Lyrics",
            duration = "03:15"
        )
        RingtoneTrimmerBottomSheet(
            song = targetSong,
            onDismiss = { showTrimmerDialog = false }
        )
    }

    if (showReferralDialog) {
        ReferralEarningDialog(
            userReferralCode = "SUR-" + (currentUserArtistName.replace(" ", "").take(4).uppercase()) + "77",
            totalEarnings = 350.0,
            invitedCount = 7,
            onDismiss = { showReferralDialog = false }
        )
    }

    if (showWatchAdDialog) {
        WatchAdForCreditsDialog(
            onRewardEarned = { earnedCoins ->
                onRewardClaimed(earnedCoins, "বিজ্ঞাপন পুরস্কার")
            },
            onDismiss = { showWatchAdDialog = false }
        )
    }

    if (showRoyaltyCashoutDialog) {
        CreatorRoyaltyCashoutDialog(
            creatorBalance = 1450.0,
            soldTracksCount = 12,
            onWithdrawRequested = { amount, method ->
                // Registered payout
            },
            onDismiss = { showRoyaltyCashoutDialog = false }
        )
    }

    if (selectedSongForDownload != null) {
        val song = selectedSongForDownload!!
        AlertDialog(
            onDismissRequest = { selectedSongForDownload = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "গান ডাউনলোড অপশন (Download Track)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "${song.title} - ${song.artist}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Divider()
                    Button(
                        onClick = {
                            selectedSongForDownload = null
                            scope.launch {
                                Toast.makeText(context, "${song.title} MP3 (320kbps) ডাউনলোড শুরু হয়েছে...", Toast.LENGTH_SHORT).show()
                                delay(600)
                                Toast.makeText(context, "${song.title} সফলভাবে ডাউনলোড হয়েছে!", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("MP3 Audio (320 kbps HQ)")
                    }

                    OutlinedButton(
                        onClick = {
                            selectedSongForDownload = null
                            scope.launch {
                                Toast.makeText(context, "${song.title} WAV স্টুডিও মাস্টার ডাউনলোড সম্পন্ন!", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("WAV Lossless Master (24-bit)")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedSongForDownload = null }) {
                    Text("বাতিল (Cancel)")
                }
            }
        )
    }

    // Floating Action Button on Feed Screen to Upload Audio
    ExtendedFloatingActionButton(
        onClick = { showUploadAudioDialog = true },
        icon = {
            Icon(
                imageVector = Icons.Default.CloudUpload,
                contentDescription = "Upload Audio to Supabase Storage",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        },
        text = {
            Text(
                text = if (appLanguage == "bn") "অডিও আপলোড" else "Upload Track",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(bottom = 16.dp, end = 8.dp)
    )

    // Upload Audio Dialog
    if (showUploadAudioDialog) {
        UploadAudioDialog(
            appLanguage = appLanguage,
            currentUserArtistName = currentUserArtistName,
            onDismiss = { showUploadAudioDialog = false },
            onUploadConfirmed = { title, artist, genre, prompt, duration, fileName, bytes, coverUrl, isPublic ->
                onUploadTrack?.invoke(title, artist, genre, prompt, duration, fileName, bytes, coverUrl, isPublic)
                    ?: Result.failure(Exception("Upload handler not configured"))
            }
        )
    }

    val fallbackSong = songs.firstOrNull() ?: SongEntity(
        id = 1,
        title = "মেঘের দেশে সুরের খেলা",
        artist = "Sur AI Live",
        genre = "Folk Fusion",
        audioUrl = "",
        imageUrl = "",
        lyrics = "",
        duration = "3:20",
        isFavorite = false,
        isGenerated = true
    )

    // Live Karaoke Studio Dialog
    if (showLiveKaraokeDialog) {
        LiveKaraokeVocalStudioDialog(
            song = studioSelectedSong ?: fallbackSong,
            onDismiss = { showLiveKaraokeDialog = false }
        )
    }

    // Multi-Track DAW Timeline Dialog
    if (showMultiTrackDawDialog) {
        MultiTrackDawTimelineDialog(
            song = studioSelectedSong ?: fallbackSong,
            onDismiss = { showMultiTrackDawDialog = false }
        )
    }

    // Guitar & Dotara Chords Visualizer Dialog
    if (showGuitarChordsDialog) {
        GuitarChordsVisualizerDialog(
            song = studioSelectedSong ?: fallbackSong,
            onDismiss = { showGuitarChordsDialog = false }
        )
    }

    // Tanpura Drone & Classical Riyaz Studio
    if (showTanpuraRiyazDialog) {
        RiyazTanpuraStudioDialog(
            song = studioSelectedSong ?: fallbackSong,
            onDismiss = { showTanpuraRiyazDialog = false }
        )
    }

    // Pro Audio Mastering & 7-Band Graphic EQ
    if (showMasteringEqDialog) {
        AudioMasteringEqVisualizerDialog(
            song = studioSelectedSong ?: fallbackSong,
            onDismiss = { showMasteringEqDialog = false }
        )
    }

    // Bengali Lyricist Pad & Rhyme Engine
    if (showLyricistNotepadDialog) {
        BengaliLyricistNotepadDialog(
            initialLyrics = studioSelectedSong?.lyrics ?: "",
            onDismiss = { showLyricistNotepadDialog = false }
        )
    }
}
}

@Composable
fun SongItemCard(
    song: SongEntity,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onDownloadClick: () -> Unit = {},
    onShareClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = song.imageUrl,
                    contentDescription = song.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            val primaryMood = remember(song) { TrackMoodHelper.extractMoodTags(song).firstOrNull() }
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = song.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${song.artist} • ${song.genre}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (primaryMood != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(primaryMood.gradientColors.first()).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${primaryMood.emoji} ${primaryMood.nameEn}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(primaryMood.gradientColors.first()),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            IconButton(onClick = onDownloadClick) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Download",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onShareClick) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (song.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

