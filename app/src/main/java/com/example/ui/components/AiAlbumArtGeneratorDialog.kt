package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ArtStylePreset(
    val id: String,
    val nameEn: String,
    val nameBn: String,
    val emoji: String,
    val promptSuffix: String,
    val sampleImage: String
)

val ART_STYLE_PRESETS = listOf(
    ArtStylePreset("cyberpunk", "Cyberpunk Neon", "সাইবারপাংক নিয়ন", "⚡", "cyberpunk futuristic neon lights dark atmospheric 8k octane render", "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600"),
    ArtStylePreset("bangla_folk", "Bangla Baul & River", "বাউল ও নদী রূপকথা", "🪕", "bengali folk art serene river village sunset acoustic baul traditional painting", "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600"),
    ArtStylePreset("retro_vinyl", "Retro 80s Vinyl", "রেট্রো ৮০'র ভিনাইল", "📼", "vintage 1980s synthwave vinyl record album cover aesthetic vaporwave chrome", "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600"),
    ArtStylePreset("anime_lofi", "Anime Lofi Chill", "অ্যানিমে লোফাই ভাইব", "☕", "studio ghibli lofi anime cozy rainy aesthetic pastel colors relaxing art", "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=600"),
    ArtStylePreset("mystic_galaxy", "Cosmic Galaxy", "কসমিক গ্যালাক্সি", "🌌", "deep space nebula glittering stars celestial astral cosmic sound waves 3d", "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=600"),
    ArtStylePreset("oil_master", "Vintage Oil Painting", "ক্লাসিক অয়েল পেইন্টিং", "🎨", "rembrandt dramatic oil painting artistic brush strokes expressive emotional", "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=600"),
    ArtStylePreset("3d_render", "3D Surreal Render", "থ্রিডি রেন্ডার আর্ট", "🔮", "surreal 3d metallic holographic sphere iridescent glass reflections blender render", "https://images.unsplash.com/photo-1634017839464-5c339ebe3cb4?w=600")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAlbumArtGeneratorDialog(
    songTitle: String = "My AI Masterpiece",
    genre: String = "Pop",
    onDismiss: () -> Unit,
    onArtApplied: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var prompt by remember { mutableStateOf(songTitle.ifEmpty { "A vibrant melodic soundscape with glowing lights" }) }
    var isGenerating by remember { mutableStateOf(false) }
    var selectedStyle by remember { mutableStateOf(ART_STYLE_PRESETS[0]) }
    var generatedImage by remember { mutableStateOf<String?>(selectedStyle.sampleImage) }
    var selectedLighting by remember { mutableStateOf("Neon Glow") }

    val lightingOptions = listOf("Neon Glow", "Golden Hour", "Dark Moody", "Sunset Silhouette", "Stage Spotlight")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .testTag("ai_album_art_generator_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp)
                            )
                        }
                        Column {
                            Text("AI Album Art Generator", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("কাস্টম এআই কাভার আর্ট স্টুডিও", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Art Preview Canvas with Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.4f))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isGenerating) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                            Text("Generating neural cover art...", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text("Applying ${selectedStyle.nameEn} style • 8K Render", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp)
                        }
                    } else if (generatedImage != null) {
                        AsyncImage(
                            model = generatedImage,
                            contentDescription = "Generated Cover Art",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Gradient footer on preview
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                                    )
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${selectedStyle.emoji} ${selectedStyle.nameEn}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = "HD 4K",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Prompt Input
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    label = { Text("Visual Prompt / কাভার আর্টের বিবরণ") },
                    placeholder = { Text("e.g. Glowing guitar floating in cyberpunk rainy street") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Art Style Selector
                Text(
                    text = "Artistic Style (শিল্পশৈলী নির্বাচন):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ART_STYLE_PRESETS) { style ->
                        val isSelected = selectedStyle.id == style.id
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedStyle = style
                                generatedImage = style.sampleImage
                            },
                            label = {
                                Text("${style.emoji} ${style.nameEn}", fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Lighting Mood Modifiers
                Text(
                    text = "Lighting & Atmosphere:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    lightingOptions.forEach { light ->
                        val isLightSelected = selectedLighting == light
                        Surface(
                            onClick = { selectedLighting = light },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isLightSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, if (isLightSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = light,
                                fontSize = 11.sp,
                                fontWeight = if (isLightSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isLightSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                isGenerating = true
                                delay(1200)
                                val seed = Math.abs((prompt + selectedStyle.id + selectedLighting).hashCode())
                                generatedImage = "https://picsum.photos/seed/$seed/600/600"
                                isGenerating = false
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        enabled = !isGenerating
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Re-roll Art", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val finalUrl = generatedImage ?: selectedStyle.sampleImage
                            onArtApplied(finalUrl)
                            Toast.makeText(context, "Cover Art সফলভাবে যুক্ত করা হয়েছে!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.3f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("কাভার আর্ট ব্যবহার করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

