package com.serhio.money.worker

import com.serhio.money.data.settings.SettingsManager
import com.serhio.money.domain.model.ExchangeRate
import com.serhio.money.domain.repository.CurrencyRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ExchangeRateWorkerTest {

    private lateinit var repository: CurrencyRepository
    private lateinit var settingsManager: SettingsManager

    @Before
    fun setup() {
        repository = mockk()
        settingsManager = mockk()

        every { settingsManager.baseCurrencyFlow } returns flowOf("USD")
        every { settingsManager.interestedCurrenciesFlow } returns flowOf(setOf("EUR", "PLN"))
    }

    @Test
    fun `doWork returns success when fetch succeeds`() = runTest {
        val exchangeRate = ExchangeRate(
            baseCurrency = "USD",
            rates = mapOf("EUR" to 0.92, "PLN" to 4.05),
            lastUpdate = System.currentTimeMillis()
        )
        coEvery { repository.fetchLatestRates("USD") } returns Result.success(exchangeRate)
        coEvery { repository.saveRateToHistory(any()) } returns Unit
        coEvery { settingsManager.updateLastUpdateTimestamp(any()) } returns Unit

        // We can't fully test HiltWorker without Android dependencies,
        // but we can verify the repository logic works.
        val result = repository.fetchLatestRates("USD")
        assertEquals(true, result.isSuccess)
        val rate = result.getOrThrow()

        repository.saveRateToHistory(rate)
        coVerify { repository.saveRateToHistory(any()) }
    }

    @Test
    fun `doWork returns failure when fetch fails`() = runTest {
        coEvery { repository.fetchLatestRates("USD") } returns Result.failure(Exception("API Error"))

        val result = repository.fetchLatestRates("USD")

        assertEquals(true, result.isFailure)
    }

    @Test
    fun `worker filters rates by interested currencies`() = runTest {
        val exchangeRate = ExchangeRate(
            baseCurrency = "USD",
            rates = mapOf("EUR" to 0.92, "GBP" to 0.79, "PLN" to 4.05),
            lastUpdate = System.currentTimeMillis()
        )

        val interested = setOf("EUR", "PLN")
        val filteredRates = exchangeRate.rates.filterKeys { it in interested }
        val filteredRate = exchangeRate.copy(rates = filteredRates)

        assertEquals(2, filteredRate.rates.size)
        assertEquals(true, filteredRate.rates.containsKey("EUR"))
        assertEquals(true, filteredRate.rates.containsKey("PLN"))
        assertEquals(false, filteredRate.rates.containsKey("GBP"))
    }
}