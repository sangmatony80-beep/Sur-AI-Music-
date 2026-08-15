package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.SongEntity
import com.example.ui.components.InteractiveWaveformVisualizer
import com.example.ui.components.RingtoneTrimmerDialog
import com.example.ui.components.SocialStoryGeneratorDialog
import com.example.ui.components.StemSeparationDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PlayerScreen(
    song: SongEntity,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var sliderPosition by remember { mutableFloatStateOf(0.35f) }
    var showDownloadDialog by remember { mutableStateOf(false) }
    var showStemDialog by remember { mutableStateOf(false) }
    var showStoryDialog by remember { mutableStateOf(false) }
    var showRingtoneDialog by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var downloadMessage by remember { mutableStateOf("") }
    
    // Equalizer & FX quick states
    var bassBoostEnabled by remember { mutableStateOf(true) }
    var vocalClarityEnabled by remember { mutableStateOf(true) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            Color(0xFF0F172A),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClose) {
                        Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Close", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    Text(
                        text = "Now Playing",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row {
                        IconButton(onClick = { showRingtoneDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.ContentCut,
                                contentDescription = "Ringtone Maker",
                                tint = Color(0xFFF59E0B)
                            )
                        }
                        IconButton(onClick = { showStoryDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Social Story Export",
                                tint = Color(0xFFEC4899)
                            )
                        }
                        IconButton(onClick = { showStemDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "Stems Mixer",
                                tint = Color(0xFF38BDF8)
                            )
                        }
                        IconButton(onClick = { showDownloadDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download Song",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onFavoriteClick) {
                            Icon(
                                imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (song.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Artwork
                AsyncImage(
                    model = song.imageUrl,
                    contentDescription = song.title,
                    modifier = Modifier
                        .size(240.dp)
                        .clip(RoundedCornerShape(28.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Song Info & Lyrics Preview
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${song.artist} • ${song.genre}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ) {
                        Box(modifier = Modifier.padding(10.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = song.lyrics,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                maxLines = 3
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive Real-Time Waveform Visualizer
                InteractiveWaveformVisualizer(
                    progress = sliderPosition,
                    isPlaying = isPlaying,
                    onSeek = { sliderPosition = it },
                    modifier = Modifier.fillMaxWidth()
                )

                // Time counters
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "1:15", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = song.duration, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Playback Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { }) {
                        Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.onBackground)
                    }
                    FloatingActionButton(
                        onClick = onPlayPauseClick,
                        modifier = Modifier.size(68.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    IconButton(onClick = { }) {
                        Icon(imageVector = Icons.Default.SkipNext, contentDescription = "Next", modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.onBackground)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Studio Action Bar (Stems Mixer + Story Export + Ringtone + Download)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilledTonalButton(
                        onClick = { showRingtoneDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.ContentCut, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFF59E0B))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Ringtone", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    FilledTonalButton(
                        onClick = { showStemDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Stems", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    FilledTonalButton(
                        onClick = { showStoryDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Story", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showDownloadDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Save", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Ringtone Maker Trimmer Dialog
    if (showRingtoneDialog) {
        RingtoneTrimmerDialog(
            song = song,
            onDismiss = { showRingtoneDialog = false },
            onSetRingtoneSuccess = { msg ->
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Stem Separation Dialog
    if (showStemDialog) {
        StemSeparationDialog(
            song = song,
            onDismiss = { showStemDialog = false }
        )
    }

    // Social Story Generator Dialog
    if (showStoryDialog) {
        SocialStoryGeneratorDialog(
            song = song,
            onDismiss = { showStoryDialog = false }
        )
    }

    // Download Audio Options Dialog
    if (showDownloadDialog) {
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
                    text = "Download Audio Options",
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
                        text = "${song.title} by ${song.artist}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Divider()

                    // MP3 320kbps
                    Surface(
                        onClick = {
                            showDownloadDialog = false
                            scope.launch {
                                isDownloading = true
                                downloadMessage = "Downloading MP3 (320kbps)..."
                                for (i in 1..100 step 20) {
                                    downloadProgress = i / 100f
                                    delay(100)
                                }
                                isDownloading = false
                                Toast.makeText(context, "${song.title} MP3 (320kbps) downloaded successfully!", Toast.LENGTH_LONG).show()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text("MP3 Audio (320 kbps HQ)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("High-fidelity stereo audio (~7.5 MB)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // WAV Lossless
                    Surface(
                        onClick = {
                            showDownloadDialog = false
                            scope.launch {
                                isDownloading = true
                                downloadMessage = "Downloading 24-bit WAV Master..."
                                for (i in 1..100 step 15) {
                                    downloadProgress = i / 100f
                                    delay(120)
                                }
                                isDownloading = false
                                Toast.makeText(context, "${song.title} WAV Studio Master downloaded!", Toast.LENGTH_LONG).show()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text("WAV Studio Master (24-bit Lossless)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Uncompressed studio master (~35 MB)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Stems ZIP
                    Surface(
                        onClick = {
                            showDownloadDialog = false
                            scope.launch {
                                isDownloading = true
                                downloadMessage = "Packaging AI Stems (Vocals + Bass + Drums)..."
                                for (i in 1..100 step 10) {
                                    downloadProgress = i / 100f
                                    delay(100)
                                }
                                isDownloading = false
                                Toast.makeText(context, "${song.title} Stems ZIP package downloaded!", Toast.LENGTH_LONG).show()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.FolderZip, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text("AI Stems Package (ZIP)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Separated Vocals, Drums & Instrument tracks", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDownloadDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
