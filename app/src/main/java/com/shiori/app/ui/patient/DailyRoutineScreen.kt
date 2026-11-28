package com.shiori.app.ui.patient

import androidx.activity.compose.BackHandler
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
import com.shiori.app.domain.model.RoutineTask
import com.shiori.app.domain.repository.IRoutineRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun DailyRoutineScreen(
    routineRepository: IRoutineRepository? = null,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val scope = rememberCoroutineScope()
    val todayDate = remember { LocalDate.now().toString() }

    LaunchedEffect(routineRepository) {
        routineRepository?.ensureSeeded()
    }

    val persistedTasks by (routineRepository?.observeRoutineTasks() ?: flowOf(emptyList()))
        .collectAsState(initial = emptyList())

    var previewTasks by remember {
        mutableStateOf(
            RoutineTask.defaultTasks().mapIndexed { index, task ->
                task.copy(id = (index + 1).toLong())
            }
        )
    }

    val tasks = if (routineRepository != null) persistedTasks else previewTasks
    val completedCount = tasks.count { it.isDoneToday(todayDate) }
    val progress = if (tasks.isNotEmpty()) completedCount.toFloat() / tasks.size else 0f

    fun toggleTask(task: RoutineTask) {
        if (routineRepository != null) {
            scope.launch {
                routineRepository.toggleTaskCompletion(task, todayDate)
            }
        } else {
            val becomingDone = !task.isDoneToday(todayDate)
            previewTasks = previewTasks.map {
                if (it.id == task.id) {
                    it.copy(
                        isCompleted = becomingDone,
                        lastCompletedDate = if (becomingDone) todayDate else ""
                    )
                } else {
                    it
                }
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFFBFDF9)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
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

            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No daily routine tasks yet. Ask your caregiver to add tasks.",
                        fontSize = 18.sp,
                        color = Color(0xFF424742)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RoutineTask.TIME_BUCKETS.forEach { bucket ->
                        val bucketTasks = tasks.filter { it.timeBucket == bucket }
                        if (bucketTasks.isNotEmpty()) {
                            item(key = "header_$bucket") {
                                Text(
                                    text = "$bucket Routine",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF191C1A),
                                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                                )
                            }
                            items(bucketTasks, key = { it.id }) { task ->
                                val isDone = task.isDoneToday(todayDate)
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 68.dp)
                                        .clickable { toggleTask(task) },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isDone) Color(0xFFE8F5E9) else Color.White
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
                                            checked = isDone,
                                            onCheckedChange = { toggleTask(task) },
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
                                            fontWeight = if (isDone) FontWeight.Normal else FontWeight.Medium,
                                            color = if (isDone) Color(0xFF757575) else Color(0xFF191C1A),
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
    }
}
