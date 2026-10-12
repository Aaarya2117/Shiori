package com.shiori.app.data.repository

import com.shiori.app.data.local.dao.MedicationScheduleDao
import com.shiori.app.data.local.dao.ReminderRecordDao
import com.shiori.app.data.local.entity.MedicationScheduleEntity
import com.shiori.app.data.local.entity.ReminderRecordEntity
import com.shiori.app.domain.model.MedicationSchedule
import com.shiori.app.domain.model.ReminderRecord
import com.shiori.app.domain.repository.IReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepositoryImpl @Inject constructor(
    private val scheduleDao: MedicationScheduleDao,
    private val recordDao: ReminderRecordDao
) : IReminderRepository {

    override fun observeActiveSchedules(): Flow<List<MedicationSchedule>> {
        return scheduleDao.observeActiveSchedules().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getActiveSchedules(): List<MedicationSchedule> {
        return scheduleDao.getActiveSchedules().map { it.toDomain() }
    }

    override suspend fun getScheduleById(id: Long): MedicationSchedule? {
        return scheduleDao.getScheduleById(id)?.toDomain()
    }

    override suspend fun insertSchedule(schedule: MedicationSchedule): Long {
        return scheduleDao.insertSchedule(schedule.toEntity())
    }

    override suspend fun updateSchedule(schedule: MedicationSchedule) {
        scheduleDao.updateSchedule(schedule.toEntity())
    }

    override suspend fun deleteSchedule(id: Long) {
        scheduleDao.deleteSchedule(id)
    }

    override fun observeTodayRecords(): Flow<List<ReminderRecord>> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return recordDao.observeRecordsFrom(calendar.timeInMillis).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun insertRecord(record: ReminderRecord): Long {
        return recordDao.insertRecord(record.toEntity())
    }

    override suspend fun recordReminderTaken(recordId: Long, takenAtMillis: Long) {
        recordDao.markReminderTaken(recordId, takenAtMillis)
    }

    override suspend fun getPendingUnacknowledgedReminders(thresholdMillis: Long): List<ReminderRecord> {
        return recordDao.getUnacknowledgedReminders(thresholdMillis).map { it.toDomain() }
    }

    private fun MedicationScheduleEntity.toDomain() = MedicationSchedule(
        id = id,
        medicineName = medicineName,
        dosage = dosage,
        scheduledHour = scheduledHour,
        scheduledMinute = scheduledMinute,
        daysOfWeek = daysOfWeek,
        isActive = isActive,
        instructionAudioResId = instructionAudioResId,
        updatedAt = updatedAt,
        syncStatus = syncStatus
    )

    private fun MedicationSchedule.toEntity() = MedicationScheduleEntity(
        id = id,
        medicineName = medicineName,
        dosage = dosage,
        scheduledHour = scheduledHour,
        scheduledMinute = scheduledMinute,
        daysOfWeek = daysOfWeek,
        isActive = isActive,
        instructionAudioResId = instructionAudioResId,
        updatedAt = updatedAt,
        syncStatus = syncStatus
    )

    private fun ReminderRecordEntity.toDomain() = ReminderRecord(
        id = id,
        scheduleId = scheduleId,
        scheduledEpochMillis = scheduledEpochMillis,
        status = status,
        acknowledgedAt = acknowledgedAt,
        caregiverAlertSent = caregiverAlertSent,
        updatedAt = updatedAt,
        syncStatus = syncStatus
    )

    private fun ReminderRecord.toEntity() = ReminderRecordEntity(
        id = id,
        scheduleId = scheduleId,
        scheduledEpochMillis = scheduledEpochMillis,
        status = status,
        acknowledgedAt = acknowledgedAt,
        caregiverAlertSent = caregiverAlertSent,
        updatedAt = updatedAt,
        syncStatus = syncStatus
    )
}
