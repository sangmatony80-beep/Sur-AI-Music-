package com.example.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 1. Referral & Viral Growth Dialog (রেফারেল ও আয় সিস্টেম)
 * Users earn 50 Credits / ৳50 per invited friend who creates their first AI song.
 */
@Composable
fun ReferralEarningDialog(
    userReferralCode: String = "SUR-VIP786",
    totalEarnings: Double = 350.0,
    invitedCount: Int = 7,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("referral_earning_dialog"),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = Color(0xFF10B981)
                    )
                }
                Column {
                    Text(
                        text = "রেফার করে ইনকাম করুন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "প্রতি সফল রেফারে ৫০ কয়েন / ৳৫০ বোনাস",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Earnings stats banner
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("মোট ইনকাম", fontSize = 12.sp, color = Color.LightGray)
                            Text("৳$totalEarnings", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF10B981))
                        }
                        Divider(modifier = Modifier.height(30.dp).width(1.dp), color = Color.DarkGray)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("আমন্ত্রিত বন্ধু", fontSize = 12.sp, color = Color.LightGray)
                            Text("$invitedCount জন", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF38BDF8))
                        }
                    }
                }

                Text(
                    text = "আপনার স্পেশাল রেফারেল কোড:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )

                // Referral code copy container
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = userReferralCode,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 2.sp
                        )
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(userReferralCode))
                                Toast.makeText(context, "কোড কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }
                }

                // Share link button
                Button(
                    onClick = {
                        val shareText = "আমার স্পেশাল কোড '$userReferralCode' দিয়ে Sur AI Music অ্যাপে যোগ দিয়ে পেয়ে যান ফ্রি AI গান তৈরির কয়েন! ডাউনলোড লিঙ্ক: https://ais-dev-2nvormwokf73vx7z6l3czk-435778467539.asia-southeast1.run.app"
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "বন্ধুদের সাথে শেয়ার করুন"))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("হোয়াটসঅ্যাপ/ফেসবুকে শেয়ার করুন", color = Color.White)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("বন্ধ করুন")
            }
        }
    )
}

/**
 * 2. Rewarded Video Ad Simulation for Free Coin Refill (বিজ্ঞাপন দেখে ফ্রি কয়েন আয়)
 * Allows free users to watch a 5-sec sponsor ad to earn +5 AI Studio generation credits.
 */
@Composable
fun WatchAdForCreditsDialog(
    onRewardEarned: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isAdPlaying by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableStateOf(5) }
    var isAdCompleted by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isAdPlaying) onDismiss() },
        modifier = Modifier.testTag("rewarded_ad_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color(0xFFF59E0B))
                Text("ফ্রি কয়েন অর্জন করুন", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (isAdPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF1E1B4B), Color(0xFF4338CA))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("স্পন্সর বিজ্ঞাপন চলছে...", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("পুরস্কার পেতে আর $secondsLeft সেকেন্ড অপেক্ষা করুন", color = Color.LightGray, fontSize = 12.sp)
                        }
                    }
                } else if (isAdCompleted) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("অভিনন্দন! +৫ কয়েন যোগ হয়েছে", fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        }
                    }
                } else {
                    Text(
                        text = "একটি ছোট স্পন্সর বিজ্ঞাপন দেখে পেয়ে যান সাথে সাথে +৫ AI মিউজিক জেনারেশন কয়েন!",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Stars, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("দৈনিক সর্বোচ্চ ৫টি বিজ্ঞাপন দেখা যাবে", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!isAdPlaying && !isAdCompleted) {
                Button(
                    onClick = {
                        isAdPlaying = true
                        coroutineScope.launch {
                            for (i in 5 downTo 1) {
                                secondsLeft = i
                                delay(1000L)
                            }
                            isAdPlaying = false
                            isAdCompleted = true
                            onRewardEarned(5)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                ) {
                    Text("বিজ্ঞাপন দেখুন (+৫ কয়েন)", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            } else if (isAdCompleted) {
                Button(onClick = onDismiss) {
                    Text("ঠিক আছে")
                }
            }
        },
        dismissButton = {
            if (!isAdPlaying && !isAdCompleted) {
                TextButton(onClick = onDismiss) {
                    Text("পরে দেখব")
                }
            }
        }
    )
}

/**
 * 3. Creator Royalty & Audio NFT Marketplace Pitch (গান বিক্রি ও রয়্যালটি ইনকাম)
 * Let creators monetize their generated AI beats & license them to other users.
 */
@Composable
fun CreatorRoyaltyCashoutDialog(
    creatorBalance: Double = 1450.0,
    soldTracksCount: Int = 12,
    onWithdrawRequested: (Double, String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedMfs by remember { mutableStateOf("bKash") }
    var accountNumber by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("creator_royalty_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFF8B5CF6))
                Text("ক্রিয়েটর রয়্যালটি ও ক্যাশআউট", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Balance summary
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1E1B4B),
                    border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("আপনার মোট উপার্জিত রয়্যালটি:", fontSize = 12.sp, color = Color.LightGray)
                        Text("৳$creatorBalance BDT", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFFA78BFA))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("মোট বিক্রি হওয়া ট্র্যাক: $soldTracksCount টি", fontSize = 12.sp, color = Color(0xFF38BDF8))
                    }
                }

                if (isSubmitted) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981))
                            Text("৳$creatorBalance উইথড্রয়াল রিকোয়েস্ট সফল! ২ ঘণ্টার মধ্যে আপনার অ্যাকাউন্টে টাকা পৌঁছে যাবে।", fontSize = 12.sp, color = Color(0xFF10B981))
                        }
                    }
                } else {
                    Text("উইথড্রয়াল মেথড বেছে নিন:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("bKash", "Nagad", "Rocket").forEach { mfs ->
                            FilterChip(
                                selected = selectedMfs == mfs,
                                onClick = { selectedMfs = mfs },
                                label = { Text(mfs) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = accountNumber,
                        onValueChange = { accountNumber = it },
                        label = { Text("$selectedMfs পার্সোনাল নম্বর দিন") },
                        placeholder = { Text("01XXXXXXXXX") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            if (!isSubmitted) {
                Button(
                    onClick = {
                        if (accountNumber.length >= 11) {
                            onWithdrawRequested(creatorBalance, "$selectedMfs ($accountNumber)")
                            isSubmitted = true
                            Toast.makeText(context, "উইথড্রয়াল রিকোয়েস্ট গৃহীত হয়েছে!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "সঠিক ১১ ডিজিটের ফোন নম্বর দিন", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                ) {
                    Text("টাকা তুলুন (Withdraw)", color = Color.White)
                }
            } else {
                Button(onClick = onDismiss) {
                    Text("ঠিক আছে")
                }
            }
        },
        dismissButton = {
            if (!isSubmitted) {
                TextButton(onClick = onDismiss) {
                    Text("বাতিল")
                }
            }
        }
    )
}
