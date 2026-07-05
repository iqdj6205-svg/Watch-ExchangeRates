package com.serhio.money.presentation.config

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.*
import androidx.wear.compose.material3.ButtonDefaults

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
                Text("Tile Configuration", fontWeight = FontWeight.Bold)
            }
        }

        item {
            ListHeader {
                Text("Display Currency", fontWeight = FontWeight.Bold)
            }
        }

        val currencies = listOf("USD", "EUR", "PLN", "UAH", "GBP", "JPY", "CHF", "CZK", "BTC")
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
                Text("Display Mode", fontWeight = FontWeight.Bold)
            }
        }

        val modes = listOf("rate" to "Rate", "change" to "Change %", "arrow" to "Arrow", "date" to "Date")
        modes.chunked(2).forEach { row ->
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    row.forEach { (key, label) ->
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
                            Text(label, maxLines = 1)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        item {
            ListHeader {
                Text("Complication Configuration", fontWeight = FontWeight.Bold)
            }
        }

        item {
            ListHeader {
                Text("Display Currency", fontWeight = FontWeight.Bold)
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
                Text("Display Mode", fontWeight = FontWeight.Bold)
            }
        }

        modes.chunked(2).forEach { row ->
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    row.forEach { (key, label) ->
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
                            Text(label, maxLines = 1)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onBack) {
                Text("Back")
            }
        }
    }
}