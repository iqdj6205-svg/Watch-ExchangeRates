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
class ChangeCurrencyTileService : BaseCurrencyTileService() {

    override suspend fun buildTile(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile {
        val baseCurrency = settingsManager.baseCurrencyFlow.first()
        val interested = settingsManager.interestedCurrenciesFlow.first()
        val target = interested.firstOrNull() ?: "EUR"

        val changeText = if (baseCurrency == "USD") {
            val history = repository.getRecentHistory("USD", target, 2).first()
            if (history.size >= 2) {
                val last = history[0].rates[target] ?: 0.0
                val prev = history[1].rates[target] ?: 0.0
                val diff = last - prev
                val prefix = if (diff > 0) "+" else ""
                String.format(Locale.US, "%s%.2f", prefix, diff)
            } else {
                null
            }
        } else {
            val targetHistory = repository.getRecentHistory("USD", target, 2).first()
            val baseHistory = repository.getRecentHistory("USD", baseCurrency, 2).first()
            if (targetHistory.size >= 2 && baseHistory.size >= 2) {
                val lastTarget = targetHistory[0].rates[target] ?: 0.0
                val lastBase = baseHistory[0].rates[baseCurrency] ?: 1.0
                val prevTarget = targetHistory[1].rates[target] ?: 0.0
                val prevBase = baseHistory[1].rates[baseCurrency] ?: 1.0
                val last = lastTarget / lastBase
                val prev = prevTarget / prevBase
                val diff = last - prev
                val prefix = if (diff > 0) "+" else ""
                String.format(Locale.US, "%s%.2f", prefix, diff)
            } else {
                null
            }
        }

        val layout = createLayout(this, requestParams.deviceConfiguration, target, changeText)
        return createTimeline(layout)
    }

    private fun createLayout(
        context: Context,
        deviceConfiguration: DeviceParameters,
        currency: String,
        change: String?
    ): LayoutElementBuilders.LayoutElement {
        return materialScope(context, deviceConfiguration) {
            primaryLayout(
                mainSlot = {
                    text(
                        if (change != null) "$currency change: $change".layoutString else "$currency: --".layoutString
                    )
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
