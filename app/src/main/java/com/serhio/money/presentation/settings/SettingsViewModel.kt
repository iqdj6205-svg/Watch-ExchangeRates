package com.serhio.money.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.wear.tiles.TileService
import com.serhio.money.data.settings.SettingsLogic
import com.serhio.money.data.settings.SettingsManager
import com.serhio.money.tiles.BigNumberCurrencyTileService
import com.serhio.money.tiles.ChangeCurrencyTileService
import com.serhio.money.tiles.CompactCurrencyTileService
import com.serhio.money.tiles.CurrencyTileService
import com.serhio.money.tiles.MultiCurrencyTileService
import com.serhio.money.utils.WorkerUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsManager: SettingsManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val baseCurrency: StateFlow<String> = settingsManager.baseCurrencyFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsLogic.DEFAULT_BASE_CURRENCY)
    val interestedCurrencies: StateFlow<List<String>> = settingsManager.interestedCurrenciesFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsLogic.parseCurrencyList(SettingsLogic.DEFAULT_INTERESTED_CURRENCIES))
    val updateInterval: StateFlow<Long> = settingsManager.updateIntervalFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsLogic.DEFAULT_UPDATE_INTERVAL_MS)
    val alertInterval: StateFlow<Long> = settingsManager.alertIntervalFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsLogic.DEFAULT_ALERT_INTERVAL_MS)

    private val tileUpdater = TileService.getUpdater(context)

    private fun requestTileUpdates() {
        tileUpdater.requestUpdate(CurrencyTileService::class.java)
        tileUpdater.requestUpdate(MultiCurrencyTileService::class.java)
        tileUpdater.requestUpdate(BigNumberCurrencyTileService::class.java)
        tileUpdater.requestUpdate(CompactCurrencyTileService::class.java)
        tileUpdater.requestUpdate(ChangeCurrencyTileService::class.java)
    }

    fun setBaseCurrency(currency: String) { viewModelScope.launch { settingsManager.setBaseCurrency(currency); requestTileUpdates() } }

    fun setUpdateInterval(intervalMs: Long) { viewModelScope.launch { settingsManager.setUpdateInterval(intervalMs); WorkerUtils.schedulePeriodicUpdate(context, intervalMs); requestTileUpdates() } }

    fun setAlertInterval(intervalMs: Long) { viewModelScope.launch { settingsManager.setAlertInterval(intervalMs); WorkerUtils.scheduleAlertCheck(context, intervalMs) } }

    private var pendingOrder: List<String>? = null

    fun toggleInterestedCurrency(currency: String) {
        val normalizedCurrency = SettingsLogic.normalizeCurrency(currency)
        val current = (pendingOrder ?: interestedCurrencies.value).toMutableList()
        if (current.contains(normalizedCurrency)) current.remove(normalizedCurrency) else current.add(normalizedCurrency)
        pendingOrder = SettingsLogic.normalizeCurrencies(current)
        viewModelScope.launch { settingsManager.setInterestedCurrencies(current); requestTileUpdates() }
    }

    fun reorderCurrencies(newOrder: List<String>) {
        val existing = pendingOrder ?: interestedCurrencies.value
        val merged = SettingsLogic.mergeReorderedCurrencies(newOrder, existing)
        pendingOrder = merged
        viewModelScope.launch { settingsManager.setInterestedCurrencies(merged); requestTileUpdates() }
    }
}
