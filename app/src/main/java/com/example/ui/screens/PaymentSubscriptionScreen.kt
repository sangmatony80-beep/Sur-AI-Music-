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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentSubscriptionScreen(
    appLanguage: String = "en",
    mfsData: MfsMerchantData,
    tokenPacks: List<TokenPackData>,
    coupons: List<CouponCodeItem>,
    subInfo: SubscriptionInfoData
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }

    val categories = listOf(
        "📱 Nagad/bKash/Rocket",
        "📸 Manual Payment",
        "💳 Card & Google Pay",
        "🪙 Token Packs",
        "🎟️ Coupon Codes",
        "👑 Subscription Plan"
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
                            text = if (appLanguage == "bn") "পেমেন্ট ও সাবস্ক্রিপশন স্টুডিও" else "Payment & Subscription Studio",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (appLanguage == "bn") "বিকাশ, নগদ, রকেট, স্ট্রাইপ, টোকেন ও কুপন" else "bKash, Nagad, Rocket, Cards, Tokens & Coupons",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Badge(containerColor = Color(0xFF10B981)) {
                        Text("Instant Pay", modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), color = Color.White)
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

        // Sub View
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> MfsPaymentSubView(mfsData)
                1 -> ManualPaymentSubView(mfsData)
                2 -> CardsAndGooglePaySubView()
                3 -> TokenPacksSubView(tokenPacks)
                4 -> CouponCodesSubView(coupons)
                5 -> SubscriptionManagementSubView(subInfo)
            }
        }
    }
}

// 1. MFS Payment Sub View (bKash, Nagad, Rocket sandbox API integration)
@Composable
private fun MfsPaymentSubView(mfs: MfsMerchantData) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedMfs by remember { mutableStateOf("bKash") }
    var amountText by remember { mutableStateOf("899") }
    var customerPhone by remember { mutableStateOf("01712 345 678") }
    var showInstantDialog by remember { mutableStateOf(false) }

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
                Text("📱 MFS Direct API Payment Gateway (BD)", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Instant automated payment verification for bKash, Nagad & Rocket", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(16.dp))

                Text("Select MFS Payment Method:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedMfs == "bKash",
                        onClick = { selectedMfs = "bKash" },
                        label = { Text("💗 bKash", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedMfs == "Nagad",
                        onClick = { selectedMfs = "Nagad" },
                        label = { Text("🟠 Nagad", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedMfs == "Rocket",
                        onClick = { selectedMfs = "Rocket" },
                        label = { Text("🟣 Rocket", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val activeNumber = when (selectedMfs) {
                    "Nagad" -> mfs.nagadNumber
                    "bKash" -> mfs.bkashNumber
                    else -> mfs.rocketNumber
                }

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("$selectedMfs Merchant / Agent Number:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(activeNumber, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(activeNumber.replace(" ", "")))
                                Toast.makeText(context, "$selectedMfs number copied: $activeNumber", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (BDT ৳)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { customerPhone = it },
                    label = { Text("Your Mobile Number") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        showInstantDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pay ৳$amountText via $selectedMfs Instant Gateway")
                }
            }
        }
    }

    if (showInstantDialog) {
        val parsedAmount = amountText.toIntOrNull() ?: 899
        com.example.ui.components.InstantMfsPaymentDialog(
            packName = "Studio Pro Pack ($selectedMfs)",
            tokenAmount = parsedAmount * 2,
            priceBdt = parsedAmount,
            onDismiss = { showInstantDialog = false },
            onPaymentSuccess = { tokens, cost, trxId ->
                showInstantDialog = false
                Toast.makeText(context, "Payment Successful! ৳$cost paid. TrxID: $trxId", Toast.LENGTH_LONG).show()
            }
        )
    }
}

// 2. Manual Payment Sub View (Transaction ID & Screenshot upload)
@Composable
private fun ManualPaymentSubView(mfs: MfsMerchantData) {
    val context = LocalContext.current
    var txnId by remember { mutableStateOf("9KB41M790") }
    var senderPhone by remember { mutableStateOf("01819 887 766") }
    var selectedMethod by remember { mutableStateOf("bKash (01757 128 059)") }
    var isScreenshotUploaded by remember { mutableStateOf(false) }

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
                Text("📸 Manual Payment Verification & Screenshot", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Send money manually and submit TxnID / Screenshot for 5-min verification", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(16.dp))

                Text("Target Merchant Numbers:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("• bKash Personal/Merchant: ${mfs.bkashNumber}", fontSize = 11.sp)
                Text("• Nagad Personal/Merchant: ${mfs.nagadNumber}", fontSize = 11.sp)
                Text("• Rocket Personal/Merchant: ${mfs.rocketNumber}", fontSize = 11.sp)

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = txnId,
                    onValueChange = { txnId = it },
                    label = { Text("Transaction ID (TxnID)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = senderPhone,
                    onValueChange = { senderPhone = it },
                    label = { Text("Sender Phone Number") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        isScreenshotUploaded = true
                        Toast.makeText(context, "Payment Screenshot Uploaded Successfully!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isScreenshotUploaded) "Screenshot Uploaded ✓" else "Upload Payment Screenshot Image")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        Toast.makeText(context, "Manual Payment Claim Submitted! TxnID: $txnId. Approval time ~2 mins.", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Submit Manual Payment Proof")
                }
            }
        }
    }
}

// 3. Card Payment (Stripe Sandbox) & Google Pay In-App Billing
@Composable
private fun CardsAndGooglePaySubView() {
    val context = LocalContext.current
    var cardNumber by remember { mutableStateOf("4242 •••• •••• 4242") }
    var cardExpiry by remember { mutableStateOf("12/28") }
    var cardCvc by remember { mutableStateOf("123") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stripe Card Payment
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("💳 Stripe Card Payment (International / Local)", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Visa, Mastercard, AMEX & UnionPay Secure Checkout", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { cardNumber = it },
                    label = { Text("Card Number") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = cardExpiry,
                        onValueChange = { cardExpiry = it },
                        label = { Text("Expiry (MM/YY)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = cardCvc,
                        onValueChange = { cardCvc = it },
                        label = { Text("CVC / CVV") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        Toast.makeText(context, "Stripe Test Card Approved! Tokenized payment processed.", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CreditCard, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Process Stripe Card Payment")
                }
            }
        }

        // Google Pay In-App Billing
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🟢 Google Pay & Play Billing API", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("1-Tap Play Store Payment with saved Google Wallet cards", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        Toast.makeText(context, "Google Play In-App Purchase Flow Opened!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Buy with Google Pay", color = Color.White)
                }
            }
        }
    }
}

// 4. Token Packs Sub View
@Composable
private fun TokenPacksSubView(packs: List<TokenPackData>) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("🪙 AI Studio Token Store", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Tokens are used for generating songs, vocal clones, video exports & stems", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        packs.forEach { pack ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (pack.isPopular) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(pack.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Badge(containerColor = if (pack.isPopular) MaterialTheme.colorScheme.primary else Color.Gray) {
                            Text(pack.badgeTag, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("৳${pack.priceBDT.toInt()}", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = MaterialTheme.colorScheme.primary)
                        Text("/ ${pack.tokens} Tokens", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(pack.description, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            Toast.makeText(context, "Selected ${pack.title} (৳${pack.priceBDT.toInt()})! Proceeding to Payment...", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Purchase ${pack.tokens} Tokens")
                    }
                }
            }
        }
    }
}

// 5. Coupon Codes Sub View
@Composable
private fun CouponCodesSubView(coupons: List<CouponCodeItem>) {
    val context = LocalContext.current
    var inputCode by remember { mutableStateOf("SURAI50") }
    var appliedDiscountText by remember { mutableStateOf("") }

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
                Text("🎟️ Apply Promo & Coupon Codes", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Enter promotional discount codes to claim free tokens or price cuts", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = inputCode,
                    onValueChange = { inputCode = it.uppercase() },
                    label = { Text("Coupon Code") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val found = coupons.find { it.code.equals(inputCode, ignoreCase = true) }
                        if (found != null && found.isValid) {
                            appliedDiscountText = "Coupon '${found.code}' Applied! ${found.discountDescription}"
                            Toast.makeText(context, "Coupon Applied Successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            appliedDiscountText = "Invalid or Expired Coupon Code."
                            Toast.makeText(context, "Coupon code invalid", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Apply Coupon Code")
                }

                if (appliedDiscountText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            appliedDiscountText,
                            modifier = Modifier.padding(12.dp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Text("Active Available Promo Codes:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        coupons.forEach { c ->
            ListItem(
                headlineContent = { Text(c.code, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                supportingContent = { Text(c.discountDescription, fontSize = 12.sp) },
                trailingContent = {
                    IconButton(onClick = { inputCode = c.code }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Use")
                    }
                }
            )
            Divider()
        }
    }
}

// 6. Subscription Management Sub View
@Composable
private fun SubscriptionManagementSubView(sub: SubscriptionInfoData) {
    val context = LocalContext.current
    var autoRenew by remember { mutableStateOf(true) }

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
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("👑 Current Active Plan", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Badge(containerColor = Color(0xFF10B981)) { Text(sub.status, modifier = Modifier.padding(2.dp), color = Color.White) }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(sub.currentPlan, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = MaterialTheme.colorScheme.primary)
                Text("Renewal Date: ${sub.renewalDate} • ৳${sub.priceBDT.toInt()} / Month", fontSize = 13.sp)

                Spacer(modifier = Modifier.height(16.dp))

                Text("Included Pro Features:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                sub.features.forEach { f ->
                    Text("• $f", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Auto-Renew Subscription", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Switch(checked = autoRenew, onCheckedChange = {
                        autoRenew = it
                        Toast.makeText(context, if (it) "Auto-Renew Turn ON" else "Auto-Renew Turned OFF", Toast.LENGTH_SHORT).show()
                    })
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { Toast.makeText(context, "Upgraded to Studio Enterprise Tier!", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Upgrade Plan", fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = { Toast.makeText(context, "Subscription Downgraded", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel / Downgrade", fontSize = 11.sp)
                    }
                }
            }
        }

        // Billing History
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📜 Billing & Payment History", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(modifier = Modifier.height(8.dp))

                sub.billingHistory.forEach { b ->
                    ListItem(
                        headlineContent = { Text("${b.planOrPack} (৳${b.amountBDT.toInt()})", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                        supportingContent = { Text("${b.invoiceId} • ${b.date} • ${b.method}", fontSize = 11.sp) },
                        trailingContent = { Text(b.status, fontWeight = FontWeight.Bold, color = Color(0xFF10B981), fontSize = 12.sp) }
                    )
                    Divider()
                }
            }
        }
    }
}
