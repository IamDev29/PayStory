package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Category
import com.example.data.models.Transaction
import com.example.ui.viewmodel.ExpenseViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AnalyticsScreen(viewModel: ExpenseViewModel) {
    val txList by viewModel.allTransactions.collectAsState()
    val totalSpend = txList.filter { it.transactionType == "SENT" }.sumOf { it.amount }
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedPeriod by remember { mutableStateOf("This Month") }
    val periods = listOf("This Month", "Last Month", "Last 3 Months", "All Time")

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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PayStoryTokens.SpaceLg)
            ) {
                Text(
                    text = "Analytics",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Category breakdowns & spending habits",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // PERIOD SELECTION PILLS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = PayStoryTokens.SpaceLg),
                horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceSm)
            ) {
                periods.forEach { period ->
                    val isSelected = selectedPeriod == period
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedPeriod = period },
                        label = { Text(period) },
                        shape = PayStoryTokens.RadiusFull,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            selectedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            if (txList.isEmpty() || totalSpend == 0.0) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = PayStoryTokens.Radius2Xl,
                    border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PayStoryTokens.SpaceLg)
                ) {
                    Column(
                        modifier = Modifier.padding(PayStoryTokens.Space2Xl),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceSm)
                    ) {
                        Text("📊", fontSize = 40.sp)
                        Text(
                            text = "No analytics data yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Transactions logged from UPI alerts and bank SMS will produce real-time spending insights here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // 1. SPEND DISTRIBUTION / DONUT CHART CARD
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PayStoryTokens.SpaceLg)
                        .testTag("category_analytics_card"),
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
                                text = "SPEND DISTRIBUTION",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            val currentMonthPill = remember { SimpleDateFormat("MMM", Locale.getDefault()).format(Date()) }
                            PayStoryPill(
                                text = currentMonthPill,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        val categoryMap = viewModel.getCategorySpendingMap(txList)
                        val effectiveTotal = totalSpend

                        val chartSlices = remember(categoryMap, effectiveTotal) {
                            var cumulativeAngle = -90f
                            val mapToUse = categoryMap

                            mapToUse.map { entry ->
                                val percent = if (effectiveTotal > 0) (entry.value / effectiveTotal).toFloat() else 0f
                                val sweep = percent * 360f
                                val start = cumulativeAngle
                                cumulativeAngle += sweep

                                val catObj = Category.values().firstOrNull { it.name == entry.key } ?: Category.OTHERS
                                val sectorColor = when (catObj) {
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

                                CategoryChartSlice(
                                    key = entry.key,
                                    amount = entry.value,
                                    label = catObj.displayName,
                                    icon = catObj.icon,
                                    color = sectorColor,
                                    startAngle = start,
                                    sweepAngle = sweep,
                                    percentage = (percent * 100).toInt()
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Donut Canvas
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(130.dp)
                            ) {
                                Canvas(modifier = Modifier.size(110.dp)) {
                                    chartSlices.forEach { slice ->
                                        drawArc(
                                            color = slice.color,
                                            startAngle = slice.startAngle,
                                            sweepAngle = slice.sweepAngle,
                                            useCenter = false,
                                            style = Stroke(width = 30f, cap = StrokeCap.Round),
                                            size = Size(size.width, size.height)
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "₹${"%,.0f".format(effectiveTotal)}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Total Spend",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(PayStoryTokens.SpaceMd))

                            // Legend breakdown
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                chartSlices.take(5).forEach { slice ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(slice.color)
                                            )
                                            Text(
                                                text = slice.label,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Text(
                                            text = "${slice.percentage}% • ₹${"%,.0f".format(slice.amount)}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. WEEKLY SPEND TREND (Bar Chart Card)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PayStoryTokens.SpaceLg)
                        .testTag("monthly_analytics_card"),
                    shape = PayStoryTokens.Radius2Xl,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(PayStoryTokens.SpaceLg),
                        verticalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceMd)
                    ) {
                        val monthlyHistory = viewModel.getMonthlySpendingHistory(txList)
                        val historyBars = monthlyHistory.map { (name, amt) -> name to amt }
                        val avgPeriod = if (historyBars.isNotEmpty()) historyBars.sumOf { it.second } / historyBars.size else 0.0

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "WEEKLY SPEND TREND",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Avg. ₹${"%,.0f".format(avgPeriod)} / period",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        val maxAmt = historyBars.maxOfOrNull { it.second } ?: 1.0

                        if (historyBars.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No periodic spend trend yet",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .padding(top = PayStoryTokens.SpaceSm),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                historyBars.forEach { (label, amt) ->
                                    val ratio = if (maxAmt > 0) (amt / maxAmt).toFloat().coerceIn(0.12f, 1f) else 0.12f
                                    val isPeak = amt == maxAmt && amt > 0

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Bottom,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "₹${"%,.0f".format(amt)}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = if (isPeak) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = if (isPeak) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight(ratio)
                                                .width(24.dp)
                                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                                .background(
                                                    if (isPeak) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                                                )
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. TOP SPENDING CATEGORIES
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PayStoryTokens.SpaceLg)
                        .testTag("top_categories_card"),
                    shape = PayStoryTokens.Radius2Xl,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(PayStoryTokens.SpaceLg),
                        verticalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceMd)
                    ) {
                        Text(
                            text = "TOP CATEGORIES & MERCHANTS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val sortedCategoriesSpending = viewModel.getCategorySpendingMap(txList)
                            .toList()
                            .sortedByDescending { it.second }

                        val highestVal = sortedCategoriesSpending.firstOrNull()?.second ?: 1.0
                        val itemsToShow = sortedCategoriesSpending.take(4)

                        if (itemsToShow.isEmpty()) {
                            Text(
                                text = "No category breakdowns available yet",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            itemsToShow.forEach { (catName, amt) ->
                                val catObj = Category.values().firstOrNull { it.name == catName } ?: Category.OTHERS
                                val progress = (amt / highestVal).toFloat().coerceIn(0.1f, 1f)

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(catObj.icon, fontSize = 18.sp)
                                        Text(
                                            text = catObj.displayName,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Text(
                                        text = "₹${"%,.0f".format(amt)}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

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
                                            .background(MaterialTheme.colorScheme.primary)
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
}

data class CategoryChartSlice(
    val key: String,
    val amount: Double,
    val label: String,
    val icon: String,
    val color: Color,
    val startAngle: Float,
    val sweepAngle: Float,
    val percentage: Int
)
