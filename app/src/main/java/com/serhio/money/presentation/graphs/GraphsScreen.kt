package com.serhio.money.presentation.graphs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.*
import com.serhio.money.R
import java.text.SimpleDateFormat
import java.util.*

enum class ChartPeriod(@androidx.annotation.StringRes val labelRes: Int, val millis: Long) {
    DAY(R.string.period_24h, 24 * 60 * 60 * 1000L),
    WEEK(R.string.period_7d, 7 * 24 * 60 * 60 * 1000L),
    MONTH(R.string.period_30d, 30 * 24 * 60 * 60 * 1000L),
    QUARTER(R.string.period_90d, 90 * 24 * 60 * 60 * 1000L),
    YEAR(R.string.period_1y, 365 * 24 * 60 * 60 * 1000L),
    ALL(R.string.period_all, Long.MAX_VALUE)
}

data class ChartDataPoint(
    val timestamp: Long,
    val value: Double
)

@Composable
fun GraphsScreen(
    baseCurrency: String,
    targetCurrency: String,
    history: List<ChartDataPoint>,
    onBack: () -> Unit
) {
    var selectedPeriod by remember { mutableStateOf(ChartPeriod.WEEK) }
    val listState = rememberScalingLazyListState()

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "$baseCurrency/$targetCurrency",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ChartPeriod.entries.forEach { period ->
                    val selected = period == selectedPeriod
                    Button(
                        onClick = { selectedPeriod = period },
                        modifier = Modifier.height(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected) MaterialTheme.colorScheme.primary else Color(0xFF2C2C2C),
                            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Text(stringResource(period.labelRes), fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            val filteredData = filterDataByPeriod(history, selectedPeriod)
            if (filteredData.size >= 2) {
                val maxVal = filteredData.maxOf { it.value }
                val minVal = filteredData.minOf { it.value }
                val change = filteredData.last().value - filteredData.first().value
                val changePercent = if (filteredData.first().value != 0.0)
                    (change / filteredData.first().value) * 100 else 0.0

                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.graph_change,
                            String.format(Locale.ROOT, "%.4f", change),
                            String.format(Locale.ROOT, "%.2f", changePercent)),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (change >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LineChart(
                        data = filteredData,
                        modifier = Modifier.fillMaxWidth().height(120.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val dateFormat = remember { SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()) }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = dateFormat.format(Date(filteredData.first().timestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp
                        )
                        Text(
                            text = dateFormat.format(Date(filteredData.last().timestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(R.string.graph_high_low,
                            String.format(Locale.ROOT, "%.4f", maxVal),
                            String.format(Locale.ROOT, "%.4f", minVal)),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.graph_no_data),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onBack) {
                Text(stringResource(R.string.back))
            }
        }
    }
}

private fun filterDataByPeriod(data: List<ChartDataPoint>, period: ChartPeriod): List<ChartDataPoint> {
    if (period == ChartPeriod.ALL) return data
    val cutoff = System.currentTimeMillis() - period.millis
    return data.filter { it.timestamp >= cutoff }
}

@Composable
private fun LineChart(
    data: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF4CAF50)
) {
    if (data.size < 2) return

    Canvas(modifier = modifier) {
        val min = data.minOf { it.value }
        val max = data.maxOf { it.value }
        val range = (max - min).coerceAtLeast(0.0001)
        val width = size.width
        val height = size.height
        val stepX = width / (data.size - 1)

        val path = Path()
        data.forEachIndexed { index, point ->
            val x = index * stepX
            val y = height - ((point.value - min) / range * height).toFloat()
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx())
        )

        data.forEachIndexed { index, point ->
            val x = index * stepX
            val y = height - ((point.value - min) / range * height).toFloat()
            drawCircle(
                color = lineColor,
                radius = 2.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}