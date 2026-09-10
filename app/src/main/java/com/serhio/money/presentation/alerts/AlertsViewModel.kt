package com.serhio.money.presentation.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serhio.money.data.repository.AlertRepository
import com.serhio.money.data.settings.SettingsManager
import com.serhio.money.domain.model.Alert
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val alertRepository: AlertRepository,
    private val settingsManager: SettingsManager
) : ViewModel() {

    val alerts = alertRepository.getAllAlerts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val baseCurrency = settingsManager.baseCurrencyFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "USD")

    val alertCurrencies = combine(
        settingsManager.baseCurrencyFlow,
        settingsManager.interestedCurrenciesFlow
    ) { base, interested ->
        val fallback = listOf("EUR", "USD", "PLN", "UAH", "GBP", "CHF", "JPY", "CAD", "AUD", "CNY", "BTC", "XAU")
        (interested + fallback)
            .map { it.trim().uppercase() }
            .filter { it.isNotBlank() && it != base }
            .distinct()
            .take(12)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("EUR", "PLN", "UAH"))

    fun addAlert(currencyCode: String, targetRate: Double, isAbove: Boolean) {
        viewModelScope.launch {
            alertRepository.addAlert(
                Alert(
                    currencyCode = currencyCode.trim().uppercase(),
                    targetRate = targetRate,
                    isAbove = isAbove
                )
            )
        }
    }

    fun toggleAlert(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            alertRepository.toggleAlert(id, enabled)
        }
    }

    fun deleteAlert(alert: Alert) {
        viewModelScope.launch {
            alertRepository.deleteAlert(alert)
        }
    }
}
