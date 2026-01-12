package dev.maximpollak.neokey.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SecretEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun secretDao(): SecretDao
}