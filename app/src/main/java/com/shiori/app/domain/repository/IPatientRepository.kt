package com.shiori.app.domain.repository

import com.shiori.app.domain.model.PatientProfile
import kotlinx.coroutines.flow.Flow

interface IPatientRepository {
    fun observePatientProfile(): Flow<PatientProfile?>
    suspend fun getPatientProfile(): PatientProfile?
    suspend fun savePatientProfile(profile: PatientProfile)
}
