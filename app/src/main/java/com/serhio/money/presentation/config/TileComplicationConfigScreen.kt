package com.serhio.money.presentation.config

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.*
import androidx.wear.compose.material3.ButtonDefaults
import com.serhio.money.R

@Composable
fun TileComplicationConfigScreen(
    tileCurrency: String,
    tileMode: String,
    complicationCurrency: String,
    complicationMode: String,
    onTileCurrencyChange: (String) -> Unit,
    onTileModeChange: (String) -> Unit,
    onComplicationCurrencyChange: (String) -> Unit,
    onComplicationModeChange: (String) -> Unit,
    onBack: () -> Unit
) {
    val listState = rememberScalingLazyListState()

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            ListHeader {
                Text(stringResource(R.string.section_tile_config), fontWeight = FontWeight.Bold)
            }
        }

        item {
            ListHeader {
                Text(stringResource(R.string.section_tile_currency), fontWeight = FontWeight.Bold)
            }
        }

        val currencies = listOf(
            "USD", "EUR", "GBP", "JPY", "CHF", "AUD", "CAD", "CNY",
            "PLN", "UAH", "CZK", "SEK", "INR", "KRW", "SGD", "TRY"
        )
        val currencyChunks = currencies.chunked(3)
        currencyChunks.forEach { row ->
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    row.forEach { currency ->
                        Button(
                            onClick = { onTileCurrencyChange(currency) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currency == tileCurrency)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Text(currency, maxLines = 1)
                        }
                    }
                }
            }
        }

        item {
            ListHeader {
                Text(stringResource(R.string.section_tile_mode), fontWeight = FontWeight.Bold)
            }
        }

        val modes = listOf(
            "rate" to R.string.mode_rate,
            "change" to R.string.mode_change_pct,
            "arrow" to R.string.mode_arrow,
            "date" to R.string.mode_date
        )
        modes.chunked(2).forEach { row ->
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    row.forEach { (key, labelRes) ->
                        Button(
                            onClick = { onTileModeChange(key) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (key == tileMode)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Text(stringResource(labelRes), maxLines = 1)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        item {
            ListHeader {
                Text(stringResource(R.string.section_complication_config), fontWeight = FontWeight.Bold)
            }
        }

        item {
            ListHeader {
                Text(stringResource(R.string.section_tile_currency), fontWeight = FontWeight.Bold)
            }
        }

        currencyChunks.forEach { row ->
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    row.forEach { currency ->
                        Button(
                            onClick = { onComplicationCurrencyChange(currency) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currency == complicationCurrency)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Text(currency, maxLines = 1)
                        }
                    }
                }
            }
        }

        item {
            ListHeader {
                Text(stringResource(R.string.section_tile_mode), fontWeight = FontWeight.Bold)
            }
        }

        modes.chunked(2).forEach { row ->
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    row.forEach { (key, labelRes) ->
                        Button(
                            onClick = { onComplicationModeChange(key) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (key == complicationMode)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Text(stringResource(labelRes), maxLines = 1)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onBack) {
                Text(stringResource(R.string.back))
            }
        }
    }
}