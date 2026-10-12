package com.shiori.app.data.local.entity

import com.shiori.app.domain.model.ReminderStatus
import com.shiori.app.domain.model.SyncStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class EntityMappingTest {
    @Test
    fun testMedicationScheduleEntityDefaults() {
        val entity = MedicationScheduleEntity(
            id = 5L,
            medicineName = "Donepezil",
            dosage = "10mg",
            scheduledHour = 20,
            scheduledMinute = 0,
            daysOfWeek = listOf(1, 2, 3, 4, 5, 6, 7),
            isActive = true,
            instructionAudioResId = 101,
            updatedAt = 1000L,
            syncStatus = SyncStatus.PENDING
        )
        assertEquals(5L, entity.id)
        assertEquals("Donepezil", entity.medicineName)
        assertEquals("10mg", entity.dosage)
        assertEquals(20, entity.scheduledHour)
        assertEquals(0, entity.scheduledMinute)
        assertEquals(7, entity.daysOfWeek.size)
    }

    @Test
    fun testReminderRecordEntity() {
        val entity = ReminderRecordEntity(
            id = 12L,
            scheduleId = 5L,
            scheduledEpochMillis = 2000L,
            status = ReminderStatus.TAKEN,
            acknowledgedAt = 2050L,
            caregiverAlertSent = false,
            updatedAt = 2050L,
            syncStatus = SyncStatus.SYNCED
        )
        assertEquals(ReminderStatus.TAKEN, entity.status)
        assertEquals(2050L, entity.acknowledgedAt)
        assertEquals(SyncStatus.SYNCED, entity.syncStatus)
    }
}
