package com.serhio.money.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.*
import com.serhio.money.presentation.MainUiState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MainScreen(
    uiState: MainUiState,
    isOnline: Boolean,
    onRefresh: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenTileConfig: () -> Unit = {},
    onOpenGraphs: (String, String) -> Unit = { _, _ -> }
) {
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = rememberScalingLazyListState(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (uiState) {
            is MainUiState.Loading -> {
                item {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(top = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            is MainUiState.Success -> {
                successContent(uiState, isOnline, onRefresh, onOpenSettings, onOpenTileConfig, onOpenGraphs)
            }
            is MainUiState.Error -> {
                errorContent(uiState.message, onRefresh)
            }
        }
    }
}

private fun ScalingLazyListScope.errorContent(message: String, onRetry: () -> Unit) {
    item {
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onRetry) {
            Text("Retry")
        }
    }
}

private fun ScalingLazyListScope.successContent(
    state: MainUiState.Success,
    isOnline: Boolean,
    onRefresh: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenTileConfig: () -> Unit,
    onOpenGraphs: (String, String) -> Unit
) {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    if (!isOnline) {
        item {
            Text(
                text = "Offline",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }

    if (state.isFromCache) {
        item {
            Text(
                text = "Cached",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }

    item {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, start = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = state.baseCurrency,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "\u2192",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }

    items(state.rates.size) { index ->
        val entry = state.rates.entries.toList()[index]
        val currencyCode = entry.key
        val rateValue = entry.value
        val history = state.history[currencyCode]

        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = currencyCode,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = String.format("%.4f", rateValue),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )

                if (history != null && history.size >= 2) {
                    val last = history.last()
                    val prev = history[history.size - 2]
                    val color = when {
                        last > prev -> Color(0xFF4CAF50)
                        last < prev -> Color(0xFFF44336)
                        else -> Color.Gray
                    }
                    val arrow = when {
                        last > prev -> "\u25B2"
                        last < prev -> "\u25BC"
                        else -> "\u2014"
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = arrow,
                        color = color,
                        fontSize = 12.sp
                    )
                }
            }

            if (history != null && history.isNotEmpty()) {
                SparklineChart(
                    data = history,
                    modifier = Modifier.fillMaxWidth(0.9f).height(28.dp).padding(top = 2.dp)
                )
            }

            if (history != null && history.size >= 2) {
                TextButton(onClick = { onOpenGraphs(state.baseCurrency, currencyCode) }) {
                    Text("Chart", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }

    item {
        Text(
            text = dateFormat.format(Date(state.lastUpdate)),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 4.dp)
        )
    }

    item {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(onClick = onOpenSettings) {
                Text("\u2699", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onOpenTileConfig) {
                Text("\u25A6", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onRefresh) {
                Text("\u21BB", fontSize = 18.sp)
            }
        }
    }
}