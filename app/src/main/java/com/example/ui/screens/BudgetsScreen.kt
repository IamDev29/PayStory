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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.models.Budget
import com.example.data.models.Category
import com.example.ui.viewmodel.ExpenseViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(viewModel: ExpenseViewModel) {
    val budgetLimits by viewModel.budgets.collectAsState()
    val allTx by viewModel.allTransactions.collectAsState()
    val alertHistory by viewModel.alerts.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val overallLimit by viewModel.overallBudgetLimit.collectAsState()
    val overallPeriod by viewModel.overallBudgetPeriod.collectAsState()
    val periodSpending = viewModel.getPeriodSpending(allTx, overallPeriod)

    val totalBalance by viewModel.totalBalance.collectAsState()
    val isBaseBalanceConfigured by viewModel.isBaseBalanceConfigured.collectAsState()

    var showTotalBalanceDialog by remember { mutableStateOf(false) }
    var inputTotalBalance by remember { mutableStateOf("") }

    var showBudgetForm by remember { mutableStateOf<Category?>(null) }
    var inputLimitAmount by remember { mutableStateOf("") }

    var showOverallBudgetDialog by remember { mutableStateOf(false) }
    var inputOverallLimit by remember { mutableStateOf("") }
    var inputOverallPeriod by remember { mutableStateOf("") }

    val spentRatio = if (overallLimit > 0) (periodSpending / overallLimit).toFloat() else 0f
    val spentPercent = (spentRatio * 100).toInt()
    val remainingBudget = maxOf(0.0, overallLimit - periodSpending)

    val daysLeftInMonth = remember {
        val cal = Calendar.getInstance()
        val maxD = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val curD = cal.get(Calendar.DAY_OF_MONTH)
        maxOf(0, maxD - curD)
    }
    val currentMonthLabel = remember { SimpleDateFormat("MMM", Locale.getDefault()).format(Date()) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(bottom = PayStoryTokens.Space2Xl),
            verticalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceBase)
        ) {
            // TOP APP BAR
            PayStoryTopAppBar(
                userInitials = currentUser?.name?.take(2)?.uppercase()?.ifBlank { "PS" } ?: "PS"
            )

            // TITLE & SUBTITLE ROW
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PayStoryTokens.SpaceLg),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Budgets",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Monthly limits & category allocations",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = {
                        inputOverallLimit = overallLimit.toInt().toString()
                        inputOverallPeriod = overallPeriod
                        showOverallBudgetDialog = true
                    },
                    shape = PayStoryTokens.RadiusFull,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("configure_overall_budget_button")
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(PayStoryTokens.SpaceXs))
                    Text("Config", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }

            // TOTAL BALANCE CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PayStoryTokens.SpaceLg)
                    .testTag("budgets_total_balance_card"),
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceSm)
                        ) {
                            Text(
                                text = "TOTAL BALANCE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (isBaseBalanceConfigured) {
                                PayStoryPill(
                                    text = "Active",
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    contentColor = MaterialTheme.colorScheme.primary,
                                    dotColor = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Add or Edit Total Balance Action Button
                        FilledTonalButton(
                            onClick = {
                                inputTotalBalance = if (totalBalance != 0.0) totalBalance.toInt().toString() else ""
                                showTotalBalanceDialog = true
                            },
                            shape = PayStoryTokens.RadiusFull,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("add_or_edit_total_balance_button")
                        ) {
                            Icon(
                                imageVector = if (isBaseBalanceConfigured && totalBalance != 0.0) Icons.Default.Edit else Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(PayStoryTokens.SpaceXs))
                            Text(
                                text = if (isBaseBalanceConfigured && totalBalance != 0.0) "Edit Balance" else "Add Balance",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "₹${"%,.2f".format(totalBalance)}",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = if (isBaseBalanceConfigured) "Available liquid balance across accounts" else "Set your starting balance to track live funds",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // MONTHLY SPEND LIMIT CARD
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
                            text = "MONTHLY SPEND LIMIT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceSm)
                        ) {
                            PayStoryPill(
                                text = "$spentPercent% USED",
                                containerColor = if (spentRatio >= 1f) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                                contentColor = if (spentRatio >= 1f) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            IconButton(
                                onClick = {
                                    inputOverallLimit = overallLimit.toInt().toString()
                                    inputOverallPeriod = overallPeriod
                                    showOverallBudgetDialog = true
                                },
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("edit_monthly_spend_limit_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Spend Limit",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "₹${"%,.2f".format(periodSpending)}",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "of ₹${"%,.2f".format(overallLimit)} limit",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(PayStoryTokens.RadiusFull)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(minOf(1f, spentRatio))
                                .fillMaxHeight()
                                .clip(PayStoryTokens.RadiusFull)
                                .background(
                                    if (spentRatio >= 1f) MaterialTheme.colorScheme.error
                                    else if (spentRatio >= 0.8f) MaterialTheme.colorScheme.tertiary
                                    else MaterialTheme.colorScheme.primary
                                )
                        )
                    }

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
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = "₹${"%,.2f".format(remainingBudget)} remaining",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "$daysLeftInMonth days left in $currentMonthLabel",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // SET LIMIT FOR CATEGORIES SCROLL
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceSm)
            ) {
                Text(
                    text = "Add Category Limit",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = PayStoryTokens.SpaceLg)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = PayStoryTokens.SpaceLg),
                    horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceSm)
                ) {
                    Category.values().filter { it != Category.UNCATEGORIZED }.forEach { cat ->
                        val hasActiveLimit = budgetLimits.any { it.category == cat.name }

                        Card(
                            modifier = Modifier
                                .clickable {
                                    val activeLimit = budgetLimits.firstOrNull { it.category == cat.name }
                                    inputLimitAmount = activeLimit?.limitAmount?.toString() ?: ""
                                    showBudgetForm = cat
                                }
                                .width(130.dp),
                            shape = PayStoryTokens.RadiusXl,
                            colors = CardDefaults.cardColors(
                                containerColor = if (hasActiveLimit) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                PayStoryTokens.BorderThin,
                                if (hasActiveLimit) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(PayStoryTokens.SpaceMd),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(cat.icon, fontSize = 24.sp)
                                Text(
                                    text = cat.displayName,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (hasActiveLimit) {
                                    val limit = budgetLimits.first { it.category == cat.name }.limitAmount
                                    Text(
                                        text = "₹${"%,.0f".format(limit)}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Text(
                                        text = "+ Set Limit",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // CATEGORY BUDGETS LIST
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PayStoryTokens.SpaceLg),
                verticalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceMd)
            ) {
                Text(
                    text = "Category Budgets",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (budgetLimits.isEmpty()) {
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
                            Text("📊", fontSize = 36.sp)
                            Text(
                                text = "No category budgets defined",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tap any category above to set spend thresholds and receive warnings.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    budgetLimits.forEach { budget ->
                        val catObj = Category.values().firstOrNull { it.name == budget.category } ?: Category.OTHERS
                        val currentSpent = viewModel.getCategoryBudgetSpent(budget.category, allTx)
                        val remaining = budget.limitAmount - currentSpent
                        val progressRatio = if (budget.limitAmount > 0) (currentSpent / budget.limitAmount).toFloat() else 0f
                        val progressPercent = (progressRatio * 100).toInt()

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("budget_category_card_${budget.category}"),
                            shape = PayStoryTokens.Radius2Xl,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(PayStoryTokens.SpaceLg),
                                verticalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceSm)
                            ) {
                                // Header row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceMd)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(PayStoryTokens.RadiusMd)
                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(catObj.icon, fontSize = 22.sp)
                                        }

                                        Column {
                                            Text(
                                                text = catObj.displayName,
                                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Auto-tracked from spends",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        PayStoryPill(
                                            text = "$progressPercent%",
                                            containerColor = if (progressPercent > 100) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = if (progressPercent > 100) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                                        )

                                        IconButton(
                                            onClick = {
                                                inputLimitAmount = budget.limitAmount.toString()
                                                showBudgetForm = catObj
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { viewModel.removeBudgetLimit(budget.category) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(PayStoryTokens.SpaceXs))

                                // Figures
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Text(
                                        text = "₹${"%,.0f".format(currentSpent)} / ₹${"%,.0f".format(budget.limitAmount)}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Text(
                                        text = if (remaining >= 0) "₹${"%,.0f".format(remaining)} left" else "! Exceeded by ₹${"%,.0f".format(-remaining)}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = if (remaining >= 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                                    )
                                }

                                // Linear Progress bar
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(PayStoryTokens.RadiusFull)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(minOf(1f, progressRatio))
                                            .fillMaxHeight()
                                            .clip(PayStoryTokens.RadiusFull)
                                            .background(
                                                if (progressPercent >= 100) MaterialTheme.colorScheme.error
                                                else if (progressPercent >= 80) MaterialTheme.colorScheme.tertiary
                                                else MaterialTheme.colorScheme.primary
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // TRIGGERED BUDGET ALERTS SECTION
            if (alertHistory.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PayStoryTokens.SpaceLg),
                    verticalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceSm)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Triggered Alerts",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        TextButton(
                            onClick = { viewModel.clearAlerts() },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Clear All",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    alertHistory.forEach { alert ->
                        val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
                        val alertTime = dateFormat.format(Date(alert.timestamp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = PayStoryTokens.RadiusLg,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(PayStoryTokens.SpaceMd),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceMd)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = alert.message,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = alertTime,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // FORM BUDGET DIALOG MODAL
    showBudgetForm?.let { cat ->
        Dialog(onDismissRequest = { showBudgetForm = null }) {
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
                    Text(cat.icon, fontSize = 44.sp)
                    Text(
                        text = "Set Limit for ${cat.displayName}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = inputLimitAmount,
                        onValueChange = { inputLimitAmount = it },
                        label = { Text("Limit Amount (₹)") },
                        placeholder = { Text("e.g. 15000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("budget_limit_input_field"),
                        singleLine = true,
                        shape = PayStoryTokens.RadiusMd
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceSm)
                    ) {
                        OutlinedButton(
                            onClick = { showBudgetForm = null },
                            modifier = Modifier.weight(1f),
                            shape = PayStoryTokens.RadiusFull
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                val limitVal = inputLimitAmount.toDoubleOrNull() ?: 0.0
                                if (limitVal > 0.0) {
                                    viewModel.setBudgetLimit(cat.name, limitVal)
                                    showBudgetForm = null
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = PayStoryTokens.RadiusFull,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Save Limit", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // OVERALL BUDGET CONFIG DIALOG
    if (showOverallBudgetDialog) {
        Dialog(onDismissRequest = { showOverallBudgetDialog = false }) {
            Card(
                shape = PayStoryTokens.Radius2Xl,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(PayStoryTokens.SpaceXl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceBase)
                ) {
                    Text(
                        text = "Configure Monthly Budget",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = inputOverallLimit,
                        onValueChange = { inputOverallLimit = it },
                        label = { Text("Budget Limit (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("overall_limit_input"),
                        shape = PayStoryTokens.RadiusMd
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceXs)
                    ) {
                        Text(
                            text = "Budget Period Frequency",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceSm)
                        ) {
                            listOf("MONTH", "WEEK").forEach { period ->
                                val isSelected = inputOverallPeriod == period
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { inputOverallPeriod = period },
                                    label = { Text(if (period == "WEEK") "Weekly" else "Monthly") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("budget_period_card_$period"),
                                    shape = PayStoryTokens.RadiusFull
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceSm)
                    ) {
                        OutlinedButton(
                            onClick = { showOverallBudgetDialog = false },
                            modifier = Modifier.weight(1f),
                            shape = PayStoryTokens.RadiusFull
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                val limitVal = inputOverallLimit.toDoubleOrNull() ?: 65000.0
                                viewModel.updateOverallBudget(limitVal, inputOverallPeriod)
                                showOverallBudgetDialog = false
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

    // TOTAL BALANCE CONFIG DIALOG
    if (showTotalBalanceDialog) {
        Dialog(onDismissRequest = { showTotalBalanceDialog = false }) {
            Card(
                shape = PayStoryTokens.Radius2Xl,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("total_balance_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(PayStoryTokens.SpaceXl),
                    verticalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceBase)
                ) {
                    Text(
                        text = if (isBaseBalanceConfigured && totalBalance != 0.0) "Edit Total Balance" else "Add Total Balance",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Enter your current available balance across bank accounts and UPI wallets to establish your real-time ledger.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = inputTotalBalance,
                        onValueChange = { inputTotalBalance = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Total Balance (₹)") },
                        placeholder = { Text("e.g. 50000") },
                        prefix = { Text("₹ ", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("total_balance_input"),
                        shape = PayStoryTokens.RadiusMd
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceSm)
                    ) {
                        OutlinedButton(
                            onClick = { showTotalBalanceDialog = false },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cancel_total_balance_button"),
                            shape = PayStoryTokens.RadiusFull
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                val newBal = inputTotalBalance.toDoubleOrNull() ?: 0.0
                                viewModel.setTotalBalance(newBal)
                                showTotalBalanceDialog = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_total_balance_button"),
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
