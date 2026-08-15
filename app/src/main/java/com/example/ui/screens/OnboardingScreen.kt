package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)

@Composable
fun OnboardingScreen(onCompleted: () -> Unit) {
    var pageIndex by remember { mutableStateOf(0) }
    val pages = listOf(
        OnboardingPage(
            "AI Song Generation",
            "Generate professional tracks, vocals, and lyrics instantly using Suno v4 & OpenAI neural models.",
            Icons.Default.AutoAwesome
        ),
        OnboardingPage(
            "Global Community Feed",
            "Stream hits created by top creators worldwide, like, remix, and share your musical creations.",
            Icons.Default.MusicNote
        ),
        OnboardingPage(
            "Marketplace & Licensing",
            "Buy and sell beats, stems, and commercial licenses with bKash, Nagad, and Rocket integration.",
            Icons.Default.Storefront
        )
    )

    val page = pages[pageIndex]

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                pages.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(6.dp)
                            .width(if (pageIndex == index) 32.dp else 12.dp)
                            .background(
                                color = if (pageIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(3.dp)
                            )
                    )
                }
            }

            // Center Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                )
                            ),
                            shape = RoundedCornerShape(32.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = page.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                }
                Spacer(modifier = Modifier.height(36.dp))
                Text(
                    text = page.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = page.subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Bottom Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Button(
                    onClick = {
                        if (pageIndex < pages.size - 1) {
                            pageIndex++
                        } else {
                            onCompleted()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = if (pageIndex < pages.size - 1) "Next" else "Get Started",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (pageIndex < pages.size - 1) {
                    TextButton(
                        onClick = onCompleted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text(text = "Skip", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
