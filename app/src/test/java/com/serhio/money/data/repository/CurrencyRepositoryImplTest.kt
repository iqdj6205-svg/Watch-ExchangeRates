package com.serhio.money.data.repository

import com.serhio.money.data.database.CurrencyDao
import com.serhio.money.data.database.entities.CurrencyHistoryEntity
import com.serhio.money.data.network.ExchangeRateApi
import com.serhio.money.data.network.dto.ExchangeRateResponseDto
import com.serhio.money.data.repository.mapper.ExchangeRateMapper
import com.serhio.money.domain.model.ExchangeRate
import com.serhio.money.utils.NetworkMonitor
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class CurrencyRepositoryImplTest {

    private lateinit var api: ExchangeRateApi
    private lateinit var dao: CurrencyDao
    private lateinit var mapper: ExchangeRateMapper
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var repository: CurrencyRepositoryImpl

    @Before
    fun setup() {
        api = mockk()
        dao = mockk()
        mapper = ExchangeRateMapper()
        networkMonitor = mockk()
        repository = CurrencyRepositoryImpl(api, dao, mapper, networkMonitor)
    }

    @Test
    fun `fetchLatestRates returns rate when api succeeds`() = runTest {
        every { networkMonitor.isOnline } returns flowOf(true)

        val dto = ExchangeRateResponseDto(
            base = "USD",
            rates = mapOf("EUR" to 0.92),
            date = "2026-07-05",
            timestamp = null
        )
        coEvery { api.getLatestRates("USD") } returns Response.success(dto)
        coEvery { dao.insertHistoryRecords(any()) } returns Unit

        val result = repository.fetchLatestRates("USD")

        assertTrue(result.isSuccess)
        val rate = result.getOrThrow()
        assertEquals("USD", rate.baseCurrency)
        assertEquals(0.92, rate.rates["EUR"]!!, 0.001)
    }

    @Test
    fun `fetchLatestRates returns cached data when offline`() = runTest {
        every { networkMonitor.isOnline } returns flowOf(false)

        val cachedEntity = CurrencyHistoryEntity(
            id = 1,
            timestamp = 1751702400000L,
            baseCurrency = "USD",
            targetCurrency = "EUR",
            rate = 0.91,
            source = "exchangerate.fun"
        )
        coEvery { dao.getCachedRates("USD") } returns listOf(cachedEntity)

        val result = repository.fetchLatestRates("USD")

        assertTrue(result.isSuccess)
        val rate = result.getOrThrow()
        assertEquals("USD", rate.baseCurrency)
        assertEquals(0.91, rate.rates["EUR"]!!, 0.001)
        assertEquals(1751702400000L, rate.lastUpdate)
    }

    @Test
    fun `fetchLatestRates returns error when offline and no cache`() = runTest {
        every { networkMonitor.isOnline } returns flowOf(false)
        coEvery { dao.getCachedRates("USD") } returns emptyList()

        val result = repository.fetchLatestRates("USD")

        assertTrue(result.isFailure)
    }

    @Test
    fun `fetchLatestRates falls back to cache when api fails`() = runTest {
        every { networkMonitor.isOnline } returns flowOf(true)
        coEvery { api.getLatestRates("USD") } throws RuntimeException("Network error")

        val cachedEntity = CurrencyHistoryEntity(
            id = 1,
            timestamp = 1751702400000L,
            baseCurrency = "USD",
            targetCurrency = "EUR",
            rate = 0.91,
            source = "exchangerate.fun"
        )
        coEvery { dao.getCachedRates("USD") } returns listOf(cachedEntity)

        val result = repository.fetchLatestRates("USD")

        assertTrue(result.isSuccess)
        assertEquals(0.91, result.getOrThrow().rates["EUR"]!!, 0.001)
    }

    @Test
    fun `saveRateToHistory inserts entity for each rate`() = runTest {
        val rate = ExchangeRate(
            baseCurrency = "USD",
            rates = mapOf("EUR" to 0.92, "GBP" to 0.79),
            lastUpdate = 1751702400000L
        )
        coEvery { dao.insertHistoryRecord(any()) } returns Unit

        repository.saveRateToHistory(rate)

        coVerify(exactly = 2) { dao.insertHistoryRecord(any()) }
    }

    @Test
    fun `getHistory returns flow of exchange rates`() = runTest {
        val entities = listOf(
            CurrencyHistoryEntity(
                id = 1, timestamp = 1751702400000L,
                baseCurrency = "USD", targetCurrency = "EUR",
                rate = 0.92, source = "exchangerate.fun"
            )
        )
        every { dao.getHistoryForPair("USD", "EUR") } returns flowOf(entities)

        val result = repository.getHistory("USD", "EUR")

        result.collect { rates ->
            assertEquals(1, rates.size)
            assertEquals("USD", rates[0].baseCurrency)
            assertEquals(0.92, rates[0].rates["EUR"]!!, 0.001)
        }
    }

    @Test
    fun `clearOldRecords delegates to dao`() = runTest {
        coEvery { dao.deleteOldRecords(any()) } returns Unit

        repository.clearOldRecords(1000L)

        coVerify { dao.deleteOldRecords(1000L) }
    }
}