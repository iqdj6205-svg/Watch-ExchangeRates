package com.serhio.money.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.tileStore by preferencesDataStore(name = "tile_settings")

@Singleton
class TileComplicationSettings @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val TILE_DISPLAY_CURRENCY = stringPreferencesKey("tile_display_currency")
        private val TILE_DISPLAY_MODE = stringPreferencesKey("tile_display_mode")
        private val COMPLICATION_DISPLAY_CURRENCY = stringPreferencesKey("complication_display_currency")
        private val COMPLICATION_DISPLAY_MODE = stringPreferencesKey("complication_display_mode")
    }

    val tileDisplayCurrency: Flow<String> = context.tileStore.data.map { prefs ->
        prefs[TILE_DISPLAY_CURRENCY] ?: "EUR"
    }

    val tileDisplayMode: Flow<String> = context.tileStore.data.map { prefs ->
        prefs[TILE_DISPLAY_MODE] ?: "rate"
    }

    val complicationDisplayCurrency: Flow<String> = context.tileStore.data.map { prefs ->
        prefs[COMPLICATION_DISPLAY_CURRENCY] ?: "EUR"
    }

    val complicationDisplayMode: Flow<String> = context.tileStore.data.map { prefs ->
        prefs[COMPLICATION_DISPLAY_MODE] ?: "rate"
    }

    suspend fun setTileDisplayCurrency(currency: String) {
        context.tileStore.edit { it[TILE_DISPLAY_CURRENCY] = currency }
    }

    suspend fun setTileDisplayMode(mode: String) {
        context.tileStore.edit { it[TILE_DISPLAY_MODE] = mode }
    }

    suspend fun setComplicationDisplayCurrency(currency: String) {
        context.tileStore.edit { it[COMPLICATION_DISPLAY_CURRENCY] = currency }
    }

    suspend fun setComplicationDisplayMode(mode: String) {
        context.tileStore.edit { it[COMPLICATION_DISPLAY_MODE] = mode }
    }
}