package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Category
import com.example.data.models.Transaction
import com.example.ui.viewmodel.ExpenseViewModel
import com.example.ui.viewmodel.MainTab
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(viewModel: ExpenseViewModel) {
    val transactions by viewModel.allTransactions.collectAsState()
    val pendingReviews by viewModel.pendingReviewTransactions.collectAsState()
    val budgets by viewModel.budgets.collectAsState()
    
    val todaySpending = viewModel.getTodaySpending(transactions)
    val overallLimit by viewModel.overallBudgetLimit.collectAsState()
    val overallPeriod by viewModel.overallBudgetPeriod.collectAsState()
    val periodSpending = viewModel.getPeriodSpending(transactions, overallPeriod)
    val totalCount = transactions.size

    val currentUser by viewModel.currentUser.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Welcoming Title Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1.0f)) {
                Text(
                    text = "PayStory",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Track the story behind every transaction.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            // User Profile Avatar on Right
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val initials = currentUser?.name?.take(2)?.uppercase() ?: "JS"
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Quick Simulate & Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Welcome back, ${currentUser?.name ?: "User"}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.primary
            )

            // Simulate Pay Button (Fully functional)
            Button(
                onClick = { viewModel.simulateAutoTransactionArrival() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("simulate_payment_button")
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = "Simulate payment",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Simulate Pay", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
            }
        }

        // SPENDING HUD METRIC TILES
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 1: Today's spending (Background: #313033, Label: #D0BCFF, Corners: rounded-3xl)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(112.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Today",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary // #D0BCFF
                    )
                    Column {
                        Text(
                            text = "₹${"%,.0f".format(todaySpending)}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val todayCount = transactions.filter { 
                            val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                            sdf.format(Date(it.timestamp)) == sdf.format(Date())
                        }.size
                        Text(
                            text = "$todayCount transaction${if (todayCount == 1) "" else "s"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Card 2: This month's spending (Background: #EADDFF, Text: #21005D, Corners: rounded-3xl)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(112.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (overallPeriod == "WEEK") "This Week" else "This Month",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Column {
                        Text(
                            text = "₹${"%,.0f".format(periodSpending)}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        // Simple leftover calculation
                        val leftAmount = maxOf(0.0, overallLimit - periodSpending)
                        Text(
                            text = "₹${"%,.0f".format(leftAmount)} left of ₹${"%,.0f".format(overallLimit)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // TRANSACTION REVIEW SECTION (Satisfies "Transaction Review Flow")
        AnimatedVisibility(
            visible = pendingReviews.isNotEmpty(),
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            val pendingTx = pendingReviews.first()
            var selectedReviewCategory by remember(pendingTx.transactionId) { mutableStateOf(Category.FOOD.name) }
            var reviewDescription by remember(pendingTx.transactionId) { mutableStateOf("") }
            var isExpanded by remember(pendingTx.transactionId) { mutableStateOf(false) }
            
            // Background: #49454F, Corner Radius: 24.dp (rounded-3xl)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("in_app_notification_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    // Left border highlight: #D0BCFF (4dp bar)
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp)
                    ) {
                        // Title Indicator Row
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // "New Detection" Badge
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(100.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "NEW DETECTION",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            Text(
                                text = "Just now",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }

                        // Core Details Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = "₹${"%,.0f".format(pendingTx.amount)} to ${pendingTx.merchantName}",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = if (reviewDescription.isEmpty()) "\"Add a description for this...\"" else "\"$reviewDescription\"",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    ),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }

                            if (!isExpanded) {
                                Button(
                                    onClick = { isExpanded = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Add Context",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }

                        // Expanded controls for tagging context
                        AnimatedVisibility(visible = isExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp)
                            ) {
                                // Category Selection
                                Text(
                                    text = "Why did you spend this money?",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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

                                Spacer(modifier = Modifier.height(12.dp))

                                // Description field
                                OutlinedTextField(
                                    value = reviewDescription,
                                    onValueChange = { reviewDescription = it },
                                    placeholder = { Text("What did you buy? e.g. Lunch with team, Uber...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Action buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { 
                                            viewModel.skipTransaction(pendingTx) 
                                            isExpanded = false
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                        )
                                    ) {
                                        Text("Skip")
                                    }

                                    Button(
                                        onClick = {
                                            val desc = reviewDescription.ifBlank { "Uncategorized payment" }
                                            viewModel.reviewTransaction(pendingTx, selectedReviewCategory, desc)
                                            isExpanded = false
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Save Context")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // QUICK STATS CARDS ROW
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Total Recorded",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Text(
                        "$totalCount payments",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Pending reviews",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Text(
                        "${pendingReviews.size} to review",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (pendingReviews.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
            }
        }

        // RECENT TRANSACTIONS HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Recent Transactions",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            TextButton(onClick = { viewModel.changeTab(MainTab.Transactions) }) {
                Text("See All")
                Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }

        // RECENT TRANSACTIONS LIST
        if (transactions.isEmpty()) {
            // Empty state placeholder
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("✍️", fontSize = 44.sp)
                    Text(
                        text = "No stories yet.",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Your transactions will appear here once detected.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                transactions.take(5).forEach { tx ->
                    TransactionListItem(tx = tx, onClick = {
                        // Switches tab to transations & highlights details optionally!
                        viewModel.changeTab(MainTab.Transactions)
                    })
                }
            }
        }

        // BUDGET MINI-TRACK GROUP (Matches: section class="bg-[#1C1B1F] border border-[#49454F] rounded-2xl p-3 mb-2")
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            "Budget Trackers Status",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        val topBudgetToShow = budgets.maxByOrNull { budget ->
            val spent = viewModel.getCategoryBudgetSpent(budget.category, transactions)
            if (budget.limitAmount > 0) (spent / budget.limitAmount) else 0.0
        }
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.changeTab(MainTab.Budgets) },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background // #1C1B1F
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp, 
                MaterialTheme.colorScheme.outlineVariant // #49454F
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (topBudgetToShow != null) {
                    val spent = viewModel.getCategoryBudgetSpent(topBudgetToShow.category, transactions)
                    val percent = if (topBudgetToShow.limitAmount > 0) (spent / topBudgetToShow.limitAmount) else 0.0
                    val percentFormatted = minOf(100, (percent * 100).toInt())
                    val categoryDisplay = Category.values().firstOrNull { it.name == topBudgetToShow.category } ?: Category.UNCATEGORIZED
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${categoryDisplay.icon} ", fontSize = 16.sp)
                            Text(
                                text = "${categoryDisplay.displayName} Budget",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Text(
                            text = "$percentFormatted% reached",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (percent >= 0.8) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Linear Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(
                                color = MaterialTheme.colorScheme.outlineVariant, // #49454F
                                shape = RoundedCornerShape(100.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(minOf(1f, percent.toFloat()))
                                .fillMaxHeight()
                                .background(
                                    color = if (percent >= 0.8) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(100.dp)
                                )
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📊 ", fontSize = 16.sp)
                            Text(
                                text = "Category Budget Status",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "No category budgets set yet. Tap here to define maximum spend limits.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionListItem(tx: Transaction, onClick: () -> Unit) {
    val categoryDetails = Category.values().firstOrNull { it.name == tx.category } ?: Category.UNCATEGORIZED
    val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
    val dateString = dateFormat.format(Date(tx.timestamp))

    // Badges category color associations from our Elegant Dark decorative tokens
    val badgeBg = when (categoryDetails) {
        Category.FOOD -> DecorativeOrange.copy(alpha = 0.15f)
        Category.GROCERY -> DecorativeGold.copy(alpha = 0.15f)
        Category.SHOPPING -> DecorativePink.copy(alpha = 0.15f)
        Category.TRAVEL -> DecorativeBlue.copy(alpha = 0.15f)
        Category.FUEL -> DecorativeBlue.copy(alpha = 0.15f)
        Category.BILLS -> DecorativePurple.copy(alpha = 0.15f)
        Category.RENT -> DecorativeRed.copy(alpha = 0.15f)
        Category.EDUCATION -> DecorativeTeal.copy(alpha = 0.15f)
        Category.HEALTH -> DecorativeCoral.copy(alpha = 0.15f)
        Category.ENTERTAINMENT -> DecorativeIndigo.copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    }
    
    val badgeTextColor = when (categoryDetails) {
        Category.FOOD -> DecorativeOrange
        Category.GROCERY -> DecorativeGold
        Category.SHOPPING -> DecorativePink
        Category.TRAVEL -> DecorativeBlue
        Category.FUEL -> DecorativeBlue
        Category.BILLS -> DecorativePurple
        Category.RENT -> DecorativeRed
        Category.EDUCATION -> DecorativeTeal
        Category.HEALTH -> DecorativeCoral
        Category.ENTERTAINMENT -> DecorativeIndigo
        else -> MaterialTheme.colorScheme.primary
    }

    // Container: #2B2930 (surface) with Rounded Corners
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Category emoji badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = badgeBg,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(categoryDetails.icon, fontSize = 22.sp)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = tx.merchantName,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = categoryDetails.displayName,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = badgeTextColor
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                        Text(
                            text = dateString,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (tx.transactionType == "SENT") "-₹${"%,.0f".format(tx.amount)}" else "+₹${"%,.0f".format(tx.amount)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (tx.transactionType == "SENT") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                
                // Show source type visually
                val sourceBadgeText = if (tx.source == "sms") "Expense (SMS)" else "Income (Notification)"
                val sourceBadgeBg = if (tx.source == "sms") MaterialTheme.colorScheme.error.copy(alpha = 0.12f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                val sourceBadgeColor = if (tx.source == "sms") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .background(
                            color = sourceBadgeBg,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = sourceBadgeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = sourceBadgeColor
                    )
                }

                if (!tx.isReviewed) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "NEW Context Required",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}
