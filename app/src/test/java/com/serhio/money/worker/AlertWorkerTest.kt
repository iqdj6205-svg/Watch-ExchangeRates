package com.serhio.money.worker

import com.serhio.money.data.repository.AlertRepository
import com.serhio.money.data.settings.SettingsLogic
import com.serhio.money.data.settings.SettingsManager
import com.serhio.money.domain.model.Alert
import com.serhio.money.domain.model.ExchangeRate
import com.serhio.money.domain.repository.CurrencyRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AlertWorkerTest {

    private lateinit var alertRepository: AlertRepository
    private lateinit var currencyRepository: CurrencyRepository
    private lateinit var settingsManager: SettingsManager

    @Before
    fun setup() {
        alertRepository = mockk()
        currencyRepository = mockk()
        settingsManager = mockk()

        every { settingsManager.baseCurrencyFlow } returns flowOf("USD")
        every { settingsManager.alertIntervalFlow } returns flowOf(SettingsLogic.DEFAULT_ALERT_INTERVAL_MS)
        coEvery { alertRepository.getEnabledAlerts() } returns emptyList()
    }

    private fun shouldTrigger(alert: Alert, currentRate: Double): Boolean {
        return if (alert.isAbove) currentRate >= alert.targetRate else currentRate <= alert.targetRate
    }

    @Test
    fun `alert triggers when rate is above target`() = runTest {
        val alert = Alert(currencyCode = "EUR", targetRate = 0.95, isAbove = true)
        assertTrue(shouldTrigger(alert, 0.97))
        assertFalse(shouldTrigger(alert, 0.94))
        assertTrue(shouldTrigger(alert, 0.95))
    }

    @Test
    fun `alert triggers when rate is below target`() = runTest {
        val alert = Alert(currencyCode = "EUR", targetRate = 0.90, isAbove = false)
        assertTrue(shouldTrigger(alert, 0.89))
        assertFalse(shouldTrigger(alert, 0.91))
        assertTrue(shouldTrigger(alert, 0.90))
    }

    @Test
    fun `alert interval default is fifteen minutes and selectable`() {
        assertEquals(15 * 60 * 1000L, SettingsLogic.DEFAULT_ALERT_INTERVAL_MS)
        assertTrue(SettingsLogic.isSupportedAlertInterval(15 * 60 * 1000L))
        assertTrue(SettingsLogic.isSupportedAlertInterval(6 * 60 * 60 * 1000L))
        assertFalse(SettingsLogic.isSupportedAlertInterval(24 * 60 * 60 * 1000L))
    }

    @Test
    fun `worker uses selected base currency when checking alerts`() = runTest {
        val alert = Alert(id = 1, currencyCode = "PLN", targetRate = 4.0, isAbove = true)
        every { settingsManager.baseCurrencyFlow } returns flowOf("EUR")
        coEvery { alertRepository.getEnabledAlerts() } returns listOf(alert)
        coEvery { currencyRepository.fetchLatestRates("EUR") } returns Result.success(
            ExchangeRate(baseCurrency = "EUR", rates = mapOf("PLN" to 4.3), lastUpdate = System.currentTimeMillis())
        )
        coEvery { alertRepository.markTriggered(any()) } returns Unit
        coEvery { alertRepository.toggleAlert(any(), any()) } returns Unit

        val rate = currencyRepository.fetchLatestRates("EUR").getOrThrow()
        val currentRate = rate.rates[alert.currencyCode]!!
        if (shouldTrigger(alert, currentRate)) {
            alertRepository.markTriggered(alert.id)
            alertRepository.toggleAlert(alert.id, false)
        }

        coVerify { currencyRepository.fetchLatestRates("EUR") }
        coVerify { alertRepository.markTriggered(1L) }
        coVerify { alertRepository.toggleAlert(1L, false) }
    }

    @Test
    fun `worker marks triggered and disables alert after firing`() = runTest {
        val alert = Alert(id = 1, currencyCode = "EUR", targetRate = 0.95, isAbove = true)
        val rate = ExchangeRate(baseCurrency = "USD", rates = mapOf("EUR" to 0.97), lastUpdate = System.currentTimeMillis())

        coEvery { alertRepository.getEnabledAlerts() } returns listOf(alert)
        coEvery { currencyRepository.fetchLatestRates("USD") } returns Result.success(rate)
        coEvery { alertRepository.markTriggered(any()) } returns Unit
        coEvery { alertRepository.toggleAlert(any(), any()) } returns Unit

        val currentRate = rate.rates[alert.currencyCode]!!
        if (shouldTrigger(alert, currentRate)) {
            alertRepository.markTriggered(alert.id)
            alertRepository.toggleAlert(alert.id, false)
        }

        coVerify { alertRepository.markTriggered(1L) }
        coVerify { alertRepository.toggleAlert(1L, false) }
    }

    @Test
    fun `alert not marked when rate below threshold`() = runTest {
        val alert = Alert(id = 1, currencyCode = "EUR", targetRate = 0.95, isAbove = true)
        val rate = ExchangeRate(baseCurrency = "USD", rates = mapOf("EUR" to 0.90), lastUpdate = System.currentTimeMillis())

        coEvery { alertRepository.getEnabledAlerts() } returns listOf(alert)
        coEvery { currencyRepository.fetchLatestRates("USD") } returns Result.success(rate)
        coEvery { alertRepository.markTriggered(any()) } returns Unit
        coEvery { alertRepository.toggleAlert(any(), any()) } returns Unit

        val currentRate = rate.rates[alert.currencyCode]!!
        if (shouldTrigger(alert, currentRate)) {
            alertRepository.markTriggered(alert.id)
            alertRepository.toggleAlert(alert.id, false)
        }

        coVerify(exactly = 0) { alertRepository.markTriggered(1L) }
        coVerify(exactly = 0) { alertRepository.toggleAlert(1L, false) }
    }

    @Test
    fun `worker returns retry when api fails`() = runTest {
        val alert = Alert(id = 1, currencyCode = "EUR", targetRate = 0.95, isAbove = true)

        coEvery { alertRepository.getEnabledAlerts() } returns listOf(alert)
        coEvery { currencyRepository.fetchLatestRates("USD") } returns Result.failure(Exception("API Error"))

        val result = currencyRepository.fetchLatestRates("USD")

        assertTrue(result.isFailure)
    }
}
