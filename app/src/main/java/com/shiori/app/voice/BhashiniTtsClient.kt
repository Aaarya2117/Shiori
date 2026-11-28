package com.shiori.app.voice

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

interface IBhashiniTtsClient {
    suspend fun synthesizeSpeech(
        englishPrompt: String,
        targetLanguage: ReminderLanguage,
        config: VoiceConfig
    ): ByteArray?
}

@Singleton
class BhashiniTtsClient @Inject constructor() : IBhashiniTtsClient {

    data class ResolvedPipelineConfig(
        val callbackUrl: String,
        val authHeaderName: String,
        val authHeaderValue: String,
        val translationServiceId: String?,
        val ttsServiceId: String?
    )

    companion object {
        private const val ULCA_CONFIG_URL =
            "https://meity-auth.ulcacontrib.org/ulca/apis/v0/model/getModelsPipeline"
        private const val DEFAULT_PIPELINE_ID = "64392f96daac500b55c543cd"
        private const val DEFAULT_NMT_SERVICE_ID = "ai4bharat/indictrans-v2-all-gpu--t4"
        private const val CONNECT_TIMEOUT_MS = 5_000
        private const val READ_TIMEOUT_MS = 6_000
    }

    override suspend fun synthesizeSpeech(
        englishPrompt: String,
        targetLanguage: ReminderLanguage,
        config: VoiceConfig
    ): ByteArray? = withContext(Dispatchers.IO) {
        if (englishPrompt.isBlank() || !config.hasBhashiniCredentials) {
            return@withContext null
        }

        try {
            var targetUrl = config.bhashiniEndpointUrl.ifBlank { DEFAULT_BHASHINI_URL }
            var authHeaderName = "Authorization"
            var authHeaderValue = config.bhashiniApiKey.trim()
            var nmtServiceId = DEFAULT_NMT_SERVICE_ID
            var ttsServiceId = targetLanguage.ttsFamily.defaultServiceId

            if (authHeaderValue.isBlank() &&
                config.bhashiniUserId.isNotBlank() &&
                config.bhashiniUlcaApiKey.isNotBlank()
            ) {
                val configPayload = buildConfigPayload(targetLanguage)
                val configResp = postJson(
                    urlString = ULCA_CONFIG_URL,
                    payload = configPayload,
                    headers = mapOf(
                        "userID" to config.bhashiniUserId.trim(),
                        "ulcaApiKey" to config.bhashiniUlcaApiKey.trim()
                    )
                ) ?: return@withContext null

                val resolved = parseConfigResponse(configResp) ?: return@withContext null
                targetUrl = resolved.callbackUrl.ifBlank { targetUrl }
                authHeaderName = resolved.authHeaderName.ifBlank { "Authorization" }
                authHeaderValue = resolved.authHeaderValue
                if (!resolved.translationServiceId.isNullOrBlank()) {
                    nmtServiceId = resolved.translationServiceId
                }
                if (!resolved.ttsServiceId.isNullOrBlank()) {
                    ttsServiceId = resolved.ttsServiceId
                }
            }

            if (authHeaderValue.isBlank()) return@withContext null

            val computePayload = buildComputePayload(
                englishPrompt = englishPrompt,
                targetLanguage = targetLanguage,
                translationServiceId = nmtServiceId,
                ttsServiceId = ttsServiceId
            )

            val computeResp = postJson(
                urlString = targetUrl,
                payload = computePayload,
                headers = mapOf(authHeaderName to authHeaderValue)
            ) ?: return@withContext null

            val base64 = extractAudioBase64(computeResp) ?: return@withContext null
            val bytes = Base64.getMimeDecoder().decode(base64)
            if (bytes.isEmpty()) null else bytes
        } catch (_: Exception) {
            null
        }
    }

    internal fun buildConfigPayload(targetLanguage: ReminderLanguage): String {
        val tasksJson = if (targetLanguage == ReminderLanguage.ENGLISH) {
            """[{"taskType":"tts","config":{"language":{"sourceLanguage":"en"}}}]"""
        } else {
            """[{"taskType":"translation","config":{"language":{"sourceLanguage":"en","targetLanguage":"${targetLanguage.code}"}}},{"taskType":"tts","config":{"language":{"sourceLanguage":"${targetLanguage.code}"}}}]"""
        }
        return """{"pipelineTasks":$tasksJson,"pipelineRequestConfig":{"pipelineId":"$DEFAULT_PIPELINE_ID"}}"""
    }

    internal fun buildComputePayload(
        englishPrompt: String,
        targetLanguage: ReminderLanguage,
        translationServiceId: String = DEFAULT_NMT_SERVICE_ID,
        ttsServiceId: String = targetLanguage.ttsFamily.defaultServiceId
    ): String {
        val escapedPrompt = escapeJson(englishPrompt)
        val escapedNmt = escapeJson(translationServiceId)
        val escapedTts = escapeJson(ttsServiceId)

        val tasksJson = if (targetLanguage == ReminderLanguage.ENGLISH) {
            """[{"taskType":"tts","config":{"language":{"sourceLanguage":"en"},"serviceId":"$escapedTts","gender":"female","samplingRate":16000}}]"""
        } else {
            """[{"taskType":"translation","config":{"language":{"sourceLanguage":"en","targetLanguage":"${targetLanguage.code}"},"serviceId":"$escapedNmt"}},{"taskType":"tts","config":{"language":{"sourceLanguage":"${targetLanguage.code}"},"serviceId":"$escapedTts","gender":"female","samplingRate":16000}}]"""
        }
        return """{"pipelineTasks":$tasksJson,"inputData":{"input":[{"source":"$escapedPrompt"}]}}"""
    }

    internal fun extractAudioBase64(json: String): String? {
        val match = Regex(""""audioContent"\s*:\s*"([^"]+)"""").find(json) ?: return null
        val raw = match.groupValues.getOrNull(1)?.replace("\\/", "/")?.trim()
        return raw?.takeIf { it.isNotEmpty() }
    }

    internal fun parseConfigResponse(json: String): ResolvedPipelineConfig? {
        val callbackUrl = Regex(""""callbackUrl"\s*:\s*"([^"]+)"""")
            .find(json)?.groupValues?.getOrNull(1)?.replace("\\/", "/") ?: return null

        val inferenceBlock = Regex(""""inferenceApiKey"\s*:\s*\{([^}]*)\}""")
            .find(json)?.groupValues?.getOrNull(1) ?: ""
        val authHeaderName = Regex(""""name"\s*:\s*"([^"]+)"""")
            .find(inferenceBlock)?.groupValues?.getOrNull(1) ?: "Authorization"
        val authHeaderValue = Regex(""""value"\s*:\s*"([^"]+)"""")
            .find(inferenceBlock)?.groupValues?.getOrNull(1) ?: return null

        val translationServiceId = Regex(
            """"taskType"\s*:\s*"translation"[\s\S]*?"serviceId"\s*:\s*"([^"]+)""""
        ).find(json)?.groupValues?.getOrNull(1)
        val ttsServiceId = Regex(
            """"taskType"\s*:\s*"tts"[\s\S]*?"serviceId"\s*:\s*"([^"]+)""""
        ).find(json)?.groupValues?.getOrNull(1)

        return ResolvedPipelineConfig(
            callbackUrl = callbackUrl,
            authHeaderName = authHeaderName,
            authHeaderValue = authHeaderValue,
            translationServiceId = translationServiceId,
            ttsServiceId = ttsServiceId
        )
    }

    private fun postJson(
        urlString: String,
        payload: String,
        headers: Map<String, String>
    ): String? {
        val connection = (URL(urlString).openConnection() as? HttpURLConnection) ?: return null
        return try {
            connection.requestMethod = "POST"
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            headers.forEach { (key, value) ->
                connection.setRequestProperty(key, value)
            }
            connection.outputStream.use { out ->
                out.write(payload.toByteArray(Charsets.UTF_8))
                out.flush()
            }
            val code = connection.responseCode
            if (code !in 200..299) {
                return null
            }
            connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private fun escapeJson(value: String): String {
        val sb = StringBuilder(value.length + 8)
        for (ch in value) {
            when (ch) {
                '\\' -> sb.append("\\\\")
                '"' -> sb.append("\\\"")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }
}
