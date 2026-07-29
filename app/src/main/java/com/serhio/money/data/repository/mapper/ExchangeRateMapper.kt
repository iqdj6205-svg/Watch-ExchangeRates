package com.serhio.money.data.repository.mapper

import com.serhio.money.data.network.dto.ExchangeRateResponseDto
import com.serhio.money.domain.model.ExchangeRate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExchangeRateMapper @Inject constructor() {

    fun toDomain(dto: ExchangeRateResponseDto): ExchangeRate {
        val lastUpdateMillis = when {
            dto.timestamp != null -> dto.timestamp * 1000L
            dto.date != null -> {
                try {
                    java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                        .parse(dto.date)?.time ?: System.currentTimeMillis()
                } catch (e: Exception) {
                    System.currentTimeMillis()
                }
            }
            else -> System.currentTimeMillis()
        }

        return ExchangeRate(
            baseCurrency = dto.base,
            rates = dto.rates,
            lastUpdate = lastUpdateMillis
        )
    }
}