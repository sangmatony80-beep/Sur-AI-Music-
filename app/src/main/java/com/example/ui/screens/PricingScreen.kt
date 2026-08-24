package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PlanEntity
import com.example.data.local.UserSubscriptionEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PricingScreen(
    plans: List<PlanEntity>,
    activeSubscription: UserSubscriptionEntity?,
    onSubscribe: (planId: String, billingCycle: String) -> Unit,
    onBack: () -> Unit
) {
    var isYearly by remember { mutableStateOf(false) }
    var showComparisonTable by remember { mutableStateOf(false) }

    var showPaymentDialogForPlan by remember { mutableStateOf<PlanEntity?>(null) }

    if (showPaymentDialogForPlan != null) {
        val plan = showPaymentDialogForPlan!!
        val price = if (isYearly) plan.priceYearly else plan.priceMonthly
        com.example.ui.components.InstantMfsPaymentDialog(
            packName = "${plan.name} (${if (isYearly) "Yearly" else "Monthly"})",
            tokenAmount = plan.tokensPerMonth,
            priceBdt = price,
            onDismiss = { showPaymentDialogForPlan = null },
            onPaymentSuccess = { _, _, _ ->
                onSubscribe(plan.id, if (isYearly) "yearly" else "monthly")
                showPaymentDialogForPlan = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose Your Plan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Unlock Studio-Grade AI Music",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Supercharge your creativity with Sur AI Studio engine, massive token allowances, and professional licensing.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Monthly / Yearly Toggle
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { isYearly = false },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!isYearly) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    contentColor = if (!isYearly) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("Monthly Billing")
                            }
                            Button(
                                onClick = { isYearly = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isYearly) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    contentColor = if (isYearly) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("Yearly (Save 15%)")
                            }
                        }
                    }
                }
            }

            // Pricing Cards
            items(plans) { plan ->
                val isCurrent = activeSubscription?.planId == plan.id
                val price = if (isYearly) plan.priceYearly else plan.priceMonthly
                val cycleText = if (isYearly) "/year" else "/month"

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(24.dp),
                    border = if (isCurrent) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = plan.name,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isCurrent) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "Active Plan",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = if (price == 0) "Free" else "৳$price",
                                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (price > 0) {
                                Text(
                                    text = cycleText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Features list
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            PricingFeatureRow(text = if (plan.lyricsLimitPerDay < 9999) "${plan.lyricsLimitPerDay} lyrics / day" else if (plan.lyricsLimitPerMonth > 0) "${plan.lyricsLimitPerMonth} lyrics / month" else "Unlimited lyrics generation")
                            PricingFeatureRow(text = "${plan.tokensPerMonth} tokens / month")
                            PricingFeatureRow(text = if (plan.hasWatermark) "Standard watermark on export" else "No Watermark on export")
                            PricingFeatureRow(text = if (plan.commercialLicense) "Commercial License Included" else "Personal Use Only")
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (price > 0) {
                                    showPaymentDialogForPlan = plan
                                } else {
                                    onSubscribe(plan.id, if (isYearly) "yearly" else "monthly")
                                }
                            },
                            enabled = !isCurrent,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCurrent) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(text = if (isCurrent) "Current Plan" else if (price == 0) "Get Started Free" else "Upgrade to ${plan.name}")
                        }
                    }
                }
            }

            // Comparison Table Toggle
            item {
                OutlinedButton(
                    onClick = { showComparisonTable = !showComparisonTable },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.CompareArrows, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (showComparisonTable) "Hide Detailed Comparison" else "View Detailed Comparison Table")
                }
            }

            if (showComparisonTable) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Feature Matrix",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            plans.forEach { plan ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(plan.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text("৳${plan.priceMonthly}/mo • ${plan.tokensPerMonth} tokens")
                                }
                                Text(
                                    text = "Lyrics: ${if (plan.lyricsLimitPerMonth == -1) "Unlimited" else "${plan.lyricsLimitPerMonth}/mo"}, Watermark: ${if (plan.hasWatermark) "Yes" else "No"}, Commercial: ${if (plan.commercialLicense) "Yes" else "No"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun PricingFeatureRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
