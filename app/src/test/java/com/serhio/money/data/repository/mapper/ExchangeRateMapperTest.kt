package com.serhio.money.data.repository.mapper

import com.serhio.money.data.network.dto.ExchangeRateResponseDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExchangeRateMapperTest {

    private lateinit var mapper: ExchangeRateMapper

    @Before
    fun setup() {
        mapper = ExchangeRateMapper()
    }

    @Test
    fun `toDomain maps DTO to domain model correctly`() {
        val dto = ExchangeRateResponseDto(
            base = "USD",
            rates = mapOf("EUR" to 0.92, "PLN" to 4.05),
            date = "2026-07-05",
            timestamp = null
        )

        val result = mapper.toDomain(dto)

        assertEquals("USD", result.baseCurrency)
        assertEquals(0.92, result.rates["EUR"]!!, 0.001)
        assertEquals(4.05, result.rates["PLN"]!!, 0.001)
        assertTrue(result.lastUpdate > 0)
    }

    @Test
    fun `toDomain uses timestamp when available`() {
        val dto = ExchangeRateResponseDto(
            base = "EUR",
            rates = mapOf("USD" to 1.09),
            date = null,
            timestamp = 1751702400L
        )

        val result = mapper.toDomain(dto)

        assertEquals(1751702400000L, result.lastUpdate)
    }

    @Test
    fun `toDomain falls back to current time when no date or timestamp`() {
        val dto = ExchangeRateResponseDto(
            base = "GBP",
            rates = mapOf("USD" to 1.27),
            date = null,
            timestamp = null
        )

        val result = mapper.toDomain(dto)

        assertTrue(result.lastUpdate > 0)
    }

    @Test
    fun `toEntities maps domain to list of entities`() {
        val domain = com.serhio.money.domain.model.ExchangeRate(
            baseCurrency = "USD",
            rates = mapOf("EUR" to 0.92, "GBP" to 0.79),
            lastUpdate = 1751702400000L
        )

        val entities = mapper.toEntities(domain)

        assertEquals(2, entities.size)
        entities.forEach { entity ->
            assertEquals("USD", entity.baseCurrency)
            assertEquals("exchangerate.fun", entity.source)
            assertEquals(1751702400000L, entity.timestamp)
        }
        assertEquals("EUR", entities[0].targetCurrency)
        assertEquals(0.92, entities[0].rate, 0.001)
        assertEquals("GBP", entities[1].targetCurrency)
        assertEquals(0.79, entities[1].rate, 0.001)
    }
}