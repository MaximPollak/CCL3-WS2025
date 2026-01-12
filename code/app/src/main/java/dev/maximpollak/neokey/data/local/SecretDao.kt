package dev.maximpollak.neokey.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SecretDao {

    // READ: get all secrets
    @Query("SELECT * FROM secrets ORDER BY createdAt DESC")
    fun getAllSecrets(): Flow<List<SecretEntity>>

    // READ: get one secret
    @Query("SELECT * FROM secrets WHERE id = :id")
    suspend fun getSecretById(id: Int): SecretEntity?

    // CREATE
    @Insert
    suspend fun insertSecret(secret: SecretEntity): Long

    // UPDATE
    @Update
    suspend fun updateSecret(secret: SecretEntity)

    // DELETE
    @Delete
    suspend fun deleteSecret(secret: SecretEntity)
}