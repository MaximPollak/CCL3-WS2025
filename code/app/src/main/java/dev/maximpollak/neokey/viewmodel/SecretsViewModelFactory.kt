package dev.maximpollak.neokey.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.maximpollak.neokey.data.local.DatabaseProvider
import dev.maximpollak.neokey.data.repository.SecretRepository

class SecretsViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SecretsViewModel::class.java)) {
            val database = DatabaseProvider.getDatabase(context)
            val repository = SecretRepository(database.secretDao())
            @Suppress("UNCHECKED_CAST")
            return SecretsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}