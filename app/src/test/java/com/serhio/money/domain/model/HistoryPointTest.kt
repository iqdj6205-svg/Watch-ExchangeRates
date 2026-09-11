package com.serhio.money.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryPointTest {

    @Test
    fun `data class equality works correctly`() {
        val point1 = HistoryPoint(timestamp = 1000L, rate = 0.92)
        val point2 = HistoryPoint(timestamp = 1000L, rate = 0.92)
        assertEquals(point1, point2)
    }

    @Test
    fun `copy preserves unchanged fields`() {
        val original = HistoryPoint(timestamp = 1000L, rate = 0.92)
        val copied = original.copy(rate = 0.95)
        assertEquals(0.95, copied.rate, 0.001)
        assertEquals(original.timestamp, copied.timestamp)
    }
}
