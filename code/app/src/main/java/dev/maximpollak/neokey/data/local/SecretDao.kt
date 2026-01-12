package dev.maximpollak.neokey.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SecretDao {

    @Transaction
    @Query("SELECT * FROM secrets")
    fun getAllWithCredentials(): Flow<List<SecretWithCredential>>

    @Transaction
    @Query("SELECT * FROM secrets WHERE id = :id")
    suspend fun getByIdWithCredential(id: Int): SecretWithCredential?

    @Insert
    suspend fun insertSecret(secret: SecretEntity): Long

    @Insert
    suspend fun insertCredential(cred: CredentialEntity)

    @Update
    suspend fun updateSecret(secret: SecretEntity)

    @Update
    suspend fun updateCredential(cred: CredentialEntity)

    @Query("DELETE FROM secrets WHERE id = :id")
    suspend fun deleteSecret(id: Int)
}