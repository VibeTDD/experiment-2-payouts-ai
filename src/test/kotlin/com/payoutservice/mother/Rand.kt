package com.payoutservice.mother

import java.util.*
import kotlin.random.Random

object Rand {

    fun string(): String = UUID.randomUUID().toString()

    fun currency(): String = Currency.getAvailableCurrencies().random().currencyCode

    fun amount(): Double = Random.nextDouble(0.01, 100.0).round(2)

    fun double(min: Double, max: Double): Double = Random.nextDouble(min, max).round(2)

    fun int(min: Int, max: Int): Int = Random.nextInt(min, max + 1)

    fun boolean(): Boolean = Random.nextBoolean()

    fun <T> fromList(items: List<T>): T = items.random()

    // Helper extension for rounding
    private fun Double.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return kotlin.math.round(this * multiplier) / multiplier
    }
}