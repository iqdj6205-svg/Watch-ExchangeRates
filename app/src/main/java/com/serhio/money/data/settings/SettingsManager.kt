package com.serhio.money.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
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
        private val INTERESTED_CURRENCIES = stringSetPreferencesKey("interested_currencies")
        private val UPDATE_INTERVAL_MS = longPreferencesKey("update_interval_ms")
        private val LAST_UPDATE_TIMESTAMP = longPreferencesKey("last_update_timestamp")
    }

    val baseCurrencyFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[BASE_CURRENCY] ?: "USD"
    }

    val interestedCurrenciesFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[INTERESTED_CURRENCIES] ?: setOf("EUR", "PLN", "UAH")
    }

    val updateIntervalFlow: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[UPDATE_INTERVAL_MS] ?: (3600 * 1000L) // По умолчанию 1 час
    }

    suspend fun setBaseCurrency(currency: String) {
        context.dataStore.edit { it[BASE_CURRENCY] = currency }
    }

    suspend fun setInterestedCurrencies(currencies: Set<String>) {
        context.dataStore.edit { it[INTERESTED_CURRENCIES] = currencies }
    }

    suspend fun setUpdateInterval(intervalMs: Long) {
        context.dataStore.edit { it[UPDATE_INTERVAL_MS] = intervalMs }
    }

    suspend fun updateLastUpdateTimestamp(timestamp: Long) {
        context.dataStore.edit { it[LAST_UPDATE_TIMESTAMP] = timestamp }
    }
}
