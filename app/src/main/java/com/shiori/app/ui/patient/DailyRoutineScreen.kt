package com.shiori.app.ui.patient

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class RoutineTask(
    val id: String,
    val title: String,
    val timeBucket: String,
    val emoji: String,
    var isDone: Boolean = false
)

@Composable
fun DailyRoutineScreen(
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var tasks by remember {
        mutableStateOf(
            listOf(
                RoutineTask("1", "Morning Walk & Fresh Air", "Morning", "🚶‍♂️"),
                RoutineTask("2", "Drink 1 Glass of Water", "Morning", "💧"),
                RoutineTask("3", "Take Morning Medications", "Morning", "💊"),
                RoutineTask("4", "Nutritious Lunch", "Afternoon", "🥗"),
                RoutineTask("5", "Afternoon Hydration", "Afternoon", "💧"),
                RoutineTask("6", "Play 1 Cognitive Brain Game", "Afternoon", "🧠"),
                RoutineTask("7", "Light Dinner", "Evening", "🍲"),
                RoutineTask("8", "Evening Medications", "Evening", "💊"),
                RoutineTask("9", "Bedtime Relaxation & Sleep", "Evening", "🌙")
            )
        )
    }

    val completedCount = tasks.count { it.isDone }
    val progress = if (tasks.isNotEmpty()) completedCount.toFloat() / tasks.size else 0f

    Scaffold(
        containerColor = Color(0xFFFBFDF9)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Text("← Back", fontSize = 18.sp, color = Color(0xFF006C50))
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "DAILY ROUTINE",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF006C50)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Progress Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F5EA)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Checklist",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF191C1A)
                        )
                        Text(
                            text = "$completedCount of ${tasks.size} Done",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF006C50)
                        )
                    }
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp),
                        color = Color(0xFF006C50),
                        trackColor = Color.White
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Task List grouped by Time Bucket
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val buckets = listOf("Morning", "Afternoon", "Evening")
                buckets.forEach { bucket ->
                    val bucketTasks = tasks.filter { it.timeBucket == bucket }
                    item {
                        Text(
                            text = "$bucket Routine",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF191C1A),
                            modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                        )
                    }
                    items(bucketTasks, key = { it.id }) { task ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 68.dp)
                                .clickable {
                                    tasks = tasks.map {
                                        if (it.id == task.id) it.copy(isDone = !it.isDone) else it
                                    }
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (task.isDone) Color(0xFFE8F5E9) else Color.White
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Checkbox(
                                    checked = task.isDone,
                                    onCheckedChange = { checked ->
                                        tasks = tasks.map {
                                            if (it.id == task.id) it.copy(isDone = checked) else it
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFF006C50)
                                    )
                                )
                                Text(
                                    text = task.emoji,
                                    fontSize = 26.sp
                                )
                                Text(
                                    text = task.title,
                                    fontSize = 18.sp,
                                    fontWeight = if (task.isDone) FontWeight.Normal else FontWeight.Medium,
                                    color = if (task.isDone) Color(0xFF757575) else Color(0xFF191C1A),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
