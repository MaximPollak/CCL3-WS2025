package dev.maximpollak.neokey.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.maximpollak.neokey.data.repository.SecretRepository
import dev.maximpollak.neokey.domain.model.Secret
import dev.maximpollak.neokey.security.CryptoManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SecretsViewModel(
    private val repository: SecretRepository
) : ViewModel() {

    private val _secrets = MutableStateFlow<List<Secret>>(emptyList())
    val secrets: StateFlow<List<Secret>> = _secrets

    private val _selectedSecret = MutableStateFlow<Secret?>(null)
    val selectedSecret: StateFlow<Secret?> = _selectedSecret

    init {
        viewModelScope.launch {
            // Repo already decrypts + does work off main thread (flowOn in repo)
            repository.getAllSecrets().collect { list ->
                _secrets.value = list
            }
        }
    }

    fun loadSecretById(id: Int) = viewModelScope.launch {
        _selectedSecret.value = repository.getSecretById(id)
    }

    fun clearSelectedSecret() {
        _selectedSecret.value = null
    }

    fun addSecret(secret: Secret) = viewModelScope.launch {
        // Repo encrypts
        repository.insertSecret(secret)
    }

    fun updateSecret(secret: Secret) = viewModelScope.launch {
        // Repo encrypts
        repository.updateSecret(secret)
    }

    fun deleteSecret(secret: Secret) = viewModelScope.launch {
        repository.deleteSecret(secret)
        // Optional: also clear it so UI doesn't show old state
        if (_selectedSecret.value?.id == secret.id) {
            _selectedSecret.value = null
        }
    }

    private fun decryptOrFallback(value: String, fallback: String?): String {
        return try {
            CryptoManager.decrypt(value)
        } catch (_: Exception) {
            fallback ?: ""
        }
    }
}

