package dev.maximpollak.neokey.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class SecretWithCredential(
    @Embedded val secret: SecretEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "secretId"
    )
    val credential: CredentialEntity?
)