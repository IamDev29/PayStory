package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.models.Category
import com.example.ui.viewmodel.ExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: ExpenseViewModel) {
    val context = LocalContext.current
    val isDarkTheme by viewModel.isDarkMode.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var isNotificationAccessGranted by remember { mutableStateOf(isNotificationServiceEnabled(context)) }
    var isSmsAccessGranted by remember { mutableStateOf(
        context.checkSelfPermission(android.Manifest.permission.RECEIVE_SMS) == android.content.pm.PackageManager.PERMISSION_GRANTED &&
        context.checkSelfPermission(android.Manifest.permission.READ_SMS) == android.content.pm.PackageManager.PERMISSION_GRANTED
    ) }
    val powerManager = remember(context) { context.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager }
    var isIgnoringBatteryOptimizations by remember {
        mutableStateOf(powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false)
    }
    var showCategoryViewer by remember { mutableStateOf(false) }
    var showMerchantMappingsViewer by remember { mutableStateOf(false) }

    // Repetitive service state polling inside UI
    LaunchedEffect(Unit) {
        while (true) {
            val enabled = isNotificationServiceEnabled(context)
            if (isNotificationAccessGranted != enabled) {
                isNotificationAccessGranted = enabled
            }
            val smsEnabled = context.checkSelfPermission(android.Manifest.permission.RECEIVE_SMS) == android.content.pm.PackageManager.PERMISSION_GRANTED &&
                    context.checkSelfPermission(android.Manifest.permission.READ_SMS) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (isSmsAccessGranted != smsEnabled) {
                isSmsAccessGranted = smsEnabled
            }
            val pm = context.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
            val batteryExempted = pm?.isIgnoringBatteryOptimizations(context.packageName) ?: false
            if (isIgnoringBatteryOptimizations != batteryExempted) {
                isIgnoringBatteryOptimizations = batteryExempted
            }
            kotlinx.coroutines.delay(1200)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Headers
        Text(
            "Settings",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.primary
        )

        // Profile brief card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("✍️", fontSize = 28.sp)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = currentUser?.name ?: "PayStory User",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = currentUser?.email ?: "local-session@paystory",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // CONFIGURATION OPTIONS LIST
        Text(
            "Application Settings",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        // Option 1: Manage Categories
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showCategoryViewer = true }
                .testTag("manage_categories_setting"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Label,
                        contentDescription = "Categories",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text("Manage Categories", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                        Text("Review predefined spend categories & templates", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                    }
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
            }
        }

        // Option 1.5: PayStory Smart Mappings
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showMerchantMappingsViewer = true }
                .testTag("manage_merchant_mappings_setting"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("🧠", fontSize = 24.sp)
                    Column {
                        Text("PayStory Smart Mappings", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                        Text("Edit or delete automated merchant recommendations", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                    }
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
            }
        }

        // Option 2: OS-level Notification Listener Status Toggle Redirect
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                    context.startActivity(intent)
                }
                .testTag("notification_access_setting"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications Listener",
                        tint = if (isNotificationAccessGranted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                    )
                    Column {
                        Text("Notification Listener Access", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                        Text("Requires settings authorization to log UPI app notifications", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                    }
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isNotificationAccessGranted) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isNotificationAccessGranted) "Authorized" else "Unauthorized",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isNotificationAccessGranted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Option 2B: SMS Permission Status Redirect
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
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "SMS Permission",
                        tint = if (isSmsAccessGranted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                    )
                    Column {
                        Text("SMS Access Status", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                        Text("Requires system permission to auto-detect bank transaction SMS", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                    }
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSmsAccessGranted) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isSmsAccessGranted) "Authorized" else "Unauthorized",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isSmsAccessGranted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Option 2C: Battery Optimization exemption status
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
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Battery Optimization",
                        tint = if (isIgnoringBatteryOptimizations) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text("Battery Saver Exemption", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                        Text("Exempt PayStory to prevent Android from pausing background readers", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                    }
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isIgnoringBatteryOptimizations) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isIgnoringBatteryOptimizations) "Unrestricted" else "Optimized",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isIgnoringBatteryOptimizations) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Option 3: Premium Dark Mode toggler
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dark_mode_setting"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DarkMode,
                        contentDescription = "Dark Mode",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text("Dark Mode Theme", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                        Text("Enable deep OLED Slate color styling template", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                    }
                }
                
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { viewModel.toggleDarkMode() }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Option 4: Sign out button
        OutlinedButton(
            onClick = { viewModel.handleLogout() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("logout_button"),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = "Log Out")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log Out Session", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        Text(
            text = "PayStory • V1.0 Stable Build\nEvery payment has a story.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }

    // CATEGORY DIALOG VIEWER
    if (showCategoryViewer) {
        Dialog(onDismissRequest = { showCategoryViewer = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Categories Repository",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { showCategoryViewer = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(Category.values().filter { it != Category.UNCATEGORIZED }) { cat ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(cat.icon, fontSize = 20.sp)
                                        Text(
                                            cat.displayName,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Text(
                        text = "Note: V1 contains native static categories with responsive tracking schemas.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
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
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxSize()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Smart Rules Mapping",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Auto-pilot classification mappings",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }
                        IconButton(onClick = { showMerchantMappingsViewer = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), modifier = Modifier.padding(bottom = 12.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // SECTION A: ADD NEW RULE CONTROL
                        if (isAddingNew) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("Add Intelligence Rule", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                                    
                                    OutlinedTextField(
                                        value = newMerchant,
                                        onValueChange = { newMerchant = it },
                                        placeholder = { Text("Merchant name (e.g. Google)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    Text("Suggested Category", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                                        placeholder = { Text("Suggested story text (e.g. GSuite Subscription)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
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
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Add")
                                        }
                                    }
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = { isAddingNew = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("➕ Add Smart Rule Mapping")
                            }
                        }

                        // SECTION B: RENDER EXISTING RULES
                        if (mappings.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No learned Smart Rules yet.\nMake a payment correction to auto-pilot mappings!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            mappings.forEach { mapping ->
                                val isEditingThis = editTargetMerchant == mapping.merchantName

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isEditingThis) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f) 
                                                         else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (isEditingThis) {
                                            Text("Editing: ${mapping.merchantName}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                                            
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
                                                shape = RoundedCornerShape(8.dp)
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
                                                    shape = RoundedCornerShape(8.dp)
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
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        Text(matchedCategory.icon, fontSize = 12.sp)
                                                        Text(
                                                            text = matchedCategory.displayName,
                                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                    Text(
                                                        text = "Story: \"${mapping.story}\"",
                                                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                    )
                                                }

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    IconButton(onClick = {
                                                        editTargetMerchant = mapping.merchantName
                                                        editCategory = mapping.category
                                                        editStory = mapping.story
                                                    }) {
                                                        Icon(Icons.Default.Edit, contentDescription = "Edit Rule", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                                    }
                                                    IconButton(onClick = {
                                                        viewModel.deleteMerchantMapping(mapping.merchantName)
                                                    }) {
                                                        Icon(Icons.Default.Delete, contentDescription = "Delete Rule", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
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
