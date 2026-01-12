package dev.maximpollak.neokey.data.repository

import dev.maximpollak.neokey.data.local.SecretDao
import dev.maximpollak.neokey.data.local.toDomain
import dev.maximpollak.neokey.data.local.toEntity
import dev.maximpollak.neokey.domain.model.Secret
import dev.maximpollak.neokey.security.CryptoManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SecretRepository(
    private val dao: SecretDao
) {

    fun getAllSecrets(): Flow<List<Secret>> {
        return dao.getAllSecrets().map { entityList ->
            entityList.map { entity ->
                val decrypted = CryptoManager.decrypt(entity.content)
                entity.copy(content = decrypted).toDomain()
            }
        }
    }

    suspend fun insertSecret(secret: Secret) {
        val encrypted = secret.copy(
            content = CryptoManager.encrypt(secret.content)
        )
        dao.insertSecret(encrypted.toEntity())
    }

    suspend fun updateSecret(secret: Secret) {
        val encrypted = secret.copy(
            content = CryptoManager.encrypt(secret.content)
        )
        dao.updateSecret(encrypted.toEntity())
    }

    suspend fun deleteSecret(secret: Secret) {
        dao.deleteSecret(secret.toEntity())
    }
}