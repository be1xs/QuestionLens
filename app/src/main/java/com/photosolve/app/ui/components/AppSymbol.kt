package com.photosolve.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.dp

enum class Symbol { HOME, PERSONAL, SCAN, HISTORY, BOOKMARK, API, ARROW, BACK, ORB }

/** Decorative symbols: their parent control supplies the accessible label. */
@Composable
fun AppSymbol(symbol: Symbol, modifier: Modifier = Modifier, color: Color = LocalContentColor.current) {
    Canvas(modifier.size(24.dp)) {
        scale(size.width / 24f, size.height / 24f, pivot = Offset.Zero) {
            val stroke = Stroke(1.7f, cap = StrokeCap.Round)
            fun line(x: Float, y: Float, endX: Float, endY: Float) =
                drawLine(color, Offset(x, y), Offset(endX, endY), 1.7f, StrokeCap.Round)
            fun path(vararg points: Pair<Float, Float>, closed: Boolean = false) {
                drawPath(Path().apply {
                    points.forEachIndexed { index, p -> if (index == 0) moveTo(p.first, p.second) else lineTo(p.first, p.second) }
                    if (closed) close()
                }, color, style = stroke)
            }
            when (symbol) {
                Symbol.HOME -> {
                    path(3f to 10f, 12f to 3f, 21f to 10f)
                    path(5f to 9f, 5f to 21f, 10f to 21f, 10f to 15f, 14f to 15f, 14f to 21f, 19f to 21f, 19f to 9f)
                }
                Symbol.PERSONAL -> {
                    drawCircle(color, 3.5f, Offset(12f, 7f), style = stroke)
                    drawArc(color, 180f, 180f, false, Offset(4f, 14f), Size(16f, 12f), style = stroke)
                }
                Symbol.SCAN -> {
                    path(8f to 3f, 3f to 3f, 3f to 8f); path(16f to 3f, 21f to 3f, 21f to 8f)
                    path(3f to 16f, 3f to 21f, 8f to 21f); path(16f to 21f, 21f to 21f, 21f to 16f)
                    line(7f, 10f, 17f, 10f); line(7f, 14f, 14f, 14f)
                }
                Symbol.HISTORY -> {
                    drawCircle(color, 8.5f, Offset(12f, 12f), style = stroke)
                    path(12f to 7f, 12f to 12f, 16f to 14f)
                }
                Symbol.BOOKMARK -> path(6f to 3f, 18f to 3f, 18f to 21f, 12f to 17f, 6f to 21f, closed = true)
                Symbol.API -> {
                    path(8f to 6f, 3f to 12f, 8f to 18f); path(16f to 6f, 21f to 12f, 16f to 18f)
                    line(14f, 4f, 10f, 20f)
                }
                Symbol.ARROW -> path(9f to 5f, 16f to 12f, 9f to 19f)
                Symbol.BACK -> { path(14f to 5f, 7f to 12f, 14f to 19f); line(7f, 12f, 21f, 12f) }
                Symbol.ORB -> {
                    drawCircle(color, 8.5f, Offset(12f, 12f), style = stroke)
                    drawCircle(color.copy(alpha = 0.18f), 5f, Offset(12f, 12f))
                    drawCircle(color, 2f, Offset(12f, 12f))
                }
            }
        }
    }
}
