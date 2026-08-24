package com.example.aimusic

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiMusicScreen(
    viewModel: MusicViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    // UI স্টেট ট্র্যাকিং
    var lyricsInput by remember { mutableStateOf("") }
    var styleInput by remember { mutableStateOf("Bangla Melodic Acoustic Pop, Soft Vocal") }
    var isAudioPlaying by remember { mutableStateOf(false) }
    var isAudioBuffering by remember { mutableStateOf(false) }
    var showApiSettingsDialog by remember { mutableStateOf(false) }
    
    // AdMob Interstitial Ad স্টেট
    var mInterstitialAd by remember { mutableStateOf<InterstitialAd?>(null) }

    // SDK ইনিশিয়ালাইজেশন এবং বিজ্ঞাপন লোড করা
    LaunchedEffect(Unit) {
        try {
            MobileAds.initialize(context) {}
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context, 
                "ca-app-pub-3940256099942544/1033173712", 
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        mInterstitialAd = null
                    }
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        mInterstitialAd = interstitialAd
                    }
                }
            )
        } catch (_: Exception) {}
    }

    // ViewModel থেকে ডাটা অবজার্ভ করা
    val musicUrl by viewModel.musicUrl.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val progressStatus by viewModel.progressStatus.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val currentApiUrl by viewModel.apiUrl.collectAsState()
    val currentApiToken by viewModel.apiToken.collectAsState()
    val tokenBalance by viewModel.tokenBalance.collectAsState()

    // এপিআই সেটিংস ডায়ালগ
    if (showApiSettingsDialog) {
        var tempUrl by remember { mutableStateOf(currentApiUrl) }
        var tempToken by remember { mutableStateOf(currentApiToken) }

        AlertDialog(
            onDismissRequest = { showApiSettingsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("সুর এআই API কনফিগারেশন", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "আপনার সুর এআই সার্ভার URL এবং টোকেন এখানে সেট করতে পারেন:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = tempUrl,
                        onValueChange = { tempUrl = it },
                        label = { Text("API Base URL") },
                        placeholder = { Text("https://api.suraimusic.com/") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempToken,
                        onValueChange = { tempToken = it },
                        label = { Text("Secret Token / Key (ঐচ্ছিক)") },
                        placeholder = { Text("Bearer sk_...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveApiConfig(tempUrl, tempToken)
                        showApiSettingsDialog = false
                        Toast.makeText(context, "API সেটিংস সংরক্ষিত হয়েছে!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiSettingsDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // প্রিমিয়াম গ্রেডিয়েন্ট ব্যাকগ্রাউন্ড থিম কালার
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
            MaterialTheme.colorScheme.surface
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("সুর এআই মিউজিক স্টুডিও", fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                    }
                },
                actions = {
                    IconButton(onClick = { showApiSettingsDialog = true }) {
                        Icon(Icons.Default.Key, contentDescription = "API Settings", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(gradientBackground)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))
            
            // API Status Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (currentApiToken.isNotBlank()) Icons.Default.CheckCircle else Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (currentApiToken.isNotBlank()) "সুর এআই Custom API সংযুক্ত" else "সুর এআই ইঞ্জিন সক্রিয়",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "সরাসরি AI অডিও জেনারেশন ও ডাউনলোড",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Stars, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("$tokenBalance Tokens", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }

                        FilledTonalButton(
                            onClick = { showApiSettingsDialog = true },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("API Key", fontSize = 12.sp)
                        }
                    }
                }
            }

            // ১. প্রিমিয়াম লিরিক্স ইনপুট কার্ড
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EditNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("গানের লিরিক্স বা কথা (Lyrics)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    OutlinedTextField(
                        value = lyricsInput,
                        onValueChange = { lyricsInput = it },
                        placeholder = { Text("[Verse 1]\nমন মাঝি তোর বৈঠা নে রে,\nমেঘের দেশে গান শোনাবো...\n\n[Chorus]\nউড়ে যাবো সুরের দেশে...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }

            // ২. মিউজিক স্টাইল ইনপুট কার্ড
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.QueueMusic, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("মিউজিক জনরা বা ধরণ (Style & Mood)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    OutlinedTextField(
                        value = styleInput,
                        onValueChange = { styleInput = it },
                        placeholder = { Text("Bangla Acoustic Folk Pop, Emotional Melodic") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }

            // ৩. সাবমিট বাটন (বিজ্ঞাপন শো করার লজিকসহ)
            Button(
                onClick = {
                    if (lyricsInput.isNotBlank() && styleInput.isNotBlank()) {
                        if (mInterstitialAd != null && activity != null) {
                            mInterstitialAd?.show(activity)
                        }
                        viewModel.requestMusicGeneration(lyricsInput, styleInput)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(14.dp)),
                enabled = !isLoading && lyricsInput.isNotBlank() && styleInput.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(progressStatus, fontSize = 14.sp)
                } else {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("এআই দিয়ে গান তৈরি করুন", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // এরর মেসেজ অ্যালার্ট
            AnimatedVisibility(visible = errorMessage != null) {
                errorMessage?.let {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // ৪. অডিও প্লেয়ার কন্ট্রোলার এবং ডাউনলোড অপশন কার্ড
            AnimatedVisibility(
                visible = musicUrl != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                musicUrl?.let { url ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "🎉 আপনার নতুন গান সম্পূর্ণ প্রস্তুত!",
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 16.sp
                            )

                            Icon(
                                imageVector = Icons.Default.Audiotrack,
                                contentDescription = null,
                                modifier = Modifier.size(44.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // প্লে/পজ বাটন
                                Button(
                                    onClick = {
                                        if (isAudioPlaying) {
                                            AudioPlayerHelper.stopAudio()
                                            isAudioPlaying = false
                                        } else {
                                            isAudioBuffering = true
                                            AudioPlayerHelper.playAudioFromUrl(
                                                audioUrl = url,
                                                onPrepared = {
                                                    isAudioBuffering = false
                                                    isAudioPlaying = true
                                                },
                                                onComplete = {
                                                    isAudioPlaying = false
                                                }
                                            )
                                        }
                                    },
                                    enabled = !isAudioBuffering,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (isAudioBuffering) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    } else {
                                        Icon(
                                            imageVector = if (isAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = null
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (isAudioPlaying) "পজ" else "প্লে করুন")
                                    }
                                }

                                // ডাউনলোড বাটন
                                FilledTonalButton(
                                    onClick = {
                                        downloadMusicFile(context, url)
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Download, contentDescription = "Download")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("ডাউনলোড (MP3)")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ==========================================
// ৫. এন্ড্রয়েড ডাউনলোড ম্যানেজার ফাংশন
// ==========================================
fun downloadMusicFile(context: Context, audioUrl: String) {
    try {
        Toast.makeText(context, "ডাউনলোড শুরু হচ্ছে...", Toast.LENGTH_SHORT).show()
        val request = DownloadManager.Request(Uri.parse(audioUrl)).apply {
            setTitle("Sur_AI_Song.mp3")
            setDescription("সুর এআই স্টুডিও থেকে আপনার তৈরি করা গান ডাউনলোড হচ্ছে।")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Sur_AI_Song_${System.currentTimeMillis()}.mp3")
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    } catch (e: Exception) {
        Toast.makeText(context, "ডাউনলোড ব্যর্থ হয়েছে: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}
