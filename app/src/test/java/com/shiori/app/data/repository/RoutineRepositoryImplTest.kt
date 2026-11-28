package com.shiori.app.data.repository

import com.shiori.app.data.local.dao.RoutineTaskDao
import com.shiori.app.data.local.entity.RoutineTaskEntity
import com.shiori.app.domain.model.RoutineTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RoutineRepositoryImplTest {

    private class FakeRoutineTaskDao : RoutineTaskDao {
        private val items = mutableListOf<RoutineTaskEntity>()
        private val flow = MutableStateFlow<List<RoutineTaskEntity>>(emptyList())
        private var nextId = 1L

        override fun observeAllTasks(): Flow<List<RoutineTaskEntity>> = flow

        override suspend fun getAllTasks(): List<RoutineTaskEntity> = sortedItems()

        override suspend fun countTasks(): Int = items.size

        override suspend fun insertTask(task: RoutineTaskEntity): Long {
            val id = if (task.id == 0L) nextId++ else task.id
            val entity = task.copy(id = id)
            items.removeAll { it.id == id }
            items.add(entity)
            emit()
            return id
        }

        override suspend fun insertTasks(tasks: List<RoutineTaskEntity>) {
            tasks.forEach { task ->
                val id = if (task.id == 0L) nextId++ else task.id
                items.add(task.copy(id = id))
            }
            emit()
        }

        override suspend fun updateTask(task: RoutineTaskEntity) {
            val idx = items.indexOfFirst { it.id == task.id }
            if (idx != -1) {
                items[idx] = task
                emit()
            }
        }

        override suspend fun deleteTask(id: Long) {
            items.removeAll { it.id == id }
            emit()
        }

        private fun sortedItems(): List<RoutineTaskEntity> =
            items.sortedWith(compareBy<RoutineTaskEntity> { it.sortOrder }.thenBy { it.id })

        private fun emit() {
            flow.value = sortedItems()
        }
    }

    private lateinit var dao: FakeRoutineTaskDao
    private lateinit var repository: RoutineRepositoryImpl

    @Before
    fun setUp() {
        dao = FakeRoutineTaskDao()
        repository = RoutineRepositoryImpl(dao = dao, context = null)
    }

    @Test
    fun testEnsureSeededOnceAndDoesNotReseedAfterDeletion() = runBlocking {
        repository.ensureSeeded()
        val seeded = repository.observeRoutineTasks().first()
        assertEquals(9, seeded.size)

        seeded.forEach { repository.deleteRoutineTask(it.id) }
        assertEquals(0, repository.getRoutineTasks().size)

        repository.ensureSeeded()
        assertEquals(0, repository.getRoutineTasks().size)
    }

    @Test
    fun testCrudAndDailyResetByDate() = runBlocking {
        val id = repository.addRoutineTask(
            title = "Evening Herbal Tea",
            timeBucket = RoutineTask.BUCKET_EVENING,
            emoji = "🍵"
        )
        var tasks = repository.getRoutineTasks()
        assertEquals(1, tasks.size)
        assertEquals("Evening Herbal Tea", tasks.first().title)

        repository.updateRoutineTask(tasks.first().copy(title = "Evening Warm Water"))
        tasks = repository.getRoutineTasks()
        assertEquals("Evening Warm Water", tasks.first().title)

        repository.toggleTaskCompletion(tasks.first(), todayDate = "2026-11-27")
        tasks = repository.getRoutineTasks()
        assertTrue(tasks.first().isDoneToday("2026-11-27"))
        assertFalse(tasks.first().isDoneToday("2026-11-28"))

        repository.deleteRoutineTask(id)
        assertEquals(0, repository.getRoutineTasks().size)
    }
}
