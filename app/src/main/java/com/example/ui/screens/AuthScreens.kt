package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.ExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: ExpenseViewModel) {
    var passwordVisible by remember { mutableStateOf(false) }
    val errorMsg by viewModel.authError.collectAsState()
    val isLoading by viewModel.isAuthLoading.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .padding(PayStoryTokens.SpaceLg)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(PayStoryTokens.SpaceXl))

            // App Branding Logo
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(PayStoryTokens.RadiusLg)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✍️",
                    fontSize = 28.sp
                )
            }

            Spacer(modifier = Modifier.height(PayStoryTokens.SpaceMd))

            // App Branding Title & Tagline
            Text(
                text = "PayStory",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Every payment has a story.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = PayStoryTokens.SpaceXs, bottom = PayStoryTokens.Space2Xl),
                textAlign = TextAlign.Center
            )

            // Login Card Form
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = PayStoryTokens.Radius2Xl,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(PayStoryTokens.SpaceXl),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sign In",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        PayStoryPill(
                            text = "v2.4",
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(PayStoryTokens.SpaceBase))

                    // Error Box
                    AnimatedVisibility(visible = errorMsg != null) {
                        errorMsg?.let { msg ->
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = PayStoryTokens.SpaceBase),
                                shape = PayStoryTokens.RadiusMd,
                                border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = msg,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(PayStoryTokens.SpaceMd),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Email Field
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "EMAIL ADDRESS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = PayStoryTokens.SpaceXs)
                        )
                        OutlinedTextField(
                            value = viewModel.authEmail.value,
                            onValueChange = { viewModel.authEmail.value = it },
                            placeholder = { Text("name@example.com", style = MaterialTheme.typography.bodyMedium) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("username_input"),
                            shape = PayStoryTokens.RadiusMd,
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(PayStoryTokens.SpaceBase))

                    // Password Field
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "PASSWORD",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = PayStoryTokens.SpaceXs)
                        )
                        OutlinedTextField(
                            value = viewModel.authPassword.value,
                            onValueChange = { viewModel.authPassword.value = it },
                            placeholder = { Text("••••••••••••", style = MaterialTheme.typography.bodyMedium) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Password",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("password_input"),
                            shape = PayStoryTokens.RadiusMd,
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(PayStoryTokens.SpaceLg))

                    // Sign In Button
                    Button(
                        onClick = { viewModel.handleLogin() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_button"),
                        shape = PayStoryTokens.RadiusFull,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Sign In →",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(PayStoryTokens.SpaceLg))
                }
            }

            Spacer(modifier = Modifier.height(PayStoryTokens.SpaceLg))

            // Create Account Redirection link
            TextButton(
                onClick = {
                    viewModel.setAuthError(null)
                    viewModel.isSignUpActive.value = true
                }
            ) {
                Text(
                    text = "Don't have an account? Create one",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(PayStoryTokens.SpaceSm))

            // Biometric Quick Unlock Pill Button
            OutlinedButton(
                onClick = {
                    viewModel.completeOnboarding()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = PayStoryTokens.RadiusFull,
                border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.outline),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Biometrics",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(PayStoryTokens.SpaceSm))
                Text(
                    text = "Unlock with Biometrics",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
            }

            Spacer(modifier = Modifier.height(PayStoryTokens.SpaceXl))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(viewModel: ExpenseViewModel, onBack: () -> Unit) {
    var passwordVisible by remember { mutableStateOf(false) }
    val errorMsg by viewModel.authError.collectAsState()
    val isLoading by viewModel.isAuthLoading.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .padding(PayStoryTokens.SpaceLg)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(PayStoryTokens.SpaceXl))

            // App Logo
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(PayStoryTokens.RadiusLg)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✍️",
                    fontSize = 28.sp
                )
            }

            Spacer(modifier = Modifier.height(PayStoryTokens.SpaceMd))

            Text(
                text = "Join PayStory",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Start tracking the story behind every transaction.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = PayStoryTokens.SpaceXs, bottom = PayStoryTokens.Space2Xl),
                textAlign = TextAlign.Center
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = PayStoryTokens.Radius2Xl,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(PayStoryTokens.SpaceXl),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Create Profile",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(PayStoryTokens.SpaceBase))

                    // Error status
                    AnimatedVisibility(visible = errorMsg != null) {
                        errorMsg?.let { msg ->
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = PayStoryTokens.SpaceBase),
                                shape = PayStoryTokens.RadiusMd,
                                border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = msg,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(PayStoryTokens.SpaceMd),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Full Name Field
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "FULL NAME",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = PayStoryTokens.SpaceXs)
                        )
                        OutlinedTextField(
                            value = viewModel.authName.value,
                            onValueChange = { viewModel.authName.value = it },
                            placeholder = { Text("e.g. Alex Morgan", style = MaterialTheme.typography.bodyMedium) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Name",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("name_signup"),
                            shape = PayStoryTokens.RadiusMd,
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(PayStoryTokens.SpaceBase))

                    // Email Field
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "EMAIL ADDRESS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = PayStoryTokens.SpaceXs)
                        )
                        OutlinedTextField(
                            value = viewModel.authEmail.value,
                            onValueChange = { viewModel.authEmail.value = it },
                            placeholder = { Text("name@example.com", style = MaterialTheme.typography.bodyMedium) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("email_signup"),
                            shape = PayStoryTokens.RadiusMd,
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(PayStoryTokens.SpaceBase))

                    // Password Field
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PASSWORD",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = PayStoryTokens.SpaceXs)
                            )
                            Text(
                                text = "Strong",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = PayStoryTokens.SpaceXs)
                            )
                        }
                        OutlinedTextField(
                            value = viewModel.authPassword.value,
                            onValueChange = { viewModel.authPassword.value = it },
                            placeholder = { Text("Min. 6 characters", style = MaterialTheme.typography.bodyMedium) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Password",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("password_signup"),
                            shape = PayStoryTokens.RadiusMd,
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(PayStoryTokens.SpaceLg))

                    // Sign Up CTA
                    Button(
                        onClick = { viewModel.handleSignUp() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("register_button"),
                        shape = PayStoryTokens.RadiusFull,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Create Account →",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(PayStoryTokens.SpaceLg))

            TextButton(onClick = onBack) {
                Text(
                    "Already have an account? Sign In",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(PayStoryTokens.SpaceXl))
        }
    }
}
