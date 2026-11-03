package com.photosolve.app.data.history

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [HistoryEntity::class], version = 1, exportSchema = false)
abstract class PhotoSolveDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var instance: PhotoSolveDatabase? = null

        fun getInstance(context: Context): PhotoSolveDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    PhotoSolveDatabase::class.java,
                    DATABASE_NAME,
                ).build().also { instance = it }
            }
        }

        private const val DATABASE_NAME = "photosolve.db"
    }
}
