package dev.maximpollak.neokey.utils

import androidx.compose.ui.graphics.Color

data class PasswordStrength(
    val score: Int,
    val label: String,
    val color: Color
)

fun calculatePasswordStrength(password: String): PasswordStrength {
    var score = 0

    if (password.length >= 8) score++
    if (password.length >= 12) score++
    if (password.any { it.isLowerCase() }) score++
    if (password.any { it.isUpperCase() }) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { "!@#$%^&*()-_=+[]{}".contains(it) }) score++

    return when (score) {
        in 0..2 -> PasswordStrength(score, "Weak", Color(0xFFD32F2F))
        in 3..4 -> PasswordStrength(score, "Medium", Color(0xFFFFA000))
        5 -> PasswordStrength(score, "Strong", Color(0xFF388E3C))
        else -> PasswordStrength(score, "Very Strong", Color(0xFF1F8EF1))
    }
}