package dev.maximpollak.neokey.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SecretEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDataBase : RoomDatabase() {
    abstract fun secretDao(): SecretDao
}