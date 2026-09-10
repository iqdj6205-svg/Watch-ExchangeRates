@file:OptIn(com.google.android.horologist.annotations.ExperimentalHorologistApi::class)

package com.serhio.money.tiles

import android.content.Context
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
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

@AndroidEntryPoint
class MultiCurrencyTileService : BaseCurrencyTileService() {
    override suspend fun buildTile(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile {
        val baseCurrency = settingsManager.baseCurrencyFlow.first()
        val configuredTarget = configuredTileCurrency()
        val interested = settingsManager.interestedCurrenciesFlow.first()
        val mode = settingsManager.tileDisplayModeFlow.first()
        val result = repository.fetchLatestRates(baseCurrency)
        val rates = result.getOrNull()?.rates ?: emptyMap()
        val targets = (listOf(configuredTarget) + interested).distinct().take(4)
        val values = targets.associateWith { target -> formatTileValue(mode, baseCurrency, target, rates[target]) }
        return createTimeline(createLayout(this, requestParams.deviceConfiguration, baseCurrency, targets, values))
    }

    private fun createLayout(context: Context, deviceConfiguration: DeviceParameters, baseCurrency: String, targets: List<String>, values: Map<String, String>): LayoutElementBuilders.LayoutElement {
        val openAction = ActionBuilders.LaunchAction.Builder().setAndroidActivity(ActionBuilders.AndroidActivity.Builder().setPackageName(context.packageName).setClassName("${context.packageName}.presentation.MainActivity").build()).build()
        val onClick = ModifiersBuilders.Clickable.Builder().setId("open_app").setOnClick(openAction).build()
        return materialScope(context, deviceConfiguration) {
            primaryLayout(
                titleSlot = { text(baseCurrency.layoutString, typography = Typography.TITLE_MEDIUM) },
                mainSlot = {
                    val column = LayoutElementBuilders.Column.Builder()
                    if (targets.isEmpty()) {
                        column.addContent(text("No currencies".layoutString, typography = Typography.BODY_LARGE))
                    } else {
                        targets.forEach { target ->
                            column.addContent(text("$target  ${values[target] ?: "--"}".layoutString, typography = Typography.BODY_LARGE))
                        }
                    }
                    column.build()
                },
                bottomSlot = { textEdgeButton(onClick = onClick) { text("Open".layoutString) } }
            )
        }
    }
}
