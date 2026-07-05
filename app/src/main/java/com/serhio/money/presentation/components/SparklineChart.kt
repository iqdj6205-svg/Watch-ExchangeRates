package com.serhio.money.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun SparklineChart(
    data: List<Double>,
    modifier: Modifier = Modifier,
    color: Color = Color.Green
) {
    if (data.size < 2) return

    Canvas(
        modifier = modifier.fillMaxWidth()
    ) {
        val min = data.minOrNull() ?: 0.0
        val max = data.maxOrNull() ?: 1.0
        val range = max - min
        val width = size.width
        val height = size.height

        val path = Path()
        data.forEachIndexed { index, value ->
            val x = index * (width / (data.size - 1))
            val y = height - ((value - min) / range * height).toFloat()
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}
