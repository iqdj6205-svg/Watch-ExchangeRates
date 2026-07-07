@file:OptIn(com.google.android.horologist.annotations.ExperimentalHorologistApi::class)

package com.serhio.money.tiles

import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import com.google.android.horologist.tiles.SuspendingTileService
import com.serhio.money.data.settings.SettingsManager
import com.serhio.money.domain.repository.CurrencyRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class ChangeCurrencyTileService : SuspendingTileService() {

    @Inject
    lateinit var repository: CurrencyRepository

    @Inject
    lateinit var settingsManager: SettingsManager

    override suspend fun tileRequest(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile {
        val baseCurrency = settingsManager.baseCurrencyFlow.first()
        val interested = settingsManager.interestedCurrenciesFlow.first()
        val target = interested.firstOrNull() ?: "EUR"

        val result = repository.fetchLatestRates(baseCurrency)
        val changeText = if (result.isSuccess) {
            val rate = result.getOrThrow()
            val currentValue = rate.rates[target] ?: 0.0
            val history = repository.getRecentHistory(baseCurrency, target, 2).first()
            val change = if (history.size >= 2) {
                val prev = history.last().rates[target] ?: currentValue
                if (prev != 0.0) ((currentValue - prev) / prev * 100) else 0.0
            } else {
                0.0
            }
            val arrow = if (change > 0) "▲" else if (change < 0) "▼" else "—"
            "$arrow ${String.format(Locale.ROOT, "%.2f", change)}%"
        } else {
            "--%"
        }

        return TileBuilders.Tile.Builder()
            .setResourcesVersion("1")
            .setFreshnessIntervalMillis(1800 * 1000)
            .setTileTimeline(
                TimelineBuilders.Timeline.Builder()
                    .addTimelineEntry(
                        TimelineBuilders.TimelineEntry.Builder()
                            .setLayout(
                                LayoutElementBuilders.Layout.Builder()
                                    .setRoot(createLayout(target, changeText))
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build()
    }

    private fun createLayout(currency: String, change: String): LayoutElementBuilders.LayoutElement {
        return LayoutElementBuilders.Text.Builder()
            .setText("$currency $change")
            .build()
    }

    override suspend fun resourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ResourceBuilders.Resources {
        return ResourceBuilders.Resources.Builder()
            .setVersion("1")
            .build()
    }
}