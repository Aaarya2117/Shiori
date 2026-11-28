package com.shiori.app.data.repository

import com.shiori.app.data.local.dao.MedicationScheduleDao
import com.shiori.app.data.local.dao.ReminderRecordDao
import com.shiori.app.data.local.entity.MedicationScheduleEntity
import com.shiori.app.data.local.entity.ReminderRecordEntity
import com.shiori.app.domain.model.MedicationSchedule
import com.shiori.app.domain.model.ReminderStatus
import com.shiori.app.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class ReminderRepositoryImplTest {

    private class FakeScheduleDao : MedicationScheduleDao {
        private val schedules = mutableListOf<MedicationScheduleEntity>()
        private val allFlow = MutableStateFlow<List<MedicationScheduleEntity>>(emptyList())
        private val activeFlow = MutableStateFlow<List<MedicationScheduleEntity>>(emptyList())

        override fun observeAllSchedules(): Flow<List<MedicationScheduleEntity>> = allFlow

        override fun observeActiveSchedules(): Flow<List<MedicationScheduleEntity>> = activeFlow

        override suspend fun getActiveSchedules(): List<MedicationScheduleEntity> =
            schedules.filter { it.isActive }

        override suspend fun getScheduleById(id: Long): MedicationScheduleEntity? =
            schedules.find { it.id == id }

        override suspend fun insertSchedule(schedule: MedicationScheduleEntity): Long {
            val assignedId = if (schedule.id == 0L) (schedules.size + 1).toLong() else schedule.id
            val entity = schedule.copy(id = assignedId)
            schedules.removeAll { it.id == assignedId }
            schedules.add(entity)
            emitFlows()
            return assignedId
        }

        override suspend fun updateSchedule(schedule: MedicationScheduleEntity) {
            schedules.removeAll { it.id == schedule.id }
            schedules.add(schedule)
            emitFlows()
        }

        override suspend fun deleteSchedule(id: Long) {
            schedules.removeAll { it.id == id }
            emitFlows()
        }

        private fun emitFlows() {
            val sorted = schedules.sortedWith(
                compareBy<MedicationScheduleEntity> { it.scheduledHour }.thenBy { it.scheduledMinute }
            )
            allFlow.value = sorted
            activeFlow.value = sorted.filter { it.isActive }
        }
    }

    private class FakeRecordDao : ReminderRecordDao {
        private val records = mutableListOf<ReminderRecordEntity>()
        private val stateFlow = MutableStateFlow<List<ReminderRecordEntity>>(emptyList())

        override fun observeRecordsFrom(startOfDayMillis: Long): Flow<List<ReminderRecordEntity>> = stateFlow

        override suspend fun getRecordById(id: Long): ReminderRecordEntity? =
            records.find { it.id == id }

        override suspend fun insertRecord(record: ReminderRecordEntity): Long {
            val assignedId = if (record.id == 0L) (records.size + 1).toLong() else record.id
            val entity = record.copy(id = assignedId)
            records.add(entity)
            stateFlow.value = records.toList()
            return assignedId
        }

        override suspend fun markReminderTaken(
            recordId: Long,
            takenAtMillis: Long,
            status: ReminderStatus,
            syncStatus: SyncStatus
        ) {
            val index = records.indexOfFirst { it.id == recordId }
            if (index != -1) {
                records[index] = records[index].copy(
                    status = status,
                    acknowledgedAt = takenAtMillis,
                    syncStatus = syncStatus,
                    updatedAt = takenAtMillis
                )
                stateFlow.value = records.toList()
            }
        }

        override suspend fun getUnacknowledgedReminders(thresholdMillis: Long): List<ReminderRecordEntity> =
            records.filter { it.status == ReminderStatus.TRIGGERED && it.scheduledEpochMillis <= thresholdMillis && !it.caregiverAlertSent }
    }

    private lateinit var scheduleDao: FakeScheduleDao
    private lateinit var recordDao: FakeRecordDao
    private lateinit var repository: ReminderRepositoryImpl

    @Before
    fun setUp() {
        scheduleDao = FakeScheduleDao()
        recordDao = FakeRecordDao()
        repository = ReminderRepositoryImpl(scheduleDao, recordDao)
    }

    @Test
    fun testInsertAndRetrieveSchedule() = runBlocking {
        val schedule = MedicationSchedule(
            medicineName = "Aspirin",
            dosage = "75mg",
            scheduledHour = 9,
            scheduledMinute = 15,
            daysOfWeek = listOf(1, 3, 5),
            isActive = true
        )
        val id = repository.insertSchedule(schedule)
        val fetched = repository.getScheduleById(id)
        assertNotNull(fetched)
        assertEquals("Aspirin", fetched?.medicineName)
        assertEquals(9, fetched?.scheduledHour)

        val activeList = repository.observeActiveSchedules().first()
        assertEquals(1, activeList.size)
    }

    @Test
    fun testRecordTaken() = runBlocking {
        val recordId = repository.insertRecord(
            com.shiori.app.domain.model.ReminderRecord(
                scheduleId = 1L,
                scheduledEpochMillis = 1000L,
                status = ReminderStatus.TRIGGERED
            )
        )
        repository.recordReminderTaken(recordId, 1050L)
        val pending = repository.getPendingUnacknowledgedReminders(2000L)
        assertEquals(0, pending.size)
    }

    @Test
    fun testObserveAllIncludesInactiveWhileObserveActiveFilters() = runBlocking {
        val activeId = repository.insertSchedule(
            MedicationSchedule(
                medicineName = "Morning Pill",
                dosage = "1 tab",
                scheduledHour = 8,
                scheduledMinute = 0,
                daysOfWeek = listOf(1, 2, 3, 4, 5, 6, 7),
                isActive = true
            )
        )
        val pausedId = repository.insertSchedule(
            MedicationSchedule(
                medicineName = "Evening Walk Reminder",
                dosage = "15 mins",
                scheduledHour = 18,
                scheduledMinute = 30,
                daysOfWeek = listOf(1, 2, 3, 4, 5, 6, 7),
                isActive = false
            )
        )

        val all = repository.observeAllSchedules().first()
        val active = repository.observeActiveSchedules().first()

        assertEquals(2, all.size)
        assertEquals(1, active.size)
        assertEquals(activeId, active.first().id)
        assertEquals(pausedId, all.last().id)
    }
}
