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
class BigNumberCurrencyTileService : SuspendingTileService() {

    @Inject
    lateinit var repository: CurrencyRepository

    @Inject
    lateinit var settingsManager: SettingsManager

    override suspend fun tileRequest(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile {
        val baseCurrency = settingsManager.baseCurrencyFlow.first()
        val interested = settingsManager.interestedCurrenciesFlow.first()
        val target = interested.firstOrNull() ?: "EUR"

        val result = repository.fetchLatestRates(baseCurrency)
        val rateText = if (result.isSuccess) {
            val rate = result.getOrThrow().rates[target]
            String.format(Locale.ROOT, "%.2f", rate ?: 0.0)
        } else {
            "--"
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
                                    .setRoot(createLayout(rateText))
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build()
    }

    private fun createLayout(rate: String): LayoutElementBuilders.LayoutElement {
        return LayoutElementBuilders.Text.Builder()
            .setText(rate)
            .build()
    }

    override suspend fun resourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ResourceBuilders.Resources {
        return ResourceBuilders.Resources.Builder()
            .setVersion("1")
            .build()
    }
}