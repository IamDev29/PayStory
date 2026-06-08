package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Category
import com.example.data.models.Transaction
import com.example.ui.viewmodel.ExpenseViewModel
import com.example.ui.viewmodel.MainTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: ExpenseViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val pendingReviews by viewModel.pendingReviewTransactions.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar")
            ) {
                // Tab 1: Home
                NavigationBarItem(
                    selected = currentTab is MainTab.Home,
                    onClick = { viewModel.changeTab(MainTab.Home) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    modifier = Modifier.testTag("nav_tab_home")
                )

                // Tab 2: Transactions
                NavigationBarItem(
                    selected = currentTab is MainTab.Transactions,
                    onClick = { viewModel.changeTab(MainTab.Transactions) },
                    icon = { Icon(Icons.Default.List, contentDescription = "Transactions") },
                    label = { Text("Spends") },
                    modifier = Modifier.testTag("nav_tab_transactions")
                )

                // Tab 3: Budgets
                NavigationBarItem(
                    selected = currentTab is MainTab.Budgets,
                    onClick = { viewModel.changeTab(MainTab.Budgets) },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Budgets") },
                    label = { Text("Budgets") },
                    modifier = Modifier.testTag("nav_tab_budgets")
                )

                // Tab 4: Analytics
                NavigationBarItem(
                    selected = currentTab is MainTab.Analytics,
                    onClick = { viewModel.changeTab(MainTab.Analytics) },
                    icon = { Icon(Icons.Default.Assessment, contentDescription = "Analytics") },
                    label = { Text("Charts") },
                    modifier = Modifier.testTag("nav_tab_analytics")
                )

                // Tab 5: Settings
                NavigationBarItem(
                    selected = currentTab is MainTab.Settings,
                    onClick = { viewModel.changeTab(MainTab.Settings) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Animated Tab Transitions
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

    // Modal Bottom Sheet for unreviewed transactions
    if (pendingReviews.isNotEmpty()) {
        val tx = pendingReviews.first()
        var selectedCategory by remember(tx.transactionId) { mutableStateOf(Category.FOOD.name) }
        var descriptionText by remember(tx.transactionId) { mutableStateOf("") }
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = {
                // If dismissed, skip it automatically as per duplicate/prompt instruction
                viewModel.skipTransaction(tx)
            },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "New payment story detected",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Help future you remember why this occurred.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = tx.merchantName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Detected via ${tx.source.uppercase()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    Text(
                        text = if (tx.transactionType == "SENT") "-₹${"%,.0f".format(tx.amount)}" else "+₹${"%,.0f".format(tx.amount)}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (tx.transactionType == "SENT") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Category selection
                Text(
                    text = "Select Category",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Category.values().filter { it != Category.UNCATEGORIZED }.forEach { cat ->
                        val isSelected = selectedCategory == cat.name
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat.name },
                            label = { Text("${cat.icon} ${cat.displayName}") }
                        )
                    }
                }

                // Description field
                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    placeholder = { Text("What did you buy? e.g. Lunch, taxi...") },
                    label = { Text("Add Notes/Description") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Actions: Skip / Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.skipTransaction(tx)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Skip")
                    }

                    Button(
                        onClick = {
                            val desc = descriptionText.ifBlank { "" }
                            viewModel.reviewTransaction(tx, selectedCategory, desc)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
