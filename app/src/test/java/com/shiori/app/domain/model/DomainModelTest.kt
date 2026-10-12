package com.shiori.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DomainModelTest {
    @Test
    fun testMedicationScheduleCreation() {
        val schedule = MedicationSchedule(
            id = 1L,
            medicineName = "Metformin",
            dosage = "500mg",
            scheduledHour = 8,
            scheduledMinute = 30,
            daysOfWeek = listOf(1, 2, 3, 4, 5, 6, 7),
            isActive = true
        )
        assertEquals("Metformin", schedule.medicineName)
        assertEquals(8, schedule.scheduledHour)
        assertEquals(30, schedule.scheduledMinute)
        assertEquals(SyncStatus.PENDING, schedule.syncStatus)
    }

    @Test
    fun testReminderRecordStatus() {
        val record = ReminderRecord(
            id = 10L,
            scheduleId = 1L,
            scheduledEpochMillis = 1700000000000L,
            status = ReminderStatus.SCHEDULED
        )
        assertEquals(ReminderStatus.SCHEDULED, record.status)
        assertNull(record.acknowledgedAt)
    }
}
