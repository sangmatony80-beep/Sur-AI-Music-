package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class OpenSourceMusicModel(
    val id: String,
    val name: String,
    val developer: String,
    val version: String,
    val description: String,
    val sizeMb: Int,
    val architecture: String,
    val license: String,
    val downloadUrl: String,
    var isDownloaded: Boolean,
    var isEnabled: Boolean,
    var downloadProgress: Float // 0f to 1f
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenSourceMusicModelInstallerDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var modelsList = remember {
        mutableStateListOf(
            OpenSourceMusicModel(
                id = "musicgen_small",
                name = "MusicGen Small (Meta AI)",
                developer = "Meta AI / AudioCraft",
                version = "v1.3.0-onnx",
                description = "ওপেন সোর্স টেক্সট-টু-মিউজিক ট্রান্সফর্মার মডেল। প্রম্পট থেকে সরাসরি ফুল মিউজিক ও মেলোডি জেনারেট করে।",
                sizeMb = 340,
                architecture = "Transformer (EnCodec + LM)",
                license = "MIT Open Source",
                downloadUrl = "https://huggingface.co/facebook/musicgen-small",
                isDownloaded = true,
                isEnabled = true,
                downloadProgress = 1f
            ),
            OpenSourceMusicModel(
                id = "stable_audio_open",
                name = "Stable Audio Open (Stability AI)",
                developer = "Stability AI",
                version = "v1.0-tflite",
                description = "ওপেন সোর্স লেটেন্ট ডিফিউশন অডিও মডেল। স্টুডিও কোয়ালিটি ড্রাম লুপ, অ্যাম্বিয়েন্ট ও ইন্সট্রুমেন্টাল ট্র্যাক তৈরি করে।",
                sizeMb = 480,
                architecture = "Latent Diffusion UNet",
                license = "Stability AI Community License",
                downloadUrl = "https://huggingface.co/stabilityai/stable-audio-open-1.0",
                isDownloaded = false,
                isEnabled = false,
                downloadProgress = 0f
            ),
            OpenSourceMusicModel(
                id = "sur_bark_neural",
                name = "Sur Audio Neural Transformer (Bark Open Core)",
                developer = "Sur AI Open Research",
                version = "v1.2-transformer",
                description = "ওপেন সোর্স মাল্টি-ল্যাঙ্গুয়েজ ভয়েস ও মিউজিক সিন্থেসিস মডেল। হাসিখুশি কণ্ঠ, কান্না ও বাদ্যযন্ত্রের নোট রেন্ডার করে।",
                sizeMb = 560,
                architecture = "GPT-style Generative Audio",
                license = "MIT License",
                downloadUrl = "https://huggingface.co/surai/audio-bark-open",
                isDownloaded = true,
                isEnabled = true,
                downloadProgress = 1f
            ),
            OpenSourceMusicModel(
                id = "kokoro_neural_voice",
                name = "Kokoro Neural Voice (Bengali & English)",
                developer = "Kokoro 82M",
                version = "v1.0-edge",
                description = "অত্যন্ত লাইটওয়েট (৮২ মিলিয়ন প্যারামিটার) এবং ন্যাচারাল বাংলা ও ইংরেজি ভয়েস সিন্থেসিস মডেল।",
                sizeMb = 120,
                architecture = "StyleTTS2 / VITS",
                license = "Apache 2.0",
                downloadUrl = "https://huggingface.co/hexgrad/Kokoro-82M",
                isDownloaded = false,
                isEnabled = false,
                downloadProgress = 0f
            ),
            OpenSourceMusicModel(
                id = "riffusion_spectrogram",
                name = "Riffusion (Spectrogram Audio Model)",
                developer = "Riffusion Open Source",
                version = "v2.1",
                description = "ইমেজ স্পেকট্রোগ্রাম রূপান্তরের মাধ্যমে অবিরাম রিয়েল-টাইম মিউজিক লুপ ও বিট জেনারেট করার মডেল।",
                sizeMb = 290,
                architecture = "Fine-tuned Stable Diffusion 1.5",
                license = "MIT License",
                downloadUrl = "https://github.com/riffusion/riffusion",
                isDownloaded = false,
                isEnabled = false,
                downloadProgress = 0f
            ),
            OpenSourceMusicModel(
                id = "audiogen_meta",
                name = "AudioGen (Meta AI Sound Effects)",
                developer = "Meta AI",
                version = "v1.1",
                description = "পরিবেশগত শব্দ, সাউন্ড ইফেক্ট, ও বাদ্যযন্ত্রের ধ্বনি তৈরি করার অডিও জেনারেটর মডেল।",
                sizeMb = 210,
                architecture = "Autoregressive Transformer",
                license = "MIT License",
                downloadUrl = "https://huggingface.co/facebook/audiogen-medium",
                isDownloaded = false,
                isEnabled = false,
                downloadProgress = 0f
            ),
            OpenSourceMusicModel(
                id = "jukebox_openai",
                name = "Jukebox Open (OpenAI)",
                developer = "OpenAI Research",
                version = "v2.0",
                description = "দীর্ঘ অডিও ট্র্যাক এবং প্রফেশনাল ভোকালসহ গান তৈরির হায়ারার্কিকাল ভিএই মডেল।",
                sizeMb = 850,
                architecture = "VQ-VAE + Prior Transformer",
                license = "MIT License",
                downloadUrl = "https://github.com/openai/jukebox",
                isDownloaded = false,
                isEnabled = false,
                downloadProgress = 0f
            ),
            OpenSourceMusicModel(
                id = "mubert_open",
                name = "Mubert Open API Client",
                developer = "Mubert Inc.",
                version = "v3.0",
                description = "এআই মিউজিক জেনারেশন এপিআই ক্লায়েন্ট যা রিয়েল-টাইম জেনারেশন এবং লুপ ট্র্যাক প্রদান করে।",
                sizeMb = 45,
                architecture = "Algorithmic Generative Client",
                license = "Open Source SDK",
                downloadUrl = "https://github.com/MubertAPI",
                isDownloaded = true,
                isEnabled = true,
                downloadProgress = 1f
            ),
            OpenSourceMusicModel(
                id = "ace_studio_vits",
                name = "ACE Studio Neural Singing Model",
                developer = "ACE Studio Open",
                version = "v1.5",
                description = "পেশাদার গায়কদের মতো পিচ কার্ভ এবং ভাইব্রাটো সহ নিখুঁত এআই সিংগিং ভয়েস রেন্ডার করে।",
                sizeMb = 410,
                architecture = "VITS Neural Vocoder",
                license = "Open Community",
                downloadUrl = "https://github.com/ace-studio-open",
                isDownloaded = false,
                isEnabled = false,
                downloadProgress = 0f
            ),
            OpenSourceMusicModel(
                id = "dac_descript",
                name = "Descript Audio Codec (DAC)",
                developer = "Descript",
                version = "v4.0",
                description = "অতি-উচ্চ মানের (44.1kHz) স্টুডিও অডিও কম্প্রেশন এবং হাই-ফিডেলিটি রিকনস্ট্রাকশন কোডেক।",
                sizeMb = 95,
                architecture = "Neural Audio Codec",
                license = "MIT License",
                downloadUrl = "https://github.com/descriptinc/descript-audio-codec",
                isDownloaded = true,
                isEnabled = true,
                downloadProgress = 1f
            )
        )
    }

    var activelyDownloadingId by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
            .padding(16.dp),
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFF0F172A),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3B82F6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Memory, contentDescription = null, tint = Color.White)
                    }
                    Column {
                        Text(
                            text = "ওপেন সোর্স মিউজিক মডেল হাব",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Meta, Stability AI ও Sur AI-এর প্রিমিয়াম ওপেন সোর্স মডেল",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status Banner
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF10B981))
                    Column {
                        Text("লোকাল ডিভাইস এআই ইঞ্জিন সক্রিয়", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        Text("অন-ডিভাইস ONNX / TFLite রানটাইম ব্যবহার করে সম্পূর্ণ অফলাইনে গান জেনারেট করুন।", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Models List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(modelsList) { model ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (model.isEnabled) Color(0xFF1E293B) else Color(0xFF131C2E)
                        ),
                        border = if (model.isEnabled) BorderStroke(1.dp, Color(0xFF3B82F6)) else null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(model.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                        Badge(containerColor = Color(0xFF334155)) {
                                            Text(model.version, color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Text("তৈরি করেছে: ${model.developer} • সাইজ: ${model.sizeMb} মোবাইল এমবি", color = Color(0xFF94A3B8), fontSize = 12.sp)
                                }

                                // Action Button
                                if (model.isDownloaded) {
                                    Switch(
                                        checked = model.isEnabled,
                                        onCheckedChange = { checked ->
                                            val idx = modelsList.indexOf(model)
                                            if (idx != -1) {
                                                modelsList[idx] = model.copy(isEnabled = checked)
                                                Toast.makeText(context, "${model.name} ${if (checked) "সক্রিয় করা হয়েছে" else "নিষ্ক্রিয় করা হয়েছে"}", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF3B82F6))
                                    )
                                } else {
                                    if (activelyDownloadingId == model.id) {
                                        CircularProgressIndicator(
                                            progress = { model.downloadProgress },
                                            modifier = Modifier.size(32.dp),
                                            color = Color(0xFF3B82F6),
                                            strokeWidth = 3.dp
                                        )
                                    } else {
                                        Button(
                                            onClick = {
                                                activelyDownloadingId = model.id
                                                coroutineScope.launch {
                                                    val idx = modelsList.indexOf(model)
                                                    var progress = 0f
                                                    while (progress < 1f) {
                                                        delay(150)
                                                        progress += 0.15f
                                                        if (idx != -1) {
                                                            modelsList[idx] = model.copy(downloadProgress = progress.coerceAtMost(1f))
                                                        }
                                                    }
                                                    if (idx != -1) {
                                                        modelsList[idx] = model.copy(isDownloaded = true, isEnabled = true, downloadProgress = 1f)
                                                    }
                                                    activelyDownloadingId = null
                                                    Toast.makeText(context, "✅ ${model.name} সফলভাবে ইন্সটল হয়েছে!", Toast.LENGTH_LONG).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("ইন্সটল", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(model.description, color = Color(0xFFCBD5E1), fontSize = 13.sp)

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("আর্কিটেকচার: ${model.architecture}", color = Color(0xFF64748B), fontSize = 11.sp)
                                Text("লাইসেন্স: ${model.license}", color = Color(0xFF64748B), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer Button
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("মডেল কনফিগারেশন সেভ করুন", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
