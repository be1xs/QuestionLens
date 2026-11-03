package com.photosolve.app.data.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.core.content.FileProvider
import com.photosolve.app.domain.image.CropRect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max

object QuestionImages {
    // ImageDecoder applies EXIF orientation and bounds memory use before decoding.
    suspend fun load(context: Context, uri: Uri): Bitmap = withContext(Dispatchers.IO) {
        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, info, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            val longest = max(info.size.width, info.size.height)
            if (longest > MAX_EDGE) {
                decoder.setTargetSize(
                    (info.size.width.toLong() * MAX_EDGE / longest).toInt().coerceAtLeast(1),
                    (info.size.height.toLong() * MAX_EDGE / longest).toInt().coerceAtLeast(1),
                )
            }
        }
    }

    suspend fun crop(context: Context, bitmap: Bitmap, area: CropRect): Uri = withContext(Dispatchers.IO) {
        val rect = area.pixels(bitmap.width, bitmap.height)
        val cropped = Bitmap.createBitmap(bitmap, rect.x, rect.y, rect.width, rect.height)
        val directory = File(context.filesDir, "cropped_images")
        check(directory.isDirectory || directory.mkdirs()) { "无法创建图片目录" }
        val file = File.createTempFile("crop_", ".jpg", directory)
        try {
            file.outputStream().use { check(cropped.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it)) }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (error: Exception) {
            file.delete()
            throw error
        } finally {
            // A full-image crop may return the input bitmap itself.
            if (cropped !== bitmap) cropped.recycle()
        }
    }

    private const val MAX_EDGE = 4096
    private const val JPEG_QUALITY = 94
}
