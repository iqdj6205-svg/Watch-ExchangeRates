package com.serhio.money.presentation.config

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.wear.tiles.TileService
import com.serhio.money.data.settings.SettingsManager
import com.serhio.money.tiles.BigNumberCurrencyTileService
import com.serhio.money.tiles.ChangeCurrencyTileService
import com.serhio.money.tiles.CompactCurrencyTileService
import com.serhio.money.tiles.CurrencyTileService
import com.serhio.money.tiles.MultiCurrencyTileService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TileComplicationConfigViewModel @Inject constructor(
    private val application: Application,
    private val settingsManager: SettingsManager
) : AndroidViewModel(application) {

    val tileDisplayCurrency: StateFlow<String> = settingsManager.tileDisplayCurrencyFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "EUR"
    )

    val tileDisplayMode: StateFlow<String> = settingsManager.tileDisplayModeFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "rate"
    )

    val complicationDisplayCurrency: StateFlow<String> = settingsManager.complicationDisplayCurrencyFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "EUR"
    )

    val complicationDisplayMode: StateFlow<String> = settingsManager.complicationDisplayModeFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "rate"
    )

    private val tileUpdater = TileService.getUpdater(application)

    private fun requestTileUpdates() {
        tileUpdater.requestUpdate(CurrencyTileService::class.java)
        tileUpdater.requestUpdate(MultiCurrencyTileService::class.java)
        tileUpdater.requestUpdate(BigNumberCurrencyTileService::class.java)
        tileUpdater.requestUpdate(CompactCurrencyTileService::class.java)
        tileUpdater.requestUpdate(ChangeCurrencyTileService::class.java)
    }

    fun setTileCurrency(currency: String) {
        viewModelScope.launch {
            settingsManager.setTileDisplayCurrency(currency)
            requestTileUpdates()
        }
    }

    fun setTileMode(mode: String) {
        viewModelScope.launch {
            settingsManager.setTileDisplayMode(mode)
            requestTileUpdates()
        }
    }

    fun setComplicationCurrency(currency: String) {
        viewModelScope.launch {
            settingsManager.setComplicationDisplayCurrency(currency)
            // The complication currently follows the first favorite currency.
            // Keep this value stored for future explicit complication modes, but refresh tiles now.
            requestTileUpdates()
        }
    }

    fun setComplicationMode(mode: String) {
        viewModelScope.launch {
            settingsManager.setComplicationDisplayMode(mode)
            requestTileUpdates()
        }
    }
}
