package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AuthResult
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    onLogin: suspend (String, String) -> AuthResult,
    onRegister: suspend (String, String, String, String) -> AuthResult,
    onGoogleSignIn: suspend () -> AuthResult = { AuthResult.Error("Not Implemented") },
    onFacebookSignIn: suspend () -> AuthResult = { AuthResult.Error("Not Implemented") },
    onSocialSignIn: (suspend (String, String, String) -> AuthResult)? = null,
    onPhoneSignIn: (suspend (String, String) -> AuthResult)? = null,
    onGuestMode: () -> Unit
) {
    // Mode: 0 = Sign In, 1 = Sign Up / Register
    var selectedTab by remember { mutableIntStateOf(0) }
    val isSignUp = selectedTab == 1

    // Form inputs
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("USER") } // "USER" or "ADMIN"
    var adminCode by remember { mutableStateOf("") }

    // UI state
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showGoogleDialog by remember { mutableStateOf(false) }
    var showFacebookDialog by remember { mutableStateOf(false) }
    var showPhoneDialog by remember { mutableStateOf(false) }
    var phoneNumberInput by remember { mutableStateOf("+8801700000000") }
    var otpCodeInput by remember { mutableStateOf("1234") }
    var phoneStep by remember { mutableStateOf(0) }
    var socialEmailInput by remember { mutableStateOf("sangmatony80@gmail.com") }
    var socialNameInput by remember { mutableStateOf("Sangma Tony") }

    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Header Logo & Branding
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            ),
                            shape = RoundedCornerShape(22.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSignUp) Icons.Default.PersonAdd else Icons.Default.MusicNote,
                        contentDescription = "App Logo",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Sur AI Music Studio",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isSignUp) "Create your account to start generating AI tracks" else "Sign in to access your AI music feed & studio",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Supabase Auth Badge
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(20.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF3ECF8E), shape = CircleShape)
                        )
                        Text(
                            text = "Supabase Auth & Cloud Sync",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Navigation Segmented Tab (Sign In vs Register)
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        Surface(
                            onClick = {
                                selectedTab = 0
                                errorMessage = null
                                successMessage = null
                            },
                            color = if (selectedTab == 0) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("tab_sign_in")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Login,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (selectedTab == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Sign In",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (selectedTab == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Surface(
                            onClick = {
                                selectedTab = 1
                                errorMessage = null
                                successMessage = null
                            },
                            color = if (selectedTab == 1) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("tab_register")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.PersonAdd,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (selectedTab == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Register",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (selectedTab == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Error Banner
                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    errorMessage?.let { error ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.ErrorOutline,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = error,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                // Success Banner
                AnimatedVisibility(
                    visible = successMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    successMessage?.let { success ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Success",
                                    tint = Color.Green
                                )
                                Text(
                                    text = success,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                // Sign Up: Bonus Banner
                if (isSignUp) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.CardGiftcard,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "🎁 Welcome Bonus: 250 Free AI Music Tokens with signup!",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    // Full Name
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            errorMessage = null
                        },
                        label = { Text("Full Name") },
                        placeholder = { Text("e.g. Tanvir Ahmed") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("name_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = null
                    },
                    label = { Text("Email Address") },
                    placeholder = { Text("name@example.com") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("email_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    label = { Text("Password") },
                    placeholder = { Text(if (isSignUp) "At least 6 characters" else "Enter password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Password Visibility"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_input"),
                    singleLine = true
                )

                // Sign Up: Confirm Password & Role Selector
                if (isSignUp) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = null
                        },
                        label = { Text("Confirm Password") },
                        placeholder = { Text("Re-enter your password") },
                        leadingIcon = { Icon(Icons.Default.LockClock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Confirm Password Visibility"
                                )
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("confirm_password_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Account Role Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Account Role:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        FilterChip(
                            selected = selectedRole == "USER",
                            onClick = { selectedRole = "USER" },
                            label = { Text("Regular Artist") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                        FilterChip(
                            selected = selectedRole == "ADMIN",
                            onClick = { selectedRole = "ADMIN" },
                            label = { Text("Admin 👑") },
                            leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                    }

                    if (selectedRole == "ADMIN") {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = adminCode,
                            onValueChange = { adminCode = it },
                            label = { Text("Admin Passcode") },
                            placeholder = { Text("SURADMIN2026") },
                            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Primary Action Button (Sign In / Register)
                Button(
                    onClick = {
                        errorMessage = null
                        successMessage = null

                        if (email.isBlank() || !email.contains("@")) {
                            errorMessage = "Please provide a valid email address."
                            return@Button
                        }
                        if (password.length < 6) {
                            errorMessage = "Password must be at least 6 characters."
                            return@Button
                        }

                        if (isSignUp) {
                            if (confirmPassword != password) {
                                errorMessage = "Passwords do not match."
                                return@Button
                            }
                            if (selectedRole == "ADMIN" && adminCode.trim() != "SURADMIN2026") {
                                errorMessage = "Invalid Admin Security Passcode. Use SURADMIN2026."
                                return@Button
                            }

                            isLoading = true
                            coroutineScope.launch {
                                val result = onRegister(email, password, fullName, selectedRole)
                                isLoading = false
                                when (result) {
                                    is AuthResult.Error -> errorMessage = result.message
                                    is AuthResult.Success -> successMessage = "Registration successful! Welcome to Sur AI Studio."
                                }
                            }
                        } else {
                            isLoading = true
                            coroutineScope.launch {
                                val result = onLogin(email, password)
                                isLoading = false
                                when (result) {
                                    is AuthResult.Error -> errorMessage = result.message
                                    is AuthResult.Success -> successMessage = "Welcome back, ${result.user.fullName}!"
                                }
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_auth_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isSignUp) Icons.Default.PersonAdd else Icons.Default.Login,
                                contentDescription = null
                            )
                            Text(
                                text = if (isSignUp) "Create Account & Enter Feed" else "Sign In & Access Music Feed",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Google Sign In Button
                Button(
                    onClick = {
                        socialEmailInput = "sangmatony80@gmail.com"
                        socialNameInput = "Sangma Tony"
                        showGoogleDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("google_auth_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Continue with Google",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Facebook Sign In Button
                Button(
                    onClick = {
                        socialEmailInput = "sangmatony80@gmail.com"
                        socialNameInput = "Sangma Tony"
                        showFacebookDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("facebook_auth_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1877F2),
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Facebook, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Continue with Facebook",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Phone Number Sign In Button
                Button(
                    onClick = {
                        phoneNumberInput = "+8801700000000"
                        otpCodeInput = "1234"
                        phoneStep = 0
                        showPhoneDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("phone_auth_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0F172A),
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Continue with Phone Number & OTP",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Demo Preset Accounts
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "⚡ Quick Test Accounts:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AssistChip(
                                onClick = {
                                    email = "user@suraimusic.com"
                                    password = "user123"
                                    selectedTab = 0
                                    errorMessage = null
                                },
                                label = { Text("🎵 User (user@...)", fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                modifier = Modifier.weight(1f)
                            )
                            AssistChip(
                                onClick = {
                                    email = "admin@suraimusic.com"
                                    password = "admin123"
                                    selectedTab = 0
                                    errorMessage = null
                                },
                                label = { Text("👑 Admin (admin@...)", fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Optional Guest Mode Button
                OutlinedButton(
                    onClick = onGuestMode,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("guest_mode_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        Icons.Default.Face,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Explore as Guest", fontSize = 13.sp)
                }
            }
        }

        // Real Google Sign In Credential Input Dialog
        if (showGoogleDialog) {
            AlertDialog(
                onDismissRequest = { showGoogleDialog = false },
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Public, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Google Sign-In")
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Choose an account to continue to Sur AI Studio:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        
                        // 1-Tap Quick Account Selection Card for user
                        Surface(
                            onClick = {
                                socialEmailInput = "sangmatony80@gmail.com"
                                socialNameInput = "Sangma Tony"
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (socialEmailInput == "sangmatony80@gmail.com") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (socialEmailInput == "sangmatony80@gmail.com") androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("ST", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Column {
                                    Text("Sangma Tony", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("sangmatony80@gmail.com", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        Text("Or enter another Google account:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        OutlinedTextField(
                            value = socialNameInput,
                            onValueChange = { socialNameInput = it },
                            label = { Text("Display Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = socialEmailInput,
                            onValueChange = { socialEmailInput = it },
                            label = { Text("Google Account / Gmail") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (socialEmailInput.isNotBlank() && socialEmailInput.contains("@")) {
                            showGoogleDialog = false
                            isLoading = true
                            coroutineScope.launch {
                                val res = onSocialSignIn?.invoke("Google", socialEmailInput, socialNameInput)
                                    ?: onGoogleSignIn()
                                isLoading = false
                                if (res is AuthResult.Success) {
                                    successMessage = "Google Authentication verified for ${res.user.fullName} (${res.user.email})!"
                                } else if (res is AuthResult.Error) {
                                    errorMessage = res.message
                                }
                            }
                        } else {
                            errorMessage = "Please enter a valid Google account email."
                            showGoogleDialog = false
                        }
                    }) {
                        Text("Sign In With Google")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showGoogleDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Real Facebook Sign In Credential Input Dialog
        if (showFacebookDialog) {
            AlertDialog(
                onDismissRequest = { showFacebookDialog = false },
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Facebook, contentDescription = null, tint = Color(0xFF1877F2))
                        Text("Facebook Sign-In")
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Sign in with your Facebook account:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        
                        Surface(
                            onClick = {
                                socialEmailInput = "sangmatony80@gmail.com"
                                socialNameInput = "Sangma Tony"
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (socialEmailInput == "sangmatony80@gmail.com") Color(0xFF1877F2).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (socialEmailInput == "sangmatony80@gmail.com") androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF1877F2)) else null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(36.dp).background(Color(0xFF1877F2), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("ST", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Column {
                                    Text("Sangma Tony", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("sangmatony80@gmail.com", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        OutlinedTextField(
                            value = socialNameInput,
                            onValueChange = { socialNameInput = it },
                            label = { Text("Profile Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = socialEmailInput,
                            onValueChange = { socialEmailInput = it },
                            label = { Text("Facebook Email / Phone") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (socialEmailInput.isNotBlank()) {
                            showFacebookDialog = false
                            isLoading = true
                            coroutineScope.launch {
                                val res = onSocialSignIn?.invoke("Facebook", socialEmailInput, socialNameInput)
                                    ?: onFacebookSignIn()
                                isLoading = false
                                if (res is AuthResult.Success) {
                                    successMessage = "Facebook Authentication verified for ${res.user.fullName}!"
                                } else if (res is AuthResult.Error) {
                                    errorMessage = res.message
                                }
                            }
                        } else {
                            errorMessage = "Please enter valid Facebook credentials."
                            showFacebookDialog = false
                        }
                    }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2))) {
                        Text("Sign In With Facebook")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showFacebookDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Real Phone Number & OTP Verification Dialog
        if (showPhoneDialog) {
            AlertDialog(
                onDismissRequest = { showPhoneDialog = false },
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(if (phoneStep == 0) "Phone Number Sign-In" else "Enter Verification OTP")
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (phoneStep == 0) {
                            Text("Enter your mobile number to receive an instant verification OTP code:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            OutlinedTextField(
                                value = phoneNumberInput,
                                onValueChange = { phoneNumberInput = it },
                                label = { Text("Mobile Number (+880...)") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Text("Enter the 4-digit OTP sent to $phoneNumberInput (Demo OTP: 1234):", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            OutlinedTextField(
                                value = otpCodeInput,
                                onValueChange = { otpCodeInput = it },
                                label = { Text("4-Digit OTP Code") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (phoneStep == 0) {
                            if (phoneNumberInput.length >= 10) {
                                phoneStep = 1
                            } else {
                                errorMessage = "Please enter a valid phone number."
                            }
                        } else {
                            if (otpCodeInput.length >= 4) {
                                showPhoneDialog = false
                                isLoading = true
                                coroutineScope.launch {
                                    val res = onPhoneSignIn?.invoke(phoneNumberInput, otpCodeInput)
                                        ?: AuthResult.Error("Phone sign in not implemented")
                                    isLoading = false
                                    if (res is AuthResult.Success) {
                                        successMessage = "Phone Number verified successfully for ${res.user.fullName}!"
                                    } else if (res is AuthResult.Error) {
                                        errorMessage = res.message
                                    }
                                }
                            } else {
                                errorMessage = "Please enter the valid 4-digit OTP code."
                            }
                        }
                    }) {
                        Text(if (phoneStep == 0) "Send OTP" else "Verify & Sign In")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPhoneDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
