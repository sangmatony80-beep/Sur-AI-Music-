package com.example.aimusic

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.view.ViewGroup
import android.webkit.*
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.account.MultiAccountPoolManager
import com.example.ui.components.MultiAccountPoolManagementDialog
import kotlinx.coroutines.launch

/**
 * Sur AI Live Music Studio & Engine Screen.
 * Fully integrates the neural music engine under 100% 'সুর এআই মিউজিক স্টুডিও' (Sur AI Music Studio) branding.
 * Features 100+ temporary email accounts pool, automatic token rate-limit failover, and audio stream capture.
 */
@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiMusicStudio(
    modifier: Modifier = Modifier,
    initialPrompt: String = ""
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val poolManager = remember { MultiAccountPoolManager(context) }
    val poolState by poolManager.poolState.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var showPoolDialog by remember { mutableStateOf(false) }
    var showSessionSwitcherDialog by remember { mutableStateOf(false) }

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var webLoadingProgress by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var customSessionUrlInput by remember { mutableStateOf(poolState.currentSessionUrl) }
    var capturedAudioUrl by remember { mutableStateOf("") }
    var activePrompt by remember { mutableStateOf(initialPrompt) }

    val primarySessionUrl = remember(poolState.currentSessionUrl) { poolState.currentSessionUrl }
    val currentActiveAccount = remember(poolState.accounts, poolState.activeAccountId) {
        poolState.accounts.firstOrNull { it.id == poolState.activeAccountId } ?: poolState.accounts.firstOrNull()
    }

    if (showPoolDialog) {
        MultiAccountPoolManagementDialog(onDismiss = { showPoolDialog = false })
    }

    if (showSessionSwitcherDialog) {
        AlertDialog(
            onDismissRequest = { showSessionSwitcherDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("ইঞ্জিন সেশন লিংক পরিবর্তন", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("ইঞ্জিন ও গুগল মিউজিক লাইভ সেশন URL নির্বাচন বা পেস্ট করুন:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = customSessionUrlInput,
                        onValueChange = { customSessionUrlInput = it },
                        placeholder = { Text("https://www.flowmusic.app/session/3bffdfed-ee48-4f78-a919-566e1fba0d00?t=true") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("দ্রুত নোড লিংক নির্বাচন করুন:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = customSessionUrlInput == "https://www.flowmusic.app/session/3bffdfed-ee48-4f78-a919-566e1fba0d00?t=true",
                                onClick = { customSessionUrlInput = "https://www.flowmusic.app/session/3bffdfed-ee48-4f78-a919-566e1fba0d00?t=true" },
                                label = { Text("গুগল ফ্লো ১ (Flow Master)", fontSize = 10.sp) }
                            )
                            FilterChip(
                                selected = customSessionUrlInput == "https://www.flowmusic.app/create",
                                onClick = { customSessionUrlInput = "https://www.flowmusic.app/create" },
                                label = { Text("গুগল ফ্লো ২ (Flow Create)", fontSize = 10.sp) }
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = customSessionUrlInput == "https://aitestkitchen.withgoogle.com/tools/music-fx",
                                onClick = { customSessionUrlInput = "https://aitestkitchen.withgoogle.com/tools/music-fx" },
                                label = { Text("Google MusicFX (AI Kitchen)", fontSize = 10.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            poolManager.setEngineSessionUrl(customSessionUrlInput)
                            webViewInstance?.loadUrl(customSessionUrlInput)
                            Toast.makeText(context, "ইঞ্জিন সেশন লোড করা হয়েছে!", Toast.LENGTH_SHORT).show()
                            showSessionSwitcherDialog = false
                        }
                    }
                ) {
                    Text("লোড করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSessionSwitcherDialog = false }) { Text("বাতিল") }
            }
        )
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Tab Navigation
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("লাইভ সুর স্টুডিও", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("সুর এআই নেটিভ", fontSize = 12.sp)
                    }
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudQueue, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("১০০+ টেম্প মেইল হাব", fontSize = 12.sp)
                    }
                }
            )
        }

        when (selectedTab) {
            0 -> {
                // Live Interactive Flow Music Studio Engine WebView with 100% Masking
                Column(modifier = Modifier.fillMaxSize()) {
                    // Engine Control & Active Temp Node Toolbar
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 3.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
                            if (activePrompt.isNotBlank()) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "প্রম্পট: $activePrompt",
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                        FilledTonalButton(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                val clip = android.content.ClipData.newPlainText("Prompt", activePrompt)
                                                clipboard.setPrimaryClip(clip)
                                                Toast.makeText(context, "📋 প্রম্পট কপি হয়েছে! ইঞ্জিনের বক্সে পেস্ট করুন।", Toast.LENGTH_SHORT).show()
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(26.dp)
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("কপি করুন", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = {
                                            if (webViewInstance?.canGoBack() == true) {
                                                webViewInstance?.goBack()
                                            } else {
                                                webViewInstance?.loadUrl(primarySessionUrl)
                                            }
                                        },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = { webViewInstance?.reload() },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Reload", modifier = Modifier.size(16.dp))
                                    }
                                    Surface(
                                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(Color(0xFF10B981), CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("সুর এআই স্টুডিও লাইভ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    // 1-Click Fast Rotate Node upon Limit
                                    FilledTonalButton(
                                        onClick = {
                                            scope.launch {
                                                val nextAcc = poolManager.rotateToNextAvailableAccount()
                                                if (nextAcc != null) {
                                                    webViewInstance?.loadUrl(nextAcc.sessionUrl)
                                                    Toast.makeText(context, "🔄 ফ্রেশ নোড স্যুইচ করা হয়েছে: ${nextAcc.email}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Icon(Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("নোড রোটেশন", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { showPoolDialog = true },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Icon(Icons.Default.CloudQueue, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("১০০+ মেইল", fontSize = 9.sp)
                                    }
                                }
                            }

                            // Current Active Temp Node Status Banner
                            if (currentActiveAccount != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "⚡ সক্রিয় নোড: ${currentActiveAccount.email}",
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        "ফ্রি কোটা: ${currentActiveAccount.remainingCredits}/${currentActiveAccount.dailyFreeQuota}",
                                        fontSize = 9.sp,
                                        color = Color(0xFF10B981),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    if (isLoading) {
                        LinearProgressIndicator(
                            progress = { webLoadingProgress / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Captured Audio Download Banner if detected
                    if (capturedAudioUrl.isNotBlank()) {
                        Surface(
                            color = Color(0xFF0F172A),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.DownloadDone, contentDescription = null, tint = Color(0xFF10B981))
                                    Column {
                                        Text("✨ সুর এআই মাস্টার অডিও ট্র্যাক প্রস্তুত!", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                                        Text(capturedAudioUrl.takeLast(35), fontSize = 9.sp, color = Color.Gray)
                                    }
                                }
                                Button(
                                    onClick = {
                                        try {
                                            val request = DownloadManager.Request(Uri.parse(capturedAudioUrl)).apply {
                                                setTitle("Sur_AI_Engine_Master.mp3")
                                                setDescription("সুর এআই ইঞ্জিন থেকে অডিও ফাইল ডাউনলোড হচ্ছে...")
                                                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Sur_AI_Track_${System.currentTimeMillis()}.mp3")
                                                setAllowedOverMetered(true)
                                                setAllowedOverRoaming(true)
                                            }
                                            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                            dm.enqueue(request)
                                            Toast.makeText(context, "ডাউনলোড শুরু হয়েছে!", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "ডাউনলোড এরর: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("MP3 ডাউনলোড", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Embedded Flow Music Studio Engine View with 100% airtight masking
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    databaseEnabled = true
                                    mediaPlaybackRequiresUserGesture = false
                                    allowFileAccess = true
                                    allowContentAccess = true
                                    useWideViewPort = true
                                    loadWithOverviewMode = true
                                    setSupportZoom(true)
                                    builtInZoomControls = true
                                    displayZoomControls = false
                                    userAgentString = "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36 SurAIStudio/5.0"
                                }

                                val cookieManager = CookieManager.getInstance()
                                cookieManager.setAcceptCookie(true)
                                cookieManager.setAcceptThirdPartyCookies(this, true)

                                addJavascriptInterface(object {
                                    @JavascriptInterface
                                    fun onAudioGenerated(url: String) {
                                        capturedAudioUrl = url
                                    }

                                    @JavascriptInterface
                                    fun onRateLimitDetected() {
                                        scope.launch {
                                            val next = poolManager.rotateToNextAvailableAccount()
                                            if (next != null) {
                                                loadUrl(next.sessionUrl)
                                            }
                                        }
                                    }
                                }, "SurAndroidBridge")

                                webChromeClient = object : WebChromeClient() {
                                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                        webLoadingProgress = newProgress
                                        isLoading = newProgress < 100
                                    }
                                }

                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                        val url = request?.url?.toString() ?: return false
                                        if (url.endsWith(".mp3") || url.endsWith(".wav") || url.contains("/audio/") || url.contains("/download")) {
                                            capturedAudioUrl = url
                                        }
                                        return false
                                    }

                                    override fun onLoadResource(view: WebView?, url: String?) {
                                        if (url != null && (url.endsWith(".mp3") || url.endsWith(".wav") || url.contains("blob:") || url.contains("/stream/"))) {
                                            capturedAudioUrl = url
                                        }
                                        super.onLoadResource(view, url)
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        isLoading = false
                                        // Complete Airtight Masking & Audio Interception JS Injection
                                        val maskingJs = """
                                            (function() {
                                                try {
                                                    var style = document.createElement('style');
                                                    style.type = 'text/css';
                                                    style.innerHTML = `
                                                        header, 
                                                        header a[href*="flowmusic"], 
                                                        header img, 
                                                        nav[class*="navbar"],
                                                        footer, 
                                                        .brand-logo, 
                                                        .logo-container,
                                                        [aria-label*="Flow"], 
                                                        [title*="Flow Music"],
                                                        a[href*="twitter.com"],
                                                        a[href*="discord.gg"],
                                                        a[href*="github.com"],
                                                        a[href*="google.com/search"],
                                                        div[class*="pricing"],
                                                        div[class*="upgrade-modal"],
                                                        div[class*="token-exhausted"] {
                                                            display: none !important;
                                                            visibility: hidden !important;
                                                        }
                                                        body {
                                                            background-color: #0B0F19 !important;
                                                            color: #F1F5F9 !important;
                                                        }
                                                    `;
                                                    document.head.appendChild(style);

                                                    function maskBrandText() {
                                                        var walk = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT, null, false);
                                                        var node;
                                                        while(node = walk.nextNode()) {
                                                            if (node.nodeValue && (node.nodeValue.includes('Flow Music') || node.nodeValue.includes('FlowMusic') || node.nodeValue.includes('flowmusic') || node.nodeValue.includes('Google Flow') || node.nodeValue.includes('Google Music') || node.nodeValue.includes('Dialogflow') || node.nodeValue.includes('Google Dialogflow') || node.nodeValue.includes('Gemini'))) {
                                                                node.nodeValue = node.nodeValue
                                                                    .replace(/Google\s*Flow/gi, 'সুর এআই')
                                                                    .replace(/Google\s*Music/gi, 'সুর এআই মিউজিক স্টুডিও')
                                                                    .replace(/Google\s*Dialogflow/gi, 'সুর এআই বট')
                                                                    .replace(/Dialogflow/gi, 'সুর এআই স্টুডিও')
                                                                    .replace(/Flow\s*Music/gi, 'সুর এআই মিউজিক স্টুডিও')
                                                                    .replace(/FlowMusic/gi, 'সুর এআই')
                                                                    .replace(/Gemini/gi, 'Sur AI Neural')
                                                                    .replace(/flowmusic\.app/gi, 'suraistudio.com');
                                                            }
                                                        }
                                                    }
                                                    maskBrandText();
                                                    setInterval(maskBrandText, 1500);

                                                    // Intercept Audio Generation
                                                    function interceptAudio() {
                                                        var audios = document.querySelectorAll('audio, source');
                                                        audios.forEach(function(a) {
                                                            if (a.src && (a.src.includes('.mp3') || a.src.includes('.wav') || a.src.includes('blob:')) && !a.dataset.surCaptured) {
                                                                a.dataset.surCaptured = "true";
                                                                if (window.SurAndroidBridge) {
                                                                    window.SurAndroidBridge.onAudioGenerated(a.src);
                                                                }
                                                            }
                                                        });
                                                    }
                                                    interceptAudio();
                                                    setInterval(interceptAudio, 1500);
                                                } catch(e) {}
                                            })();
                                        """.trimIndent()
                                        view?.evaluateJavascript(maskingJs, null)
                                        super.onPageFinished(view, url)
                                    }
                                }

                                setDownloadListener { url, _, _, _, _ ->
                                    capturedAudioUrl = url
                                    try {
                                        val request = DownloadManager.Request(Uri.parse(url)).apply {
                                            setTitle("Sur_AI_Song.mp3")
                                            setDescription("সুর এআই ইঞ্জিন থেকে গান ডাউনলোড হচ্ছে...")
                                            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Sur_AI_${System.currentTimeMillis()}.mp3")
                                        }
                                        val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                        dm.enqueue(request)
                                        Toast.makeText(ctx, "অডিও ডাউনলোড শুরু হয়েছে!", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(ctx, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                loadUrl(primarySessionUrl)
                                webViewInstance = this
                            }
                        },
                        update = { view ->
                            webViewInstance = view
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            1 -> {
                // Sur AI Native Direct Audio Generation Studio
                AiMusicScreen()
            }

            2 -> {
                // Multi-Account Pool & 100+ Email Engine Hub
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Default.Stars, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text("সুর এআই আনলিমিটেড ১০০+ টেম্পোরারি মেইল পুল", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Text("১০০+ টেম্পোরারি মেইল ও নোড ব্যবহার করে টোকেন লিমিট সম্পূর্ণ এড়িয়ে চলুন। প্রতিটি নোডে ১০০ ফ্রি দৈনিক ক্রেডিট থাকে (মোট ১০,০০০+ ক্রেডিট প্রতিদিন)।", fontSize = 13.sp, lineHeight = 18.sp)

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { showPoolDialog = true },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.CloudQueue, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("১০০+ মেইল হাব", fontSize = 12.sp)
                                }

                                FilledTonalButton(
                                    onClick = {
                                        scope.launch {
                                            poolManager.generate100TempAccounts()
                                            Toast.makeText(context, "১০০+ ফ্রেশ টেম্প মেইল রিচার্জ হয়েছে!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("১-ক্লিকে রিচার্জ", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("💡 সুর এআই ইঞ্জিন ও নোড স্ট্যাটাস:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("• বর্তমান সক্রিয় নোড: ${currentActiveAccount?.email ?: "Active"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Text("• মোট সংযুক্ত নোড: ${poolState.accounts.size}টি অ্যাকাউন্ট", fontSize = 12.sp)
                            Text("• মোট উপলব্ধ দৈনিক ক্রেডিট: ⚡ ${poolState.totalAvailableCredits} টি", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            Text("• অটো-টোকেন বাইপাস রোটেশন এবং ব্র্যান্ডিং মাস্কিং সক্রিয় রয়েছে", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
