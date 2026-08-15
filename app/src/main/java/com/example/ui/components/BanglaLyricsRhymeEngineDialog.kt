package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/**
 * Advanced Bengali Lyrics Generator with Rhyme Engine (অন্ত্যমিল) & Classical Song Structure
 * Supports স্থায়ী, অন্তরা, সঞ্চারী, আভোগ and instant rhyme suggestions.
 */
@Composable
fun BanglaLyricsRhymeEngineDialog(
    onDismiss: () -> Unit,
    onApplyLyrics: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedMood by remember { mutableStateOf("রোমান্টিক ও প্রেম") }
    var selectedStructure by remember { mutableStateOf("স্থায়ী + অন্তরা") }
    var searchRhymeWord by remember { mutableStateOf("") }

    val rhymeDictionary = remember {
        mapOf(
            "আকাশ" to listOf("বাতাস", "আভাস", "বিশ্বাস", "নিশ্বাস", "হতাশ", "উচ্ছ্বাস"),
            "মন" to listOf("স্বপন", "ভুবন", "আপন", "গোপন", "যতন", "নয়ন"),
            "গান" to listOf("প্রাণ", "তান", "মান", "কলতান", "দান", "সন্ধান"),
            "রাত" to listOf("প্রভাত", "হাতে হাত", "আঘাত", "উৎপাত", "নিশীথাত"),
            "সুর" to listOf("দূর", "নূপুর", "রোদ্দুর", "ভাঙুর", "সুমধুর"),
            "মেঘ" to listOf("আবেগ", "উদ্বেগ", "অনুরূপ", "বিদ্যুৎ-বেগ"),
            "নদী" to listOf("যদি", "নিরবধি", "প্রতিশ্রুতি", "স্মৃতি"),
            "ভালোবাসা" to listOf("আশা", "ভাষা", "পিপাসা", "প্রত্যাশা", "দুরাশা")
        )
    }

    val generatedBengaliStructure = remember(selectedMood, selectedStructure) {
        when (selectedStructure) {
            "স্থায়ী + অন্তরা" -> """
[স্থায়ী]
মেঘের ডানায় ভেসে আসে তোমার মায়াবী সুর,
হৃদয় মাঝে তুমি আছো, নও তো কভু দূর।
আজ বাতাসে বাতাসে বাজে প্রেমেরই আহবান,
তোমার চোখে হারিয়ে যায় আমার অবুঝ প্রাণ।

[অন্তরা]
রাতের আকাশে চাঁদের আলোয় আঁকা ছবিখানি,
তুমি আমার সুখের ঠিকানা, চিরদিনের রানী।
হাতটি ধরে চলবো আমি অনন্তের ওই পারে,
তুমি ছাড়া এ জীবনটা যাবে গো আঁধারে।
""".trimIndent()

            "পূর্ণাঙ্গ চার স্তবক (স্থায়ী, অন্তরা, সঞ্চারী, আভোগ)" -> """
[স্থায়ী]
তুমি আমার সুরের নদী, তুমি গানের তান,
তোমায় ভালোবেসে জুড়ায় আমার তৃষিত প্রাণ।

[অন্তরা]
সকাল সাঁঝে তোমার স্মৃতির আলপনা আঁকি,
বুকের খাঁচায় যত্নে তোমায় রেখেছি যে পাখি।

[সঞ্চারী]
ঝুম বরষার রিনিঝিনি নূপুর পরা রাতে,
হারিয়ে যাবো অচিন দেশে তোমার হাতটি ধরে।

[আভোগ]
চিরকালের শপথ নিয়ে বাঁধবো সুখের ঘর,
তুমি আমার আপন মানুষ, নও তো কভু পর।
""".trimIndent()

            else -> """
[মুখড়া]
মনের আকাশে মেঘ জমেছে আজ,
সুর ছাড়া নেই যে আমার কোনো কাজ।

[অন্তরা]
ভালোবাসার ছন্দে বাজে বাঁশি,
তোমায় ভেবেই নিরবধি হাসি।
""".trimIndent()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0F172A),
            tonalElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF10B981))
                        Column {
                            Text("বাংলা অন্ত্যমিল ও লিরিক্স ইঞ্জিন", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                            Text("Rhyme Engine & Classical Song Structure", fontSize = 10.sp, color = Color.LightGray)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Structure Selector
                Text("গানের কাঠামো (Song Structure):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("স্থায়ী + অন্তরা", "পূর্ণাঙ্গ চার স্তবক (স্থায়ী, অন্তরা, সঞ্চারী, আভোগ)", "আধুনিক পপ").forEach { st ->
                        val isSel = selectedStructure == st
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedStructure = st },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) Color(0xFF10B981) else Color(0xFF1E293B)
                        ) {
                            Text(
                                text = if (st.startsWith("স্থায়ী")) "স্থায়ী+অন্তরা" else if (st.startsWith("পূর্ণাঙ্গ")) "৪ স্তবক" else "পপ",
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = TextAlign.Center,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) Color.Black else Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive Rhyme Finder (অন্ত্যমিল অভিধান)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                            Text("অন্ত্যমিল অভিধান (Rhyme Pairs)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rhymeDictionary.keys.forEach { word ->
                                val isSel = searchRhymeWord == word
                                Surface(
                                    modifier = Modifier.clickable { searchRhymeWord = word },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSel) Color(0xFFF59E0B) else Color(0xFF334155)
                                ) {
                                    Text(
                                        text = word,
                                        fontSize = 11.sp,
                                        color = if (isSel) Color.Black else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        if (searchRhymeWord.isNotBlank() && rhymeDictionary.containsKey(searchRhymeWord)) {
                            Spacer(modifier = Modifier.height(6.dp))
                            val rhymes = rhymeDictionary[searchRhymeWord] ?: emptyList()
                            Text(
                                text = "✨ '${searchRhymeWord}' এর ছন্দ: " + rhymes.joinToString(", "),
                                fontSize = 11.sp,
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Generated Bengali Lyrics Preview Box
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF0B1329),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        item {
                            Text(
                                text = generatedBengaliStructure,
                                fontSize = 13.sp,
                                color = Color.White,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(generatedBengaliStructure))
                            Toast.makeText(context, "লিরিক্স ক্লিপবোর্ডে কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("কপি করুন", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            onApplyLyrics(generatedBengaliStructure)
                            Toast.makeText(context, "লিরিক্স স্টুডিওতে যুক্ত করা হলো!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("স্টুডিওতে ব্যবহার করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
