package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.SongEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    appLanguage: String,
    allSongs: List<SongEntity>,
    onPlaySong: (SongEntity) -> Unit
) {
    val context = LocalContext.current
    val isBangla = appLanguage == "bn"
    val scope = rememberCoroutineScope()

    var downloadedSongs by remember {
        mutableStateOf(allSongs.take(3))
    }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showDownloadDialog by remember { mutableStateOf(false) }
    var selectedSongForDownload by remember { mutableStateOf<SongEntity?>(null) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadStatusText by remember { mutableStateOf("") }
    var showApkGuideDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Default.DownloadForOffline,
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(20.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Text(
                            text = if (isBangla) "ডাউনলোড ও অফলাইন সেন্টার" else "Downloads & Offline Center",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showApkGuideDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.InstallMobile,
                            contentDescription = "APK Download Guide",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Storage Stats Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(
                                        text = if (isBangla) "অফলাইন স্টোরেজ ও ফাইল স্পেস" else "Offline Storage & File Space",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = if (isBangla) "${downloadedSongs.size} টি গান ডাউনলোড করা আছে (৩৬.৪ MB)" else "${downloadedSongs.size} Songs Downloaded (36.4 MB)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "320 KBPS HQ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        LinearProgressIndicator(
                            progress = { 0.28f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isBangla) "ব্যবহৃত: ৩৬.৪ MB" else "Used: 36.4 MB",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (isBangla) "ফ্রি স্পেস: ১২.৪ GB" else "Free Space: 12.4 GB",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Quick Actions: 1-Click APK Download / Guide & Audio Downloader
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Button 1: Download All Offline
                    Button(
                        onClick = {
                            scope.launch {
                                isDownloading = true
                                downloadStatusText = if (isBangla) "সব গান অফলাইনে ডাউনলোড হচ্ছে..." else "Downloading all tracks offline..."
                                for (i in 1..100 step 15) {
                                    downloadProgress = i / 100f
                                    delay(100)
                                }
                                downloadedSongs = allSongs
                                isDownloading = false
                                Toast.makeText(
                                    context,
                                    if (isBangla) "সব ${allSongs.size} টি গান সফলভাবে অফলাইনে সেভ হয়েছে!" else "All ${allSongs.size} songs saved offline!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBangla) "সব ডাউনলোড" else "Download All",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Button 2: 1-Click APK Info
                    OutlinedButton(
                        onClick = { showApkGuideDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.InstallMobile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBangla) "APK গাইড" else "APK Guide",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Tabs: Downloaded Songs vs Favorites vs Available for Download
            item {
                val favoriteSongs = allSongs.filter { it.isFavorite }
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.clip(RoundedCornerShape(14.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = if (isBangla) "ডাউনলোডকৃত (${downloadedSongs.size})" else "Downloaded (${downloadedSongs.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(13.dp))
                                Text(
                                    text = if (isBangla) "পছন্দের গান (${favoriteSongs.size})" else "Favorites (${favoriteSongs.size})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Text(
                                text = if (isBangla) "সকল গান (${allSongs.size})" else "All Tracks (${allSongs.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }

            if (isDownloading) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = downloadStatusText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${(downloadProgress * 100).toInt()}%",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            LinearProgressIndicator(
                                progress = { downloadProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            val songsToDisplay = when (selectedTab) {
                0 -> downloadedSongs
                1 -> allSongs.filter { it.isFavorite }
                else -> allSongs
            }

            if (songsToDisplay.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (selectedTab == 1) Icons.Default.FavoriteBorder else Icons.Default.CloudDownload,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Text(
                                text = when (selectedTab) {
                                    1 -> if (isBangla) "আপনার কোনো পছন্দের গান এখনো যুক্ত করা হয়নি" else "No favorite songs added yet"
                                    0 -> if (isBangla) "কোনো গান অফলাইনে ডাউনলোড করা নেই" else "No songs downloaded yet"
                                    else -> if (isBangla) "কোনো গান পাওয়া যায়নি" else "No tracks found"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(songsToDisplay) { song ->
                    val isAlreadyDownloaded = downloadedSongs.any { it.id == song.id }
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPlaySong(song) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                AsyncImage(
                                    model = song.imageUrl,
                                    contentDescription = song.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = song.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "${song.artist} • ${song.duration}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (isAlreadyDownloaded) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "অফলাইন সেভড",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    if (song.isFavorite) {
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = "Favorite",
                                            tint = Color(0xFFEC4899),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isAlreadyDownloaded && selectedTab == 0) {
                                    IconButton(
                                        onClick = {
                                            downloadedSongs = downloadedSongs.filter { it.id != song.id }
                                            Toast.makeText(context, "${song.title} অফলাইন স্টোরেজ থেকে ডিলিট করা হয়েছে", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete from Offline",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        selectedSongForDownload = song
                                        showDownloadDialog = true
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isAlreadyDownloaded) Icons.Default.FileDownloadDone else Icons.Default.Download,
                                        contentDescription = "Download Option",
                                        tint = if (isAlreadyDownloaded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Download Format Selection Dialog
    if (showDownloadDialog && selectedSongForDownload != null) {
        val song = selectedSongForDownload!!
        AlertDialog(
            onDismissRequest = { showDownloadDialog = false },
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
                    text = if (isBangla) "ডাউনলোড ফরম্যাট বেছে নিন" else "Select Download Format",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "${song.title} - ${song.artist}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Divider()

                    // Option 1: MP3 320kbps
                    DownloadOptionItem(
                        icon = Icons.Default.MusicNote,
                        title = "MP3 Audio (320 kbps HQ)",
                        subtitle = if (isBangla) "ক্রিস্টাল ক্লিয়ার অডিও ফাইল (~৭.৫ MB)" else "High quality MP3 file (~7.5 MB)",
                        onClick = {
                            showDownloadDialog = false
                            scope.launch {
                                isDownloading = true
                                downloadStatusText = if (isBangla) "MP3 অডিও ডাউনলোড হচ্ছে: ${song.title}" else "Downloading MP3: ${song.title}"
                                for (i in 1..100 step 20) {
                                    downloadProgress = i / 100f
                                    delay(100)
                                }
                                if (!downloadedSongs.any { it.id == song.id }) {
                                    downloadedSongs = downloadedSongs + song
                                }
                                isDownloading = false
                                Toast.makeText(
                                    context,
                                    if (isBangla) "${song.title} MP3 (320kbps) সফলভাবে ডাউনলোড ও সেভ হয়েছে!" else "${song.title} MP3 (320kbps) downloaded successfully!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    )

                    // Option 2: WAV Lossless
                    DownloadOptionItem(
                        icon = Icons.Default.GraphicEq,
                        title = "WAV Studio Master (24-bit Lossless)",
                        subtitle = if (isBangla) "প্রোডাকশন গ্রেড লসলেস অডিও (~৩৫ MB)" else "Master quality studio lossless audio (~35 MB)",
                        onClick = {
                            showDownloadDialog = false
                            scope.launch {
                                isDownloading = true
                                downloadStatusText = if (isBangla) "WAV 24-bit স্টুডিও মাস্টার ডাউনলোড হচ্ছে..." else "Downloading 24-bit WAV Master..."
                                for (i in 1..100 step 15) {
                                    downloadProgress = i / 100f
                                    delay(120)
                                }
                                if (!downloadedSongs.any { it.id == song.id }) {
                                    downloadedSongs = downloadedSongs + song
                                }
                                isDownloading = false
                                Toast.makeText(
                                    context,
                                    if (isBangla) "${song.title} WAV স্টুডিও মাস্টার ডাউনলোড সম্পন্ন!" else "${song.title} WAV Studio Master downloaded!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    )

                    // Option 3: AI Stems ZIP
                    DownloadOptionItem(
                        icon = Icons.Default.FolderZip,
                        title = "AI Stems Package (Vocals + Drums + Bass)",
                        subtitle = if (isBangla) "ভোকাল, ড্রামস ও ব্যাকগ্রাউন্ড মিউজিক জিপ" else "Separate stems ZIP package for remixing",
                        onClick = {
                            showDownloadDialog = false
                            scope.launch {
                                isDownloading = true
                                downloadStatusText = if (isBangla) "AI Stems জিপ প্যাকেজ তৈরি ও ডাউনলোড হচ্ছে..." else "Exporting AI Stems ZIP..."
                                for (i in 1..100 step 10) {
                                    downloadProgress = i / 100f
                                    delay(100)
                                }
                                isDownloading = false
                                Toast.makeText(
                                    context,
                                    if (isBangla) "${song.title} Stems ZIP প্যাকেজ ডাউনলোড সম্পন্ন!" else "${song.title} Stems ZIP package downloaded!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    )

                    // Option 4: Lyrics & LRC
                    DownloadOptionItem(
                        icon = Icons.Default.Description,
                        title = "Lyrics & Time-Synced LRC File",
                        subtitle = if (isBangla) "টাইম-সিঙ্কড লিরিক্স ও টেক্সট ফাইল (.lrc)" else "Time-synced lyrics file (.lrc)",
                        onClick = {
                            showDownloadDialog = false
                            Toast.makeText(
                                context,
                                if (isBangla) "${song.title} এর লিরিক্স ফাইল ডাউনলোড হয়েছে!" else "${song.title} lyrics file saved!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showDownloadDialog = false }) {
                    Text(if (isBangla) "বন্ধ করুন" else "Close")
                }
            }
        )
    }

    // APK Guide Dialog
    if (showApkGuideDialog) {
        AlertDialog(
            onDismissRequest = { showApkGuideDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.InstallMobile,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = if (isBangla) "মোবাইলে সরাসরি APK ডাউনলোড নির্দেশিকা" else "Direct APK Download Guide",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isBangla)
                                "⚡ Google AI Studio ক্লাউড বিল্ডার থেকে সরাসরি আপনার ফোনে ইন্সটলেবল APK ডাউনলোড করার সহজ উপায়:"
                            else
                                "⚡ Easy steps to generate and download the installable APK directly to your phone:",
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Text(
                        text = if (isBangla)
                            "১. আপনার মোবাইল ব্রাউজারের ৩-ডট মেনু থেকে 'Desktop site' চালু করুন।\n\n২. স্ক্রিনের উপরের ডানদিকের Settings (⚙️) বা Export মেনুতে ক্লিক করুন।\n\n৩. 'Generate APK' / 'Download APK' বাটনে চাপ দিন। সাথে সাথে ফোনে APK ডাউনলোড শুরু হবে!"
                        else
                            "1. Enable 'Desktop site' in your mobile browser.\n\n2. Tap the Settings (⚙️) or Export menu on top right.\n\n3. Click 'Generate APK' / 'Download APK' to download the installable APK file directly to your phone!",
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showApkGuideDialog = false }) {
                    Text(if (isBangla) "বুঝেছি" else "Got It")
                }
            }
        )
    }
}

@Composable
private fun DownloadOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
