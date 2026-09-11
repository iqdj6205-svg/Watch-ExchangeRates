package com.serhio.money.presentation.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.serhio.money.presentation.theme.MoneyGold
import com.serhio.money.presentation.theme.Red500
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MainScreen(uiState: MainUiState, isOnline: Boolean, onRefresh: () -> Unit, onOpenSettings: () -> Unit, onOpenTileConfig: () -> Unit = {}, onOpenGraphs: (String, String) -> Unit = { _, _ -> }, onOpenAlerts: () -> Unit = {}, onReorderFavorites: (List<String>) -> Unit = {}) {
    when (uiState) {
        is MainUiState.Loading -> SkeletonLoading()
        is MainUiState.Success -> MainContent(uiState, isOnline, onRefresh, onOpenSettings, onOpenTileConfig, onOpenGraphs, onOpenAlerts, onReorderFavorites)
        is MainUiState.Error -> ErrorContent(uiState.message, onRefresh)
    }
}

@Composable
private fun ErrorContent(message: String?, onRetry: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(message ?: stringResource(R.string.unknown_error), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
    }
}

@Composable
private fun SkeletonLoading() {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val shimmerOffset by infiniteTransition.animateFloat(-200f, 600f, infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart), label = "shimmer")
    val shimmerBrush = Brush.linearGradient(listOf(Color(0xFF1A1A1A), Color(0xFF2A2A2A), Color(0xFF1A1A1A)), Offset(shimmerOffset, 0f), Offset(shimmerOffset + 200f, 0f))
    Column(Modifier.fillMaxSize().padding(top = 34.dp, start = 12.dp, end = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.page_favorites), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp))
        repeat(3) { Box(Modifier.fillMaxWidth().padding(vertical = 4.dp).height(70.dp).clip(RoundedCornerShape(14.dp)).background(shimmerBrush).border(1.dp, MoneyGold.copy(alpha = 0.22f), RoundedCornerShape(14.dp))) }
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) { CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.loading), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
private fun MainContent(state: MainUiState.Success, isOnline: Boolean, onRefresh: () -> Unit, onOpenSettings: () -> Unit, onOpenTileConfig: () -> Unit, onOpenGraphs: (String, String) -> Unit, onOpenAlerts: () -> Unit, onReorderFavorites: (List<String>) -> Unit) {
    var page by remember { mutableIntStateOf(0) }
    var reorderMode by remember { mutableStateOf(false) }
    LaunchedEffect(page) { if (page != 0) reorderMode = false }
    Box(Modifier.fillMaxSize()) {
        AnimatedContent(targetState = page, transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) }, label = "mainPageContent") { selectedPage ->
            when (selectedPage) {
                0 -> FavoritesPage(state, isOnline, reorderMode, onOpenGraphs, { reorderMode = true }, { reorderMode = false }) { newOrder -> onReorderFavorites(newOrder); reorderMode = false }
                1 -> AllCurrenciesPage(state, isOnline, onOpenGraphs)
                else -> ActionsPage(onOpenSettings, onOpenTileConfig, onRefresh, onOpenAlerts)
            }
        }
        PageTabs(page, reorderMode, { page = it }, Modifier.align(Alignment.TopCenter).padding(top = 6.dp))
    }
}

@Composable
private fun PageTabs(selectedPage: Int, reorderMode: Boolean, onSelectPage: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.clip(CircleShape).background(if (reorderMode) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else Color.Black.copy(alpha = 0.28f)).border(1.dp, MoneyGold.copy(alpha = if (reorderMode) 0.45f else 0.18f), CircleShape).padding(horizontal = 6.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        listOf("★", "☆", "☰").forEachIndexed { index, label -> Box(Modifier.size(if (reorderMode && index == 0) 28.dp else 24.dp).clip(CircleShape).background(if (selectedPage == index) MaterialTheme.colorScheme.primary.copy(alpha = 0.24f) else Color.Transparent).clickable(enabled = !reorderMode) { onSelectPage(index) }, contentAlignment = Alignment.Center) { Text(label, fontSize = 11.sp, color = if (selectedPage == index || reorderMode && index == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) } }
    }
}

@Composable
private fun ActionsPage(onSettings: () -> Unit, onTileConfig: () -> Unit, onRefresh: () -> Unit, onAlerts: () -> Unit) {
    ScalingLazyColumn(state = rememberScalingLazyListState(), horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp), contentPadding = PaddingValues(top = 40.dp, bottom = 28.dp)) {
        item { Text(stringResource(R.string.menu_title), style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(bottom = 8.dp)) }
        item { ActionMenuCard("⚙", stringResource(R.string.menu_settings).withoutLeadingMenuIcon(), stringResource(R.string.menu_settings_desc), onSettings) }
        item { ActionMenuCard("□", stringResource(R.string.menu_tiles).withoutLeadingMenuIcon(), stringResource(R.string.menu_tiles_desc), onTileConfig) }
        item { ActionMenuCard("↻", stringResource(R.string.menu_refresh).withoutLeadingMenuIcon(), stringResource(R.string.menu_refresh_desc), onRefresh) }
        item { ActionMenuCard("🔔", stringResource(R.string.menu_alerts).withoutLeadingMenuIcon(), stringResource(R.string.menu_alerts_desc), onAlerts) }
    }
}

@Composable
private fun ActionMenuCard(icon: String, title: String, description: String, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 5.dp).clip(MaterialTheme.shapes.medium).background(Brush.verticalGradient(listOf(Color(0xFF1E1E1E), Color(0xFF151515)))).border(1.dp, MoneyGold.copy(alpha = 0.34f), MaterialTheme.shapes.medium).clickable { onClick() }.padding(horizontal = 14.dp, vertical = 11.dp)) {
        Column(Modifier.fillMaxWidth()) { Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(30.dp).clip(CircleShape).background(MoneyGold.copy(alpha = 0.14f)).border(1.dp, MoneyGold.copy(alpha = 0.36f), CircleShape), contentAlignment = Alignment.Center) { Text(icon, fontSize = 15.sp) }; Spacer(Modifier.width(10.dp)); Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) }; Spacer(Modifier.height(7.dp)); Text(description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 13.sp) }
    }
}

@Composable
private fun FavoritesPage(state: MainUiState.Success, isOnline: Boolean, reorderMode: Boolean, onOpenGraphs: (String, String) -> Unit, onStartReorder: () -> Unit, onCancelReorder: () -> Unit, onSaveReorder: (List<String>) -> Unit) {
    val displayed = state.interestedCurrencies.filter { state.rates.containsKey(it) }
    var editOrder by remember(reorderMode) { mutableStateOf(displayed) }
    LaunchedEffect(displayed) { if (!reorderMode) editOrder = displayed }
    if (reorderMode) FavoritesReorderList(state, isOnline, editOrder, { editOrder = it }, onCancelReorder, onSaveReorder) else {
        ScalingLazyColumn(state = rememberScalingLazyListState(), horizontalAlignment = Alignment.CenterHorizontally, contentPadding = PaddingValues(top = 40.dp, bottom = 28.dp)) {
            item { Text(stringResource(R.string.page_favorites), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 4.dp)) }
            if (displayed.isEmpty()) item { Box(Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) { Text(stringResource(R.string.empty_currencies), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface) } }
            items(displayed.size, key = { displayed[it] }) { index -> val code = displayed[index]; AnimatedCurrencyCard(index) { CurrencyCard(Modifier.clickable { onOpenGraphs(state.baseCurrency, code) }, state.baseCurrency, code, state.rates[code] ?: 0.0, state.history[code], true) } }
            if (displayed.size > 1) item { EditOrderButton(onStartReorder) }
            item { StatusFooter(isOnline, state.isFromCache, state.lastUpdate) }
        }
    }
}

@Composable
private fun FavoritesReorderList(state: MainUiState.Success, isOnline: Boolean, order: List<String>, onOrderChanged: (List<String>) -> Unit, onCancel: () -> Unit, onSave: (List<String>) -> Unit) {
    val view = LocalView.current
    val listState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(listState) { from, to -> if (from.index in order.indices && to.index in order.indices) { onOrderChanged(order.toMutableList().apply { add(to.index, removeAt(from.index)) }); view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK) } }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, contentPadding = PaddingValues(top = 40.dp, bottom = 72.dp, start = 4.dp, end = 4.dp)) {
            item { Text(stringResource(R.string.reorder_hint), fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(bottom = 6.dp)) }
            items(order.size, key = { order[it] }) { index -> val code = order[index]; ReorderableItem(reorderState, key = code) { isDragging -> val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp, label = "reorderElevation"); val interactionSource = remember { MutableInteractionSource() }; CurrencyCard(Modifier, state.baseCurrency, code, state.rates[code] ?: 0.0, state.history[code], true, elevation) { Text("⋮⋮", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, modifier = Modifier.longPressDraggableHandle(interactionSource = interactionSource, onDragStarted = { view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS) }, onDragStopped = { view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK) }).padding(start = 6.dp)) } } }
            item { StatusFooter(isOnline, state.isFromCache, state.lastUpdate) }
        }
        ReorderActions(onCancel, { onSave(order) }, Modifier.align(Alignment.BottomCenter).padding(horizontal = 12.dp, vertical = 8.dp))
    }
}

@Composable
private fun EditOrderButton(onClick: () -> Unit) { Text(stringResource(R.string.edit_order), color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)).border(1.dp, MoneyGold.copy(alpha = 0.3f), CircleShape).clickable { onClick() }.padding(horizontal = 14.dp, vertical = 8.dp)) }

@Composable
private fun ReorderActions(onCancel: () -> Unit, onSave: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().clip(CircleShape).background(Color.Black.copy(alpha = 0.68f)).border(1.dp, MoneyGold.copy(alpha = 0.28f), CircleShape).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.weight(1f).clip(CircleShape).border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), CircleShape).clickable { onCancel() }.padding(vertical = 8.dp), textAlign = TextAlign.Center)
        Text(stringResource(R.string.save), color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)).border(1.dp, MoneyGold.copy(alpha = 0.38f), CircleShape).clickable { onSave() }.padding(vertical = 8.dp), textAlign = TextAlign.Center)
    }
}

@Composable
private fun AllCurrenciesPage(state: MainUiState.Success, isOnline: Boolean, onOpenGraphs: (String, String) -> Unit) { val entries = state.rates.entries.toList(); ScalingLazyColumn(state = rememberScalingLazyListState(), horizontalAlignment = Alignment.CenterHorizontally, contentPadding = PaddingValues(top = 40.dp, bottom = 28.dp)) { item { Text(stringResource(R.string.page_all), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 4.dp)) }; items(entries.size, key = { entries[it].key }) { index -> val entry = entries[index]; AnimatedCurrencyCard(index) { CurrencyCard(Modifier.clickable { onOpenGraphs(state.baseCurrency, entry.key) }, state.baseCurrency, entry.key, entry.value, state.history[entry.key], entry.key in state.interestedCurrencies) } }; item { StatusFooter(isOnline, state.isFromCache, state.lastUpdate) } } }

@Composable
private fun AnimatedCurrencyCard(index: Int, content: @Composable () -> Unit) { val animProgress = remember { Animatable(0f) }; LaunchedEffect(index) { animProgress.snapTo(0f); animProgress.animateTo(1f, tween(300, delayMillis = index * 35, easing = LinearOutSlowInEasing)) }; Box(Modifier.graphicsLayer { alpha = animProgress.value; translationY = (1f - animProgress.value) * 20f }) { content() } }

@Composable
private fun StatusFooter(isOnline: Boolean, isFromCache: Boolean, lastUpdate: Long) { val df = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }; Column(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 2.dp), horizontalAlignment = Alignment.CenterHorizontally) { Box(Modifier.width(40.dp).height(1.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))); Spacer(Modifier.height(6.dp)); Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) { if (!isOnline) Text(stringResource(R.string.status_offline), color = MaterialTheme.colorScheme.error, fontSize = 9.sp, modifier = Modifier.padding(end = 4.dp)) else if (isFromCache) Text(stringResource(R.string.status_cached), color = MaterialTheme.colorScheme.tertiary, fontSize = 9.sp, modifier = Modifier.padding(end = 4.dp)); Text(df.format(Date(lastUpdate)), fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }

@Composable
private fun CurrencyCard(modifier: Modifier = Modifier, baseCurrency: String, currencyCode: String, rateValue: Double, history: List<HistoryPoint>?, isFavorite: Boolean, elevation: androidx.compose.ui.unit.Dp = 0.dp, trailingContent: @Composable RowScope.() -> Unit = {}) {
    val reciprocal = if (rateValue != 0.0) 1.0 / rateValue else 0.0
    val rateValues = history?.map { it.rate }
    val change = if (rateValues != null && rateValues.size >= 2) rateValues.last() - rateValues[rateValues.size - 2] else null
    val changePercent = if (change != null && rateValues != null && rateValues.size >= 2 && rateValues[rateValues.size - 2] != 0.0) (change / rateValues[rateValues.size - 2]) * 100 else null
    val changeColor = when { change == null -> Color.Gray; change > 0 -> Green500; else -> Red500 }
    val animatedRate by animateFloatAsState(rateValue.toFloat(), tween(400))
    val animatedRecip by animateFloatAsState(reciprocal.toFloat(), tween(400))
    Box(modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp).shadow(elevation, MaterialTheme.shapes.medium).clip(MaterialTheme.shapes.medium).background(Brush.verticalGradient(listOf(Color(0xFF1E1E1E), Color(0xFF161616)))).border(1.dp, MoneyGold.copy(alpha = if (elevation > 0.dp) 0.58f else 0.28f), MaterialTheme.shapes.medium)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Row(verticalAlignment = Alignment.CenterVertically) { Text("${currencyFlag(currencyCode)} $currencyCode", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); if (isFavorite) { Spacer(Modifier.width(4.dp)); Text("★", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary) } }; if (changePercent != null && change != null) Text("${if (change > 0) "+" else ""}${String.format(Locale.ROOT, "%.2f", changePercent)}%", color = changeColor, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) }; Spacer(Modifier.height(6.dp)); Box(Modifier.fillMaxWidth().height(1.dp).background(MoneyGold.copy(alpha = 0.14f))); Spacer(Modifier.height(6.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("1 $baseCurrency = ${formatRate(animatedRate)}", fontSize = 13.sp, fontWeight = FontWeight.Medium); Spacer(Modifier.height(2.dp)); Text("1 $currencyCode = ${formatRate(animatedRecip)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Column(horizontalAlignment = Alignment.End) { Row(verticalAlignment = Alignment.CenterVertically) { if (change != null) Text(if (change > 0) "▲" else "▼", color = changeColor, fontSize = 16.sp); trailingContent() }; if (!rateValues.isNullOrEmpty()) { Spacer(Modifier.height(6.dp)); SparklineChart(rateValues, Modifier.width(56.dp).height(22.dp), changeColor) } } } }
    }
}

private fun String.withoutLeadingMenuIcon(): String = substringAfter(' ', this).trim()
private fun formatRate(v: Float): String = when { v >= 100f -> String.format(Locale.ROOT, "%.1f", v); v >= 1f -> String.format(Locale.ROOT, "%.4f", v); else -> String.format(Locale.ROOT, "%.6f", v) }
