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

    val interestedCurrencies: StateFlow<List<String>> = settingsManager.interestedCurrenciesFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("EUR", "PLN", "UAH")
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
        }
    }

    fun reorderCurrencies(newOrder: List<String>) {
        val existing = pendingOrder ?: interestedCurrencies.value
        val merged = newOrder + existing.filter { it !in newOrder }
        pendingOrder = merged
        viewModelScope.launch {
            settingsManager.setInterestedCurrencies(merged)
        }
    }
}