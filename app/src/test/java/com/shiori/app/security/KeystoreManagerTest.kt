package com.shiori.app.security

import android.content.Context
import android.content.ContextWrapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class KeystoreManagerTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var context: Context
    private lateinit var keystoreManager: KeystoreManager

    @Before
    fun setUp() {
        val testFilesDir = tempFolder.newFolder("files")
        context = object : ContextWrapper(null) {
            override fun getFilesDir(): File = testFilesDir
        }
        keystoreManager = KeystoreManager(context)
    }

    @Test
    fun testGetOrCreateDatabasePassphraseReturnsDeterministicKey() {
        val passphrase1 = keystoreManager.getOrCreateDatabasePassphrase()
        assertNotNull(passphrase1)
        assertTrue(passphrase1.isNotEmpty())
        assertEquals(32, passphrase1.size) // 256-bit passphrase

        val passphrase2 = keystoreManager.getOrCreateDatabasePassphrase()
        assertEquals(passphrase1.toList(), passphrase2.toList())
    }
}
