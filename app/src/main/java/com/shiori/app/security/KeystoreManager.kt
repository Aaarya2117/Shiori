package com.shiori.app.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.File
import java.security.KeyStore
import java.security.SecureRandom
import java.security.Security
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeystoreManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val MASTER_KEY_ALIAS = "shiori_master_key"
        private const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128
        private const val PASSPHRASE_FILE = "db_passphrase.enc"
        private const val KEY_SIZE_BITS = 256
    }

    private val isAndroidKeyStoreAvailable: Boolean =
        Security.getProviders().any { it.name == ANDROID_KEYSTORE }

    private val keyStore: KeyStore? = if (isAndroidKeyStoreAvailable) {
        try {
            KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        } catch (e: Exception) {
            null
        }
    } else {
        null
    }

    @Synchronized
    fun getOrCreateDatabasePassphrase(): ByteArray {
        val file = File(context.filesDir, PASSPHRASE_FILE)
        return if (file.exists()) {
            decryptPassphrase(file.readText())
        } else {
            val rawPassphrase = ByteArray(32).apply {
                SecureRandom().nextBytes(this)
            }
            val encryptedBase64 = encryptPassphrase(rawPassphrase)
            file.writeText(encryptedBase64)
            rawPassphrase
        }
    }

    private fun getOrCreateMasterKey(): SecretKey {
        if (keyStore != null) {
            if (!keyStore.containsAlias(MASTER_KEY_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    ANDROID_KEYSTORE
                )
                val specBuilder = KeyGenParameterSpec.Builder(
                    MASTER_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(KEY_SIZE_BITS)

                try {
                    specBuilder.setIsStrongBoxBacked(true)
                    keyGenerator.init(specBuilder.build())
                    keyGenerator.generateKey()
                } catch (e: Exception) {
                    val fallbackSpec = KeyGenParameterSpec.Builder(
                        MASTER_KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(KEY_SIZE_BITS)
                        .build()
                    keyGenerator.init(fallbackSpec)
                    keyGenerator.generateKey()
                }
            }
            val entry = keyStore.getEntry(MASTER_KEY_ALIAS, null) as KeyStore.SecretKeyEntry
            return entry.secretKey
        } else {
            // JVM / unit test environment fallback: persist a master key in private app storage
            val keyFile = File(context.filesDir, "master_key.bin")
            return if (keyFile.exists()) {
                val bytes = keyFile.readBytes()
                SecretKeySpec(bytes, "AES")
            } else {
                val keyGen = KeyGenerator.getInstance("AES")
                keyGen.init(KEY_SIZE_BITS)
                val secretKey = keyGen.generateKey()
                keyFile.writeBytes(secretKey.encoded)
                secretKey
            }
        }
    }

    private fun encryptPassphrase(plainBytes: ByteArray): String {
        val masterKey = getOrCreateMasterKey()
        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, masterKey)
        val iv = cipher.iv
        val cipherBytes = cipher.doFinal(plainBytes)

        val combined = ByteArray(iv.size + cipherBytes.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(cipherBytes, 0, combined, iv.size, cipherBytes.size)
        return Base64.getEncoder().encodeToString(combined)
    }

    private fun decryptPassphrase(encryptedBase64: String): ByteArray {
        val combined = Base64.getDecoder().decode(encryptedBase64)
        val iv = ByteArray(GCM_IV_LENGTH)
        val cipherBytes = ByteArray(combined.size - GCM_IV_LENGTH)
        System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH)
        System.arraycopy(combined, GCM_IV_LENGTH, cipherBytes, 0, cipherBytes.size)

        val masterKey = getOrCreateMasterKey()
        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, masterKey, spec)
        return cipher.doFinal(cipherBytes)
    }
}
