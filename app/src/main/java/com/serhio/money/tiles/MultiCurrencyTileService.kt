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
class MultiCurrencyTileService : BaseCurrencyTileService() {

    override suspend fun buildTile(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile {
        val baseCurrency = settingsManager.baseCurrencyFlow.first()
        val interested = settingsManager.interestedCurrenciesFlow.first()

        val result = repository.fetchLatestRates(baseCurrency)
        val rates = result.getOrNull()?.rates ?: emptyMap()

        val layout = createLayout(this, requestParams.deviceConfiguration, baseCurrency, interested, rates)
        return createTimeline(layout)
    }

    private fun createLayout(
        context: Context,
        deviceConfiguration: DeviceParameters,
        baseCurrency: String,
        interested: List<String>,
        rates: Map<String, Double>
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
                    text(baseCurrency.layoutString, typography = Typography.TITLE_MEDIUM)
                },
                mainSlot = {
                    val column = LayoutElementBuilders.Column.Builder()
                    val maxItems = if (interested.size > 4) 4 else interested.size
                    interested.take(maxItems).forEach { target ->
                        val rateValue = rates[target]
                        val valStr = if (rateValue != null) {
                            String.format(Locale.US, "%.2f", rateValue)
                        } else {
                            "--"
                        }
                        column.addContent(
                            text("$target: $valStr".layoutString, typography = Typography.BODY_LARGE)
                        )
                    }
                    if (interested.size > 4) {
                        column.addContent(
                            text("+${interested.size - 4} more".layoutString, typography = Typography.BODY_LARGE)
                        )
                    }
                    column.build()
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
