package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
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

@Composable
fun AnalyticsScreen(viewModel: ExpenseViewModel) {
    val txList by viewModel.allTransactions.collectAsState()
    val totalSpend = txList.filter { it.transactionType == "SENT" }.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Headers
        Text(
            "Spend Analytics",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.primary
        )

        if (txList.isEmpty() || totalSpend == 0.0) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("📊", fontSize = 48.sp)
                    Text("No Analytics Available", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                    Text(
                        "Once you log or simulate expenditures, real-time graphical category breakouts and monthly analytics will render here instantly.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Spend summary card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Total Money Tracked",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        "₹${"%,.2f".format(totalSpend)}",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 1. CHART: CATEGORY-WISE SPENDING DONUT CHART
            Card(
                modifier = Modifier.fillMaxWidth().testTag("category_analytics_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        "Category Division",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    val categoryMap = viewModel.getCategorySpendingMap(txList)
                    val chartSlices = remember(categoryMap) {
                        var cumulativeSum = 0f
                        categoryMap.map { entry ->
                            val percent = (entry.value / totalSpend).toFloat()
                            val sweepAngle = percent * 360f
                            val startAngle = cumulativeSum
                            cumulativeSum += sweepAngle
                            
                            val catObj = Category.values().firstOrNull { it.name == entry.key } ?: Category.OTHERS
                            // Custom colorful assignment
                            val sectorColor = when (catObj) {
                                Category.FOOD -> Color(0xFFFB923C) // Orange
                                Category.GROCERY -> Color(0xFFFACC15) // Gold
                                Category.SHOPPING -> Color(0xFFEC4899) // Pink
                                Category.TRAVEL -> Color(0xFF3B82F6) // Blue
                                Category.FUEL -> Color(0xFF0EA5E9) // Sky Blue
                                Category.BILLS -> Color(0xFF8B5CF6) // Purple
                                Category.RENT -> Color(0xFFEF4444) // Red
                                Category.EDUCATION -> Color(0xFF10B981) // Teal Mint
                                Category.HEALTH -> Color(0xFFF43F5E) // Coral
                                Category.ENTERTAINMENT -> Color(0xFF6366F1) // Indigo
                                else -> Color(0xFF94A3B8) // Gray
                            }

                            CategoryChartSlice(
                                key = entry.key,
                                amount = entry.value,
                                label = catObj.displayName,
                                icon = catObj.icon,
                                color = sectorColor,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                percentage = (percent * 100).toInt()
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left: Canvas Drawing Donut
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(140.dp)
                        ) {
                            Canvas(modifier = Modifier.size(120.dp)) {
                                chartSlices.forEach { slice ->
                                    drawArc(
                                        color = slice.color,
                                        startAngle = slice.startAngle,
                                        sweepAngle = slice.sweepAngle,
                                        useCenter = false,
                                        style = Stroke(width = 44f, cap = StrokeCap.Round),
                                        size = Size(size.width, size.height)
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Spends", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                                Text("Context", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Right: Map Legend details
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            chartSlices.take(5).forEach { slice ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(slice.color, CircleShape)
                                    )
                                    Text(
                                        text = "${slice.icon} ${slice.label} • ${slice.percentage}%",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. CHART: MONTHLY SPENDING BAR GRAPH
            Card(
                modifier = Modifier.fillMaxWidth().testTag("monthly_analytics_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        "Monthly Spending History",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    val monthlyHistory = viewModel.getMonthlySpendingHistory(txList)
                    val historyLabels = monthlyHistory.keys.toList()
                    val historyValues = monthlyHistory.values.toList()
                    val maxMonthVal = historyValues.maxOrNull() ?: 1.0

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        monthlyHistory.forEach { (monthName, amount) ->
                            val scaleRatio = (amount / maxMonthVal).toFloat().coerceAtLeast(0.08f)
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "₹${"%,.0f".format(amount)}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 8.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                // Decorative cylinder bar column
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight(scaleRatio)
                                        .width(28.dp)
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.primary,
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                                )
                                            ),
                                            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                        )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = monthName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            // 3. FEATURE: TOP SPENDING CATEGORIES
            Card(
                modifier = Modifier.fillMaxWidth().testTag("top_categories_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Top Spending Categories",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    val sortedCategoriesSpending = viewModel.getCategorySpendingMap(txList)
                        .toList()
                        .sortedByDescending { it.second }

                    val highestSpendingCategoryValue = sortedCategoriesSpending.firstOrNull()?.second ?: 1.0

                    sortedCategoriesSpending.take(4).forEach { (categoryName, amountVal) ->
                        val catDetails = Category.values().firstOrNull { it.name == categoryName } ?: Category.OTHERS
                        val ratio = (amountVal / highestSpendingCategoryValue).toFloat().coerceAtLeast(0.05f)

                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
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
                                    Text(catDetails.icon, fontSize = 20.sp)
                                    Text(catDetails.displayName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                }

                                Text(
                                    text = "₹${"%,.0f".format(amountVal)} spent",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            LinearProgressIndicator(
                                progress = { ratio },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                strokeCap = StrokeCap.Round,
                                color = MaterialTheme.colorScheme.secondary,
                                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                            )
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
