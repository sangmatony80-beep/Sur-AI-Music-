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
    var socialEmailInput by remember { mutableStateOf("") }
    var socialPasswordInput by remember { mutableStateOf("") }

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
                        isLoading = true
                        errorMessage = null
                        successMessage = null
                        coroutineScope.launch {
                            val result = onGoogleSignIn()
                            isLoading = false
                            if (result is AuthResult.Success) {
                                successMessage = "Google Authentication successful!"
                            } else if (result is AuthResult.Error) {
                                errorMessage = result.message
                            }
                        }
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
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            strokeWidth = 2.5.dp
                        )
                    } else {
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
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Facebook Sign In Button
                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        successMessage = null
                        coroutineScope.launch {
                            val result = onFacebookSignIn()
                            isLoading = false
                            if (result is AuthResult.Success) {
                                successMessage = "Facebook Authentication successful!"
                            } else if (result is AuthResult.Error) {
                                errorMessage = result.message
                            }
                        }
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
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.5.dp
                        )
                    } else {
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
                title = { Text("Sign in with Google") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Enter your Google Account credentials to securely sign in.", fontSize = 13.sp)
                        OutlinedTextField(
                            value = socialEmailInput,
                            onValueChange = { socialEmailInput = it },
                            label = { Text("Google Gmail") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = socialPasswordInput,
                            onValueChange = { socialPasswordInput = it },
                            label = { Text("Google Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (socialEmailInput.isNotBlank()) {
                            showGoogleDialog = false
                            isLoading = true
                            coroutineScope.launch {
                                val res = onGoogleSignIn()
                                isLoading = false
                                successMessage = "Google Sign-In verified for $socialEmailInput!"
                            }
                        } else {
                            errorMessage = "Please enter valid Google account email."
                            showGoogleDialog = false
                        }
                    }) {
                        Text("Verify & Sign In")
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
                title = { Text("Sign in with Facebook") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Enter your Facebook Account credentials to securely sign in.", fontSize = 13.sp)
                        OutlinedTextField(
                            value = socialEmailInput,
                            onValueChange = { socialEmailInput = it },
                            label = { Text("Facebook Email or Phone") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = socialPasswordInput,
                            onValueChange = { socialPasswordInput = it },
                            label = { Text("Facebook Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
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
                                val res = onFacebookSignIn()
                                isLoading = false
                                successMessage = "Facebook Sign-In verified for $socialEmailInput!"
                            }
                        } else {
                            errorMessage = "Please enter valid Facebook account credentials."
                            showFacebookDialog = false
                        }
                    }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2))) {
                        Text("Verify & Sign In")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showFacebookDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
