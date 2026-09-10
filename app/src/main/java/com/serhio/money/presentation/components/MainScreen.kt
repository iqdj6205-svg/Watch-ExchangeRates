package com.serhio.money.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.serhio.money.domain.model.HistoryPoint
import com.serhio.money.presentation.MainUiState
import com.serhio.money.presentation.theme.Green500
import com.serhio.money.presentation.theme.Red500
import android.view.HapticFeedbackConstants
import kotlinx.coroutines.launch
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
    onOpenAlerts: () -> Unit = {},
    onReorderFavorites: (List<String>) -> Unit = { }
) {
    when (uiState) {
        is MainUiState.Loading -> SkeletonLoading()
        is MainUiState.Success -> MainContent(
            state = uiState,
            isOnline = isOnline,
            onRefresh = onRefresh,
            onOpenSettings = onOpenSettings,
            onOpenTileConfig = onOpenTileConfig,
            onOpenGraphs = onOpenGraphs,
            onOpenAlerts = onOpenAlerts,
            onReorderFavorites = onReorderFavorites
        )
        is MainUiState.Error -> ErrorContent(message = uiState.message, onRetry = onRefresh)
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
        Text(displayMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
    }
}

@Composable
private fun SkeletonLoading() {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -200f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(animation = tween(1200, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "shimmer"
    )
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(Color(0xFF1A1A1A), Color(0xFF2A2A2A), Color(0xFF1A1A1A)),
        start = Offset(shimmerOffset, 0f),
        end = Offset(shimmerOffset + 200f, 0f)
    )
    Column(Modifier.fillMaxSize().padding(top = 34.dp, start = 12.dp, end = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.page_favorites), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp))
        repeat(3) {
            Box(Modifier.fillMaxWidth().padding(vertical = 4.dp).height(70.dp).clip(RoundedCornerShape(14.dp)).background(shimmerBrush))
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.loading), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
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
    onOpenAlerts: () -> Unit,
    onReorderFavorites: (List<String>) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize()) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            when (page) {
                0 -> FavoritesPage(state, isOnline, onOpenGraphs, onReorderFavorites)
                1 -> AllCurrenciesPage(state, isOnline, onOpenGraphs)
                else -> ActionsPage(
                    onSettings = onOpenSettings,
                    onTileConfig = onOpenTileConfig,
                    onRefresh = onRefresh,
                    onAlerts = onOpenAlerts
                )
            }
        }
        Row(Modifier.align(Alignment.TopCenter).padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(if (pagerState.currentPage == index) 7.dp else 5.dp)
                        .clip(CircleShape)
                        .background(if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                        .clickable { scope.launch { pagerState.animateScrollToPage(index) } }
                )
            }
        }
    }
}

@Composable
private fun ActionsPage(onSettings: () -> Unit, onTileConfig: () -> Unit, onRefresh: () -> Unit, onAlerts: () -> Unit) {
    ScalingLazyColumn(
        state = rememberScalingLazyListState(),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 30.dp, bottom = 28.dp)
    ) {
        item {
            Text(stringResource(R.string.menu_title), style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(bottom = 8.dp))
        }
        item { MenuCard("\u2699", stringResource(R.string.menu_settings).withoutLeadingMenuIcon(), onSettings) }
        item { MenuCard("\u25A1", stringResource(R.string.menu_tiles).withoutLeadingMenuIcon(), onTileConfig) }
        item { MenuCard("\u21BB", stringResource(R.string.menu_refresh).withoutLeadingMenuIcon(), onRefresh) }
        item { MenuCard("\uD83D\uDD14", stringResource(R.string.menu_alerts).withoutLeadingMenuIcon(), onAlerts) }
    }
}

@Composable
private fun MenuCard(icon: String, label: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = MaterialTheme.shapes.medium) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 18.sp, modifier = Modifier.padding(end = 12.dp))
            Text(label, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun FavoritesPage(state: MainUiState.Success, isOnline: Boolean, onOpenGraphs: (String, String) -> Unit, onReorderFavorites: (List<String>) -> Unit) {
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
        contentPadding = PaddingValues(top = 34.dp, bottom = 28.dp, start = 4.dp, end = 4.dp)
    ) {
        item(key = "header") {
            Text(stringResource(R.string.page_favorites), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 4.dp))
        }
        if (order.isEmpty()) {
            item(key = "empty") {
                Box(Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.empty_currencies), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        items(order.size, key = { order[it] }) { index ->
            val code = order[index]
            val animProgress = remember { Animatable(0f) }
            LaunchedEffect(code) {
                animProgress.snapTo(0f)
                animProgress.animateTo(1f, tween(durationMillis = 300, delayMillis = index * 50, easing = LinearOutSlowInEasing))
            }
            ReorderableItem(reorderState, key = code) { isDragging ->
                val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp, label = "elevation")
                val interactionSource = remember { MutableInteractionSource() }
                CurrencyCard(
                    modifier = Modifier
                        .graphicsLayer { alpha = animProgress.value; translationY = (1f - animProgress.value) * 20f }
                        .clickable(interactionSource = interactionSource, indication = null) { onOpenGraphs(state.baseCurrency, code) },
                    dragHandleModifier = Modifier.longPressDraggableHandle(
                        onDragStarted = { view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS) },
                        onDragStopped = { view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK); onReorderFavorites(order) },
                        interactionSource = interactionSource
                    ),
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
        item(key = "footer") { StatusFooter(isOnline = isOnline, isFromCache = state.isFromCache, lastUpdate = state.lastUpdate) }
    }
}

@Composable
private fun AllCurrenciesPage(state: MainUiState.Success, isOnline: Boolean, onOpenGraphs: (String, String) -> Unit) {
    val entries = state.rates.entries.toList()
    ScalingLazyColumn(
        state = rememberScalingLazyListState(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(top = 30.dp, bottom = 28.dp)
    ) {
        item { Text(stringResource(R.string.page_all), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 4.dp)) }
        items(entries.size, key = { entries[it].key }) { index ->
            val entry = entries[index]
            val animProgress = remember { Animatable(0f) }
            LaunchedEffect(entry.key) {
                animProgress.snapTo(0f)
                animProgress.animateTo(1f, tween(durationMillis = 300, delayMillis = index * 40, easing = LinearOutSlowInEasing))
            }
            CurrencyCard(
                modifier = Modifier.graphicsLayer { alpha = animProgress.value; translationY = (1f - animProgress.value) * 20f }.clickable { onOpenGraphs(state.baseCurrency, entry.key) },
                baseCurrency = state.baseCurrency,
                currencyCode = entry.key,
                rateValue = entry.value,
                history = state.history[entry.key],
                isFavorite = entry.key in state.interestedCurrencies
            )
        }
        item { StatusFooter(isOnline = isOnline, isFromCache = state.isFromCache, lastUpdate = state.lastUpdate) }
    }
}

@Composable
private fun StatusFooter(isOnline: Boolean, isFromCache: Boolean, lastUpdate: Long) {
    val df = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    Column(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 2.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.width(40.dp).height(1.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)))
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            if (!isOnline) Text(stringResource(R.string.status_offline), color = MaterialTheme.colorScheme.error, fontSize = 9.sp, modifier = Modifier.padding(end = 4.dp))
            else if (isFromCache) Text(stringResource(R.string.status_cached), color = MaterialTheme.colorScheme.tertiary, fontSize = 9.sp, modifier = Modifier.padding(end = 4.dp))
            Text(df.format(Date(lastUpdate)), fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CurrencyCard(
    modifier: Modifier = Modifier,
    dragHandleModifier: Modifier = Modifier,
    baseCurrency: String,
    currencyCode: String,
    rateValue: Double,
    history: List<HistoryPoint>?,
    isFavorite: Boolean,
    isDragging: Boolean = false,
    elevation: androidx.compose.ui.unit.Dp = 0.dp
) {
    val reciprocal = if (rateValue != 0.0) 1.0 / rateValue else 0.0
    val rateValues = history?.map { it.rate }
    val change = if (rateValues != null && rateValues.size >= 2) rateValues.last() - rateValues[rateValues.size - 2] else null
    val changePercent = if (change != null && rateValues != null && rateValues.size >= 2 && rateValues[rateValues.size - 2] != 0.0) (change / rateValues[rateValues.size - 2]) * 100 else null
    val changeColor = when { change == null -> Color.Gray; change > 0 -> Green500; else -> Red500 }
    val animatedRate by animateFloatAsState(targetValue = rateValue.toFloat(), animationSpec = tween(400))
    val animatedRecip by animateFloatAsState(targetValue = reciprocal.toFloat(), animationSpec = tween(400))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .shadow(elevation, MaterialTheme.shapes.medium)
            .clip(MaterialTheme.shapes.medium)
            .background(Brush.verticalGradient(if (isDragging) listOf(Color(0xFF2A2A2A), Color(0xFF1E1E1E)) else listOf(Color(0xFF1E1E1E), Color(0xFF161616))))
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${currencyFlag(currencyCode)} $currencyCode", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (isFavorite) { Spacer(Modifier.width(4.dp)); Text("\u2605", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary) }
                }
                if (changePercent != null && change != null) Text("${if (change > 0) "+" else ""}${String.format(Locale.ROOT, "%.2f", changePercent)}%", color = changeColor, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(6.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)))
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("1 $baseCurrency = ${formatRate(animatedRate)}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(2.dp))
                    Text("1 $currencyCode = ${formatRate(animatedRecip)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (change != null) Text(if (change > 0) "\u25B2" else "\u25BC", color = changeColor, fontSize = 16.sp)
                        if (dragHandleModifier != Modifier) Text("  ⋮⋮", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, modifier = dragHandleModifier.padding(start = 4.dp))
                    }
                    if (rateValues != null && rateValues.isNotEmpty()) {
                        Spacer(Modifier.height(6.dp))
                        SparklineChart(data = rateValues, modifier = Modifier.width(56.dp).height(22.dp), color = changeColor)
                    }
                }
            }
        }
    }
}

private fun String.withoutLeadingMenuIcon(): String = substringAfter(' ', this).trim()

private fun formatRate(v: Float): String = when {
    v >= 100f -> String.format(Locale.ROOT, "%.1f", v)
    v >= 1f -> String.format(Locale.ROOT, "%.4f", v)
    else -> String.format(Locale.ROOT, "%.6f", v)
}
