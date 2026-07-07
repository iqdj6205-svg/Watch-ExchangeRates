package com.serhio.money.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.*
import com.serhio.money.R
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
    when (uiState) {
        is MainUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is MainUiState.Success -> {
            MainContent(
                state = uiState,
                isOnline = isOnline,
                onRefresh = onRefresh,
                onOpenSettings = onOpenSettings,
                onOpenTileConfig = onOpenTileConfig,
                onOpenGraphs = onOpenGraphs
            )
        }
        is MainUiState.Error -> {
            ErrorContent(message = uiState.message, onRetry = onRefresh)
        }
    }
}

@Composable
private fun ErrorContent(message: String?, onRetry: () -> Unit) {
    val displayMessage = message ?: stringResource(R.string.unknown_error)
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(displayMessage, color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
    }
}

@Composable
private fun MainContent(
    state: MainUiState.Success,
    isOnline: Boolean,
    onRefresh: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenTileConfig: () -> Unit,
    onOpenGraphs: (String, String) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    var showMenu by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Box(Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            PageContent(page, state, isOnline, onOpenGraphs)
        }

        Column(
            Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = showMenu,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    onClick = {},
                    modifier = Modifier.padding(bottom = 4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(vertical = 4.dp)) {
                        TextButton(
                            onClick = { showMenu = false; onOpenSettings() },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                        ) {
                            Text(stringResource(R.string.menu_settings), modifier = Modifier.fillMaxWidth(),
                                fontSize = 13.sp, textAlign = TextAlign.Start)
                        }
                        TextButton(
                            onClick = { showMenu = false; onOpenTileConfig() },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                        ) {
                            Text(stringResource(R.string.menu_tiles),
                                modifier = Modifier.fillMaxWidth(), fontSize = 13.sp,
                                textAlign = TextAlign.Start)
                        }
                        TextButton(
                            onClick = { showMenu = false; onRefresh() },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                        ) {
                            Text(stringResource(R.string.menu_refresh), modifier = Modifier.fillMaxWidth(),
                                fontSize = 13.sp, textAlign = TextAlign.Start)
                        }
                    }
                }
            }

            IconButton(onClick = { showMenu = !showMenu }) {
                Text(if (showMenu) "\u2715" else "\u2026", fontSize = 20.sp)
            }
        }
    }
}

@Composable
private fun PageContent(
    page: Int,
    state: MainUiState.Success,
    isOnline: Boolean,
    onOpenGraphs: (String, String) -> Unit
) {
    val entries = if (page == 0) {
        state.rates.filterKeys { it in state.interestedCurrencies }.entries.toList()
    } else {
        state.rates.entries.toList()
    }

    ScalingLazyColumn(
        state = rememberScalingLazyListState(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (entries.isEmpty() && page == 0) {
            item {
                Box(Modifier.fillMaxSize().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.empty_currencies),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface)
                }
            }
            return@ScalingLazyColumn
        }

        item {
            Text(
                if (page == 0) stringResource(R.string.page_favorites) else stringResource(R.string.page_all),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
            )
        }

        items(entries.size) { index ->
            val entry = entries[index]
            CurrencyCard(
                baseCurrency = state.baseCurrency,
                currencyCode = entry.key,
                rateValue = entry.value,
                history = state.history[entry.key],
                isFavorite = entry.key in state.interestedCurrencies,
                onOpenGraphs = onOpenGraphs
            )
        }

        item {
            val df = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
            Row(
                Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isOnline) {
                    Text(stringResource(R.string.status_offline), color = MaterialTheme.colorScheme.error,
                        fontSize = 9.sp, modifier = Modifier.padding(end = 4.dp))
                } else if (state.isFromCache) {
                    Text(stringResource(R.string.status_cached), color = MaterialTheme.colorScheme.tertiary,
                        fontSize = 9.sp, modifier = Modifier.padding(end = 4.dp))
                }
                Text(df.format(Date(state.lastUpdate)),
                    fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        }

        item {
            Spacer(Modifier.height(72.dp))
        }
    }
}

@Composable
private fun CurrencyCard(
    baseCurrency: String,
    currencyCode: String,
    rateValue: Double,
    history: List<Double>?,
    isFavorite: Boolean,
    onOpenGraphs: (String, String) -> Unit
) {
    val reciprocal = 1.0 / rateValue

    val change = if (history != null && history.size >= 2) {
        history.last() - history[history.size - 2]
    } else null

    val changePercent = if (change != null && history != null && history.size >= 2 && history[history.size - 2] != 0.0) {
        (change / history[history.size - 2]) * 100
    } else null

    val changeColor = when {
        change == null -> Color.Gray
        change > 0 -> Color(0xFF4CAF50)
        else -> Color(0xFFF44336)
    }

    Card(
        onClick = { onOpenGraphs(baseCurrency, currencyCode) },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${currencyFlag(currencyCode)} $currencyCode",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    if (isFavorite) {
                        Spacer(Modifier.width(4.dp))
                        Text("\u2605", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
                if (changePercent != null && change != null) {
                    Text(
                        "${if (change > 0) "+" else ""}${String.format(Locale.ROOT, "%.2f", changePercent)}%",
                        color = changeColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "1 $baseCurrency = ${
                            if (rateValue >= 100) String.format(Locale.ROOT, "%.1f", rateValue)
                            else if (rateValue >= 1) String.format(Locale.ROOT, "%.4f", rateValue)
                            else String.format(Locale.ROOT, "%.6f", rateValue)
                        }",
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "1 $currencyCode = ${
                            if (reciprocal >= 100) String.format(Locale.ROOT, "%.1f", reciprocal)
                            else if (reciprocal >= 1) String.format(Locale.ROOT, "%.4f", reciprocal)
                            else String.format(Locale.ROOT, "%.6f", reciprocal)
                        }",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    if (change != null) {
                        Text(
                            if (change > 0) "\u25B2" else "\u25BC",
                            color = changeColor, fontSize = 16.sp
                        )
                    }
                    if (history != null && history.isNotEmpty()) {
                        Spacer(Modifier.height(6.dp))
                        SparklineChart(
                            data = history,
                            modifier = Modifier.width(56.dp).height(22.dp),
                            color = changeColor
                        )
                    }
                }
            }
        }
    }
}
