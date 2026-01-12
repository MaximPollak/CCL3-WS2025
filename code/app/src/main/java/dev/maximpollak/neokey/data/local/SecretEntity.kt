package dev.maximpollak.neokey.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "secrets")
data class SecretEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,

    // encrypted (e.g. username/email)
    val account: String,

    // encrypted
    val password: String,

    // predefined category key (e.g. "SOCIAL", "SCHOOL", ...)
    val category: String,

    // encrypted or plaintext depending on your threat model
    val note: String?,

    val createdAt: Long
)