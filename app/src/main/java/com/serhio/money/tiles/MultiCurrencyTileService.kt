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
import javax.inject.Inject

@AndroidEntryPoint
class MultiCurrencyTileService : SuspendingTileService() {

    @Inject
    lateinit var repository: CurrencyRepository

    @Inject
    lateinit var settingsManager: SettingsManager

    override suspend fun tileRequest(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile {
        val baseCurrency = settingsManager.baseCurrencyFlow.first()
        val interested = settingsManager.interestedCurrenciesFlow.first()
        
        val result = repository.fetchLatestRates(baseCurrency)
        val column = LayoutElementBuilders.Column.Builder()

        if (result.isSuccess) {
            val rates = result.getOrThrow().rates
            interested.take(3).forEach { target ->
                val valStr = String.format("%.2f", rates[target] ?: 0.0)
                column.addContent(
                    LayoutElementBuilders.Text.Builder()
                        .setText("$target: $valStr")
                        .build()
                )
            }
        } else {
            column.addContent(
                LayoutElementBuilders.Text.Builder().setText("Error").build()
            )
        }

        return TileBuilders.Tile.Builder()
            .setResourcesVersion("1")
            .setFreshnessIntervalMillis(3600 * 1000)
            .setTileTimeline(
                TimelineBuilders.Timeline.Builder()
                    .addTimelineEntry(
                        TimelineBuilders.TimelineEntry.Builder()
                            .setLayout(
                                LayoutElementBuilders.Layout.Builder()
                                    .setRoot(column.build())
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build()
    }

    override suspend fun resourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ResourceBuilders.Resources {
        return ResourceBuilders.Resources.Builder()
            .setVersion("1")
            .build()
    }
}
