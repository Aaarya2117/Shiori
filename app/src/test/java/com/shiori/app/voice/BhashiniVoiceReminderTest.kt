package com.shiori.app.voice

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Base64

class BhashiniVoiceReminderTest {

    private class FakeVoiceSettings(initial: VoiceConfig) : IVoiceReminderSettings {
        private val _config = MutableStateFlow(initial)
        override val configFlow: StateFlow<VoiceConfig> = _config

        override fun getConfig(): VoiceConfig = _config.value

        override fun setVoiceRemindersEnabled(enabled: Boolean) {
            _config.value = _config.value.copy(voiceRemindersEnabled = enabled)
        }

        override fun setPreferredLanguage(langCode: String) {
            _config.value = _config.value.copy(preferredLanguage = langCode)
        }

        override fun saveBhashiniCredentials(
            apiKey: String,
            userId: String,
            ulcaApiKey: String,
            endpointUrl: String
        ) {
            _config.value = _config.value.copy(
                bhashiniApiKey = apiKey,
                bhashiniUserId = userId,
                bhashiniUlcaApiKey = ulcaApiKey,
                bhashiniEndpointUrl = endpointUrl
            )
        }
    }

    @Test
    fun testAllSupportedLanguagesAndFallbackPrompts() {
        assertEquals(10, ReminderLanguage.entries.size)
        for (lang in ReminderLanguage.entries) {
            assertEquals(lang, ReminderLanguage.fromCode(lang.code.uppercase()))
            val prompt = ReminderLanguage.buildLocalizedFallbackPrompt(
                langCode = lang.code,
                title = "Donepezil",
                details = "5mg"
            )
            assertTrue("Prompt for ${lang.code} should contain title", prompt.contains("Donepezil"))
            assertTrue("Prompt for ${lang.code} should contain details", prompt.contains("5mg"))
        }
    }

    @Test
    fun testBhashiniPayloadForEnglishAndIndianLanguages() {
        val client = BhashiniTtsClient()

        val englishPayload = client.buildComputePayload(
            englishPrompt = "Take medicine",
            targetLanguage = ReminderLanguage.ENGLISH
        )
        assertTrue(englishPayload.contains(""""taskType":"tts""""))
        assertTrue(!englishPayload.contains(""""taskType":"translation""""))
        assertTrue(englishPayload.contains("ai4bharat/indic-tts-coqui-misc-gpu--t4"))

        val hindiPayload = client.buildComputePayload(
            englishPrompt = "Take \"morning\" medicine",
            targetLanguage = ReminderLanguage.HINDI
        )
        assertTrue(hindiPayload.contains(""""taskType":"translation""""))
        assertTrue(hindiPayload.contains(""""targetLanguage":"hi""""))
        assertTrue(hindiPayload.contains("ai4bharat/indic-tts-coqui-indo_aryan-gpu--t4"))
        assertTrue(hindiPayload.contains("""Take \"morning\" medicine"""))

        val tamilPayload = client.buildComputePayload(
            englishPrompt = "Drink water",
            targetLanguage = ReminderLanguage.TAMIL
        )
        assertTrue(tamilPayload.contains(""""targetLanguage":"ta""""))
        assertTrue(tamilPayload.contains("ai4bharat/indic-tts-coqui-dravidian-gpu--t4"))
    }

    @Test
    fun testExtractAudioBase64AndUlcaConfigParsing() {
        val client = BhashiniTtsClient()
        val expectedBytes = "RIFFtestaudio".toByteArray()
        val encoded = Base64.getEncoder().encodeToString(expectedBytes)

        val dhruvaJson = """{"pipelineResponse":[{"taskType":"tts","audio":[{"audioContent":"$encoded"}]}]}"""
        val extracted = client.extractAudioBase64(dhruvaJson)
        assertNotNull(extracted)
        assertArrayEquals(expectedBytes, Base64.getMimeDecoder().decode(extracted))

        assertNull(client.extractAudioBase64("""{"message":"error"}"""))

        val ulcaConfigJson = """
            {
              "pipelineResponseConfig": [
                {"taskType":"translation","config":[{"serviceId":"nmt-svc-1"}]},
                {"taskType":"tts","config":[{"serviceId":"tts-svc-2"}]}
              ],
              "pipelineInferenceAPIEndPoint": {
                "callbackUrl": "https:\/\/dhruva-api.bhashini.gov.in\/services\/inference\/pipeline",
                "inferenceApiKey": {"name": "Authorization", "value": "secret-token-123"}
              }
            }
        """.trimIndent()
        val parsed = client.parseConfigResponse(ulcaConfigJson)
        assertNotNull(parsed)
        assertEquals("https://dhruva-api.bhashini.gov.in/services/inference/pipeline", parsed?.callbackUrl)
        assertEquals("Authorization", parsed?.authHeaderName)
        assertEquals("secret-token-123", parsed?.authHeaderValue)
        assertEquals("nmt-svc-1", parsed?.translationServiceId)
        assertEquals("tts-svc-2", parsed?.ttsServiceId)
    }

    @Test
    fun testSpeakerRespectsToggleAndPlaysBhashiniAudioWhenConfigured() = runBlocking {
        val settings = FakeVoiceSettings(
            VoiceConfig(
                voiceRemindersEnabled = false,
                preferredLanguage = "kn",
                bhashiniApiKey = "test-api-key"
            )
        )
        val expectedAudio = byteArrayOf(1, 2, 3, 4)
        val fakeClient = object : IBhashiniTtsClient {
            override suspend fun synthesizeSpeech(
                englishPrompt: String,
                targetLanguage: ReminderLanguage,
                config: VoiceConfig
            ): ByteArray = expectedAudio
        }
        val promptManager = AudioPromptManager(context = null)
        val speaker = VoiceReminderSpeaker(
            context = null,
            voiceSettings = settings,
            bhashiniClient = fakeClient,
            audioPromptManager = promptManager
        )
        var capturedBytes: ByteArray? = null
        speaker.audioBytesPlayer = { bytes ->
            capturedBytes = bytes
            true
        }

        val disabledResult = speaker.speakReminderIfEnabled("Water", "1 glass")
        assertEquals(VoicePlaybackResult.Disabled, disabledResult)

        settings.setVoiceRemindersEnabled(true)
        val enabledResult = speaker.speakReminderIfEnabled("Water", "1 glass")
        assertEquals(VoicePlaybackResult.PlayedViaBhashini(ReminderLanguage.KANNADA), enabledResult)
        assertArrayEquals(expectedAudio, capturedBytes)
    }

    @Test
    fun testSpeakerHandlesMissingCredentialsOrBhashiniFailureGracefully() = runBlocking {
        val settings = FakeVoiceSettings(
            VoiceConfig(
                voiceRemindersEnabled = true,
                preferredLanguage = "ta",
                bhashiniApiKey = ""
            )
        )
        val failingClient = object : IBhashiniTtsClient {
            override suspend fun synthesizeSpeech(
                englishPrompt: String,
                targetLanguage: ReminderLanguage,
                config: VoiceConfig
            ): ByteArray? = null
        }
        val speaker = VoiceReminderSpeaker(
            context = null,
            voiceSettings = settings,
            bhashiniClient = failingClient,
            audioPromptManager = AudioPromptManager(context = null)
        )
        val result = speaker.speakReminderIfEnabled("Lunch", "Healthy meal")
        assertTrue(result is VoicePlaybackResult.Failed)
    }
}
