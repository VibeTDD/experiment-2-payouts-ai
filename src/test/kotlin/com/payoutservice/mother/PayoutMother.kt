package com.payoutservice.mother

import com.payoutservice.domain.Payout

object PayoutMother {

    private val validCurrencies = listOf("EUR", "USD", "GBP")

    fun of(
        userId: String? = Rand.string(),
        amount: Double = Rand.double(0.01, 30.0),
        currency: String? = Rand.fromList(validCurrencies),
    ) = Payout(
        userId = userId,
        amount = amount,
        currency = currency,
    )
}