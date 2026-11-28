package com.shiori.app.ui.caregiver

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shiori.app.alarm.AlarmScheduler
import com.shiori.app.domain.model.GameSession
import com.shiori.app.domain.model.MedicationSchedule
import com.shiori.app.domain.model.ReminderRecord
import com.shiori.app.domain.model.ReminderStatus
import com.shiori.app.domain.model.RoutineTask
import com.shiori.app.domain.repository.IGameRepository
import com.shiori.app.domain.repository.IPatientRepository
import com.shiori.app.domain.repository.IReminderRepository
import com.shiori.app.domain.repository.IRoutineRepository
import com.shiori.app.voice.IVoiceReminderSettings
import com.shiori.app.voice.VoiceReminderSpeaker
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate

@Composable
fun CaregiverDashboardScreen(
    gameRepository: IGameRepository? = null,
    reminderRepository: IReminderRepository? = null,
    routineRepository: IRoutineRepository? = null,
    patientRepository: IPatientRepository? = null,
    alarmScheduler: AlarmScheduler? = null,
    voiceSettings: IVoiceReminderSettings? = null,
    voiceSpeaker: VoiceReminderSpeaker? = null,
    onBackToPatient: () -> Unit
) {
    BackHandler { onBackToPatient() }

    var selectedTab by remember { mutableIntStateOf(0) }
    var showDisclaimerDialog by remember { mutableStateOf(false) }
    val todayDate = remember { LocalDate.now().toString() }

    LaunchedEffect(routineRepository) {
        routineRepository?.ensureSeeded()
    }

    val sessions: List<GameSession> by (gameRepository?.observeGameSessions() ?: flowOf(emptyList()))
        .collectAsState(initial = emptyList())

    val reminders: List<ReminderRecord> by (reminderRepository?.observeTodayRecords() ?: flowOf(emptyList()))
        .collectAsState(initial = emptyList())

    val schedules: List<MedicationSchedule> by (reminderRepository?.observeAllSchedules() ?: flowOf(emptyList()))
        .collectAsState(initial = emptyList())

    val routineTasks: List<RoutineTask> by (routineRepository?.observeRoutineTasks() ?: flowOf(emptyList()))
        .collectAsState(initial = emptyList())

    val totalGamesPlayed = sessions.size
    val averageAccuracy = if (sessions.isNotEmpty()) {
        sessions.map { it.accuracyPercent }.average().toFloat()
    } else 0f

    val takenReminders = reminders.count { it.status == ReminderStatus.TAKEN }
    val totalReminders = reminders.size

    val doneRoutineCount = routineTasks.count { it.isDoneToday(todayDate) }
    val totalRoutineCount = routineTasks.size

    val nextScheduleSummary = remember(schedules) {
        val nextActive = schedules.firstOrNull { it.isActive }
        if (nextActive != null) {
            "Active: ${nextActive.medicineName} at ${formatTime12Hour(nextActive.scheduledHour, nextActive.scheduledMinute)}"
        } else {
            "No active reminders scheduled"
        }
    }

    if (showDisclaimerDialog) {
        DpdpDisclaimerDialog(onDismiss = { showDisclaimerDialog = false })
    }

    Scaffold(
        containerColor = Color(0xFFFBFDF9)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Caregiver Dashboard",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF006C50)
                    )
                    Text(
                        text = "Routine, Reminders & Voice Settings",
                        fontSize = 15.sp,
                        color = Color(0xFF424742)
                    )
                }

                Button(
                    onClick = onBackToPatient,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006C50)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Exit", fontSize = 16.sp)
                }
            }

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFFFBFDF9),
                contentColor = Color(0xFF006C50)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Overview", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Routine & Reminders", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Voice & Bhashini", fontWeight = FontWeight.SemiBold) }
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // Patient Profile Overview Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F5EA))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text("🧓", fontSize = 42.sp)
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = "Patient Profile: Primary",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF191C1A)
                                    )
                                    Text(
                                        text = "Mode: Offline Secure | Local Encrypted",
                                        fontSize = 15.sp,
                                        color = Color(0xFF006C50)
                                    )
                                }
                            }
                        }

                        // Daily Routine Completion Card
                        Text(
                            text = "Daily Routine Adherence (Today)",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF191C1A)
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Tasks Completed", fontSize = 18.sp, color = Color(0xFF424742))
                                    Text(
                                        text = "$doneRoutineCount / $totalRoutineCount",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF006C50)
                                    )
                                }
                                val routineProgress = if (totalRoutineCount > 0) {
                                    doneRoutineCount.toFloat() / totalRoutineCount
                                } else 0f
                                LinearProgressIndicator(
                                    progress = { routineProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp),
                                    color = Color(0xFF006C50),
                                    trackColor = Color(0xFFE0E0E0)
                                )
                            }
                        }

                        // Reminder Adherence Section
                        Text(
                            text = "Reminder Adherence (Today)",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF191C1A)
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Reminders Acknowledged", fontSize = 18.sp, color = Color(0xFF424742))
                                    Text(
                                        text = if (totalReminders > 0) "$takenReminders / $totalReminders" else "${schedules.count { it.isActive }} Active",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF006C50)
                                    )
                                }
                                val adhProgress = if (totalReminders > 0) takenReminders.toFloat() / totalReminders else 1.0f
                                LinearProgressIndicator(
                                    progress = { adhProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp),
                                    color = Color(0xFF006C50),
                                    trackColor = Color(0xFFE0E0E0)
                                )
                                Text(
                                    text = nextScheduleSummary,
                                    fontSize = 14.sp,
                                    color = Color(0xFF757575)
                                )
                            }
                        }

                        // Cognitive Game Analytics Section
                        Text(
                            text = "Cognitive Game Trends",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF191C1A)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            StatCard(
                                title = "Total Sessions",
                                value = "$totalGamesPlayed",
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Avg Accuracy",
                                value = if (totalGamesPlayed > 0) "${averageAccuracy.toInt()}%" else "—",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (sessions.isNotEmpty()) {
                            Text(
                                text = "Recent Activity Log",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF191C1A)
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                sessions.takeLast(4).reversed().forEach { session ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(1.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = formatGameName(session.gameType),
                                                    fontSize = 17.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF191C1A)
                                                )
                                                Text(
                                                    text = "Difficulty: Lvl ${session.difficultyLevel}",
                                                    fontSize = 14.sp,
                                                    color = Color(0xFF757575)
                                                )
                                            }
                                            Text(
                                                text = "${session.accuracyPercent.toInt()}%",
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (session.accuracyPercent >= 80f) Color(0xFF006C50) else Color(0xFFE65100)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = { showDisclaimerDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("📜 View DPDP & Medical Notice", fontSize = 16.sp, color = Color(0xFF006C50))
                        }

                        Button(
                            onClick = onBackToPatient,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006C50)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Return to Patient Home", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    1 -> {
                        CaregiverScheduleManagerSection(
                            routineTasks = routineTasks,
                            schedules = schedules,
                            routineRepository = routineRepository,
                            reminderRepository = reminderRepository,
                            alarmScheduler = alarmScheduler
                        )
                    }

                    2 -> {
                        CaregiverVoiceSettingsSection(
                            voiceSettings = voiceSettings,
                            voiceSpeaker = voiceSpeaker,
                            patientRepository = patientRepository
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F5EA))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 15.sp, color = Color(0xFF424742))
            Spacer(Modifier.height(6.dp))
            Text(
                value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF006C50)
            )
        }
    }
}

private fun formatGameName(type: String): String = when (type) {
    "MEMORY_MATCH" -> "Memory Match"
    "PATTERN_SEQUENCE" -> "Pattern Sequence"
    "FACE_RECALL" -> "Face Recall"
    else -> type
}
