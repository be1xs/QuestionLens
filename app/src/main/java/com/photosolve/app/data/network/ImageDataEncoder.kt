package com.photosolve.app.data.network

import android.content.ContentResolver
import android.net.Uri
import android.util.Base64
import java.io.IOException

class ImageDataEncoder(
    private val contentResolver: ContentResolver,
) {
    fun encode(uri: Uri): EncodedImage {
        val bytes = contentResolver.openInputStream(uri)?.use { input -> input.readBytes() }
            ?: throw IOException("无法读取题目图片")
        if (bytes.isEmpty()) throw IOException("题目图片为空")

        val mimeType = contentResolver.getType(uri)?.takeIf { it.startsWith("image/") }
            ?: DEFAULT_MIME_TYPE
        val encoded = Base64.encodeToString(bytes, Base64.NO_WRAP)
        return EncodedImage(mimeType = mimeType, base64 = encoded)
    }
}

data class EncodedImage(
    val mimeType: String,
    val base64: String,
)

private const val DEFAULT_MIME_TYPE = "image/jpeg"
