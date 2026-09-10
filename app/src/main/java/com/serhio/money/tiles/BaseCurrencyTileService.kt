package com.serhio.money.tiles

import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.serhio.money.R
import com.serhio.money.data.settings.SettingsManager
import com.serhio.money.domain.repository.CurrencyRepository
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalHorologistApi::class)
abstract class BaseCurrencyTileService : SuspendingTileService() {

    @Inject lateinit var repository: CurrencyRepository
    @Inject lateinit var settingsManager: SettingsManager

    protected suspend fun topFavoriteCurrency(fallback: String = "EUR"): String {
        return settingsManager.interestedCurrenciesFlow.first().firstOrNull() ?: fallback
    }

    protected suspend fun configuredTileCurrency(): String {
        val configured = settingsManager.tileDisplayCurrencyFlow.first()
        return configured.ifBlank { topFavoriteCurrency() }
    }

    protected suspend fun fetchRate(baseCurrency: String, target: String): Double? {
        return withContext(Dispatchers.IO) {
            val result = repository.fetchLatestRates(baseCurrency)
            result.getOrNull()?.rates?.get(target)
        }
    }

    protected suspend fun fetchChangePercent(baseCurrency: String, target: String): Double? {
        val history = repository.getRecentHistory(baseCurrency, target, 2).first()
        if (history.size < 2) return null
        val latest = history[0].rates[target] ?: return null
        val previous = history[1].rates[target] ?: return null
        if (previous == 0.0) return null
        return ((latest - previous) / previous) * 100.0
    }

    protected fun formatRate(rateValue: Double): String {
        val decimals = when {
            rateValue < 0.01 -> 4
            rateValue < 1.0 -> 3
            else -> 2
        }
        return String.format(Locale.US, "%.${decimals}f", rateValue)
    }

    protected fun formatPercent(percent: Double?): String {
        return percent?.let { String.format(Locale.US, "%s%.2f%%", if (it > 0) "+" else "", it) } ?: "--"
    }

    protected suspend fun formatTileValue(mode: String, baseCurrency: String, currency: String, rateValue: Double?): String {
        return when (mode) {
            "date" -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            "arrow" -> fetchChangePercent(baseCurrency, currency)?.let { if (it > 0) "↗" else if (it < 0) "↘" else "→" } ?: "--"
            "change" -> formatPercent(fetchChangePercent(baseCurrency, currency))
            else -> rateValue?.let { formatRate(it) } ?: "--"
        }
    }

    override suspend fun tileRequest(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile {
        return withContext(Dispatchers.IO) { buildTile(requestParams) }
    }

    abstract suspend fun buildTile(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile

    override suspend fun resourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ResourceBuilders.Resources {
        return ResourceBuilders.Resources.Builder()
            .setVersion("2")
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
            .setResourcesVersion("2")
            .setFreshnessIntervalMillis(15 * 60 * 1000)
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
