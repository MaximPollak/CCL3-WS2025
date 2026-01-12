package dev.maximpollak.neokey.data.local

import dev.maximpollak.neokey.domain.model.Secret
import dev.maximpollak.neokey.domain.model.SecretType


fun SecretEntity.toDomain(): Secret {
    val secretType = when (type.uppercase()) {
        "PASSWORD" -> SecretType.PASSWORD
        "WIFI" -> SecretType.WIFI
        "NOTE" -> SecretType.NOTE
        else -> SecretType.NOTE // fallback default
    }

    return Secret(
        id = id,
        title = title,
        content = content,
        type = secretType,
        createdAt = createdAt
    )
}

fun Secret.toEntity(): SecretEntity {
    return SecretEntity(
        id = id,
        title = title,
        content = content,
        type = type.name,
        createdAt = createdAt
    )
}