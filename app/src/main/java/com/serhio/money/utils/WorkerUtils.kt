package com.serhio.money.utils

import android.content.Context
import androidx.work.*
import com.serhio.money.worker.ExchangeRateWorker
import java.util.concurrent.TimeUnit

object WorkerUtils {
    private const val WORK_NAME = "ExchangeRateUpdateWork"

    fun schedulePeriodicUpdate(context: Context, intervalMs: Long) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<ExchangeRateWorker>(
            intervalMs, TimeUnit.MILLISECONDS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}
