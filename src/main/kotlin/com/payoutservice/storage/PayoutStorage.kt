package com.payoutservice.storage

import com.payoutservice.domain.Payout

interface PayoutStorage {
    fun store(payout: Payout)
    fun getUserTotal(userId: String): Double
    fun getPayoutsForUser(userId: String): List<Payout>
}

class InMemoryPayoutStorage : PayoutStorage {

    private val payouts = mutableListOf<Payout>()

    override fun store(payout: Payout) {
        payouts.add(payout)
    }

    override fun getUserTotal(userId: String): Double {
        return payouts
            .filter { it.userId == userId }
            .sumOf { it.amount }
    }

    override fun getPayoutsForUser(userId: String): List<Payout> {
        return payouts
            .filter { it.userId == userId }
            .toList()
    }
}