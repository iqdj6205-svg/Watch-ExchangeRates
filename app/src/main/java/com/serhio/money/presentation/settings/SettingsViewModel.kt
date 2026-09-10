package com.serhio.money.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.wear.tiles.TileService
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

    val baseCurrency: StateFlow<String> = settingsManager.baseCurrencyFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "USD"
    )

    val interestedCurrencies: StateFlow<List<String>> = settingsManager.interestedCurrenciesFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("EUR", "PLN", "UAH")
    )

    val updateInterval: StateFlow<Long> = settingsManager.updateIntervalFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 3600 * 1000L
    )

    private val tileUpdater = TileService.getUpdater(context)

    private fun requestTileUpdates() {
        tileUpdater.requestUpdate(CurrencyTileService::class.java)
        tileUpdater.requestUpdate(MultiCurrencyTileService::class.java)
        tileUpdater.requestUpdate(BigNumberCurrencyTileService::class.java)
        tileUpdater.requestUpdate(CompactCurrencyTileService::class.java)
        tileUpdater.requestUpdate(ChangeCurrencyTileService::class.java)
    }

    fun setBaseCurrency(currency: String) {
        viewModelScope.launch {
            settingsManager.setBaseCurrency(currency)
            requestTileUpdates()
        }
    }

    fun setUpdateInterval(intervalMs: Long) {
        viewModelScope.launch {
            settingsManager.setUpdateInterval(intervalMs)
            WorkerUtils.schedulePeriodicUpdate(context, intervalMs)
            requestTileUpdates()
        }
    }

    private var pendingOrder: List<String>? = null

    fun toggleInterestedCurrency(currency: String) {
        val current = (pendingOrder ?: interestedCurrencies.value).toMutableList()
        if (current.contains(currency)) {
            current.remove(currency)
        } else {
            current.add(currency)
        }
        pendingOrder = current
        viewModelScope.launch {
            settingsManager.setInterestedCurrencies(current)
            requestTileUpdates()
        }
    }

    fun reorderCurrencies(newOrder: List<String>) {
        val existing = pendingOrder ?: interestedCurrencies.value
        val merged = newOrder + existing.filter { it !in newOrder }
        pendingOrder = merged
        viewModelScope.launch {
            settingsManager.setInterestedCurrencies(merged)
            requestTileUpdates()
        }
    }
}
