package com.serhio.money.presentation.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.*
import com.serhio.money.R
import com.serhio.money.presentation.components.currencyFlag

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
            ListHeader { Text(stringResource(R.string.section_base), fontWeight = FontWeight.Bold) }
        }

        item {
            Text(
                text = "${currencyFlag(baseCurrency)} $baseCurrency",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        val currencies = listOf(
            "USD", "EUR", "GBP", "JPY", "CHF", "AUD", "CAD", "CNY",
            "PLN", "UAH", "CZK", "DKK", "NOK", "SEK", "HUF", "RON",
            "INR", "KRW", "SGD", "HKD", "MXN", "BRL", "ZAR", "TRY",
            "RUB", "ILS", "NZD", "BTC", "ETH", "XAU", "XAG"
        )

        currencies.chunked(2).forEach { row ->
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    row.forEach { currency ->
                        val selected = currency == baseCurrency
                        val bg = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.setBaseCurrency(currency) }
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${currencyFlag(currency)} $currency",
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            ListHeader { Text(stringResource(R.string.section_interested), fontWeight = FontWeight.Bold) }
        }

        currencies.chunked(2).forEach { row ->
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    row.forEach { currency ->
                        val selected = interestedCurrencies.contains(currency)
                        val bg = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.toggleInterestedCurrency(currency) }
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (selected) "\u2611" else "\u2610",
                                fontSize = 14.sp,
                                color = if (selected) MaterialTheme.colorScheme.primary else Color.White,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text("${currencyFlag(currency)} $currency",
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            ListHeader { Text(stringResource(R.string.section_interval), fontWeight = FontWeight.Bold) }
        }

        item {
            var showPopup by remember { mutableStateOf(false) }
            val intervals = listOf(
                "15m" to (15 * 60 * 1000L),
                "30m" to (30 * 60 * 1000L),
                "1h" to (60 * 60 * 1000L),
                "2h" to (2 * 60 * 60 * 1000L),
                "6h" to (6 * 60 * 60 * 1000L),
                "12h" to (12 * 60 * 60 * 1000L),
                "24h" to (24 * 60 * 60 * 1000L)
            )
            val currentLabel = intervals.find { it.second == updateInterval }?.first ?: "—"

            Box(Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPopup = true }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(currentLabel,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                    Text(" \u25BC",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(start = 4.dp))
                }

                AnimatedVisibility(
                    visible = showPopup,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Card(
                        onClick = {},
                        modifier = Modifier.align(Alignment.Center).padding(horizontal = 8.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(Modifier.padding(vertical = 4.dp)) {
                            intervals.forEach { (label, ms) ->
                                val selected = ms == updateInterval
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.setUpdateInterval(ms); showPopup = false }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        if (selected) "\u25C9" else "\u25CB",
                                        fontSize = 14.sp,
                                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(label,
                                        fontSize = 13.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
