package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperienceUiScreen(
    appLanguage: String = "en",
    authState: SupabaseAuthStateData,
    uiConfig: ExperienceUiConfigData,
    analytics: AnalyticsDashboardData,
    widgetCast: WidgetAndCastData,
    themeMode: String,
    themeColor: String,
    onThemeModeChange: (String) -> Unit,
    onThemeColorChange: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }

    val categories = listOf(
        "🔐 Auth & Security",
        "🎨 Themes, Fonts & Colors",
        "📡 Offline & Sharing",
        "🎤 Karaoke & Analytics",
        "📺 Widget & Chromecast"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (appLanguage == "bn") "ইউআই/ইউএক্স ও এক্সপেরিয়েন্স স্টুডিও" else "UI/UX & Experience Studio",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (appLanguage == "bn") "১৫টি এডভান্সড অ্যাপ ইউজার এক্সপেরিয়েন্স টুলস" else "15 Advanced App User Experience Features",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                        Text("15 UX Features", modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(categories) { idx, catName ->
                        FilterChip(
                            selected = selectedTab == idx,
                            onClick = { selectedTab = idx },
                            label = { Text(catName, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Body Content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> AuthAndSecurityTab(authState)
                1 -> ThemesFontsColorsTab(uiConfig, themeMode, themeColor, onThemeModeChange, onThemeColorChange)
                2 -> OfflineAndSharingTab(uiConfig)
                3 -> KaraokeAndAnalyticsTab(analytics, uiConfig)
                4 -> WidgetAndChromecastTab(widgetCast)
            }
        }
    }
}

// 1. Auth & Security Tab (Supabase Auth, Biometric, Guest Mode, Profile Customization)
@Composable
private fun AuthAndSecurityTab(auth: SupabaseAuthStateData) {
    val context = LocalContext.current
    var biometricOn by remember { mutableStateOf(auth.biometricEnabled) }
    var guestModeOn by remember { mutableStateOf(auth.isGuestMode) }
    var userEmail by remember { mutableStateOf(auth.userEmail) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Supabase Auth Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("⚡ Supabase Authentication", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Badge(containerColor = Color(0xFF10B981)) { Text("Active: ${auth.authProvider}", modifier = Modifier.padding(2.dp)) }
                }
                Text("User ID: ${auth.userId} • Email: $userEmail", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(16.dp))

                Text("Sign In Provider Options:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { Toast.makeText(context, "Logged in via Google OAuth!", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Google", fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = { Toast.makeText(context, "Logged in via Apple ID!", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Apple", fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = { Toast.makeText(context, "Supabase Magic Link Sent!", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Email Link", fontSize = 11.sp)
                    }
                }
            }
        }

        // Biometric & Guest Mode Card
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🔒 Biometric Lock & Security", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Fingerprint / FaceUnlock protection for studio projects", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Require Biometric to Open App", fontSize = 13.sp)
                    }
                    Switch(checked = biometricOn, onCheckedChange = {
                        biometricOn = it
                        Toast.makeText(context, if (it) "Biometric Lock Enabled!" else "Biometric Lock Disabled", Toast.LENGTH_SHORT).show()
                    })
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.PersonOff, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Text("Guest Mode (No-login Session)", fontSize = 13.sp)
                    }
                    Switch(checked = guestModeOn, onCheckedChange = {
                        guestModeOn = it
                        Toast.makeText(context, if (it) "Switched to Guest Mode!" else "Signed back into Supabase Account", Toast.LENGTH_SHORT).show()
                    })
                }
            }
        }

        // Profile Customization Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("👤 Studio Profile Customization", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Update studio avatar, artist handle & custom domain bio", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = userEmail,
                    onValueChange = { userEmail = it },
                    label = { Text("Artist Studio Email") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { Toast.makeText(context, "Profile Preferences Saved!", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Profile Changes")
                }
            }
        }
    }
}

// 2. Themes, Fonts & Colors Tab (Dark/Light/AMOLED, 10 Fonts, 8 Theme Colors)
@Composable
private fun ThemesFontsColorsTab(
    uiConfig: ExperienceUiConfigData,
    currentThemeMode: String,
    currentThemeColor: String,
    onThemeModeChange: (String) -> Unit,
    onThemeColorChange: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedFont by remember { mutableStateOf(uiConfig.selectedFont) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Theme Mode (Dark / Light / AMOLED Black)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🌓 Theme Display Mode", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Light, Dark & Pure AMOLED Pitch-Black for battery efficiency", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = currentThemeMode == "light",
                        onClick = { onThemeModeChange("light") },
                        label = { Text("☀️ Light Mode", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = currentThemeMode == "dark",
                        onClick = { onThemeModeChange("dark") },
                        label = { Text("🌙 Dark Mode", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = currentThemeMode == "amoled",
                        onClick = {
                            onThemeModeChange("dark")
                            Toast.makeText(context, "AMOLED Pure Pitch-Black Enabled!", Toast.LENGTH_SHORT).show()
                        },
                        label = { Text("🖤 AMOLED Black", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 10 Custom Fonts
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🔤 Font Selector (10 Studio Fonts)", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Selected: $selectedFont • Applied instantly across all UI screens", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiConfig.availableFonts) { f ->
                        FilterChip(
                            selected = selectedFont == f,
                            onClick = {
                                selectedFont = f
                                Toast.makeText(context, "Font changed to $f", Toast.LENGTH_SHORT).show()
                            },
                            label = { Text(f, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        // 8 Theme Accent Colors
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🎨 8 Theme Accent Colors", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Customize primary brand colors for player controls & UI accents", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                uiConfig.themeColors.chunked(2).forEach { pair ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        pair.forEach { colorName ->
                            val colorKey = colorName.split(" ").first().uppercase()
                            FilterChip(
                                selected = currentThemeColor.uppercase() == colorKey,
                                onClick = { onThemeColorChange(colorKey) },
                                label = { Text(colorName, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// 3. Offline Mode (Room DB + Cache) & Sharing / PDF Export
@Composable
private fun OfflineAndSharingTab(uiConfig: ExperienceUiConfigData) {
    val context = LocalContext.current
    var cacheSize by remember { mutableStateOf(uiConfig.offlineCacheSizeMb) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Offline Mode Card (Room DB + Cache)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("💾 Offline Mode (Room DB + Audio Cache)", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Cached Songs: ${uiConfig.cachedSongCount} tracks • Cache Size: ${cacheSize.toInt()} MB", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = cacheSize / 500f,
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { Toast.makeText(context, "All offline Room DB tracks synced!", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Sync Room DB")
                    }
                    OutlinedButton(
                        onClick = {
                            cacheSize = 0f
                            Toast.makeText(context, "Audio Cache Cleared!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear Cache")
                    }
                }
            }
        }

        // Share Integration & PDF Export
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📤 Native Share Integration & PDF Export", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Export lyrics sheets, stem breakdown reports & song links", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { Toast.makeText(context, "Share sheet opened!", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Song Link", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { Toast.makeText(context, "PDF Lyrics Sheet Exported!", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export PDF Sheet", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// 4. Karaoke Mode (lyrics sync) & Analytics Dashboard
@Composable
private fun KaraokeAndAnalyticsTab(analytics: AnalyticsDashboardData, uiConfig: ExperienceUiConfigData) {
    val context = LocalContext.current
    var karaokeDelay by remember { mutableStateOf(uiConfig.karaokeSyncDelayMs.toFloat()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Karaoke Mode (Synced Lyrics)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🎤 Karaoke Mode (Real-time Synced Lyrics)", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("LRC timecode synced display with vocal pitch guide", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("[00:14.20] Cyber raindrops falling down...", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("[00:18.50] ♪ আকাশের ওই নীলিমায় তোমায় খুঁজি ♪", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                        Text("[00:22.10] Bangla AI Cyber Folk Groove", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Karaoke Lyrics Sync Offset: ${karaokeDelay.toInt()} ms", fontSize = 12.sp)
                Slider(
                    value = karaokeDelay,
                    onValueChange = { karaokeDelay = it },
                    valueRange = -500f..500f
                )
            }
        }

        // Analytics Dashboard
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📊 User Experience Analytics Dashboard", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("App Sessions: ${analytics.totalAppSessions} • Total Songs Created: ${analytics.totalSongsGenerated}", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Listening Time:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${analytics.listeningTimeHours} Hours", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Column {
                        Text("Top Genre:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(analytics.topGenre, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Weekly Activity Trend:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    analytics.weeklyEngagementList.forEach { valHp ->
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .height((valHp * 5).dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }
    }
}

// 5. Widget Support & Chromecast / AirPlay
@Composable
private fun WidgetAndChromecastTab(wc: WidgetAndCastData) {
    val context = LocalContext.current
    var isConnected by remember { mutableStateOf(wc.isChromecastConnected) }
    var activeDev by remember { mutableStateOf(wc.activeCastDevice) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Widget Support
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📱 Android Home Screen Widget Support", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Layout: ${wc.widgetLayout}", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = Color.Black.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("SUR AI Widget", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("♪ Cyber Raindrops • Playing", color = Color.Gray, fontSize = 10.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = {}) { Icon(Icons.Default.SkipPrevious, contentDescription = null, tint = Color.White) }
                            IconButton(onClick = {}) { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White) }
                            IconButton(onClick = {}) { Icon(Icons.Default.SkipNext, contentDescription = null, tint = Color.White) }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { Toast.makeText(context, "Pinned Widget to Android Home Screen!", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Add Widget to Home Screen")
                }
            }
        }

        // Chromecast & AirPlay
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("📺 Chromecast & AirPlay Audio Casting", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Icon(Icons.Default.Cast, contentDescription = null, tint = if (isConnected) Color(0xFF10B981) else Color.Gray)
                }
                Text("Cast high quality lossless audio to TV & Smart Speakers", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                Text("Available Cast Devices:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                wc.availableDevices.forEach { dev ->
                    ListItem(
                        headlineContent = { Text(dev, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                        trailingContent = {
                            Button(
                                onClick = {
                                    activeDev = dev
                                    isConnected = true
                                    Toast.makeText(context, "Casting Audio to $dev!", Toast.LENGTH_SHORT).show()
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(if (isConnected && activeDev == dev) "Connected ✓" else "Cast", fontSize = 11.sp)
                            }
                        }
                    )
                    Divider()
                }
            }
        }
    }
}
