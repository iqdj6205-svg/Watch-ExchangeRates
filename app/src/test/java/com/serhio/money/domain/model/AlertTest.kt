package com.serhio.money.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AlertTest {

    @Test
    fun `direction returns greater-than symbol when isAbove is true`() {
        val alert = Alert(currencyCode = "EUR", targetRate = 0.95, isAbove = true)
        assertEquals("≥", alert.direction)
    }

    @Test
    fun `direction returns less-than symbol when isAbove is false`() {
        val alert = Alert(currencyCode = "EUR", targetRate = 0.95, isAbove = false)
        assertEquals("≤", alert.direction)
    }

    @Test
    fun `description formats correctly for above alert`() {
        val alert = Alert(currencyCode = "EUR", targetRate = 0.95, isAbove = true)
        assertEquals("1 EUR ≥ 0.95", alert.description)
    }

    @Test
    fun `description formats correctly for below alert`() {
        val alert = Alert(currencyCode = "PLN", targetRate = 4.10, isAbove = false)
        assertEquals("1 PLN ≤ 4.1", alert.description)
    }

    @Test
    fun `default values are correct`() {
        val alert = Alert(currencyCode = "USD", targetRate = 1.0, isAbove = true)
        assertEquals(0L, alert.id)
        assertTrue(alert.isEnabled)
        assertTrue(alert.createdAt > 0)
        assertEquals(null, alert.triggeredAt)
    }
}
