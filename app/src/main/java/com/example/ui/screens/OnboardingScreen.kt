package com.example.ui.screens

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.ExpenseViewModel

// Helper check function to verify if listener service is running/authorized
fun isNotificationServiceEnabled(context: Context): Boolean {
    val pkgName = context.packageName
    val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
    if (!flat.isNullOrEmpty()) {
        val names = flat.split(":")
        for (name in names) {
            val cn = ComponentName.unflattenFromString(name)
            if (cn != null && cn.packageName == pkgName) {
                return true
            }
        }
    }
    return false
}

data class OnboardingStep(
    val stepNumber: Int,
    val badge: String,
    val title: String,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(viewModel: ExpenseViewModel) {
    val context = LocalContext.current
    val overallLimit by viewModel.overallBudgetLimit.collectAsState()
    var isPermissionGranted by remember { mutableStateOf(isNotificationServiceEnabled(context)) }

    // Periodic state polling while user returns from Settings page
    LaunchedEffect(Unit) {
        while (true) {
            val enabled = isNotificationServiceEnabled(context)
            if (isPermissionGranted != enabled) {
                isPermissionGranted = enabled
            }
            kotlinx.coroutines.delay(1500)
        }
    }

    val steps = remember {
        listOf(
            OnboardingStep(
                stepNumber = 1,
                badge = "Create Your Space",
                title = "Welcome to PayStory",
                description = "Start your automated story ledger with clean, private financial tracking."
            ),
            OnboardingStep(
                stepNumber = 2,
                badge = "Auto-Detection Engine",
                title = "Capture Spends Instantly",
                description = "We scan incoming transactional alerts completely offline to make logging effortless."
            ),
            OnboardingStep(
                stepNumber = 3,
                badge = "Category Budgets",
                title = "Every Payment Has a Story",
                description = "Add notes and categories to every transaction so future you always remembers why it happened."
            )
        )
    }

    var currentStepIndex by remember { mutableStateOf(0) }
    val currentStep = steps[currentStepIndex]
    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(PayStoryTokens.SpaceLg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Top Bar: Logo, Step Indicator & Skip CTA
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceSm)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(PayStoryTokens.RadiusMd)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✍️", fontSize = 16.sp)
                        }
                        Text(
                            text = "PayStory",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    if (currentStepIndex < steps.size - 1) {
                        TextButton(
                            onClick = { viewModel.completeOnboarding() },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                        ) {
                            Text(
                                text = "Skip",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                }

                Spacer(modifier = Modifier.height(PayStoryTokens.SpaceMd))

                // Progress Step Indicators (Segmented Bar)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    steps.forEachIndexed { index, _ ->
                        val isActive = index <= currentStepIndex
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(PayStoryTokens.RadiusFull)
                                .background(
                                    if (isActive) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(PayStoryTokens.SpaceSm))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "STEP ${currentStepIndex + 1} OF ${steps.size}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Animated Center Content
            AnimatedContent(
                targetState = currentStepIndex,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width } + fadeIn()) togetherWith
                                (slideOutHorizontally { width -> -width } + fadeOut())
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()) togetherWith
                                (slideOutHorizontally { width -> width } + fadeOut())
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                label = "OnboardingStepTransition"
            ) { targetIndex ->
                val step = steps[targetIndex]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Top
                ) {
                    Spacer(modifier = Modifier.height(PayStoryTokens.SpaceLg))

                    // Green Pill Badge
                    PayStoryPill(
                        text = step.badge,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        dotColor = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(PayStoryTokens.SpaceMd))

                    // Heading & Subtitle
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(PayStoryTokens.SpaceSm))

                    Text(
                        text = step.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(PayStoryTokens.SpaceXl))

                    // Step 1 Body: Profile Details Form
                    if (targetIndex == 0) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = PayStoryTokens.Radius2Xl,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(PayStoryTokens.SpaceLg),
                                verticalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceBase)
                            ) {
                                // Full Name
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Full Name",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "As on bank account",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(PayStoryTokens.SpaceXs))
                                    OutlinedTextField(
                                        value = viewModel.authName.value,
                                        onValueChange = { viewModel.authName.value = it },
                                        placeholder = { Text("Enter your name") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
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

                                // Email
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "Email Address",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(PayStoryTokens.SpaceXs))
                                    OutlinedTextField(
                                        value = viewModel.authEmail.value,
                                        onValueChange = { viewModel.authEmail.value = it },
                                        placeholder = { Text("Enter your email") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Email,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
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

                                // Password
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Security PIN / Password",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Strong",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(PayStoryTokens.SpaceXs))
                                    OutlinedTextField(
                                        value = viewModel.authPassword.value,
                                        onValueChange = { viewModel.authPassword.value = it },
                                        placeholder = { Text("Enter security PIN or password") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        },
                                        trailingIcon = {
                                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                                Icon(
                                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
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

                                // Currency Preview Card
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = PayStoryTokens.RadiusLg,
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(PayStoryTokens.SpaceMd),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceMd)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("₹", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                        }
                                        Column {
                                            Text(
                                                text = "Default Currency: Indian Rupee (₹)",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Formatted with high-contrast tabular figures.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else if (targetIndex == 1) {
                        // Step 2 Body: Notification & SMS Permission Setup
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = PayStoryTokens.Radius2Xl,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(PayStoryTokens.SpaceLg),
                                verticalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceBase)
                            ) {
                                Text(
                                    text = "Automated Transaction Tracking",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "PayStory reads incoming bank SMS and UPI payment notifications entirely on your device. Zero cloud uploads, zero privacy compromises.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (isPermissionGranted) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = PayStoryTokens.RadiusMd,
                                        border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(PayStoryTokens.SpaceMd),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceSm)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "Notification Listener Active",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
                                        shape = PayStoryTokens.RadiusFull,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.tertiary,
                                            contentColor = MaterialTheme.colorScheme.onTertiary
                                        )
                                    ) {
                                        Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(PayStoryTokens.SpaceSm))
                                        Text("Authorize Notification Access", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    }
                                }
                            }
                        }
                    } else {
                        // Step 3 Body: Ready to launch
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = PayStoryTokens.Radius2Xl,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(PayStoryTokens.SpaceLg),
                                verticalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceBase)
                            ) {
                                Text(
                                    text = "Ready to Begin",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Your personalized expense story is prepared. Track dining, travel, shopping, and bills with smart auto-categorization and budget alerts.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = PayStoryTokens.RadiusMd,
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Column(modifier = Modifier.padding(PayStoryTokens.SpaceMd)) {
                                        Text(
                                            text = "🎯 Monthly Budget Cap: ₹${"%,.0f".format(overallLimit)}",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "You can adjust category thresholds at any time in the Budgets tab.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom CTA Button
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        if (currentStepIndex < steps.size - 1) {
                            currentStepIndex++
                        } else {
                            viewModel.completeOnboarding()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = PayStoryTokens.RadiusFull,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = if (currentStepIndex < steps.size - 1) "Continue to Next Step →" else "Continue to Dashboard →",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
