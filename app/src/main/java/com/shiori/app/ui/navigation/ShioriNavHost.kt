package com.shiori.app.ui.navigation

import com.shiori.app.ui.patient.FaceRecallScreen
import com.shiori.app.ui.patient.PatternSequenceScreen
import com.shiori.app.ui.patient.MemoryMatchScreen
import com.shiori.app.ui.patient.PatientHomeScreen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.shiori.app.domain.model.GameSession
import com.shiori.app.domain.model.SyncStatus
import com.shiori.app.domain.repository.IGameRepository
import com.shiori.app.domain.repository.IReminderRepository
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun ShioriNavHost(
    gameRepository: IGameRepository? = null,
    reminderRepository: IReminderRepository? = null
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

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
        //1. Patient Home Screen
        composable(NavRoutes.PatientHome.route) {
            PatientHomeScreen(
                onGameClick = { game ->
                    when (game) {
                        "Memory Match" -> {
                            navController.navigate(NavRoutes.MemoryMatch.route)
                        }
                        "pattern" -> {
                            navController.navigate(NavRoutes.PatternSequence.route)
                        }
                        "face" -> {
                            navController.navigate(NavRoutes.FaceRecall.route)
                        }
                    }
                },
                onRoutineClick = {
                    // Routine navigation will be added later
                },
                onReminderClick = {
                    // Reminder navigation will be added later
                },
                onCaregiverClick = {
                    navController.navigate(NavRoutes.CaregiverPin.route)
                }
            )
        }
        //2. Memory Match
        composable(NavRoutes.MemoryMatch.route) {
            MemoryMatchScreen(
                onBack = {
                    navController.popBackStack()
                },
                onGameCompleted = { score, accuracy, durationSecs ->
                    recordGameSession("MEMORY_MATCH", score, accuracy, durationSecs)
                }
            )
        }
        // Pattern Sequence
        composable(NavRoutes.PatternSequence.route) {
            PatternSequenceScreen(
                onBack = {
                    navController.popBackStack()
                },
                onGameCompleted = { score, accuracy, durationSecs ->
                    recordGameSession("PATTERN_SEQUENCE", score, accuracy, durationSecs)
                }
            )
        }
        //FaceRecall
        composable(NavRoutes.FaceRecall.route) {
            FaceRecallScreen(
                onBack = { navController.popBackStack() },
                onGameCompleted = { score, accuracy, durationSecs ->
                    recordGameSession("FACE_RECALL", score, accuracy, durationSecs)
                }
            )
        }
        //3. Caregiver PIN
        composable(NavRoutes.CaregiverPin.route) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Caregiver Mode",
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Placeholder for Person 4 PIN Gate",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = { navController.popBackStack() }) {
                    Text("Back to Patient Mode", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
