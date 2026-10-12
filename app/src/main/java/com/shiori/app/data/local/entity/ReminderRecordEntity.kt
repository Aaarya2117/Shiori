package com.shiori.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shiori.app.domain.model.ReminderStatus
import com.shiori.app.domain.model.SyncStatus

@Entity(tableName = "reminder_records")
data class ReminderRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val scheduleId: Long,
    val scheduledEpochMillis: Long,
    val status: ReminderStatus = ReminderStatus.SCHEDULED,
    val acknowledgedAt: Long? = null,
    val caregiverAlertSent: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING
)
