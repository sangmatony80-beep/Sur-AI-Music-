package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ui.theme.ThemeColorPreset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    appLanguage: String,
    themeMode: String,
    themeColor: String,
    autoPlay: Boolean,
    hqAudio: Boolean,
    studioFx: Boolean,
    notificationsEnabled: Boolean,
    onLanguageChange: (String) -> Unit,
    onThemeModeChange: (String) -> Unit,
    onThemeColorChange: (String) -> Unit,
    onAutoPlayChange: (Boolean) -> Unit,
    onHqAudioChange: (Boolean) -> Unit,
    onStudioFxChange: (Boolean) -> Unit,
    onNotificationsChange: (Boolean) -> Unit,
    isOnline: Boolean = true,
    onTriggerTestError: ((String, String) -> Unit)? = null,
    onTestConnectivity: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val isBangla = appLanguage == "bn"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(20.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Text(
                            text = if (isBangla) "ইউজার সেটিংস ও প্রিফারেন্স" else "User Settings & Preferences",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Language Settings Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(
                                        text = if (isBangla) "অ্যাপের ভাষা (Language Toggle)" else "App Language Switch",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = if (isBangla) "বাংলা / English পরিবর্তন করুন" else "Switch between Bangla & English UI",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FilterChip(
                                selected = appLanguage == "bn",
                                onClick = {
                                    onLanguageChange("bn")
                                    Toast.makeText(context, "ভাষা পরিবর্তন করা হয়েছে: বাংলা", Toast.LENGTH_SHORT).show()
                                },
                                label = { Text("🇧🇩 বাংলা (Bangla)") },
                                modifier = Modifier.weight(1f),
                                leadingIcon = if (appLanguage == "bn") {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )

                            FilterChip(
                                selected = appLanguage == "en",
                                onClick = {
                                    onLanguageChange("en")
                                    Toast.makeText(context, "Language Switched: English", Toast.LENGTH_SHORT).show()
                                },
                                label = { Text("🇺🇸 English") },
                                modifier = Modifier.weight(1f),
                                leadingIcon = if (appLanguage == "en") {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }
            }

            item {
                // Toggles Section Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = if (isBangla) "সিস্টেম টগল ও সুইচ" else "System Toggles & Switches",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // 1. Dark Mode Toggle
                        ToggleItemRow(
                            icon = Icons.Default.DarkMode,
                            title = if (isBangla) "ডার্ক থিম মোড (Dark Theme)" else "Dark Theme Mode",
                            subtitle = if (isBangla) "চোখের আরামদায়ক ডার্ক ইন্টারফেস" else "Eye-comfort dark UI interface",
                            isChecked = themeMode == "dark",
                            onCheckedChange = { checked ->
                                onThemeModeChange(if (checked) "dark" else "light")
                            }
                        )

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // 2. HQ Audio Toggle
                        ToggleItemRow(
                            icon = Icons.Default.HighQuality,
                            title = if (isBangla) "এইচডি অডিও (320kbps Lossless MP3)" else "HQ HD Audio (320kbps MP3)",
                            subtitle = if (isBangla) "হাই-কোয়ালিটি লসলেস স্টুডিও রেন্ডারিং" else "Ultra high definition audio rendering",
                            isChecked = hqAudio,
                            onCheckedChange = onHqAudioChange
                        )

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // 3. Studio Sound FX Engine
                        ToggleItemRow(
                            icon = Icons.Default.Equalizer,
                            title = if (isBangla) "এআই স্টুডিও সাউন্ড এফেক্টস (Studio Sound FX)" else "AI Studio Sound FX",
                            subtitle = if (isBangla) "ডাইনামিক রিভার্ব, ইকুইলাইজার ও বাস বুস্ট" else "Dynamic reverb, EQ & bass boost",
                            isChecked = studioFx,
                            onCheckedChange = onStudioFxChange
                        )

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // 4. Auto-Play Toggle
                        ToggleItemRow(
                            icon = Icons.Default.PlayCircle,
                            title = if (isBangla) "স্বয়ংক্রিয় গান প্লে (Auto-Play Next Track)" else "Auto-Play Next Track",
                            subtitle = if (isBangla) "পরবর্তী তৈরি গান অটো প্লে হবে" else "Automatically play next AI generated song",
                            isChecked = autoPlay,
                            onCheckedChange = onAutoPlayChange
                        )

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // 5. Notifications Toggle
                        ToggleItemRow(
                            icon = Icons.Default.Notifications,
                            title = if (isBangla) "নোটিফিকেশন নোটিশ (Push Notifications)" else "Push Notifications",
                            subtitle = if (isBangla) "নতুন গান তৈরি ও অফার সম্পর্কে আপডেট পান" else "Updates on newly generated songs & offers",
                            isChecked = notificationsEnabled,
                            onCheckedChange = onNotificationsChange
                        )
                    }
                }
            }

            item {
                // Download Options & Preferences
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column {
                                Text(
                                    text = if (isBangla) "অডিও ও গান ডাউনলোড অপশন" else "Audio & Song Download Options",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = if (isBangla) "অফলাইন ডাউনলোড ফরম্যাট ও স্টোরেজ প্রিফারেন্স" else "Offline audio format & storage preferences",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Default format
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isBangla) "ডিফল্ট ডাউনলোড কোয়ালিটি" else "Default Download Quality",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "320 KBPS MP3 (HQ)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Direct 1-Click APK Info
                        Surface(
                            onClick = {
                                Toast.makeText(
                                    context,
                                    if (isBangla) "স্ক্রিনের উপরে Settings (⚙️) বা Export থেকে 'Generate APK' এ চাপ দিন" else "Click Settings (⚙️) / Export on top-right to Generate APK",
                                    Toast.LENGTH_LONG
                                ).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.InstallMobile, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isBangla) "অ্যাপ APK ডাউনলোড ও ব্যাকআপ" else "App APK Download & Offline Backup",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = if (isBangla) "ক্লাউড বিল্ডার থেকে সরাসরি ইনস্টলেবল APK পান" else "Direct installable APK export assistant",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            item {
                // Theme Preset Color Selection
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (isBangla) "থিম একসেন্ট কালার (Theme Accent Color)" else "Theme Accent Colors",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ThemeColorPreset.values().forEach { preset ->
                                val isSelected = themeColor == preset.name
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(preset.primary)
                                        .clickable { onThemeColorChange(preset.name) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Network & System Diagnostics Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isBangla) "নেটওয়ার্ক ও সিস্টেম ডায়াগনস্টিকস" else "Network & System Diagnostics",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Surface(
                                color = if (isOnline) Color(0xFF1B5E20) else MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(if (isOnline) Color.Green else Color.Red, CircleShape)
                                    )
                                    Text(
                                        text = if (isOnline) "ONLINE" else "OFFLINE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isOnline) Color.White else MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (isBangla)
                                "সার্ভার সংযোগ এবং অথেনটিকেশন স্টেট পরীক্ষা করুন। কোনো সমস্যা হলে গ্লোবাল ব্যানার প্রদর্শিত হবে।"
                            else
                                "Monitor real-time network connectivity and test global error handling alerts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onTestConnectivity?.invoke() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Wifi,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isBangla) "সংযোগ পরীক্ষা" else "Check Network",
                                    fontSize = 12.sp
                                )
                            }

                            Button(
                                onClick = {
                                    onTriggerTestError?.invoke(
                                        if (isBangla) "পরীক্ষামূলক অথেনটিকেশন ত্রুটি" else "Session Expiration Alert",
                                        if (isBangla) "আপনার অথেনটিকেশন টোকেন নবায়ন প্রয়োজন। অনুগ্রহ করে পুনরায় সাইন ইন করুন।" else "Your authentication session has expired. Please re-authenticate."
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isBangla) "ত্রুটি টেস্ট" else "Test Error",
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
private fun ToggleItemRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}
