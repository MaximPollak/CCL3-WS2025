package dev.maximpollak.neokey.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object SessionManager {
    private val _unlocked = MutableStateFlow(false)
    val unlocked: StateFlow<Boolean> = _unlocked

    fun unlock() { _unlocked.value = true }
    fun lock() { _unlocked.value = false }
}
