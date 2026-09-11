package com.serhio.money.data.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsLogicTest {

    @Test
    fun `normalizeCurrency trims and uppercases code`() {
        assertEquals("PLN", SettingsLogic.normalizeCurrency(" pln "))
    }

    @Test
    fun `normalizeCurrencies removes blanks duplicates and normalizes order`() {
        val result = SettingsLogic.normalizeCurrencies(listOf(" eur ", "", "PLN", "eur", "uah "))
        assertEquals(listOf("EUR", "PLN", "UAH"), result)
    }

    @Test
    fun `parseCurrencyList handles saved comma separated preferences`() {
        val result = SettingsLogic.parseCurrencyList(" eur, PLN,,uah,eur ")
        assertEquals(listOf("EUR", "PLN", "UAH"), result)
    }

    @Test
    fun `serializeCurrencyList stores normalized comma separated value`() {
        val result = SettingsLogic.serializeCurrencyList(listOf(" eur ", "PLN", "eur", " uah"))
        assertEquals("EUR,PLN,UAH", result)
    }

    @Test
    fun `mergeReorderedCurrencies keeps reordered items first and preserves hidden existing items`() {
        val result = SettingsLogic.mergeReorderedCurrencies(
            newOrder = listOf("UAH", "EUR"),
            existing = listOf("EUR", "PLN", "UAH", "CHF")
        )
        assertEquals(listOf("UAH", "EUR", "PLN", "CHF"), result)
    }

    @Test
    fun `alert intervals expose user selectable values with fifteen minute default`() {
        assertEquals(15 * 60 * 1000L, SettingsLogic.DEFAULT_ALERT_INTERVAL_MS)
        assertEquals(listOf("15m", "30m", "1h", "2h", "6h"), SettingsLogic.alertIntervalOptions.map { it.first })
        assertTrue(SettingsLogic.isSupportedAlertInterval(30 * 60 * 1000L))
        assertFalse(SettingsLogic.isSupportedAlertInterval(24 * 60 * 60 * 1000L))
    }

    @Test
    fun `update interval options include longer periods than alert checks`() {
        assertTrue(SettingsLogic.updateIntervalOptions.any { it.first == "12h" })
        assertTrue(SettingsLogic.updateIntervalOptions.any { it.first == "24h" })
        assertFalse(SettingsLogic.alertIntervalOptions.any { it.first == "24h" })
    }
}
