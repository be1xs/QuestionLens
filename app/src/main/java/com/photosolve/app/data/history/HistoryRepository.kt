package com.photosolve.app.data.history

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.IOException

class HistoryRepository(
    private val context: Context,
    private val dao: HistoryDao,
) {
    fun observeAll(): Flow<List<HistoryEntity>> = dao.observeAll()

    fun observeFavorites(): Flow<List<HistoryEntity>> = dao.observeFavorites()

    suspend fun save(uri: Uri, answerContent: String): Long {
        val imageFile = copyImageToHistory(uri)
        return try {
            dao.insert(
                HistoryEntity(
                    imagePath = imageFile.absolutePath,
                    answerContent = answerContent,
                    createdAt = System.currentTimeMillis(),
                ),
            )
        } catch (exception: Exception) {
            imageFile.delete()
            throw exception
        }
    }

    suspend fun setFavorite(id: Long, isFavorite: Boolean) {
        dao.setFavorite(id, isFavorite)
    }

    suspend fun delete(record: HistoryEntity) {
        dao.delete(record)
        File(record.imagePath).delete()
    }

    private fun copyImageToHistory(uri: Uri): File {
        val directory = File(context.filesDir, HISTORY_DIRECTORY).apply { mkdirs() }
        val target = File.createTempFile(IMAGE_FILE_PREFIX, IMAGE_FILE_SUFFIX, directory)
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            } ?: throw IOException("无法读取题目图片")
            return target
        } catch (exception: Exception) {
            target.delete()
            throw exception
        }
    }

    private companion object {
        const val HISTORY_DIRECTORY = "history_images"
        const val IMAGE_FILE_PREFIX = "history_"
        const val IMAGE_FILE_SUFFIX = ".jpg"
    }
}
