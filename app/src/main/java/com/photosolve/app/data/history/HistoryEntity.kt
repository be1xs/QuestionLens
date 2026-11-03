package com.photosolve.app.data.history

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_records")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val imagePath: String,
    val answerContent: String,
    val createdAt: Long,
    val isFavorite: Boolean = false,
)
