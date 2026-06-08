package com.example.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.util.Log
import com.example.ExpenseApplication
import com.example.data.models.Category
import com.example.data.models.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.regex.Pattern

class SmsReceiver : BroadcastReceiver() {

    private val receiverScope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "android.provider.Telephony.SMS_RECEIVED") return

        val extras = intent.extras ?: return
        val pdus = extras.get("pdus") as? Array<*> ?: return
        val format = extras.getString("format")

        val fullBody = StringBuilder()
        var senderAddress = ""

        try {
            for (pdu in pdus) {
                val message = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    SmsMessage.createFromPdu(pdu as ByteArray, format)
                } else {
                    @Suppress("DEPRECATION")
                    SmsMessage.createFromPdu(pdu as ByteArray)
                }
                fullBody.append(message.messageBody)
                senderAddress = message.originatingAddress ?: ""
            }

            val text = fullBody.toString()
            Log.i("SmsReceiver", "[LOG] SMS received: address=$senderAddress, body='$text'")

            parseAndSaveSmsTransaction(context, senderAddress, text)
        } catch (e: Exception) {
            Log.e("SmsReceiver", "Error receiving or parsing SMS", e)
        }
    }

    private fun parseAndSaveSmsTransaction(context: Context, sender: String, text: String) {
        val lowercaseText = text.lowercase()
        val isExpense = lowercaseText.contains("debited") ||
                lowercaseText.contains("spent") ||
                lowercaseText.contains("transferred") ||
                lowercaseText.contains("successful for") ||
                lowercaseText.contains("paid") ||
                lowercaseText.contains("sent") ||
                lowercaseText.contains("withdrawn")

        if (!isExpense) {
            Log.d("SmsReceiver", "[LOG] SMS parsed: SMS content is not an expense. Ignoring.")
            return
        }

        // Regex supporting: Rs. 250, Rs 500, Rs. 1,000, INR 12.50, and unspaced values
        val amountRegex = "(?:Rs\\.?|INR|₹)\\s*([0-9,]+(?:\\.[0-9]{2})?)"
        val amountPattern = Pattern.compile(amountRegex, Pattern.CASE_INSENSITIVE)
        val amountMatcher = amountPattern.matcher(text)

        if (!amountMatcher.find()) {
            Log.d("SmsReceiver", "[LOG] SMS parsed: No amount found in text.")
            return
        }

        val amountStr = amountMatcher.group(1)?.replace(",", "") ?: return
        val amount = amountStr.toDoubleOrNull() ?: return

        // Extract Reference / UTR Number
        val refRegex = "(?i)(?:ref(?:erence)?\\s*(?:no)?|utr|id|txn|transaction\\s*id)\\s*(?::|\\s|-)*\\s*([0-9a-zA-Z]+)"
        val refPattern = Pattern.compile(refRegex)
        val refMatcher = refPattern.matcher(text)
        var referenceNumber: String? = null

        if (refMatcher.find()) {
            referenceNumber = refMatcher.group(1)
        } else {
            val digitPattern = Pattern.compile("\\b\\d{8,12}\\b")
            val digitMatcher = digitPattern.matcher(text)
            if (digitMatcher.find()) {
                referenceNumber = digitMatcher.group()
            }
        }

        if (referenceNumber == null) {
            referenceNumber = "SMS-" + text.hashCode().toString()
        }

        // Extract Bank Name
        var bankName = "Bank Account"
        val senderUpper = sender.uppercase()
        val matchedBank = when {
            senderUpper.contains("HDFC") -> "HDFC Bank"
            senderUpper.contains("SBI") -> "SBI Bank"
            senderUpper.contains("ICICI") -> "ICICI Bank"
            senderUpper.contains("AXIS") -> "AXIS Bank"
            senderUpper.contains("KOTAK") -> "Kotak Bank"
            senderUpper.contains("PNB") -> "PNB Bank"
            senderUpper.contains("BOB") -> "BOB Bank"
            senderUpper.contains("UNION") -> "Union Bank"
            senderUpper.contains("CANARA") -> "Canara Bank"
            senderUpper.contains("YESB") -> "Yes Bank"
            senderUpper.contains("PAYTM") -> "Paytm Bank"
            else -> {
                val bodyUpper = text.uppercase()
                when {
                    bodyUpper.contains("HDFC") -> "HDFC Bank"
                    bodyUpper.contains("SBI") -> "SBI Bank"
                    bodyUpper.contains("ICICI") -> "ICICI Bank"
                    bodyUpper.contains("AXIS") -> "AXIS Bank"
                    bodyUpper.contains("KOTAK") -> "Kotak Bank"
                    bodyUpper.contains("PNB") -> "PNB Bank"
                    bodyUpper.contains("BOB") -> "BOB Bank"
                    bodyUpper.contains("UNION") -> "Union Bank"
                    bodyUpper.contains("CANARA") -> "Canara Bank"
                    bodyUpper.contains("YES BANK") -> "Yes Bank"
                    bodyUpper.contains("PAYTM BANK") -> "Paytm Bank"
                    else -> null
                }
            }
        }

        if (matchedBank != null) {
            bankName = matchedBank
        } else {
            if (sender.contains("-")) {
                val parts = sender.split("-")
                if (parts.size > 1 && parts[1].length >= 3) {
                    bankName = parts[1].trim()
                }
            }
        }

        Log.i("SmsReceiver", "[LOG] SMS parsed: amount=₹$amount, bank=$bankName, ref=$referenceNumber")

        val app = context.applicationContext as? ExpenseApplication ?: return
        val repo = app.repository
        val activeUserId = repo.currentUser.value?.userId ?: "demo_user_123"
        val timestamp = System.currentTimeMillis()

        receiverScope.launch {
            try {
                val allTx = repo.getTransactionsSync(activeUserId)
                val isDuplicate = allTx.any { tx ->
                    tx.source == "sms" &&
                    tx.amount == amount &&
                    (tx.referenceNumber == referenceNumber || kotlin.math.abs(tx.timestamp - timestamp) < 10000)
                }

                if (isDuplicate) {
                    Log.i("SmsReceiver", "[LOG] Duplicate skipped: SMS transaction with amount ₹$amount, ref $referenceNumber already exists")
                    return@launch
                }

                val transaction = Transaction(
                    transactionId = UUID.randomUUID().toString(),
                    amount = amount,
                    merchantName = bankName,
                    transactionType = "SENT",
                    category = Category.UNCATEGORIZED.name,
                    description = "Auto-detected expense via Bank SMS: $text",
                    timestamp = timestamp,
                    userId = activeUserId,
                    createdAt = timestamp,
                    isReviewed = false, // Not reviewed yet
                    source = "sms",
                    referenceNumber = referenceNumber
                )

                repo.saveTransaction(transaction)
                Log.i("SmsReceiver", "[LOG] Transaction created: amount=₹$amount, source=sms, ref=$referenceNumber")
                NotificationHelper.sendTransactionReminder(context, amount, "sms", bankName)
            } catch (e: Exception) {
                Log.e("SmsReceiver", "Error saving transaction", e)
            }
        }
    }
}
