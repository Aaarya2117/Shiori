package com.shiori.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shiori.app.alarm.AlarmScheduler
import com.shiori.app.domain.model.GameSession
import com.shiori.app.domain.model.SyncStatus
import com.shiori.app.domain.repository.IGameRepository
import com.shiori.app.domain.repository.IPatientRepository
import com.shiori.app.domain.repository.IReminderRepository
import com.shiori.app.domain.repository.IRoutineRepository
import com.shiori.app.ui.caregiver.CaregiverDashboardScreen
import com.shiori.app.ui.caregiver.CaregiverPinScreen
import com.shiori.app.ui.patient.DailyRoutineScreen
import com.shiori.app.ui.patient.FaceRecallScreen
import com.shiori.app.ui.patient.MemoryMatchScreen
import com.shiori.app.ui.patient.PatientHomeScreen
import com.shiori.app.ui.patient.PatternSequenceScreen
import com.shiori.app.voice.IVoiceReminderSettings
import com.shiori.app.voice.VoiceReminderSpeaker
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun ShioriNavHost(
    gameRepository: IGameRepository? = null,
    reminderRepository: IReminderRepository? = null,
    routineRepository: IRoutineRepository? = null,
    patientRepository: IPatientRepository? = null,
    alarmScheduler: AlarmScheduler? = null,
    voiceSettings: IVoiceReminderSettings? = null,
    voiceSpeaker: VoiceReminderSpeaker? = null
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val todayDate = remember { LocalDate.now().toString() }

    LaunchedEffect(routineRepository) {
        routineRepository?.ensureSeeded()
    }

    val routineTasks by (routineRepository?.observeRoutineTasks() ?: flowOf(emptyList()))
        .collectAsState(initial = emptyList())

    fun recordGameSession(gameType: String, score: Int, accuracy: Float, durationSecs: Long) {
        if (gameRepository != null) {
            scope.launch {
                val now = System.currentTimeMillis()
                val session = GameSession(
                    id = "session-${now}-${(1000..9999).random()}",
                    gameType = gameType,
                    startTimeMillis = now - (durationSecs * 1000),
                    endTimeMillis = now,
                    accuracyPercent = accuracy,
                    reactionLatencyMs = 800L,
                    difficultyLevel = 1,
                    completed = true,
                    syncStatus = SyncStatus.PENDING
                )
                gameRepository.insertGameSession(session)
            }
        }
    }

    NavHost(navController = navController, startDestination = NavRoutes.PatientHome.route) {
        composable(NavRoutes.PatientHome.route) {
            PatientHomeScreen(
                completedRoutineCount = routineTasks.count { it.isDoneToday(todayDate) },
                totalRoutineCount = routineTasks.size,
                onGameClick = { game ->
                    when (game) {
                        "Memory Match" -> navController.navigate(NavRoutes.MemoryMatch.route)
                        "pattern" -> navController.navigate(NavRoutes.PatternSequence.route)
                        "face" -> navController.navigate(NavRoutes.FaceRecall.route)
                    }
                },
                onRoutineClick = {
                    navController.navigate(NavRoutes.DailyRoutine.route)
                },
                onCaregiverClick = {
                    navController.navigate(NavRoutes.CaregiverPin.route)
                }
            )
        }
        composable(NavRoutes.MemoryMatch.route) {
            MemoryMatchScreen(
                onBack = { navController.popBackStack() },
                onGameCompleted = { score, accuracy, durationSecs ->
                    recordGameSession("MEMORY_MATCH", score, accuracy, durationSecs)
                }
            )
        }
        composable(NavRoutes.PatternSequence.route) {
            PatternSequenceScreen(
                onBack = { navController.popBackStack() },
                onGameCompleted = { score, accuracy, durationSecs ->
                    recordGameSession("PATTERN_SEQUENCE", score, accuracy, durationSecs)
                }
            )
        }
        composable(NavRoutes.FaceRecall.route) {
            FaceRecallScreen(
                onBack = { navController.popBackStack() },
                onGameCompleted = { score, accuracy, durationSecs ->
                    recordGameSession("FACE_RECALL", score, accuracy, durationSecs)
                }
            )
        }
        composable(NavRoutes.DailyRoutine.route) {
            DailyRoutineScreen(
                routineRepository = routineRepository,
                onBack = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.CaregiverPin.route) {
            CaregiverPinScreen(
                onPinCorrect = {
                    navController.navigate(NavRoutes.CaregiverDashboard.route) {
                        popUpTo(NavRoutes.CaregiverPin.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.CaregiverDashboard.route) {
            CaregiverDashboardScreen(
                gameRepository = gameRepository,
                reminderRepository = reminderRepository,
                routineRepository = routineRepository,
                patientRepository = patientRepository,
                alarmScheduler = alarmScheduler,
                voiceSettings = voiceSettings,
                voiceSpeaker = voiceSpeaker,
                onBackToPatient = {
                    navController.navigate(NavRoutes.PatientHome.route) {
                        popUpTo(NavRoutes.PatientHome.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
