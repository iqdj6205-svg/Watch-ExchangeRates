package com.serhio.money.presentation.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serhio.money.data.repository.AlertRepository
import com.serhio.money.domain.model.Alert
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val alertRepository: AlertRepository
) : ViewModel() {

    val alerts = alertRepository.getAllAlerts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addAlert(currencyCode: String, targetRate: Double, isAbove: Boolean) {
        viewModelScope.launch {
            alertRepository.addAlert(
                Alert(
                    currencyCode = currencyCode,
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
