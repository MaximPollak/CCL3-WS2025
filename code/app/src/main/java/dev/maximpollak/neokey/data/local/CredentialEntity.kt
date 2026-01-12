package dev.maximpollak.neokey.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "credentials",
    foreignKeys = [
        ForeignKey(
            entity = SecretEntity::class,
            parentColumns = ["id"],
            childColumns = ["secretId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("secretId")]
)
data class CredentialEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val secretId: Int,
    val username: String?,
    val password: String?
)