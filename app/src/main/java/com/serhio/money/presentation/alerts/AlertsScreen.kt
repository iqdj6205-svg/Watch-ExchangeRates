package com.serhio.money.presentation.alerts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.*
import com.serhio.money.R
import com.serhio.money.domain.model.Alert
import com.serhio.money.presentation.theme.SubtextGray

@Composable
fun AlertsScreen(
    viewModel: AlertsViewModel
) {
    val alerts by viewModel.alerts.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val listState = rememberScalingLazyListState()

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            ListHeader {
                Text(stringResource(R.string.alerts_title), fontWeight = FontWeight.Bold)
            }
        }

        item {
            TextButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                Text("+ ${stringResource(R.string.alert_add)}", fontSize = 13.sp)
            }
        }

        if (alerts.isEmpty()) {
            item {
                Box(
                    Modifier.fillMaxWidth().padding(top = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.alerts_empty),
                        style = MaterialTheme.typography.bodySmall,
                        color = SubtextGray
                    )
                }
            }
        }

        items(alerts.size) { index ->
            val alert = alerts[index]
            AlertItem(
                alert = alert,
                onToggle = { viewModel.toggleAlert(alert.id, it) },
                onDelete = { viewModel.deleteAlert(alert) }
            )
        }
    }

    if (showAddDialog) {
        AddAlertDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { code, rate, above ->
                viewModel.addAlert(code, rate, above)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AlertItem(
    alert: Alert,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        onClick = { }
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    alert.description,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                if (alert.triggeredAt != null) {
                    Text(
                        stringResource(R.string.alert_triggered),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (alert.isEnabled) "ON" else "OFF",
                    fontSize = 10.sp,
                    color = if (alert.isEnabled) MaterialTheme.colorScheme.primary else SubtextGray,
                    modifier = Modifier.clickable { onToggle(!alert.isEnabled) }
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "\u2715",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.clickable { onDelete() }
                )
            }
        }
    }
}

@Composable
private fun AddAlertDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Double, Boolean) -> Unit
) {
    var currencyIndex by remember { mutableIntStateOf(0) }
    var targetRate by remember { mutableStateOf("") }
    var isAbove by remember { mutableStateOf(true) }

    val currencies = listOf(
        "EUR", "GBP", "JPY", "CHF", "AUD", "CAD", "CNY",
        "PLN", "UAH", "BTC", "ETH", "XAU"
    )

    Card(
        onClick = {},
        modifier = Modifier.padding(16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.alert_add), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))

            Text(stringResource(R.string.alert_currency), fontSize = 11.sp, color = SubtextGray)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                currencies.take(6).forEachIndexed { index, code ->
                    val selected = currencyIndex == index
                    Text(
                        code,
                        fontSize = 10.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .clickable { currencyIndex = index }
                            .padding(4.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(stringResource(R.string.alert_rate), fontSize = 11.sp, color = SubtextGray)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("0.", "1", "2", "3", "5", ".").forEach { char ->
                    Text(
                        char,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .clickable { targetRate = targetRate + char }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
            }
            Text(
                targetRate.ifEmpty { "0" },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(8.dp))

            Text(stringResource(R.string.alert_direction), fontSize = 11.sp, color = SubtextGray)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = { isAbove = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "\u2265 Above",
                        color = if (isAbove) MaterialTheme.colorScheme.primary else SubtextGray
                    )
                }
                TextButton(
                    onClick = { isAbove = false },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "\u2264 Below",
                        color = if (!isAbove) MaterialTheme.colorScheme.primary else SubtextGray
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.cancel))
                }
                TextButton(
                    onClick = {
                        targetRate.toDoubleOrNull()?.let { rate ->
                            onAdd(currencies[currencyIndex], rate, isAbove)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.confirm))
                }
            }
        }
    }
}
