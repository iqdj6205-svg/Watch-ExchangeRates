package com.serhio.money.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val BASE_CURRENCY = stringPreferencesKey("base_currency")
        private val INTERESTED_CURRENCIES = stringPreferencesKey("interested_currencies_list")
        private val UPDATE_INTERVAL_MS = longPreferencesKey("update_interval_ms")
        private val ALERT_INTERVAL_MS = longPreferencesKey("alert_interval_ms")
        private val LAST_UPDATE_TIMESTAMP = longPreferencesKey("last_update_timestamp")

        private val tileDisplayCurrency = stringPreferencesKey("tile_display_currency")
        private val tileDisplayMode = stringPreferencesKey("tile_display_mode")
        private val complicationDisplayCurrency = stringPreferencesKey("complication_display_currency")
        private val complicationDisplayMode = stringPreferencesKey("complication_display_mode")
    }

    val baseCurrencyFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[BASE_CURRENCY] ?: SettingsLogic.DEFAULT_BASE_CURRENCY
    }

    val interestedCurrenciesFlow: Flow<List<String>> = context.dataStore.data.map { preferences ->
        SettingsLogic.parseCurrencyList(preferences[INTERESTED_CURRENCIES] ?: SettingsLogic.DEFAULT_INTERESTED_CURRENCIES)
    }

    val updateIntervalFlow: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[UPDATE_INTERVAL_MS] ?: SettingsLogic.DEFAULT_UPDATE_INTERVAL_MS
    }

    val alertIntervalFlow: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[ALERT_INTERVAL_MS] ?: SettingsLogic.DEFAULT_ALERT_INTERVAL_MS
    }

    val tileDisplayCurrencyFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[tileDisplayCurrency] ?: "EUR"
    }

    val tileDisplayModeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[tileDisplayMode] ?: "rate"
    }

    val complicationDisplayCurrencyFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[complicationDisplayCurrency] ?: "EUR"
    }

    val complicationDisplayModeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[complicationDisplayMode] ?: "rate"
    }

    suspend fun setBaseCurrency(currency: String) {
        context.dataStore.edit { it[BASE_CURRENCY] = SettingsLogic.normalizeCurrency(currency) }
    }

    suspend fun setInterestedCurrencies(currencies: List<String>) {
        context.dataStore.edit { it[INTERESTED_CURRENCIES] = SettingsLogic.serializeCurrencyList(currencies) }
    }

    suspend fun setUpdateInterval(intervalMs: Long) {
        context.dataStore.edit { it[UPDATE_INTERVAL_MS] = intervalMs }
    }

    suspend fun setAlertInterval(intervalMs: Long) {
        context.dataStore.edit { it[ALERT_INTERVAL_MS] = intervalMs }
    }

    suspend fun updateLastUpdateTimestamp(timestamp: Long) {
        context.dataStore.edit { it[LAST_UPDATE_TIMESTAMP] = timestamp }
    }

    suspend fun setTileDisplayCurrency(currency: String) {
        context.dataStore.edit { it[tileDisplayCurrency] = SettingsLogic.normalizeCurrency(currency) }
    }

    suspend fun setTileDisplayMode(mode: String) {
        context.dataStore.edit { it[tileDisplayMode] = mode }
    }

    suspend fun setComplicationDisplayCurrency(currency: String) {
        context.dataStore.edit { it[complicationDisplayCurrency] = SettingsLogic.normalizeCurrency(currency) }
    }

    suspend fun setComplicationDisplayMode(mode: String) {
        context.dataStore.edit { it[complicationDisplayMode] = mode }
    }
}
