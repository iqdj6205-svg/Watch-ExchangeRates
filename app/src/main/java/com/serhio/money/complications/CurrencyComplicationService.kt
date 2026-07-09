package com.serhio.money.complications

import androidx.wear.watchface.complications.data.*
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import com.serhio.money.R
import com.serhio.money.data.settings.SettingsManager
import com.serhio.money.domain.repository.CurrencyRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class CurrencyComplicationService : ComplicationDataSourceService() {

    @Inject
    lateinit var repository: CurrencyRepository

    @Inject
    lateinit var settingsManager: SettingsManager

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onComplicationRequest(
        request: ComplicationRequest,
        listener: ComplicationRequestListener
    ) {
        serviceScope.launch {
            try {
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

                val data: ComplicationData? = when (request.complicationType) {
                    ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                        text = PlainComplicationText.Builder(text = rateText).build(),
                        contentDescription = PlainComplicationText.Builder(text = "$target Rate").build()
                    ).setTitle(PlainComplicationText.Builder(text = target).build())
                        .build()

                    ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                        text = PlainComplicationText.Builder(text = "$target: $rateText").build(),
                        contentDescription = PlainComplicationText.Builder(text = "$target Exchange Rate").build()
                    ).setTitle(PlainComplicationText.Builder(text = "Currency").build())
                        .build()

                    ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
                        value = rateValue.toFloat(),
                        min = 0f,
                        max = (if (rateValue > 0) rateValue * 2 else 100.0).toFloat(),
                        contentDescription = PlainComplicationText.Builder(text = rateText).build()
                    ).setText(PlainComplicationText.Builder(text = target).build())
                        .build()

                    ComplicationType.MONOCHROMATIC_IMAGE -> MonochromaticImageComplicationData.Builder(
                        monochromaticImage = MonochromaticImage.Builder(
                            image = android.graphics.drawable.Icon.createWithResource(
                                this@CurrencyComplicationService,
                                R.drawable.ic_trending_up
                            )
                        ).build(),
                        contentDescription = PlainComplicationText.Builder(text = rateText).build()
                    ).build()

                    else -> null
                }
                listener.onComplicationData(data)
            } catch (e: Exception) {
                listener.onComplicationData(null)
            }
        }
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text = "1.23").build(),
            contentDescription = PlainComplicationText.Builder(text = "Preview").build()
        ).setTitle(PlainComplicationText.Builder(text = "EUR").build())
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
