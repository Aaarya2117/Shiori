package com.shiori.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shiori.app.data.local.entity.ReminderRecordEntity
import com.shiori.app.domain.model.ReminderStatus
import com.shiori.app.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderRecordDao {
    @Query("SELECT * FROM reminder_records WHERE scheduledEpochMillis >= :startOfDayMillis ORDER BY scheduledEpochMillis DESC")
    fun observeRecordsFrom(startOfDayMillis: Long): Flow<List<ReminderRecordEntity>>

    @Query("SELECT * FROM reminder_records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: Long): ReminderRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: ReminderRecordEntity): Long

    @Query("UPDATE reminder_records SET status = :status, acknowledgedAt = :takenAtMillis, syncStatus = :syncStatus, updatedAt = :takenAtMillis WHERE id = :recordId")
    suspend fun markReminderTaken(
        recordId: Long,
        takenAtMillis: Long,
        status: ReminderStatus = ReminderStatus.TAKEN,
        syncStatus: SyncStatus = SyncStatus.PENDING
    )

    @Query("SELECT * FROM reminder_records WHERE status = 'TRIGGERED' AND scheduledEpochMillis <= :thresholdMillis AND caregiverAlertSent = 0")
    suspend fun getUnacknowledgedReminders(thresholdMillis: Long): List<ReminderRecordEntity>
}
