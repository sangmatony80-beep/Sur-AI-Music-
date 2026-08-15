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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    appLanguage: String = "en",
    beats: List<MarketplaceBeatItem> = emptyList(),
    lyrics: List<MarketplaceLyricsItem> = emptyList(),
    nftState: PolygonNftMintState = PolygonNftMintState("0x71C...89aF", "ERC-1155 Audio NFT", 32.5f, 1.5f, "0x3Aa9821f00b92138a44d8234190c", "0x98f...21cb"),
    courses: List<MasterclassCourseItem> = emptyList(),
    whiteLabel: WhiteLabelConfigData = WhiteLabelConfigData("Sur AI Studio Pro", "studio.mybrandmusic.com", "#8B5CF6", "wt_live_9921_x884a22b001", true),
    apiDashboard: SurAiApiDashboardData = SurAiApiDashboardData("sur_live_sk_88291_a9b8c7d6e5", "Developer Pro Plan", 14200, 50000, listOf("https://api.myapp.com/webhooks/song-ready")),
    commercialLicense: CommercialLicenseData = CommercialLicenseData("LIC-2026-BANGLA-99812", "Cyber Raindrops (Bangla AI Remix)", "Tanvir Music Productions Ltd.", "US-SUR-26-00129", "100% Commercial Sync & Streaming Rights", "2026-08-09"),
    spotifyDist: SpotifyDistributionData = SpotifyDistributionData("Bangla AI Cyber Folk Vol. 1", "889012345678", "DISPATCHED_TO_STORES", listOf("Spotify", "Apple Music", "YouTube Music", "Amazon Music"), "2026-08-15"),
    referralData: ReferralProgramData = ReferralProgramData("SUR-DESHI-2026", "https://sur.ai/ref/SUR-DESHI-2026", 20, 38, 184.50f, 620.00f),
    affiliateData: AffiliateProgramData = AffiliateProgramData("Pro Partner (30% Commission)", "DESHI30OFF", 1420, 85, 5.98f, 450.00f),
    sponsorships: List<SponsorshipDealItem> = emptyList(),
    tipJar: TipJarStateData = TipJarStateData("@sur_artist_official", 340.50f, listOf("Rony sent \$10.00", "Tanvir sent \$25.00"), listOf("bKash", "Nagad", "Credit Card", "Crypto"))
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }

    val categories = listOf(
        "🎹 Beat & Lyrics Store",
        "⛓️ Web3 NFT & Licenses",
        "🎓 Courses & White Label",
        "🔑 Dev API & Spotify",
        "💸 Referral & Affiliate",
        "🤝 Sponsorship & Tip Jar"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
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
                            text = if (appLanguage == "bn") "বিজনেস ও মার্কেটপ্লেস" else "Business & Marketplace",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (appLanguage == "bn") "১২টি ব্যবসায়িক ও মনিটাইজেশন টুল" else "12 Monetization & Enterprise Features",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Badge(containerColor = Color(0xFF10B981)) {
                        Text("Monetize", modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), color = Color.White)
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

        // Tab Content Area
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> BeatAndLyricsStoreTab(beats, lyrics)
                1 -> NftAndCommercialLicenseTab(nftState, commercialLicense)
                2 -> CoursesAndWhiteLabelTab(courses, whiteLabel)
                3 -> DeveloperApiAndSpotifyTab(apiDashboard, spotifyDist)
                4 -> ReferralAndAffiliateTab(referralData, affiliateData)
                5 -> SponsorshipAndTipJarTab(sponsorships, tipJar)
            }
        }
    }
}

// 1. Beat & Lyrics Store SubView
@Composable
private fun BeatAndLyricsStoreTab(
    beats: List<MarketplaceBeatItem>,
    lyrics: List<MarketplaceLyricsItem>
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Beats Marketplace
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🎹 Beat Marketplace (Buy/Sell)", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Browse WAV stems, leases & exclusive rights for instrumental beats", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(12.dp))

                beats.forEach { beat ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(beat.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${beat.producerName} • ${beat.bpm} BPM • Key: ${beat.scaleKey} • ${beat.genre}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Button(
                                    onClick = { Toast.makeText(context, "Lease purchased for \$${beat.leasePriceUSD}!", Toast.LENGTH_SHORT).show() },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Lease \$${beat.leasePriceUSD}", fontSize = 11.sp)
                                }
                                OutlinedButton(
                                    onClick = { Toast.makeText(context, "Exclusive License offer sent: \$${beat.exclusivePriceUSD}", Toast.LENGTH_SHORT).show() },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Exclusive \$${beat.exclusivePriceUSD}", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Lyrics Store
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📝 Original Lyrics Marketplace", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("100% Copyright-checked song lyrics for artists & producers", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                lyrics.forEach { l ->
                    ListItem(
                        headlineContent = { Text(l.title, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("By ${l.authorName} • Language: ${l.language} • Rights: ${l.rightsType}") },
                        trailingContent = {
                            Button(
                                onClick = { Toast.makeText(context, "Lyrics Buyout Successful: \$${l.priceUSD}", Toast.LENGTH_SHORT).show() }
                            ) {
                                Text("\$${l.priceUSD}", fontSize = 12.sp)
                            }
                        },
                        leadingContent = {
                            if (l.copyrightVerified) {
                                Icon(Icons.Default.Verified, contentDescription = "Verified", tint = Color(0xFF3B82F6))
                            }
                        }
                    )
                    Divider()
                }
            }
        }
    }
}

// 2. Web3 NFT Minting & Commercial License Generator
@Composable
private fun NftAndCommercialLicenseTab(
    nft: PolygonNftMintState,
    license: CommercialLicenseData
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Web3 Polygon NFT Minting Engine
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
                        Icon(Icons.Default.Token, contentDescription = null, tint = Color(0xFF8B5CF6))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("⛓️ Polygon NFT Audio Mint Engine", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Badge(containerColor = Color(0xFF8B5CF6)) { Text("Polygon POS", modifier = Modifier.padding(2.dp)) }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Wallet Connected: ${nft.connectedWallet}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("Standard: ${nft.tokenStandard} • Gas Fee: ${nft.gasFeeGwei} Gwei", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Smart Contract: ${nft.contractAddress}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { Toast.makeText(context, "Minting Audio NFT on Polygon POS Network...", Toast.LENGTH_LONG).show() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                ) {
                    Text("Mint Audio Track NFT (${nft.mintPriceMatic} MATIC)")
                }
            }
        }

        // Commercial License Generator
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📄 Commercial Clearance & License Generator", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Instant PDF clearance certificate with ISRC & 100% royalty-free guarantee", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("License ID: ${license.licenseId}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Track Title: ${license.trackTitle}", fontSize = 12.sp)
                        Text("Licensee: ${license.licenseeName}", fontSize = 12.sp)
                        Text("ISRC Code: ${license.isrcCode}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        Text("Clearance: ${license.clearanceLevel}", fontSize = 11.sp, color = Color(0xFF10B981))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { Toast.makeText(context, "Generated Clearance PDF Certificate!", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download Commercial PDF License")
                }
            }
        }
    }
}

// 3. Courses & White Label Option
@Composable
private fun CoursesAndWhiteLabelTab(
    courses: List<MasterclassCourseItem>,
    whiteLabel: WhiteLabelConfigData
) {
    val context = LocalContext.current
    var customName by remember { mutableStateOf(whiteLabel.customAppName) }
    var customDomain by remember { mutableStateOf(whiteLabel.customDomain) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Masterclass Courses
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🎓 AI Music Masterclasses & Courses", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Learn Suno v4 prompt engineering, stem separation & mixing", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                courses.forEach { c ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(c.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                Text("⭐ ${c.rating}", fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B), fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Instructor: ${c.instructor} • ${c.studentsEnrolled} Students", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(c.details, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                Button(
                                    onClick = { Toast.makeText(context, "Enrolled in ${c.title}!", Toast.LENGTH_SHORT).show() }
                                ) {
                                    Text("Buy \$${c.priceUSD}", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // White Label Configuration
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🏷️ White Label Studio Rebranding", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Deploy your own branded AI Music Studio app & domain name", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text("Brand App Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customDomain,
                    onValueChange = { customDomain = it },
                    label = { Text("Custom Domain CNAME") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { Toast.makeText(context, "White Label Tenant Configuration Saved!", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save White Label Branding")
                }
            }
        }
    }
}

// 4. Developer API & Spotify Direct Distribution
@Composable
private fun DeveloperApiAndSpotifyTab(
    api: SurAiApiDashboardData,
    spotify: SpotifyDistributionData
) {
    val context = LocalContext.current
    var generatedKey by remember { mutableStateOf(api.apiKey) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Developer API Keys Dashboard
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🔑 Sur AI Developer API Key Portal", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Integrate music generation directly into your apps & web platforms", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = generatedKey,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Secret API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { Toast.makeText(context, "API Key Copied to Clipboard!", Toast.LENGTH_SHORT).show() }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = api.requestsUsedThisMonth.toFloat() / api.requestLimitMonthly.toFloat(),
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                )
                Text("Usage: ${api.requestsUsedThisMonth} / ${api.requestLimitMonthly} Requests (${api.tierName})", fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        generatedKey = "sur_live_sk_${(10000..99999).random()}_new_key"
                        Toast.makeText(context, "New API Key Generated!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Regenerate Secret API Key")
                }
            }
        }

        // Spotify Direct Distribution
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🎧 Spotify Direct Distribution & DDEX", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Distribute tracks directly to Spotify, Apple Music & YouTube Music", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = Color(0xFF1DB954).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Release: ${spotify.releaseTitle}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("UPC Code: ${spotify.upcCode} • Release Date: ${spotify.targetReleaseDate}", fontSize = 12.sp)
                        Text("Status: ${spotify.distributionStatus}", fontWeight = FontWeight.Bold, color = Color(0xFF10B981), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Target Stores: ${spotify.targetStores.joinToString(", ")}", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { Toast.makeText(context, "Release dispatched to DDEX Distribution Pipeline!", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Distribute Release to 150+ Stores")
                }
            }
        }
    }
}

// 5. Referral Income & Creator Affiliate Program
@Composable
private fun ReferralAndAffiliateTab(
    ref: ReferralProgramData,
    aff: AffiliateProgramData
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Referral Income 20%
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("💸 Referral Income Program (20%)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Badge(containerColor = Color(0xFF10B981)) { Text("20% Lifetime", modifier = Modifier.padding(2.dp)) }
                }
                Text("Earn 20% recurring income on every friend you invite", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = ref.referralLink,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Your Personal Referral Link") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { Toast.makeText(context, "Referral Link Copied!", Toast.LENGTH_SHORT).show() }) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${ref.totalReferredUsers}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Invited Users", fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("\$${ref.pendingPayoutUSD}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF10B981))
                        Text("Pending Payout", fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("\$${ref.totalEarnedUSD}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Total Earned", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { Toast.makeText(context, "Payout Request Sent to bKash / Bank!", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Withdraw Referral Earnings (\$${ref.pendingPayoutUSD})")
                }
            }
        }

        // Creator Affiliate Program 30%
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🌟 Creator Affiliate Program (30%)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${aff.affiliateTier} • Exclusive coupon promo codes for channel partners", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Promo Code: ${aff.customPromoCode}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Conversion: ${aff.conversionRatePercent}%", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Total Clicks: ${aff.totalClicks} • Total Conversions: ${aff.conversions}", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { Toast.makeText(context, "Affiliate Banner Assets Copied!", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Get Affiliate Banners & Promo Assets")
                }
            }
        }
    }
}

// 6. Sponsorship & Tip Jar
@Composable
private fun SponsorshipAndTipJarTab(
    sponsorships: List<SponsorshipDealItem>,
    tipJar: TipJarStateData
) {
    val context = LocalContext.current
    var tipAmount by remember { mutableStateOf("10.00") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Digital Tip Jar
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("☕ Creator Digital Tip Jar", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Support ${tipJar.creatorHandle} • Total Received: \$${tipJar.totalTipsReceivedUSD}", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = tipAmount,
                    onValueChange = { tipAmount = it },
                    label = { Text("Tip Amount (\$ USD / bKash)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { Toast.makeText(context, "Thank you for tipping \$$tipAmount via bKash!", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2136E))
                    ) {
                        Text("bKash Tip", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { Toast.makeText(context, "Thank you for tipping \$$tipAmount via Card!", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Card / Crypto", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Recent Tips:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                tipJar.recentTipList.forEach { t ->
                    Text("• $t", fontSize = 11.sp)
                }
            }
        }

        // Sponsorship Manager
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🤝 Brand Sponsorship Marketplace", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Connect with audio gear brands, vst makers & tech sponsors", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                sponsorships.forEach { sp ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(sp.sponsorName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("\$${sp.payoutUSD}", fontWeight = FontWeight.Bold, color = Color(0xFF10B981), fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(sp.campaignScope, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Badge { Text(sp.status) }
                                OutlinedButton(
                                    onClick = { Toast.makeText(context, "Sponsorship Application Submitted for ${sp.sponsorName}", Toast.LENGTH_SHORT).show() },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Accept Deal", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
