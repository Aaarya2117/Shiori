package com.shiori.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shiori.app.domain.model.SyncStatus

@Entity(tableName = "medication_schedules")
data class MedicationScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicineName: String,
    val dosage: String,
    val scheduledHour: Int,
    val scheduledMinute: Int,
    val daysOfWeek: List<Int>,
    val isActive: Boolean = true,
    val instructionAudioResId: Int? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING
)
