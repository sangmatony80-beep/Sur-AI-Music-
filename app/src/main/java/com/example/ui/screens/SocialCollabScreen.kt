package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.repository.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialCollabScreen(
    appLanguage: String,
    posts: List<SocialTikTokFeedPost>,
    trending: List<TrendingTrackItem>,
    profile: SocialUserProfile,
    collabSessions: List<SupabaseCollabSession>,
    jamRooms: List<LiveJamRoomState>,
    battleState: LyricsBattleState,
    duets: List<DuetVideoItem>,
    remixes: List<RemixStemItem>,
    playlists: List<PlaylistData>,
    fanTiers: List<FanClubTierItem>,
    liveStream: LiveStreamStage,
    songRequests: List<SongRequestData>,
    gigs: List<CollabMarketplaceGig>,
    contest: CoverContestData,
    initialTab: Int = 0
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(initialTab) }

    // Tab names mapping 15 features across 6 top category groups
    val categories = listOf(
        "🔥 Public Feed & TikTok",
        "📈 Trending & Charts",
        "👤 Profile & Follow",
        "⚡ Realtime & Jam",
        "⚔️ Lyrics Battle & Duet",
        "🎛️ Remix & Playlists",
        "👑 Fan Club & Live",
        "💼 Marketplace & Contest"
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
                            text = if (appLanguage == "bn") "সোশ্যাল ও কোলাব স্টুডিও" else "Social & Collaboration",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (appLanguage == "bn") "১৫টি সম্পূর্ণ কমিউনিটি ও লাইভ ফিচার" else "15 Complete Community & Live Features",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                        Text("Supabase & WebRTC", modifier = Modifier.padding(2.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable category tabs
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(categories) { index, catName ->
                        FilterChip(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
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

        // Tab Content Display
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> TikTokFeedSubView(posts)
                1 -> TrendingPageSubView(trending)
                2 -> UserProfileAndFollowSubView(profile)
                3 -> RealtimeCollabAndJamSubView(collabSessions, jamRooms)
                4 -> LyricsBattleAndDuetSubView(battleState, duets)
                5 -> RemixAndPlaylistSubView(remixes, playlists)
                6 -> FanClubAndLiveStreamSubView(fanTiers, liveStream, songRequests)
                7 -> MarketplaceAndContestSubView(gigs, contest)
            }
        }
    }
}

// 1. TikTok Style Public Feed
@Composable
private fun TikTokFeedSubView(posts: List<SocialTikTokFeedPost>) {
    val context = LocalContext.current
    var likedState by remember { mutableStateOf(posts.associate { it.id to it.isLiked }) }
    var likesCountState by remember { mutableStateOf(posts.associate { it.id to it.likesCount }) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(posts) { post ->
            val isLiked = likedState[post.id] ?: false
            val currentLikes = likesCountState[post.id] ?: post.likesCount

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Creator Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = post.avatarUrl,
                            contentDescription = post.artistName,
                            modifier = Modifier.size(40.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(post.artistName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(post.hashtag, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Button(
                            onClick = { Toast.makeText(context, "Followed ${post.artistName}!", Toast.LENGTH_SHORT).show() },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("+ Follow", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Media Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        AsyncImage(
                            model = post.coverUrl,
                            contentDescription = post.songTitle,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))))
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp)
                        ) {
                            Text(post.songTitle, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                            Text(post.lyricsSnippet, color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Social Action Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            IconButton(onClick = {
                                val newLiked = !isLiked
                                likedState = likedState.toMutableMap().apply { put(post.id, newLiked) }
                                likesCountState = likesCountState.toMutableMap().apply { put(post.id, if (newLiked) currentLikes + 1 else currentLikes - 1) }
                            }) {
                                Icon(
                                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Like",
                                    tint = if (isLiked) Color.Red else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text("$currentLikes", modifier = Modifier.align(Alignment.CenterVertically), fontSize = 13.sp)

                            IconButton(onClick = { Toast.makeText(context, "Comment modal opened", Toast.LENGTH_SHORT).show() }) {
                                Icon(Icons.Default.Comment, contentDescription = "Comment")
                            }
                            Text("${post.commentsCount}", modifier = Modifier.align(Alignment.CenterVertically), fontSize = 13.sp)

                            IconButton(onClick = { Toast.makeText(context, "Link copied to share!", Toast.LENGTH_SHORT).show() }) {
                                Icon(Icons.Default.Share, contentDescription = "Share")
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { Toast.makeText(context, "Opening Remix Stem Studio...", Toast.LENGTH_SHORT).show() },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Remix (${post.remixCount})", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// 2. Trending Page
@Composable
private fun TrendingPageSubView(trending: List<TrendingTrackItem>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Sur AI Top 50 Global Charts", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Updated hourly based on plays, remixes & duet interactions", fontSize = 12.sp)
                    }
                }
            }
        }

        items(trending) { track ->
            ListItem(
                headlineContent = { Text("#${track.rank} ${track.title}", fontWeight = FontWeight.Bold) },
                supportingContent = { Text("${track.artist} • ${track.category} • ${track.playCount} Plays") },
                trailingContent = {
                    Surface(
                        color = Color(0xFF10B981).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("+${track.trendPercent}%", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                },
                leadingContent = {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("${track.rank}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            )
            Divider()
        }
    }
}

// 3. User Profile & Follow System
@Composable
private fun UserProfileAndFollowSubView(profile: SocialUserProfile) {
    val context = LocalContext.current
    var isFollowing by remember { mutableStateOf(false) }

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
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = profile.avatarUrl,
                    contentDescription = profile.username,
                    modifier = Modifier.size(80.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(profile.username, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    if (profile.verifiedBadge) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = Color(0xFF3B82F6), modifier = Modifier.size(18.dp))
                    }
                }
                Text(profile.handle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Text(profile.bio, textAlign = TextAlign.Center, fontSize = 13.sp)

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${profile.followersCount + if (isFollowing) 1 else 0}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Followers", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${profile.followingCount}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Following", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${profile.totalPlaysCount / 1000}K", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Total Plays", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        isFollowing = !isFollowing
                        Toast.makeText(context, if (isFollowing) "Followed Profile!" else "Unfollowed Profile", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFollowing) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(if (isFollowing) "✓ Following" else "+ Follow Creator")
                }
            }
        }

        // Badges Card
        Card(
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Creator Achievements & Badges", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(profile.topBadges) { badge ->
                        AssistChip(
                            onClick = {},
                            label = { Text(badge, fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFF59E0B)) }
                        )
                    }
                }
            }
        }
    }
}

// 4 & 5. Realtime Collaboration & WebRTC Live Jam Mode
@Composable
private fun RealtimeCollabAndJamSubView(
    sessions: List<SupabaseCollabSession>,
    jamRooms: List<LiveJamRoomState>
) {
    val context = LocalContext.current
    var activeRoomCode by remember { mutableStateOf("JAM-8821") }
    var selectedInstrument by remember { mutableStateOf("Lead Synth") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Supabase Realtime Session Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Supabase Realtime DAW Sync", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Badge(containerColor = Color(0xFF10B981)) { Text("ONLINE", modifier = Modifier.padding(2.dp)) }
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = activeRoomCode,
                    onValueChange = { activeRoomCode = it },
                    label = { Text("Room Code") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { Toast.makeText(context, "Joined Room $activeRoomCode", Toast.LENGTH_SHORT).show() }) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "Join")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Active Collaborators & Live Action Stream:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                sessions.firstOrNull()?.recentEvents?.forEach { evt ->
                    Text("• $evt", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // WebRTC Live Jam Stage
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🎙️ WebRTC Low-Latency Live Jam Stage", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("12ms Latency", fontSize = 11.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Select Your Instrument Slot:", fontSize = 12.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("Lead Synth", "Electric Guitar", "Drums/808", "Vocal Mic", "Dotara/Folk")) { inst ->
                        FilterChip(
                            selected = selectedInstrument == inst,
                            onClick = { selectedInstrument = inst },
                            label = { Text(inst, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { Toast.makeText(context, "WebRTC Audio Channel Active on $selectedInstrument", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Live Jam Broadcast")
                }
            }
        }
    }
}

// 7 & 8. Lyrics Battle & Duet Challenge
@Composable
private fun LyricsBattleAndDuetSubView(
    battle: LyricsBattleState,
    duets: List<DuetVideoItem>
) {
    val context = LocalContext.current
    var p1Votes by remember { mutableStateOf(battle.votesP1) }
    var p2Votes by remember { mutableStateOf(battle.votesP2) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Live Lyrics 1v1 Battle Arena
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("⚔️ Live 1v1 Verse Battle Arena", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Round ${battle.currentRound} • Timer: ${battle.timeLeftSec}s remaining", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Player 1
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(battle.player1Name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Rhyme Score: ${battle.p1RhymesScore}/100", fontSize = 12.sp, color = Color(0xFF10B981))
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { p1Votes++ },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Vote ($p1Votes)", fontSize = 12.sp)
                        }
                    }

                    Text("VS", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, modifier = Modifier.align(Alignment.CenterVertically))

                    // Player 2
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(battle.player2Name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Rhyme Score: ${battle.p2RhymesScore}/100", fontSize = 12.sp, color = Color(0xFF10B981))
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { p2Votes++ },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Vote ($p2Votes)", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Duet Challenge
        Card(
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🎤 Duet Challenge Studio", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Split-screen video duet recorder with vocal balance offset", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                duets.forEach { duet ->
                    ListItem(
                        headlineContent = { Text(duet.originalSongTitle, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("Original by ${duet.originalArtist} • Status: ${duet.duetStatus}") },
                        trailingContent = {
                            OutlinedButton(
                                onClick = { Toast.makeText(context, "Opening Split Screen Duet Cam...", Toast.LENGTH_SHORT).show() }
                            ) {
                                Text("Duet Now", fontSize = 11.sp)
                            }
                        }
                    )
                    Divider()
                }
            }
        }
    }
}

// 9 & 10. Remix & Playlist Maker
@Composable
private fun RemixAndPlaylistSubView(
    remixes: List<RemixStemItem>,
    playlists: List<PlaylistData>
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stem Remix Tree
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🎛️ Stem Flip & Remix Feed", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Tracks created from public stems with parent track attribution", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                remixes.forEach { r ->
                    ListItem(
                        headlineContent = { Text(r.remixerName, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("${r.stemFlipped} • ${r.newBpm} BPM • ${r.newGenre}") },
                        trailingContent = {
                            IconButton(onClick = { Toast.makeText(context, "Loaded remix into DAW", Toast.LENGTH_SHORT).show() }) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                            }
                        }
                    )
                    Divider()
                }
            }
        }

        // Playlist Maker
        Card(
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🎧 Custom Playlists", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Button(
                        onClick = { Toast.makeText(context, "New Playlist Created", Toast.LENGTH_SHORT).show() },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("+ Create", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                playlists.forEach { pl ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = pl.coverUrl,
                            contentDescription = pl.title,
                            modifier = Modifier.size(50.dp).clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(pl.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${pl.trackCount} Tracks • ${if (pl.isPublic) "Public" else "Private"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.MoreVert, contentDescription = null)
                    }
                }
            }
        }
    }
}

// 11, 12, 13. Fan Club, Live Streaming & Song Requests
@Composable
private fun FanClubAndLiveStreamSubView(
    fanTiers: List<FanClubTierItem>,
    liveStream: LiveStreamStage,
    songRequests: List<SongRequestData>
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Live Stream Virtual Stage
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Badge(containerColor = Color.Red) { Text("LIVE", modifier = Modifier.padding(2.dp)) }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(liveStream.streamerName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Text("${liveStream.viewerCount} Viewers", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(liveStream.liveTitle, fontSize = 13.sp)

                Spacer(modifier = Modifier.height(12.dp))

                // Chat Stream Box
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        liveStream.recentChat.forEach { chat ->
                            Text(chat, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { Toast.makeText(context, "Sent 50 Gems to Streamer! 💎", Toast.LENGTH_SHORT).show() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                    ) {
                        Text("💎 Send Gift", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = { Toast.makeText(context, "Heart reaction floating!", Toast.LENGTH_SHORT).show() }
                    ) {
                        Text("❤️ React", fontSize = 12.sp)
                    }
                }
            }
        }

        // Fan Club Tier Subscriptions
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("👑 Creator Fan Club Tiers", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Exclusive subscriber perks, WAV downloads & direct VIP access", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                fanTiers.forEach { tier ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tier.tierName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${tier.subscriberCount} Supporters", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Button(
                                onClick = { Toast.makeText(context, "Subscribed to ${tier.tierName}!", Toast.LENGTH_SHORT).show() }
                            ) {
                                Text("\$${tier.priceMonthly}/mo", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Song Request Queue Box
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📮 Live Song Request Box", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                songRequests.forEach { req ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(req.requestTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("By ${req.requesterName} • Tip: ${req.tipCoins} Coins", fontSize = 11.sp)
                        }
                        Badge { Text(req.status, modifier = Modifier.padding(2.dp)) }
                    }
                    Divider()
                }
            }
        }
    }
}

// 14 & 15. Collaboration Marketplace & Cover Contest
@Composable
private fun MarketplaceAndContestSubView(
    gigs: List<CollabMarketplaceGig>,
    contest: CoverContestData
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cover Contest Banner
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🏆 ${contest.contestName}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Badge(containerColor = Color(0xFFF59E0B)) { Text("\$${contest.prizePoolUSD} Pool", modifier = Modifier.padding(2.dp)) }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("${contest.submissionsCount} Submissions • ${contest.deadlineDays} Days Left", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))
                Text("Leaderboard Top Performers:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                contest.topLeaderboard.forEach { lb ->
                    Text("• $lb", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { Toast.makeText(context, "Cover Submitted to Contest!", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Submit Your Cover Song")
                }
            }
        }

        // Collaboration Marketplace
        Card(
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("💼 Collaboration Marketplace", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Hire producers, vocalists & audio mix engineers with escrow protection", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                gigs.forEach { gig ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(gig.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                Text("\$${gig.budgetUSD}", fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981), fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Role: ${gig.role} • Rating: ⭐ ${gig.sellerRating}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                OutlinedButton(
                                    onClick = { Toast.makeText(context, "Proposal Submitted for ${gig.title}", Toast.LENGTH_SHORT).show() },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Apply", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
