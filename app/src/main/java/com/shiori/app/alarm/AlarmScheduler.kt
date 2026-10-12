package com.shiori.app.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.shiori.app.domain.model.MedicationSchedule
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    private val context: Context
) {
    private val alarmManager: AlarmManager? by lazy {
        context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
    }

    fun scheduleSchedule(schedule: MedicationSchedule) {
        if (!schedule.isActive) return
        val triggerMillis = calculateNextTriggerMillis(schedule, System.currentTimeMillis())
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_ALARM_TRIGGER
            putExtra(ReminderReceiver.EXTRA_SCHEDULE_ID, schedule.id)
            putExtra(ReminderReceiver.EXTRA_MEDICINE_NAME, schedule.medicineName)
            putExtra(ReminderReceiver.EXTRA_DOSAGE, schedule.dosage)
            putExtra(ReminderReceiver.EXTRA_TRIGGER_MILLIS, triggerMillis)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val am = alarmManager ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            } else {
                am.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            }
        } else {
            am.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
        }
    }

    fun cancelSchedule(scheduleId: Long) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null && alarmManager != null) {
            alarmManager?.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun calculateNextTriggerMillis(schedule: MedicationSchedule, currentEpochMillis: Long): Long {
        for (dayOffset in 0..7) {
            val candidate = Calendar.getInstance().apply {
                timeInMillis = currentEpochMillis
                add(Calendar.DAY_OF_YEAR, dayOffset)
                set(Calendar.HOUR_OF_DAY, schedule.scheduledHour)
                set(Calendar.MINUTE, schedule.scheduledMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (candidate.timeInMillis <= currentEpochMillis) {
                continue
            }

            val dayOfWeek = candidate.get(Calendar.DAY_OF_WEEK)
            if (schedule.daysOfWeek.isEmpty() || schedule.daysOfWeek.contains(dayOfWeek)) {
                return candidate.timeInMillis
            }
        }
        // Fallback: 24h from now
        return currentEpochMillis + 24 * 3600 * 1000L
    }
}
