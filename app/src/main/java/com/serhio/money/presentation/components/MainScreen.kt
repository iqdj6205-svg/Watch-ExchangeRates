package com.serhio.money.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
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
import android.view.HapticFeedbackConstants
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
    onMoveCurrency: (String, Int) -> Unit = { _, _ -> }
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
                onMoveCurrency = onMoveCurrency
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
    onMoveCurrency: (String, Int) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    var showMenu by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            PageContent(page, state, isOnline, onOpenGraphs, onMoveCurrency)
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
    onOpenGraphs: (String, String) -> Unit,
    onMoveCurrency: (String, Int) -> Unit
) {
    val entries = if (page == 0) {
        state.interestedCurrencies
            .mapNotNull { code -> state.rates[code]?.let { rate -> java.util.AbstractMap.SimpleEntry(code, rate) } }
    } else {
        state.rates.entries.toList()
    }

    var selectedForReorder by remember { mutableStateOf<String?>(null) }

    ScalingLazyColumn(
        state = rememberScalingLazyListState(),
        horizontalAlignment = Alignment.CenterHorizontally,
        userScrollEnabled = selectedForReorder == null
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
            val isSelected = selectedForReorder == entry.key
            CurrencyCard(
                baseCurrency = state.baseCurrency,
                currencyCode = entry.key,
                rateValue = entry.value,
                history = state.history[entry.key],
                isFavorite = entry.key in state.interestedCurrencies,
                isReorderSelected = isSelected,
                onClick = {
                    if (isSelected) {
                        selectedForReorder = null
                    } else {
                        onOpenGraphs(state.baseCurrency, entry.key)
                    }
                },
                onLongPress = if (page == 0 && state.interestedCurrencies.size > 1) {
                    { selectedForReorder = if (isSelected) null else entry.key }
                } else null,
                onMove = { direction ->
                    onMoveCurrency(entry.key, direction)
                }
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CurrencyCard(
    modifier: Modifier = Modifier,
    baseCurrency: String,
    currencyCode: String,
    rateValue: Double,
    history: List<Double>?,
    isFavorite: Boolean,
    onClick: () -> Unit,
    isReorderSelected: Boolean = false,
    onLongPress: (() -> Unit)? = null,
    onMove: (Int) -> Unit = {}
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

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha: Float by infiniteTransition.animateFloat(
        initialValue = 0.06f,
        targetValue = 0.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val view = LocalView.current

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
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isReorderSelected) MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha)
                else Color(0xFF2C2C2C)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress ?: {}
            )
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
                    if (isReorderSelected) {
                        Box(
                            modifier = Modifier
                                .pointerInput(Unit) {
                                    var moved = false
                                    detectVerticalDragGestures(
                                        onDragStart = { moved = false },
                                        onDragEnd = { },
                                        onVerticalDrag = { change, dragAmount ->
                                            if (!moved) {
                                                if (dragAmount < -30f) {
                                                    change.consume()
                                                    moved = true
                                                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                                    onMove(-1)
                                                } else if (dragAmount > 30f) {
                                                    change.consume()
                                                    moved = true
                                                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                                    onMove(1)
                                                }
                                            } else {
                                                change.consume()
                                            }
                                        }
                                    )
                                }
                                .size(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("\u2630", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
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
}
