package com.shiori.app.ui.patient

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ShioriGreen = Color(0xFF006C50)

@Composable
fun PatientHomeScreen(
    onGameClick: (String) -> Unit = {},
    onRoutineClick: () -> Unit = {},
    onReminderClick: () -> Unit = {},
    onCaregiverClick: () -> Unit = {}
) {
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

            Text(
                "Good Morning!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF191C1A)
            )

            Text(
                "Welcome to Shiori. Let's have a great day!",
                fontSize = 20.sp,
                color = Color(0xFF191C1A)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE0F5EA)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Today's Progress", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("Your daily activities will appear here.", fontSize = 18.sp)
                }
            }

            Text("Brain Games", fontSize = 26.sp, fontWeight = FontWeight.Bold)

            GameCard("🧠", "Memory Match", "Match pairs of hidden cards") {
                onGameClick("Memory Match")
            }

            GameCard("🎨", "Pattern Sequence", "Remember and repeat patterns") {
                onGameClick("pattern")
            }

            GameCard("😊", "Face Recall", "Remember familiar faces") {
                onGameClick("face")
            }

            Text("Daily Routine", fontSize = 26.sp, fontWeight = FontWeight.Bold)

            ActionButton("📋", "View Daily Routine", onRoutineClick)

            Text("Reminders", fontSize = 26.sp, fontWeight = FontWeight.Bold)

            ActionButton("🔔", "Medication & Hydration", onReminderClick)

            OutlinedButton(
                onClick = onCaregiverClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Caregiver Mode (PIN)", fontSize = 19.sp)
            }
        }
    }
}

@Composable
private fun GameCard(
    emoji: String,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 34.sp)

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(description, fontSize = 16.sp)
            }

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ShioriGreen
                )
            ) {
                Text("Play", fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun ActionButton(
    emoji: String,
    title: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ShioriGreen
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text("$emoji  $title", fontSize = 19.sp)
    }
}