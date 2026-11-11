package com.shiori.app.ui.caregiver

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import com.shiori.app.domain.model.GameSession
import com.shiori.app.domain.model.ReminderRecord
import com.shiori.app.domain.model.ReminderStatus
import com.shiori.app.domain.repository.IGameRepository
import com.shiori.app.domain.repository.IReminderRepository
import kotlinx.coroutines.flow.flowOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CaregiverDashboardScreen(
    gameRepository: IGameRepository? = null,
    reminderRepository: IReminderRepository? = null,
    onBackToPatient: () -> Unit
) {
    BackHandler { onBackToPatient() }

    var showDisclaimerDialog by remember { mutableStateOf(false) }

    // Observe game sessions
    val sessions: List<GameSession> by (gameRepository?.observeGameSessions() ?: flowOf(emptyList<GameSession>()))
        .collectAsState(initial = emptyList())

    // Observe reminders for today
    val reminders: List<ReminderRecord> by (reminderRepository?.observeTodayRecords() ?: flowOf(emptyList<ReminderRecord>()))
        .collectAsState(initial = emptyList())

    val totalGamesPlayed = sessions.size
    val averageAccuracy = if (sessions.isNotEmpty()) {
        sessions.map { it.accuracyPercent }.average().toFloat()
    } else 0f

    val takenReminders = reminders.count { it.status == ReminderStatus.TAKEN }
    val totalReminders = reminders.size

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
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Caregiver Dashboard",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF006C50)
                    )
                    Text(
                        text = "Local Health & Cognitive Monitor",
                        fontSize = 16.sp,
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

            // Medication & Hydration Adherence Section
            Text(
                text = "Medication Adherence (Today)",
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
                        Text("Doses Taken", fontSize = 18.sp, color = Color(0xFF424742))
                        Text(
                            text = if (totalReminders > 0) "$takenReminders / $totalReminders" else "Active Schedule",
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
                        text = "Next scheduled: Evening Dose (08:00 PM)",
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

            // Recent Sessions List
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

            Spacer(Modifier.height(8.dp))

            // Action Buttons
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
