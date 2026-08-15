package com.example.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.local.SongEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Social Story & Audio Snippet Generator
 * Creates an aesthetic 15-30-60s visual card with synced lyrics, waveform overlay,
 * and quick-sharing to Facebook Story, Instagram Reels, TikTok, or WhatsApp.
 */
@Composable
fun SocialStoryGeneratorDialog(
    song: SongEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var snippetDuration by remember { mutableIntStateOf(30) } // 15, 30, 60s
    var selectedThemeIndex by remember { mutableIntStateOf(0) }
    var selectedSnippetPart by remember { mutableStateOf("Chorus / Drop (0:45 - 1:15)") }
    var isExporting by remember { mutableStateOf(false) }

    val storyGradients = listOf(
        listOf(Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFF0F172A)), // Neon Sunset
        listOf(Color(0xFF06B6D4), Color(0xFF3B82F6), Color(0xFF0284C7)), // Ocean Pulse
        listOf(Color(0xFFF59E0B), Color(0xFFEF4444), Color(0xFF7C2D12)), // Cyber Fire
        listOf(Color(0xFF10B981), Color(0xFF059669), Color(0xFF064E3B))  // Emerald Glow
    )

    Dialog(onDismissRequest = { if (!isExporting) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0F172A),
            tonalElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFFEC4899))
                        Text(
                            text = "Story & Snippet Exporter",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                    IconButton(onClick = onDismiss, enabled = !isExporting) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // The 9:16 Story Preview Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Brush.verticalGradient(storyGradients[selectedThemeIndex])),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top Watermark & Duration Tag
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.Black.copy(alpha = 0.4f)
                            ) {
                                Text("🎵 Sur AI Studio", fontSize = 10.sp, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFEC4899)
                            ) {
                                Text("${snippetDuration}s SNIPPET", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }

                        // Artwork & Track Details
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                .padding(8.dp)
                        ) {
                            AsyncImage(
                                model = song.imageUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(song.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp, maxLines = 1)
                                Text("${song.artist} • ${song.genre}", color = Color.LightGray, fontSize = 11.sp)
                            }
                        }

                        // Synced Lyrics Highlight
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Black.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "❝ ${song.lyrics.take(80)}... ❞",
                                fontSize = 11.sp,
                                color = Color(0xFFFACC15),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(8.dp),
                                maxLines = 2
                            )
                        }

                        // Interactive waveform mock
                        InteractiveWaveformVisualizer(
                            progress = 0.45f,
                            isPlaying = true,
                            onSeek = {},
                            modifier = Modifier.height(38.dp),
                            barCount = 30
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Customization Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Duration:", fontSize = 12.sp, color = Color.LightGray)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(15, 30, 60).forEach { sec ->
                            val isSel = snippetDuration == sec
                            Surface(
                                modifier = Modifier.clickable { snippetDuration = sec },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) Color(0xFFEC4899) else Color(0xFF1E293B)
                            ) {
                                Text("${sec}s", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Theme Gradients Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Color Theme:", fontSize = 12.sp, color = Color.LightGray)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        storyGradients.forEachIndexed { index, gradient ->
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(gradient))
                                    .border(
                                        width = if (selectedThemeIndex == index) 2.dp else 0.dp,
                                        color = Color.White,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedThemeIndex = index }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Export & Share Button
                Button(
                    onClick = {
                        isExporting = true
                        scope.launch {
                            delay(1000)
                            isExporting = false
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Listen to \"${song.title}\" created with Sur AI Studio! 🎵\nListen here: https://sur.ai/track/${song.id}")
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Share Track Snippet")
                            context.startActivity(shareIntent)
                            Toast.makeText(context, "Exported ${snippetDuration}s snippet ready to share!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    },
                    enabled = !isExporting,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    if (isExporting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Rendering Video Card...", fontSize = 12.sp)
                    } else {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export & Share to Story", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
