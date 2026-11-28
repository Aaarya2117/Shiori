package com.shiori.app.voice

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.PowerManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

sealed class VoicePlaybackResult {
    data class PlayedViaBhashini(val language: ReminderLanguage) : VoicePlaybackResult()
    data class PlayedViaDeviceTts(val language: ReminderLanguage) : VoicePlaybackResult()
    data object Disabled : VoicePlaybackResult()
    data class Failed(val reason: String) : VoicePlaybackResult()
}

@Singleton
class VoiceReminderSpeaker(
    private val context: Context?,
    private val voiceSettings: IVoiceReminderSettings,
    private val bhashiniClient: IBhashiniTtsClient,
    private val audioPromptManager: AudioPromptManager
) {
    internal var audioBytesPlayer: ((ByteArray) -> Boolean)? = null
    private var activePlayer: MediaPlayer? = null
    private val playerLock = Any()

    suspend fun speakReminderIfEnabled(title: String, details: String): VoicePlaybackResult {
        if (!voiceSettings.getConfig().voiceRemindersEnabled) {
            return VoicePlaybackResult.Disabled
        }
        return speakReminderNow(title, details)
    }

    suspend fun speakReminderNow(title: String, details: String): VoicePlaybackResult {
        val wakeLock = acquireShortWakeLock()
        return try {
            val config = voiceSettings.getConfig()
            val language = ReminderLanguage.fromCode(config.preferredLanguage)
            audioPromptManager.setLanguage(language.code)

            if (config.hasBhashiniCredentials) {
                val sourcePrompt = ReminderLanguage.buildEnglishSourcePrompt(title, details)
                val audioBytes = bhashiniClient.synthesizeSpeech(sourcePrompt, language, config)
                if (audioBytes != null && audioBytes.isNotEmpty()) {
                    val played = playAudioBytes(audioBytes)
                    if (played) {
                        return VoicePlaybackResult.PlayedViaBhashini(language)
                    }
                }
            }

            val fallbackPrompt = ReminderLanguage.buildLocalizedFallbackPrompt(
                langCode = language.code,
                title = title,
                details = details
            )
            val spoke = audioPromptManager.speakDynamicText(fallbackPrompt, language.code)
            if (spoke) {
                VoicePlaybackResult.PlayedViaDeviceTts(language)
            } else {
                VoicePlaybackResult.Failed("Voice engine unavailable")
            }
        } finally {
            releaseWakeLock(wakeLock)
        }
    }

    private fun playAudioBytes(audioBytes: ByteArray): Boolean {
        audioBytesPlayer?.let { overridePlayer ->
            return overridePlayer(audioBytes)
        }
        val appContext = context ?: return false
        return try {
            val tempFile = File(appContext.cacheDir, "bhashini_reminder.wav")
            FileOutputStream(tempFile).use { it.write(audioBytes) }

            synchronized(playerLock) {
                activePlayer?.runCatching {
                    if (isPlaying) stop()
                    release()
                }
                val player = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    setDataSource(tempFile.absolutePath)
                    setOnCompletionListener { mp ->
                        mp.release()
                        synchronized(playerLock) {
                            if (activePlayer === mp) activePlayer = null
                        }
                    }
                    setOnErrorListener { mp, _, _ ->
                        mp.release()
                        synchronized(playerLock) {
                            if (activePlayer === mp) activePlayer = null
                        }
                        true
                    }
                    prepare()
                    start()
                }
                activePlayer = player
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun acquireShortWakeLock(): PowerManager.WakeLock? {
        val appContext = context ?: return null
        return try {
            val pm = appContext.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return null
            pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "shiori:voice_reminder").apply {
                setReferenceCounted(false)
                acquire(15_000L)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun releaseWakeLock(wakeLock: PowerManager.WakeLock?) {
        try {
            if (wakeLock != null && wakeLock.isHeld) {
                wakeLock.release()
            }
        } catch (_: Exception) {
        }
    }
}
