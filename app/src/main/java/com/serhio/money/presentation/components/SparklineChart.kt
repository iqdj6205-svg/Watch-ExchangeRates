package com.serhio.money.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun SparklineChart(
    data: List<Double>,
    modifier: Modifier = Modifier,
    color: Color = Color.Green,
    animate: Boolean = true
) {
    if (data.size < 2) return

    val progress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        progress.snapTo(0f)
        if (animate) progress.animateTo(1f, tween(durationMillis = 900, easing = FastOutSlowInEasing))
        else progress.snapTo(1f)
    }

    Canvas(modifier = modifier.fillMaxWidth()) {
        val min = data.minOrNull() ?: 0.0
        val max = data.maxOrNull() ?: 1.0
        val range = (max - min).coerceAtLeast(0.0001)
        val width = size.width
        val height = size.height
        val visibleCount = (data.size * progress.value).toInt().coerceIn(2, data.size)

        val path = Path()
        repeat(visibleCount) { i ->
            val x = i * (width / (data.size - 1))
            val y = height - ((data[i] - min) / range * height).toFloat()
            if (i == 0) path.moveTo(x, y) else {
                val prevX = (i - 1) * (width / (data.size - 1))
                val prevY = height - ((data[i - 1] - min) / range * height).toFloat()
                path.cubicTo(prevX + (x - prevX) * 0.45f, prevY, x - (x - prevX) * 0.45f, y, x, y)
            }
        }

        val fillPath = Path().apply {
            addPath(path)
            lineTo((visibleCount - 1) * (width / (data.size - 1)), height)
            lineTo(0f, height)
            close()
        }
        drawPath(
            fillPath,
            Brush.verticalGradient(listOf(color.copy(alpha = 0.22f), color.copy(alpha = 0.04f), Color.Transparent))
        )
        drawPath(path, color.copy(alpha = 0.22f), style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(path, color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

        val lastX = (visibleCount - 1) * (width / (data.size - 1))
        val lastY = height - ((data[visibleCount - 1] - min) / range * height).toFloat()
        drawCircle(Color.Black.copy(alpha = 0.5f), radius = 4.dp.toPx(), center = Offset(lastX, lastY))
        drawCircle(color, radius = 2.8.dp.toPx(), center = Offset(lastX, lastY))
    }
}
