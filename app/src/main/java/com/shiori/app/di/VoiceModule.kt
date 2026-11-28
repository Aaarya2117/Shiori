package com.shiori.app.di

import android.content.Context
import com.shiori.app.voice.AudioPromptManager
import com.shiori.app.voice.BhashiniTtsClient
import com.shiori.app.voice.IBhashiniTtsClient
import com.shiori.app.voice.IVoiceReminderSettings
import com.shiori.app.voice.SharedPrefsVoiceReminderSettings
import com.shiori.app.voice.VoiceReminderSpeaker
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class VoiceModule {
    @Binds
    @Singleton
    abstract fun bindVoiceReminderSettings(
        impl: SharedPrefsVoiceReminderSettings
    ): IVoiceReminderSettings

    @Binds
    @Singleton
    abstract fun bindBhashiniTtsClient(
        impl: BhashiniTtsClient
    ): IBhashiniTtsClient

    companion object {
        @Provides
        @Singleton
        fun provideVoiceReminderSpeaker(
            @ApplicationContext context: Context,
            voiceSettings: IVoiceReminderSettings,
            bhashiniClient: IBhashiniTtsClient,
            audioPromptManager: AudioPromptManager
        ): VoiceReminderSpeaker = VoiceReminderSpeaker(
            context = context,
            voiceSettings = voiceSettings,
            bhashiniClient = bhashiniClient,
            audioPromptManager = audioPromptManager
        )
    }
}
