package com.shiori.app.domain.model

data class MedicationSchedule(
    val id: Long = 0,
    val medicineName: String,
    val dosage: String,
    val scheduledHour: Int, // 0..23
    val scheduledMinute: Int, // 0..59
    val daysOfWeek: List<Int>, // 1 = Sunday, 2 = Monday .. 7 = Saturday (Calendar.DAY_OF_WEEK)
    val isActive: Boolean = true,
    val instructionAudioResId: Int? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING
)
