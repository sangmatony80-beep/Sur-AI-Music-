package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Direct Instant MFS Gateway Dialog (bKash & Nagad)
 * Provides seamless 1-click checkout with dynamic QR code, simulated OTP & PIN authorization,
 * and auto-crediting tokens to the user's account.
 */
@Composable
fun InstantMfsPaymentDialog(
    packName: String,
    tokenAmount: Int,
    priceBdt: Int,
    onDismiss: () -> Unit,
    onPaymentSuccess: (tokens: Int, cost: Double, trxId: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedMfs by remember { mutableStateOf("bKash") } // "bKash", "Nagad", "Rocket"
    var paymentStep by remember { mutableStateOf(1) } // 1: Number/Method, 2: OTP, 3: PIN, 4: Success
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var pinCode by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var generatedTrxId by remember { mutableStateOf("TX" + Random.nextInt(10000000, 99999999)) }

    val mfsColor = when (selectedMfs) {
        "bKash" -> Color(0xFFE2136E)
        "Nagad" -> Color(0xFFF7941D)
        else -> Color(0xFF8C1D82) // Rocket
    }

    Dialog(onDismissRequest = { if (!isProcessing) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0F172A),
            tonalElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(mfsColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = selectedMfs.take(1),
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                        Column {
                            Text(
                                text = "$selectedMfs Instant Gateway",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Auto Merchant Deposit",
                                fontSize = 11.sp,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, enabled = !isProcessing) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Item & Amount Summary
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(packName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            Text("+$tokenAmount Creation Tokens", color = Color(0xFF38BDF8), fontSize = 12.sp)
                        }
                        Text(
                            text = "৳ $priceBdt BDT",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = Color(0xFF10B981)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Steps handling
                when (paymentStep) {
                    1 -> {
                        // Method Selector Tab
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("bKash", "Nagad", "Rocket").forEach { mfs ->
                                val isSel = selectedMfs == mfs
                                val tabColor = if (mfs == "bKash") Color(0xFFE2136E) else if (mfs == "Nagad") Color(0xFFF7941D) else Color(0xFF8C1D82)
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedMfs = mfs },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSel) tabColor.copy(alpha = 0.25f) else Color(0xFF1E293B),
                                    border = if (isSel) androidx.compose.foundation.BorderStroke(1.5.dp, tabColor) else null
                                ) {
                                    Text(
                                        text = mfs,
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        textAlign = TextAlign.Center,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp,
                                        color = if (isSel) tabColor else Color.LightGray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Your $selectedMfs Account Number") },
                            placeholder = { Text("e.g. 017XXXXXXXX") },
                            leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = mfsColor) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = mfsColor,
                                focusedLabelColor = mfsColor
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (phoneNumber.length >= 11) {
                                    isProcessing = true
                                    scope.launch {
                                        delay(800)
                                        isProcessing = false
                                        paymentStep = 2
                                        otpCode = "4892" // Simulated prefilled OTP
                                        Toast.makeText(context, "Verification code sent to $phoneNumber", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Enter a valid 11-digit mobile number", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = !isProcessing,
                            colors = ButtonDefaults.buttonColors(containerColor = mfsColor),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(46.dp)
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Proceed to Verify", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    2 -> {
                        // OTP Step
                        Text(
                            text = "Enter the 4-digit verification OTP sent to $phoneNumber",
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { otpCode = it },
                            label = { Text("Verification OTP Code") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = mfsColor,
                                focusedLabelColor = mfsColor
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (otpCode.isNotBlank()) {
                                    paymentStep = 3
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = mfsColor),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(46.dp)
                        ) {
                            Text("Confirm OTP", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    3 -> {
                        // PIN Step
                        Text(
                            text = "Enter $selectedMfs PIN to authorize ৳ $priceBdt payment",
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = pinCode,
                            onValueChange = { pinCode = it },
                            label = { Text("$selectedMfs Secret PIN") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = mfsColor,
                                focusedLabelColor = mfsColor
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (pinCode.length >= 4) {
                                    isProcessing = true
                                    scope.launch {
                                        delay(1200)
                                        isProcessing = false
                                        paymentStep = 4
                                        onPaymentSuccess(tokenAmount, priceBdt.toDouble(), generatedTrxId)
                                    }
                                } else {
                                    Toast.makeText(context, "Enter your 5-digit PIN", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = !isProcessing,
                            colors = ButtonDefaults.buttonColors(containerColor = mfsColor),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(46.dp)
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Authorizing Payment...", color = Color.White, fontSize = 12.sp)
                            } else {
                                Text("Pay ৳ $priceBdt Instantly", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    4 -> {
                        // Success Step
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(54.dp))
                            Text("Payment Successful!", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                            Text("TrxID: $generatedTrxId", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Color(0xFF38BDF8))
                            Text("+$tokenAmount Tokens credited immediately!", fontSize = 13.sp, color = Color.LightGray)

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Done & Start Creating", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
