package com.example

import com.example.services.TransactionFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TransactionFilterTest {

    @Before
    fun setUp() {
        TransactionFilter.resetPromoKeywords()
    }

    // =========================================================================
    // 10 Genuine Transaction Test Cases (Indian Banks / UPI) -> Expected: TRUE
    // =========================================================================

    @Test
    fun testRealTransaction_01_HdfcBankDebitWithVpaAndUpiRef() {
        val message = "Rs.450 debited from A/c XX1234 on 08-Sep-26 to VPA merchant@upi. UPI Ref 123456789012. Avl Bal Rs.5,230.10 -HDFC Bank"
        assertTrue("HDFC debit with VPA and UPI Ref should be real", TransactionFilter.isLikelyRealTransaction(message))
    }

    @Test
    fun testRealTransaction_02_PhonePeReceivedWithTransactionId() {
        val message = "You received Rs.1200 from Rahul Sharma via PhonePe. UPI transaction ID: 987654321."
        assertTrue("PhonePe received with UPI transaction ID should be real", TransactionFilter.isLikelyRealTransaction(message))
    }

    @Test
    fun testRealTransaction_03_SbiDebitWithEndingAccountAndRefNo() {
        val message = "Dear SBI User, your A/c ending with 4321 is debited for Rs 1,499.00 on 07-Jul-26 by transfer to Netflix. Ref No 623456789012. Avl Bal: INR 23,450.00"
        assertTrue("SBI debit with account ending and Ref No should be real", TransactionFilter.isLikelyRealTransaction(message))
    }

    @Test
    fun testRealTransaction_04_IciciCardSpentWithAuthCode() {
        val message = "Your ICICI Bank Card XX5678 spent INR 3,250.00 at AMAZON INDIA on 06-Jul-26. Avl Lmt: INR 65,000.00. Auth code: 458921"
        assertTrue("ICICI card spent with card XX5678 should be real", TransactionFilter.isLikelyRealTransaction(message))
    }

    @Test
    fun testRealTransaction_05_AxisBankDebitWithUpiRefAndAvlBal() {
        val message = "INR 750.00 debited from Axis Bank A/c no. XX8890 on 05-Jul-26. Info: Swiggy. UPI Ref: 319283746192. Avl bal: INR 8,120.50"
        assertTrue("Axis Bank debit with UPI Ref should be real", TransactionFilter.isLikelyRealTransaction(message))
    }

    @Test
    fun testRealTransaction_06_KotakSentToVpaWithUpiRef() {
        val message = "Sent Rs.2,000 from Kotak Bank A/c XX3344 to ankit@okaxis on 04-Jul-26. UPI Ref No 401928374651. Bal Rs.15,670"
        assertTrue("Kotak sent to VPA should be real", TransactionFilter.isLikelyRealTransaction(message))
    }

    @Test
    fun testRealTransaction_07_PaytmPaymentsBankPaidWithUpiRef() {
        val message = "Paid Rs. 180 to Sharma General Store from Paytm Payments Bank A/c XX9012. UPI Ref 382910293847."
        assertTrue("Paytm Payments Bank payment should be real", TransactionFilter.isLikelyRealTransaction(message))
    }

    @Test
    fun testRealTransaction_08_PnbDebitWithUpiAndAvlBal() {
        val message = "A/C 1234 debited by Rs.850.00 on 03-Jul-26 by UPI: 382910482910. Avl Bal: Rs.4200.00 - PNB"
        assertTrue("PNB debit with Avl Bal should be real", TransactionFilter.isLikelyRealTransaction(message))
    }

    @Test
    fun testRealTransaction_09_BobAtmWithdrawalWithAvlBal() {
        val message = "BOB Txn Alert: Rs. 500.00 withdrawn from A/c XX4455 via ATM on 02-Jul-26. Avl Bal: Rs. 12,300.00"
        assertTrue("Bank of Baroda ATM withdrawal should be real", TransactionFilter.isLikelyRealTransaction(message))
    }

    @Test
    fun testRealTransaction_10_SalaryCreditWithTotalBalAndUtr() {
        val message = "Your A/c XX7890 is credited with INR 75,000.00 on 01-Jul-26 towards July Salary. UPI/NEFT Ref: CORP91823746. Total Bal: INR 92,450.00"
        assertTrue("Salary credit with NEFT Ref should be real", TransactionFilter.isLikelyRealTransaction(message))
    }

    // =========================================================================
    // 10 Promotional / Marketing False Positives -> Expected: FALSE
    // =========================================================================

    @Test
    fun testPromoMessage_11_CashbackNextOrderUseCode() {
        val message = "Get ₹200 cashback on your next order, use code SAVE200"
        assertFalse("Cashback promo with promo code should be rejected", TransactionFilter.isLikelyRealTransaction(message))
    }

    @Test
    fun testPromoMessage_12_SpecialOfferWinVoucher() {
        val message = "You've received a special offer — spend ₹500 and win a voucher"
        assertFalse("Special offer to win voucher should be rejected", TransactionFilter.isLikelyRealTransaction(message))
    }

    @Test
    fun testPromoMessage_13_CashbackNextWeekNoStructuralSignal() {
        val message = "Cashback of ₹100 credited on your Amazon purchase next week!"
        assertFalse("Promotional cashback for future purchase should be rejected", TransactionFilter.isLikelyRealTransaction(message))
    }

    @Test
    fun testPromoMessage_14_LuckySpinWinClaimNow() {
        val message = "Congratulations! You won Rs. 5000 cashback in the lucky spin! Claim now at https://reward.app"
        assertFalse("Lucky draw and claim now should be rejected", TransactionFilter.isLikelyRealTransaction(message))
    }

    @Test
    fun testPromoMessage_15_FlatDiscountLimitedTimeOffer() {
        val message = "Dear customer, flat 50% off on all dining when you spend Rs. 1000 or more with your HDFC card. Limited time offer."
        assertFalse("Card promotional discount offer should be rejected", TransactionFilter.isLikelyRealTransaction(message))
    }

    @Test
    fun testPromoMessage_16_ReferAndEarnUseCode() {
        val message = "Refer and earn ₹100 for every friend who signs up. Use code REF100."
        assertFalse("Refer and earn promo should be rejected", TransactionFilter.isLikelyRealTransaction(message))
    }

    @Test
    fun testPromoMessage_17_ApplyCouponDiscountValidTill() {
        val message = "Apply code TREAT150 to get ₹150 discount on your order at Zomato. Valid till Sunday."
        assertFalse("Coupon discount promo should be rejected", TransactionFilter.isLikelyRealTransaction(message))
    }

    @Test
    fun testPromoMessage_18_PreApprovedLoanApplyNow() {
        val message = "Pre-approved personal loan of Rs. 5,00,000 credited to your account instantly. Click here to claim now."
        assertFalse("Pre-approved loan marketing should be rejected", TransactionFilter.isLikelyRealTransaction(message))
    }

    @Test
    fun testPromoMessage_19_RewardPointsNextPurchaseLimitedPeriod() {
        val message = "Unlock 1000 reward points on your next fuel purchase of Rs. 2000. Limited period offer."
        assertFalse("Reward points promo should be rejected", TransactionFilter.isLikelyRealTransaction(message))
    }

    @Test
    fun testPromoMessage_20_CashbackUpToBillPaymentHurry() {
        val message = "Get cashback of up to Rs. 500 on your first electricity bill payment. Hurry, offer ends tonight!"
        assertFalse("Cashback of up to with hurry should be rejected", TransactionFilter.isLikelyRealTransaction(message))
    }

    // =========================================================================
    // Core Logic & Edge Cases Verification
    // =========================================================================

    @Test
    fun testPromoWinsEvenIfRealLookingSignalsPresent() {
        // A message with account digits, but containing promotional keyword "win" or "voucher"
        val message = "Rs. 500 spent from A/c XX1234. Use code WIN500 to win a voucher on your next order! UPI Ref 123456"
        assertFalse("Promo exclusion must win over real-looking structural signals", TransactionFilter.isLikelyRealTransaction(message))
    }

    @Test
    fun testSubstringsDoNotTriggerFalsePromo() {
        // Words like "Edwin", "Windows", "offering" should not trigger promo match for "win" or "offer"
        val message = "Rs. 300 debited from A/c XX9999 to Edwin at Chai Point. Ref: 987654321012. Avl Bal: Rs. 1,000"
        assertTrue("Substrings like 'Edwin' must not falsely match 'win'", TransactionFilter.isLikelyRealTransaction(message))
    }

    @Test
    fun testDynamicPromoKeywordConfig() {
        val message = "Rs. 250 debited from A/c XX1111 for mega blockbuster combo. Ref: 112233445566."
        assertTrue("Initially accepted as real transaction", TransactionFilter.isLikelyRealTransaction(message))

        // Extend negative keywords dynamically
        TransactionFilter.addPromoKeyword("blockbuster")
        assertFalse("Should be rejected after adding custom promo keyword", TransactionFilter.isLikelyRealTransaction(message))

        // Remove keyword
        TransactionFilter.removePromoKeyword("blockbuster")
        assertTrue("Should be accepted again after removing keyword", TransactionFilter.isLikelyRealTransaction(message))
    }

    @Test
    fun testNotificationFilter_ValidPaymentApp() {
        val gpayPkg = "com.google.android.apps.nbu.paisa.user"
        val validNotification = "Received ₹450 from Priya Patel"
        assertTrue("Genuine Google Pay notification should be accepted",
            TransactionFilter.isLikelyRealNotification(gpayPkg, "Google Pay", validNotification)
        )
    }

    @Test
    fun testNotificationFilter_PromoNotificationRejected() {
        val phonepePkg = "com.phonepe.app"
        val promoNotification = "Get ₹50 cashback on bill payment! Use code BILL50"
        assertFalse("Promotional notification from payment app should be rejected",
            TransactionFilter.isLikelyRealNotification(phonepePkg, "PhonePe", promoNotification)
        )
    }

    @Test
    fun testNotificationFilter_InvalidAppRejected() {
        val invalidPkg = "com.random.chat"
        val text = "Received ₹1,000 from John"
        assertFalse("Notification from unverified app must be rejected",
            TransactionFilter.isLikelyRealNotification(invalidPkg, "Chat", text)
        )
    }
}
