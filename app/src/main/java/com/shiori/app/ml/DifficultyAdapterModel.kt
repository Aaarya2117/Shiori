package com.shiori.app.ml

import com.shiori.app.domain.model.GameSession

/**
 * Interface definition for cognitive game adaptive difficulty algorithms (Owned by Person 3).
 */
interface DifficultyAdapterModel {
    suspend fun calculateNextDifficulty(history: List<GameSession>): Int
}
