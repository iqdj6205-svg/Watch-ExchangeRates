@file:OptIn(com.google.android.horologist.annotations.ExperimentalHorologistApi::class)

package com.serhio.money.tiles

import android.content.Context
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.material3.Typography
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
class CompactCurrencyTileService : BaseCurrencyTileService() {

    override suspend fun buildTile(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile {
        val baseCurrency = settingsManager.baseCurrencyFlow.first()
        val interested = settingsManager.interestedCurrenciesFlow.first()
        val target = interested.firstOrNull() ?: "EUR"

        val rateValue = fetchRate(baseCurrency, target)
        val rateText = if (rateValue != null) {
            val decimals = when {
                rateValue < 0.01 -> 4
                rateValue < 1.0 -> 3
                else -> 2
            }
            String.format(Locale.US, "%.${decimals}f", rateValue)
        } else {
            null
        }

        val layout = createLayout(this, requestParams.deviceConfiguration, target, rateText)
        return createTimeline(layout)
    }

    private fun createLayout(
        context: Context,
        deviceConfiguration: DeviceParameters,
        currency: String,
        rate: String?
    ): LayoutElementBuilders.LayoutElement {
        return materialScope(context, deviceConfiguration) {
            primaryLayout(
                mainSlot = {
                    text(
                        if (rate != null) "$currency $rate".layoutString else "$currency: --".layoutString,
                        typography = Typography.LABEL_SMALL
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
