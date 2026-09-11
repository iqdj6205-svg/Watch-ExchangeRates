package com.serhio.money.presentation.settings

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.*
import com.serhio.money.R
import com.serhio.money.presentation.components.currencyFlag
import com.serhio.money.presentation.theme.CardBottom
import com.serhio.money.presentation.theme.CardTop
import com.serhio.money.presentation.theme.MoneyGold
import com.serhio.money.presentation.theme.SubtextGray

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val baseCurrency by viewModel.baseCurrency.collectAsState()
    val interestedCurrencies by viewModel.interestedCurrencies.collectAsState()
    val updateInterval by viewModel.updateInterval.collectAsState()
    val alertInterval by viewModel.alertInterval.collectAsState()
    val listState = rememberScalingLazyListState()
    val currencies = remember { listOf("USD", "EUR", "GBP", "JPY", "CHF", "AUD", "CAD", "CNY", "PLN", "UAH", "CZK", "DKK", "NOK", "SEK", "HUF", "RON", "INR", "KRW", "SGD", "HKD", "MXN", "BRL", "ZAR", "TRY", "RUB", "ILS", "NZD", "BTC", "ETH", "XAU", "XAG") }
    val priorityCurrencies = remember(interestedCurrencies, baseCurrency) { (listOf(baseCurrency) + interestedCurrencies + listOf("USD", "EUR", "PLN", "UAH", "GBP", "CHF")).distinct() }
    val otherCurrencies = remember(priorityCurrencies) { currencies.filterNot { it in priorityCurrencies } }
    var showAllBase by remember { mutableStateOf(false) }
    var showAllInterested by remember { mutableStateOf(false) }

    ScalingLazyColumn(modifier = Modifier.fillMaxSize(), state = listState, horizontalAlignment = Alignment.CenterHorizontally, contentPadding = PaddingValues(top = 18.dp, bottom = 24.dp, start = 10.dp, end = 10.dp)) {
        item { Text(stringResource(R.string.menu_settings).removePrefix("⚙ "), style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(bottom = 4.dp)) }
        item { PremiumSectionCard(title = stringResource(R.string.section_base), subtitle = stringResource(R.string.settings_base_hint)) { CurrencyChipGrid(if (showAllBase) currencies else priorityCurrencies, listOf(baseCurrency), 2, viewModel::setBaseCurrency); ShowToggle(showAllBase) { showAllBase = !showAllBase } } }
        item { PremiumSectionCard(title = stringResource(R.string.section_interested), subtitle = stringResource(R.string.settings_interested_hint)) { CurrencyChipGrid(if (showAllInterested) currencies else priorityCurrencies + otherCurrencies.take(6), interestedCurrencies, 2, viewModel::toggleInterestedCurrency); ShowToggle(showAllInterested) { showAllInterested = !showAllInterested } } }
        item { PremiumSectionCard(title = stringResource(R.string.section_interval), subtitle = stringResource(R.string.settings_interval_hint)) { IntervalPicker(updateInterval = updateInterval, onSelect = viewModel::setUpdateInterval) } }
        item { PremiumSectionCard(title = stringResource(R.string.section_alert_interval), subtitle = stringResource(R.string.settings_alert_interval_hint)) { IntervalPicker(updateInterval = alertInterval, onSelect = viewModel::setAlertInterval, alertMode = true) } }
    }
}

@Composable
private fun ShowToggle(expanded: Boolean, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(if (expanded) stringResource(R.string.settings_show_less) else stringResource(R.string.settings_show_all), color = MoneyGold, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clip(CircleShape).border(1.dp, MoneyGold.copy(alpha = 0.28f), CircleShape).clickable { onClick() }.padding(horizontal = 12.dp, vertical = 6.dp))
    }
}

@Composable
private fun IntervalPicker(updateInterval: Long, onSelect: (Long) -> Unit, alertMode: Boolean = false) {
    var showPopup by remember { mutableStateOf(false) }
    val intervals = if (alertMode) listOf("15m" to (15 * 60 * 1000L), "30m" to (30 * 60 * 1000L), "1h" to (60 * 60 * 1000L), "2h" to (2 * 60 * 60 * 1000L), "6h" to (6 * 60 * 60 * 1000L)) else listOf("15m" to (15 * 60 * 1000L), "30m" to (30 * 60 * 1000L), "1h" to (60 * 60 * 1000L), "2h" to (2 * 60 * 60 * 1000L), "6h" to (6 * 60 * 60 * 1000L), "12h" to (12 * 60 * 60 * 1000L), "24h" to (24 * 60 * 60 * 1000L))
    val currentLabel = intervals.find { it.second == updateInterval }?.first ?: "—"
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$currentLabel  ▾", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MoneyGold, modifier = Modifier.clip(CircleShape).border(1.dp, MoneyGold.copy(alpha = 0.32f), CircleShape).clickable { showPopup = !showPopup }.padding(horizontal = 18.dp, vertical = 8.dp))
        if (showPopup) Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color(0xEE0B1020)).border(1.dp, MoneyGold.copy(alpha = 0.26f), RoundedCornerShape(18.dp)).padding(6.dp)) {
            intervals.forEach { (label, ms) ->
                val selected = ms == updateInterval
                Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable { onSelect(ms); showPopup = false }.background(if (selected) MoneyGold.copy(alpha = 0.16f) else Color.Transparent).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (selected) "●" else "○", color = if (selected) MoneyGold else SubtextGray, modifier = Modifier.padding(end = 8.dp))
                    Text(label, fontSize = 13.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
    }
}

@Composable
private fun PremiumSectionCard(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp).clip(RoundedCornerShape(22.dp)).background(Brush.verticalGradient(listOf(CardTop, CardBottom))).border(1.dp, MoneyGold.copy(alpha = 0.34f), RoundedCornerShape(22.dp)).padding(horizontal = 12.dp, vertical = 10.dp)) {
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(subtitle, fontSize = 10.sp, color = SubtextGray, modifier = Modifier.padding(bottom = 8.dp))
        content()
    }
}

@Composable
private fun CurrencyChipGrid(currencies: List<String>, selected: List<String>, columns: Int, onClick: (String) -> Unit) {
    currencies.chunked(columns).forEach { row ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            row.forEach { currency ->
                val isSelected = currency in selected
                Row(modifier = Modifier.weight(1f).padding(vertical = 3.dp).clip(RoundedCornerShape(16.dp)).background(if (isSelected) MoneyGold.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.04f)).border(1.dp, if (isSelected) MoneyGold.copy(alpha = 0.42f) else MoneyGold.copy(alpha = 0.14f), RoundedCornerShape(16.dp)).clickable { onClick(currency) }.padding(horizontal = 9.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (isSelected) "●" else "", color = MoneyGold, fontSize = 8.sp, modifier = Modifier.width(10.dp))
                    Text("${currencyFlag(currency)} $currency", fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) MoneyGold else MaterialTheme.colorScheme.onSurface)
                }
            }
            repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}
