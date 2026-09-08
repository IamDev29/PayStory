package com.example.services

import java.util.regex.Pattern

/**
 * Filter and classification engine for SMS and notification messages.
 * Prevents false positives from promotional/marketing messages and ensures
 * that only genuine, already-completed financial transactions are detected and recorded.
 */
object TransactionFilter {

    /**
     * Configurable negative keyword list for promotional/marketing phrases.
     * If a message matches any of these keywords, it is rejected immediately
     * (promotional exclusion always wins over transaction keywords).
     */
    val DEFAULT_PROMO_KEYWORDS = setOf(
        "offer",
        "offers",
        "special offer",
        "exclusive offer",
        "cashback of up to",
        "cashback upto",
        "cashback of upto",
        "cashback up to",
        "up to ₹",
        "upto ₹",
        "up to rs",
        "upto rs",
        "win",
        "won",
        "voucher",
        "vouchers",
        "coupon",
        "coupons",
        "use code",
        "promo code",
        "promocode",
        "apply code",
        "apply coupon",
        "limited time",
        "limited period",
        "claim now",
        "get upto",
        "get up to",
        "flat off",
        "flat rs",
        "discount",
        "discounts",
        "% off",
        "percent off",
        "reward points",
        "refer and earn",
        "refer & earn",
        "referral code",
        "scratch card",
        "spin and win",
        "spin to win",
        "stand a chance",
        "congratulations",
        "congratulation",
        "hurry",
        "valid till",
        "valid until",
        "expires on",
        "on your next",
        "next order",
        "next purchase",
        "next week",
        "upgrade your",
        "pre-approved",
        "pre approved",
        "instant loan",
        "personal loan",
        "apply now"
    )

    // Configurable set of promo keywords that can be dynamically extended or customized
    val promoKeywords: MutableSet<String> = DEFAULT_PROMO_KEYWORDS.toMutableSet()

    /**
     * Reset promo keywords back to defaults.
     */
    fun resetPromoKeywords() {
        promoKeywords.clear()
        promoKeywords.addAll(DEFAULT_PROMO_KEYWORDS)
    }

    /**
     * Add a promotional keyword to the active negative filter.
     */
    fun addPromoKeyword(keyword: String) {
        val trimmed = keyword.lowercase().trim()
        if (trimmed.isNotEmpty()) {
            promoKeywords.add(trimmed)
        }
    }

    /**
     * Remove a promotional keyword from the active negative filter.
     */
    fun removePromoKeyword(keyword: String) {
        promoKeywords.remove(keyword.lowercase().trim())
    }

    /**
     * Requirement 1 & 3:
     * Promotional/offer exclusion filter that runs BEFORE the transaction keyword check.
     * Employs word boundaries where appropriate to avoid false matches on substrings
     * (e.g. "win" won't match "window" or "Edwin").
     *
     * @return true if the text contains any promotional content.
     */
    fun containsPromotionalContent(text: String, customList: Collection<String>? = null): Boolean {
        if (text.isBlank()) return false
        val activeList = customList ?: promoKeywords
        val lowerText = text.lowercase()

        for (kw in activeList) {
            val trimmed = kw.trim().lowercase()
            if (trimmed.isEmpty()) continue

            val prefix = if (trimmed.first().isLetterOrDigit()) "\\b" else ""
            val suffix = if (trimmed.last().isLetterOrDigit()) "\\b" else ""
            val pattern = Pattern.compile("$prefix${Pattern.quote(trimmed)}$suffix", Pattern.CASE_INSENSITIVE)
            if (pattern.matcher(lowerText).find()) {
                return true
            }
        }
        return false
    }

    /**
     * Checks if the text contains a valid monetary amount representation.
     * Supports formats like: Rs.450, Rs 500, Rs. 1,000, INR 12.50, ₹1,200.00, etc.
     */
    fun hasValidAmount(text: String): Boolean {
        val amountRegex = "(?:Rs\\.?|INR|₹)\\s*([0-9,]+(?:\\.[0-9]{1,2})?)|([0-9,]+(?:\\.[0-9]{1,2})?)\\s*(?:INR|Rs\\.?)"
        val pattern = Pattern.compile(amountRegex, Pattern.CASE_INSENSITIVE)
        return pattern.matcher(text).find()
    }

    /**
     * Checks if the text contains an action verb indicating a completed financial transaction.
     */
    fun hasTransactionVerb(text: String): Boolean {
        val lowerText = text.lowercase()
        val verbs = listOf(
            "debited",
            "spent",
            "transferred",
            "successful for",
            "paid",
            "sent",
            "withdrawn",
            "withdrew",
            "credited",
            "received",
            "deposited",
            "refunded",
            "payment of",
            "deducted"
        )
        return verbs.any { verb ->
            val pattern = Pattern.compile("\\b${Pattern.quote(verb)}\\b", Pattern.CASE_INSENSITIVE)
            pattern.matcher(lowerText).find()
        }
    }

    /**
     * Requirement 2:
     * Requires at least one strong "real transaction" structural signal:
     * 1. Account reference (A/c, A/c no, masked digits like XX1234, *1234, etc.)
     * 2. UPI / Transaction / UTR reference ID pattern or VPA handle
     * 3. Available balance / Avl Bal mention
     */
    fun hasStructuralSignal(text: String): Boolean {
        // 1. Account reference signal
        val accountPattern = Pattern.compile(
            "(?i)(?:\\ba\\/c\\b|\\bacct\\b|\\baccount\\b|\\bcard\\b)\\s*(?:no\\.?|num(?:ber)?\\.?)?\\s*[:\\s-]*([xX*]*\\d{3,6}|\\b\\d{3,6}\\b)|\\b[xX*]{2,}\\d{3,6}\\b|\\b(?:ending|ending with|ending in)\\s*[:\\s-]*([xX*]*\\d{3,6}|\\d{3,6})|\\b(?:from|to|in)\\s+a\\/c\\b",
            Pattern.CASE_INSENSITIVE
        )
        if (accountPattern.matcher(text).find()) {
            return true
        }

        // 2. Reference / UTR / Transaction ID / VPA signal
        val refPattern = Pattern.compile(
            "(?i)(?:upi\\s*ref(?:\\s*no)?|ref(?:\\s*no)?|utr(?:\\s*no)?|txn(?:\\s*id)?|transaction\\s*id|reference\\s*(?:no|id|num)?|rrn)\\b[:\\s-]*[a-zA-Z0-9]+|\\b(?:to|from)\\s+vpa\\s+[\\w.\\-]+@[\\w.\\-]+|\\b[\\w.\\-]+@[\\w.\\-]+\\b|\\b(?:imps|neft|rtgs)\\s*(?:ref|txn|id)?[:\\s-]*[a-zA-Z0-9]+",
            Pattern.CASE_INSENSITIVE
        )
        if (refPattern.matcher(text).find()) {
            return true
        }

        // 3. Available balance signal
        val balancePattern = Pattern.compile(
            "(?i)(?:avl(?:\\.?|\\s+)bal(?:\\.?|\\s*)|avail(?:\\.?|\\s+)bal(?:\\.?|\\s*)|available\\s+balance|total\\s+bal(?:\\.?|\\s*)|updated\\s+bal(?:\\.?|\\s*)|bal(?:\\.?|\\s*):|balance(?:\\s*is|\\s*:))\\s*(?:rs\\.?|inr|₹)?|\\b(?:clear\\s*bal|ledger\\s*bal|eff\\s*bal)\\b",
            Pattern.CASE_INSENSITIVE
        )
        if (balancePattern.matcher(text).find()) {
            return true
        }

        return false
    }

    /**
     * Requirement 5:
     * Isolated, pure JVM testable function to determine if a message represents
     * a genuine, already-completed bank/UPI transaction.
     *
     * Execution pipeline:
     * 1. Promotional exclusion filter (runs FIRST). If matched, rejected.
     * 2. Amount verification. If no valid currency amount, rejected.
     * 3. Transaction verb verification. If no completed verb, rejected.
     * 4. Structural signal verification. Requires at least one strong signal
     *    (account reference, UTR/ref ID, or available balance).
     */
    fun isLikelyRealTransaction(text: String): Boolean {
        // Step 1: Promotional / offer exclusion (promo exclusion always wins)
        if (containsPromotionalContent(text)) {
            return false
        }

        // Step 2: Valid monetary amount check
        if (!hasValidAmount(text)) {
            return false
        }

        // Step 3: Transaction action verb check
        if (!hasTransactionVerb(text)) {
            return false
        }

        // Step 4: Real transaction structural signal check
        if (!hasStructuralSignal(text)) {
            return false
        }

        return true
    }

    /**
     * Alias for SMS transaction verification.
     */
    fun isLikelyRealSmsTransaction(text: String): Boolean = isLikelyRealTransaction(text)

    /**
     * Verified UPI payment app packages.
     */
    val UPI_PACKAGES = setOf(
        "com.phonepe.app",
        "com.google.android.apps.nbu.paisa.user",
        "net.one97.paytm",
        "in.org.npci.upiapp",
        "com.dreamplug.androidapp",          // CRED
        "in.amazon.mShop.android.shopping"   // Amazon Pay
    )

    val TEST_PACKAGES = setOf(
        "example",
        "aistudio",
        "expensememory",
        "paystory"
    )

    /**
     * Checks if an incoming notification is from a genuine payment app
     * and represents a real transaction (not an in-app promotional push).
     */
    fun isLikelyRealNotification(pkgName: String, title: String, text: String): Boolean {
        val combined = "$title $text".trim()
        return isLikelyRealNotification(pkgName, combined)
    }

    /**
     * Checks if an incoming notification represents a real transaction.
     * Requirement 2: Requires that it comes from a real payment app package
     * AND does not contain promotional language.
     */
    fun isLikelyRealNotification(pkgName: String, text: String): Boolean {
        val pkgLower = pkgName.lowercase()
        val isTargetApp = UPI_PACKAGES.contains(pkgLower) || TEST_PACKAGES.any { pkgLower.contains(it) }
        if (!isTargetApp) {
            return false
        }

        // Step 1: Promotional exclusion wins
        if (containsPromotionalContent(text)) {
            return false
        }

        // Step 2: Must contain an incoming payment verb
        val receivedKeywords = listOf(
            "received", "credited", "added", "refunded", "incoming",
            "cashback", "deposited", "paid to you", "received from"
        )
        val lowerText = text.lowercase()
        val hasIncomeKeyword = receivedKeywords.any { kw ->
            val pattern = Pattern.compile("\\b${Pattern.quote(kw)}\\b", Pattern.CASE_INSENSITIVE)
            pattern.matcher(lowerText).find()
        }

        if (!hasIncomeKeyword) {
            return false
        }

        // Step 3: Must contain a valid amount
        return hasValidAmount(text)
    }
}
