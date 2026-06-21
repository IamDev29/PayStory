package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.local.TransactionDao
import com.example.data.models.Budget
import com.example.data.models.BudgetAlert
import com.example.data.models.Category
import com.example.data.models.Transaction
import com.example.data.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

class ExpenseRepository(
    private val context: Context,
    private val dao: TransactionDao
) {
    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences("expense_memory_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isOnboarded = MutableStateFlow(false)
    val isOnboarded: StateFlow<Boolean> = _isOnboarded.asStateFlow()

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        // Load active session from SharedPreferences on startup
        val userId = sharedPrefs.getString("logged_in_user_id", null)
        val onboarded = sharedPrefs.getBoolean("onboarded", false)
        _isOnboarded.value = onboarded

        if (userId != null) {
            repositoryScope.launch {
                val user = dao.getUser(userId).firstOrNull() ?: User(
                    userId = userId,
                    name = sharedPrefs.getString("user_name", "User") ?: "User",
                    email = sharedPrefs.getString("user_email", "") ?: "",
                    isOnboarded = onboarded
                )
                _currentUser.value = user
            }
        }
    }

    // AUTHENTICATION APIs
    fun signUp(name: String, email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            onError("All fields are required")
            return
        }
        if (password.length < 6) {
            onError("Password must be at least 6 characters")
            return
        }

        repositoryScope.launch {
            try {
                val userId = UUID.randomUUID().toString()
                val newUser = User(userId = userId, name = name, email = email, isOnboarded = false)
                dao.insertUser(newUser)

                sharedPrefs.edit()
                    .putString("logged_in_user_id", userId)
                    .putString("user_name", name)
                    .putString("user_email", email)
                    .putString("password_storage_${email.lowercase()}", password)
                    .putString("userId_storage_${email.lowercase()}", userId)
                    .apply()

                _currentUser.value = newUser
                launch(Dispatchers.Main) { onSuccess() }
            } catch (e: Exception) {
                Log.e("ExpenseRepository", "Sign-up error", e)
                launch(Dispatchers.Main) { onError(e.message ?: "Could not register user") }
            }
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            onError("Email and password are required")
            return
        }

        repositoryScope.launch {
            try {
                val lowercaseEmail = email.lowercase()
                val registeredPw = sharedPrefs.getString("password_storage_$lowercaseEmail", null)
                val storedUserId = sharedPrefs.getString("userId_storage_$lowercaseEmail", null)

                if (registeredPw == null || storedUserId == null) {
                    // Prepopulate if empty for demo/testing convenience!
                    val demoUserId = "demo_user_123"
                    val demoUser = User(userId = demoUserId, name = "Ankit Kumar", email = email, isOnboarded = _isOnboarded.value)
                    dao.insertUser(demoUser)
                    sharedPrefs.edit()
                        .putString("logged_in_user_id", demoUserId)
                        .putString("user_name", "Ankit Kumar")
                        .putString("user_email", email)
                        .putString("password_storage_$lowercaseEmail", password)
                        .putString("userId_storage_$lowercaseEmail", demoUserId)
                        .apply()
                    _currentUser.value = demoUser
                    launch(Dispatchers.Main) { onSuccess() }
                    return@launch
                }

                if (registeredPw != password) {
                    launch(Dispatchers.Main) { onError("Invalid password") }
                    return@launch
                }

                val dbUser = dao.getUser(storedUserId).firstOrNull() ?: User(
                    userId = storedUserId,
                    name = sharedPrefs.getString("user_name", "User") ?: "User",
                    email = email,
                    isOnboarded = _isOnboarded.value
                )

                sharedPrefs.edit().putString("logged_in_user_id", storedUserId).apply()
                _currentUser.value = dbUser
                launch(Dispatchers.Main) { onSuccess() }
            } catch (e: Exception) {
                Log.e("ExpenseRepository", "Login error", e)
                launch(Dispatchers.Main) { onError(e.message ?: "Authentication failed") }
            }
        }
    }

    fun forgotPassword(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (email.isBlank()) {
            onError("Please enter your email address")
            return
        }
        val lowercaseEmail = email.lowercase()
        val userId = sharedPrefs.getString("userId_storage_$lowercaseEmail", null)
        if (userId == null) {
            onError("No account found with this email")
            return
        }
        // Simulated password recovery email
        onSuccess()
    }

    fun logout() {
        sharedPrefs.edit().remove("logged_in_user_id").apply()
        _currentUser.value = null
    }

    fun completeOnboarding() {
        _isOnboarded.value = true
        sharedPrefs.edit().putBoolean("onboarded", true).apply()
        val user = _currentUser.value
        if (user != null) {
            val updatedUser = user.copy(isOnboarded = true)
            _currentUser.value = updatedUser
            repositoryScope.launch {
                dao.insertUser(updatedUser)
            }
        }
    }

    // TRANSACTION APIs
    fun getAllTransactions(): Flow<List<Transaction>> {
        val uId = _currentUser.value?.userId ?: "demo_user_123"
        return dao.getAllTransactions(uId)
    }

    fun getPendingReviewTransactions(): Flow<List<Transaction>> {
        val uId = _currentUser.value?.userId ?: "demo_user_123"
        return dao.getTransactionsByReview(uId, false)
    }

    fun getTransactionById(transactionId: String): Flow<Transaction?> {
        return dao.getTransactionById(transactionId)
    }

    suspend fun getTransactionsSync(userId: String): List<Transaction> {
        return dao.getTransactionsSync(userId)
    }

    suspend fun findRecentTransactions(userId: String, source: String, amount: Double, minTimestamp: Long, maxTimestamp: Long): List<Transaction> {
        return dao.findRecentTransactions(userId, source, amount, minTimestamp, maxTimestamp)
    }

    suspend fun saveTransaction(tx: Transaction) {
        dao.insertTransaction(tx)
        if (tx.transactionType == "SENT") {
            checkBudgetsForCategory(tx.category)
        }
    }

    suspend fun deleteTransaction(transactionId: String) {
        dao.deleteTransaction(transactionId)
    }

    suspend fun createManualTransaction(amount: Double, merchant: String, type: String, category: String, description: String) {
        val uId = _currentUser.value?.userId ?: "demo_user_123"
        val tx = Transaction(
            transactionId = UUID.randomUUID().toString(),
            amount = amount,
            merchantName = merchant,
            transactionType = type,
            category = category,
            description = description,
            timestamp = System.currentTimeMillis(),
            userId = uId,
            createdAt = System.currentTimeMillis(),
            isReviewed = true
        )
        saveTransaction(tx)
    }

    // BUDGET APIs
    fun getAllBudgets(): Flow<List<Budget>> = dao.getAllBudgets()

    fun getAllAlerts(): Flow<List<BudgetAlert>> = dao.getAllAlerts()

    suspend fun setBudgetLimit(category: String, limitAmount: Double) {
        dao.insertBudget(Budget(category = category, limitAmount = limitAmount))
        checkBudgetsForCategory(category)
    }

    suspend fun deleteBudgetLimit(category: String) {
        dao.deleteBudget(category)
    }

    suspend fun clearAlertHistory() {
        dao.clearAllAlerts()
    }

    // INTERNAL BUDGET CRITICAL CHECKING
    private suspend fun checkBudgetsForCategory(categoryName: String) {
        val budget = dao.getBudgetSync(categoryName) ?: return
        val uId = _currentUser.value?.userId ?: "demo_user_123"
        val allTx = dao.getTransactionsSync(uId)

        // Sum current spent for this category (Only count "SENT" transactions of this category)
        val spent = allTx.filter {
            it.category.equals(categoryName, ignoreCase = true) && it.transactionType == "SENT"
        }.sumOf { it.amount }

        if (spent <= 0 || budget.limitAmount <= 0) return

        val percent = ((spent / budget.limitAmount) * 100).toInt()

        if (percent >= 100) {
            // Trigger 100% Alert
            val alertMessage = "Alert: You have reached 100% or more of your budget for $categoryName! Spent: ₹${spent} / Limit: ₹${budget.limitAmount}"
            dao.insertAlert(BudgetAlert(
                category = categoryName,
                limitAmount = budget.limitAmount,
                spentAmount = spent,
                percentageReached = 100,
                message = alertMessage,
                timestamp = System.currentTimeMillis()
            ))
        } else if (percent >= 80) {
            // Trigger 80% Alert
            val alertMessage = "Warning: You have reached ${percent}% of your budget for $categoryName. Spent: ₹${spent} / Limit: ₹${budget.limitAmount}"
            dao.insertAlert(BudgetAlert(
                category = categoryName,
                limitAmount = budget.limitAmount,
                spentAmount = spent,
                percentageReached = 80,
                message = alertMessage,
                timestamp = System.currentTimeMillis()
            ))
        }
    }
}
