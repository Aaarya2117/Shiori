package com.shiori.app.domain.model

data class ReminderRecord(
    val id: Long = 0,
    val scheduleId: Long,
    val scheduledEpochMillis: Long,
    val status: ReminderStatus = ReminderStatus.SCHEDULED,
    val acknowledgedAt: Long? = null,
    val caregiverAlertSent: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING
)
