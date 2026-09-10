package com.serhio.money.presentation.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.serhio.money.domain.model.Alert
import com.serhio.money.presentation.theme.CardBottom
import com.serhio.money.presentation.theme.CardTop
import com.serhio.money.presentation.theme.MoneyGold
import com.serhio.money.presentation.theme.SubtextGray
import java.util.Locale

@Composable
fun AlertsScreen(viewModel: AlertsViewModel) {
    val alerts by viewModel.alerts.collectAsState()
    val baseCurrency by viewModel.baseCurrency.collectAsState()
    val alertCurrencies by viewModel.alertCurrencies.collectAsState()
    var showComposer by remember { mutableStateOf(false) }

    if (showComposer) {
        AlertComposer(
            baseCurrency = baseCurrency,
            currencies = alertCurrencies,
            onDismiss = { showComposer = false },
            onAdd = { code, rate, above ->
                viewModel.addAlert(code, rate, above)
                showComposer = false
            }
        )
    } else {
        AlertsList(
            baseCurrency = baseCurrency,
            alerts = alerts,
            onAdd = { showComposer = true },
            onToggle = { id, enabled -> viewModel.toggleAlert(id, enabled) },
            onDelete = viewModel::deleteAlert
        )
    }
}

@Composable
private fun AlertsList(baseCurrency: String, alerts: List<Alert>, onAdd: () -> Unit, onToggle: (Long, Boolean) -> Unit, onDelete: (Alert) -> Unit) {
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = rememberScalingLazyListState(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(top = 22.dp, bottom = 26.dp, start = 10.dp, end = 10.dp)
    ) {
        item {
            Text(stringResource(R.string.alerts_title), style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(bottom = 6.dp))
        }
        item { AddAlertCard(baseCurrency = baseCurrency, onClick = onAdd) }
        if (alerts.isEmpty()) {
            item {
                PremiumPanel {
                    Text("◇", fontSize = 22.sp, color = MoneyGold, modifier = Modifier.padding(bottom = 4.dp))
                    Text(stringResource(R.string.alerts_empty), style = MaterialTheme.typography.bodySmall, color = SubtextGray, textAlign = TextAlign.Center)
                }
            }
        }
        items(alerts.size, key = { alerts[it].id }) { index ->
            AlertCard(baseCurrency = baseCurrency, alert = alerts[index], onToggle = { onToggle(alerts[index].id, it) }, onDelete = { onDelete(alerts[index]) })
        }
    }
}

@Composable
private fun AddAlertCard(baseCurrency: String, onClick: () -> Unit) {
    PremiumPanel(modifier = Modifier.clickable { onClick() }) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(Modifier.size(32.dp).clip(CircleShape).background(MoneyGold.copy(alpha = 0.16f)).border(1.dp, MoneyGold.copy(alpha = 0.38f), CircleShape), contentAlignment = Alignment.Center) {
                Text("＋", fontSize = 18.sp, color = MoneyGold)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.alert_add), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("$baseCurrency → ${stringResource(R.string.alert_add_desc)}", fontSize = 10.sp, color = SubtextGray)
            }
        }
    }
}

@Composable
private fun AlertCard(baseCurrency: String, alert: Alert, onToggle: (Boolean) -> Unit, onDelete: () -> Unit) {
    PremiumPanel {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(34.dp).clip(CircleShape).background(if (alert.isEnabled) MoneyGold.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.05f)).border(1.dp, if (alert.isEnabled) MoneyGold.copy(alpha = 0.42f) else Color.White.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                Text(if (alert.isAbove) "≥" else "≤", fontSize = 17.sp, color = if (alert.isEnabled) MoneyGold else SubtextGray, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("$baseCurrency/${alert.currencyCode}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (alert.isEnabled) MaterialTheme.colorScheme.onSurface else SubtextGray)
                Text("1 $baseCurrency ${alert.direction} ${String.format(Locale.US, "%.4f", alert.targetRate)} ${alert.currencyCode}", fontSize = 10.sp, color = MoneyGold, maxLines = 1)
                if (alert.triggeredAt != null) Text(stringResource(R.string.alert_triggered), fontSize = 10.sp, color = MaterialTheme.colorScheme.tertiary)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (alert.isEnabled) stringResource(R.string.alert_on) else stringResource(R.string.alert_off), fontSize = 10.sp, color = if (alert.isEnabled) MoneyGold else SubtextGray, modifier = Modifier.clip(CircleShape).clickable { onToggle(!alert.isEnabled) }.padding(horizontal = 8.dp, vertical = 5.dp))
                Text("×", fontSize = 16.sp, color = MaterialTheme.colorScheme.error, modifier = Modifier.clip(CircleShape).clickable { onDelete() }.padding(horizontal = 8.dp, vertical = 2.dp))
            }
        }
    }
}

@Composable
private fun AlertComposer(baseCurrency: String, currencies: List<String>, onDismiss: () -> Unit, onAdd: (String, Double, Boolean) -> Unit) {
    var selectedCurrency by remember(currencies) { mutableStateOf(currencies.firstOrNull() ?: "EUR") }
    var isAbove by remember { mutableStateOf(true) }
    var targetRate by remember { mutableStateOf("") }
    val canSave = targetRate.toDoubleOrNull() != null

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = rememberScalingLazyListState(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(top = 20.dp, bottom = 26.dp, start = 10.dp, end = 10.dp)
    ) {
        item {
            Text(stringResource(R.string.alert_add), style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(bottom = 6.dp))
        }
        item {
            PremiumPanel {
                Text("1 $baseCurrency → $selectedCurrency", fontSize = 13.sp, color = MoneyGold, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
                CurrencyGrid(currencies = currencies, selected = selectedCurrency, onSelect = { selectedCurrency = it })
            }
        }
        item {
            PremiumPanel {
                Text(stringResource(R.string.alert_direction), fontSize = 12.sp, color = SubtextGray, modifier = Modifier.padding(bottom = 6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChoicePill("≥", stringResource(R.string.alert_above), selected = isAbove, modifier = Modifier.weight(1f)) { isAbove = true }
                    ChoicePill("≤", stringResource(R.string.alert_below), selected = !isAbove, modifier = Modifier.weight(1f)) { isAbove = false }
                }
            }
        }
        item {
            PremiumPanel {
                Text(stringResource(R.string.alert_rate), fontSize = 12.sp, color = SubtextGray)
                Text(targetRate.ifBlank { "0" }, fontSize = 24.sp, color = MoneyGold, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(vertical = 6.dp))
                NumericPad(value = targetRate, onValueChange = { targetRate = it })
            }
        }
        item {
            PremiumPanel {
                Text(
                    "1 $baseCurrency ${if (isAbove) "≥" else "≤"} ${targetRate.ifBlank { "0" }} $selectedCurrency",
                    fontSize = 11.sp,
                    color = SubtextGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionPill(text = stringResource(R.string.cancel), selected = false, modifier = Modifier.weight(1f), onClick = onDismiss)
                ActionPill(text = stringResource(R.string.confirm), selected = canSave, enabled = canSave, modifier = Modifier.weight(1f)) {
                    targetRate.toDoubleOrNull()?.let { onAdd(selectedCurrency, it, isAbove) }
                }
            }
        }
    }
}

@Composable
private fun CurrencyGrid(currencies: List<String>, selected: String, onSelect: (String) -> Unit) {
    currencies.chunked(3).forEach { row ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            row.forEach { code -> ChoicePill(icon = if (selected == code) "●" else "", text = code, selected = selected == code, modifier = Modifier.weight(1f)) { onSelect(code) } }
            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
        }
        Spacer(Modifier.height(6.dp))
    }
}

@Composable
private fun NumericPad(value: String, onValueChange: (String) -> Unit) {
    val keys = listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"), listOf("C", "0", "⌫"), listOf("."))
    keys.forEach { row ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            row.forEach { key ->
                Box(Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = 0.05f)).border(1.dp, MoneyGold.copy(alpha = 0.16f), RoundedCornerShape(14.dp)).clickable {
                    when (key) {
                        "⌫" -> onValueChange(value.dropLast(1))
                        "C" -> onValueChange("")
                        "." -> if (!value.contains('.')) onValueChange(if (value.isBlank()) "0." else value + ".")
                        else -> onValueChange((value + key).trimStart('0').ifBlank { "0" }.take(10))
                    }
                }.padding(vertical = 9.dp), contentAlignment = Alignment.Center) { Text(key, fontSize = 14.sp, color = if (key == "⌫" || key == "C") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) }
            }
            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
        }
        Spacer(Modifier.height(6.dp))
    }
}

@Composable
private fun ChoicePill(icon: String, text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(modifier.clip(RoundedCornerShape(16.dp)).background(if (selected) MoneyGold.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.04f)).border(1.dp, if (selected) MoneyGold.copy(alpha = 0.45f) else MoneyGold.copy(alpha = 0.14f), RoundedCornerShape(16.dp)).clickable { onClick() }.padding(horizontal = 8.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
        Text((if (icon.isNotBlank()) "$icon " else "") + text, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, color = if (selected) MoneyGold else MaterialTheme.colorScheme.onSurface, maxLines = 1)
    }
}

@Composable
private fun ActionPill(text: String, selected: Boolean, enabled: Boolean = true, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(modifier.clip(CircleShape).background(if (selected) MoneyGold.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.04f)).border(1.dp, if (selected) MoneyGold.copy(alpha = 0.42f) else Color.White.copy(alpha = 0.12f), CircleShape).clickable(enabled = enabled) { onClick() }.padding(vertical = 9.dp), contentAlignment = Alignment.Center) {
        Text(text, color = if (selected) MoneyGold else SubtextGray, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
private fun PremiumPanel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth().padding(vertical = 5.dp).clip(RoundedCornerShape(22.dp)).background(Brush.verticalGradient(listOf(CardTop, CardBottom))).border(1.dp, MoneyGold.copy(alpha = 0.34f), RoundedCornerShape(22.dp)).padding(horizontal = 12.dp, vertical = 11.dp), horizontalAlignment = Alignment.CenterHorizontally) { content() }
}
