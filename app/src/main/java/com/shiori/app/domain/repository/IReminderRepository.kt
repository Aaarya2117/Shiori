package com.shiori.app.domain.repository

import com.shiori.app.domain.model.MedicationSchedule
import com.shiori.app.domain.model.ReminderRecord
import kotlinx.coroutines.flow.Flow

interface IReminderRepository {
    fun observeAllSchedules(): Flow<List<MedicationSchedule>>
    fun observeActiveSchedules(): Flow<List<MedicationSchedule>>
    suspend fun getActiveSchedules(): List<MedicationSchedule>
    suspend fun getScheduleById(id: Long): MedicationSchedule?
    suspend fun insertSchedule(schedule: MedicationSchedule): Long
    suspend fun updateSchedule(schedule: MedicationSchedule)
    suspend fun deleteSchedule(id: Long)

    fun observeTodayRecords(): Flow<List<ReminderRecord>>
    suspend fun insertRecord(record: ReminderRecord): Long
    suspend fun recordReminderTaken(recordId: Long, takenAtMillis: Long)
    suspend fun getPendingUnacknowledgedReminders(thresholdMillis: Long): List<ReminderRecord>
}
