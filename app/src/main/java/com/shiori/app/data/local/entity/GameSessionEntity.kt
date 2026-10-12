package com.shiori.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shiori.app.domain.model.SyncStatus

@Entity(tableName = "game_sessions")
data class GameSessionEntity(
    @PrimaryKey
    val id: String,
    val gameType: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val accuracyPercent: Float,
    val reactionLatencyMs: Long,
    val difficultyLevel: Int,
    val completed: Boolean,
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING
)
