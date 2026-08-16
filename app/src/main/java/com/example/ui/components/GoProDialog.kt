package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GoProDialog(
    isBangla: Boolean = false,
    onDismiss: () -> Unit,
    onUpgradeClick: () -> Unit,
    onTopUpClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF6366F1), Color(0xFFEC4899), Color(0xFFF59E0B))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = "Go Pro",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isBangla) "ডেইলি টোকেন লিমিট শেষ!" else "AI Token Limit Reached!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isBangla)
                        "আপনার ফ্রি ট্রায়াল বা দৈনিক টোকেন কোটা শেষ হয়ে গেছে। সীমাহীন মিউজিক জেনারেশন এবং প্রিমিয়াম ফিচারের জন্য প্রো প্ল্যানে উন্নীত করুন।"
                        else "You have exhausted your free daily AI generation tokens. Upgrade to Pro for unlimited creativity and exclusive features.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Benefits List
                val benefits = if (isBangla) listOf(
                    "সীমাহীন এআই সং ও লিরিক্স জেনারেশন",
                    "উচ্চমানের স্টেম সেপারেশন (ভোকাল ও মিউজিক আলাদা)",
                    "প্রায়োরিটি ফাস্ট জেনারেশন কিউ",
                    "ওয়াটারমার্ক ছাড়া ফুল রাইটস এক্সপোর্ট"
                ) else listOf(
                    "Unlimited AI Song & Lyrics Generation",
                    "High-Definition Stem Separation",
                    "Priority Generation Queue",
                    "Watermark-free Full Rights Export"
                )

                benefits.forEach { benefit ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = benefit,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onDismiss()
                        onUpgradeClick()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isBangla) "প্রো প্ল্যানে যান (Go Pro)" else "Upgrade to Pro Now", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onTopUpClick()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isBangla) "টোকেন টপ-আপ কিনুন" else "Buy Token Top-Up Pack")
                }
            }
        },
        dismissButton = {
            // Optional text button
        }
    )
}
