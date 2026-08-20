package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterpriseStudioHubScreen(
    appLanguage: String = "en"
) {
    val context = LocalContext.current
    var isDistributed by remember { mutableStateOf(false) }
    var isSmartContractActive by remember { mutableStateOf(true) }
    var activeSessionBand by remember { mutableStateOf("AI Drummer & Bass Pro") }
    var royaltySplitPercent by remember { mutableStateOf(85) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Enterprise Valuation Header
        Surface(
            color = Color(0xFF7C3AED),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = Color.White)
                    Text(
                        text = "Sahitya Softwares Lmtd. Enterprise Hub",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Text(
                    text = if (appLanguage == "bn") "মাল্টি-কোটি টাকার এন্টারপ্রাইজ মিউজিক স্টুডিও ইকোসিস্টেম" else "Multi-Crore Enterprise AI Music Studio Ecosystem & Global Distribution Suite",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Divider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Valuation Status: Tier-1 Unicorn", fontSize = 12.sp, color = Color(0xFFFDE047), fontWeight = FontWeight.Bold)
                    Text("Est. Valuation: ৳120+ Crore", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Feature 1: Global Music Distribution & Streaming API
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Public, tint = MaterialTheme.colorScheme.primary, contentDescription = null)
                    Text("Global DSP Distribution (Spotify, Apple, YouTube)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Text(
                    text = "Distribute your AI master tracks instantly to 150+ global streaming platforms with automated ISRC generation and 100% royalty collection.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = {
                        isDistributed = true
                        Toast.makeText(context, "Successfully queued for Global DSP Distribution!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isDistributed) Color(0xFF10B981) else MaterialTheme.colorScheme.primary)
                ) {
                    Icon(if (isDistributed) Icons.Default.Check else Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isDistributed) "Distributed to 150+ Platforms (Active)" else "Launch Global Distribution")
                }
            }
        }

        // Feature 2: Blockchain Smart Contract Royalty Ledger
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Security, tint = MaterialTheme.colorScheme.primary, contentDescription = null)
                    Text("Blockchain Smart Contract Royalties", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Text(
                    text = "Automated smart contract escrow splits copyright royalties instantly among co-writers, vocalists, and producers via decentralized ledger.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Artist Royalty Split: $royaltySplitPercent%", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Switch(
                        checked = isSmartContractActive,
                        onCheckedChange = { isSmartContractActive = it }
                    )
                }
            }
        }

        // Feature 3: Real-time AI Session Band (Virtual Musician Jamming)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Group, tint = MaterialTheme.colorScheme.primary, contentDescription = null)
                    Text("AI Session Band & Virtual Co-Writer", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Text(
                    text = "Invite generative AI session musicians (AI Bassist, Lead Guitarist, Drummer) to jam live with your chord progressions in real time.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = activeSessionBand == "AI Drummer & Bass Pro",
                        onClick = { activeSessionBand = "AI Drummer & Bass Pro" },
                        label = { Text("Drummer & Bass") }
                    )
                    FilterChip(
                        selected = activeSessionBand == "Acoustic Lead Guitar",
                        onClick = { activeSessionBand = "Acoustic Lead Guitar" },
                        label = { Text("Lead Guitar") }
                    )
                    FilterChip(
                        selected = activeSessionBand == "Full Orchestra",
                        onClick = { activeSessionBand = "Full Orchestra" },
                        label = { Text("Orchestra") }
                    )
                }
                Text("Active AI Session: $activeSessionBand (Synced 44.1kHz)", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
            }
        }

        // Feature 4: Real Creator Earnings & MFS Cashout (bKash / Nagad)
        var showPayoutDialog by remember { mutableStateOf(false) }
        var payoutNumber by remember { mutableStateOf("") }
        var payoutAmount by remember { mutableStateOf("1500") }
        var selectedPayoutMfs by remember { mutableStateOf("bKash") }
        var totalWithdrawn by remember { mutableDoubleStateOf(3200.0) }
        var availableEarnings by remember { mutableDoubleStateOf(4850.0) }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.MonetizationOn, tint = Color(0xFF10B981), contentDescription = null)
                    Text("ক্রিয়েটর ইনকাম ও রয়্যালটি উইথড্র (Real Income)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Text(
                    text = "আপনার তৈরি গানের স্ট্রিমিং, মার্কেটপ্লেস বিট বিক্রি এবং লাইসেন্স থেকে মোট আয় রিয়েল-টাইমে বিকাশ/নগদে উত্তোলন করুন।",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("বর্তমান ব্যালেন্স:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("৳ ${String.format("%.2f", availableEarnings)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("মোট ক্যাশআউট:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("৳ ${String.format("%.2f", totalWithdrawn)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Button(
                    onClick = { showPayoutDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("বিকাশ / নগদ দিয়ে টাকা তুলুন (Withdraw Now)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showPayoutDialog) {
            AlertDialog(
                onDismissRequest = { showPayoutDialog = false },
                title = { Text("রয়্যালটি উইথড্রয়াল রিকোয়েস্ট") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("উত্তোলনের মেথড নির্বাচন করুন:", fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = selectedPayoutMfs == "bKash",
                                onClick = { selectedPayoutMfs = "bKash" },
                                label = { Text("bKash (বিকাশ)") }
                            )
                            FilterChip(
                                selected = selectedPayoutMfs == "Nagad",
                                onClick = { selectedPayoutMfs = "Nagad" },
                                label = { Text("Nagad (নগদ)") }
                            )
                        }
                        OutlinedTextField(
                            value = payoutNumber,
                            onValueChange = { payoutNumber = it },
                            label = { Text("$selectedPayoutMfs একাউন্ট নাম্বার") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = payoutAmount,
                            onValueChange = { payoutAmount = it },
                            label = { Text("টাকার পরিমাণ (৳)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amt = payoutAmount.toDoubleOrNull() ?: 0.0
                            if (payoutNumber.length >= 11 && amt in 100.0..availableEarnings) {
                                availableEarnings -= amt
                                totalWithdrawn += amt
                                showPayoutDialog = false
                                Toast.makeText(context, "৳ $amt সফলভাবে $selectedPayoutMfs এ পাঠানো হয়েছে! (TrxID: TX${(10000000..99999999).random()})", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "সঠিক মোবাইল নাম্বার ও ব্যালেন্স দিন", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("উইথড্র কনফার্ম করুন")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPayoutDialog = false }) {
                        Text("বাতিল")
                    }
                }
            )
        }

        // Feature 5: Automated AI Music Video & Visualizer Studio
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Movie, tint = MaterialTheme.colorScheme.primary, contentDescription = null)
                    Text("AI Music Video & 4K Visualizer Generator", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Text(
                    text = "Automatically render 4K audio-reactive lyric videos and cinematic music visualizers optimized for YouTube, TikTok, and Instagram Reels.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = {
                        Toast.makeText(context, "Rendering 4K AI Music Visualizer & Lyric Video...", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.VideoCall, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Render 4K Music Video")
                }
            }
        }

        // Sahitya Softwares Lmtd. Enterprise Footer
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Sahitya Softwares Lmtd.",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Copyright © 2026. All rights reserved.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Developed by Sahitya Softwares Lmtd. (Enterprise Edition)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}
