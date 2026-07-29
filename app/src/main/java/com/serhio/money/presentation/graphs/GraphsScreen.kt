package com.serhio.money.presentation.graphs

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.*
import com.serhio.money.R
import com.serhio.money.presentation.theme.Green500
import com.serhio.money.presentation.theme.Red500
import com.serhio.money.presentation.theme.SubtextGray
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.min

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
            if (raw.size >= 2) {
                val data = downsample(raw, selectedPeriod)
                val maxVal = data.maxOf { it.value }
                val minVal = data.minOf { it.value }
                val firstVal = data.first().value
                val lastVal = data.last().value
                val change = lastVal - firstVal
                val changePercent = if (firstVal != 0.0) (change / firstVal) * 100 else 0.0
                val isUp = change >= 0
                val changeColor = if (isUp) Green500 else Red500

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(8.dp))
                    Text("$baseCurrency/$targetCurrency",
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(2.dp))
                    Text(formatValue(lastVal),
                        fontSize = 22.sp, fontWeight = FontWeight.Bold, color = changeColor)
                    Text("${if (isUp) "\u25B2" else "\u25BC"} ${String.format(Locale.ROOT, "%.2f", changePercent)}%",
                        fontSize = 12.sp, color = changeColor)
                    Spacer(Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("H: ${formatValue(maxVal)}", fontSize = 10.sp, color = Green500)
                        Text("L: ${formatValue(minVal)}", fontSize = 10.sp, color = Red500)
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ChartPeriod.entries.forEach { period ->
                    val selected = period == selectedPeriod
                    Button(
                        onClick = { selectedPeriod = period },
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected) MaterialTheme.colorScheme.primary else Color(0xFF2C2C2C),
                            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                    ) { Text(stringResource(period.labelRes), fontSize = 10.sp) }
                }
            }
        }

        item {
            val raw = filterDataByPeriod(history, selectedPeriod)
            if (raw.size >= 2) {
                val data = downsample(raw, selectedPeriod)
                val lineColor = if (data.last().value >= data.first().value) Green500 else Red500
                val maxVal = data.maxOf { it.value }
                val minVal = data.minOf { it.value }

                ModernChart(
                    data = data,
                    lineColor = lineColor,
                    minVal = minVal,
                    maxVal = maxVal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(horizontal = 2.dp)
                        .clip(RoundedCornerShape(24.dp))
                )
            } else {
                Text(stringResource(R.string.graph_no_data),
                    modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall)
            }
        }

        item {
            val raw = filterDataByPeriod(history, selectedPeriod)
            if (raw.size >= 2) {
                val data = downsample(raw, selectedPeriod)
                val changeColor = if (data.last().value >= data.first().value) Green500 else Red500

                Spacer(Modifier.height(4.dp))
                DetailTable(
                    data = data,
                    period = selectedPeriod,
                    changeColor = changeColor
                )
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

@Composable
private fun DetailTable(
    data: List<ChartDataPoint>,
    period: ChartPeriod,
    changeColor: Color
) {
    val detailData = prepareDetailData(data, period)
    val dateFmt = remember(period) {
        when (period) {
            ChartPeriod.DAY -> SimpleDateFormat("HH:mm", Locale.getDefault())
            ChartPeriod.WEEK -> SimpleDateFormat("EEE dd", Locale.getDefault())
            ChartPeriod.MONTH -> SimpleDateFormat("dd MMM", Locale.getDefault())
            ChartPeriod.QUARTER -> SimpleDateFormat("dd MMM", Locale.getDefault())
            ChartPeriod.YEAR -> SimpleDateFormat("MMM yy", Locale.getDefault())
            ChartPeriod.ALL -> SimpleDateFormat("MMM yy", Locale.getDefault())
        }
    }

    val firstVal = detailData.firstOrNull()?.value ?: return

    Column(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        detailData.forEachIndexed { idx, pt ->
            val change = pt.value - firstVal
            val changePct = if (firstVal != 0.0) (change / firstVal) * 100 else 0.0
            val isUp = change >= 0
            val itemColor = if (isUp) Green500 else Red500

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(dateFmt.format(Date(pt.timestamp)),
                    fontSize = 10.sp, color = SubtextGray, modifier = Modifier.weight(1f))
                Text(formatValue(pt.value),
                    fontSize = 11.sp, fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                Text("${if (isUp) "+" else ""}${String.format(Locale.ROOT, "%.2f", changePct)}%",
                    fontSize = 10.sp, color = itemColor)
            }
            if (idx < detailData.size - 1) {
                Box(Modifier.fillMaxWidth().height(0.5.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)))
            }
        }
    }
}

private fun prepareDetailData(data: List<ChartDataPoint>, period: ChartPeriod): List<ChartDataPoint> {
    val maxRows = when (period) {
        ChartPeriod.DAY -> 24
        ChartPeriod.WEEK -> 14
        ChartPeriod.MONTH -> 15
        ChartPeriod.QUARTER -> 15
        ChartPeriod.YEAR -> 12
        ChartPeriod.ALL -> 12
    }
    if (data.size <= maxRows) return data
    val step = data.size / maxRows
    return data.filterIndexed { i, _ -> i % step == 0 }.let {
        if (it.last() != data.last()) it + data.last() else it
    }
}

private fun filterDataByPeriod(data: List<ChartDataPoint>, period: ChartPeriod): List<ChartDataPoint> {
    if (period == ChartPeriod.ALL) return data
    val cutoff = System.currentTimeMillis() - period.millis
    return data.filter { it.timestamp >= cutoff }.ifEmpty { data }
}

private fun downsample(data: List<ChartDataPoint>, period: ChartPeriod): List<ChartDataPoint> {
    val maxPoints = when (period) {
        ChartPeriod.DAY -> 96
        ChartPeriod.WEEK -> 168
        ChartPeriod.MONTH -> 120
        ChartPeriod.QUARTER -> 90
        ChartPeriod.YEAR -> 365
        ChartPeriod.ALL -> 500
    }
    if (data.size <= maxPoints) return data
    val step = data.size / maxPoints
    return data.filterIndexed { i, _ -> i % step == 0 }.let {
        if (it.last() != data.last()) it + data.last() else it
    }
}

private fun formatValue(v: Double): String = when {
    abs(v) >= 1000 -> String.format(Locale.ROOT, "%.1f", v)
    abs(v) >= 1 -> String.format(Locale.ROOT, "%.4f", v)
    else -> String.format(Locale.ROOT, "%.6f", v)
}

@Composable
private fun ModernChart(
    data: List<ChartDataPoint>,
    lineColor: Color,
    minVal: Double,
    maxVal: Double,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(durationMillis = 1000))
    }

    var selectedIdx by remember { mutableStateOf(-1) }
    val density = LocalDensity.current
    val labelPaint = remember {
        android.graphics.Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = with(density) { 9.dp.toPx() }
            textAlign = android.graphics.Paint.Align.LEFT
        }
    }
    val dash = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)

    Canvas(
        modifier = modifier
            .pointerInput(data) {
                detectTapGestures { tapOffset ->
                    val labelW = with(density) { 36.dp.toPx() }
                    val chartL = labelW + 4.dp.toPx()
                    val chartW = (size.width.toFloat() - chartL).coerceAtLeast(1f)
                    val stepX = chartW / (data.size - 1).coerceAtLeast(1)
                    val idx = ((tapOffset.x - chartL) / stepX + 0.5f).toInt().coerceIn(0, data.size - 1)
                    selectedIdx = if (selectedIdx == idx) -1 else idx
                }
            }
    ) {
        val range = (maxVal - minVal).coerceAtLeast(0.0001)
        val w = size.width
        val h = size.height
        val labelW = with(density) { 36.dp.toPx() }
        val chartL = labelW + 4.dp.toPx()
        val chartW = (w - chartL).coerceAtLeast(1f)
        val stepX = chartW / (data.size - 1).coerceAtLeast(1)
        val visibleCount = (data.size * progress.value).toInt().coerceIn(2, data.size)
        val gridColor = Color(0xFF555555)

        val n = 4
        for (i in 0..n) {
            val y = h * i / n
            drawLine(gridColor.copy(alpha = 0.2f), Offset(chartL, y), Offset(w, y), strokeWidth = 0.5f, pathEffect = if (i > 0 && i < n) dash else null)
            val label = formatAxisLabel(maxVal - (range * i / n))
            drawContext.canvas.nativeCanvas.drawText(label, 0f, y + 4.dp.toPx(), labelPaint)
        }

        val gradient = Brush.verticalGradient(
            colors = listOf(lineColor.copy(alpha = 0.15f), lineColor.copy(alpha = 0.02f), Color.Transparent),
            startY = 0f,
            endY = h
        )

        val path = Path()
        repeat(visibleCount) { i ->
            val x = chartL + i * stepX
            val y = h - ((data[i].value - minVal) / range * h).toFloat()
            if (i == 0) path.moveTo(x, y)
            else {
                val prevY = h - ((data[i - 1].value - minVal) / range * h).toFloat()
                val ctrl1 = Offset(x - stepX * 0.5f, prevY)
                val ctrl2 = Offset(x - stepX * 0.5f, y)
                path.cubicTo(ctrl1.x, ctrl1.y, ctrl2.x, ctrl2.y, x, y)
            }
        }

        val fillPath = Path().apply {
            addPath(path)
            lineTo(chartL + (visibleCount - 1) * stepX, h)
            lineTo(chartL, h)
            close()
        }
        drawPath(fillPath, gradient)

        drawPath(path, lineColor, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

        drawPath(path, lineColor.copy(alpha = 0.25f), style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

        val stepDots = (data.size / 15).coerceAtLeast(1)
        for (i in 0 until visibleCount step stepDots) {
            val x = chartL + i * stepX
            val y = h - ((data[i].value - minVal) / range * h).toFloat()
            drawCircle(lineColor.copy(alpha = 0.4f), radius = 1.5.dp.toPx(), center = Offset(x, y))
        }

        val lx = chartL + (visibleCount - 1) * stepX
        val ly = h - ((data[visibleCount - 1].value - minVal) / range * h).toFloat()
        drawCircle(lineColor, radius = 3.5.dp.toPx(), center = Offset(lx, ly))
        drawCircle(Color.Black, radius = 1.5.dp.toPx(), center = Offset(lx, ly))

        if (selectedIdx >= 0 && selectedIdx < visibleCount) {
            val sx = chartL + selectedIdx * stepX
            val sy = h - ((data[selectedIdx].value - minVal) / range * h).toFloat()
            drawLine(Color.White.copy(alpha = 0.4f), Offset(sx, 0f), Offset(sx, h), strokeWidth = 1.dp.toPx(), pathEffect = dash)
            drawCircle(lineColor, radius = 5.dp.toPx(), center = Offset(sx, sy))
            drawCircle(Color.White, radius = 2.5.dp.toPx(), center = Offset(sx, sy))

            val tooltipText = formatValue(data[selectedIdx].value)
            val tooltipPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = with(density) { 11.dp.toPx() }
                textAlign = android.graphics.Paint.Align.CENTER
            }
            val txtW = tooltipPaint.measureText(tooltipText) + with(density) { 14.dp.toPx() }
            val txtH = with(density) { 20.dp.toPx() }
            val tooltipX = sx.coerceIn(txtW / 2, w - txtW / 2)
            val tooltipY = (sy - with(density) { 18.dp.toPx() }).coerceAtLeast(txtH + with(density) { 4.dp.toPx() })
            val rx = with(density) { 6.dp.toPx() }
            drawRoundRect(Color(0xCC1E1E1E), Offset(tooltipX - txtW / 2, tooltipY - txtH), Size(txtW, txtH), CornerRadius(rx, rx))
            drawContext.canvas.nativeCanvas.drawText(tooltipText, tooltipX, tooltipY - 3.dp.toPx(), tooltipPaint)
        }
    }
}

private fun formatAxisLabel(v: Double): String = when {
    abs(v) >= 1000 -> String.format(Locale.ROOT, "%.0f", v)
    abs(v) >= 1 -> String.format(Locale.ROOT, "%.2f", v)
    abs(v) >= 0.01 -> String.format(Locale.ROOT, "%.4f", v)
    else -> String.format(Locale.ROOT, "%.6f", v)
}
