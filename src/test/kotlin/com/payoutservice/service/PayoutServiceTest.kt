package com.payoutservice.service

import com.payoutservice.domain.Payout
import com.payoutservice.exception.PayoutValidationException
import com.payoutservice.mother.PayoutMother
import com.payoutservice.mother.Rand
import com.payoutservice.storage.PayoutStorage
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PayoutServiceTest {

    private lateinit var payoutService: PayoutService
    private lateinit var mockStorage: MockPayoutStorage

    @BeforeEach
    fun setUp() {
        mockStorage = MockPayoutStorage()
        payoutService = PayoutService(mockStorage)
    }

    @Test
    fun `should successfully process valid payout`() {
        // Given
        val payout = PayoutMother.of()

        // When
        val result = payoutService.processPayout(payout)

        // Then
        result shouldBe true
        mockStorage.verifyStored(payout)
    }

    @Test
    fun `should reject payout with null userId`() {
        // Given
        val payout = PayoutMother.of(userId = null)

        // When & Then
        val exception = shouldThrow<PayoutValidationException> {
            payoutService.processPayout(payout)
        }
        exception.message shouldContain "userId is required"
        mockStorage.verifyNotStored()
    }

    @Test
    fun `should reject payout with empty userId`() {
        // Given
        val payout = PayoutMother.of(userId = "")

        // When & Then
        val exception = shouldThrow<PayoutValidationException> {
            payoutService.processPayout(payout)
        }
        exception.message shouldContain "userId cannot be empty"
        mockStorage.verifyNotStored()
    }

    @Test
    fun `should reject payout with amount exceeding 30`() {
        // Given
        val payout = PayoutMother.of(amount = Rand.double(30.01, 50.0))

        // When & Then
        val exception = shouldThrow<PayoutValidationException> {
            payoutService.processPayout(payout)
        }
        exception.message shouldContain "amount must not exceed 30"
        mockStorage.verifyNotStored()
    }

    @Test
    fun `should accept payout with amount equal to 30`() {
        // Given
        val payout = PayoutMother.of(amount = 30.0)

        // When
        val result = payoutService.processPayout(payout)

        // Then
        result shouldBe true
        mockStorage.verifyStored(payout)
    }

    @Test
    fun `should reject payout with negative amount`() {
        // Given
        val payout = PayoutMother.of(amount = Rand.double(-100.0, -0.01))

        // When & Then
        val exception = shouldThrow<PayoutValidationException> {
            payoutService.processPayout(payout)
        }
        exception.message shouldContain "amount must be positive"
        mockStorage.verifyNotStored()
    }

    @Test
    fun `should reject payout with zero amount`() {
        // Given
        val payout = PayoutMother.of(amount = 0.0)

        // When & Then
        val exception = shouldThrow<PayoutValidationException> {
            payoutService.processPayout(payout)
        }
        exception.message shouldContain "amount must be positive"
        mockStorage.verifyNotStored()
    }

    @Test
    fun `should accept payout with valid EUR currency`() {
        // Given
        val payout = PayoutMother.of(currency = "EUR")

        // When
        val result = payoutService.processPayout(payout)

        // Then
        result shouldBe true
        mockStorage.verifyStored(payout)
    }

    @Test
    fun `should accept payout with valid USD currency`() {
        // Given
        val payout = PayoutMother.of(currency = "USD")

        // When
        val result = payoutService.processPayout(payout)

        // Then
        result shouldBe true
        mockStorage.verifyStored(payout)
    }

    @Test
    fun `should accept payout with valid GBP currency`() {
        // Given
        val payout = PayoutMother.of(currency = "GBP")

        // When
        val result = payoutService.processPayout(payout)

        // Then
        result shouldBe true
        mockStorage.verifyStored(payout)
    }

    @Test
    fun `should reject payout with invalid currency`() {
        // Given
        val invalidCurrencies = listOf("JPY", "CAD", "AUD", "CHF")
        val payout = PayoutMother.of(currency = Rand.fromList(invalidCurrencies))

        // When & Then
        val exception = shouldThrow<PayoutValidationException> {
            payoutService.processPayout(payout)
        }
        exception.message shouldContain "currency must be one of: EUR, USD, GBP"
        mockStorage.verifyNotStored()
    }

    @Test
    fun `should reject payout with empty currency`() {
        // Given
        val payout = PayoutMother.of(currency = "")

        // When & Then
        val exception = shouldThrow<PayoutValidationException> {
            payoutService.processPayout(payout)
        }
        exception.message shouldContain "currency cannot be empty"
        mockStorage.verifyNotStored()
    }

    @Test
    fun `should accept first payout for new user`() {
        // Given
        val userId = "newUser"
        val payout = PayoutMother.of(userId = userId, amount = 50.0)
        mockStorage.setUserTotal(userId, 0.0)

        // When
        val result = payoutService.processPayout(payout)

        // Then
        result shouldBe true
        mockStorage.verifyStored(payout)
    }

    @Test
    fun `should accept multiple payouts within user limit`() {
        // Given
        val userId = "testUser"
        val payout = PayoutMother.of(userId = userId, amount = 10.0)
        mockStorage.setUserTotal(userId, 90.0) // Existing total

        // When
        val result = payoutService.processPayout(payout)

        // Then
        result shouldBe true
        mockStorage.verifyStored(payout)
    }

    @Test
    fun `should accept payout that brings user total to exactly 100`() {
        // Given
        val userId = "limitUser"
        val payout = PayoutMother.of(userId = userId, amount = 30.0)
        mockStorage.setUserTotal(userId, 70.0) // Existing total

        // When
        val result = payoutService.processPayout(payout)

        // Then
        result shouldBe true
        mockStorage.verifyStored(payout)
    }

    @Test
    fun `should reject payout that exceeds user total limit`() {
        // Given
        val userId = "overLimitUser"
        val payout = PayoutMother.of(userId = userId, amount = 25.0)
        mockStorage.setUserTotal(userId, 80.0) // Existing total

        // When & Then
        val exception = shouldThrow<PayoutValidationException> {
            payoutService.processPayout(payout)
        }
        exception.message shouldContain "user total limit of 100 exceeded"
        mockStorage.verifyNotStored()
    }

    @Test
    fun `should reject small payout when user already at limit`() {
        // Given
        val userId = "atLimitUser"
        val payout = PayoutMother.of(userId = userId, amount = 0.01)
        mockStorage.setUserTotal(userId, 100.0) // At limit

        // When & Then
        val exception = shouldThrow<PayoutValidationException> {
            payoutService.processPayout(payout)
        }
        exception.message shouldContain "user total limit of 100 exceeded"
        mockStorage.verifyNotStored()
    }

    @Test
    fun `should handle mixed currencies for same user within limit`() {
        // Given
        val userId = "mixedCurrencyUser"
        val payout = PayoutMother.of(userId = userId, amount = 40.0, currency = "GBP")
        mockStorage.setUserTotal(userId, 60.0) // Existing USD + EUR

        // When
        val result = payoutService.processPayout(payout)

        // Then
        result shouldBe true
        mockStorage.verifyStored(payout)
    }

    @Test
    fun `should track different users independently`() {
        // Given
        val user1 = "user1"
        val payout = PayoutMother.of(userId = user1, amount = 100.0)
        mockStorage.setUserTotal(user1, 0.0)

        // When
        val result = payoutService.processPayout(payout)

        // Then
        result shouldBe true
        mockStorage.verifyStored(payout)
    }

    // Mock implementation for testing
    private class MockPayoutStorage : PayoutStorage {
        private val userTotals = mutableMapOf<String, Double>()
        private val storedPayouts = mutableListOf<Payout>()
        private var storeWasCalled = false

        override fun store(payout: Payout) {
            storedPayouts.add(payout)
            storeWasCalled = true
        }

        override fun getUserTotal(userId: String): Double {
            return userTotals[userId] ?: 0.0
        }

        override fun getPayoutsForUser(userId: String): List<Payout> {
            return storedPayouts.filter { it.userId == userId }
        }

        fun setUserTotal(userId: String, total: Double) {
            userTotals[userId] = total
        }

        fun verifyStored(expectedPayout: Payout) {
            if (!storeWasCalled) {
                throw AssertionError("Expected store() to be called but it wasn't")
            }
            if (!storedPayouts.contains(expectedPayout)) {
                throw AssertionError("Expected payout $expectedPayout to be stored but it wasn't")
            }
        }

        fun verifyNotStored() {
            if (storeWasCalled) {
                throw AssertionError("Expected store() NOT to be called but it was")
            }
        }
    }
}