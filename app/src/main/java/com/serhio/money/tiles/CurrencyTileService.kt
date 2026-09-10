@file:OptIn(com.google.android.horologist.annotations.ExperimentalHorologistApi::class)

package com.serhio.money.tiles

import android.content.Context
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.DimensionBuilders
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
class CurrencyTileService : BaseCurrencyTileService() {
    override suspend fun buildTile(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile {
        val baseCurrency = settingsManager.baseCurrencyFlow.first()
        val target = configuredTileCurrency()
        val mode = settingsManager.tileDisplayModeFlow.first()
        val rateValue = fetchRate(baseCurrency, target)
        val valueText = formatTileValue(mode, baseCurrency, target, rateValue)
        return createTimeline(createLayout(this, requestParams.deviceConfiguration, baseCurrency, target, valueText))
    }

    private fun createLayout(context: Context, deviceConfiguration: DeviceParameters, baseCurrency: String, currency: String, value: String): LayoutElementBuilders.LayoutElement {
        val openAction = ActionBuilders.LaunchAction.Builder().setAndroidActivity(ActionBuilders.AndroidActivity.Builder().setPackageName(context.packageName).setClassName("${context.packageName}.presentation.MainActivity").build()).build()
        val onClick = ModifiersBuilders.Clickable.Builder().setId("open_app").setOnClick(openAction).build()
        return materialScope(context, deviceConfiguration) {
            primaryLayout(
                titleSlot = { text("$baseCurrency/$currency".layoutString, typography = Typography.TITLE_MEDIUM) },
                mainSlot = {
                    LayoutElementBuilders.Column.Builder()
                        .addContent(LayoutElementBuilders.Image.Builder().setResourceId("icon_exchange").setWidth(DimensionBuilders.dp(28f)).setHeight(DimensionBuilders.dp(28f)).setContentScaleMode(LayoutElementBuilders.CONTENT_SCALE_MODE_FIT).build())
                        .addContent(text(value.layoutString, typography = Typography.NUMERAL_LARGE))
                        .build()
                },
                bottomSlot = { textEdgeButton(onClick = onClick) { text("Open".layoutString) } }
            )
        }
    }
}
