package dev.maximpollak.neokey.utils

import dev.maximpollak.neokey.domain.model.SecretType
import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.*

fun SecretType.color(): Color {
    return when (this) {
        SecretType.PASSWORD -> Color.Red
        SecretType.WIFI -> Color.Blue
        SecretType.NOTE -> Color(0xFF388E3C)
    }
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}