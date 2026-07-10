@file:OptIn(com.google.android.horologist.annotations.ExperimentalHorologistApi::class)

package com.serhio.money.tiles

import android.content.Context
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import java.util.Locale

@AndroidEntryPoint
class MultiCurrencyTileService : BaseCurrencyTileService() {

    override suspend fun buildTile(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile {
        val baseCurrency = settingsManager.baseCurrencyFlow.first()
        val interested = settingsManager.interestedCurrenciesFlow.first()

        val usdResult = repository.fetchLatestRates()
        val usdRates = usdResult.getOrNull()?.rates ?: emptyMap()
        val baseRate = usdRates[baseCurrency] ?: 1.0
        val displayRates = if (baseCurrency == "USD") {
            usdRates
        } else {
            usdRates.mapValues { (_, v) -> v / baseRate }
        }

        val layout = createLayout(this, requestParams.deviceConfiguration, interested, displayRates)
        return createTimeline(layout)
    }

    private fun createLayout(
        context: Context,
        deviceConfiguration: DeviceParameters,
        interested: List<String>,
        rates: Map<String, Double>
    ): LayoutElementBuilders.LayoutElement {
        return materialScope(context, deviceConfiguration) {
            primaryLayout(
                mainSlot = {
                    val column = LayoutElementBuilders.Column.Builder()
                    val maxItems = if (interested.size > 4) 4 else interested.size
                    interested.take(maxItems).forEach { target ->
                        val rateValue = rates[target]
                        val valStr = if (rateValue != null) {
                            String.format(Locale.US, "%.2f", rateValue)
                        } else {
                            "No data"
                        }
                        column.addContent(
                            text("$target: $valStr".layoutString)
                        )
                    }
                    if (interested.size > 4) {
                        column.addContent(
                            text("+${interested.size - 4} more".layoutString)
                        )
                    }
                    column.build()
                }
            )
        }
    }

    override suspend fun resourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ResourceBuilders.Resources {
        return ResourceBuilders.Resources.Builder()
            .setVersion("1")
            .build()
    }
}
