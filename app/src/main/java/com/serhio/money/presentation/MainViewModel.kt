package com.serhio.money.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serhio.money.data.settings.SettingsManager
import com.serhio.money.domain.model.ExchangeRate
import com.serhio.money.domain.repository.CurrencyRepository
import com.serhio.money.utils.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: CurrencyRepository,
    private val settingsManager: SettingsManager,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )

    init {
        observeRates()
    }

    private fun observeRates() {
        viewModelScope.launch {
            combine(
                settingsManager.baseCurrencyFlow,
                settingsManager.interestedCurrenciesFlow
            ) { base, interested ->
                Pair(base, interested)
            }.collect { (base, interested) ->
                refreshRates(base, interested)
            }
        }
    }

    fun refreshRates(base: String? = null, interested: List<String>? = null) {
        viewModelScope.launch {
            _uiState.value = MainUiState.Loading

            val currentBase = base ?: settingsManager.baseCurrencyFlow.first()
            val currentInterested = interested ?: settingsManager.interestedCurrenciesFlow.first()

            val result = repository.fetchLatestRates()

            if (result.isSuccess) {
                val rate = result.getOrThrow()
                val isFromCache = rate.lastUpdate < System.currentTimeMillis() - 60000

                val usdRates = rate.rates
                val baseRate = usdRates[currentBase] ?: 1.0
                val displayRates = if (currentBase == "USD") {
                    usdRates
                } else {
                    usdRates.mapValues { (_, v) -> v / baseRate }
                }

                val historyMap = mutableMapOf<String, List<Double>>()
                currentInterested.forEach { target ->
                    if (currentBase == "USD") {
                        val historyEntities = repository.getRecentHistory("USD", target, 50).first()
                        historyMap[target] = historyEntities.mapNotNull { it.rates[target] }.reversed()
                    } else {
                        val targetHistory = repository.getRecentHistory("USD", target, 50).first()
                        val baseHistory = repository.getRecentHistory("USD", currentBase, 50).first()
                        val historyValues = targetHistory.mapNotNull { targetEntry ->
                            val baseEntry = baseHistory.minByOrNull {
                                kotlin.math.abs(it.lastUpdate - targetEntry.lastUpdate)
                            }
                            val br = baseEntry?.rates?.get(currentBase) ?: return@mapNotNull null
                            val tr = targetEntry.rates[target] ?: return@mapNotNull null
                            tr / br
                        }
                        historyMap[target] = historyValues.reversed()
                    }
                }

                _uiState.value = MainUiState.Success(
                    baseCurrency = currentBase,
                    rates = displayRates,
                    interestedCurrencies = currentInterested,
                    lastUpdate = rate.lastUpdate,
                    history = historyMap,
                    isFromCache = isFromCache
                )

                if (!isFromCache) {
                    repository.saveRates(rate)
                }
            } else {
                val errorMsg = result.exceptionOrNull()?.message
                _uiState.value = MainUiState.Error(errorMsg)
            }
        }
    }
}

sealed class MainUiState {
    object Loading : MainUiState()
    data class Success(
        val baseCurrency: String,
        val rates: Map<String, Double>,
        val interestedCurrencies: List<String>,
        val lastUpdate: Long,
        val history: Map<String, List<Double>> = emptyMap(),
        val isFromCache: Boolean = false
    ) : MainUiState()
    data class Error(val message: String? = null) : MainUiState()
}