package com.serhio.money.data.settings

object SettingsLogic {
    const val DEFAULT_BASE_CURRENCY = "USD"
    const val DEFAULT_INTERESTED_CURRENCIES = "EUR,PLN,UAH"
    const val DEFAULT_UPDATE_INTERVAL_MS = 60 * 60 * 1000L
    const val DEFAULT_ALERT_INTERVAL_MS = 15 * 60 * 1000L

    val alertIntervalOptions: List<Pair<String, Long>> = listOf(
        "15m" to 15 * 60 * 1000L,
        "30m" to 30 * 60 * 1000L,
        "1h" to 60 * 60 * 1000L,
        "2h" to 2 * 60 * 60 * 1000L,
        "6h" to 6 * 60 * 60 * 1000L
    )

    val updateIntervalOptions: List<Pair<String, Long>> = alertIntervalOptions + listOf(
        "12h" to 12 * 60 * 60 * 1000L,
        "24h" to 24 * 60 * 60 * 1000L
    )

    fun normalizeCurrency(currency: String): String = currency.trim().uppercase()

    fun normalizeCurrencies(currencies: List<String>): List<String> {
        return currencies
            .map(::normalizeCurrency)
            .filter { it.isNotBlank() }
            .distinct()
    }

    fun parseCurrencyList(raw: String): List<String> = normalizeCurrencies(raw.split(","))

    fun serializeCurrencyList(currencies: List<String>): String = normalizeCurrencies(currencies).joinToString(",")

    fun mergeReorderedCurrencies(newOrder: List<String>, existing: List<String>): List<String> {
        val normalizedNewOrder = normalizeCurrencies(newOrder)
        val normalizedExisting = normalizeCurrencies(existing)
        return normalizedNewOrder + normalizedExisting.filter { it !in normalizedNewOrder }
    }

    fun isSupportedAlertInterval(intervalMs: Long): Boolean = alertIntervalOptions.any { it.second == intervalMs }
}
