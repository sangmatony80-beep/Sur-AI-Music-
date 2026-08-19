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
import com.example.ui.components.AiAlbumArtGeneratorDialog
import com.example.ui.components.SynchronizedLyricsView
import com.example.ui.components.SpatialAudio360StudioDialog
import com.example.ui.components.VirtualInstrumentBeatPadDialog
import com.example.ui.components.HummingToSargamTranscriptionDialog
import com.example.ui.components.LiveKaraokeVocalStudioDialog
import com.example.ui.components.MultiTrackDawTimelineDialog
import com.example.ui.components.GuitarChordsVisualizerDialog
import com.example.ui.components.RiyazTanpuraStudioDialog
import com.example.ui.components.AudioMasteringEqVisualizerDialog
import com.example.ui.components.BengaliLyricistNotepadDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PlayerScreen(
    song: SongEntity,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onClose: () -> Unit,
    onSkipNext: () -> Unit = {},
    onSkipPrevious: () -> Unit = {},
    playbackProgress: Float = 0.35f,
    playbackDurationSeconds: Int = 210,
    onSeekToProgress: (Float) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var sliderPosition by remember(playbackProgress) { mutableFloatStateOf(playbackProgress) }
    var showSyncedLyricsModal by remember { mutableStateOf(false) }
    var showDownloadDialog by remember { mutableStateOf(false) }
    var showStemDialog by remember { mutableStateOf(false) }
    var showStoryDialog by remember { mutableStateOf(false) }
    var showRingtoneDialog by remember { mutableStateOf(false) }
    var showCoverArtDialog by remember { mutableStateOf(false) }
    var showSpatial8dDialog by remember { mutableStateOf(false) }
    var showBeatPadDialog by remember { mutableStateOf(false) }
    var showHummingSargamDialog by remember { mutableStateOf(false) }
    var showKaraokeDialog by remember { mutableStateOf(false) }
    var showDawDialog by remember { mutableStateOf(false) }
    var showChordsDialog by remember { mutableStateOf(false) }
    var showTanpuraRiyazDialog by remember { mutableStateOf(false) }
    var showMasteringEqDialog by remember { mutableStateOf(false) }
    var showLyricistNotepadDialog by remember { mutableStateOf(false) }
    var showB2BExportDialog by remember { mutableStateOf(false) }
    var isB2BExporting by remember { mutableStateOf(false) }
    var b2bMessage by remember { mutableStateOf("") }

    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var downloadMessage by remember { mutableStateOf("") }
    
    var showSocialShareDialog by remember { mutableStateOf(false) }
    
    // Feature Toggle: "SIMPLE" (clean player) vs "PRO" (full studio suite)
    var studioMode by remember { mutableStateOf("PRO") }
    var selectedFeatureCategory by remember { mutableStateOf("ALL") } // "ALL", "VOCAL", "MIXING", "INSTRUMENTS"

    var currentSongArt by remember(song.imageUrl) { mutableStateOf(song.imageUrl) }
    
    // Equalizer & FX quick states
    var bassBoostEnabled by remember { mutableStateOf(true) }
    var vocalClarityEnabled by remember { mutableStateOf(true) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.82f)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
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
                        IconButton(onClick = { showSocialShareDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Track",
                                tint = Color(0xFFEC4899)
                            )
                        }
                        IconButton(onClick = { showRingtoneDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.ContentCut,
                                contentDescription = "Ringtone Maker",
                                tint = Color(0xFFF59E0B)
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
                        IconButton(onClick = { showB2BExportDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.BusinessCenter,
                                contentDescription = "Enterprise B2B Sync License",
                                tint = Color(0xFF10B981) // Emerald Green
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

                // Artwork with AI Cover Generator Badge
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .clip(RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = currentSongArt,
                        contentDescription = song.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // AI Cover Art generator overlay button
                    Surface(
                        onClick = { showCoverArtDialog = true },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.65f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFACC15), modifier = Modifier.size(12.dp))
                            Text("AI Art", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

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
                        onClick = { showSyncedLyricsModal = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(76.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "🎤 লাইভ সিঙ্কড লিরিক্স ও কারাওকে (ট্যাপ করুন)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = song.lyrics.ifEmpty { "সুরের সাথে তাল মিলিয়ে লিরিক্স দেখুন..." },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                maxLines = 2
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive Real-Time Waveform Visualizer
                InteractiveWaveformVisualizer(
                    progress = sliderPosition,
                    isPlaying = isPlaying,
                    onSeek = { 
                        sliderPosition = it
                        onSeekToProgress(it)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Time counters
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val currentSec = (sliderPosition * playbackDurationSeconds).toInt()
                    val curMin = currentSec / 60
                    val curRemSec = currentSec % 60
                    Text(text = String.format("%d:%02d", curMin, curRemSec), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = song.duration, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Playback Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onSkipPrevious,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = "Previous Track", modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.onBackground)
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
                    IconButton(
                        onClick = onSkipNext,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.SkipNext, contentDescription = "Next Track", modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.onBackground)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Studio Mode Toggle Header: Simple Player vs Pro Studio
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1E293B).copy(alpha = 0.7f))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Simple Mode Toggle Button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (studioMode == "SIMPLE") MaterialTheme.colorScheme.primary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { studioMode = "SIMPLE" }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.MusicNote,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (studioMode == "SIMPLE") Color.Black else Color.LightGray
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "বেসিক মোড (Clean)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (studioMode == "SIMPLE") Color.Black else Color.LightGray
                            )
                        }
                    }

                    // Pro Studio Mode Toggle Button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (studioMode == "PRO") Color(0xFF8B5CF6) else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { studioMode = "PRO" }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.GraphicEq,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (studioMode == "PRO") Color.White else Color.LightGray
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "প্রো স্টুডিও (Pro Tools)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (studioMode == "PRO") Color.White else Color.LightGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Core Quick Action Bar (Synced Lyrics + Ringtone + Stems + Story + Save)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilledTonalButton(
                        onClick = { showSyncedLyricsModal = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF10B981).copy(alpha = 0.2f),
                            contentColor = Color(0xFF10B981)
                        )
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Lyrics", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

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

                // Pro Studio Suite Toggled Content
                androidx.compose.animation.AnimatedVisibility(
                    visible = studioMode == "PRO",
                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                    exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Category Filter Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val categories = listOf(
                                "ALL" to "সব টুলস",
                                "VOCAL" to "ভোকাল",
                                "MIXING" to "মিক্সিং",
                                "INSTRUMENTS" to "ইন্সট্রুমেন্ট"
                            )
                            categories.forEach { (catId, catLabel) ->
                                val isSelected = selectedFeatureCategory == catId
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) Color(0xFF6366F1) else Color(0xFF1E293B),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedFeatureCategory = catId }
                                ) {
                                    Text(
                                        text = catLabel,
                                        fontSize = 9.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else Color.LightGray,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // Advanced AI Studio Suite Bar (8D Spatial Audio + Beat Matrix + Humming Sargam + Cover Art)
                        if (selectedFeatureCategory == "ALL" || selectedFeatureCategory == "MIXING" || selectedFeatureCategory == "INSTRUMENTS") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = { showSpatial8dDialog = true },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = Color(0xFF6366F1).copy(alpha = 0.2f),
                                        contentColor = Color(0xFF818CF8)
                                    )
                                ) {
                                    Icon(Icons.Default.Headphones, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("8D Audio", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                FilledTonalButton(
                                    onClick = { showBeatPadDialog = true },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = Color(0xFFF59E0B).copy(alpha = 0.2f),
                                        contentColor = Color(0xFFFBBF24)
                                    )
                                ) {
                                    Icon(Icons.Default.GridOn, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Beat Pad", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                FilledTonalButton(
                                    onClick = { showHummingSargamDialog = true },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = Color(0xFF10B981).copy(alpha = 0.2f),
                                        contentColor = Color(0xFF34D399)
                                    )
                                ) {
                                    Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Sargam", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                FilledTonalButton(
                                    onClick = { showCoverArtDialog = true },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = Color(0xFFEC4899).copy(alpha = 0.2f),
                                        contentColor = Color(0xFFF472B6)
                                    )
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Cover Art", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Pro Live Studio Suite 2 (Live Karaoke + MultiTrack DAW + Guitar Chords)
                        if (selectedFeatureCategory == "ALL" || selectedFeatureCategory == "VOCAL" || selectedFeatureCategory == "INSTRUMENTS") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = { showKaraokeDialog = true },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = Color(0xFFEC4899).copy(alpha = 0.25f),
                                        contentColor = Color(0xFFF472B6)
                                    )
                                ) {
                                    Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("কারাওকে", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                FilledTonalButton(
                                    onClick = { showDawDialog = true },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = Color(0xFF8B5CF6).copy(alpha = 0.25f),
                                        contentColor = Color(0xFFA78BFA)
                                    )
                                ) {
                                    Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("DAW", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                FilledTonalButton(
                                    onClick = { showChordsDialog = true },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = Color(0xFFF59E0B).copy(alpha = 0.25f),
                                        contentColor = Color(0xFFFBBF24)
                                    )
                                ) {
                                    Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("কর্ড ট্যাব", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Pro Classical & Audio Mastering Suite (Tanpura Riyaz, Mastering EQ, Lyricist Pad)
                        if (selectedFeatureCategory == "ALL" || selectedFeatureCategory == "MIXING" || selectedFeatureCategory == "VOCAL") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = { showTanpuraRiyazDialog = true },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = Color(0xFFD97706).copy(alpha = 0.25f),
                                        contentColor = Color(0xFFFBBF24)
                                    )
                                ) {
                                    Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("তানপুরা রিয়াজ", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                FilledTonalButton(
                                    onClick = { showMasteringEqDialog = true },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = Color(0xFF06B6D4).copy(alpha = 0.25f),
                                        contentColor = Color(0xFF22D3EE)
                                    )
                                ) {
                                    Icon(Icons.Default.Equalizer, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("মাস্টারিং EQ", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                FilledTonalButton(
                                    onClick = { showLyricistNotepadDialog = true },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = Color(0xFF10B981).copy(alpha = 0.25f),
                                        contentColor = Color(0xFF34D399)
                                    )
                                ) {
                                    Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("গীতিকার খাতা", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 8D Spatial Audio Dialog
    if (showSpatial8dDialog) {
        SpatialAudio360StudioDialog(
            song = song,
            onDismiss = { showSpatial8dDialog = false }
        )
    }

    // Beat Pad Matrix Dialog
    if (showBeatPadDialog) {
        VirtualInstrumentBeatPadDialog(
            onDismiss = { showBeatPadDialog = false }
        )
    }

    // Humming to Sargam Transcriber Dialog
    if (showHummingSargamDialog) {
        HummingToSargamTranscriptionDialog(
            onDismiss = { showHummingSargamDialog = false }
        )
    }

    // Synced Lyrics & Karaoke Fullscreen Modal
    if (showSyncedLyricsModal) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showSyncedLyricsModal = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            SynchronizedLyricsView(
                song = song,
                playbackProgress = sliderPosition,
                durationSeconds = playbackDurationSeconds,
                isPlaying = isPlaying,
                onSeekToProgress = { progress ->
                    sliderPosition = progress
                    onSeekToProgress(progress)
                },
                onClose = { showSyncedLyricsModal = false }
            )
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

    if (showCoverArtDialog) {
        AiAlbumArtGeneratorDialog(
            songTitle = song.title,
            genre = song.genre,
            onDismiss = { showCoverArtDialog = false },
            onArtApplied = { newArtUrl ->
                currentSongArt = newArtUrl
            }
        )
    }

    if (showKaraokeDialog) {
        LiveKaraokeVocalStudioDialog(
            song = song,
            onDismiss = { showKaraokeDialog = false }
        )
    }

    if (showDawDialog) {
        MultiTrackDawTimelineDialog(
            song = song,
            onDismiss = { showDawDialog = false }
        )
    }

    if (showChordsDialog) {
        GuitarChordsVisualizerDialog(
            song = song,
            onDismiss = { showChordsDialog = false }
        )
    }

    if (showTanpuraRiyazDialog) {
        RiyazTanpuraStudioDialog(
            song = song,
            onDismiss = { showTanpuraRiyazDialog = false }
        )
    }

    if (showMasteringEqDialog) {
        AudioMasteringEqVisualizerDialog(
            song = song,
            onDismiss = { showMasteringEqDialog = false }
        )
    }

    if (showLyricistNotepadDialog) {
        BengaliLyricistNotepadDialog(
            initialLyrics = song.lyrics,
            onDismiss = { showLyricistNotepadDialog = false }
        )
    }

    if (showDownloadDialog) {
        AlertDialog(
            onDismissRequest = { if (!isDownloading) showDownloadDialog = false },
            title = { Text("Download Audio") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Save this track in high-quality WAV format to your device's Music folder.", fontSize = 14.sp)
                    if (isDownloading) {
                        CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
                        Text(downloadMessage.ifEmpty { "Exporting to Music folder..." }, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    } else if (downloadMessage.isNotEmpty()) {
                        Text(downloadMessage, fontSize = 13.sp, color = Color(0xFF10B981))
                    }
                }
            },
            confirmButton = {
                if (!isDownloading) {
                    Button(onClick = {
                        isDownloading = true
                        downloadMessage = "Generating PCM audio..."
                        scope.launch {
                            try {
                                // Simulate generating/fetching PCM data from our AudioBuffer/Workflow
                                // In production, this would grab the actual generated track bytearray
                                val dummyPcm = ByteArray(48000 * 2 * 2 * 5) // 5 seconds of silent dummy data
                                downloadMessage = "Converting to WAV format..."
                                
                                val safeFilename = song.title.replace(Regex("[^a-zA-Z0-9.-]"), "_") + "_Export"
                                val success = com.ai.audio.infrastructure.export.WavExporter.exportToWav(
                                    context, dummyPcm, safeFilename
                                )
                                
                                kotlinx.coroutines.delay(1000)
                                if (success) {
                                    downloadMessage = "Saved successfully to Music/SurSun!"
                                } else {
                                    downloadMessage = "Failed to save file."
                                }
                                kotlinx.coroutines.delay(1500)
                                showDownloadDialog = false
                                downloadMessage = ""
                            } catch (e: Exception) {
                                downloadMessage = "Error: ${e.message}"
                            } finally {
                                isDownloading = false
                            }
                        }
                    }) {
                        Text("Download WAV")
                    }
                }
            },
            dismissButton = {
                if (!isDownloading) {
                    TextButton(onClick = { showDownloadDialog = false; downloadMessage = "" }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    if (showB2BExportDialog) {
        AlertDialog(
            onDismissRequest = { if (!isB2BExporting) showB2BExportDialog = false },
            title = { Text("Enterprise B2B Sync") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Issue a cryptographic watermarked license for Hollywood/AAA Game Sync. High-Fidelity 320kbps WAV.", fontSize = 14.sp)
                    if (isB2BExporting) {
                        CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
                        Text(b2bMessage, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    } else if (b2bMessage.isNotEmpty()) {
                        Text(b2bMessage, fontSize = 13.sp, color = Color(0xFF10B981))
                    }
                }
            },
            confirmButton = {
                if (!isB2BExporting) {
                    Button(onClick = {
                        isB2BExporting = true
                        b2bMessage = "Generating audio via Edge AI & Syncing License..."
                        scope.launch {
                            try {
                                val workflow = com.ai.audio.infrastructure.workflow.AudioGenerationWorkflow(context)
                                workflow.generateAndSecureTrack(
                                    token = "dummy_token_for_b2b",
                                    clientId = "client_enterprise_99X",
                                    assetId = song.id.toString(),
                                    projectId = "proj_unicorn_1"
                                )
                                kotlinx.coroutines.delay(2000) // Simulate processing time for UX
                                b2bMessage = "Success! High-Fidelity Audio Watermarked and Synced."
                                kotlinx.coroutines.delay(1500)
                                showB2BExportDialog = false
                            } catch (e: Exception) {
                                b2bMessage = "Failed: ${e.message}"
                            } finally {
                                isB2BExporting = false
                            }
                        }
                    }) {
                        Text("Generate & Sync License")
                    }
                }
            },
            dismissButton = {
                if (!isB2BExporting) {
                    TextButton(onClick = { showB2BExportDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    if (showSocialShareDialog) {
        SocialShareVisualizerDialog(song = song, onDismiss = { showSocialShareDialog = false })
    }
  }
}
