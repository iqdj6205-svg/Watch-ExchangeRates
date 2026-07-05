package com.serhio.money.domain.repository

import com.serhio.money.domain.model.ExchangeRate
import kotlinx.coroutines.flow.Flow

interface CurrencyRepository {
    suspend fun fetchLatestRates(baseCurrency: String): Result<ExchangeRate>
    fun getHistory(base: String, target: String): Flow<List<ExchangeRate>>
    fun getRecentHistory(base: String, target: String, limit: Int): Flow<List<ExchangeRate>>
    suspend fun saveRateToHistory(rate: ExchangeRate)
    suspend fun clearOldRecords(threshold: Long)
}