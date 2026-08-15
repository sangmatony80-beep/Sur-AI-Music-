package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

enum class ErrorSeverity {
    ERROR,
    WARNING,
    INFO,
    SUCCESS
}

enum class ErrorCategory {
    CONNECTIVITY,
    AUTHENTICATION,
    SUPABASE_SYNC,
    STUDIO_GENERATION,
    PAYMENT,
    GENERAL
}

data class GlobalErrorInfo(
    val id: String = java.util.UUID.randomUUID().toString(),
    val category: ErrorCategory = ErrorCategory.GENERAL,
    val severity: ErrorSeverity = ErrorSeverity.ERROR,
    val title: String,
    val message: String,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
    val isDismissable: Boolean = true,
    val autoDismissMillis: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun GlobalErrorBanner(
    errorInfo: GlobalErrorInfo?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    appLanguage: String = "en"
) {
    val isBangla = appLanguage == "bn"

    AnimatedVisibility(
        visible = errorInfo != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        if (errorInfo != null) {
            // Auto dismiss if configured
            LaunchedEffect(errorInfo.id) {
                errorInfo.autoDismissMillis?.let { duration ->
                    delay(duration)
                    onDismiss()
                }
            }

            val (bgColor, accentColor, iconVector, defaultTitle) = when (errorInfo.severity) {
                ErrorSeverity.ERROR -> Tuple4(
                    MaterialTheme.colorScheme.errorContainer,
                    MaterialTheme.colorScheme.error,
                    when (errorInfo.category) {
                        ErrorCategory.CONNECTIVITY -> Icons.Default.WifiOff
                        ErrorCategory.AUTHENTICATION -> Icons.Default.Lock
                        ErrorCategory.SUPABASE_SYNC -> Icons.Default.CloudOff
                        ErrorCategory.PAYMENT -> Icons.Default.Payment
                        else -> Icons.Default.ErrorOutline
                    },
                    if (isBangla) "ত্রুটি সনাক্ত হয়েছে" else "Attention Required"
                )
                ErrorSeverity.WARNING -> Tuple4(
                    Color(0xFF3E2723),
                    Color(0xFFFFB74D),
                    Icons.Default.WarningAmber,
                    if (isBangla) "সতর্কবার্তা" else "Warning"
                )
                ErrorSeverity.INFO -> Tuple4(
                    MaterialTheme.colorScheme.secondaryContainer,
                    MaterialTheme.colorScheme.primary,
                    Icons.Default.Info,
                    if (isBangla) "তথ্য" else "Notice"
                )
                ErrorSeverity.SUCCESS -> Tuple4(
                    Color(0xFF1B5E20),
                    Color(0xFF81C784),
                    Icons.Default.CheckCircle,
                    if (isBangla) "সফল হয়েছে" else "Success"
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .testTag("global_error_banner"),
                shape = RoundedCornerShape(16.dp),
                color = bgColor,
                tonalElevation = 6.dp,
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Category Icon with Circle Accent
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(accentColor.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Text Details
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = errorInfo.title.ifBlank { defaultTitle },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                // Category Tag
                                Surface(
                                    color = accentColor.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = when (errorInfo.category) {
                                            ErrorCategory.CONNECTIVITY -> if (isBangla) "ইন্টারনেট" else "Network"
                                            ErrorCategory.AUTHENTICATION -> if (isBangla) "লগইন/নিরাপত্তা" else "Auth"
                                            ErrorCategory.SUPABASE_SYNC -> if (isBangla) "ক্লাউড সিঙ্ক" else "Cloud"
                                            ErrorCategory.STUDIO_GENERATION -> if (isBangla) "এআই স্টুডিও" else "Studio"
                                            ErrorCategory.PAYMENT -> if (isBangla) "পেমেন্ট" else "Payment"
                                            ErrorCategory.GENERAL -> if (isBangla) "সিস্টেম" else "System"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = accentColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = errorInfo.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }

                        // Close / Dismiss button
                        if (errorInfo.isDismissable) {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss error",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Optional Action Button (e.g. "Retry", "Re-authenticate", "Go to Settings")
                    if (errorInfo.actionLabel != null && errorInfo.onAction != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    errorInfo.onAction.invoke()
                                    if (errorInfo.isDismissable) onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentColor,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(
                                    text = errorInfo.actionLabel,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
