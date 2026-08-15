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
fun ProfessionalLegalScreen(
    appLanguage: String = "en",
    royaltySplit: RoyaltySplitData,
    isrcData: IsrcGeneratorData,
    contractData: MusicContractMakerData,
    arManager: AiArManagerData,
    trademarkData: TrademarkSearchData,
    syncOpps: List<SyncOpportunityItem>,
    invoiceData: InvoiceData,
    expenseData: ExpenseTrackerData,
    taxReport: TaxReportData,
    rhythmData: RhythmTrainingData
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }

    val categories = listOf(
        "📊 Royalties & ISRC",
        "📄 Contracts & Trademark",
        "🤖 AI A&R & Sync License",
        "🧾 Invoices & Expenses",
        "📈 Tax & Rhythm Training"
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
                            text = if (appLanguage == "bn") "প্রফেশনাল ও লিগ্যাল হাব" else "Professional & Legal Hub",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (appLanguage == "bn") "১০টি মিউজিক বিজনেস, লিগ্যাল ও ট্রেইনিং ফিচার" else "10 Music Business, Legal & Rhythm Training Tools",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Badge(containerColor = Color(0xFF3B82F6)) {
                        Text("PRO Legal", modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), color = Color.White)
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

        // Body Content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> RoyaltiesAndIsrcTab(royaltySplit, isrcData)
                1 -> ContractsAndTrademarkTab(contractData, trademarkData)
                2 -> AiArAndSyncLicenseTab(arManager, syncOpps)
                3 -> InvoicesAndExpensesTab(invoiceData, expenseData)
                4 -> TaxAndRhythmTab(taxReport, rhythmData)
            }
        }
    }
}

// 1. Royalties & ISRC Generator
@Composable
private fun RoyaltiesAndIsrcTab(royalty: RoyaltySplitData, isrc: IsrcGeneratorData) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Royalty Split Calculator
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📊 Royalty Split Calculator", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Track: ${royalty.songTitle} • Total Earnings: \$${royalty.totalRevenueUSD}", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                royalty.splits.forEach { s ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(s.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(s.role, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${s.sharePercent}% Share", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                            Text("\$${s.estimatedPayoutUSD}", fontSize = 11.sp, color = Color(0xFF10B981))
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                }

                Button(
                    onClick = { Toast.makeText(context, "Royalty Payment Dispatched via Smart Contract!", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Execute Payouts to Collaborators")
                }
            }
        }

        // ISRC Code Generator
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🏷️ Official ISRC Code Generator", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Country: ${isrc.countryCode} • Registrant: ${isrc.registrantCode} • Year: 20${isrc.year}", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                isrc.designations.forEach { (code, track) ->
                    ListItem(
                        headlineContent = { Text(code, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                        supportingContent = { Text(track, fontSize = 12.sp) },
                        trailingContent = {
                            IconButton(onClick = { Toast.makeText(context, "ISRC $code Copied!", Toast.LENGTH_SHORT).show() }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                            }
                        }
                    )
                    Divider()
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { Toast.makeText(context, "Generated New ISRC Code: BD-SUR-26-00004", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Mint Next Sequential ISRC Code")
                }
            }
        }
    }
}

// 2. Music Contract Maker & Trademark Search
@Composable
private fun ContractsAndTrademarkTab(contract: MusicContractMakerData, tm: TrademarkSearchData) {
    val context = LocalContext.current
    var selectedContractType by remember { mutableStateOf(contract.selectedType) }
    var partyA by remember { mutableStateOf(contract.partyA) }
    var partyB by remember { mutableStateOf(contract.partyB) }

    var tmQuery by remember { mutableStateOf(tm.searchQuery) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Contract Maker
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📄 Music Contract Maker (PDF Generator)", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Legally binding contracts for producers, vocalists & feature artists", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                Text("Contract Type:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(contract.contractTypes) { type ->
                        FilterChip(
                            selected = selectedContractType == type,
                            onClick = { selectedContractType = type },
                            label = { Text(type, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = partyA,
                    onValueChange = { partyA = it },
                    label = { Text("Party A (Label / Producer)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = partyB,
                    onValueChange = { partyB = it },
                    label = { Text("Party B (Artist / Feature)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { Toast.makeText(context, "Generated $selectedContractType PDF Document!", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate & Sign PDF Contract")
                }
            }
        }

        // Trademark Search
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🛡️ Music Trademark & Brand Search", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Check international trademark availability for stage names & band logos", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = tmQuery,
                    onValueChange = { tmQuery = it },
                    label = { Text("Brand / Stage Name Query") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { Toast.makeText(context, "Trademark Database Searched!", Toast.LENGTH_SHORT).show() }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Status: CLEAR & AVAILABLE ✓", fontWeight = FontWeight.Bold, color = Color(0xFF10B981), fontSize = 13.sp)
                        Text("Class: ${tm.registrationClass}", fontSize = 11.sp)
                        Text("Estimated Filing Cost: \$${tm.estimatedCostUSD}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { Toast.makeText(context, "Filing application for trademark '$tmQuery'...", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("File Trademark Application")
                }
            }
        }
    }
}

// 3. AI A&R Manager & Sync Licensing Finder
@Composable
private fun AiArAndSyncLicenseTab(ar: AiArManagerData, syncs: List<SyncOpportunityItem>) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI A&R Manager
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("🤖 AI A&R Talent & Hit Evaluator", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Badge(containerColor = MaterialTheme.colorScheme.primary) { Text("Score: ${ar.hitScore}/100", modifier = Modifier.padding(2.dp)) }
                }
                Text("Artist: ${ar.artistName} • Potential: ${ar.marketPotential}", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                Text("A&R Hit Analysis Strengths:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                ar.strengths.forEach { st ->
                    Text("• $st", fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("A&R Production Improvements:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                ar.improvementSuggestions.forEach { imp ->
                    Text("• $imp", fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { Toast.makeText(context, "Full A&R Pitch Deck Generated for Record Labels!", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Generate Record Label Pitch Deck")
                }
            }
        }

        // Sync Licensing Finder
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🎬 Sync Licensing Opportunity Finder", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Pitch tracks to TV shows, Netflix movies, video games & ads", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                syncs.forEach { s ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(s.projectTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("\$${s.budgetUSD}", fontWeight = FontWeight.Bold, color = Color(0xFF10B981), fontSize = 13.sp)
                            }
                            Text("Genre: ${s.requiredGenre} • Scope: ${s.licenseScope}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Badge { Text(s.status) }
                                Button(
                                    onClick = { Toast.makeText(context, "Pitched track to ${s.projectTitle}!", Toast.LENGTH_SHORT).show() },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Submit Track", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 4. Invoice Generator & Expense Tracker
@Composable
private fun InvoicesAndExpensesTab(inv: InvoiceData, exp: ExpenseTrackerData) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Invoice Generator
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🧾 Professional Studio Invoice Generator", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Invoice #: ${inv.invoiceNumber} • Client: ${inv.clientName}", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                inv.items.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${item.description} (x${item.quantity})", fontSize = 12.sp, modifier = Modifier.weight(1f))
                        Text("\$${item.unitPriceUSD}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tax (${inv.taxPercent}%):", fontSize = 12.sp)
                    Text("\$${inv.totalUSD * inv.taxPercent / 100f}", fontSize = 12.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Due:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("\$${inv.totalUSD}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF10B981))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { Toast.makeText(context, "Invoice Sent to Client via Email & bKash Request!", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Send Invoice to Client")
                }
            }
        }

        // Expense Tracker
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("💸 Studio Expense & Gear Tracker", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Spent: \$${exp.totalSpentUSD} / Budget: \$${exp.monthlyBudgetUSD}", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = exp.totalSpentUSD / exp.monthlyBudgetUSD,
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                )

                Spacer(modifier = Modifier.height(12.dp))

                exp.recentExpenses.forEach { e ->
                    ListItem(
                        headlineContent = { Text(e.title, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                        supportingContent = { Text("Category: ${e.category} • Date: ${e.date}", fontSize = 11.sp) },
                        trailingContent = { Text("\$${e.amountUSD}", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444), fontSize = 13.sp) }
                    )
                    Divider()
                }

                Button(
                    onClick = { Toast.makeText(context, "Added new gear expense \$50.00!", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Log New Expense Item")
                }
            }
        }
    }
}

// 5. Tax Report & Rhythm Training
@Composable
private fun TaxAndRhythmTab(tax: TaxReportData, rhythm: RhythmTrainingData) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tax Report
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📈 Annual Music Tax & Income Report (${tax.taxYear})", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Automatically calculates deductions, write-offs & net tax liabilities", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("\$${tax.grossMusicIncomeUSD}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF10B981))
                        Text("Gross Income", fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("-\$${tax.deductibleExpensesUSD}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFEF4444))
                        Text("Deductions", fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("\$${tax.netTaxableIncomeUSD}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                        Text("Net Taxable", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { Toast.makeText(context, "Tax Summary PDF Downloaded!", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Download Official Tax Filing Summary")
                }
            }
        }

        // Rhythm Training
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("🥁 Interactive Rhythm & Timing Trainer", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Badge(containerColor = Color(0xFF10B981)) { Text("Accuracy: ${rhythm.userScorePercent}%", modifier = Modifier.padding(2.dp)) }
                }
                Text("${rhythm.exerciseName} • Tempo: ${rhythm.bpm} BPM", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                Text("Level ${rhythm.unlockedLevels} / ${rhythm.totalLevels} Unlocked", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LinearProgressIndicator(
                    progress = rhythm.unlockedLevels.toFloat() / rhythm.totalLevels.toFloat(),
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { Toast.makeText(context, "Metronome Active! Tap in sync with the 808 kick...", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Timer, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Rhythm Tap Challenge")
                }
            }
        }
    }
}
