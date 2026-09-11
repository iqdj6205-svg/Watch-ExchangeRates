package com.serhio.money.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.serhio.money.R
import com.serhio.money.data.repository.AlertRepository
import com.serhio.money.data.settings.SettingsManager
import com.serhio.money.domain.repository.CurrencyRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.util.Locale

@HiltWorker
class AlertWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val alertRepository: AlertRepository,
    private val currencyRepository: CurrencyRepository,
    private val settingsManager: SettingsManager
) : CoroutineWorker(context, params) {
    companion object {
        const val CHANNEL_ID = "rate_alerts"
        const val WORK_NAME = "alert_checker"
    }

    override suspend fun doWork(): Result {
        return try {
            val alerts = alertRepository.getEnabledAlerts()
            if (alerts.isEmpty()) return Result.success()

            val baseCurrency = settingsManager.baseCurrencyFlow.first()
            val result = currencyRepository.fetchLatestRates(baseCurrency)
            if (result.isFailure) return Result.retry()
            val rates = result.getOrThrow().rates

            for (alert in alerts) {
                val currentRate = rates[alert.currencyCode] ?: continue
                val shouldTrigger = if (alert.isAbove) currentRate >= alert.targetRate else currentRate <= alert.targetRate
                if (shouldTrigger) {
                    sendNotification(baseCurrency, alert.currencyCode, alert.targetRate, alert.isAbove, currentRate)
                    alertRepository.markTriggered(alert.id)
                    alertRepository.toggleAlert(alert.id, false)
                    Timber.d("Alert triggered: $baseCurrency/${alert.currencyCode} ${alert.direction} ${alert.targetRate}")
                }
            }
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error in AlertWorker")
            Result.retry()
        }
    }

    private fun sendNotification(baseCurrency: String, currencyCode: String, targetRate: Double, isAbove: Boolean, currentRate: Double) {
        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Timber.w("Skipping alert notification: POST_NOTIFICATIONS not granted")
            return
        }
        val manager = applicationContext.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(CHANNEL_ID, applicationContext.getString(R.string.alerts_title), NotificationManager.IMPORTANCE_HIGH).apply {
            description = applicationContext.getString(R.string.alert_channel_desc)
        }
        manager.createNotificationChannel(channel)
        val direction = applicationContext.getString(if (isAbove) R.string.alert_above else R.string.alert_below).lowercase(Locale.getDefault())
        val rate = String.format(Locale.getDefault(), "%.4f", currentRate)
        val target = String.format(Locale.getDefault(), "%.4f", targetRate)
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_exchange)
            .setContentTitle(applicationContext.getString(R.string.alert_notification_title, baseCurrency, currencyCode))
            .setContentText(applicationContext.getString(R.string.alert_notification_text, baseCurrency, rate, currencyCode, direction, target))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        manager.notify((baseCurrency + currencyCode).hashCode(), notification)
    }
}
