package com.shiori.app.ui.caregiver

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shiori.app.alarm.AlarmScheduler
import com.shiori.app.domain.model.MedicationSchedule
import com.shiori.app.domain.model.RoutineTask
import com.shiori.app.domain.model.SyncStatus
import com.shiori.app.domain.repository.IReminderRepository
import com.shiori.app.domain.repository.IRoutineRepository
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

private val ShioriGreen = Color(0xFF006C50)

@Composable
fun CaregiverScheduleManagerSection(
    routineTasks: List<RoutineTask>,
    schedules: List<MedicationSchedule>,
    routineRepository: IRoutineRepository?,
    reminderRepository: IReminderRepository?,
    alarmScheduler: AlarmScheduler?
) {
    val scope = rememberCoroutineScope()
    var showRoutineDialog by remember { mutableStateOf(false) }
    var editingRoutineTask by remember { mutableStateOf<RoutineTask?>(null) }

    var showReminderDialog by remember { mutableStateOf(false) }
    var editingSchedule by remember { mutableStateOf<MedicationSchedule?>(null) }

    if (showRoutineDialog) {
        RoutineTaskDialog(
            initialTask = editingRoutineTask,
            onDismiss = {
                showRoutineDialog = false
                editingRoutineTask = null
            },
            onSave = { title, bucket, emoji ->
                scope.launch {
                    val existing = editingRoutineTask
                    if (existing == null) {
                        routineRepository?.addRoutineTask(title, bucket, emoji)
                    } else {
                        routineRepository?.updateRoutineTask(
                            existing.copy(
                                title = title,
                                timeBucket = bucket,
                                emoji = emoji
                            )
                        )
                    }
                }
                showRoutineDialog = false
                editingRoutineTask = null
            }
        )
    }

    if (showReminderDialog) {
        ReminderScheduleDialog(
            initialSchedule = editingSchedule,
            onDismiss = {
                showReminderDialog = false
                editingSchedule = null
            },
            onSave = { title, dosage, hour, minute, daysOfWeek ->
                scope.launch {
                    val existing = editingSchedule
                    if (existing == null) {
                        val newSchedule = MedicationSchedule(
                            medicineName = title,
                            dosage = dosage,
                            scheduledHour = hour,
                            scheduledMinute = minute,
                            daysOfWeek = daysOfWeek,
                            isActive = true,
                            updatedAt = System.currentTimeMillis(),
                            syncStatus = SyncStatus.PENDING
                        )
                        val newId = reminderRepository?.insertSchedule(newSchedule) ?: 0L
                        if (newId > 0L) {
                            alarmScheduler?.scheduleSchedule(newSchedule.copy(id = newId))
                        }
                    } else {
                        val updated = existing.copy(
                            medicineName = title,
                            dosage = dosage,
                            scheduledHour = hour,
                            scheduledMinute = minute,
                            daysOfWeek = daysOfWeek,
                            updatedAt = System.currentTimeMillis(),
                            syncStatus = SyncStatus.PENDING
                        )
                        reminderRepository?.updateSchedule(updated)
                        if (updated.isActive) {
                            alarmScheduler?.scheduleSchedule(updated)
                        } else {
                            alarmScheduler?.cancelSchedule(updated.id)
                        }
                    }
                }
                showReminderDialog = false
                editingSchedule = null
            }
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        // Daily Routine Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Daily Routine Tasks",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF191C1A)
                )
                Text(
                    text = "Add, edit, or remove tasks shown to the patient",
                    fontSize = 14.sp,
                    color = Color(0xFF424742)
                )
            }
            Button(
                onClick = {
                    editingRoutineTask = null
                    showRoutineDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = ShioriGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("+ Add Task", fontSize = 14.sp)
            }
        }

        if (routineTasks.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Text(
                    text = "No routine tasks configured. Tap + Add Task to create one.",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 15.sp,
                    color = Color(0xFF424742)
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                RoutineTask.TIME_BUCKETS.forEach { bucket ->
                    val bucketTasks = routineTasks.filter { it.timeBucket == bucket }
                    if (bucketTasks.isNotEmpty()) {
                        Text(
                            text = bucket,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShioriGreen,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        bucketTasks.forEach { task ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(task.emoji, fontSize = 24.sp)
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            text = task.title,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF191C1A)
                                        )
                                        Text(
                                            text = task.timeBucket,
                                            fontSize = 13.sp,
                                            color = Color(0xFF757575)
                                        )
                                    }
                                    TextButton(
                                        onClick = {
                                            editingRoutineTask = task
                                            showRoutineDialog = true
                                        }
                                    ) {
                                        Text("Edit", color = ShioriGreen)
                                    }
                                    TextButton(
                                        onClick = {
                                            scope.launch {
                                                routineRepository?.deleteRoutineTask(task.id)
                                            }
                                        }
                                    ) {
                                        Text("Delete", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = Color(0xFFE0E0E0))

        // Scheduled Reminders Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Scheduled Reminders",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF191C1A)
                )
                Text(
                    text = "Exact-time alarms with optional voice announcements",
                    fontSize = 14.sp,
                    color = Color(0xFF424742)
                )
            }
            Button(
                onClick = {
                    editingSchedule = null
                    showReminderDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = ShioriGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("+ Add Reminder", fontSize = 14.sp)
            }
        }

        if (schedules.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Text(
                    text = "No scheduled reminders yet. Tap + Add Reminder to schedule one.",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 15.sp,
                    color = Color(0xFF424742)
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                schedules.forEach { schedule ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (schedule.isActive) Color.White else Color(0xFFF5F5F5)
                        ),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = "${formatTime12Hour(schedule.scheduledHour, schedule.scheduledMinute)} • ${schedule.medicineName}",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF191C1A)
                                    )
                                    if (schedule.dosage.isNotBlank()) {
                                        Text(
                                            text = schedule.dosage,
                                            fontSize = 14.sp,
                                            color = Color(0xFF424742)
                                        )
                                    }
                                    Text(
                                        text = formatDaysSummary(schedule.daysOfWeek),
                                        fontSize = 13.sp,
                                        color = Color(0xFF757575)
                                    )
                                }
                                Switch(
                                    checked = schedule.isActive,
                                    onCheckedChange = { enabled ->
                                        scope.launch {
                                            val updated = schedule.copy(
                                                isActive = enabled,
                                                updatedAt = System.currentTimeMillis(),
                                                syncStatus = SyncStatus.PENDING
                                            )
                                            reminderRepository?.updateSchedule(updated)
                                            if (enabled) {
                                                alarmScheduler?.scheduleSchedule(updated)
                                            } else {
                                                alarmScheduler?.cancelSchedule(updated.id)
                                            }
                                        }
                                    },
                                    colors = SwitchDefaults.colors(checkedTrackColor = ShioriGreen)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        editingSchedule = schedule
                                        showReminderDialog = true
                                    }
                                ) {
                                    Text("Edit", color = ShioriGreen)
                                }
                                TextButton(
                                    onClick = {
                                        scope.launch {
                                            alarmScheduler?.cancelSchedule(schedule.id)
                                            reminderRepository?.deleteSchedule(schedule.id)
                                        }
                                    }
                                ) {
                                    Text("Delete", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoutineTaskDialog(
    initialTask: RoutineTask?,
    onDismiss: () -> Unit,
    onSave: (title: String, bucket: String, emoji: String) -> Unit
) {
    val emojiOptions = listOf("🚶‍♂️", "💧", "💊", "🥗", "🧠", "🍲", "🌙", "🧘", "📖", "🍎")
    var title by remember(initialTask) { mutableStateOf(initialTask?.title ?: "") }
    var selectedBucket by remember(initialTask) {
        mutableStateOf(initialTask?.timeBucket ?: RoutineTask.BUCKET_MORNING)
    }
    var selectedEmoji by remember(initialTask) {
        mutableStateOf(initialTask?.emoji ?: "✅")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialTask == null) "Add Daily Routine Task" else "Edit Daily Routine Task",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    placeholder = { Text("e.g., Drink 1 Glass of Water") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Time of Day", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RoutineTask.TIME_BUCKETS.forEach { bucket ->
                        FilterChip(
                            selected = selectedBucket == bucket,
                            onClick = { selectedBucket = bucket },
                            label = { Text(bucket, fontSize = 12.sp) }
                        )
                    }
                }

                Text("Icon", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    emojiOptions.chunked(5).forEach { rowEmojis ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            rowEmojis.forEach { emoji ->
                                FilterChip(
                                    selected = selectedEmoji == emoji,
                                    onClick = { selectedEmoji = emoji },
                                    label = { Text(emoji, fontSize = 18.sp) }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title.trim(), selectedBucket, selectedEmoji)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ShioriGreen)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ReminderScheduleDialog(
    initialSchedule: MedicationSchedule?,
    onDismiss: () -> Unit,
    onSave: (title: String, dosage: String, hour: Int, minute: Int, daysOfWeek: List<Int>) -> Unit
) {
    var title by remember(initialSchedule) { mutableStateOf(initialSchedule?.medicineName ?: "") }
    var dosage by remember(initialSchedule) { mutableStateOf(initialSchedule?.dosage ?: "") }
    var hour by remember(initialSchedule) { mutableIntStateOf(initialSchedule?.scheduledHour ?: 9) }
    var minute by remember(initialSchedule) { mutableIntStateOf(initialSchedule?.scheduledMinute ?: 0) }
    var selectedDays by remember(initialSchedule) {
        mutableStateOf(
            initialSchedule?.daysOfWeek?.takeIf { it.isNotEmpty() }?.toSet()
                ?: (1..7).toSet()
        )
    }

    val dayChips = listOf(
        Calendar.SUNDAY to "Sun",
        Calendar.MONDAY to "Mon",
        Calendar.TUESDAY to "Tue",
        Calendar.WEDNESDAY to "Wed",
        Calendar.THURSDAY to "Thu",
        Calendar.FRIDAY to "Fri",
        Calendar.SATURDAY to "Sat"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialSchedule == null) "Add Reminder" else "Edit Reminder",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Reminder Title / Medicine") },
                    placeholder = { Text("e.g., Morning Medication") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosage / Voice Note (Optional)") },
                    placeholder = { Text("e.g., 1 tablet with warm water") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Time: ${formatTime12Hour(hour, minute)}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = ShioriGreen
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Hour", fontSize = 12.sp, color = Color(0xFF424742))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(onClick = { hour = (hour + 23) % 24 }) { Text("-") }
                            Text(
                                text = String.format(Locale.US, "%02d", hour),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            OutlinedButton(onClick = { hour = (hour + 1) % 24 }) { Text("+") }
                        }
                    }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Minute", fontSize = 12.sp, color = Color(0xFF424742))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(onClick = { minute = (minute + 55) % 60 }) { Text("-") }
                            Text(
                                text = String.format(Locale.US, "%02d", minute),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            OutlinedButton(onClick = { minute = (minute + 5) % 60 }) { Text("+") }
                        }
                    }
                }

                Text("Repeat Days", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    dayChips.chunked(4).forEach { rowDays ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            rowDays.forEach { (dayInt, label) ->
                                val isSelected = selectedDays.contains(dayInt)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedDays = if (isSelected && selectedDays.size > 1) {
                                            selectedDays - dayInt
                                        } else {
                                            selectedDays + dayInt
                                        }
                                    },
                                    label = { Text(label, fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            title.trim(),
                            dosage.trim(),
                            hour,
                            minute,
                            selectedDays.sorted()
                        )
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ShioriGreen)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

internal fun formatTime12Hour(hour: Int, minute: Int): String {
    val amPm = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return String.format(Locale.US, "%02d:%02d %s", displayHour, minute, amPm)
}

private fun formatDaysSummary(days: List<Int>): String {
    if (days.isEmpty() || days.size == 7) return "Every day"
    val names = mapOf(
        Calendar.SUNDAY to "Sun",
        Calendar.MONDAY to "Mon",
        Calendar.TUESDAY to "Tue",
        Calendar.WEDNESDAY to "Wed",
        Calendar.THURSDAY to "Thu",
        Calendar.FRIDAY to "Fri",
        Calendar.SATURDAY to "Sat"
    )
    return days.sorted().mapNotNull { names[it] }.joinToString(", ")
}
