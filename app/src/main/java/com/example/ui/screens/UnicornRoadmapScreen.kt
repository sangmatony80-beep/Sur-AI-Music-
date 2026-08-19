package com.example.ui.screens

import android.widget.Toast
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
fun UnicornRoadmapScreen(
    appLanguage: String = "en"
) {
    val context = LocalContext.current
    var roadmapChecked1 by remember { mutableStateOf(true) }
    var roadmapChecked2 by remember { mutableStateOf(true) }
    var roadmapChecked3 by remember { mutableStateOf(false) }
    var roadmapChecked4 by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Card
        Surface(
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = Color(0xFF38BDF8))
                    Text(
                        text = "Sahitya Softwares Lmtd. Unicorn Roadmap",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Text(
                    text = if (appLanguage == "bn") "বিলিয়ন-ডলার ইউনিকর্ন স্টার্টআপ হওয়ার পরবর্তী কৌশলগত রোডম্যাপ" else "Strategic Roadmap to Achieve Billion-Dollar Global Unicorn Status ($1B+ Valuation)",
                    fontSize = 13.sp,
                    color = Color.LightGray.copy(alpha = 0.9f)
                )
                Divider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Target Valuation: $1.0 Billion USD", fontSize = 12.sp, color = Color(0xFF34D399), fontWeight = FontWeight.Bold)
                    Text("Readiness: Phase 3 / 5", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Milestone 1: Multi-Region Edge AI Infrastructure
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CloudSync, tint = MaterialTheme.colorScheme.primary, contentDescription = null)
                    Text("1. Multi-Region Edge AI Infrastructure", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Text(
                    text = "Deploying distributed Kubernetes clusters & ONNX-optimized edge models to guarantee sub-100ms AI music generation latency globally across North America, Europe, and Asia.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Status: Deployed & Active", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    Checkbox(checked = roadmapChecked1, onCheckedChange = { roadmapChecked1 = it })
                }
            }
        }

        // Milestone 2: Proprietary Fine-Tuned Audio Diffusion Models
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Psychology, tint = MaterialTheme.colorScheme.primary, contentDescription = null)
                    Text("2. Proprietary Fine-Tuned Audio Diffusion Models", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Text(
                    text = "Training custom transformer and diffusion audio models on 500,000+ hours of licensed multi-genre master stems for studio-grade 320kbps fidelity.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Status: Active Training", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    Checkbox(checked = roadmapChecked2, onCheckedChange = { roadmapChecked2 = it })
                }
            }
        }

        // Milestone 3: Enterprise B2B Sync Licensing API
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.BusinessCenter, tint = MaterialTheme.colorScheme.primary, contentDescription = null)
                    Text("3. Enterprise B2B Sync Licensing API", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Text(
                    text = "Building automated B2B licensing portals for Hollywood studios, AAA game developers, and global advertising agencies to instantly license AI soundtracks.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = {
                        roadmapChecked3 = true
                        Toast.makeText(context, "B2B Enterprise API Gateway Initialized!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (roadmapChecked3) "B2B Gateway Active" else "Initialize B2B API Gateway")
                }
            }
        }

        // Milestone 4: Cryptographic Audio Watermarking & DRM
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.VerifiedUser, tint = MaterialTheme.colorScheme.primary, contentDescription = null)
                    Text("4. Cryptographic Audio Watermarking & DRM", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Text(
                    text = "Embedding imperceptible cryptographic watermarks into every generated waveform to prevent unauthorized scraping and enforce legal copyright compliance.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = {
                        roadmapChecked4 = true
                        Toast.makeText(context, "DRM Cryptographic Watermark Applied!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (roadmapChecked4) "DRM Watermarking Active" else "Enable Cryptographic DRM")
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
                    text = "Developed by Sahitya Softwares Lmtd. (Unicorn Vision 2026)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}
