package com.shiori.app.data.repository

import android.content.Context
import com.shiori.app.data.local.dao.RoutineTaskDao
import com.shiori.app.data.local.entity.RoutineTaskEntity
import com.shiori.app.domain.model.RoutineTask
import com.shiori.app.domain.model.SyncStatus
import com.shiori.app.domain.repository.IRoutineRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineRepositoryImpl @Inject constructor(
    private val dao: RoutineTaskDao,
    @param:ApplicationContext private val context: Context?
) : IRoutineRepository {

    companion object {
        private const val PREFS_NAME = "shiori_routine_prefs"
        private const val KEY_DEFAULTS_SEEDED = "routine_defaults_seeded"
    }

    private val seedMutex = Mutex()
    private var inMemorySeededFlag = false

    private fun isSeededFlagSet(): Boolean {
        val prefs = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs?.getBoolean(KEY_DEFAULTS_SEEDED, false) ?: inMemorySeededFlag
    }

    private fun markSeededFlag() {
        val prefs = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs != null) {
            prefs.edit().putBoolean(KEY_DEFAULTS_SEEDED, true).apply()
        } else {
            inMemorySeededFlag = true
        }
    }

    override fun observeRoutineTasks(): Flow<List<RoutineTask>> {
        return dao.observeAllTasks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getRoutineTasks(): List<RoutineTask> {
        return dao.getAllTasks().map { it.toDomain() }
    }

    override suspend fun ensureSeeded() {
        seedMutex.withLock {
            if (!isSeededFlagSet()) {
                if (dao.countTasks() == 0) {
                    dao.insertTasks(RoutineTask.defaultTasks().map { it.toEntity() })
                }
                markSeededFlag()
            }
        }
    }

    override suspend fun addRoutineTask(title: String, timeBucket: String, emoji: String): Long {
        val existing = dao.getAllTasks()
        val nextOrder = (existing.maxOfOrNull { it.sortOrder } ?: 0) + 1
        val task = RoutineTask(
            title = title.trim(),
            timeBucket = timeBucket,
            emoji = emoji.ifBlank { "✅" },
            sortOrder = nextOrder,
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING
        )
        return dao.insertTask(task.toEntity())
    }

    override suspend fun updateRoutineTask(task: RoutineTask) {
        dao.updateTask(
            task.copy(
                title = task.title.trim(),
                emoji = task.emoji.ifBlank { "✅" },
                updatedAt = System.currentTimeMillis(),
                syncStatus = SyncStatus.PENDING
            ).toEntity()
        )
    }

    override suspend fun toggleTaskCompletion(task: RoutineTask, todayDate: String) {
        val becomingDone = !task.isDoneToday(todayDate)
        val updated = task.copy(
            isCompleted = becomingDone,
            lastCompletedDate = if (becomingDone) todayDate else "",
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING
        )
        dao.updateTask(updated.toEntity())
    }

    override suspend fun deleteRoutineTask(id: Long) {
        dao.deleteTask(id)
    }

    private fun RoutineTaskEntity.toDomain() = RoutineTask(
        id = id,
        title = title,
        timeBucket = timeBucket,
        emoji = emoji,
        isCompleted = isCompleted,
        lastCompletedDate = lastCompletedDate,
        sortOrder = sortOrder,
        updatedAt = updatedAt,
        syncStatus = syncStatus
    )

    private fun RoutineTask.toEntity() = RoutineTaskEntity(
        id = id,
        title = title,
        timeBucket = timeBucket,
        emoji = emoji,
        isCompleted = isCompleted,
        lastCompletedDate = lastCompletedDate,
        sortOrder = sortOrder,
        updatedAt = updatedAt,
        syncStatus = syncStatus
    )
}
