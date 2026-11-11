package com.shiori.app.ml

import com.shiori.app.domain.model.GameSession
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Tier1StaircaseDifficultyAdapter @Inject constructor() : DifficultyAdapterModel {

    override suspend fun calculateNextDifficulty(history: List<GameSession>): Int {
        if (history.isEmpty()) {
            return 1
        }

        val lastSession = history.last()
        val currentLevel = lastSession.difficultyLevel.coerceIn(1, 5)

        // Rule 1: Single failure drops level (-1)
        if (lastSession.accuracyPercent < 50.0f) {
            return (currentLevel - 1).coerceAtLeast(1)
        }

        // Rule 2: Two consecutive high scores step up (+1)
        if (history.size >= 2) {
            val secondLast = history[history.size - 2]
            if (lastSession.accuracyPercent >= 80.0f && secondLast.accuracyPercent >= 80.0f) {
                return (currentLevel + 1).coerceAtMost(5)
            }
        }

        return currentLevel
    }
}
