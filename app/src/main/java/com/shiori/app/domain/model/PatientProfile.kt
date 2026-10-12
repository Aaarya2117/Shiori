package com.shiori.app.domain.model

data class PatientProfile(
    val id: String = "default_patient",
    val name: String,
    val preferredLanguage: String = "hi", // hi, en, kn
    val caregiverPhoneNumber: String,
    val caregiverPinHash: String = "",
    val isLocalOnlyMode: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING
)
