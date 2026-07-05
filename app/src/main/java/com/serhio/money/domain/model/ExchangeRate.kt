package com.serhio.money.domain.model

/**
 * Доменная модель курса валюты.
 */
data class ExchangeRate(
    val baseCurrency: String,
    val rates: Map<String, Double>,
    val lastUpdate: Long
)
