package com.serhio.money.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ExchangeRateTest {

    @Test
    fun `data class equality works correctly`() {
        val rate1 = ExchangeRate(baseCurrency = "USD", rates = mapOf("EUR" to 0.92), lastUpdate = 1000L)
        val rate2 = ExchangeRate(baseCurrency = "USD", rates = mapOf("EUR" to 0.92), lastUpdate = 1000L)
        assertEquals(rate1, rate2)
    }

    @Test
    fun `copy preserves unchanged fields`() {
        val original = ExchangeRate(baseCurrency = "USD", rates = mapOf("EUR" to 0.92, "GBP" to 0.79), lastUpdate = 1000L)
        val copied = original.copy(baseCurrency = "EUR")
        assertEquals("EUR", copied.baseCurrency)
        assertEquals(original.rates, copied.rates)
        assertEquals(original.lastUpdate, copied.lastUpdate)
    }
}
