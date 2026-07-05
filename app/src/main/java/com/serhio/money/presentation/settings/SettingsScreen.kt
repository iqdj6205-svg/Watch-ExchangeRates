package com.serhio.money.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.*
import androidx.wear.compose.material3.ButtonDefaults

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val baseCurrency by viewModel.baseCurrency.collectAsState()
    val interestedCurrencies by viewModel.interestedCurrencies.collectAsState()
    val updateInterval by viewModel.updateInterval.collectAsState()
    val listState = rememberScalingLazyListState()

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            ListHeader {
                Text("Base Currency", fontWeight = FontWeight.Bold)
            }
        }

        val currencies = listOf("USD", "EUR", "PLN", "UAH", "GBP", "JPY", "CHF", "CZK", "BTC", "ETH", "XAU", "XAG")

        val chunked = currencies.chunked(3)
        chunked.forEach { row ->
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    row.forEach { currency ->
                        val selected = currency == baseCurrency
                        Button(
                            onClick = { viewModel.setBaseCurrency(currency) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primary else Color(0xFF2C2C2C),
                                contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else Color.White
                            )
                        ) {
                            Text(currency, maxLines = 1)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            ListHeader {
                Text("Interested Currencies", fontWeight = FontWeight.Bold)
            }
        }

        val interestedChunked = currencies.chunked(3)
        interestedChunked.forEach { row ->
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    row.forEach { currency ->
                        val selected = interestedCurrencies.contains(currency)
                        Button(
                            onClick = { viewModel.toggleInterestedCurrency(currency) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primary else Color(0xFF2C2C2C),
                                contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else Color.White
                            )
                        ) {
                            Text(if (selected) "\u2713 $currency" else currency, maxLines = 1)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            ListHeader {
                Text("Update Interval", fontWeight = FontWeight.Bold)
            }
        }

        val intervals = listOf(
            "15 min" to (15 * 60 * 1000L),
            "30 min" to (30 * 60 * 1000L),
            "1 hour" to (60 * 60 * 1000L),
            "2 hours" to (2 * 60 * 60 * 1000L),
            "6 hours" to (6 * 60 * 60 * 1000L),
            "12 hours" to (12 * 60 * 60 * 1000L),
            "24 hours" to (24 * 60 * 60 * 1000L)
        )

        intervals.chunked(2).forEach { row ->
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    row.forEach { (label, ms) ->
                        val selected = ms == updateInterval
                        Button(
                            onClick = { viewModel.setUpdateInterval(ms) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primary else Color(0xFF2C2C2C),
                                contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else Color.White
                            )
                        ) {
                            Text(label, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}