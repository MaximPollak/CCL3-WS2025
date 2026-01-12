// File: SecretsViewModel.kt
package dev.maximpollak.neokey.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.maximpollak.neokey.data.repository.SecretRepository
import dev.maximpollak.neokey.domain.model.Secret
import dev.maximpollak.neokey.security.CryptoManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SecretsViewModel(
    private val repository: SecretRepository
) : ViewModel() {

    private val _secrets = MutableStateFlow<List<Secret>>(emptyList())
    val secrets: StateFlow<List<Secret>> = _secrets

    init {
        viewModelScope.launch {
            repository.getAllSecrets()
                .map { list ->
                    list.map { secret ->
                        secret.copy(
                            password = decryptOrFallback(secret.password, fallback = "Decryption failed"),
                            note = secret.note?.let { decryptOrFallback(it, fallback = null) }
                        )
                    }
                }
                .collect { decryptedList ->
                    _secrets.value = decryptedList
                }
        }
    }

    fun addSecret(secret: Secret) = viewModelScope.launch {
        val encrypted = secret.copy(
            password = CryptoManager.encrypt(secret.password),
            note = secret.note?.let { CryptoManager.encrypt(it) }
        )
        repository.insertSecret(encrypted)
    }

    fun updateSecret(secret: Secret) = viewModelScope.launch {
        val encrypted = secret.copy(
            password = CryptoManager.encrypt(secret.password),
            note = secret.note?.let { CryptoManager.encrypt(it) }
        )
        repository.updateSecret(encrypted)
    }

    fun deleteSecret(secret: Secret) = viewModelScope.launch {
        repository.deleteSecret(secret)
    }

    private fun decryptOrFallback(value: String, fallback: String?): String {
        return try {
            CryptoManager.decrypt(value)
        } catch (_: Exception) {
            fallback ?: ""
        }
    }
}
