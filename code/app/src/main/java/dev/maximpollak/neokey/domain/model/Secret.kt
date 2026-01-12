package dev.maximpollak.neokey.domain.model

data class Secret(
    val id: Int = 0,
    val title: String,
    val account: String,
    val password: String,
    val category: SecretType,
    val note: String?,
    val createdAt: Long
)