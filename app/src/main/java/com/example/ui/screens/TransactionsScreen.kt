package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(viewModel: ExpenseViewModel) {
    val items by viewModel.filteredTransactions.collectAsState()
    val searchQuery by viewModel.searchText.collectAsState()
    val activeCategory by viewModel.categoryFilter.collectAsState()
    val sortAsc by viewModel.sortByDateAsc.collectAsState()

    var showFormDialog by remember { mutableStateOf(false) }
    var selectedDetailTx by remember { mutableStateOf<Transaction?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.prepareAddTransaction()
                    showFormDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_transaction_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Search Bar & Filter Headers
            Text(
                "All Spends",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.primary
            )

            // Search input field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchText(it) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchText("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                placeholder = { Text("Search merchant, amount, notes...") },
                modifier = Modifier.fillMaxWidth().testTag("transaction_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Category horizontally scrolling filter list & Sort button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // "ALL" Filter option
                    FilterChip(
                        selected = activeCategory == "ALL",
                        onClick = { viewModel.updateCategoryFilter("ALL") },
                        label = { Text("🌍 All") }
                    )

                    // Categories options
                    Category.values().forEach { cat ->
                        FilterChip(
                            selected = activeCategory == cat.name,
                            onClick = { viewModel.updateCategoryFilter(cat.name) },
                            label = { Text("${cat.icon} ${cat.displayName}") }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Sort toggle button
                IconButton(
                    onClick = { viewModel.toggleDateSort() },
                    modifier = Modifier.background(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ).size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Toggle Sort Date",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // Transaction Count Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${items.size} matching transactions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                if (sortAsc) {
                    Text(
                        "Showing: Oldest First",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        "Showing: Newest First",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Results List
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("🔍", fontSize = 36.sp)
                        Text(
                            "No matching transactions",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                        Text(
                            "Try altering search query or categories.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items, key = { it.transactionId }) { tx ->
                        TransactionListItem(tx = tx, onClick = {
                            selectedDetailTx = tx
                        })
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
                modifier = Modifier.fillMaxWidth().testTag("transaction_detail_dialog"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Dialog
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (tx.isReviewed) "Reviewed" else "Pending Review",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        IconButton(onClick = { selectedDetailTx = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    // Huge Amount display
                    Text(
                        text = if (tx.transactionType == "SENT") "-₹${"%,.2f".format(tx.amount)}" else "+₹${"%,.2f".format(tx.amount)}",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                        color = if (tx.transactionType == "SENT") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                    )

                    // Details block
                    Text(
                        text = tx.merchantName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        textAlign = TextAlign.Center
                    )

                    // Details grid elements
                    Column(
                        modifier = Modifier.fillMaxWidth().background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("Category:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                            Text("${catDetails.icon} ${catDetails.displayName}", style = MaterialTheme.typography.bodyMedium)
                        }
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("Type:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                            Text(if (tx.transactionType == "SENT") "Sent Money" else "Received Money", style = MaterialTheme.typography.bodyMedium, color = if (tx.transactionType == "SENT") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary)
                        }

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("Source:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                            Text(if (tx.source == "sms") "Bank SMS" else "App Notification", style = MaterialTheme.typography.bodyMedium)
                        }

                        if (tx.referenceNumber != null) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("Ref No/UTR:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                                Text(tx.referenceNumber, style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("Time:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                            Text(dateString, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End)
                        }

                        if (tx.description.isNotBlank()) {
                            Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                                Text("Story:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text(tx.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                            }
                        }
                    }

                    // Controls Inside modal
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Delete Transaction button
                        IconButton(
                            onClick = {
                                viewModel.deleteTransaction(tx.transactionId)
                                selectedDetailTx = null
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }

                        // Edit Transaction button (Prepares editing forms and brings up Form)
                        Button(
                            onClick = {
                                viewModel.prepareEditTransaction(tx)
                                selectedDetailTx = null
                                showFormDialog = true
                            },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Edit Memory")
                        }
                    }
                }
            }
        }
    }

    // 2. ADD & EDIT DIALOG PANEL FORM (Satisfies "Add/Edit Transaction Screen")
    if (showFormDialog) {
        Dialog(onDismissRequest = { showFormDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("add_edit_transaction_dialog"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    val isEditMode = viewModel.editingTransactionId != null
                    
                    Text(
                        text = if (isEditMode) "Edit Memory Context" else "Record New Memory",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    // Input 1: Amount Textbox
                    OutlinedTextField(
                        value = viewModel.editTxAmount.value,
                        onValueChange = { viewModel.editTxAmount.value = it },
                        label = { Text("Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("transaction_amount_field"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Input 2: Merchant Name Textbox
                    OutlinedTextField(
                        value = viewModel.editTxMerchant.value,
                        onValueChange = { viewModel.editTxMerchant.value = it },
                        label = { Text("Merchant / Person Name") },
                        modifier = Modifier.fillMaxWidth().testTag("transaction_merchant_field"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Input 3: Transaction Type (Sent vs Received Selectors)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        viewModel.editTxType.value.let { activeType ->
                            OutlinedIconToggleButton(
                                checked = activeType == "SENT",
                                onCheckedChange = { viewModel.editTxType.value = "SENT" },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("💸 Sent Out", fontWeight = FontWeight.SemiBold, color = if (activeType == "SENT") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                            }

                            OutlinedIconToggleButton(
                                checked = activeType == "RECEIVED",
                                onCheckedChange = { viewModel.editTxType.value = "RECEIVED" },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("📥 Received", fontWeight = FontWeight.SemiBold, color = if (activeType == "RECEIVED") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    // Input 4: Select Category scroll row
                    Text("Select Category", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Category.values().filter { it != Category.UNCATEGORIZED }.forEach { cat ->
                            val isSelected = viewModel.editTxCategory.value == cat.name
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.editTxCategory.value = cat.name },
                                label = { Text("${cat.icon} ${cat.displayName}") }
                            )
                        }
                    }

                    // Input 5: Memory Description / Notes
                    OutlinedTextField(
                        value = viewModel.editTxDescription.value,
                        onValueChange = { viewModel.editTxDescription.value = it },
                        label = { Text("What's the story behind this payment?") },
                        placeholder = { Text("Enter the story...") },
                        modifier = Modifier.fillMaxWidth().testTag("transaction_description_field"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Submit indicators
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showFormDialog = false },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                viewModel.saveTransactionForm {
                                    showFormDialog = false
                                }
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Save changes")
                        }
                    }
                }
            }
        }
    }
}
