package com.payoutservice.domain

data class Payout(
    val userId: String?,
    val amount: Double,
    val currency: String?
)