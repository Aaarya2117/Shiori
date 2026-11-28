package com.shiori.app.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

enum class PromptType {
    WELCOME,
    PRAISE,
    ENCOURAGE,
    REMINDER
}

/**
 * Audio prompt controller providing multilingual spoken prompts (Hindi, English, Kannada)
 * with graceful fallback (Owned by Person 3).
 */
@Singleton
class AudioPromptManager @Inject constructor(
    @param:ApplicationContext private val context: Context?
) : TextToSpeech.OnInitListener {

    var currentLanguage: String = "en"
        private set

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private val prompts = mapOf(
        "en" to mapOf(
            PromptType.WELCOME to "Welcome to Shiori. Let's exercise your memory today.",
            PromptType.PRAISE to "Excellent work!",
            PromptType.ENCOURAGE to "Good try, let's keep going!",
            PromptType.REMINDER to "Time for your scheduled medication."
        ),
        "hi" to mapOf(
            PromptType.WELCOME to "शिओरी में आपका स्वागत है। चलिए अपनी याददाश्त का अभ्यास करें।",
            PromptType.PRAISE to "शाबाश! बहुत बढ़िया!",
            PromptType.ENCOURAGE to "अच्छा प्रयास, जारी रखें!",
            PromptType.REMINDER to "दवा लेने का समय हो गया है।"
        ),
        "kn" to mapOf(
            PromptType.WELCOME to "ಶಿಯೋರಿಗೆ ಸುಸ್ವಾಗತ. ನಿಮ್ಮ ನೆನಪಿನ ಶಕ್ತಿಯನ್ನು ಅಭ್ಯಾಸ ಮಾಡೋಣ.",
            PromptType.PRAISE to "ಉತ್ತಮ ಕೆಲಸ! ಅದ್ಭುತ!",
            PromptType.ENCOURAGE to "ಉತ್ತಮ ಪ್ರಯತ್ನ, ಮುಂದುವರಿಸಿ!",
            PromptType.REMINDER to "ನಿಮ್ಮ ಔಷಧಿ ತೆಗೆದುಕೊಳ್ಳುವ ಸಮಯವಾಗಿದೆ."
        )
    )

    init {
        if (context != null) {
            try {
                tts = TextToSpeech(context.applicationContext, this)
            } catch (_: Exception) {
                isTtsReady = false
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            applyLocale()
        } else {
            isTtsReady = false
        }
    }

    fun setLanguage(langCode: String) {
        currentLanguage = when (langCode.lowercase()) {
            "hi" -> "hi"
            "kn" -> "kn"
            else -> "en"
        }
        applyLocale()
    }

    private fun applyLocale() {
        if (!isTtsReady || tts == null) return
        val locale = when (currentLanguage) {
            "hi" -> Locale("hi", "IN")
            "kn" -> Locale("kn", "IN")
            else -> Locale.US
        }
        try {
            tts?.language = locale
        } catch (_: Exception) {
            // Ignore if locale not supported by device TTS
        }
    }

    fun getPromptText(promptType: PromptType): String {
        val langPrompts = prompts[currentLanguage] ?: prompts["en"]!!
        return langPrompts[promptType] ?: prompts["en"]!![promptType] ?: ""
    }

    fun speakPrompt(promptType: PromptType) {
        val text = getPromptText(promptType)
        if (isTtsReady && tts != null && text.isNotEmpty()) {
            try {
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "shiori_${promptType.name}")
            } catch (_: Exception) {
                // Fallback gracefully
            }
        }
    }

    suspend fun speakDynamicText(text: String, langCode: String): Boolean {
        if (context == null || text.isBlank()) return false

        var waitedMs = 0
        while (!isTtsReady && tts != null && waitedMs < 1500) {
            delay(100)
            waitedMs += 100
        }

        val engine = tts ?: return false
        if (!isTtsReady) return false

        return try {
            val normalized = langCode.trim().lowercase()
            val locale = if (normalized == "en" || normalized.isEmpty()) {
                Locale.US
            } else {
                Locale(normalized, "IN")
            }
            val langResult = engine.setLanguage(locale)
            if (langResult == TextToSpeech.LANG_MISSING_DATA ||
                langResult == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                engine.language = Locale.US
            }
            val result = engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "shiori_dynamic_reminder")
            result == TextToSpeech.SUCCESS
        } catch (_: Exception) {
            false
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {
        } finally {
            tts = null
            isTtsReady = false
        }
    }
}
