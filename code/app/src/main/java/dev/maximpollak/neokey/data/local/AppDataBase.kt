package dev.maximpollak.neokey.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        SecretEntity::class,
        CredentialEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDataBase : RoomDatabase() {
    abstract fun secretDao(): SecretDao
}