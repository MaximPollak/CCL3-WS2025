package dev.maximpollak.neokey.utils

import dev.maximpollak.neokey.domain.model.SecretType
import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.*

// Extension function to get color for each SecretType
fun SecretType.color(): Color {
    return when (this) {
        SecretType.WORK -> Color(0xFFFFA000)
        SecretType.WIFI -> Color.Blue
        SecretType.EDUCATION -> Color(0xFF1976D2)
        SecretType.PRIVATE -> Color(0xFF9C27B0)
        SecretType.ELSE -> Color.Gray
    }
}

// Format timestamp into human-readable string
fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

// Constants for database columns (field names)
const val PASSWORD = "password"
const val NOTE = "note"
const val TYPE = "type"
const val ACCOUNT = "account"
const val TITLE = "title"
const val CATEGORY = "category"