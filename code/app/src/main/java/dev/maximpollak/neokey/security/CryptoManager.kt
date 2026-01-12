package dev.maximpollak.neokey.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object CryptoManager {

    private const val KEY_ALIAS = "SecretumAESKey"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val IV_SIZE = 12

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

        if (keyStore.containsAlias(KEY_ALIAS)) {
            return keyStore.getKey(KEY_ALIAS, null) as SecretKey
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )

        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())

        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        val combined = iv + encryptedBytes
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    fun decrypt(cipherText: String): String {
        return try {
            val combined = Base64.decode(cipherText, Base64.DEFAULT)

            // 🔐 SAFETY CHECK
            if (combined.size <= IV_SIZE) {
                // Data is invalid or from old version
                ""
            } else {
                val iv = combined.copyOfRange(0, IV_SIZE)
                val encryptedBytes = combined.copyOfRange(IV_SIZE, combined.size)

                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(
                    Cipher.DECRYPT_MODE,
                    getSecretKey(),
                    GCMParameterSpec(128, iv)
                )

                String(cipher.doFinal(encryptedBytes), Charsets.UTF_8)
            }
        } catch (e: Exception) {
            ""
        }
    }
}