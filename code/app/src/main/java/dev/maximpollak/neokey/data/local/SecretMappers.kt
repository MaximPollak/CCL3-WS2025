package dev.maximpollak.neokey.data.local

import dev.maximpollak.neokey.domain.model.Secret
import dev.maximpollak.neokey.domain.model.SecretType


fun SecretEntity.toDomain(): Secret {
    val secretType = when (category.uppercase()) {
        "WORK" -> SecretType.WORK
        "WIFI" -> SecretType.WIFI
        "EDUCATION" -> SecretType.EDUCATION
        "PRIVATE" -> SecretType.PRIVATE
        else -> SecretType.ELSE
    }

    return Secret(
        id = id,
        title = title,
        account = account,
        password = password,
        category = secretType,
        note = note,
        createdAt = createdAt
    )
}

fun Secret.toEntity(): SecretEntity {
    return SecretEntity(
        id = id,
        title = title,
        account = account,
        password = password,
        category = category.name,
        note = note,
        createdAt = createdAt
    )
}