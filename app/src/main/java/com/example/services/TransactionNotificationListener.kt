package com.example.services

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.ExpenseApplication
import com.example.data.models.Category
import com.example.data.models.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.regex.Pattern

class TransactionNotificationListener : NotificationListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return

        val pkgName = sbn.packageName ?: return
        val isTargetApp = pkgName.contains("phonepe", ignoreCase = true) ||
                pkgName.contains("paisa", ignoreCase = true) || // GPay
                pkgName.contains("paytm", ignoreCase = true) ||
                pkgName.contains("upiapp", ignoreCase = true) ||
                pkgName.contains("example", ignoreCase = true) || // for testing/system simulation
                pkgName.contains("aistudio", ignoreCase = true) ||
                pkgName.contains("expensememory", ignoreCase = true) ||
                pkgName.contains("paystory", ignoreCase = true)

        if (!isTargetApp) return

        val extras = sbn.notification.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        Log.i("NotificationListener", "[LOG] Notification received: packagename=$pkgName, title='$title', text='$text'")

        parseAndSaveTransaction(pkgName, title, text)
    }

    private fun parseAndSaveTransaction(pkgName: String, title: String, text: String) {
        try {
            val fullText = "$title $text"
            val lowercaseText = fullText.lowercase()

            // ONLY detect incoming money transactions (Income)
            val receivedKeywords = listOf("received", "credited", "added", "refunded", "incoming", "cashback", "deposited")
            var isReceived = false
            for (keyword in receivedKeywords) {
                if (lowercaseText.contains(keyword)) {
                    isReceived = true
                    break
                }
            }

            if (!isReceived) {
                Log.d("NotificationListener", "[LOG] Notification parsed: Not an incoming payment. Ignoring.")
                return
            }

            // Parse amount
            // Regex supporting: ₹ 150, Rs 500, Rs. 1,000, INR 12.50, and unspaced values
            val amountRegex = "(?:₹|Rs\\.?|INR)\\s*([0-9,]+(?:\\.[0-9]{2})?)"
            val pattern = Pattern.compile(amountRegex, Pattern.CASE_INSENSITIVE)
            val matcher = pattern.matcher(fullText)

            if (!matcher.find()) {
                Log.d("NotificationListener", "[LOG] Notification parsed: No amount found in incoming notification.")
                return
            }

            val amountStr = matcher.group(1)?.replace(",", "") ?: return
            val amount = amountStr.toDoubleOrNull() ?: return

            // Extract sender name safely by referencing index in lowercase text-only
            var senderName = "UPI Sender"
            val lowerTextOnly = text.lowercase()
            val fromIndex = lowerTextOnly.indexOf("from ")
            
            if (fromIndex != -1) {
                val candidate = text.substring(fromIndex + 5).trim()
                if (candidate.isNotEmpty()) {
                    senderName = candidate.split(" ").take(2).joinToString(" ")
                }
            } else {
                // General fallback: if title is not app name, title is likely the sender name!
                if (title.isNotEmpty() && !title.equals("Google Pay", true) && 
                    !title.equals("PhonePe", true) && !title.equals("Paytm", true) && 
                    !title.equals("BHIM", true)) {
                    senderName = title
                } else {
                    senderName = when {
                        pkgName.contains("phonepe", true) -> "PhonePe Contact"
                        pkgName.contains("paisa", true) -> "Google Pay Contact"
                        pkgName.contains("paytm", true) -> "Paytm Contact"
                        else -> "Received UPI"
                    }
                }
            }

            Log.i("NotificationListener", "[LOG] Notification parsed: amount=₹$amount, sender=$senderName, isIncome=true")

            val app = applicationContext as? ExpenseApplication ?: return
            val repo = app.repository
            
            val activeUserId = repo.currentUser.value?.userId ?: "demo_user_123"
            val timestamp = System.currentTimeMillis()

            serviceScope.launch {
                try {
                    // Check duplicate: Use Amount, Sender, Timestamp within 10 seconds (10,000 ms)
                    val allTx = repo.getTransactionsSync(activeUserId)
                    val isDuplicate = allTx.any { tx ->
                        tx.source == "notification" &&
                        tx.amount == amount &&
                        tx.merchantName.equals(senderName, ignoreCase = true) &&
                        kotlin.math.abs(tx.timestamp - timestamp) < 10000
                    }

                    if (isDuplicate) {
                        Log.i("NotificationListener", "[LOG] Duplicate skipped: Notification transaction with amount ₹$amount, sender $senderName already exists")
                        return@launch
                    }

                    val transaction = Transaction(
                        transactionId = UUID.randomUUID().toString(),
                        amount = amount,
                        merchantName = senderName.trim(),
                        transactionType = "RECEIVED",
                        category = Category.UNCATEGORIZED.name,
                        description = "Auto-detected income via notification from $senderName",
                        timestamp = timestamp,
                        userId = activeUserId,
                        createdAt = timestamp,
                        isReviewed = false, // Shows the prompt sheet immediately!
                        source = "notification",
                        referenceNumber = null
                    )

                    repo.saveTransaction(transaction)
                    Log.i("NotificationListener", "[LOG] Transaction created: amount=₹$amount, source=notification")
                    NotificationHelper.sendTransactionReminder(applicationContext, amount, "notification", senderName)
                } catch (e: Exception) {
                    Log.e("NotificationListener", "Error saving received transaction", e)
                }
            }
        } catch (e: Exception) {
            Log.e("NotificationListener", "Exception in parseAndSaveTransaction", e)
        }
    }
}
