package com.serhio.money.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.*
import com.serhio.money.R
import com.serhio.money.presentation.MainUiState
import android.view.HapticFeedbackConstants
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MainScreen(
    uiState: MainUiState,
    isOnline: Boolean,
    onRefresh: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenTileConfig: () -> Unit = {},
    onOpenGraphs: (String, String) -> Unit = { _, _ -> },
    onReorderFavorites: (List<String>) -> Unit = { }
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
                onOpenGraphs = onOpenGraphs,
                onReorderFavorites = onReorderFavorites
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
    onOpenGraphs: (String, String) -> Unit,
    onReorderFavorites: (List<String>) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    var showMenu by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            if (page == 0) {
                FavoritesPage(state, isOnline, onOpenGraphs, onReorderFavorites)
            } else {
                AllCurrenciesPage(state, isOnline, onOpenGraphs)
            }
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
private fun FavoritesPage(
    state: MainUiState.Success,
    isOnline: Boolean,
    onOpenGraphs: (String, String) -> Unit,
    onReorderFavorites: (List<String>) -> Unit
) {
    val view = LocalView.current
    val displayed = state.interestedCurrencies.filter { state.rates.containsKey(it) }
    var order by remember(displayed) { mutableStateOf(displayed) }

    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
        order = order.toMutableList().apply { add(to.index - 1, removeAt(from.index - 1)) }
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(top = 34.dp, bottom = 76.dp, start = 4.dp, end = 4.dp)
    ) {
        item(key = "header") {
            Text(
                stringResource(R.string.page_favorites),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        if (order.isEmpty()) {
            item(key = "empty") {
                Box(Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.empty_currencies),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        items(order, key = { it }) { code ->
            ReorderableItem(reorderState, key = code) { isDragging ->
                val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp, label = "elevation")
                val interactionSource = remember { MutableInteractionSource() }
                CurrencyCard(
                    modifier = Modifier
                        .longPressDraggableHandle(
                            onDragStarted = {
                                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            },
                            onDragStopped = {
                                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                android.util.Log.d("REORDER_DBG", "stopped order=$order")
                                onReorderFavorites(order)
                            },
                            interactionSource = interactionSource
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onOpenGraphs(state.baseCurrency, code) },
                    baseCurrency = state.baseCurrency,
                    currencyCode = code,
                    rateValue = state.rates[code] ?: 0.0,
                    history = state.history[code],
                    isFavorite = true,
                    isDragging = isDragging,
                    elevation = elevation
                )
            }
        }

        item(key = "footer") {
            StatusFooter(isOnline = isOnline, isFromCache = state.isFromCache, lastUpdate = state.lastUpdate)
        }
    }
}

@Composable
private fun AllCurrenciesPage(
    state: MainUiState.Success,
    isOnline: Boolean,
    onOpenGraphs: (String, String) -> Unit
) {
    val entries = state.rates.entries.toList()

    ScalingLazyColumn(
        state = rememberScalingLazyListState(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                stringResource(R.string.page_all),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
            )
        }

        items(entries.size, key = { entries[it].key }) { index ->
            val entry = entries[index]
            CurrencyCard(
                modifier = Modifier.clickable { onOpenGraphs(state.baseCurrency, entry.key) },
                baseCurrency = state.baseCurrency,
                currencyCode = entry.key,
                rateValue = entry.value,
                history = state.history[entry.key],
                isFavorite = entry.key in state.interestedCurrencies
            )
        }

        item {
            StatusFooter(isOnline = isOnline, isFromCache = state.isFromCache, lastUpdate = state.lastUpdate)
        }

        item {
            Spacer(Modifier.height(72.dp))
        }
    }
}

@Composable
private fun StatusFooter(isOnline: Boolean, isFromCache: Boolean, lastUpdate: Long) {
    val df = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    Row(
        Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isOnline) {
            Text(stringResource(R.string.status_offline), color = MaterialTheme.colorScheme.error,
                fontSize = 9.sp, modifier = Modifier.padding(end = 4.dp))
        } else if (isFromCache) {
            Text(stringResource(R.string.status_cached), color = MaterialTheme.colorScheme.tertiary,
                fontSize = 9.sp, modifier = Modifier.padding(end = 4.dp))
        }
        Text(df.format(Date(lastUpdate)),
            fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
    }
}

@Composable
private fun CurrencyCard(
    modifier: Modifier = Modifier,
    baseCurrency: String,
    currencyCode: String,
    rateValue: Double,
    history: List<Double>?,
    isFavorite: Boolean,
    isDragging: Boolean = false,
    elevation: androidx.compose.ui.unit.Dp = 0.dp
) {
    val reciprocal = if (rateValue != 0.0) 1.0 / rateValue else 0.0

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

    val animatedRate by animateFloatAsState(
        targetValue = rateValue.toFloat(),
        animationSpec = tween(400)
    )
    val animatedRecip by animateFloatAsState(
        targetValue = reciprocal.toFloat(),
        animationSpec = tween(400)
    )

    fun formatRate(v: Float): String {
        return when {
            v >= 100f -> String.format(Locale.ROOT, "%.1f", v)
            v >= 1f -> String.format(Locale.ROOT, "%.4f", v)
            else -> String.format(Locale.ROOT, "%.6f", v)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .shadow(elevation, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isDragging) Color(0xFF3A3A3A)
                else Color(0xFF2C2C2C)
            )
            .semantics { contentDescription = "card_$currencyCode" }
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
                        "1 $baseCurrency = ${formatRate(animatedRate)}",
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "1 $currencyCode = ${formatRate(animatedRecip)}",
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
