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
                val decryptedAccount = CryptoManager.decrypt(entity.account)
                val decryptedPassword = CryptoManager.decrypt(entity.password)
                val decryptedNote = entity.note?.let { CryptoManager.decrypt(it) }

                entity.copy(
                    account = decryptedAccount,
                    password = decryptedPassword,
                    note = decryptedNote
                ).toDomain()
            }
        }
    }

    suspend fun insertSecret(secret: Secret) {
        val encrypted = secret.copy(
            account = CryptoManager.encrypt(secret.account),
            password = CryptoManager.encrypt(secret.password),
            note = secret.note?.let { CryptoManager.encrypt(it) }
        )

        dao.insertSecret(encrypted.toEntity())
    }

    suspend fun updateSecret(secret: Secret) {
        val encrypted = secret.copy(
            account = CryptoManager.encrypt(secret.account),
            password = CryptoManager.encrypt(secret.password),
            note = secret.note?.let { CryptoManager.encrypt(it) }
        )

        dao.updateSecret(encrypted.toEntity())
    }

    suspend fun deleteSecret(secret: Secret) {
        dao.deleteSecret(secret.toEntity())
    }
}