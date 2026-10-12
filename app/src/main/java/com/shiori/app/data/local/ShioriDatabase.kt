package com.shiori.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.shiori.app.data.local.converter.Converters
import com.shiori.app.data.local.dao.GameSessionDao
import com.shiori.app.data.local.dao.MedicationScheduleDao
import com.shiori.app.data.local.dao.PatientProfileDao
import com.shiori.app.data.local.dao.ReminderRecordDao
import com.shiori.app.data.local.entity.GameSessionEntity
import com.shiori.app.data.local.entity.MedicationScheduleEntity
import com.shiori.app.data.local.entity.PatientProfileEntity
import com.shiori.app.data.local.entity.ReminderRecordEntity

@Database(
    entities = [
        MedicationScheduleEntity::class,
        ReminderRecordEntity::class,
        PatientProfileEntity::class,
        GameSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ShioriDatabase : RoomDatabase() {
    abstract fun medicationScheduleDao(): MedicationScheduleDao
    abstract fun reminderRecordDao(): ReminderRecordDao
    abstract fun patientProfileDao(): PatientProfileDao
    abstract fun gameSessionDao(): GameSessionDao
}
