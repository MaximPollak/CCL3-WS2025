package dev.maximpollak.neokey.domain.model

data class Secret(
    val id: Int = 0,
    val title: String,
    val content: String,
    val type: SecretType,
    val createdAt: Long
)