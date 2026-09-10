package com.serhio.money.presentation.config

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
import com.serhio.money.presentation.theme.CardBottom
import com.serhio.money.presentation.theme.CardTop
import com.serhio.money.presentation.theme.MoneyGold
import com.serhio.money.presentation.theme.SubtextGray

@Composable
fun TileComplicationConfigScreen(
    tileCurrency: String,
    tileMode: String,
    complicationCurrency: String,
    complicationMode: String,
    onTileCurrencyChange: (String) -> Unit,
    onTileModeChange: (String) -> Unit,
    onComplicationCurrencyChange: (String) -> Unit,
    onComplicationModeChange: (String) -> Unit,
    onBack: () -> Unit
) {
    val listState = rememberScalingLazyListState()
    val currencies = listOf("USD", "EUR", "GBP", "JPY", "CHF", "AUD", "CAD", "CNY", "PLN", "UAH", "CZK", "SEK", "INR", "KRW", "SGD", "TRY")
    val modes = listOf("rate" to R.string.mode_rate, "change" to R.string.mode_change_pct, "arrow" to R.string.mode_arrow, "date" to R.string.mode_date)

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(top = 18.dp, bottom = 28.dp, start = 10.dp, end = 10.dp)
    ) {
        item {
            Text(
                stringResource(R.string.section_tile_config),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        item {
            PremiumConfigCard(title = stringResource(R.string.section_tile_currency)) {
                CurrencyGrid(currencies, tileCurrency, onTileCurrencyChange)
            }
        }

        item {
            PremiumConfigCard(title = stringResource(R.string.section_tile_mode)) {
                ModeGrid(modes, tileMode, onTileModeChange)
            }
        }

        item {
            PremiumConfigCard(title = stringResource(R.string.section_complication_config)) {
                Text(
                    stringResource(R.string.complication_follows_top_favorite),
                    fontSize = 11.sp,
                    color = MoneyGold,
                    lineHeight = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                Text(stringResource(R.string.section_tile_mode), fontSize = 11.sp, color = SubtextGray, modifier = Modifier.padding(bottom = 6.dp))
                ModeGrid(modes, complicationMode, onComplicationModeChange)
            }
        }

        item {
            Text(
                stringResource(R.string.back),
                color = MoneyGold,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(CircleShape)
                    .border(1.dp, MoneyGold.copy(alpha = 0.28f), CircleShape)
                    .clickable { onBack() }
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            )
        }
    }
}

@Composable
private fun PremiumConfigCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.verticalGradient(listOf(CardTop, CardBottom)))
            .border(1.dp, MoneyGold.copy(alpha = 0.34f), RoundedCornerShape(22.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        content()
    }
}

@Composable
private fun CurrencyGrid(currencies: List<String>, selected: String, onSelect: (String) -> Unit) {
    currencies.chunked(4).forEach { row ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            row.forEach { currency ->
                CompactChip(label = currency, selected = currency == selected, modifier = Modifier.weight(1f)) { onSelect(currency) }
            }
            repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
        }
        Spacer(Modifier.height(5.dp))
    }
}

@Composable
private fun ModeGrid(modes: List<Pair<String, Int>>, selected: String, onSelect: (String) -> Unit) {
    modes.chunked(2).forEach { row ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            row.forEach { (key, labelRes) ->
                CompactChip(label = stringResource(labelRes), selected = key == selected, modifier = Modifier.weight(1f)) { onSelect(key) }
            }
        }
        Spacer(Modifier.height(6.dp))
    }
}

@Composable
private fun CompactChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(15.dp))
            .background(if (selected) MoneyGold.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.04f))
            .border(1.dp, if (selected) MoneyGold.copy(alpha = 0.45f) else MoneyGold.copy(alpha = 0.14f), RoundedCornerShape(15.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 10.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, color = if (selected) MoneyGold else MaterialTheme.colorScheme.onSurface, maxLines = 1)
    }
}
