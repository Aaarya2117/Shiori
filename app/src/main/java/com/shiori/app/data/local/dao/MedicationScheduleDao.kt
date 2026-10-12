package com.shiori.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shiori.app.data.local.entity.MedicationScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationScheduleDao {
    @Query("SELECT * FROM medication_schedules WHERE isActive = 1 ORDER BY scheduledHour ASC, scheduledMinute ASC")
    fun observeActiveSchedules(): Flow<List<MedicationScheduleEntity>>

    @Query("SELECT * FROM medication_schedules WHERE isActive = 1")
    suspend fun getActiveSchedules(): List<MedicationScheduleEntity>

    @Query("SELECT * FROM medication_schedules WHERE id = :id LIMIT 1")
    suspend fun getScheduleById(id: Long): MedicationScheduleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: MedicationScheduleEntity): Long

    @Update
    suspend fun updateSchedule(schedule: MedicationScheduleEntity)

    @Query("DELETE FROM medication_schedules WHERE id = :id")
    suspend fun deleteSchedule(id: Long)
}
