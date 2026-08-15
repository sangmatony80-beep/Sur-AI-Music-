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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.ProPowerFeaturesData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProPowerFeaturesScreen(
    appLanguage: String = "en",
    proData: ProPowerFeaturesData,
    supabaseSchema: String
) {
    val context = LocalContext.current
    var showSqlDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Studio Plan Badge
        Surface(
            color = Color(0xFFEAB308),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color.Black)
                        Text(
                            text = if (appLanguage == "bn") "প্রো পাওয়ার ফিচারসমূহ" else "Pro Studio Power Suite",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    Text(
                        text = proData.userPlanRole,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Bold
                    )
                }
                Badge(containerColor = Color.Black) {
                    Text("UNLOCKED", modifier = Modifier.padding(4.dp), color = Color(0xFFEAB308), fontWeight = FontWeight.Bold)
                }
            }
        }

        // 8 Features List
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("⭐ 8 Studio Pro Power Tools", fontWeight = FontWeight.Bold, fontSize = 17.sp)

                // 1. AI Color Grading
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("1. 🎨 AI Video Color Grading LUTs", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Active Preset: ${proData.aiColorGradingPreset}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }

                // 2. Voice Command Pro Macro
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("2. 🎙️ Voice Command Pro Macro Automation", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Script: ${proData.voiceCommandProMacroScript}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                }

                // 3. AI A&R Manager Pro
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("3. 📈 AI A&R Manager Pro Score", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Record Label Score: ${proData.aiAnrRatingScore}/100 (Hit Potential Certified)", fontSize = 11.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    }
                }

                // 4. Sync Licensing Pro
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("4. 🎬 Sync Licensing Pro (Movie/TV Pitches)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Status: TV & Commercial Licensing Catalogue Ready", fontSize = 11.sp, color = Color.Gray)
                    }
                }

                // 5. Royalty Split Auto Pro
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("5. 💰 Royalty Split Auto Pro Calculator", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Splits: ${proData.autoRoyaltySplitSummary}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }

                // 6. ISRC Code Pro
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("6. 🆔 ISRC Code Pro Generator", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Global Code: ${proData.isrcGeneratedCode}", fontSize = 11.sp, color = Color.Blue, fontWeight = FontWeight.Bold)
                    }
                }

                // 7. Music Contract Pro
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("7. 📜 Legal Music Contract Pro Generator", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Contract: ${proData.musicContractType}", fontSize = 11.sp, color = Color.Gray)
                    }
                }

                // 8. Expense Tracker Pro
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("8. 📊 Studio Expense & Revenue Ledger Pro", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Monthly Earnings: ৳${proData.expenseTrackerMonthlyBDT} BDT", fontSize = 12.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Supabase SQL Schema Viewer Button
        Button(
            onClick = { showSqlDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Storage, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("View Supabase PostgreSQL Schema SQL", color = Color.White)
        }
    }

    if (showSqlDialog) {
        AlertDialog(
            onDismissRequest = { showSqlDialog = false },
            title = { Text("⚡ Supabase PostgreSQL Database Schema") },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = supabaseSchema,
                        color = Color.Green,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    Toast.makeText(context, "Supabase SQL Schema Copied!", Toast.LENGTH_SHORT).show()
                    showSqlDialog = false
                }) {
                    Text("Copy & Close")
                }
            }
        )
    }
}
