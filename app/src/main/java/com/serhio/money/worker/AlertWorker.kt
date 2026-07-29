package com.serhio.money.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.serhio.money.R
import com.serhio.money.data.repository.AlertRepository
import com.serhio.money.domain.repository.CurrencyRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.Locale

@HiltWorker
class AlertWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val alertRepository: AlertRepository,
    private val currencyRepository: CurrencyRepository
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "rate_alerts"
        const val WORK_NAME = "alert_checker"
    }

    override suspend fun doWork(): Result {
        return try {
            val alerts = alertRepository.getEnabledAlerts()
            if (alerts.isEmpty()) return Result.success()

            val result = currencyRepository.fetchLatestRates()
            if (result.isFailure) return Result.retry()

            val rates = result.getOrThrow().rates

            for (alert in alerts) {
                val currentRate = rates[alert.currencyCode] ?: continue
                val shouldTrigger = if (alert.isAbove) {
                    currentRate >= alert.targetRate
                } else {
                    currentRate <= alert.targetRate
                }

                if (shouldTrigger) {
                    sendNotification(alert.currencyCode, alert.targetRate, alert.isAbove, currentRate)
                    alertRepository.markTriggered(alert.id)
                    alertRepository.toggleAlert(alert.id, false)
                    Timber.d("Alert triggered: ${alert.currencyCode} ${alert.direction} ${alert.targetRate}")
                }
            }

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error in AlertWorker")
            Result.failure()
        }
    }

    private fun sendNotification(currencyCode: String, targetRate: Double, isAbove: Boolean, currentRate: Double) {
        val manager = applicationContext.getSystemService(NotificationManager::class.java) ?: return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Rate Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications when exchange rates cross your target"
        }
        manager.createNotificationChannel(channel)

        val direction = if (isAbove) "above" else "below"
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_exchange)
            .setContentTitle("Rate Alert: $currencyCode")
            .setContentText("1 USD is now ${String.format(Locale.US, "%.4f", currentRate)} $currencyCode (was $direction ${String.format(Locale.US, "%.4f", targetRate)})")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(currencyCode.hashCode(), notification)
    }
}
