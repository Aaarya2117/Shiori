package com.shiori.app.domain.model

import java.time.LocalDate

data class RoutineTask(
    val id: Long = 0L,
    val title: String,
    val timeBucket: String,
    val emoji: String = "✅",
    val isCompleted: Boolean = false,
    val lastCompletedDate: String = "",
    val sortOrder: Int = 0,
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING
) {
    fun isDoneToday(todayDate: String = LocalDate.now().toString()): Boolean {
        return isCompleted && lastCompletedDate == todayDate
    }

    companion object {
        const val BUCKET_MORNING = "Morning"
        const val BUCKET_AFTERNOON = "Afternoon"
        const val BUCKET_EVENING = "Evening"

        val TIME_BUCKETS = listOf(BUCKET_MORNING, BUCKET_AFTERNOON, BUCKET_EVENING)

        fun defaultTasks(): List<RoutineTask> = listOf(
            RoutineTask(title = "Morning Walk & Fresh Air", timeBucket = BUCKET_MORNING, emoji = "🚶‍♂️", sortOrder = 1),
            RoutineTask(title = "Drink 1 Glass of Water", timeBucket = BUCKET_MORNING, emoji = "💧", sortOrder = 2),
            RoutineTask(title = "Take Morning Medications", timeBucket = BUCKET_MORNING, emoji = "💊", sortOrder = 3),
            RoutineTask(title = "Nutritious Lunch", timeBucket = BUCKET_AFTERNOON, emoji = "🥗", sortOrder = 4),
            RoutineTask(title = "Afternoon Hydration", timeBucket = BUCKET_AFTERNOON, emoji = "💧", sortOrder = 5),
            RoutineTask(title = "Play 1 Cognitive Brain Game", timeBucket = BUCKET_AFTERNOON, emoji = "🧠", sortOrder = 6),
            RoutineTask(title = "Light Dinner", timeBucket = BUCKET_EVENING, emoji = "🍲", sortOrder = 7),
            RoutineTask(title = "Evening Medications", timeBucket = BUCKET_EVENING, emoji = "💊", sortOrder = 8),
            RoutineTask(title = "Bedtime Relaxation & Sleep", timeBucket = BUCKET_EVENING, emoji = "🌙", sortOrder = 9)
        )
    }
}
