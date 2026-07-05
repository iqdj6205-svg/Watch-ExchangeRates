package com.serhio.money.presentation.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serhio.money.data.settings.TileComplicationSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TileComplicationConfigViewModel @Inject constructor(
    private val settings: TileComplicationSettings
) : ViewModel() {

    val tileDisplayCurrency: StateFlow<String> = settings.tileDisplayCurrency.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "EUR"
    )

    val tileDisplayMode: StateFlow<String> = settings.tileDisplayMode.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "rate"
    )

    val complicationDisplayCurrency: StateFlow<String> = settings.complicationDisplayCurrency.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "EUR"
    )

    val complicationDisplayMode: StateFlow<String> = settings.complicationDisplayMode.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "rate"
    )

    fun setTileCurrency(currency: String) {
        viewModelScope.launch { settings.setTileDisplayCurrency(currency) }
    }

    fun setTileMode(mode: String) {
        viewModelScope.launch { settings.setTileDisplayMode(mode) }
    }

    fun setComplicationCurrency(currency: String) {
        viewModelScope.launch { settings.setComplicationDisplayCurrency(currency) }
    }

    fun setComplicationMode(mode: String) {
        viewModelScope.launch { settings.setComplicationDisplayMode(mode) }
    }
}