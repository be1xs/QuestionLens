package com.photosolve.app.capture

import android.content.Context
import android.graphics.Bitmap
import android.media.Image
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object ScreenImageWriter {
    fun save(context: Context, image: Image): Uri {
        val plane = image.planes.first()
        val paddedWidth = plane.rowStride / plane.pixelStride
        val padded = Bitmap.createBitmap(paddedWidth, image.height, Bitmap.Config.ARGB_8888)
        var cropped: Bitmap? = null
        try {
            padded.copyPixelsFromBuffer(plane.buffer)
            val bitmap = Bitmap.createBitmap(padded, 0, 0, image.width, image.height)
            cropped = bitmap
            val directory = File(context.filesDir, "screen_shots")
            check(directory.isDirectory || directory.mkdirs())
            val file = File.createTempFile("screen_", ".png", directory)
            try {
                file.outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, it)) }
                return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } catch (error: Exception) {
                file.delete()
                throw error
            }
        } finally {
            if (cropped !== padded) cropped?.recycle()
            padded.recycle()
        }
    }

    private const val PNG_QUALITY = 100
}
