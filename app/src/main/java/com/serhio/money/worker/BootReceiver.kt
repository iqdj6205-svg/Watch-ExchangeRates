package com.serhio.money.worker

import android.content.Context
import android.content.Intent
import com.serhio.money.utils.WorkerUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.content.BroadcastReceiver
import com.serhio.money.data.settings.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var settingsManager: SettingsManager

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                try {
                    val interval = settingsManager.updateIntervalFlow.first()
                    WorkerUtils.schedulePeriodicUpdate(context, interval)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
