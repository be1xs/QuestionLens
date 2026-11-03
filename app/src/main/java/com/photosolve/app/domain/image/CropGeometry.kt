package com.photosolve.app.domain.image

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min

data class CropRect(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    val width get() = right - left
    val height get() = bottom - top

    fun drag(handle: CropHandle, dx: Float, dy: Float): CropRect {
        if (handle == CropHandle.MOVE) {
            val x = dx.coerceIn(-left, 1f - right)
            val y = dy.coerceIn(-top, 1f - bottom)
            return CropRect(left + x, top + y, right + x, bottom + y)
        }
        return CropRect(
            if (handle.left) (left + dx).coerceIn(0f, (right - MIN_SIZE).coerceAtLeast(0f)) else left,
            if (handle.top) (top + dy).coerceIn(0f, (bottom - MIN_SIZE).coerceAtLeast(0f)) else top,
            if (handle.right) (right + dx).coerceIn((left + MIN_SIZE).coerceAtMost(1f), 1f) else right,
            if (handle.bottom) (bottom + dy).coerceIn((top + MIN_SIZE).coerceAtMost(1f), 1f) else bottom,
        )
    }

    fun pixels(width: Int, height: Int): PixelCrop {
        require(width > 0 && height > 0)
        val x = floor(left * width).toInt().coerceIn(0, width - 1)
        val y = floor(top * height).toInt().coerceIn(0, height - 1)
        val endX = ceil(right * width).toInt().coerceIn(x + 1, width)
        val endY = ceil(bottom * height).toInt().coerceIn(y + 1, height)
        return PixelCrop(x, y, endX - x, endY - y)
    }

    companion object {
        const val MIN_SIZE = 0.04f
        val INITIAL = CropRect(0.08f, 0.08f, 0.92f, 0.92f)
        val FULL = CropRect(0f, 0f, 1f, 1f)
    }
}

enum class CropHandle(val left: Boolean = false, val top: Boolean = false, val right: Boolean = false, val bottom: Boolean = false) {
    MOVE, LEFT(left = true), TOP(top = true), RIGHT(right = true), BOTTOM(bottom = true),
    TOP_LEFT(left = true, top = true), TOP_RIGHT(top = true, right = true),
    BOTTOM_LEFT(left = true, bottom = true), BOTTOM_RIGHT(right = true, bottom = true),
}

data class PixelCrop(val x: Int, val y: Int, val width: Int, val height: Int)

data class ImagePlacement(val x: Float, val y: Float, val width: Float, val height: Float) {
    companion object {
        fun fit(imageWidth: Int, imageHeight: Int, canvasWidth: Float, canvasHeight: Float): ImagePlacement {
            require(imageWidth > 0 && imageHeight > 0 && canvasWidth > 0 && canvasHeight > 0)
            val scale = min(canvasWidth / imageWidth, canvasHeight / imageHeight)
            val width = imageWidth * scale
            val height = imageHeight * scale
            return ImagePlacement((canvasWidth - width) / 2, (canvasHeight - height) / 2, width, height)
        }
    }
}
