package com.serhio.money.data.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO для ответа API exchangerate.fun.
 * Поддерживает оба варианта (date и timestamp) для совместимости.
 */
@JsonClass(generateAdapter = true)
data class ExchangeRateResponseDto(
    @Json(name = "base") val base: String,
    @Json(name = "rates") val rates: Map<String, Double>,
    @Json(name = "date") val date: String? = null,
    @Json(name = "timestamp") val timestamp: Long? = null
)
