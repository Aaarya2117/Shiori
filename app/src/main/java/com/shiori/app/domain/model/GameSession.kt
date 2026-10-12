package com.shiori.app.domain.model

data class GameSession(
    val id: String,
    val gameType: String, // MEMORY_MATCH, PATTERN_SEQUENCE, FACE_RECALL
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val accuracyPercent: Float,
    val reactionLatencyMs: Long,
    val difficultyLevel: Int,
    val completed: Boolean,
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING
)
