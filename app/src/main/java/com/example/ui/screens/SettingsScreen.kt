package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.models.Category
import com.example.ui.viewmodel.ExpenseViewModel
import com.example.ui.theme.*
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: ExpenseViewModel) {
    val context = LocalContext.current
    val isDarkTheme by viewModel.isDarkMode.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var isNotificationAccessGranted by remember { mutableStateOf(isNotificationServiceEnabled(context)) }
    var isSmsAccessGranted by remember {
        mutableStateOf(
            context.checkSelfPermission(android.Manifest.permission.RECEIVE_SMS) == android.content.pm.PackageManager.PERMISSION_GRANTED &&
            context.checkSelfPermission(android.Manifest.permission.READ_SMS) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    val powerManager = remember(context) { context.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager }
    var isIgnoringBatteryOptimizations by remember {
        mutableStateOf(powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false)
    }

    var showCategoryViewer by remember { mutableStateOf(false) }
    var showMerchantMappingsViewer by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            val enabled = isNotificationServiceEnabled(context)
            if (isNotificationAccessGranted != enabled) isNotificationAccessGranted = enabled

            val smsEnabled = context.checkSelfPermission(android.Manifest.permission.RECEIVE_SMS) == android.content.pm.PackageManager.PERMISSION_GRANTED &&
                    context.checkSelfPermission(android.Manifest.permission.READ_SMS) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (isSmsAccessGranted != smsEnabled) isSmsAccessGranted = smsEnabled

            val pm = context.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
            val batteryExempted = pm?.isIgnoringBatteryOptimizations(context.packageName) ?: false
            if (isIgnoringBatteryOptimizations != batteryExempted) isIgnoringBatteryOptimizations = batteryExempted

            kotlinx.coroutines.delay(1200)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(bottom = PayStoryTokens.Space2Xl),
            verticalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceLg)
        ) {
            // TOP APP BAR
            PayStoryTopAppBar(
                userInitials = currentUser?.name?.take(2)?.uppercase()?.ifBlank { "PS" } ?: "PS"
            )

            // TITLE & SUBTITLE
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PayStoryTokens.SpaceLg)
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Preferences, privacy & sync rules",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // USER PROFILE CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PayStoryTokens.SpaceLg),
                shape = PayStoryTokens.Radius2Xl,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(PayStoryTokens.SpaceLg),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceMd)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentUser?.name?.take(2)?.uppercase()?.ifBlank { "PS" } ?: "PS",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser?.name?.ifBlank { "PayStory User" } ?: "PayStory User",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = currentUser?.email?.ifBlank { "Personal Space" } ?: "Personal Space",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    PayStoryPill(
                        text = "PRO",
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // SECTION 1: TRANSACTION TRACKING
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PayStoryTokens.SpaceLg),
                verticalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceSm)
            ) {
                Text(
                    text = "TRANSACTION TRACKING",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Notification Access Row
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                            context.startActivity(intent)
                        }
                        .testTag("notification_access_setting"),
                    shape = PayStoryTokens.RadiusLg,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(PayStoryTokens.SpaceMd),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceMd),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(PayStoryTokens.RadiusMd)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notification Listener",
                                    tint = if (isNotificationAccessGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Notification Listener Access",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Auto-log UPI payments from GPay, PhonePe, Paytm",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        PayStoryPill(
                            text = if (isNotificationAccessGranted) "Authorized" else "Enable",
                            containerColor = if (isNotificationAccessGranted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                            contentColor = if (isNotificationAccessGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }

                // SMS Permission Row
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = android.net.Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                        .testTag("sms_access_setting"),
                    shape = PayStoryTokens.RadiusLg,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(PayStoryTokens.SpaceMd),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceMd),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(PayStoryTokens.RadiusMd)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "SMS Permission",
                                    tint = if (isSmsAccessGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "SMS Permission Status",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Parse bank debit/credit texts with spam rejection",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        PayStoryPill(
                            text = if (isSmsAccessGranted) "Authorized" else "Enable",
                            containerColor = if (isSmsAccessGranted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                            contentColor = if (isSmsAccessGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }

                // Battery Saver Exemption
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            try {
                                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                    data = android.net.Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                try {
                                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                    context.startActivity(intent)
                                } catch (ex: Exception) {
                                    android.util.Log.e("SettingsScreen", "Failed to launch battery settings", ex)
                                }
                            }
                        }
                        .testTag("battery_optimization_setting"),
                    shape = PayStoryTokens.RadiusLg,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(PayStoryTokens.SpaceMd),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceMd),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(PayStoryTokens.RadiusMd)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BatteryChargingFull,
                                    contentDescription = "Battery Saver",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Battery Saver Exemption",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Prevent Android from suspending background parsers",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        PayStoryPill(
                            text = if (isIgnoringBatteryOptimizations) "Unrestricted" else "Optimize",
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // SECTION 2: INTELLIGENCE & CATEGORIES
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PayStoryTokens.SpaceLg),
                verticalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceSm)
            ) {
                Text(
                    text = "INTELLIGENCE & CATEGORIES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // PayStory Smart Mappings
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showMerchantMappingsViewer = true }
                        .testTag("manage_merchant_mappings_setting"),
                    shape = PayStoryTokens.RadiusLg,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(PayStoryTokens.SpaceMd),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceMd),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(PayStoryTokens.RadiusMd)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = "Mappings",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Smart Rules & Mappings",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Custom merchant categories & learned story contexts",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Predefined Categories
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCategoryViewer = true }
                        .testTag("manage_categories_setting"),
                    shape = PayStoryTokens.RadiusLg,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(PayStoryTokens.SpaceMd),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceMd),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(PayStoryTokens.RadiusMd)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Label,
                                    contentDescription = "Categories",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Predefined Categories",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Inspect and explore active category taxonomy",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // SECTION 3: PREFERENCES
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PayStoryTokens.SpaceLg),
                verticalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceSm)
            ) {
                Text(
                    text = "PREFERENCES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Dark Mode Switch
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dark_mode_setting"),
                    shape = PayStoryTokens.RadiusLg,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(PayStoryTokens.SpaceMd),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceMd),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(PayStoryTokens.RadiusMd)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DarkMode,
                                    contentDescription = "Dark Mode",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Dark Theme",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Deep Slate OLED palette",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { viewModel.toggleDarkMode() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            // SECTION 4: SESSION & ACCOUNT
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PayStoryTokens.SpaceLg),
                verticalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceSm)
            ) {
                OutlinedButton(
                    onClick = { viewModel.handleLogout() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("logout_button"),
                    shape = PayStoryTokens.RadiusFull,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Log Out")
                    Spacer(modifier = Modifier.width(PayStoryTokens.SpaceSm))
                    Text(
                        "Sign Out of PayStory",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(PayStoryTokens.SpaceSm))

                Text(
                    text = "PayStory • V1.0 Stable\nEvery payment has a story.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // CATEGORY DIALOG VIEWER
    if (showCategoryViewer) {
        Dialog(onDismissRequest = { showCategoryViewer = false }) {
            Card(
                shape = PayStoryTokens.Radius2Xl,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(PayStoryTokens.SpaceXl),
                    verticalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceBase),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Spend Categories",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = { showCategoryViewer = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        color = Color.Transparent
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceSm),
                            verticalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceSm)
                        ) {
                            items(Category.values().filter { it != Category.UNCATEGORIZED }) { cat ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = PayStoryTokens.RadiusMd
                                ) {
                                    Row(
                                        modifier = Modifier.padding(PayStoryTokens.SpaceSm),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(cat.icon, fontSize = 20.sp)
                                        Text(
                                            text = cat.displayName,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // MERCHANT MAPPINGS DIALOG VIEWER
    if (showMerchantMappingsViewer) {
        val mappings by viewModel.merchantMappings.collectAsState()
        var editTargetMerchant by remember { mutableStateOf<String?>(null) }
        var editCategory by remember { mutableStateOf(Category.FOOD.name) }
        var editStory by remember { mutableStateOf("") }

        var isAddingNew by remember { mutableStateOf(false) }
        var newMerchant by remember { mutableStateOf("") }
        var newCategory by remember { mutableStateOf(Category.FOOD.name) }
        var newStory by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showMerchantMappingsViewer = false }) {
            Card(
                shape = PayStoryTokens.Radius2Xl,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
            ) {
                Column(
                    modifier = Modifier
                        .padding(PayStoryTokens.SpaceLg)
                        .fillMaxSize()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = PayStoryTokens.SpaceSm),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Smart Rules & Mappings",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Auto-pilot classification mappings",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { showMerchantMappingsViewer = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceSm)
                    ) {
                        if (isAddingNew) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = PayStoryTokens.RadiusLg
                            ) {
                                Column(
                                    modifier = Modifier.padding(PayStoryTokens.SpaceMd),
                                    verticalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceSm)
                                ) {
                                    Text(
                                        text = "Add Intelligence Rule",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    OutlinedTextField(
                                        value = newMerchant,
                                        onValueChange = { newMerchant = it },
                                        placeholder = { Text("Merchant name (e.g. Swiggy)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = PayStoryTokens.RadiusMd
                                    )

                                    Text("Suggested Category", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceXs)
                                    ) {
                                        Category.values().filter { it != Category.UNCATEGORIZED }.forEach { cat ->
                                            FilterChip(
                                                selected = newCategory == cat.name,
                                                onClick = { newCategory = cat.name },
                                                label = { Text("${cat.icon} ${cat.displayName}") }
                                            )
                                        }
                                    }

                                    OutlinedTextField(
                                        value = newStory,
                                        onValueChange = { newStory = it },
                                        placeholder = { Text("Story text (e.g. Late night snacks)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = PayStoryTokens.RadiusMd
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(onClick = { isAddingNew = false }) {
                                            Text("Cancel")
                                        }
                                        Button(
                                            onClick = {
                                                if (newMerchant.isNotBlank() && newStory.isNotBlank()) {
                                                    viewModel.learnOrUpdateMerchantMapping(newMerchant, newCategory, newStory)
                                                    newMerchant = ""
                                                    newStory = ""
                                                    isAddingNew = false
                                                }
                                            },
                                            shape = PayStoryTokens.RadiusFull,
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Text("Add Rule")
                                        }
                                    }
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = { isAddingNew = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = PayStoryTokens.RadiusFull
                            ) {
                                Text("+ Add Smart Rule Mapping")
                            }
                        }

                        if (mappings.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No custom rules configured yet.\nWhen you categorize a transaction, PayStory remembers it!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            mappings.forEach { mapping ->
                                val isEditingThis = editTargetMerchant == mapping.merchantName

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = PayStoryTokens.RadiusLg
                                ) {
                                    Column(
                                        modifier = Modifier.padding(PayStoryTokens.SpaceMd),
                                        verticalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceXs)
                                    ) {
                                        if (isEditingThis) {
                                            Text(
                                                text = "Editing: ${mapping.merchantName}",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.primary
                                            )

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Category.values().filter { it != Category.UNCATEGORIZED }.forEach { cat ->
                                                    FilterChip(
                                                        selected = editCategory == cat.name,
                                                        onClick = { editCategory = cat.name },
                                                        label = { Text("${cat.icon} ${cat.displayName}") }
                                                    )
                                                }
                                            }

                                            OutlinedTextField(
                                                value = editStory,
                                                onValueChange = { editStory = it },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = PayStoryTokens.RadiusMd
                                            )

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                TextButton(onClick = { editTargetMerchant = null }) {
                                                    Text("Cancel")
                                                }
                                                Button(
                                                    onClick = {
                                                        viewModel.learnOrUpdateMerchantMapping(mapping.merchantName, editCategory, editStory)
                                                        editTargetMerchant = null
                                                    },
                                                    shape = PayStoryTokens.RadiusFull
                                                ) {
                                                    Text("Save")
                                                }
                                            }
                                        } else {
                                            val matchedCategory = Category.values().firstOrNull { it.name == mapping.category } ?: Category.OTHERS
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = mapping.merchantName.uppercase(),
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = "${matchedCategory.icon} ${matchedCategory.displayName}",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Text(
                                                        text = "\"${mapping.story}\"",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    IconButton(onClick = {
                                                        editTargetMerchant = mapping.merchantName
                                                        editCategory = mapping.category
                                                        editStory = mapping.story
                                                    }) {
                                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                                    }
                                                    IconButton(onClick = { viewModel.deleteMerchantMapping(mapping.merchantName) }) {
                                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
