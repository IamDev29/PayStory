package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val userId: String,
    val name: String,
    val email: String,
    val isOnboarded: Boolean = false
)

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey val transactionId: String,
    val amount: Double,
    val merchantName: String,
    val transactionType: String, // "SENT" or "RECEIVED"
    val category: String, // String value of Category enum
    val description: String,
    val timestamp: Long,
    val userId: String,
    val createdAt: Long,
    val isReviewed: Boolean = false,
    val source: String = "notification", // "sms" or "notification"
    val referenceNumber: String? = null
)

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey val category: String, // Key is the Category name (e.g. FOOD, GROCERY)
    val limitAmount: Double
)

@Entity(tableName = "budget_alerts")
data class BudgetAlert(
    @PrimaryKey(autoGenerate = true) val alertId: Int = 0,
    val category: String,
    val limitAmount: Double,
    val spentAmount: Double,
    val percentageReached: Int, // 80 or 100
    val message: String,
    val timestamp: Long
)
