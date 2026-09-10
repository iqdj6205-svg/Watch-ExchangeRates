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
import java.util.Locale

@AndroidEntryPoint
class CurrencyTileService : BaseCurrencyTileService() {

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
                    LayoutElementBuilders.Column.Builder()
                        .addContent(
                        LayoutElementBuilders.Image.Builder()
                            .setResourceId("icon_exchange")
                            .setWidth(DimensionBuilders.dp(32f))
                            .setHeight(DimensionBuilders.dp(32f))
                            .setContentScaleMode(LayoutElementBuilders.CONTENT_SCALE_MODE_FIT)
                            .build()
                        )
                        .addContent(
                            text(
                                (rate ?: "--").layoutString,
                                typography = Typography.NUMERAL_LARGE
                            )
                        )
                        .build()
                },
                bottomSlot = {
                    textEdgeButton(onClick = onClick) {
                        text("Open".layoutString)
                    }
                }
            )
        }
    }
}
