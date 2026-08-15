package com.example.ui.components

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadAudioDialog(
    appLanguage: String = "en",
    currentUserArtistName: String = "Sur AI Artist",
    onDismiss: () -> Unit,
    onUploadConfirmed: suspend (
        title: String,
        artist: String,
        genre: String,
        prompt: String,
        duration: String,
        fileName: String,
        audioBytes: ByteArray,
        imageUrl: String,
        isPublic: Boolean
    ) -> Result<Any>
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isBangla = appLanguage == "bn"

    var songTitle by remember { mutableStateOf("") }
    var artistName by remember { mutableStateOf(currentUserArtistName) }
    var promptDescription by remember { mutableStateOf("") }
    var durationText by remember { mutableStateOf("03:20") }
    var isPublicFeed by remember { mutableStateOf(true) }

    val genreOptions = listOf(
        "Bangla Pop", "Baul Fusion", "Cyberpunk", "Lofi Chill",
        "Synthwave", "EDM Club", "Cinematic", "Folk Rock", "Acoustic Classical"
    )
    var selectedGenre by remember { mutableStateOf(genreOptions.first()) }

    val coverArtPresets = listOf(
        "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500",
        "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500",
        "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500",
        "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=500",
        "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500"
    )
    var selectedCoverUrl by remember { mutableStateOf(coverArtPresets.first()) }

    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedFileBytes by remember { mutableStateOf<ByteArray?>(null) }
    var selectedFileSizeKb by remember { mutableStateOf<Long?>(null) }

    var isUploading by remember { mutableStateOf(false) }
    var uploadStepText by remember { mutableStateOf("") }
    var uploadProgress by remember { mutableFloatStateOf(0f) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // File picker launcher
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                var fileName = "track_${System.currentTimeMillis()}.mp3"
                var fileSize = 0L
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIndex >= 0) fileName = cursor.getString(nameIndex)
                        if (sizeIndex >= 0) fileSize = cursor.getLong(sizeIndex)
                    }
                }
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null && bytes.isNotEmpty()) {
                    selectedFileName = fileName
                    selectedFileBytes = bytes
                    selectedFileSizeKb = if (fileSize > 0) fileSize / 1024 else bytes.size.toLong() / 1024
                    errorMessage = null
                    if (songTitle.isBlank()) {
                        songTitle = fileName.substringBeforeLast(".").replace("_", " ").replace("-", " ")
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Could not read audio file: ${e.message}"
            }
        }
    }

    Dialog(
        onDismissRequest = { if (!isUploading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Column {
                            Text(
                                text = if (isBangla) "সুপাবেস অডিও আপলোড" else "Upload Audio to Supabase",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isBangla) "স্টোরেজ ও ডাটাবেস সিঙ্ক" else "Storage Bucket & Database Sync",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isUploading
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable Form Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Storage target info pill
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Bucket: audio_tracks • Table: public.songs",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Audio File Selection Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedFileBytes != null)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isBangla) "অডিও ফাইল নির্বাচন" else "Audio File Source",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                if (selectedFileName != null) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "Ready (${selectedFileSizeKb ?: 0} KB)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            if (selectedFileName != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AudioFile,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = selectedFileName ?: "",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            } else {
                                Text(
                                    text = if (isBangla)
                                        "আপনার ডিভাইস থেকে MP3, WAV, M4A ফাইল আপলোড করুন অথবা ডেমো ফাইল ব্যবহার করুন।"
                                    else
                                        "Select any MP3, WAV, or M4A audio file from your device, or load a sample master track.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { audioPickerLauncher.launch("audio/*") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = !isUploading
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FolderOpen,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = if (isBangla) "ফাইল বাছুন" else "Pick Audio")
                                }

                                OutlinedButton(
                                    onClick = {
                                        // Generate demo audio sample payload
                                        val sampleBytes = ByteArray(1024 * 64) { (it % 128).toByte() }
                                        selectedFileName = "demo_sur_master_${System.currentTimeMillis() % 1000}.mp3"
                                        selectedFileBytes = sampleBytes
                                        selectedFileSizeKb = 64
                                        if (songTitle.isBlank()) songTitle = "Sur AI Cloud Reverie"
                                        errorMessage = null
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = !isUploading
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoFixHigh,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = if (isBangla) "ডেমো স্যাম্পল" else "Use Sample")
                                }
                            }
                        }
                    }

                    // Song Title Field
                    OutlinedTextField(
                        value = songTitle,
                        onValueChange = { songTitle = it },
                        label = { Text(if (isBangla) "গানের শিরোনাম *" else "Song Title *") },
                        placeholder = { Text("e.g. Cyber Baul Horizon") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.MusicNote, contentDescription = null)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !isUploading
                    )

                    // Artist Name Field
                    OutlinedTextField(
                        value = artistName,
                        onValueChange = { artistName = it },
                        label = { Text(if (isBangla) "শিল্পী / সুরকার" else "Artist / Producer") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !isUploading
                    )

                    // Genre Selector Chips
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (isBangla) "জনরা নির্বাচন" else "Select Genre",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(genreOptions) { genre ->
                                FilterChip(
                                    selected = selectedGenre == genre,
                                    onClick = { selectedGenre = genre },
                                    label = { Text(genre, fontSize = 12.sp) },
                                    enabled = !isUploading
                                )
                            }
                        }
                    }

                    // Prompt / Story Description
                    OutlinedTextField(
                        value = promptDescription,
                        onValueChange = { promptDescription = it },
                        label = { Text(if (isBangla) "প্রম্পট বা গানের বর্ণনা" else "Prompt / Track Inspiration") },
                        placeholder = { Text("e.g. Modern electronic beats combined with traditional flute melodies") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3,
                        shape = RoundedCornerShape(14.dp),
                        enabled = !isUploading
                    )

                    // Duration & Public Toggle Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = durationText,
                            onValueChange = { durationText = it },
                            label = { Text(if (isBangla) "সময়সীমা" else "Duration") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            enabled = !isUploading
                        )

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1.3f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (isBangla) "পাবলিক ফিড" else "Public Feed",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isPublicFeed) "Community" else "Private",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = isPublicFeed,
                                    onCheckedChange = { isPublicFeed = it },
                                    enabled = !isUploading
                                )
                            }
                        }
                    }

                    // Cover Art Selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (isBangla) "কভার আর্ট নির্বাচন" else "Select Cover Art",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(coverArtPresets) { url ->
                                val isSelected = selectedCoverUrl == url
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { selectedCoverUrl = url }
                                ) {
                                    AsyncImage(
                                        model = url,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }

                    // Error Message
                    if (errorMessage != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = errorMessage ?: "",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // Upload Progress Bar
                    if (isUploading) {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = uploadStepText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "${(uploadProgress * 100).toInt()}%",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { uploadProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !isUploading
                    ) {
                        Text(if (isBangla) "বাতিল" else "Cancel")
                    }

                    Button(
                        onClick = {
                            if (songTitle.isBlank()) {
                                errorMessage = if (isBangla) "গানের শিরোনাম দিন" else "Please enter a song title"
                                return@Button
                            }
                            val bytes = selectedFileBytes
                            val fileName = selectedFileName ?: "${songTitle.replace(" ", "_")}.mp3"
                            if (bytes == null) {
                                errorMessage = if (isBangla) "অনুগ্রহ করে একটি অডিও ফাইল নির্বাচন করুন" else "Please select or generate an audio file"
                                return@Button
                            }

                            scope.launch {
                                isUploading = true
                                errorMessage = null
                                uploadProgress = 0.15f
                                uploadStepText = if (isBangla) "১. অডিও বাইট বিশ্লেষণ হচ্ছে..." else "1. Reading audio byte stream..."
                                delay(300)

                                uploadProgress = 0.55f
                                uploadStepText = if (isBangla) "২. সুপাবেস স্টোরেজে আপলোড হচ্ছে..." else "2. Uploading to Supabase Storage bucket..."
                                delay(400)

                                uploadProgress = 0.85f
                                uploadStepText = if (isBangla) "৩. ডাটাবেসে মেটাডাটা সংরক্ষণ হচ্ছে..." else "3. Saving track metadata to Postgres..."

                                val result = onUploadConfirmed(
                                    songTitle,
                                    artistName,
                                    selectedGenre,
                                    promptDescription,
                                    durationText,
                                    fileName,
                                    bytes,
                                    selectedCoverUrl,
                                    isPublicFeed
                                )

                                uploadProgress = 1.0f
                                uploadStepText = if (isBangla) "৪. সফলভাবে সম্পন্ন হয়েছে!" else "4. Complete!"
                                delay(300)
                                isUploading = false

                                if (result.isSuccess) {
                                    Toast.makeText(
                                        context,
                                        if (isBangla) "গানটি সফলভাবে আপলোড ও সেভ করা হয়েছে!" else "Track uploaded & saved to database!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    onDismiss()
                                } else {
                                    errorMessage = result.exceptionOrNull()?.message ?: "Upload failed"
                                }
                            }
                        },
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !isUploading && selectedFileBytes != null
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isBangla) "আপলোড হচ্ছে..." else "Uploading...")
                        } else {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isBangla) "ক্লাউডে আপলোড" else "Upload to Cloud")
                        }
                    }
                }
            }
        }
    }
}
