package com.shiori.app.voice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioPromptManagerTest {
    private val manager = AudioPromptManager(context = null)

    @Test
    fun testDefaultLanguageIsEnglish() {
        assertEquals("en", manager.currentLanguage)
        val praise = manager.getPromptText(PromptType.PRAISE)
        assertEquals("Excellent work!", praise)
    }

    @Test
    fun testHindiLanguagePrompts() {
        manager.setLanguage("hi")
        assertEquals("hi", manager.currentLanguage)
        val praise = manager.getPromptText(PromptType.PRAISE)
        assertTrue(praise.contains("शाबाश") || praise.contains("बढ़िया"))
    }

    @Test
    fun testKannadaLanguagePrompts() {
        manager.setLanguage("kn")
        assertEquals("kn", manager.currentLanguage)
        val praise = manager.getPromptText(PromptType.PRAISE)
        assertTrue(praise.contains("ಉತ್ತಮ"))
    }

    @Test
    fun testUnknownLanguageDefaultsToEnglish() {
        manager.setLanguage("fr")
        val welcome = manager.getPromptText(PromptType.WELCOME)
        assertTrue(welcome.contains("Welcome"))
    }
}
