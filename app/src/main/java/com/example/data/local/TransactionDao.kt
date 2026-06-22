package com.example.data.local

import androidx.room.*
import com.example.data.models.Budget
import com.example.data.models.BudgetAlert
import com.example.data.models.Transaction
import com.example.data.models.User
import com.example.data.models.MerchantMapping
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    // User Operations
    @Query("SELECT * FROM users WHERE userId = :userId")
    fun getUser(userId: String): Flow<User?>

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getAnyUserSync(): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    // Transaction Operations
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllTransactions(userId: String): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND isReviewed = :isReviewed ORDER BY timestamp DESC")
    fun getTransactionsByReview(userId: String, isReviewed: Boolean): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE userId = :userId")
    suspend fun getTransactionsSync(userId: String): List<Transaction>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND source = :source AND amount = :amount AND timestamp >= :minTimestamp AND timestamp <= :maxTimestamp")
    suspend fun findRecentTransactions(userId: String, source: String, amount: Double, minTimestamp: Long, maxTimestamp: Long): List<Transaction>

    @Query("SELECT * FROM transactions WHERE transactionId = :transactionId")
    fun getTransactionById(transactionId: String): Flow<Transaction?>

    @Query("SELECT * FROM transactions WHERE transactionId = :transactionId")
    suspend fun getTransactionByIdSync(transactionId: String): Transaction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE transactionId = :transactionId")
    suspend fun deleteTransaction(transactionId: String)

    // Budget Operations
    @Query("SELECT * FROM budgets")
    fun getAllBudgets(): Flow<List<Budget>>

    @Query("SELECT * FROM budgets")
    suspend fun getAllBudgetsSync(): List<Budget>

    @Query("SELECT * FROM budgets WHERE category = :category")
    suspend fun getBudgetSync(category: String): Budget?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget)

    @Query("DELETE FROM budgets WHERE category = :category")
    suspend fun deleteBudget(category: String)

    // Alert Operations
    @Query("SELECT * FROM budget_alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<BudgetAlert>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: BudgetAlert)

    @Query("DELETE FROM budget_alerts")
    suspend fun clearAllAlerts()

    // Merchant Mapping Operations
    @Query("SELECT * FROM merchant_mappings ORDER BY merchantName ASC")
    fun getAllMerchantMappings(): Flow<List<MerchantMapping>>

    @Query("SELECT * FROM merchant_mappings ORDER BY merchantName ASC")
    suspend fun getAllMerchantMappingsSync(): List<MerchantMapping>

    @Query("SELECT * FROM merchant_mappings WHERE merchantName = :merchantName")
    suspend fun getMerchantMappingSync(merchantName: String): MerchantMapping?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMerchantMapping(mapping: MerchantMapping)

    @Query("DELETE FROM merchant_mappings WHERE merchantName = :merchantName")
    suspend fun deleteMerchantMapping(merchantName: String)
}
