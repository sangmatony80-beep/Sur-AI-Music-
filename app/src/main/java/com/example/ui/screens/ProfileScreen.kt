package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ThemeColorPreset

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    userEmail: String?,
    tokenBalance: Int,
    activeSubscription: com.example.data.local.UserSubscriptionEntity?,
    themeMode: String,
    themeColor: String,
    onThemeModeChange: (String) -> Unit,
    onThemeColorChange: (String) -> Unit,
    onNavigatePricing: () -> Unit,
    onOpenTokenPacks: () -> Unit,
    onLogout: () -> Unit
) {
    var feedbackText by remember { mutableStateOf("") }
    var feedbackSubmitted by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (userEmail?.take(1) ?: "U").uppercase(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = userEmail ?: "Guest User",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Sur AI Pro Member",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "⚡ Supabase Active",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Subscription & Tokens Card
        item {
            Card(
                onClick = onNavigatePricing,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Current Plan: ${(activeSubscription?.planId ?: "Free").replaceFirstChar { it.uppercase() }}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Token Balance: $tokenBalance Tokens",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Button(
                        onClick = onOpenTokenPacks,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Get Tokens")
                    }
                }
            }
        }

        // Theme Mode settings
        item {
            Text(
                text = "Appearance & Theme",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("dark" to "Dark", "light" to "Light", "amoled" to "AMOLED").forEach { (mode, label) ->
                    FilterChip(
                        selected = themeMode == mode,
                        onClick = { onThemeModeChange(mode) },
                        label = { Text(label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Color Presets
        item {
            Text(
                text = "Theme Color Preset",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            androidx.compose.foundation.layout.FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeColorPreset.values().forEach { preset ->
                    FilterChip(
                        selected = themeColor == preset.name,
                        onClick = { onThemeColorChange(preset.name) },
                        label = { Text(preset.nameStr) }
                    )
                }
            }
        }

        // Feedback Form
        item {
            Text(
                text = "Secure Feedback Report",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = feedbackText,
                onValueChange = { feedbackText = it },
                placeholder = { Text("Report bugs or request features...") },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (feedbackText.isNotBlank()) {
                        feedbackSubmitted = true
                        feedbackText = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Submit Encrypted Feedback")
            }
            if (feedbackSubmitted) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Feedback submitted successfully!", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
            }
        }

        // Logout
        item {
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out")
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun StatItem(label: String, count: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
