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
import java.util.Locale
import javax.inject.Inject
import timber.log.Timber

@AndroidEntryPoint
class CurrencyComplicationService : SuspendingComplicationDataSourceService() {

    @Inject
    lateinit var repository: CurrencyRepository

    @Inject
    lateinit var settingsManager: SettingsManager

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        return try {
            val baseCurrency = settingsManager.baseCurrencyFlow.first()
            val interested = settingsManager.interestedCurrenciesFlow.first()
            val target = interested.firstOrNull() ?: "EUR"

            val result = repository.fetchLatestRates(baseCurrency)
            val rateValue = if (result.isSuccess) {
                result.getOrThrow().rates[target] ?: 0.0
            } else {
                0.0
            }
            val rateText = String.format(Locale.US, "%.2f", rateValue)

            val tapIntent = packageManager.getLaunchIntentForPackage("com.serhio.money")
            val tapPendingIntent = if (tapIntent != null) {
                PendingIntent.getActivity(
                    this, 0, tapIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            } else {
                null
            }

            when (request.complicationType) {
                ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = rateText).build(),
                    contentDescription = PlainComplicationText.Builder(text = "$target Rate").build()
                ).setTitle(PlainComplicationText.Builder(text = target).build())
                    .apply { tapPendingIntent?.let { setTapAction(it) } }
                    .build()

                ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = "$target: $rateText").build(),
                    contentDescription = PlainComplicationText.Builder(text = "$target Exchange Rate").build()
                ).setTitle(PlainComplicationText.Builder(text = "Currency").build())
                    .apply { tapPendingIntent?.let { setTapAction(it) } }
                    .build()

                ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
                    value = rateValue.toFloat(),
                    min = 0f,
                    max = (if (rateValue > 0) rateValue * 2 else 100.0).toFloat(),
                    contentDescription = PlainComplicationText.Builder(text = rateText).build()
                ).setText(PlainComplicationText.Builder(text = target).build())
                    .apply { tapPendingIntent?.let { setTapAction(it) } }
                    .build()

                ComplicationType.MONOCHROMATIC_IMAGE -> MonochromaticImageComplicationData.Builder(
                    monochromaticImage = MonochromaticImage.Builder(
                        image = android.graphics.drawable.Icon.createWithResource(
                            this@CurrencyComplicationService,
                            R.drawable.ic_exchange
                        )
                    ).build(),
                    contentDescription = PlainComplicationText.Builder(text = rateText).build()
                ).apply { tapPendingIntent?.let { setTapAction(it) } }
                    .build()

                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return when (type) {
            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(text = "1.23").build(),
                contentDescription = PlainComplicationText.Builder(text = "Preview").build()
            ).setTitle(PlainComplicationText.Builder(text = "EUR").build())
                .build()

            ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                text = PlainComplicationText.Builder(text = "EUR: 1.23").build(),
                contentDescription = PlainComplicationText.Builder(text = "Preview Rate").build()
            ).setTitle(PlainComplicationText.Builder(text = "Currency").build())
                .build()

            ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
                value = 1.23f,
                min = 0f,
                max = 2.46f,
                contentDescription = PlainComplicationText.Builder(text = "1.23").build()
            ).setText(PlainComplicationText.Builder(text = "EUR").build())
                .build()

            ComplicationType.MONOCHROMATIC_IMAGE -> MonochromaticImageComplicationData.Builder(
                monochromaticImage = MonochromaticImage.Builder(
                    image = android.graphics.drawable.Icon.createWithResource(
                        this@CurrencyComplicationService,
                        R.drawable.ic_exchange
                    )
                ).build(),
                contentDescription = PlainComplicationText.Builder(text = "Preview").build()
            ).build()

            else -> null
        }
    }

    override fun onComplicationActivated(complicationId: Int, type: ComplicationType) {
        Timber.d("Complication activated: $complicationId")
    }

    override fun onComplicationDeactivated(complicationId: Int) {
        Timber.d("Complication deactivated: $complicationId")
    }
}
