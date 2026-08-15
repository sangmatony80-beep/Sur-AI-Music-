package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class TokenPack(val id: String, val tokens: Int, val price: Int, val badge: String? = null)

@Composable
fun TokenPackDialog(
    currentBalance: Int,
    onDismiss: () -> Unit,
    onBuyTokens: (tokens: Int, cost: Int, description: String) -> Unit,
    onGiftTokens: (recipientEmail: String, tokens: Int, description: String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Packs, 1: Gift
    var recipientEmail by remember { mutableStateOf("") }
    var giftAmount by remember { mutableStateOf("100") }

    val packs = listOf(
        TokenPack("pack_1", 500, 49, "Popular"),
        TokenPack("pack_2", 2000, 149, "Best Value"),
        TokenPack("pack_3", 5000, 299, "Pro Pack"),
        TokenPack("pack_4", 20000, 999, "Studio Mega Pack")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Token Center", fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp) // approximate or fill
            ) {
                // Balance Header
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Current Token Balance", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$currentBalance Tokens", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                        }
                        Button(
                            onClick = { selectedTab = 0 },
                            colors = ButtonDefaults.buttonColors(containerColor = if (selectedTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                        ) {
                            Text("Get Tokens", color = if (selectedTab == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Buy Token Packs") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Gift Tokens") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTab == 0) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        items(packs) { pack ->
                            Card(
                                onClick = {
                                    onBuyTokens(pack.tokens, pack.price, "Purchased ${pack.tokens} Tokens Pack")
                                    onDismiss()
                                },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("${pack.tokens} Tokens", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            if (pack.badge != null) {
                                                Surface(
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = pack.badge,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text("Instant delivery • Never expires", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Button(onClick = {
                                        onBuyTokens(pack.tokens, pack.price, "Purchased ${pack.tokens} Tokens Pack")
                                        onDismiss()
                                    }) {
                                        Text("৳${pack.price}")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Send tokens to a friend or collaborator in the Sur AI Studio community.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedTextField(
                            value = recipientEmail,
                            onValueChange = { recipientEmail = it },
                            label = { Text("Recipient Email") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = giftAmount,
                            onValueChange = { giftAmount = it },
                            label = { Text("Token Amount") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val amt = giftAmount.toIntOrNull() ?: 0
                                if (recipientEmail.isNotBlank() && amt > 0 && currentBalance >= amt) {
                                    onGiftTokens(recipientEmail, amt, "Gifted $amt tokens to $recipientEmail")
                                    onDismiss()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.CardGiftcard, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Send Token Gift")
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
}
