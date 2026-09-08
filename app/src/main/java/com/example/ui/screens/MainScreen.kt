package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Category
import com.example.data.models.Transaction
import com.example.ui.viewmodel.ExpenseViewModel
import com.example.ui.viewmodel.MainTab
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: ExpenseViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val pendingReviews by viewModel.pendingReviewTransactions.collectAsState()

    Scaffold(
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_nav_bar")
                ) {
                    // Tab 1: Home
                    NavigationBarItem(
                        selected = currentTab is MainTab.Home,
                        onClick = { viewModel.changeTab(MainTab.Home) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home", fontWeight = if (currentTab is MainTab.Home) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_home")
                    )

                    // Tab 2: Transactions
                    NavigationBarItem(
                        selected = currentTab is MainTab.Transactions,
                        onClick = { viewModel.changeTab(MainTab.Transactions) },
                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Transactions") },
                        label = { Text("Spends", fontWeight = if (currentTab is MainTab.Transactions) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_transactions")
                    )

                    // Tab 3: Budgets
                    NavigationBarItem(
                        selected = currentTab is MainTab.Budgets,
                        onClick = { viewModel.changeTab(MainTab.Budgets) },
                        icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Budgets") },
                        label = { Text("Budgets", fontWeight = if (currentTab is MainTab.Budgets) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_budgets")
                    )

                    // Tab 4: Analytics
                    NavigationBarItem(
                        selected = currentTab is MainTab.Analytics,
                        onClick = { viewModel.changeTab(MainTab.Analytics) },
                        icon = { Icon(Icons.Default.Assessment, contentDescription = "Analytics") },
                        label = { Text("Charts", fontWeight = if (currentTab is MainTab.Analytics) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_analytics")
                    )

                    // Tab 5: Settings
                    NavigationBarItem(
                        selected = currentTab is MainTab.Settings,
                        onClick = { viewModel.changeTab(MainTab.Settings) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings", fontWeight = if (currentTab is MainTab.Settings) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_settings")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "TabTransition"
            ) { targetTab ->
                when (targetTab) {
                    is MainTab.Home -> HomeScreen(viewModel)
                    is MainTab.Transactions -> TransactionsScreen(viewModel)
                    is MainTab.Budgets -> BudgetsScreen(viewModel)
                    is MainTab.Analytics -> AnalyticsScreen(viewModel)
                    is MainTab.Settings -> SettingsScreen(viewModel)
                }
            }
        }
    }

    // Modal Bottom Sheet for unreviewed transactions (shown on non-Home tabs; Home screen has its own inline card)
    val activeTx = pendingReviews.firstOrNull()
    if (activeTx != null && currentTab !is MainTab.Home) {
        val tx = activeTx
        val suggestion = remember(tx.transactionId) { viewModel.getMerchantSuggestion(tx.merchantName) }
        var selectedCategory by remember(tx.transactionId) { mutableStateOf(suggestion.category) }
        var descriptionText by remember(tx.transactionId) { mutableStateOf(suggestion.story) }
        var isEditing by remember(tx.transactionId) { mutableStateOf(false) }
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = {
                viewModel.skipTransaction(tx)
            },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = PayStoryTokens.SpaceXl)
                    .padding(bottom = PayStoryTokens.Space2Xl),
                verticalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceBase)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "New PayStory Detected",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Matching intelligently based on user behaviors.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    PayStoryPill(
                        text = "${suggestion.confidence} MATCH",
                        containerColor = when (suggestion.confidence) {
                            "HIGH" -> MaterialTheme.colorScheme.primaryContainer
                            "MEDIUM" -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.errorContainer
                        },
                        contentColor = when (suggestion.confidence) {
                            "HIGH" -> MaterialTheme.colorScheme.primary
                            "MEDIUM" -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = PayStoryTokens.RadiusLg,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(PayStoryTokens.SpaceMd),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = tx.merchantName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Detected via ${tx.source.uppercase()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = if (tx.transactionType == "SENT") "-₹${"%,.2f".format(tx.amount)}" else "+₹${"%,.2f".format(tx.amount)}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = if (tx.transactionType == "SENT") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                val currentMappedCategory = Category.values().firstOrNull { it.name == selectedCategory } ?: Category.OTHERS

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(currentMappedCategory.icon, fontSize = 16.sp)
                        Text(
                            text = "Suggested Category: ${currentMappedCategory.displayName}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Suggested Story: \"$descriptionText\"",
                        style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isEditing) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    Text(
                        text = "Correct Category",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceSm)
                    ) {
                        Category.values().filter { it != Category.UNCATEGORIZED }.forEach { cat ->
                            val isSelected = selectedCategory == cat.name
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat.name },
                                label = { Text("${cat.icon} ${cat.displayName}") },
                                shape = PayStoryTokens.RadiusFull
                            )
                        }
                    }

                    OutlinedTextField(
                        value = descriptionText,
                        onValueChange = { descriptionText = it },
                        placeholder = { Text("What did you buy? e.g. Lunch, taxi...") },
                        label = { Text("Custom Notes/Description") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = PayStoryTokens.RadiusMd,
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(PayStoryTokens.SpaceXs))

                // Actions: Skip / Edit or Cancel / Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceSm)
                ) {
                    if (isEditing) {
                        OutlinedButton(
                            onClick = { isEditing = false },
                            modifier = Modifier.weight(1f),
                            shape = PayStoryTokens.RadiusFull
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                val desc = descriptionText.ifBlank { "Uncategorized purchase" }
                                viewModel.reviewTransaction(tx, selectedCategory, desc)
                            },
                            modifier = Modifier.weight(1f),
                            shape = PayStoryTokens.RadiusFull,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Save Match", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { viewModel.skipTransaction(tx) },
                            modifier = Modifier.weight(1f),
                            shape = PayStoryTokens.RadiusFull
                        ) {
                            Text("Skip")
                        }

                        OutlinedButton(
                            onClick = { isEditing = true },
                            modifier = Modifier.weight(1f),
                            shape = PayStoryTokens.RadiusFull
                        ) {
                            Text("Edit")
                        }

                        Button(
                            onClick = {
                                val desc = descriptionText.ifBlank { "Uncategorized purchase" }
                                viewModel.reviewTransaction(tx, selectedCategory, desc)
                            },
                            modifier = Modifier.weight(1f),
                            shape = PayStoryTokens.RadiusFull,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Save", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
