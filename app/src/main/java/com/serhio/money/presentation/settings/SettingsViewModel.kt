package com.serhio.money.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serhio.money.data.settings.SettingsManager
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

    val interestedCurrencies: StateFlow<Set<String>> = settingsManager.interestedCurrenciesFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), setOf("EUR", "PLN", "UAH")
    )

    val updateInterval: StateFlow<Long> = settingsManager.updateIntervalFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 3600 * 1000L
    )

    fun setBaseCurrency(currency: String) {
        viewModelScope.launch {
            settingsManager.setBaseCurrency(currency)
        }
    }

    fun setUpdateInterval(intervalMs: Long) {
        viewModelScope.launch {
            settingsManager.setUpdateInterval(intervalMs)
            WorkerUtils.schedulePeriodicUpdate(context, intervalMs)
        }
    }

    fun toggleInterestedCurrency(currency: String) {
        viewModelScope.launch {
            val current = interestedCurrencies.value.toMutableSet()
            if (current.contains(currency)) {
                current.remove(currency)
            } else {
                current.add(currency)
            }
            settingsManager.setInterestedCurrencies(current)
        }
    }
}