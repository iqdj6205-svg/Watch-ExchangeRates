package com.serhio.money.presentation

import com.serhio.money.data.settings.SettingsManager
import com.serhio.money.domain.model.ExchangeRate
import com.serhio.money.domain.repository.CurrencyRepository
import com.serhio.money.utils.NetworkMonitor
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

class MainViewModelTest {

    private lateinit var repository: CurrencyRepository
    private lateinit var settingsManager: SettingsManager
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var viewModel: MainViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        repository = mockk()
        settingsManager = mockk()
        networkMonitor = mockk()

        every { settingsManager.baseCurrencyFlow } returns flowOf("USD")
        every { settingsManager.interestedCurrenciesFlow } returns flowOf(listOf("EUR", "PLN"))
        every { networkMonitor.isOnline } returns flowOf(true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState is Success after successful refresh`() = runTest {
        val exchangeRate = ExchangeRate(
            baseCurrency = "USD",
            rates = mapOf("EUR" to 0.92, "PLN" to 4.05),
            lastUpdate = System.currentTimeMillis()
        )
        coEvery { repository.fetchLatestRates("USD") } returns Result.success(exchangeRate)
        coEvery { repository.getRecentHistory("USD", "EUR", 50) } returns flowOf(
            listOf(ExchangeRate("USD", mapOf("EUR" to 0.92), 1000L))
        )
        coEvery { repository.getRecentHistory("USD", "PLN", 50) } returns flowOf(
            listOf(ExchangeRate("USD", mapOf("PLN" to 4.05), 1000L))
        )
        coEvery { repository.saveRateToHistory(any()) } returns Unit

        viewModel = MainViewModel(repository, settingsManager, networkMonitor)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is MainUiState.Success)
        val success = state as MainUiState.Success
        assertEquals("USD", success.baseCurrency)
        assertEquals(2, success.rates.size)
        assertTrue(success.rates.containsKey("EUR"))
        assertTrue(success.rates.containsKey("PLN"))
    }

    @Test
    fun `uiState is Error when repository fails`() = runTest {
        coEvery { repository.fetchLatestRates("USD") } returns Result.failure(Exception("API Error"))

        viewModel = MainViewModel(repository, settingsManager, networkMonitor)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is MainUiState.Error)
        assertEquals("API Error", (state as MainUiState.Error).message)
    }

    @Test
    fun `refreshRates updates uiState`() = runTest {
        val exchangeRate = ExchangeRate(
            baseCurrency = "USD",
            rates = mapOf("EUR" to 0.92),
            lastUpdate = System.currentTimeMillis()
        )
        coEvery { repository.fetchLatestRates("USD") } returns Result.success(exchangeRate)
        coEvery { repository.getRecentHistory("USD", "EUR", 50) } returns flowOf(
            listOf(ExchangeRate("USD", mapOf("EUR" to 0.92), 1000L))
        )
        coEvery { repository.getRecentHistory("USD", "PLN", 50) } returns flowOf(
            listOf(ExchangeRate("USD", mapOf("PLN" to 4.05), 1000L))
        )
        coEvery { repository.saveRateToHistory(any()) } returns Unit

        viewModel = MainViewModel(repository, settingsManager, networkMonitor)
        advanceUntilIdle()

        viewModel.refreshRates()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is MainUiState.Success)
    }
}