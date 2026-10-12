package com.shiori.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shiori.app.domain.model.SyncStatus

@Entity(tableName = "patient_profiles")
data class PatientProfileEntity(
    @PrimaryKey
    val id: String = "default_patient",
    val name: String,
    val preferredLanguage: String = "hi",
    val caregiverPhoneNumber: String,
    val caregiverPinHash: String = "",
    val isLocalOnlyMode: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING
)
