package com.shiori.app.data.repository

import com.shiori.app.data.local.dao.PatientProfileDao
import com.shiori.app.data.local.entity.PatientProfileEntity
import com.shiori.app.domain.model.PatientProfile
import com.shiori.app.domain.repository.IPatientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatientProfileRepositoryImpl @Inject constructor(
    private val dao: PatientProfileDao
) : IPatientRepository {
    override fun observePatientProfile(): Flow<PatientProfile?> {
        return dao.observeProfile().map { it?.toDomain() }
    }

    override suspend fun getPatientProfile(): PatientProfile? {
        return dao.getProfile()?.toDomain()
    }

    override suspend fun savePatientProfile(profile: PatientProfile) {
        dao.insertProfile(profile.toEntity())
    }

    private fun PatientProfileEntity.toDomain() = PatientProfile(
        id = id,
        name = name,
        preferredLanguage = preferredLanguage,
        caregiverPhoneNumber = caregiverPhoneNumber,
        caregiverPinHash = caregiverPinHash,
        isLocalOnlyMode = isLocalOnlyMode,
        updatedAt = updatedAt,
        syncStatus = syncStatus
    )

    private fun PatientProfile.toEntity() = PatientProfileEntity(
        id = id,
        name = name,
        preferredLanguage = preferredLanguage,
        caregiverPhoneNumber = caregiverPhoneNumber,
        caregiverPinHash = caregiverPinHash,
        isLocalOnlyMode = isLocalOnlyMode,
        updatedAt = updatedAt,
        syncStatus = syncStatus
    )
}
