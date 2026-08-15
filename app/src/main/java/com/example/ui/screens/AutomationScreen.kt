package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomationScreen(
    appLanguage: String = "en",
    autoPosts: List<AutoPostScheduleItem>,
    captions: List<AiCaptionTemplateItem>,
    trends: TrendAnalyticsData,
    autoReplies: List<AutoReplyRuleItem>,
    backupStatus: SmartBackupStatusData,
    crashState: CrashRecoveryStateData
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }

    val categories = listOf(
        "📅 Post Scheduler",
        "✍️ AI Captions",
        "📊 Trend Analyzer",
        "💬 Auto Comment Reply",
        "☁️ Smart Backup",
        "🚨 Crash Recovery"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        Surface(
            color = Color(0xFF1E1B4B),
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
                            Icon(Icons.Default.AutoMode, contentDescription = null, tint = Color(0xFFA855F7))
                            Text(
                                text = if (appLanguage == "bn") "অটোমেশন ও সোশ্যাল বোট" else "Automation & Social Bot Studio",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = if (appLanguage == "bn") "৬টি এডভান্সড অটোমেশন ও ক্যাশ রিকভারি টুলস" else "6 Advanced Automation, Social Posting & Session Recovery Tools",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray
                        )
                    }
                    Badge(containerColor = Color(0xFFA855F7)) {
                        Text("AUTO BOT", modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), color = Color.White, fontWeight = FontWeight.Bold)
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
                                selectedContainerColor = Color(0xFFA855F7),
                                containerColor = Color(0xFF312E81)
                            )
                        )
                    }
                }
            }
        }

        // Body Content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> AutoPostSchedulerTab(autoPosts)
                1 -> AiCaptionWriterTab(captions)
                2 -> TrendAnalyzerTab(trends)
                3 -> AutoReplyCommentsTab(autoReplies)
                4 -> SmartBackupTab(backupStatus)
                5 -> CrashRecoveryTab(crashState)
            }
        }
    }
}

// 1. Auto Post Scheduler
@Composable
private fun AutoPostSchedulerTab(posts: List<AutoPostScheduleItem>) {
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📅 Scheduled Social Media Releases", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Auto-publish rendered 4K music videos to YouTube, TikTok, Facebook & IG", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                posts.forEach { post ->
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(post.trackTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Badge(containerColor = Color(0xFF10B981)) {
                                    Text(post.status, modifier = Modifier.padding(2.dp), color = Color.White)
                                }
                            }
                            Text("Platforms: ${post.targetPlatforms}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            Text("Scheduled Time: ${post.scheduledTime}", fontSize = 11.sp)
                            Text("Notes: ${post.notes}", fontSize = 10.sp, color = Color.Gray)

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { Toast.makeText(context, "Published '${post.trackTitle}' immediately!", Toast.LENGTH_SHORT).show() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(4.dp)
                            ) {
                                Text("Publish Now", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// 2. AI Caption Writer
@Composable
private fun AiCaptionWriterTab(captions: List<AiCaptionTemplateItem>) {
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
                Text("✍️ AI Viral Caption Generator & Hashtags", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(modifier = Modifier.height(12.dp))

                captions.forEach { cap ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(cap.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            Text("Style: ${cap.languageStyle}", fontSize = 10.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(cap.captionContent, fontSize = 12.sp, fontWeight = FontWeight.Medium)

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = { Toast.makeText(context, "Caption copied to clipboard!", Toast.LENGTH_SHORT).show() },
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(2.dp)
                            ) {
                                Text("Copy Caption & Hashtags", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// 3. Trend Analyzer
@Composable
private fun TrendAnalyzerTab(trends: TrendAnalyticsData) {
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
                Text("📊 Music Trend Analyzer & AI Predictions", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(modifier = Modifier.height(10.dp))

                Text("• Top Trending Genre: ${trends.topTrendingGenre}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("• Predicted Next Big Sound: ${trends.predictedNextBigVibe}", fontSize = 12.sp)
                Text("• Viral Trend Match Score: ${trends.trendingScorePercent}%", fontSize = 12.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(12.dp))

                Text("Viral Hashtags List:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                    items(trends.viralHashtags) { tag ->
                        SuggestionChip(
                            onClick = {},
                            label = { Text(tag, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary) }
                        )
                    }
                }
            }
        }
    }
}

// 4. Auto Reply Comments
@Composable
private fun AutoReplyCommentsTab(rules: List<AutoReplyRuleItem>) {
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
                Text("💬 Auto Reply Comments Bot", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Auto-respond to social fans asking for track links or pricing", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                rules.forEach { rule ->
                    var isEnabled by remember { mutableStateOf(rule.isEnabled) }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(rule.triggerPattern, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Switch(
                                    checked = isEnabled,
                                    onCheckedChange = {
                                        isEnabled = it
                                        Toast.makeText(context, "Auto-reply ${if (it) "ENABLED" else "DISABLED"}", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                            Text("Reply Text: ${rule.replyText}", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

// 5. Smart Backup
@Composable
private fun SmartBackupTab(backup: SmartBackupStatusData) {
    val context = LocalContext.current
    var isBackupEnabled by remember { mutableStateOf(backup.isAutoBackupEnabled) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("☁️ Smart Background Auto-Backup", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Auto Backup Active", fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = isBackupEnabled,
                        onCheckedChange = {
                            isBackupEnabled = it
                            Toast.makeText(context, "Auto Backup ${if (it) "ON" else "OFF"}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                Text("• Sync Interval: Every ${backup.backupIntervalHours} Hours", fontSize = 12.sp)
                Text("• Last Backup: ${backup.lastBackupTimestamp}", fontSize = 12.sp)
                Text("• Cloud Synced Studio Projects: ${backup.cloudSyncedProjectsCount} Projects", fontSize = 12.sp, color = Color(0xFF10B981))

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { Toast.makeText(context, "Manual Backup Triggered!", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Backup All Stems Now")
                }
            }
        }
    }
}

// 6. Crash Recovery
@Composable
private fun CrashRecoveryTab(crash: CrashRecoveryStateData) {
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
            colors = CardDefaults.cardColors(containerColor = if (crash.hasUnsavedAutoSaveSession) Color(0xFFFEF2F2) else MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
                    Text("🚨 Crash Recovery & Unsaved Session Auto-Save", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Auto-Saved Session: ${crash.lastSessionName}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Saved At: ${crash.recoveredTimestamp} • Restored Stems: ${crash.restoredAudioTrackCount} Tracks", fontSize = 11.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { Toast.makeText(context, "Unsaved Session Restored into DAW Workstation!", Toast.LENGTH_LONG).show() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Restore Auto-Saved DAW Workspace", color = Color.White)
                }
            }
        }
    }
}
