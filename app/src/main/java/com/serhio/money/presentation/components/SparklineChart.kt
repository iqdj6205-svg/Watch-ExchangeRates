package com.serhio.money.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
        if (animate) progress.animateTo(1f, tween(durationMillis = 800))
        else progress.snapTo(1f)
    }

    Canvas(
        modifier = modifier.fillMaxWidth()
    ) {
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
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(path, color, style = Stroke(width = 2.dp.toPx()))

        val lastX = (visibleCount - 1) * (width / (data.size - 1))
        val lastY = height - ((data[visibleCount - 1] - min) / range * height).toFloat()
        drawCircle(color, radius = 3.dp.toPx(), center = Offset(lastX, lastY))
    }
}
