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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
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
            val raw = filterDataByPeriod(history, selectedPeriod)
            val sampled = sampleData(raw, selectedPeriod)
            if (sampled.size >= 2) {
                val maxVal = sampled.maxOf { it.value }
                val minVal = sampled.minOf { it.value }
                val firstVal = sampled.first().value
                val lastVal = sampled.last().value
                val change = lastVal - firstVal
                val changePercent = if (firstVal != 0.0) (change / firstVal) * 100 else 0.0
                val isUp = change >= 0
                val changeColor = if (isUp) Color(0xFF4CAF50) else Color(0xFFF44336)

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(10.dp))
                    Text("$baseCurrency/$targetCurrency",
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(2.dp))
                    Text(String.format(Locale.ROOT, "%.4f", lastVal),
                        fontSize = 22.sp, fontWeight = FontWeight.Bold, color = changeColor)
                    Text("${if (isUp) "\u25B2" else "\u25BC"} ${String.format(Locale.ROOT, "%.2f", changePercent)}%",
                        fontSize = 12.sp, color = changeColor)
                    Spacer(Modifier.height(6.dp))
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("H: ${String.format(Locale.ROOT, "%.4f", maxVal)}",
                            fontSize = 10.sp, color = Color(0xFF4CAF50))
                        Text("L: ${String.format(Locale.ROOT, "%.4f", minVal)}",
                            fontSize = 10.sp, color = Color(0xFFF44336))
                    }
                }
            }
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
                        modifier = Modifier.height(34.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected) MaterialTheme.colorScheme.primary else Color(0xFF2C2C2C),
                            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) { Text(stringResource(period.labelRes), fontSize = 11.sp) }
                }
            }
        }

        item {
            val raw = filterDataByPeriod(history, selectedPeriod)
            val sampled = sampleData(raw, selectedPeriod)
            if (sampled.size >= 2) {
                val lineColor = if (sampled.last().value >= sampled.first().value)
                    Color(0xFF4CAF50) else Color(0xFFF44336)
                val fillColor = lineColor.copy(alpha = 0.1f)
                val maxVal = sampled.maxOf { it.value }
                val minVal = sampled.minOf { it.value }
                val dateFmt = remember { SimpleDateFormat("MM/dd", Locale.getDefault()) }

                Chart(
                    data = sampled,
                    lineColor = lineColor,
                    fillColor = fillColor,
                    minVal = minVal,
                    maxVal = maxVal,
                    modifier = Modifier.fillMaxWidth().height(140.dp).padding(horizontal = 2.dp)
                )

                Spacer(Modifier.height(2.dp))

                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(dateFmt.format(Date(sampled.first().timestamp)),
                        fontSize = 8.sp, color = Color.Gray)
                    Text(dateFmt.format(Date(sampled.last().timestamp)),
                        fontSize = 8.sp, color = Color.Gray)
                }
            } else {
                Text(stringResource(R.string.graph_no_data),
                    modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall)
            }
        }

        item {
            Spacer(Modifier.height(4.dp))
            Button(onClick = onBack, modifier = Modifier.padding(bottom = 8.dp)) {
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

private fun sampleData(data: List<ChartDataPoint>, period: ChartPeriod): List<ChartDataPoint> {
    if (data.size <= 60) return data
    return when (period) {
        ChartPeriod.DAY -> data.filterIndexed { i, _ -> i % (data.size / 24).coerceAtLeast(1) == 0 || i == data.size - 1 }
        ChartPeriod.WEEK -> data.filterIndexed { i, _ -> i % (data.size / 28).coerceAtLeast(1) == 0 || i == data.size - 1 }
        ChartPeriod.MONTH, ChartPeriod.QUARTER -> data.filterIndexed { i, _ -> i % (data.size / 30).coerceAtLeast(1) == 0 || i == data.size - 1 }
        ChartPeriod.YEAR -> data.filterIndexed { i, _ -> i % (data.size / 52).coerceAtLeast(1) == 0 || i == data.size - 1 }
        ChartPeriod.ALL -> data.filterIndexed { i, _ -> i % (data.size / 60).coerceAtLeast(1) == 0 || i == data.size - 1 }
    }
}

@Composable
private fun Chart(
    data: List<ChartDataPoint>,
    lineColor: Color,
    fillColor: Color,
    minVal: Double,
    maxVal: Double,
    modifier: Modifier = Modifier
) {
    val labelPaint = remember {
        android.graphics.Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = 18f  // px, will be adjusted in Canvas
            textAlign = android.graphics.Paint.Align.LEFT
        }
    }
    val dash = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)

    Canvas(modifier = modifier) {
        val range = (maxVal - minVal).coerceAtLeast(0.0001)
        val w = size.width
        val h = size.height
        val labelW = 52.dp.toPx()
        val chartL = labelW
        val chartW = (w - chartL).coerceAtLeast(1f)
        val stepX = chartW / (data.size - 1).coerceAtLeast(1)
        val gridColor = Color(0xFF555555)

        labelPaint.textSize = 6.dp.toPx()

        // horizontal grid lines with Y labels
        val n = 4
        for (i in 0..n) {
            val y = h * i / n
            drawLine(gridColor, Offset(chartL, y), Offset(w, y), strokeWidth = 0.5f, pathEffect = dash)
            val label = String.format(Locale.ROOT, "%.2f", maxVal - (range * i / n))
            drawContext.canvas.nativeCanvas.drawText(label, chartL - 2.dp.toPx(), y + 2.dp.toPx(), labelPaint)
        }

        // line path
        val path = Path()
        data.forEachIndexed { idx, pt ->
            val x = chartL + idx * stepX
            val y = h - ((pt.value - minVal) / range * h).toFloat()
            if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        // area fill
        val fillPath = Path().apply {
            addPath(path)
            lineTo(chartL + (data.size - 1) * stepX, h)
            lineTo(chartL, h)
            close()
        }
        drawPath(fillPath, fillColor)

        // stroke
        drawPath(path, lineColor, style = Stroke(width = 2.5.dp.toPx()))

        // last point highlight
        val lx = chartL + (data.size - 1) * stepX
        val ly = h - ((data.last().value - minVal) / range * h).toFloat()
        drawCircle(lineColor, radius = 3.5.dp.toPx(), center = Offset(lx, ly))
        drawCircle(Color.Black, radius = 1.5.dp.toPx(), center = Offset(lx, ly))
    }
}
