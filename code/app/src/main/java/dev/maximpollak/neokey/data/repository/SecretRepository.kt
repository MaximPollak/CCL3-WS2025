package dev.maximpollak.neokey.data.repository

import dev.maximpollak.neokey.data.local.SecretDao
import dev.maximpollak.neokey.data.local.toDomain
import dev.maximpollak.neokey.data.local.toEntity
import dev.maximpollak.neokey.domain.model.Secret
import dev.maximpollak.neokey.security.CryptoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn

class SecretRepository(
    private val dao: SecretDao
) {

    /**
     * ✅ IMPORTANT CHANGE:
     * We DO NOT decrypt here anymore.
     * We return encrypted values as-is, and decrypt only on demand (e.g., when user presses "Reveal").
     */
    fun getAllSecrets(): Flow<List<Secret>> {
        return dao.getAllSecrets()
            .map { entityList ->
                entityList.map { entity ->
                    // no decrypt
                    entity.toDomain()
                }
            }
            .flowOn(Dispatchers.Default)
    }

    /**
     * Same idea: return encrypted fields as-is.
     * (If you want detail screen to decrypt automatically, do it in the UI/VM on-demand instead.)
     */
    suspend fun getSecretById(id: Int): Secret? {
        return dao.getSecretById(id)?.toDomain()
    }

    /**
     * Encrypt on write (this stays the same).
     */
    suspend fun insertSecret(secret: Secret) {
        val encrypted = secret.copy(
            account = CryptoManager.encrypt(secret.account),
            password = CryptoManager.encrypt(secret.password),
            note = secret.note?.let { CryptoManager.encrypt(it) }
        )
        dao.insertSecret(encrypted.toEntity())
    }

    /**
     * Encrypt on write (this stays the same).
     */
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