package com.shiori.app.data.local.converter

import androidx.room.TypeConverter
import com.shiori.app.domain.model.ReminderStatus
import com.shiori.app.domain.model.SyncStatus

class Converters {
    @TypeConverter
    fun fromIntList(list: List<Int>?): String {
        return list?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toIntList(data: String?): List<Int> {
        if (data.isNullOrEmpty()) return emptyList()
        return data.split(",").mapNotNull { it.trim().toIntOrNull() }
    }

    @TypeConverter
    fun fromSyncStatus(status: SyncStatus?): String = status?.name ?: SyncStatus.PENDING.name

    @TypeConverter
    fun toSyncStatus(value: String?): SyncStatus =
        value?.let { runCatching { SyncStatus.valueOf(it) }.getOrNull() } ?: SyncStatus.PENDING

    @TypeConverter
    fun fromReminderStatus(status: ReminderStatus?): String = status?.name ?: ReminderStatus.SCHEDULED.name

    @TypeConverter
    fun toReminderStatus(value: String?): ReminderStatus =
        value?.let { runCatching { ReminderStatus.valueOf(it) }.getOrNull() } ?: ReminderStatus.SCHEDULED
}
