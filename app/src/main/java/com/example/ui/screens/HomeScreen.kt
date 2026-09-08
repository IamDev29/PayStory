package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Category
import com.example.data.models.Transaction
import com.example.ui.viewmodel.ExpenseViewModel
import com.example.ui.viewmodel.MainTab
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: ExpenseViewModel) {
    val transactions by viewModel.allTransactions.collectAsState()
    val pendingReviews by viewModel.pendingReviewTransactions.collectAsState()
    val budgets by viewModel.budgets.collectAsState()

    val todaySpending = viewModel.getTodaySpending(transactions)
    val overallLimit by viewModel.overallBudgetLimit.collectAsState()
    val overallPeriod by viewModel.overallBudgetPeriod.collectAsState()
    val periodSpending = viewModel.getPeriodSpending(transactions, overallPeriod)
    val currentUser by viewModel.currentUser.collectAsState()

    var isBalanceHidden by remember { mutableStateOf(false) }

    // Total balance observed from ViewModel
    val currentBalance by viewModel.totalBalance.collectAsState()
    val totalIncome = transactions.filter { it.transactionType == "RECEIVED" }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.transactionType == "SENT" }.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = PayStoryTokens.Space2Xl),
        verticalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceBase)
    ) {
        // TOP APP BAR
        PayStoryTopAppBar(
            userInitials = currentUser?.name?.take(2)?.uppercase()?.ifBlank { "PS" } ?: "PS",
            notificationCount = pendingReviews.size,
            onNotificationClick = {
                if (pendingReviews.isNotEmpty()) {
                    // stays on screen to review
                }
            },
            onProfileClick = {
                viewModel.changeTab(MainTab.Settings)
            }
        )

        val greetingText = remember {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            when (hour) {
                in 4..11 -> "Good morning"
                in 12..16 -> "Good afternoon"
                else -> "Good evening"
            }
        }
        val userGreetingName = currentUser?.name?.takeIf { it.isNotBlank() } ?: "there"

        // WELCOME & OVERVIEW HEADER ROW
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PayStoryTokens.SpaceLg),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "$greetingText, $userGreetingName",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Financial overview",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            PayStoryPill(
                text = "Live Sync",
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                contentColor = MaterialTheme.colorScheme.primary,
                dotColor = MaterialTheme.colorScheme.primary
            )
        }

        // TOTAL BALANCE CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PayStoryTokens.SpaceLg),
            shape = PayStoryTokens.Radius2Xl,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(PayStoryTokens.SpaceLg),
                verticalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceMd)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOTAL BALANCE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    IconButton(
                        onClick = { isBalanceHidden = !isBalanceHidden },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isBalanceHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Balance",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Balance Value
                Text(
                    text = if (isBalanceHidden) "••••••••••" else "₹${"%,.2f".format(currentBalance)}",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(PayStoryTokens.SpaceXs))

                // Mini Stat Tiles: Spent vs Income
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceMd)
                ) {
                    // Spent Tile
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = PayStoryTokens.RadiusLg,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(PayStoryTokens.SpaceMd)) {
                            Text(
                                text = "Spent this month",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(PayStoryTokens.SpaceXs))
                            Text(
                                text = "-₹${"%,.2f".format(periodSpending)}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(PayStoryTokens.SpaceXs))
                            val debitsCount = transactions.count { it.transactionType == "SENT" }
                            Text(
                                text = "$debitsCount debit${if (debitsCount == 1) "" else "s"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    // Income Tile
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = PayStoryTokens.RadiusLg,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(PayStoryTokens.SpaceMd)) {
                            Text(
                                text = "Monthly Income",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(PayStoryTokens.SpaceXs))
                            Text(
                                text = "+₹${"%,.2f".format(totalIncome)}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(PayStoryTokens.SpaceXs))
                            val creditsCount = transactions.count { it.transactionType == "RECEIVED" }
                            Text(
                                text = "$creditsCount credit${if (creditsCount == 1) "" else "s"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // CATEGORY BUDGETS PREVIEW SECTION
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceMd)
        ) {
            val currentMonthLabel = remember { SimpleDateFormat("MMM", Locale.getDefault()).format(Date()) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PayStoryTokens.SpaceLg),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceSm)
                ) {
                    Text(
                        text = "Category Budgets",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    PayStoryPill(
                        text = currentMonthLabel,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                TextButton(
                    onClick = { viewModel.changeTab(MainTab.Budgets) },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "View all >",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (budgets.isEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = PayStoryTokens.RadiusLg,
                    border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PayStoryTokens.SpaceLg)
                        .clickable { viewModel.changeTab(MainTab.Budgets) }
                ) {
                    Row(
                        modifier = Modifier.padding(PayStoryTokens.SpaceMd),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceMd)
                    ) {
                        Text("🎯", fontSize = 22.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Set Category Budgets",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Add limits to keep expenses controlled",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Budget",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = PayStoryTokens.SpaceLg),
                    horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceMd)
                ) {
                    budgets.forEach { budget ->
                        val catObj = Category.values().firstOrNull { it.name == budget.category } ?: Category.OTHERS
                        val spent = viewModel.getCategoryBudgetSpent(budget.category, transactions)
                        val percent = if (budget.limitAmount > 0) ((spent / budget.limitAmount) * 100).toInt() else 0
                        val dotColor = when (catObj) {
                            Category.FOOD -> CategoryOrange
                            Category.GROCERY -> CategoryGold
                            Category.SHOPPING -> CategoryPink
                            Category.TRAVEL -> CategoryBlue
                            Category.FUEL -> CategorySkyBlue
                            Category.BILLS -> CategoryPurple
                            Category.RENT -> CategoryRed
                            Category.EDUCATION -> CategoryTeal
                            Category.HEALTH -> CategoryRed
                            Category.ENTERTAINMENT -> CategoryIndigo
                            else -> CategorySlate
                        }
                        HomeBudgetMiniCard(
                            categoryName = catObj.displayName,
                            dotColor = dotColor,
                            spentAmount = spent,
                            limitAmount = budget.limitAmount,
                            percentage = percent
                        )
                    }
                }
            }
        }

        // TRANSACTION REVIEW SECTION (In-App Notification Card)
        val activePendingTx = pendingReviews.firstOrNull()
        var lastPendingTx by remember { mutableStateOf<Transaction?>(null) }
        LaunchedEffect(activePendingTx) {
            if (activePendingTx != null) {
                lastPendingTx = activePendingTx
            }
        }
        val pendingTx = activePendingTx ?: lastPendingTx

        AnimatedVisibility(
            visible = activePendingTx != null && pendingTx != null,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            if (pendingTx != null) {
                val suggestion = remember(pendingTx.transactionId) { viewModel.getMerchantSuggestion(pendingTx.merchantName) }
                var selectedReviewCategory by remember(pendingTx.transactionId) { mutableStateOf(suggestion.category) }
                var reviewDescription by remember(pendingTx.transactionId) { mutableStateOf(suggestion.story) }
                var isExpanded by remember(pendingTx.transactionId) { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PayStoryTokens.SpaceLg)
                        .testTag("in_app_notification_card"),
                    shape = PayStoryTokens.Radius2Xl,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(PayStoryTokens.SpaceLg)) {
                        // Header Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceSm)) {
                                PayStoryPill(
                                    text = "✨ NEW PAYSTORY • REVIEW",
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                PayStoryPill(
                                    text = "✔ HIGH MATCH",
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            }

                            Text(
                                text = "Today, 4:15 PM",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(PayStoryTokens.SpaceMd))

                        // Merchant & Amount Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pendingTx.merchantName.ifEmpty { "Blue Tokai Coffee Roasters" },
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "via UPI (${pendingTx.source.uppercase()})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = "-₹${"%,.2f".format(pendingTx.amount)}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Spacer(modifier = Modifier.height(PayStoryTokens.SpaceSm))

                        // Tag details
                        val mappedCategory = Category.values().firstOrNull { it.name == selectedReviewCategory } ?: Category.FOOD
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(CategoryOrange)
                            )
                            Text(
                                text = "${mappedCategory.displayName} • Auto-detected",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (reviewDescription.isNotBlank()) {
                            Spacer(modifier = Modifier.height(PayStoryTokens.SpaceXs))
                            Text(
                                text = "\"$reviewDescription\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }

                        // Expanded Category / Story Editor
                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(PayStoryTokens.SpaceMd))
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(PayStoryTokens.SpaceMd))

                            Text(
                                text = "Correct Category:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(PayStoryTokens.SpaceXs))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceSm)
                            ) {
                                Category.values().filter { it != Category.UNCATEGORIZED }.forEach { cat ->
                                    val isSelected = selectedReviewCategory == cat.name
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedReviewCategory = cat.name },
                                        label = { Text("${cat.icon} ${cat.displayName}") }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(PayStoryTokens.SpaceSm))

                            OutlinedTextField(
                                value = reviewDescription,
                                onValueChange = { reviewDescription = it },
                                placeholder = { Text("Add transaction story note...") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = PayStoryTokens.RadiusMd,
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(PayStoryTokens.SpaceMd))

                        // Action Buttons Row: Skip, Edit, Save
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceSm)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.skipTransaction(pendingTx) },
                                modifier = Modifier.weight(1f),
                                shape = PayStoryTokens.RadiusFull,
                                border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.outline)
                            ) {
                                Text("Skip", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            OutlinedButton(
                                onClick = { isExpanded = !isExpanded },
                                modifier = Modifier.weight(1f),
                                shape = PayStoryTokens.RadiusFull,
                                border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.outline)
                            ) {
                                Text(if (isExpanded) "Close" else "Edit", color = MaterialTheme.colorScheme.onSurface)
                            }

                            Button(
                                onClick = {
                                    val desc = reviewDescription.ifBlank { "Auto-logged purchase" }
                                    viewModel.reviewTransaction(pendingTx, selectedReviewCategory, desc)
                                },
                                modifier = Modifier.weight(1.2f),
                                shape = PayStoryTokens.RadiusFull,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text("Save", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // RECENT TRANSACTIONS SECTION
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PayStoryTokens.SpaceLg),
            verticalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceMd)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                TextButton(
                    onClick = { viewModel.changeTab(MainTab.Transactions) },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "See all >",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (transactions.isEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = PayStoryTokens.Radius2Xl,
                    border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(PayStoryTokens.Space2Xl),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceSm)
                    ) {
                        Text("✍️", fontSize = 40.sp)
                        Text(
                            text = "No stories logged yet",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Transactions arrive automatically via bank SMS and UPI push alerts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceSm)) {
                    transactions.take(5).forEach { tx ->
                        HomeTransactionRow(tx = tx, onClick = { viewModel.changeTab(MainTab.Transactions) })
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeBudgetMiniCard(
    categoryName: String,
    dotColor: Color,
    spentAmount: Double,
    limitAmount: Double,
    percentage: Int
) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .height(112.dp),
        shape = PayStoryTokens.RadiusXl,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(PayStoryTokens.SpaceMd),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 95.dp)
                    )
                }

                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (percentage > 100) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }

            Column {
                Text(
                    text = "₹${"%,.0f".format(spentAmount)} / ₹${"%,.0f".format(limitAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(PayStoryTokens.SpaceXs))

                // Progress Bar
                val progress = (percentage / 100f).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(PayStoryTokens.RadiusFull)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .clip(PayStoryTokens.RadiusFull)
                            .background(
                                if (percentage > 100) MaterialTheme.colorScheme.error
                                else if (percentage >= 80) MaterialTheme.colorScheme.tertiary
                                else MaterialTheme.colorScheme.primary
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeTransactionRow(
    tx: Transaction,
    onClick: () -> Unit
) {
    val category = Category.values().firstOrNull { it.name == tx.category } ?: Category.UNCATEGORIZED
    val dateFormat = SimpleDateFormat("d MMM, h:mm a", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(tx.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = PayStoryTokens.RadiusLg,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .padding(PayStoryTokens.SpaceMd)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceMd),
                modifier = Modifier.weight(1f)
            ) {
                // Category Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(PayStoryTokens.RadiusMd)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = category.icon, fontSize = 20.sp)
                }

                Column {
                    Text(
                        text = tx.merchantName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "● ${category.displayName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "• $formattedDate",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Amount
            Text(
                text = if (tx.transactionType == "SENT") "-₹${"%,.2f".format(tx.amount)}" else "+₹${"%,.2f".format(tx.amount)}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (tx.transactionType == "SENT") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
    }
}
