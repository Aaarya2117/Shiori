package com.shiori.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shiori.app.domain.model.SyncStatus

@Entity(tableName = "routine_tasks")
data class RoutineTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val timeBucket: String,
    val emoji: String = "✅",
    val isCompleted: Boolean = false,
    val lastCompletedDate: String = "",
    val sortOrder: Int = 0,
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING
)
