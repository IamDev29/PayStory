package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.ExpenseRepository

class ExpenseApplication : Application() {
    
    lateinit var database: AppDatabase
        private set
        
    lateinit var repository: ExpenseRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        repository = ExpenseRepository(this, database.transactionDao())
    }
}
