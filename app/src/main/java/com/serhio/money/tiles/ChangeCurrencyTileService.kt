@file:OptIn(com.google.android.horologist.annotations.ExperimentalHorologistApi::class)

package com.serhio.money.tiles

import android.content.Context
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.material3.Typography
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.material3.textEdgeButton
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

        val history = repository.getRecentHistory(baseCurrency, target, 2).first()
        val changeText = if (history.size >= 2) {
            val last = history[0].rates[target] ?: 0.0
            val prev = history[1].rates[target] ?: 0.0
            val diff = last - prev
            val prefix = if (diff > 0) "+" else ""
            String.format(Locale.US, "%s%.2f", prefix, diff)
        } else {
            null
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
        val openAction = ActionBuilders.LaunchAction.Builder()
            .setAndroidActivity(
                ActionBuilders.AndroidActivity.Builder()
                    .setPackageName(context.packageName)
                    .setClassName("${context.packageName}.presentation.MainActivity")
                    .build()
            )
            .build()

        val onClick = ModifiersBuilders.Clickable.Builder()
            .setId("open_app")
            .setOnClick(openAction)
            .build()

        return materialScope(context, deviceConfiguration) {
            primaryLayout(
                titleSlot = {
                    text(currency.layoutString, typography = Typography.TITLE_MEDIUM)
                },
                mainSlot = {
                    text(
                        (change ?: "--").layoutString,
                        typography = Typography.DISPLAY_MEDIUM
                    )
                },
                bottomSlot = {
                    textEdgeButton(onClick = onClick) {
                        text("Open".layoutString)
                    }
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
