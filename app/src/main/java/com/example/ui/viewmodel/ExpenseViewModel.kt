package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ExpenseApplication
import com.example.data.models.Budget
import com.example.data.models.BudgetAlert
import com.example.data.models.Category
import com.example.data.models.Transaction
import com.example.data.models.User
import com.example.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

sealed interface NavigationDestination {
    object AuthLogin : NavigationDestination
    object AuthSignUp : NavigationDestination
    object AuthForgot : NavigationDestination
    object Onboarding : NavigationDestination
    object MainFlow : NavigationDestination
}

sealed class MainTab(val name: String) {
    object Home : MainTab("Home")
    object Transactions : MainTab("Transactions")
    object Budgets : MainTab("Budgets")
    object Analytics : MainTab("Analytics")
    object Settings : MainTab("Settings")
}

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository = (application as ExpenseApplication).repository

    // Navigation and Tab States
    val currentUser: StateFlow<User?> = repository.currentUser
    val isOnboarded: StateFlow<Boolean> = repository.isOnboarded

    private val _currentDestination = MutableStateFlow<NavigationDestination>(NavigationDestination.AuthLogin)
    val currentDestination: StateFlow<NavigationDestination> = _currentDestination.asStateFlow()

    private val _currentTab = MutableStateFlow<MainTab>(MainTab.Home)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    // Auth screen inputs
    var isSignUpActive = mutableStateOf(false)
    var authName = mutableStateOf("")
    var authEmail = mutableStateOf("")
    var authPassword = mutableStateOf("")
    
    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    // App Preferences
    private val sharedPrefs = application.getSharedPreferences("expense_memory_ui_prefs", Context.MODE_PRIVATE)
    private val _isDarkMode = MutableStateFlow(sharedPrefs.getBoolean("dark_mode", true)) // default dark mode for premium look!
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _overallBudgetLimit = MutableStateFlow(sharedPrefs.getFloat("overall_budget_limit", 50000f).toDouble())
    val overallBudgetLimit: StateFlow<Double> = _overallBudgetLimit.asStateFlow()

    private val _overallBudgetPeriod = MutableStateFlow(sharedPrefs.getString("overall_budget_period", "MONTH") ?: "MONTH")
    val overallBudgetPeriod: StateFlow<String> = _overallBudgetPeriod.asStateFlow()

    fun updateOverallBudget(limit: Double, period: String) {
        _overallBudgetLimit.value = limit
        _overallBudgetPeriod.value = period
        sharedPrefs.edit()
            .putFloat("overall_budget_limit", limit.toFloat())
            .putString("overall_budget_period", period)
            .apply()
    }

    fun getPeriodSpending(txList: List<Transaction>, period: String): Double {
        val startTime = Calendar.getInstance().apply {
            if (period == "WEEK") {
                set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            } else {
                set(Calendar.DAY_OF_MONTH, 1)
            }
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        return txList.filter {
            it.timestamp >= startTime && it.transactionType == "SENT"
        }.sumOf { it.amount }
    }

    // Transactions Lists & Filters
    val allTransactions: StateFlow<List<Transaction>> = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingReviewTransactions: StateFlow<List<Transaction>> = repository.getPendingReviewTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Transactions Filtes Setup
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _categoryFilter = MutableStateFlow<String>("ALL") // "ALL" or category name
    val categoryFilter: StateFlow<String> = _categoryFilter.asStateFlow()

    private val _sortByDateAsc = MutableStateFlow(false) // default desc (newest first)
    val sortByDateAsc: StateFlow<Boolean> = _sortByDateAsc.asStateFlow()

    // Filtered transaction list
    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        allTransactions, _searchText, _categoryFilter, _sortByDateAsc
    ) { txList, query, cat, asc ->
        var list = txList
        
        if (query.isNotBlank()) {
            list = list.filter {
                it.merchantName.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true) ||
                it.amount.toString().contains(query)
            }
        }
        
        if (cat != "ALL") {
            list = list.filter { it.category == cat }
        }
        
        if (asc) {
            list.sortedBy { it.timestamp }
        } else {
            list.sortedByDescending { it.timestamp }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Budgets & Alerts
    val budgets: StateFlow<List<Budget>> = repository.getAllBudgets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alerts: StateFlow<List<BudgetAlert>> = repository.getAllAlerts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active edit/add transaction form fields
    var editTxAmount = mutableStateOf("")
    var editTxMerchant = mutableStateOf("")
    var editTxType = mutableStateOf("SENT") // "SENT" or "RECEIVED"
    var editTxCategory = mutableStateOf(Category.FOOD.name)
    var editTxDescription = mutableStateOf("")
    var editingTransactionId: String? = null // if null, it's a create flow

    init {
        // Observe currentUser and onboarded to route destination
        combine(currentUser, isOnboarded) { user, onboarded ->
            when {
                user == null -> NavigationDestination.AuthLogin
                !onboarded -> NavigationDestination.Onboarding
                else -> NavigationDestination.MainFlow
            }
        }.onEach { destination ->
            _currentDestination.value = destination
        }.launchIn(viewModelScope)
    }

    // AUTH ACTION METHODS
    fun setAuthError(err: String?) {
        _authError.value = err
    }

    fun handleLogin() {
        _isAuthLoading.value = true
        _authError.value = null
        repository.login(
            email = authEmail.value,
            password = authPassword.value,
            onSuccess = {
                _isAuthLoading.value = false
                // On success, destination routes automatically via Flow observer
            },
            onError = { err ->
                _isAuthLoading.value = false
                _authError.value = err
            }
        )
    }

    fun handleSignUp() {
        _isAuthLoading.value = true
        _authError.value = null
        repository.signUp(
            name = authName.value,
            email = authEmail.value,
            password = authPassword.value,
            onSuccess = {
                _isAuthLoading.value = false
            },
            onError = { err ->
                _isAuthLoading.value = false
                _authError.value = err
            }
        )
    }

    fun handleForgot() {
        _isAuthLoading.value = true
        _authError.value = null
        repository.forgotPassword(
            email = authEmail.value,
            onSuccess = {
                _isAuthLoading.value = false
                _authError.value = "Recovery email sent successfully (Simulation)"
            },
            onError = { err ->
                _isAuthLoading.value = false
                _authError.value = err
            }
        )
    }

    fun handleLogout() {
        repository.logout()
        // Reset form inputs
        authName.value = ""
        authEmail.value = ""
        authPassword.value = ""
        _currentTab.value = MainTab.Home
    }

    fun completeOnboarding() {
        repository.completeOnboarding()
    }

    fun changeTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun toggleDarkMode() {
        val newVal = !_isDarkMode.value
        _isDarkMode.value = newVal
        sharedPrefs.edit().putBoolean("dark_mode", newVal).apply()
    }

    // SEARCH & FILTER METHODS
    fun updateSearchText(text: String) {
        _searchText.value = text
    }

    fun updateCategoryFilter(cat: String) {
        _categoryFilter.value = cat
    }

    fun toggleDateSort() {
        _sortByDateAsc.value = !_sortByDateAsc.value
    }

    // ADD / EDIT AND REVIEW TRANSACTION WORKFLOW
    fun prepareAddTransaction() {
        editingTransactionId = null
        editTxAmount.value = ""
        editTxMerchant.value = ""
        editTxType.value = "SENT"
        editTxCategory.value = Category.FOOD.name
        editTxDescription.value = ""
    }

    fun prepareEditTransaction(tx: Transaction) {
        editingTransactionId = tx.transactionId
        editTxAmount.value = tx.amount.toString()
        editTxMerchant.value = tx.merchantName
        editTxType.value = tx.transactionType
        editTxCategory.value = tx.category
        editTxDescription.value = tx.description
    }

    fun saveTransactionForm(onComplete: () -> Unit) {
        val amountValue = editTxAmount.value.toDoubleOrNull() ?: 0.0
        if (amountValue <= 0.0 || editTxMerchant.value.isBlank()) {
            return
        }

        viewModelScope.launch {
            val uId = currentUser.value?.userId ?: "demo_user_123"
            val targetId = editingTransactionId ?: UUID.randomUUID().toString()
            
            val updatedTx = Transaction(
                transactionId = targetId,
                amount = amountValue,
                merchantName = editTxMerchant.value.trim(),
                transactionType = editTxType.value,
                category = editTxCategory.value,
                description = editTxDescription.value,
                timestamp = System.currentTimeMillis(),
                userId = uId,
                createdAt = System.currentTimeMillis(),
                isReviewed = true // Saving manually guarantees reviewed state!
            )
            repository.saveTransaction(updatedTx)
            onComplete()
        }
    }

    fun reviewTransaction(tx: Transaction, category: String, description: String) {
        viewModelScope.launch {
            val updatedTx = tx.copy(
                category = category,
                description = description,
                isReviewed = true
            )
            repository.saveTransaction(updatedTx)
        }
    }

    fun skipTransaction(tx: Transaction) {
        viewModelScope.launch {
            // Skips save into "UNCATEGORIZED" or "OTHERS"
            val updatedTx = tx.copy(
                category = Category.UNCATEGORIZED.name,
                isReviewed = true
            )
            repository.saveTransaction(updatedTx)
        }
    }

    fun deleteTransaction(txId: String) {
        viewModelScope.launch {
            repository.deleteTransaction(txId)
        }
    }

    // BUDGET METHODS
    fun setBudgetLimit(categoryName: String, limit: Double) {
        viewModelScope.launch {
            repository.setBudgetLimit(categoryName, limit)
        }
    }

    fun removeBudgetLimit(categoryName: String) {
        viewModelScope.launch {
            repository.deleteBudgetLimit(categoryName)
        }
    }

    fun clearAlerts() {
        viewModelScope.launch {
            repository.clearAlertHistory()
        }
    }

    // SIMULATE TRANSACTION ARRIVAL (For UI & Demo testing purposes in the emulator!)
    fun simulateAutoTransactionArrival() {
        viewModelScope.launch {
            val amount = (100..4500).random().toDouble()
            val uId = currentUser.value?.userId ?: "demo_user_123"
            val timestamp = System.currentTimeMillis()
            
            // Alternate between Expense from SMS and Income from Notifications
            val isExpense = (0..1).random() == 0
            val incomingTx = if (isExpense) {
                val bankNames = listOf("SBI Bank", "HDFC Bank", "ICICI Bank", "AXIS Bank")
                val bank = bankNames.random()
                val refNum = "UTR" + (100000000000L..999999999999L).random().toString()
                Transaction(
                    transactionId = UUID.randomUUID().toString(),
                    amount = amount,
                    merchantName = bank,
                    transactionType = "SENT",
                    category = Category.UNCATEGORIZED.name,
                    description = "Simulated expense bank SMS: Rs. $amount debited from A/c XX",
                    timestamp = timestamp,
                    userId = uId,
                    createdAt = timestamp,
                    isReviewed = false,
                    source = "sms",
                    referenceNumber = refNum
                )
            } else {
                val senders = listOf("Rahul Sharma", "Ananya Sen", "Swiggy Refund", "Vijay Shekhar")
                val sender = senders.random()
                Transaction(
                    transactionId = UUID.randomUUID().toString(),
                    amount = amount,
                    merchantName = sender,
                    transactionType = "RECEIVED",
                    category = Category.UNCATEGORIZED.name,
                    description = "Simulated income notification: ₹$amount received from $sender",
                    timestamp = timestamp,
                    userId = uId,
                    createdAt = timestamp,
                    isReviewed = false,
                    source = "notification",
                    referenceNumber = null
                )
            }
            repository.saveTransaction(incomingTx)
        }
    }

    // COMPUTE STATS METHODS
    fun getTodaySpending(txList: List<Transaction>): Double {
        val midnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        return txList.filter {
            it.timestamp >= midnight && it.transactionType == "SENT"
        }.sumOf { it.amount }
    }

    fun getThisMonthSpending(txList: List<Transaction>): Double {
        val monthStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        return txList.filter {
            it.timestamp >= monthStart && it.transactionType == "SENT"
        }.sumOf { it.amount }
    }

    fun getCategorySpendingMap(txList: List<Transaction>): Map<String, Double> {
        return txList.filter { it.transactionType == "SENT" }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }

    fun getMonthlySpendingHistory(txList: List<Transaction>): Map<String, Double> {
        // Returns Month Name (e.g., Jun, Jul) -> Double Spending
        val format = SimpleDateFormat("MMM", Locale.US)
        return txList.filter { it.transactionType == "SENT" }
            .groupBy { format.format(Date(it.timestamp)) }
            .mapValues { it.value.sumOf { item -> item.amount } }
    }

    fun getCategoryBudgetSpent(categoryName: String, txList: List<Transaction>): Double {
        return txList.filter {
            it.category.equals(categoryName, ignoreCase = true) && it.transactionType == "SENT"
        }.sumOf { it.amount }
    }
}
