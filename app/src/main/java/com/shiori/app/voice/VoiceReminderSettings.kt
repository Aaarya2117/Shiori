package com.shiori.app.voice

import android.content.Context
import com.shiori.app.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

const val DEFAULT_BHASHINI_URL = "https://dhruva-api.bhashini.gov.in/services/inference/pipeline"

data class VoiceConfig(
    val voiceRemindersEnabled: Boolean = false,
    val preferredLanguage: String = "hi",
    val bhashiniApiKey: String = "",
    val bhashiniUserId: String = "",
    val bhashiniUlcaApiKey: String = "",
    val bhashiniEndpointUrl: String = DEFAULT_BHASHINI_URL
) {
    val hasBhashiniCredentials: Boolean
        get() = bhashiniApiKey.isNotBlank() ||
            (bhashiniUserId.isNotBlank() && bhashiniUlcaApiKey.isNotBlank())
}

interface IVoiceReminderSettings {
    val configFlow: StateFlow<VoiceConfig>
    fun getConfig(): VoiceConfig
    fun setVoiceRemindersEnabled(enabled: Boolean)
    fun setPreferredLanguage(langCode: String)
    fun saveBhashiniCredentials(
        apiKey: String,
        userId: String,
        ulcaApiKey: String,
        endpointUrl: String
    )
}

@Singleton
class SharedPrefsVoiceReminderSettings @Inject constructor(
    @ApplicationContext context: Context
) : IVoiceReminderSettings {

    companion object {
        private const val PREFS_NAME = "shiori_voice_prefs"
        private const val KEY_ENABLED = "voice_reminders_enabled"
        private const val KEY_LANG = "voice_preferred_language"
        private const val KEY_API_KEY = "bhashini_api_key"
        private const val KEY_USER_ID = "bhashini_user_id"
        private const val KEY_ULCA_KEY = "bhashini_ulca_api_key"
        private const val KEY_ENDPOINT = "bhashini_endpoint_url"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _configFlow = MutableStateFlow(readFromPrefs())
    override val configFlow: StateFlow<VoiceConfig> = _configFlow.asStateFlow()

    private fun readFromPrefs(): VoiceConfig {
        val defaultEndpoint = BuildConfig.BHASHINI_PIPELINE_URL.ifBlank { DEFAULT_BHASHINI_URL }
        return VoiceConfig(
            voiceRemindersEnabled = prefs.getBoolean(KEY_ENABLED, false),
            preferredLanguage = prefs.getString(KEY_LANG, "hi") ?: "hi",
            bhashiniApiKey = prefs.getString(KEY_API_KEY, BuildConfig.BHASHINI_API_KEY) ?: "",
            bhashiniUserId = prefs.getString(KEY_USER_ID, BuildConfig.BHASHINI_USER_ID) ?: "",
            bhashiniUlcaApiKey = prefs.getString(KEY_ULCA_KEY, BuildConfig.BHASHINI_ULCA_API_KEY) ?: "",
            bhashiniEndpointUrl = prefs.getString(KEY_ENDPOINT, defaultEndpoint)
                ?.ifBlank { defaultEndpoint } ?: defaultEndpoint
        )
    }

    override fun getConfig(): VoiceConfig = _configFlow.value

    override fun setVoiceRemindersEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
        _configFlow.value = _configFlow.value.copy(voiceRemindersEnabled = enabled)
    }

    override fun setPreferredLanguage(langCode: String) {
        val resolved = ReminderLanguage.fromCode(langCode).code
        prefs.edit().putString(KEY_LANG, resolved).apply()
        _configFlow.value = _configFlow.value.copy(preferredLanguage = resolved)
    }

    override fun saveBhashiniCredentials(
        apiKey: String,
        userId: String,
        ulcaApiKey: String,
        endpointUrl: String
    ) {
        val cleanEndpoint = endpointUrl.trim().ifBlank { DEFAULT_BHASHINI_URL }
        prefs.edit()
            .putString(KEY_API_KEY, apiKey.trim())
            .putString(KEY_USER_ID, userId.trim())
            .putString(KEY_ULCA_KEY, ulcaApiKey.trim())
            .putString(KEY_ENDPOINT, cleanEndpoint)
            .apply()
        _configFlow.value = _configFlow.value.copy(
            bhashiniApiKey = apiKey.trim(),
            bhashiniUserId = userId.trim(),
            bhashiniUlcaApiKey = ulcaApiKey.trim(),
            bhashiniEndpointUrl = cleanEndpoint
        )
    }
}
