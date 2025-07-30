package com.payoutservice.storage

import com.payoutservice.mother.PayoutMother
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PayoutStorageTest {

    private lateinit var storage: PayoutStorage

    @BeforeEach
    fun setUp() {
        storage = InMemoryPayoutStorage()
    }

    @Test
    fun `should store payout successfully`() {
        // Given
        val payout = PayoutMother.of()

        // When
        storage.store(payout)

        // Then
        val userPayouts = storage.getPayoutsForUser(payout.userId!!)
        userPayouts shouldHaveSize 1
        userPayouts.first() shouldBe payout
    }

    @Test
    fun `should return zero total for new user`() {
        // Given
        val userId = "newUser"

        // When
        val total = storage.getUserTotal(userId)

        // Then
        total shouldBe 0.0
    }

    @Test
    fun `should calculate correct user total for single payout`() {
        // Given
        val userId = "testUser"
        val payout = PayoutMother.of(userId = userId, amount = 25.50)

        // When
        storage.store(payout)

        // Then
        storage.getUserTotal(userId) shouldBe 25.50
    }

    @Test
    fun `should calculate correct user total for multiple payouts`() {
        // Given
        val userId = "multiPayoutUser"
        val payout1 = PayoutMother.of(userId = userId, amount = 10.0)
        val payout2 = PayoutMother.of(userId = userId, amount = 15.50)
        val payout3 = PayoutMother.of(userId = userId, amount = 20.25)

        // When
        storage.store(payout1)
        storage.store(payout2)
        storage.store(payout3)

        // Then
        storage.getUserTotal(userId) shouldBe 45.75
    }

    @Test
    fun `should handle different currencies for same user`() {
        // Given
        val userId = "mixedCurrencyUser"
        val payout1 = PayoutMother.of(userId = userId, amount = 10.0, currency = "USD")
        val payout2 = PayoutMother.of(userId = userId, amount = 20.0, currency = "EUR")
        val payout3 = PayoutMother.of(userId = userId, amount = 30.0, currency = "GBP")

        // When
        storage.store(payout1)
        storage.store(payout2)
        storage.store(payout3)

        // Then
        storage.getUserTotal(userId) shouldBe 60.0
        val userPayouts = storage.getPayoutsForUser(userId)
        userPayouts shouldHaveSize 3
    }

    @Test
    fun `should track different users independently`() {
        // Given
        val user1 = "user1"
        val user2 = "user2"
        val payout1 = PayoutMother.of(userId = user1, amount = 50.0)
        val payout2 = PayoutMother.of(userId = user2, amount = 75.0)

        // When
        storage.store(payout1)
        storage.store(payout2)

        // Then
        storage.getUserTotal(user1) shouldBe 50.0
        storage.getUserTotal(user2) shouldBe 75.0
        storage.getPayoutsForUser(user1) shouldHaveSize 1
        storage.getPayoutsForUser(user2) shouldHaveSize 1
    }

    @Test
    fun `should return empty list for user with no payouts`() {
        // Given
        val userId = "userWithNoPayouts"

        // When
        val payouts = storage.getPayoutsForUser(userId)

        // Then
        payouts.shouldBeEmpty()
    }
}