package com.shiori.app.domain.repository

import com.shiori.app.domain.model.RoutineTask
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface IRoutineRepository {
    fun observeRoutineTasks(): Flow<List<RoutineTask>>
    suspend fun getRoutineTasks(): List<RoutineTask>
    suspend fun ensureSeeded()
    suspend fun addRoutineTask(title: String, timeBucket: String, emoji: String): Long
    suspend fun updateRoutineTask(task: RoutineTask)
    suspend fun toggleTaskCompletion(task: RoutineTask, todayDate: String = LocalDate.now().toString())
    suspend fun deleteRoutineTask(id: Long)
}
