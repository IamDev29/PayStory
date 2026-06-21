package com.example.services

import android.app.Notification
import android.content.pm.ServiceInfo
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.ExpenseApplication
import com.example.data.models.Category
import com.example.data.models.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.regex.Pattern

class TransactionNotificationListener : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val UPI_PACKAGES = setOf(
        "com.phonepe.app",
        "com.google.android.apps.nbu.paisa.user",
        "net.one97.paytm",
        "in.org.npci.upiapp",
        "com.dreamplug.androidapp",          // CRED
        "in.amazon.mShop.android.shopping"   // Amazon Pay
    )

    private val TEST_PACKAGES = setOf(
        "example",
        "aistudio",
        "expensememory",
        "paystory"
    )

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i("NotificationListener", "[LOG] Notification listener service connected")
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    1001,
                    NotificationHelper.buildForegroundNotification(this),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(1001, NotificationHelper.buildForegroundNotification(this))
            }
            Log.i("NotificationListener", "[LOG] Notification listener successfully promoted to foreground service")
        } catch (e: Exception) {
            Log.e("NotificationListener", "Failed to start foreground service for listener", e)
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.i("NotificationListener", "[LOG] Notification listener service disconnected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return

        val pkgName = sbn.packageName ?: return
        val pkgNameLower = pkgName.lowercase()

        val isTargetApp = UPI_PACKAGES.contains(pkgNameLower) ||
                TEST_PACKAGES.any { pkgNameLower.contains(it) }

        if (!isTargetApp) return

        val extras = sbn.notification.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString() ?: ""
        val summaryText = extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString() ?: ""

        val combinedText = listOf(bigText, text, subText, summaryText, title)
            .filter { it.isNotBlank() }
            .joinToString(" ")

        Log.i("NotificationListener", "[LOG] Notification received: packagename=$pkgName, title='$title', text='$text', bigText='$bigText', subText='$subText', summaryText='$summaryText'")

        parseAndSaveTransaction(pkgName, title, combinedText)
    }

    private fun parseAndSaveTransaction(pkgName: String, title: String, combinedText: String) {
        try {
            val lowercaseText = combinedText.lowercase()

            // Detect incoming money transactions (Income)
            val receivedKeywords = listOf(
                "received", "credited", "added", "refunded", "incoming", 
                "cashback", "deposited", "got", "paid to you", "received from"
            )
            
            var isReceived = false
            for (keyword in receivedKeywords) {
                if (lowercaseText.contains(keyword)) {
                    isReceived = true
                    break
                }
            }

            if (!isReceived) {
                Log.d("NotificationListener", "[LOG] Notification parsed: Not an incoming payment keyword index match. Ignoring.")
                return
            }

            // Parse amount
            val amount = findAmountInText(combinedText)
            if (amount == null) {
                Log.d("NotificationListener", "[LOG] Notification parsed: No amount found in combined notification text.")
                return
            }

            // Extract sender name
            val senderName = extractSenderName(title, combinedText, pkgName)

            Log.i("NotificationListener", "[LOG] Notification parsed successfully: amount=₹$amount, sender=$senderName, isIncome=true")

            val app = applicationContext as? ExpenseApplication ?: return
            val repo = app.repository
            
            val activeUserId = repo.currentUser.value?.userId ?: "demo_user_123"
            val timestamp = System.currentTimeMillis()

            serviceScope.launch {
                try {
                    // Check duplicate: Use recent transactions in Room within 30 seconds range
                    val minTime = timestamp - 30000
                    val maxTime = timestamp + 10000
                    val recentTx = repo.findRecentTransactions(activeUserId, "notification", amount, minTime, maxTime)

                    val isDuplicate = recentTx.any { tx ->
                        tx.merchantName.equals(senderName, ignoreCase = true) || 
                        tx.merchantName.contains(senderName, ignoreCase = true) ||
                        senderName.contains(tx.merchantName, ignoreCase = true)
                    }

                    if (isDuplicate) {
                        Log.i("NotificationListener", "[LOG] Duplicate skipped: Notification transaction with amount ₹$amount, sender $senderName already exists in recent window")
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
                    Log.e("NotificationListener", "Error saving received transaction in coroutine", e)
                }
            }
        } catch (e: Exception) {
            Log.e("NotificationListener", "Exception in parseAndSaveTransaction", e)
        }
    }

    private fun findAmountInText(text: String): Double? {
        try {
            // 1. Primary standard regex: ₹/Rs/INR followed by numbers
            val primaryRegex = "(?:₹|Rs\\.?|INR)\\s*([0-9,]+(?:\\.[0-9]{1,2})?)"
            val primaryPattern = Pattern.compile(primaryRegex, Pattern.CASE_INSENSITIVE)
            val primaryMatcher = primaryPattern.matcher(text)
            if (primaryMatcher.find()) {
                val amountStr = primaryMatcher.group(1)?.replace(",", "")
                val parsed = amountStr?.toDoubleOrNull()
                if (parsed != null) return parsed
            }

            // 2. Secondary regex: matches bare numbers after typical income verbs
            val bareNumberRegex = "(?:received|credited|added|refunded|refund|cashback|deposited|got|paid)\\s+(?:(?:of|with|rs\\.?|₹|inr)?\\s*)*([0-9,]+(?:\\.[0-9]{1,2})?)"
            val barePattern = Pattern.compile(bareNumberRegex, Pattern.CASE_INSENSITIVE)
            val bareMatcher = barePattern.matcher(text)
            if (bareMatcher.find()) {
                val amountStr = bareMatcher.group(1)?.replace(",", "")
                val parsed = amountStr?.toDoubleOrNull()
                if (parsed != null) return parsed
            }

            // 3. Fallback: match any number that looks like [0-9,]+(\.[0-9]{1,2})? after keywords
            val keywords = listOf("received", "credited", "added", "refunded", "deposited")
            val lowercase = text.lowercase()
            for (kw in keywords) {
                val idx = lowercase.indexOf(kw)
                if (idx != -1) {
                    val trailingText = text.substring(idx + kw.length)
                    val trailingPattern = Pattern.compile("([0-9,]+(?:\\.[0-9]{1,2})?)")
                    val trailingMatcher = trailingPattern.matcher(trailingText)
                    if (trailingMatcher.find()) {
                        val amountStr = trailingMatcher.group(1)?.replace(",", "")
                        val parsed = amountStr?.toDoubleOrNull()
                        if (parsed != null) return parsed
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("NotificationListener", "Exception passing amount regex", e)
        }
        return null
    }

    private fun extractSenderName(title: String, combinedText: String, pkgName: String): String {
        val lowerText = combinedText.lowercase()

        // Try finding "from <name>"
        val fromIndex = lowerText.indexOf("from ")
        if (fromIndex != -1) {
            var candidate = combinedText.substring(fromIndex + 5).trim()
            // Clean up trailing details if any (e.g. "from Rahul. Ref: ...")
            val stopWords = listOf(".", " ref", " transaction", " id", " utr", " for", " via", " at")
            for (stopWord in stopWords) {
                val stopIndex = candidate.lowercase().indexOf(stopWord)
                if (stopIndex != -1) {
                    candidate = candidate.substring(0, stopIndex).trim()
                }
            }
            if (candidate.isNotEmpty()) {
                return candidate.split(" ").take(3).joinToString(" ").trim()
            }
        }

        // Try finding "sent by <name>"
        val sentIndex = lowerText.indexOf("sent by ")
        if (sentIndex != -1) {
            val candidate = combinedText.substring(sentIndex + 8).trim()
            if (candidate.isNotEmpty()) {
                return candidate.split(" ").take(3).joinToString(" ").trim()
            }
        }

        // Try finding "credited by <name>"
        val creditedIndex = lowerText.indexOf("credited by ")
        if (creditedIndex != -1) {
            val candidate = combinedText.substring(creditedIndex + 12).trim()
            if (candidate.isNotEmpty()) {
                return candidate.split(" ").take(3).joinToString(" ").trim()
            }
        }

        // Fallback title checking
        if (title.isNotEmpty() && !title.equals("Google Pay", true) && 
            !title.equals("PhonePe", true) && !title.equals("Paytm", true) && 
            !title.equals("BHIM", true)) {
            return title
        }

        return when {
            pkgName.contains("phonepe", true) -> "PhonePe Contact"
            pkgName.contains("paisa", true) -> "Google Pay Contact"
            pkgName.contains("paytm", true) -> "Paytm Contact"
            else -> "Received UPI"
        }
    }
}
