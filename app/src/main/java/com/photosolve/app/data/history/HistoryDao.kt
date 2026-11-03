package com.photosolve.app.data.history

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Insert
    suspend fun insert(record: HistoryEntity): Long

    @Update
    suspend fun update(record: HistoryEntity)

    @Delete
    suspend fun delete(record: HistoryEntity)

    @Query("SELECT * FROM history_records ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history_records WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun observeFavorites(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history_records WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): HistoryEntity?

    @Query("UPDATE history_records SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)
}
