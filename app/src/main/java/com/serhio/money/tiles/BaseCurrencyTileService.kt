package com.serhio.money.tiles

import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService
import com.serhio.money.data.settings.SettingsManager
import com.serhio.money.domain.repository.CurrencyRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalHorologistApi::class)
@AndroidEntryPoint
abstract class BaseCurrencyTileService : SuspendingTileService() {

    @Inject lateinit var repository: CurrencyRepository
    @Inject lateinit var settingsManager: SettingsManager

    protected suspend fun fetchRate(baseCurrency: String, target: String): Double? {
        return withContext(Dispatchers.IO) {
            repository.fetchLatestRates(baseCurrency)
                .getOrNull()?.rates?.get(target)
        }
    }

    override suspend fun tileRequest(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile {
        return withContext(Dispatchers.IO) {
            buildTile(requestParams)
        }
    }

    abstract suspend fun buildTile(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile

    protected fun createTimeline(element: LayoutElementBuilders.LayoutElement): TileBuilders.Tile {
        return TileBuilders.Tile.Builder()
            .setResourcesVersion("1")
            .setFreshnessIntervalMillis(3600 * 1000)
            .setTileTimeline(
                TimelineBuilders.Timeline.Builder()
                    .addTimelineEntry(
                        TimelineBuilders.TimelineEntry.Builder()
                            .setLayout(
                                LayoutElementBuilders.Layout.Builder()
                                    .setRoot(element)
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build()
    }
}
