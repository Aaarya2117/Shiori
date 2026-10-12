package com.shiori.app.alarm

import android.content.Context
import android.content.ContextWrapper
import com.shiori.app.domain.model.MedicationSchedule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class AlarmSchedulerTest {
    private lateinit var context: Context
    private lateinit var scheduler: AlarmScheduler

    @Before
    fun setUp() {
        context = object : ContextWrapper(null) {}
        scheduler = AlarmScheduler(context)
    }

    @Test
    fun testCalculateNextTriggerMillisFutureToday() {
        val nowCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val currentDay = nowCal.get(Calendar.DAY_OF_WEEK)
        val schedule = MedicationSchedule(
            id = 1L,
            medicineName = "Donepezil",
            dosage = "5mg",
            scheduledHour = 10,
            scheduledMinute = 30,
            daysOfWeek = listOf(currentDay),
            isActive = true
        )

        val triggerMillis = scheduler.calculateNextTriggerMillis(schedule, nowCal.timeInMillis)
        val triggerCal = Calendar.getInstance().apply { timeInMillis = triggerMillis }

        assertEquals(10, triggerCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(30, triggerCal.get(Calendar.MINUTE))
        assertTrue(triggerMillis > nowCal.timeInMillis)
    }

    @Test
    fun testCalculateNextTriggerMillisTomorrowWhenTimePassed() {
        val nowCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val allDays = (1..7).toList()
        val schedule = MedicationSchedule(
            id = 1L,
            medicineName = "Donepezil",
            dosage = "5mg",
            scheduledHour = 9,
            scheduledMinute = 0,
            daysOfWeek = allDays,
            isActive = true
        )

        val triggerMillis = scheduler.calculateNextTriggerMillis(schedule, nowCal.timeInMillis)
        assertTrue(triggerMillis > nowCal.timeInMillis)
        val triggerCal = Calendar.getInstance().apply { timeInMillis = triggerMillis }
        assertEquals(9, triggerCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, triggerCal.get(Calendar.MINUTE))
    }
}
