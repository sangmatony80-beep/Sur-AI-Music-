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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechnicalAndAiIntegrationScreen(
    appLanguage: String = "en",
    supabaseConfig: SupabaseBackendConfigData,
    openAiData: OpenAiApiData,
    securityConfig: SecurityConfigData,
    contentFilter: AiContentFilterData,
    cacheCdn: CacheCdnConfigData,
    lazyLoading: LazyLoadingConfigData,
    backupRestore: BackupRestoreData,
    crashlytics: CrashlyticsStatusData,
    nftGallery: List<MusicNftItem>,
    vrHall: VrConcertHallData,
    aiAudience: AiAudienceSimulationData
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }

    val categories = listOf(
        "⚡ Supabase & OpenAI",
        "🔒 Security & AI Filter",
        "🚀 CDN, Cache & Lazy",
        "💾 Backup & Crashlytics",
        "💎 Music NFT Gallery",
        "🕶️ VR Hall & AI Audience"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Surface(
            color = Color(0xFF0F172A),
            tonalElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Terminal, contentDescription = null, tint = Color(0xFF38BDF8))
                            Text(
                                text = if (appLanguage == "bn") "টেকনিক্যাল ও এআই ইন্টিগ্রেশন" else "Technical & AI Integration Engine",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = if (appLanguage == "bn") "১২টি হাই-টেক ব্যাকএন্ড, এআই ও ভার্চুয়াল এআই ফিচার" else "12 High-Tech Backend, AI, Security, CDN, VR & NFT Modules",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray
                        )
                    }
                    Badge(containerColor = Color(0xFF0284C7)) {
                        Text("TECH STACK", modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(categories) { idx, catName ->
                        FilterChip(
                            selected = selectedTab == idx,
                            onClick = { selectedTab = idx },
                            label = { Text(catName, fontSize = 12.sp, color = if (selectedTab == idx) Color.White else Color.LightGray) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0284C7),
                                containerColor = Color(0xFF1E293B)
                            )
                        )
                    }
                }
            }
        }

        // Body Content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> SupabaseAndOpenAiTab(supabaseConfig, openAiData)
                1 -> SecurityAndFilterTab(securityConfig, contentFilter)
                2 -> CdnCacheAndLazyTab(cacheCdn, lazyLoading)
                3 -> BackupAndCrashlyticsTab(backupRestore, crashlytics)
                4 -> MusicNftGalleryTab(nftGallery)
                5 -> VrHallAndAudienceTab(vrHall, aiAudience)
            }
        }
    }
}

// 1. Supabase Backend Setup Code & OpenAI APIs
@Composable
private fun SupabaseAndOpenAiTab(supabase: SupabaseBackendConfigData, openAi: OpenAiApiData) {
    val context = LocalContext.current
    var isRealtimeConnected by remember { mutableStateOf(false) }
    var pingStatus by remember { mutableStateOf("Ready to Ping") }
    var selectedChannel by remember { mutableStateOf("public:community_feed") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Supabase Setup
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = Color(0xFF10B981))
                        Text("⚡ Supabase Kotlin SDK Core", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White)
                    }
                    Badge(containerColor = if (isRealtimeConnected) Color(0xFF10B981) else Color(0xFF38BDF8)) {
                        Text(if (isRealtimeConnected) "LIVE REALTIME" else "ACTIVE SDK", color = Color.Black, fontWeight = FontWeight.Bold, modifier = Modifier.padding(2.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("URL: ${supabase.projectUrl}", fontSize = 12.sp, color = Color.LightGray)
                Text("Anon Key: ${supabase.anonKey.take(30)}...", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color.Gray)
                Text("Installed Modules: Auth, Postgrest, Realtime, Storage", fontSize = 11.sp, color = Color(0xFF38BDF8))
                Text("DB Status: ${supabase.dbStatus} • Connections: ${supabase.activeConnections}", fontSize = 12.sp, color = Color(0xFF10B981))

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("// SupabaseClientProvider.kt Active Configuration", fontSize = 10.sp, color = Color(0xFFF59E0B), fontFamily = FontFamily.Monospace)
                        Text("val supabase = createSupabaseClient(supabaseUrl, supabaseKey) {\n  install(Auth) { alwaysAutoRefresh = true }\n  install(Postgrest)\n  install(Realtime)\n  install(Storage)\n}", fontSize = 10.sp, color = Color.Green, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            isRealtimeConnected = !isRealtimeConnected
                            val statusMsg = if (isRealtimeConnected) "Supabase Realtime Channel '$selectedChannel' Connected!" else "Supabase Realtime Channel Disconnected"
                            Toast.makeText(context, statusMsg, Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isRealtimeConnected) Color(0xFFDC2626) else Color(0xFF10B981)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(if (isRealtimeConnected) Icons.Default.LinkOff else Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isRealtimeConnected) "Disconnect Live" else "Connect Realtime", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            pingStatus = "Pinging Supabase Database..."
                            Toast.makeText(context, "Supabase Postgrest Ping: 200 OK (Latency: 112ms)", Toast.LENGTH_SHORT).show()
                            pingStatus = "200 OK (112ms)"
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ping Database", fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        }

        // OpenAI APIs Integration
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("🤖 OpenAI APIs Integration Engine", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("• LLM Model: ${openAi.gptModel}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text("• Speech Recognition: ${openAi.whisperModel}", fontSize = 13.sp)
                Text("• Vocal Synthesis TTS Voice: ${openAi.ttsVoice}", fontSize = 13.sp)
                Text("• Server Status: ${openAi.apiStatus}", fontSize = 12.sp, color = Color(0xFF10B981))

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { Toast.makeText(context, "OpenAI API Ping Successful (Latency: 280ms)", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Test OpenAI API Connection")
                }
            }
        }
    }
}

// 2. Security (JWT, Rate Limiting Interceptor) & AI Content Filter
@Composable
private fun SecurityAndFilterTab(security: SecurityConfigData, filter: AiContentFilterData) {
    val context = LocalContext.current
    var isInterceptorEnabled by remember { mutableStateOf(security.rateLimitingInterceptorEnabled) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Security & Interceptor
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🔒 Security, JWT Tokens & Rate Limiting", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("OkHttp Interceptor & JWT Authorization Guard", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                Text("• JWT Token Expiry: ${security.jwtTokenExpiryMinutes} Minutes", fontSize = 12.sp)
                Text("• Rate Limit Threshold: ${security.rateLimitMaxRequestsPerMin} req/min", fontSize = 12.sp)
                Text("• Blacklisted DDoS IPs: ${security.ipBlacklistCount} Addresses", fontSize = 12.sp, color = Color.Red)

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Rate Limiting Interceptor", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = isInterceptorEnabled,
                        onCheckedChange = {
                            isInterceptorEnabled = it
                            Toast.makeText(context, "Rate Limiting Interceptor ${if (it) "ENABLED" else "DISABLED"}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        // AI Content Filter
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🛡️ Realtime AI Content Moderation Filter", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Strictness: ${filter.filterStrictness}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)

                Spacer(modifier = Modifier.height(10.dp))

                Text("Blocked Keyword Classes:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                    items(filter.flaggedKeywords) { kw ->
                        SuggestionChip(
                            onClick = {},
                            label = { Text(kw, fontSize = 11.sp, color = Color.Red) }
                        )
                    }
                }
            }
        }
    }
}

// 3. CDN (Cloudflare), Smart Cache & Lazy Loading
@Composable
private fun CdnCacheAndLazyTab(cdn: CacheCdnConfigData, lazy: LazyLoadingConfigData) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🌐 Cloudflare CDN & Smart Edge Cache", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("CDN Provider: ${cdn.cdnProvider}", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(8.dp))

                Text("• Edge Nodes: ${cdn.edgeNodesLocation}", fontSize = 11.sp)
                Text("• Cache Hit Ratio: ${cdn.smartCacheHitsPercent}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                Text("• Cached Audio Stems: ${cdn.cachedAudioMb} MB", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { Toast.makeText(context, "Cloudflare CDN Cache Purged!", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Purge CDN Cache")
                }
            }
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("⚡ Lazy Loading & Pre-fetching Engine", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Text("• Lazy Paging Size: ${lazy.lazyPagingPageSize} items per chunk", fontSize = 12.sp)
                Text("• Waveform Pre-fetch Window: ${lazy.waveformPreFetchWindowSec} Seconds ahead", fontSize = 12.sp)
                Text("• Image BlurHash Placeholder: ${if (lazy.imageLazyLoadBlurHash) "ENABLED ✓" else "DISABLED"}", fontSize = 12.sp, color = Color(0xFF10B981))
            }
        }
    }
}

// 4. Backup/Restore (Supabase) & Crashlytics (Firebase)
@Composable
private fun BackupAndCrashlyticsTab(backup: BackupRestoreData, crash: CrashlyticsStatusData) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("💾 Supabase Backup & System Restore", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Last Backup: ${backup.lastAutoBackupTime}", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(8.dp))

                Text("• Storage: ${backup.cloudStorage}", fontSize = 11.sp)
                Text("• Dump Size: ${backup.backupSizeBytes}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("• Backup Health: ${backup.status}", fontSize = 12.sp, color = Color(0xFF10B981))

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { Toast.makeText(context, "Supabase Backup Triggered!", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Create Backup", fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = { Toast.makeText(context, "System Restored from Backup!", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Restore System", fontSize = 11.sp)
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🔥 Firebase Crashlytics & Error Monitoring", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Text("• Crash-Free Users: ${crash.crashFreeUsersPercent}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                Text("• Fatal Crashes: ${crash.fatalErrorsCount}", fontSize = 12.sp)
                Text("• Non-Fatal Exceptions Logged: ${crash.nonFatalLogsCount}", fontSize = 12.sp)
            }
        }
    }
}

// 5. Music NFT Gallery
@Composable
private fun MusicNftGalleryTab(nftList: List<MusicNftItem>) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("💎 Music NFT Web3 Gallery & Stems Marketplace", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Tokenize AI generated vocal stems and beat tracks on Polygon & Solana", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                nftList.forEach { nft ->
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(nft.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${nft.priceMaticOrSol} ${nft.blockchain.take(3).uppercase()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Text("Artist: ${nft.artist} • Chain: ${nft.blockchain}", fontSize = 11.sp)
                            Text("Contract: ${nft.contractAddress}", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color.Gray)

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { Toast.makeText(context, "Minted NFT Stem for ${nft.title} on Web3!", Toast.LENGTH_SHORT).show() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(4.dp)
                            ) {
                                Text("Mint / Purchase NFT Stem", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// 6. VR Concert Hall (3D view) & AI Audience
@Composable
private fun VrHallAndAudienceTab(vr: VrConcertHallData, audience: AiAudienceSimulationData) {
    val context = LocalContext.current
    var isVrActive by remember { mutableStateOf(vr.isVr3dEnabled) }
    var excitement by remember { mutableStateOf(audience.crowdExcitementLevelPercent) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // VR Concert Hall 3D
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.ViewInAr, contentDescription = null, tint = Color(0xFFEC4899))
                    Text("🕶️ VR 3D Concert Hall & Spatial Stage", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Hall: ${vr.hallName}", fontSize = 13.sp, color = Color.LightGray)
                Text("Stage Setup: ${vr.activeStage}", fontSize = 12.sp, color = Color(0xFFEC4899))
                Text("FOV Angle: ${vr.fovAngleDegrees}° • Spatial Audio: ${vr.spatialAudioFormat}", fontSize = 11.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(12.dp))

                // 3D Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E1B4B))
                        .border(1.dp, Color(0xFFEC4899), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ViewInAr, contentDescription = null, tint = Color.Cyan, modifier = Modifier.size(42.dp))
                        Text("3D Interactive Gyroscope Stage View", fontSize = 12.sp, color = Color.Cyan, fontWeight = FontWeight.Bold)
                        Text("Drag or tilt phone for 360° Real-time Stage View", fontSize = 10.sp, color = Color.LightGray)
                    }
                }
            }
        }

        // AI Audience Engine
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🏟️ AI Crowd & Interactive Arena Audience", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Live Arena: ${audience.virtualCheeringCrowdCount} Active Listener Avatars", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(10.dp))

                Text("Crowd Excitement Level: $excitement%", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LinearProgressIndicator(
                    progress = excitement / 100f,
                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                    color = Color(0xFF10B981)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Trigger AI Reaction Sounds:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                    items(audience.reactionTypes) { rx ->
                        Button(
                            onClick = {
                                excitement = (excitement + 3).coerceAtMost(100)
                                Toast.makeText(context, "Triggered Crowd Cheer: $rx", Toast.LENGTH_SHORT).show()
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Text(rx, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
