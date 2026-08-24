package com.example.ui.components

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DrawerMenuItem(
    val route: String,
    val titleBn: String,
    val titleEn: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val category: String,
    val badge: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeftDrawerContent(
    currentRoute: String,
    appLanguage: String,
    userEmail: String?,
    userRole: String = "GUEST",
    tokenBalance: Int,
    activePlanName: String,
    onNavigate: (String) -> Unit,
    onCloseDrawer: () -> Unit,
    onLanguageToggle: () -> Unit
) {
    val isBangla = appLanguage == "bn"
    val isAdmin = userRole.equals("ADMIN", ignoreCase = true)

    val menuItems = buildList {
        // Main
        add(DrawerMenuItem("home", "হোম ড্যাশবোর্ড", "Home Dashboard", Icons.Default.Home, "MAIN"))
        add(DrawerMenuItem("feed", "মিউজিক ফিড ও প্লেলিস্ট", "Music Feed & Community", Icons.Default.RssFeed, "MAIN"))
        add(DrawerMenuItem("downloads", "ডাউনলোড ও অফলাইন সেন্টার", "Downloads & Offline Center", Icons.Default.DownloadForOffline, "MAIN", "HOT"))

        // Studio Features - Each as a separate distinct screen
        add(DrawerMenuItem("sur_live_studio", "সুর এআই লাইভ ইঞ্জিন স্টুডিও", "Sur AI Live Studio Engine", Icons.Default.Language, "AI_STUDIO", "LIVE"))
        add(DrawerMenuItem("create_gen", "এআই গান জেনারেটর স্টুডিও", "AI Song Generator Studio", Icons.Default.MusicNote, "AI_STUDIO", "HOT"))
        add(DrawerMenuItem("create_lyrics", "লিরিক্স স্টুডিও ও ট্রান্সক্রাইব", "Lyrics Studio & Transcribe", Icons.Default.EditNote, "AI_STUDIO"))
        add(DrawerMenuItem("create_adv", "এডভান্সড এআই ১০ ফিচার", "Advanced AI Tech (10)", Icons.Default.Psychology, "AI_STUDIO", "PRO"))
        add(DrawerMenuItem("create_stems", "প্রো স্টেম ও ডিজে টুলকিট", "Pro Stems & Mixer", Icons.Default.GraphicEq, "AI_STUDIO"))
        add(DrawerMenuItem("voice_correction", "ভয়েস কারেকশন ও টিউনিং", "Voice Correction & Tuning", Icons.Default.Tune, "AI_STUDIO", "HOT"))
        add(DrawerMenuItem("video_visual", "ভিডিও ও ভিজ্যুয়াল স্টুডিও (১৫)", "Video & Visual Studio (15)", Icons.Default.Videocam, "AI_STUDIO", "NEW"))
        add(DrawerMenuItem("social_collab", "সোশ্যাল ও কোলাব স্টুডিও (১৫)", "Social & Collab (15)", Icons.Default.Groups, "AI_STUDIO", "HOT"))
        add(DrawerMenuItem("global_lang", "গ্লোবাল ও ভাষা স্টুডিও (৬)", "Global & Language (6)", Icons.Default.Translate, "AI_STUDIO", "NEW"))
        add(DrawerMenuItem("pro_legal", "প্রফেশনাল ও লিগ্যাল (১০)", "Professional & Legal (10)", Icons.Default.Gavel, "AI_STUDIO", "PRO"))
        add(DrawerMenuItem("experience_ui", "ইউআই/ইউএক্স ও এক্সপেরিয়েন্স (১৫)", "UI/UX & Experience (15)", Icons.Default.Palette, "AI_STUDIO", "NEW"))
        add(DrawerMenuItem("payment_sub", "পেমেন্ট ও সাবস্ক্রিপশন (৭)", "Payment & Subscription (7)", Icons.Default.Payment, "AI_STUDIO", "HOT"))

        // ADMIN PANEL IS STRICTLY HIDDEN UNLESS USER HAS VERIFIED ADMIN ROLE
        if (isAdmin) {
            add(DrawerMenuItem("admin_panel", "এডমিন প্যানেল (২০)", "Admin Panel & Studio (20)", Icons.Default.AdminPanelSettings, "ADMIN_ONLY", "ADMIN"))
        }

        add(DrawerMenuItem("tech_ai", "টেকনিক্যাল ও এআই (১২)", "Technical & AI (12)", Icons.Default.Terminal, "AI_STUDIO", "TECH"))
        add(DrawerMenuItem("automation", "অটোমেশন ও সোশ্যাল (৬)", "Automation & Social Bot (6)", Icons.Default.AutoMode, "AI_STUDIO", "AUTO"))
        add(DrawerMenuItem("voice_access", "ভয়েস ও অ্যাক্সেসিবিলিটি (৬)", "Voice & Accessibility (6)", Icons.Default.Mic, "AI_STUDIO", "VOICE"))
        add(DrawerMenuItem("pro_power", "প্রো পাওয়ার ফিচারসমূহ (৮)", "Studio Pro Suite (8)", Icons.Default.WorkspacePremium, "AI_STUDIO", "PRO"))
        add(DrawerMenuItem("enterprise_hub", "এন্টারপ্রাইজ ইউনিকর্ন হাব (৳১২০+ কোটি)", "Enterprise Unicorn Hub (৳120Cr+)", Icons.Default.Verified, "AI_STUDIO", "UNICORN"))
        add(DrawerMenuItem("unicorn_roadmap", "ইউনিকর্ন স্টার্টআপ রোডম্যাপ ($1B+)", "Unicorn Roadmap ($1B+)", Icons.Default.RocketLaunch, "AI_STUDIO", "ROADMAP"))

        // Market & Profile
        add(DrawerMenuItem("marketplace", "এআই মার্কেটপ্লেস", "AI Marketplace", Icons.Default.Storefront, "MARKET"))
        add(DrawerMenuItem("profile", "মাই প্রোফাইল ও প্ল্যান", "My Profile & Plans", Icons.Default.Person, "MARKET"))
        add(DrawerMenuItem("settings", "ইউজার সেটিংস ও টগল", "User Settings & Toggles", Icons.Default.Settings, "MARKET"))
    }

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.width(310.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                modifier = Modifier.padding(8.dp).size(28.dp),
                                tint = Color.White
                            )
                        }

                        IconButton(onClick = onCloseDrawer) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    Text(
                        text = if (isBangla) "সুর এআই মিউজিক স্টুডিও" else "Sur AI Music Studio",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                                Text("$tokenBalance Tokens", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(
                            color = if (userRole == "ADMIN") Color(0xFFF59E0B) else Color.White.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (userRole == "ADMIN") "ADMIN 👑" else activePlanName,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    userEmail?.let { email ->
                        Text(
                            text = email,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Navigation Menu Items Scrollable
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (isBangla) "প্রধান নেভিগেশন" else "MAIN NAVIGATION",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )

                menuItems.filter { it.category == "MAIN" }.forEach { item ->
                    DrawerItemRow(
                        item = item,
                        isSelected = currentRoute == item.route,
                        isBangla = isBangla,
                        onClick = {
                            onNavigate(item.route)
                            onCloseDrawer()
                        }
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Text(
                    text = if (isBangla) "এআই মিউজিক স্টুডিও (আলাদা ফিচার)" else "AI MUSIC STUDIO FEATURES",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )

                menuItems.filter { it.category == "AI_STUDIO" }.forEach { item ->
                    DrawerItemRow(
                        item = item,
                        isSelected = currentRoute == item.route,
                        isBangla = isBangla,
                        onClick = {
                            onNavigate(item.route)
                            onCloseDrawer()
                        }
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Text(
                    text = if (isBangla) "মার্কেটপ্লেস ও সেটিংস" else "MARKETPLACE & SETTINGS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )

                menuItems.filter { it.category == "MARKET" }.forEach { item ->
                    DrawerItemRow(
                        item = item,
                        isSelected = currentRoute == item.route,
                        isBangla = isBangla,
                        onClick = {
                            onNavigate(item.route)
                            onCloseDrawer()
                        }
                    )
                }

                if (isAdmin) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color(0xFFF59E0B).copy(alpha = 0.5f)
                    )

                    Text(
                        text = if (isBangla) "👑 এডমিন সিকিউর কন্ট্রোল" else "👑 ADMIN SECURE CONTROLS",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFF59E0B),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )

                    menuItems.filter { it.category == "ADMIN_ONLY" }.forEach { item ->
                        DrawerItemRow(
                            item = item,
                            isSelected = currentRoute == item.route,
                            isBangla = isBangla,
                            onClick = {
                                onNavigate(item.route)
                                onCloseDrawer()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Quick Language Switcher Footer
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = if (isBangla) "ভাষা (Language)" else "Language Toggle",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    FilterChip(
                        selected = true,
                        onClick = onLanguageToggle,
                        label = { Text(if (isBangla) "🇧🇩 বাংলা" else "🇺🇸 English", fontSize = 12.sp) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerItemRow(
    item: DrawerMenuItem,
    isSelected: Boolean,
    isBangla: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        label = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBangla) item.titleBn else item.titleEn,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp
                )
                if (item.badge != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = item.badge,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        },
        selected = isSelected,
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            unselectedContainerColor = Color.Transparent
        ),
        modifier = Modifier.padding(vertical = 2.dp)
    )
}
