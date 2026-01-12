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
                    // Decrypt content in-memory for UI
                    list.map { secret ->
                        secret.copy(
                            content = try {
                                CryptoManager.decrypt(secret.content)
                            } catch (e: Exception) {
                                "Decryption failed"
                            }
                        )
                    }
                }
                .collect { decryptedList ->
                    _secrets.value = decryptedList
                }
        }
    }

    fun addSecret(secret: Secret) = viewModelScope.launch {
        val encryptedSecret = secret.copy(content = CryptoManager.encrypt(secret.content))
        repository.insertSecret(encryptedSecret)
    }

    fun updateSecret(secret: Secret) = viewModelScope.launch {
        val encryptedSecret = secret.copy(content = CryptoManager.encrypt(secret.content))
        repository.updateSecret(encryptedSecret)
    }

    fun deleteSecret(secret: Secret) = viewModelScope.launch {
        repository.deleteSecret(secret)
    }
}