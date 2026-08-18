package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

data class RhymeGroup(
    val rootWord: String,
    val rhymes: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BengaliLyricistNotepadDialog(
    initialLyrics: String = "",
    onDismiss: () -> Unit,
    onUseLyrics: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var songSection by remember { mutableStateOf("স্থায়ী (Mukhra / Chorus)") }
    var lyricsMukhra by remember { mutableStateOf(if (initialLyrics.isNotBlank()) initialLyrics else "মেঘের দেশে সুরের খেলা শুরু হলো আজ\nবৃষ্টি ভেজা মিষ্টি হাওয়া ছুঁয়ে যায় প্রাণ") }
    var lyricsAntara by remember { mutableStateOf("তোমার চোখে আমার গান খুঁজে পেল ভাষা\nভোরের আলোয় সুরের ডানায় ছড়িয়ে ভালোবাসা") }
    var lyricsSanchari by remember { mutableStateOf("দোতারার এই ছন্দে নাচে সোনালী রোদ্দুর\nচলতে গিয়ে অচিন সুরে বাজলো সুমধুর") }
    var lyricsAbhog by remember { mutableStateOf("ও সুরের পাখি, দূর আকাশে উড়ো মন খুলে\nআজকে আমি গাইব গান সব দ্বিধা ভুলে") }

    val rhymeGroups = remember {
        listOf(
            RhymeGroup("গান", listOf("প্রাণ", "টান", "বান", "শান", "আসমান", "পাষাণ", "সন্ধান", "অভিমান", "ভাসান", "সমাধান")),
            RhymeGroup("আজ", listOf("লাজ", "কাজ", "রাজ", "সাজ", "সমাজ", "আওয়াজ", "মহারাজ")),
            RhymeGroup("আলো", listOf("ভালো", "কালো", "জ্বালো", "ঢালো", "বাসলো")),
            RhymeGroup("সুর", listOf("দূর", "রোদ্দুর", "সুমধুর", "নূপুর", "ভরদুপুর", "সুরপুর")),
            RhymeGroup("মন", listOf("খন", "বন", "স্বপন", "জীবন", "ভুবন", "আপন", "গোপন", "যতন")),
            RhymeGroup("নদী", listOf("যদি", "নিরবধি", "প্রতিশ্রুতি", "অনাদি", "জলধি")),
            RhymeGroup("রাত", listOf("হাত", "বাত", "প্রভাত", "আঘাত", "উৎপাত", "সাত")),
            RhymeGroup("হাওয়া", listOf("পাওয়া", "যাওয়া", "খাওয়া", "চাওয়া", "গাওয়া"))
        )
    }

    var selectedRhymeGroup by remember { mutableStateOf(rhymeGroups[0]) }
    var customRhymeSearch by remember { mutableStateOf("") }

    // Active text based on selected section
    val currentLyricsText = when (songSection) {
        "স্থায়ী (Mukhra / Chorus)" -> lyricsMukhra
        "অন্তরা (Antara / Verse 1)" -> lyricsAntara
        "সঞ্চারী (Sanchari / Bridge)" -> lyricsSanchari
        else -> lyricsAbhog
    }

    // Bengali Matra (মাত্রা) and Syllable calculation
    val lineCount = currentLyricsText.lines().filter { it.isNotBlank() }.size
    val wordCount = currentLyricsText.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
    // Approximation for Bengali Matra count (vowels and akshars)
    val matraCount = currentLyricsText.filter { !it.isWhitespace() && it != '\n' }.length

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0B0F19),
            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF10B981),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.EditNote, contentDescription = null, tint = Color.Black, modifier = Modifier.size(22.dp))
                            }
                        }
                        Column {
                            Text(
                                text = "বাংলা গীতিকার খাতা ও ছন্দ মিটার",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Lyricist Pad, Matra Counter & Rhyme Engine",
                                fontSize = 11.sp,
                                color = Color(0xFF34D399)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Song Section Selector Tabs (স্থায়ী, অন্তরা, সঞ্চারী, আভোগ)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            "স্থায়ী (Mukhra / Chorus)",
                            "অন্তরা (Antara / Verse 1)",
                            "সঞ্চারী (Sanchari / Bridge)",
                            "আভোগ (Abhog / Outro)"
                        ).forEach { section ->
                            val isSel = songSection == section
                            val shortLabel = when (section) {
                                "স্থায়ী (Mukhra / Chorus)" -> "স্থায়ী"
                                "অন্তরা (Antara / Verse 1)" -> "অন্তরা"
                                "সঞ্চারী (Sanchari / Bridge)" -> "সঞ্চারী"
                                else -> "আভোগ"
                            }
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { songSection = section },
                                color = if (isSel) Color(0xFF10B981) else Color.Transparent
                            ) {
                                Text(
                                    text = shortLabel,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) Color.Black else Color.LightGray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Live Matra & Chhanda Metrics Bar
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column {
                                Text("লাইন সংখ্যা", fontSize = 9.sp, color = Color.Gray)
                                Text("$lineCount টি", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column {
                                Text("শব্দ সংখ্যা", fontSize = 9.sp, color = Color.Gray)
                                Text("$wordCount টি", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                            }
                            Column {
                                Text("মাত্রা (অক্ষরবৃত্ত)", fontSize = 9.sp, color = Color.Gray)
                                Text("$matraCount মাত্রা", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24))
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (matraCount > 0 && matraCount % 8 == 0) "★ সুষম ৮-মাত্রা ছন্দ" else "ছন্দ চলমান",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Main Lyrics Writing Area
                OutlinedTextField(
                    value = currentLyricsText,
                    onValueChange = { newText ->
                        when (songSection) {
                            "স্থায়ী (Mukhra / Chorus)" -> lyricsMukhra = newText
                            "অন্তরা (Antara / Verse 1)" -> lyricsAntara = newText
                            "সঞ্চারী (Sanchari / Bridge)" -> lyricsSanchari = newText
                            else -> lyricsAbhog = newText
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    placeholder = { Text("এখানে বাংলা লিরিক্স বা কবিতার চরণ লিখুন...", fontSize = 13.sp, color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                // Bengali Rhyme Suggestion Box (অন্ত্যমিল খোঁজার ডিকশনারি)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔍 অন্ত্যমিল ডিকশনারি (Rhyme Dictionary):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399))
                            Text("মূল শব্দ: ${selectedRhymeGroup.rootWord}", fontSize = 10.sp, color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold)
                        }

                        // Root word selector
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(rhymeGroups) { grp ->
                                val isSel = selectedRhymeGroup.rootWord == grp.rootWord
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSel) Color(0xFF10B981) else Color.White.copy(alpha = 0.08f),
                                    modifier = Modifier.clickable { selectedRhymeGroup = grp }
                                ) {
                                    Text(
                                        text = grp.rootWord,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) Color.Black else Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        // Rhyming matching words chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(selectedRhymeGroup.rhymes) { rhymeWord ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF0F172A),
                                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                                    modifier = Modifier.clickable {
                                        val appended = if (currentLyricsText.isEmpty()) rhymeWord else "$currentLyricsText $rhymeWord"
                                        when (songSection) {
                                            "স্থায়ী (Mukhra / Chorus)" -> lyricsMukhra = appended
                                            "অন্তরা (Antara / Verse 1)" -> lyricsAntara = appended
                                            "সঞ্চারী (Sanchari / Bridge)" -> lyricsSanchari = appended
                                            else -> lyricsAbhog = appended
                                        }
                                        Toast.makeText(context, "যোগ হয়েছে: $rhymeWord", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Text(
                                        text = "+ $rhymeWord",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF34D399),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Actions: Copy Full Lyrics & Use in Song Creation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val fullCombinedSong = buildString {
                        appendLine("[স্থায়ী]")
                        appendLine(lyricsMukhra)
                        appendLine()
                        appendLine("[অন্তরা]")
                        appendLine(lyricsAntara)
                        appendLine()
                        appendLine("[সঞ্চারী]")
                        appendLine(lyricsSanchari)
                        appendLine()
                        appendLine("[আভোগ]")
                        appendLine(lyricsAbhog)
                    }.trim()

                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(fullCombinedSong))
                            Toast.makeText(context, "সম্পূর্ণ লিরিক্স ক্লিপবোর্ডে কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(46.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("কপি করুন", color = Color.White, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            onUseLyrics(fullCombinedSong)
                            Toast.makeText(context, "গান তৈরিতে সম্পূর্ণ লিরিক্স ব্যবহার করা হচ্ছে!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.3f).height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("গান তৈরিতে যোগ করুন", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
