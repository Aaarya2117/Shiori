package com.shiori.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.shiori.app.domain.repository.IReminderRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var reminderRepository: IReminderRepository

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val activeSchedules = reminderRepository.getActiveSchedules()
                    for (schedule in activeSchedules) {
                        alarmScheduler.scheduleSchedule(schedule)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
