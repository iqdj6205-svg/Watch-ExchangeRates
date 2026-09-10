package com.serhio.money.tiles

import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService
import com.serhio.money.R
import com.serhio.money.data.settings.SettingsManager
import com.serhio.money.domain.repository.CurrencyRepository
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalHorologistApi::class)
abstract class BaseCurrencyTileService : SuspendingTileService() {

    @Inject lateinit var repository: CurrencyRepository
    @Inject lateinit var settingsManager: SettingsManager

    protected suspend fun fetchRate(baseCurrency: String, target: String): Double? {
        return withContext(Dispatchers.IO) {
            val result = repository.fetchLatestRates(baseCurrency)
            result.getOrNull()?.rates?.get(target)
        }
    }

    protected fun formatRate(rateValue: Double): String {
        val decimals = when {
            rateValue < 0.01 -> 4
            rateValue < 1.0 -> 3
            else -> 2
        }
        return String.format(Locale.US, "%.${decimals}f", rateValue)
    }

    protected fun formatTileValue(mode: String, currency: String, rateValue: Double?): String {
        return when (mode) {
            "date" -> java.text.SimpleDateFormat("HH:mm", Locale.getDefault()).format(java.util.Date())
            "arrow" -> if (rateValue == null) "--" else "↗"
            "change" -> if (rateValue == null) "--" else formatRate(rateValue)
            else -> rateValue?.let { formatRate(it) } ?: "--"
        }
    }

    override suspend fun tileRequest(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile {
        return withContext(Dispatchers.IO) { buildTile(requestParams) }
    }

    abstract suspend fun buildTile(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile

    override suspend fun resourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ResourceBuilders.Resources {
        return ResourceBuilders.Resources.Builder()
            .setVersion("1")
            .addIdToImageMapping(
                "icon_exchange",
                ResourceBuilders.ImageResource.Builder()
                    .setAndroidResourceByResId(
                        ResourceBuilders.AndroidImageResourceByResId.Builder()
                            .setResourceId(R.drawable.ic_exchange)
                            .build()
                    )
                    .build()
            )
            .build()
    }

    protected fun createTimeline(element: LayoutElementBuilders.LayoutElement): TileBuilders.Tile {
        return TileBuilders.Tile.Builder()
            .setResourcesVersion("1")
            .setFreshnessIntervalMillis(3600 * 1000)
            .setTileTimeline(
                TimelineBuilders.Timeline.Builder()
                    .addTimelineEntry(
                        TimelineBuilders.TimelineEntry.Builder()
                            .setLayout(LayoutElementBuilders.Layout.Builder().setRoot(element).build())
                            .build()
                    )
                    .build()
            )
            .build()
    }
}
