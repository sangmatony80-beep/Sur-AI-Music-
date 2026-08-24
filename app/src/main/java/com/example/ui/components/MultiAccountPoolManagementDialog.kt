package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.account.MultiAccountPoolManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiAccountPoolManagementDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val poolManager = remember { MultiAccountPoolManager(context) }
    val poolState by poolManager.poolState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showBulkDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var inputEmail by remember { mutableStateOf("") }
    var inputApiKey by remember { mutableStateOf("") }
    var inputSessionUrl by remember { mutableStateOf("") }
    var inputQuota by remember { mutableStateOf("1000") }
    var inputBulkText by remember { mutableStateOf("") }

    val filteredAccounts = remember(poolState.accounts, searchQuery) {
        if (searchQuery.isBlank()) {
            poolState.accounts
        } else {
            poolState.accounts.filter {
                it.email.contains(searchQuery, ignoreCase = true) ||
                it.note.contains(searchQuery, ignoreCase = true) ||
                it.status.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.CloudQueue, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column {
                    Text("১০০+ টেম্পোরারি মেইল ও টোকেন হাব", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("সুর এআই স্টুডিও আনলিমিটেড কোটা ও অটো-রোটেশন", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Overview Banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("মোট সক্রিয় নোড", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("${poolState.activeTempNodeCount} / ${poolState.accounts.size}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("অটো-রোটেশন", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Surface(
                                color = Color(0xFF10B981).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("অন (Bypass Active)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("মোট ফ্রি ক্রেডিট", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("⚡ ${poolState.totalAvailableCredits}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF10B981))
                        }
                    }
                }

                // Quick Action Buttons: 1-Click 100+ Temp Mails & Bulk Upload
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                poolManager.generate100TempAccounts()
                                Toast.makeText(context, "⚡ নতুন ১০০+ টেম্পোরারি মেইল নোড সক্রিয় করা হয়েছে!", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.weight(1.1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("১-ক্লিকে ১০০+ মেইল", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    FilledTonalButton(
                        onClick = { showBulkDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("মেইল আপলোড", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.weight(0.7f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Text("+১", fontSize = 10.sp)
                    }
                }

                // Search Box and Utility Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("মেইল বা নোড খুঁজুন...", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(10.dp)
                    )

                    IconButton(
                        onClick = {
                            scope.launch {
                                poolManager.resetAllDailyCredits()
                                Toast.makeText(context, "সকল নোডের দৈনিক ক্রেডিট ফুল রিচার্জ হয়েছে!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "রিসেট ক্রেডিট", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }

                    IconButton(
                        onClick = {
                            val csvData = poolManager.exportAsCsv()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Sur_AI_Accounts", csvData)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "অ্যাকাউন্ট তালিকা ক্লিপবোর্ডে কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "CSV কপি", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                    }
                }

                Text(
                    "সক্রিয় টেম্পোরারি নোড (${filteredAccounts.size}টি):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // List of accounts
                Box(modifier = Modifier.weight(1f)) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxSize()) {
                        items(filteredAccounts) { acc ->
                            val isCurrentActive = acc.id == poolState.activeAccountId
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isCurrentActive) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                    } else if (acc.isActive) {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    } else {
                                        Color.DarkGray.copy(alpha = 0.2f)
                                    }
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                                        Surface(
                                            color = if (isCurrentActive) Color(0xFF3B82F6) else if (acc.isActive) Color(0xFF10B981) else Color.Gray,
                                            shape = CircleShape,
                                            modifier = Modifier.size(8.dp)
                                        ) {}
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(acc.email, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                                                if (isCurrentActive) {
                                                    Surface(
                                                        color = MaterialTheme.colorScheme.primary,
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text("Active Node", fontSize = 8.sp, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                                    }
                                                }
                                            }
                                            Text("ক্রেডিট: ${acc.remainingCredits}/${acc.dailyFreeQuota} • ব্যবহৃত: ${acc.usedCredits}", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(acc.note, fontSize = 8.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Switch(
                                            checked = acc.isActive,
                                            onCheckedChange = {
                                                scope.launch { poolManager.toggleAccountStatus(acc.id) }
                                            },
                                            modifier = Modifier.height(20.dp)
                                        )
                                        IconButton(
                                            onClick = {
                                                scope.launch { poolManager.deleteAccount(acc.id) }
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "ডিলিট", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(15.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("সম্পন্ন")
            }
        }
    )

    // Single Add Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("নতুন গুগল/ফ্লো লিংক (Credit Pool)", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "লগইন করা গুগল মিউজিক বা ফ্লো লিংক পেস্ট করুন। এটি অ্যাপের টোটাল ক্রেডিট পুলে জমা হবে যা ইউজারদের কাছে প্ল্যান হিসেবে বিক্রি করা যাবে।",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = inputEmail,
                        onValueChange = { inputEmail = it },
                        label = { Text("টেম্প ইমেইল অ্যাড্রেস") },
                        placeholder = { Text("temp.node@example.com") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inputApiKey,
                        onValueChange = { inputApiKey = it },
                        label = { Text("টোকেন / কি (ঐচ্ছিক)") },
                        placeholder = { Text("AI Key বা Session Token...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inputSessionUrl,
                        onValueChange = { inputSessionUrl = it },
                        label = { Text("লগইন করা গুগল মিউজিক/ফ্লো সেশন লিংক") },
                        placeholder = { Text("https://www.flowmusic.app/session/...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inputQuota,
                        onValueChange = { inputQuota = it },
                        label = { Text("বিক্রয়যোগ্য প্ল্যান কোটা (Credits)") },
                        placeholder = { Text("1000") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputEmail.isNotBlank()) {
                            scope.launch {
                                poolManager.addAccount(
                                    email = inputEmail,
                                    apiKey = inputApiKey,
                                    sessionUrl = inputSessionUrl,
                                    quota = inputQuota.toIntOrNull() ?: 1000,
                                    note = "কাস্টম ব্যবহারকারী নোড"
                                )
                                Toast.makeText(context, "লিংকটি যুক্ত হয়েছে এবং অ্যাপের ক্রেডিট বৃদ্ধি পেয়েছে!", Toast.LENGTH_SHORT).show()
                                inputEmail = ""
                                inputApiKey = ""
                                inputSessionUrl = ""
                                showAddDialog = false
                            }
                        }
                    }
                ) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("বাতিল") }
            }
        )
    }

    // Bulk Upload / Paste Temp Mails Dialog
    if (showBulkDialog) {
        AlertDialog(
            onDismissRequest = { showBulkDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.FileUpload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("টেম্পোরারি মেইল বাল্ক আপলোড ও পার্সার", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "টেম্পোরারি ইমেইল তালিকা পেস্ট করুন (প্রতি লাইনে একটি করে):\n• email@domain.com\n• email@domain.com:token123\n• email@domain.com,https://session_url",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = inputBulkText,
                        onValueChange = { inputBulkText = it },
                        placeholder = { Text("temp01@mail.io\ntemp02@mail.io:secret_key\ntemp03@mail.io\ntemp04@mail.io...") },
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputBulkText.isNotBlank()) {
                            scope.launch {
                                val count = poolManager.uploadTempMailsText(inputBulkText)
                                Toast.makeText(context, "সফলভাবে $count টি টেম্প মেইল পুলে আপলোড ও সংযুক্ত হয়েছে!", Toast.LENGTH_LONG).show()
                                inputBulkText = ""
                                showBulkDialog = false
                            }
                        }
                    }
                ) {
                    Text("আপলোড ও পার্স করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBulkDialog = false }) { Text("বাতিল") }
            }
        )
    }
}
