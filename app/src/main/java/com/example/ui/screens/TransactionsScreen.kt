package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.models.Category
import com.example.data.models.Transaction
import com.example.ui.viewmodel.ExpenseViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(viewModel: ExpenseViewModel) {
    val filteredList by viewModel.filteredTransactions.collectAsState()
    val searchQuery by viewModel.searchText.collectAsState()
    val selectedCategory by viewModel.categoryFilter.collectAsState()
    val sortAsc by viewModel.sortByDateAsc.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showFormDialog by remember { mutableStateOf(false) }
    var selectedDetailTx by remember { mutableStateOf<Transaction?>(null) }

    val totalSpent = filteredList.filter { it.transactionType == "SENT" }.sumOf { it.amount }
    val avgPerDay = if (filteredList.isNotEmpty()) (totalSpent / 30.0) else 0.0

    val currentMonth = remember { SimpleDateFormat("MMMM", Locale.getDefault()).format(Date()).uppercase() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.prepareAddTransaction()
                    showFormDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = PayStoryTokens.RadiusFull,
                modifier = Modifier.testTag("add_transaction_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // TOP APP BAR
            PayStoryTopAppBar(
                userInitials = currentUser?.name?.take(2)?.uppercase()?.ifBlank { "PS" } ?: "PS"
            )

            // TITLE & SUBTITLE + ACTION BUTTONS ROW
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PayStoryTokens.SpaceLg),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Transactions",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Your monthly financial story ledger",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceXs)) {
                    IconButton(
                        onClick = { /* Calendar range selector */ },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Date range",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.toggleDateSort() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "Sort order",
                            tint = if (sortAsc) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(PayStoryTokens.SpaceMd))

            // SPENT IN MONTH CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PayStoryTokens.SpaceLg),
                shape = PayStoryTokens.Radius2Xl,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(PayStoryTokens.SpaceLg),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SPENT IN $currentMonth",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(PayStoryTokens.SpaceXs))
                        Text(
                            text = "₹${"%,.2f".format(totalSpent)}",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        PayStoryPill(
                            text = "${filteredList.size} stories",
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.primary,
                            dotColor = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(PayStoryTokens.SpaceXs))
                        Text(
                            text = "avg. ₹${"%,.0f".format(avgPerDay)} / day",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(PayStoryTokens.SpaceMd))

            // SEARCH INPUT FIELD
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchText(it) },
                placeholder = {
                    Text(
                        "Search merchant, bank, or story...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchText("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PayStoryTokens.SpaceLg)
                    .testTag("transaction_search_input"),
                shape = PayStoryTokens.RadiusMd,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.height(PayStoryTokens.SpaceSm))

            // CATEGORY FILTER CHIPS ROW
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = PayStoryTokens.SpaceLg),
                horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceSm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // All Chip
                FilterChip(
                    selected = selectedCategory == "ALL",
                    onClick = { viewModel.updateCategoryFilter("ALL") },
                    label = { Text("All") },
                    shape = PayStoryTokens.RadiusFull,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedCategory == "ALL",
                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        selectedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )

                // Category items
                Category.values().filter { it != Category.UNCATEGORIZED }.forEach { cat ->
                    val isSelected = selectedCategory == cat.name
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) viewModel.updateCategoryFilter("ALL")
                            else viewModel.updateCategoryFilter(cat.name)
                        },
                        label = { Text("${cat.icon} ${cat.displayName}") },
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

            Spacer(modifier = Modifier.height(PayStoryTokens.SpaceSm))

            // GROUPED TRANSACTIONS LIST
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceSm)
                    ) {
                        Text("🔍", fontSize = 36.sp)
                        Text(
                            text = "No matching transactions",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Try adjusting your search query or filter chips.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                val grouped = remember(filteredList) { groupTransactionsByDate(filteredList) }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = PayStoryTokens.SpaceLg),
                    verticalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceMd),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    grouped.forEach { (dateHeader, txList) ->
                        val dayTotal = txList.filter { it.transactionType == "SENT" }.sumOf { it.amount }

                        item(key = "header_$dateHeader") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = PayStoryTokens.SpaceSm),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dateHeader,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "-₹${"%,.2f".format(dayTotal)} net",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        items(txList, key = { it.transactionId }) { tx ->
                            TransactionStoryCard(
                                tx = tx,
                                onClick = { selectedDetailTx = tx }
                            )
                        }
                    }
                }
            }
        }
    }

    // 1. DETAIL SHEET DIALOG
    selectedDetailTx?.let { tx ->
        val catDetails = Category.values().firstOrNull { it.name == tx.category } ?: Category.UNCATEGORIZED
        val sdf = SimpleDateFormat("EEEE, MMMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        val dateString = sdf.format(Date(tx.timestamp))

        Dialog(onDismissRequest = { selectedDetailTx = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transaction_detail_dialog"),
                shape = PayStoryTokens.Radius2Xl,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(PayStoryTokens.SpaceXl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceBase)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PayStoryPill(
                            text = if (tx.isReviewed) "Reviewed" else "Pending Review",
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        IconButton(onClick = { selectedDetailTx = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Text(
                        text = if (tx.transactionType == "SENT") "-₹${"%,.2f".format(tx.amount)}" else "+₹${"%,.2f".format(tx.amount)}",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                        color = if (tx.transactionType == "SENT") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = tx.merchantName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = PayStoryTokens.RadiusMd,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(PayStoryTokens.SpaceBase),
                            verticalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceSm)
                        ) {
                            DetailItemRow(label = "Category", value = "${catDetails.icon} ${catDetails.displayName}")
                            DetailItemRow(label = "Type", value = if (tx.transactionType == "SENT") "Sent Out" else "Received In")
                            DetailItemRow(label = "Source", value = if (tx.source == "sms") "Bank SMS" else "Notification Listener")
                            if (tx.referenceNumber != null) {
                                DetailItemRow(label = "Ref / UTR", value = tx.referenceNumber)
                            }
                            DetailItemRow(label = "Timestamp", value = dateString)
                            if (tx.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(PayStoryTokens.SpaceXs))
                                Text("Story:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                Text(
                                    text = "\"${tx.description}\"",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceSm)
                    ) {
                        IconButton(
                            onClick = {
                                viewModel.deleteTransaction(tx.transactionId)
                                selectedDetailTx = null
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(PayStoryTokens.RadiusMd)
                                .background(MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.prepareEditTransaction(tx)
                                selectedDetailTx = null
                                showFormDialog = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = PayStoryTokens.RadiusFull,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(PayStoryTokens.SpaceSm))
                            Text("Edit Story", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // 2. ADD & EDIT DIALOG PANEL FORM
    if (showFormDialog) {
        Dialog(onDismissRequest = { showFormDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_edit_transaction_dialog"),
                shape = PayStoryTokens.Radius2Xl,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .padding(PayStoryTokens.SpaceXl)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceBase)
                ) {
                    val isEditMode = viewModel.editingTransactionId != null

                    Text(
                        text = if (isEditMode) "Edit Memory Context" else "Record New Memory",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    // Amount Textbox
                    OutlinedTextField(
                        value = viewModel.editTxAmount.value,
                        onValueChange = { viewModel.editTxAmount.value = it },
                        label = { Text("Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transaction_amount_field"),
                        singleLine = true,
                        shape = PayStoryTokens.RadiusMd
                    )

                    // Merchant Name
                    OutlinedTextField(
                        value = viewModel.editTxMerchant.value,
                        onValueChange = { viewModel.editTxMerchant.value = it },
                        label = { Text("Merchant / Payee Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transaction_merchant_field"),
                        singleLine = true,
                        shape = PayStoryTokens.RadiusMd
                    )

                    // Sent vs Received
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceSm)
                    ) {
                        val activeType = viewModel.editTxType.value
                        FilterChip(
                            selected = activeType == "SENT",
                            onClick = { viewModel.editTxType.value = "SENT" },
                            label = { Text("💸 Sent Out") },
                            modifier = Modifier.weight(1f),
                            shape = PayStoryTokens.RadiusFull
                        )

                        FilterChip(
                            selected = activeType == "RECEIVED",
                            onClick = { viewModel.editTxType.value = "RECEIVED" },
                            label = { Text("📥 Received In") },
                            modifier = Modifier.weight(1f),
                            shape = PayStoryTokens.RadiusFull
                        )
                    }

                    // Category Selection
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceSm)
                    ) {
                        Category.values().filter { it != Category.UNCATEGORIZED }.forEach { cat ->
                            val isSelected = viewModel.editTxCategory.value == cat.name
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.editTxCategory.value = cat.name },
                                label = { Text("${cat.icon} ${cat.displayName}") },
                                shape = PayStoryTokens.RadiusFull
                            )
                        }
                    }

                    // Story Notes
                    OutlinedTextField(
                        value = viewModel.editTxDescription.value,
                        onValueChange = { viewModel.editTxDescription.value = it },
                        label = { Text("What's the story behind this payment?") },
                        placeholder = { Text("e.g. Dinner with team, Groceries...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transaction_description_field"),
                        shape = PayStoryTokens.RadiusMd
                    )

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceSm)
                    ) {
                        OutlinedButton(
                            onClick = { showFormDialog = false },
                            modifier = Modifier.weight(1f),
                            shape = PayStoryTokens.RadiusFull
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                viewModel.saveTransactionForm {
                                    showFormDialog = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = PayStoryTokens.RadiusFull,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Save Changes", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionStoryCard(
    tx: Transaction,
    onClick: () -> Unit
) {
    val category = Category.values().firstOrNull { it.name == tx.category } ?: Category.UNCATEGORIZED
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(tx.timestamp))

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
                // Category Icon Badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(PayStoryTokens.RadiusMd)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(category.icon, fontSize = 22.sp)
                }

                Column {
                    Text(
                        text = tx.merchantName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (tx.description.isNotBlank()) {
                        Text(
                            text = "\"${tx.description}\"",
                            style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "$formattedTime • UPI (${tx.source.uppercase()})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (tx.transactionType == "SENT") "-₹${"%,.2f".format(tx.amount)}" else "+₹${"%,.2f".format(tx.amount)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (tx.transactionType == "SENT") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(2.dp))

                PayStoryPill(
                    text = category.displayName,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DetailItemRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun groupTransactionsByDate(transactions: List<Transaction>): Map<String, List<Transaction>> {
    val map = mutableMapOf<String, MutableList<Transaction>>()
    val todayCal = Calendar.getInstance()
    val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

    val fullFormatter = SimpleDateFormat("EEE, d MMM", Locale.getDefault())

    transactions.forEach { tx ->
        val txCal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
        val header = when {
            isSameDay(txCal, todayCal) -> "Today"
            isSameDay(txCal, yesterdayCal) -> "Yesterday"
            else -> fullFormatter.format(Date(tx.timestamp))
        }
        map.getOrPut(header) { mutableListOf() }.add(tx)
    }
    return map
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
