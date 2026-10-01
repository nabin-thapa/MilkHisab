package com.milkhisab.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Single Room database for the whole app.
 *
 * Everything lives on the device (offline-first). No cloud, no account.
 */
@Database(
    entities = [MilkRecordEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun milkRecordDao(): MilkRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "milk_hisab.db"
                )
                    // No destructive fallback: losing the family's records is
                    // worse than a crash. Version 1 has no migrations yet.
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
