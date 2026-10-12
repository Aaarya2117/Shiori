package com.shiori.app.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.shiori.app.domain.model.ReminderRecord
import com.shiori.app.domain.model.ReminderStatus
import com.shiori.app.domain.repository.IReminderRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var reminderRepository: IReminderRepository

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    companion object {
        const val CHANNEL_ID = "channel_critical_reminders"
        const val ACTION_ALARM_TRIGGER = "com.shiori.app.ACTION_ALARM_TRIGGER"
        const val ACTION_MARK_TAKEN = "com.shiori.app.ACTION_MARK_TAKEN"
        const val EXTRA_SCHEDULE_ID = "extra_schedule_id"
        const val EXTRA_RECORD_ID = "extra_record_id"
        const val EXTRA_MEDICINE_NAME = "extra_medicine_name"
        const val EXTRA_DOSAGE = "extra_dosage"
        const val EXTRA_TRIGGER_MILLIS = "extra_trigger_millis"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (action) {
                    ACTION_ALARM_TRIGGER -> {
                        val scheduleId = intent.getLongExtra(EXTRA_SCHEDULE_ID, -1L)
                        val medicineName = intent.getStringExtra(EXTRA_MEDICINE_NAME) ?: "Medicine"
                        val dosage = intent.getStringExtra(EXTRA_DOSAGE) ?: ""
                        val triggerMillis = intent.getLongExtra(EXTRA_TRIGGER_MILLIS, System.currentTimeMillis())

                        if (scheduleId != -1L) {
                            val recordId = reminderRepository.insertRecord(
                                ReminderRecord(
                                    scheduleId = scheduleId,
                                    scheduledEpochMillis = triggerMillis,
                                    status = ReminderStatus.TRIGGERED
                                )
                            )
                            showHeadsUpNotification(context, scheduleId, recordId, medicineName, dosage)
                            // Reschedule next recurring alarm
                            val schedule = reminderRepository.getScheduleById(scheduleId)
                            if (schedule != null) {
                                alarmScheduler.scheduleSchedule(schedule)
                            }
                        }
                    }
                    ACTION_MARK_TAKEN -> {
                        val recordId = intent.getLongExtra(EXTRA_RECORD_ID, -1L)
                        val notificationId = intent.getIntExtra(EXTRA_SCHEDULE_ID, 0)
                        if (recordId != -1L) {
                            reminderRepository.recordReminderTaken(recordId, System.currentTimeMillis())
                            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                            notificationManager?.cancel(notificationId)
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showHeadsUpNotification(
        context: Context,
        scheduleId: Long,
        recordId: Long,
        medicineName: String,
        dosage: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Critical Medication Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority alarms for scheduled medications"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val takenIntent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_MARK_TAKEN
            putExtra(EXTRA_RECORD_ID, recordId)
            putExtra(EXTRA_SCHEDULE_ID, scheduleId.toInt())
        }
        val takenPendingIntent = PendingIntent.getBroadcast(
            context,
            recordId.toInt(),
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Time to take: $medicineName")
            .setContentText("Dosage: $dosage")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .addAction(android.R.drawable.checkbox_on_background, "TAKEN", takenPendingIntent)
            .build()

        notificationManager.notify(scheduleId.toInt(), notification)
    }
}
