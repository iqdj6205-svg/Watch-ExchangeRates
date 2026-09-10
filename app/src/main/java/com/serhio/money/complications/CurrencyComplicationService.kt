package com.serhio.money.complications

import android.app.PendingIntent
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.MonochromaticImageComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.serhio.money.R
import com.serhio.money.data.settings.SettingsManager
import com.serhio.money.domain.repository.CurrencyRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import timber.log.Timber

@AndroidEntryPoint
class CurrencyComplicationService : SuspendingComplicationDataSourceService() {
    @Inject lateinit var repository: CurrencyRepository
    @Inject lateinit var settingsManager: SettingsManager

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        return try {
            val baseCurrency = settingsManager.baseCurrencyFlow.first()
            val target = settingsManager.complicationDisplayCurrencyFlow.first()
            val mode = settingsManager.complicationDisplayModeFlow.first()
            val result = repository.fetchLatestRates(baseCurrency)
            val rateValue = result.getOrNull()?.rates?.get(target) ?: 0.0
            val rateText = formatComplicationValue(mode, rateValue)
            val pairText = "$baseCurrency/$target"
            val tapPendingIntent = packageManager.getLaunchIntentForPackage(packageName)?.let { intent ->
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            }

            when (request.complicationType) {
                ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = rateText).build(),
                    contentDescription = PlainComplicationText.Builder(text = "$pairText $rateText").build()
                ).setTitle(PlainComplicationText.Builder(text = target).build())
                    .apply { tapPendingIntent?.let { setTapAction(it) } }
                    .build()
                ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = "$pairText $rateText").build(),
                    contentDescription = PlainComplicationText.Builder(text = "$pairText $rateText").build()
                ).setTitle(PlainComplicationText.Builder(text = "Money").build())
                    .apply { tapPendingIntent?.let { setTapAction(it) } }
                    .build()
                ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
                    value = rateValue.toFloat(),
                    min = 0f,
                    max = (if (rateValue > 0) rateValue * 2 else 100.0).toFloat(),
                    contentDescription = PlainComplicationText.Builder(text = "$pairText $rateText").build()
                ).setText(PlainComplicationText.Builder(text = target).build())
                    .apply { tapPendingIntent?.let { setTapAction(it) } }
                    .build()
                ComplicationType.MONOCHROMATIC_IMAGE -> MonochromaticImageComplicationData.Builder(
                    monochromaticImage = MonochromaticImage.Builder(android.graphics.drawable.Icon.createWithResource(this@CurrencyComplicationService, R.drawable.ic_exchange)).build(),
                    contentDescription = PlainComplicationText.Builder(text = "$pairText $rateText").build()
                ).apply { tapPendingIntent?.let { setTapAction(it) } }.build()
                else -> null
            }
        } catch (e: Exception) {
            Timber.e(e, "Complication request failed")
            null
        }
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return when (type) {
            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(PlainComplicationText.Builder("1.09").build(), PlainComplicationText.Builder("Preview").build()).setTitle(PlainComplicationText.Builder("EUR").build()).build()
            ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(PlainComplicationText.Builder("USD/EUR 1.09").build(), PlainComplicationText.Builder("Preview Rate").build()).setTitle(PlainComplicationText.Builder("Money").build()).build()
            ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(1.09f, 0f, 2.18f, PlainComplicationText.Builder("1.09").build()).setText(PlainComplicationText.Builder("EUR").build()).build()
            ComplicationType.MONOCHROMATIC_IMAGE -> MonochromaticImageComplicationData.Builder(MonochromaticImage.Builder(android.graphics.drawable.Icon.createWithResource(this@CurrencyComplicationService, R.drawable.ic_exchange)).build(), PlainComplicationText.Builder("Preview").build()).build()
            else -> null
        }
    }

    override fun onComplicationActivated(complicationInstanceId: Int, type: ComplicationType) { Timber.d("Complication activated: $complicationInstanceId") }
    override fun onComplicationDeactivated(complicationInstanceId: Int) { Timber.d("Complication deactivated: $complicationInstanceId") }

    private fun formatComplicationValue(mode: String, rateValue: Double): String = when (mode) {
        "date" -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        "arrow" -> if (rateValue > 0.0) "↗" else "--"
        "change" -> String.format(Locale.US, "%.2f%%", 0.0)
        else -> when {
            rateValue < 0.01 -> String.format(Locale.US, "%.4f", rateValue)
            rateValue < 1.0 -> String.format(Locale.US, "%.3f", rateValue)
            else -> String.format(Locale.US, "%.2f", rateValue)
        }
    }
}
