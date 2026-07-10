package com.serhio.money.data.repository

import com.serhio.money.data.database.CurrencyDao
import com.serhio.money.data.database.entities.CurrencyHistoryEntity
import com.serhio.money.data.network.ExchangeRateApi
import com.serhio.money.data.repository.mapper.ExchangeRateMapper
import com.serhio.money.domain.model.ExchangeRate
import com.serhio.money.domain.repository.CurrencyRepository
import com.serhio.money.utils.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyRepositoryImpl @Inject constructor(
    private val api: ExchangeRateApi,
    private val dao: CurrencyDao,
    private val mapper: ExchangeRateMapper,
    private val networkMonitor: NetworkMonitor
) : CurrencyRepository {

    override suspend fun fetchLatestRates(): Result<ExchangeRate> {
        return fetchLatestRates("USD")
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override suspend fun fetchLatestRates(baseCurrency: String): Result<ExchangeRate> {
        val isOnline = networkMonitor.isOnline.first()

        if (!isOnline) {
            return loadFromCache(baseCurrency)
        }

        return try {
            val response = api.getLatestRates(baseCurrency)
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                val domainModel = mapper.toDomain(dto)
                // Удалено автоматическое сохранение в БД всех курсов подряд
                Result.success(domainModel)
            } else {
                Timber.w("API error: ${response.code()} ${response.message()}")
                val cached = loadFromCache(baseCurrency)
                if (cached.isSuccess) {
                    cached
                } else {
                    Result.failure(Exception("API error ${response.code()}: ${response.message()}"))
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Network request failed")
            val cached = loadFromCache(baseCurrency)
            if (cached.isSuccess) {
                cached
            } else {
                Result.failure(e)
            }
        }
    }

    private suspend fun loadFromCache(baseCurrency: String): Result<ExchangeRate> {
        return try {
            val cached = dao.getCachedRates(baseCurrency)
            if (cached.isNotEmpty()) {
                val rates = cached.associate { it.targetCurrency to it.rate }
                val latestTimestamp = cached.maxOf { it.timestamp }
                Timber.d("Loaded ${rates.size} rates from cache for $baseCurrency")
                Result.success(
                    ExchangeRate(
                        baseCurrency = baseCurrency,
                        rates = rates,
                        lastUpdate = latestTimestamp
                    )
                )
            } else {
                Result.failure(Exception("No cached data available for $baseCurrency"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getHistory(base: String, target: String): Flow<List<ExchangeRate>> {
        return dao.getHistoryForPair(base, target).map { entities ->
            entities.map { entity ->
                ExchangeRate(
                    baseCurrency = entity.baseCurrency,
                    rates = mapOf(entity.targetCurrency to entity.rate),
                    lastUpdate = entity.timestamp
                )
            }
        }
    }

    override fun getRecentHistory(base: String, target: String, limit: Int): Flow<List<ExchangeRate>> {
        return dao.getRecentHistory(base, target, limit).map { entities ->
            entities.map { entity ->
                ExchangeRate(
                    baseCurrency = entity.baseCurrency,
                    rates = mapOf(entity.targetCurrency to entity.rate),
                    lastUpdate = entity.timestamp
                )
            }
        }
    }

    override suspend fun saveRates(rate: ExchangeRate) {
        saveRateToHistory(rate)
    }

    override suspend fun saveRateToHistory(rate: ExchangeRate) {
        rate.rates.forEach { (targetCurrency, value) ->
            val entity = CurrencyHistoryEntity(
                timestamp = rate.lastUpdate,
                baseCurrency = rate.baseCurrency,
                targetCurrency = targetCurrency,
                rate = value,
                source = "exchangerate.fun"
            )
            dao.insertHistoryRecord(entity)
        }
    }

    override fun getRecentHistory(target: String, limit: Int): Flow<List<ExchangeRate>> {
        return getRecentHistory("USD", target, limit)
    }

    override suspend fun clearOldRecords(threshold: Long) {
        dao.deleteOldRecords(threshold)
    }
}