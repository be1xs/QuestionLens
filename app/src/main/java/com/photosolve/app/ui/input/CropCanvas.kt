package com.photosolve.app.ui.input

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.photosolve.app.domain.image.CropHandle
import com.photosolve.app.domain.image.CropRect
import com.photosolve.app.domain.image.ImagePlacement
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun CropCanvas(bitmap: Bitmap, crop: CropRect, onCrop: (CropRect) -> Unit, modifier: Modifier) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val latestCrop by rememberUpdatedState(crop)
    val latestOnCrop by rememberUpdatedState(onCrop)
    val image = remember(bitmap) { bitmap.asImageBitmap() }
    Canvas(modifier.onSizeChanged { canvasSize = it }.pointerInput(bitmap, canvasSize) {
        if (canvasSize.width == 0 || canvasSize.height == 0) return@pointerInput
        val fit = ImagePlacement.fit(bitmap.width, bitmap.height, size.width.toFloat(), size.height.toFloat())
        var handle: CropHandle? = null
        detectDragGestures(
            onDragStart = { point -> handle = hitHandle(point, fit, latestCrop, 24.dp.toPx()) },
            onDragEnd = { handle = null },
            onDragCancel = { handle = null },
        ) { change, delta ->
            handle?.let {
                change.consume()
                latestOnCrop(latestCrop.drag(it, delta.x / fit.width, delta.y / fit.height))
            }
        }
    }) {
        val fit = ImagePlacement.fit(bitmap.width, bitmap.height, size.width, size.height)
        drawImage(image, dstOffset = IntOffset(fit.x.roundToInt(), fit.y.roundToInt()),
            dstSize = IntSize(fit.width.roundToInt(), fit.height.roundToInt()))
        val left = fit.x + crop.left * fit.width
        val top = fit.y + crop.top * fit.height
        val right = fit.x + crop.right * fit.width
        val bottom = fit.y + crop.bottom * fit.height
        val shade = Color.Black.copy(alpha = 0.55f)
        drawRect(shade, Offset.Zero, Size(size.width, top))
        drawRect(shade, Offset(0f, bottom), Size(size.width, (size.height - bottom).coerceAtLeast(0f)))
        drawRect(shade, Offset(0f, top), Size(left, bottom - top))
        drawRect(shade, Offset(right, top), Size((size.width - right).coerceAtLeast(0f), bottom - top))
        drawRect(Color.White, Offset(left, top), Size(right - left, bottom - top), style = Stroke(2.dp.toPx()))
        val middleX = (left + right) / 2
        val middleY = (top + bottom) / 2
        listOf(Offset(left, top), Offset(right, top), Offset(left, bottom), Offset(right, bottom),
            Offset(middleX, top), Offset(middleX, bottom), Offset(left, middleY), Offset(right, middleY)).forEach {
            drawCircle(Color.Black, 7.dp.toPx(), it)
            drawCircle(Color.White, 5.dp.toPx(), it)
        }
    }
}

private fun hitHandle(point: Offset, fit: ImagePlacement, crop: CropRect, tolerance: Float): CropHandle? {
    val left = fit.x + crop.left * fit.width
    val top = fit.y + crop.top * fit.height
    val right = fit.x + crop.right * fit.width
    val bottom = fit.y + crop.bottom * fit.height
    val nearLeft = abs(point.x - left) <= tolerance
    val nearRight = abs(point.x - right) <= tolerance
    val nearTop = abs(point.y - top) <= tolerance
    val nearBottom = abs(point.y - bottom) <= tolerance
    return when {
        nearLeft && nearTop -> CropHandle.TOP_LEFT
        nearRight && nearTop -> CropHandle.TOP_RIGHT
        nearLeft && nearBottom -> CropHandle.BOTTOM_LEFT
        nearRight && nearBottom -> CropHandle.BOTTOM_RIGHT
        point.y in top..bottom && nearLeft -> CropHandle.LEFT
        point.y in top..bottom && nearRight -> CropHandle.RIGHT
        point.x in left..right && nearTop -> CropHandle.TOP
        point.x in left..right && nearBottom -> CropHandle.BOTTOM
        point.x in left..right && point.y in top..bottom -> CropHandle.MOVE
        else -> null
    }
}
