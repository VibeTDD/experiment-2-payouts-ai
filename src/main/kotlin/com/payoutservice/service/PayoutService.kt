package com.payoutservice.service

import com.payoutservice.domain.Payout
import com.payoutservice.exception.PayoutValidationException
import com.payoutservice.storage.InMemoryPayoutStorage
import com.payoutservice.storage.PayoutStorage

class PayoutService(
    private val storage: PayoutStorage = InMemoryPayoutStorage()
) {

    private val validCurrencies = setOf("EUR", "USD", "GBP")
    private val userTotalLimit = 100.0

    fun processPayout(payout: Payout): Boolean {
        validatePayout(payout)
        storage.store(payout)
        return true
    }

    private fun validatePayout(payout: Payout) {
        validateUserId(payout.userId)
        validateAmount(payout.amount)
        validateCurrency(payout.currency)
        validateUserTotalLimit(payout)
    }

    private fun validateUserId(userId: String?) {
        if (userId == null) {
            throw PayoutValidationException("userId is required")
        }
        if (userId.isEmpty()) {
            throw PayoutValidationException("userId cannot be empty")
        }
    }

    private fun validateAmount(amount: Double) {
        if (amount <= 0.0) {
            throw PayoutValidationException("amount must be positive")
        }
        if (amount > 30.0) {
            throw PayoutValidationException("amount must not exceed 30")
        }
    }

    private fun validateCurrency(currency: String?) {
        if (currency == null || currency.isEmpty()) {
            throw PayoutValidationException("currency cannot be empty")
        }
        if (currency !in validCurrencies) {
            throw PayoutValidationException("currency must be one of: EUR, USD, GBP")
        }
    }

    private fun validateUserTotalLimit(payout: Payout) {
        // Safe to use !! here because we've already validated userId is not null
        val currentUserTotal = storage.getUserTotal(payout.userId!!)
        val newTotal = currentUserTotal + payout.amount

        if (newTotal > userTotalLimit) {
            throw PayoutValidationException("user total limit of 100 exceeded")
        }
    }
}