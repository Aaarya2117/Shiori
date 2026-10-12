package com.shiori.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shiori.app.data.local.entity.PatientProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientProfileDao {
    @Query("SELECT * FROM patient_profiles WHERE id = :id LIMIT 1")
    fun observeProfile(id: String = "default_patient"): Flow<PatientProfileEntity?>

    @Query("SELECT * FROM patient_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfile(id: String = "default_patient"): PatientProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: PatientProfileEntity)
}
