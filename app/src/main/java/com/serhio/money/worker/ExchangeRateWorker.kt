package com.serhio.money.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.serhio.money.data.settings.SettingsManager
import com.serhio.money.domain.repository.CurrencyRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber

@HiltWorker
class ExchangeRateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: CurrencyRepository,
    private val settingsManager: SettingsManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val baseCurrency = settingsManager.baseCurrencyFlow.first()
            val interestedCurrencies = settingsManager.interestedCurrenciesFlow.first()
            
            Timber.d("Starting background update for base=$baseCurrency")
            
            val result = repository.fetchLatestRates(baseCurrency)
            
            if (result.isSuccess) {
                val rate = result.getOrThrow()
                val filteredRates = rate.rates.filterKeys { it in interestedCurrencies }
                val filteredRate = rate.copy(baseCurrency = baseCurrency, rates = filteredRates)
                
                repository.saveRates(filteredRate)
                settingsManager.updateLastUpdateTimestamp(System.currentTimeMillis())
                
                Timber.d("Background update successful")
                Result.success()
            } else {
                Timber.e("Background update failed: ${result.exceptionOrNull()?.message}")
                Result.retry()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error in ExchangeRateWorker")
            Result.failure()
        }
    }
}
