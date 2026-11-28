package com.shiori.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.shiori.app.alarm.AlarmScheduler
import com.shiori.app.domain.repository.IGameRepository
import com.shiori.app.domain.repository.IPatientRepository
import com.shiori.app.domain.repository.IReminderRepository
import com.shiori.app.domain.repository.IRoutineRepository
import com.shiori.app.ui.navigation.ShioriNavHost
import com.shiori.app.ui.theme.ShioriTheme
import com.shiori.app.voice.IVoiceReminderSettings
import com.shiori.app.voice.VoiceReminderSpeaker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var gameRepository: IGameRepository

    @Inject
    lateinit var reminderRepository: IReminderRepository

    @Inject
    lateinit var routineRepository: IRoutineRepository

    @Inject
    lateinit var patientRepository: IPatientRepository

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var voiceSettings: IVoiceReminderSettings

    @Inject
    lateinit var voiceSpeaker: VoiceReminderSpeaker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShioriTheme {
                ShioriNavHost(
                    gameRepository = gameRepository,
                    reminderRepository = reminderRepository,
                    routineRepository = routineRepository,
                    patientRepository = patientRepository,
                    alarmScheduler = alarmScheduler,
                    voiceSettings = voiceSettings,
                    voiceSpeaker = voiceSpeaker
                )
            }
        }
    }
}
