package com.shiori.app.ml

import com.shiori.app.domain.model.GameSession
import com.shiori.app.domain.model.SyncStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class Tier1StaircaseDifficultyAdapterTest {
    private val adapter = Tier1StaircaseDifficultyAdapter()

    private fun createSession(accuracy: Float, level: Int): GameSession {
        val now = System.currentTimeMillis()
        return GameSession(
            id = "session-${System.nanoTime()}",
            gameType = "MEMORY_MATCH",
            startTimeMillis = now - 30000,
            endTimeMillis = now,
            accuracyPercent = accuracy,
            reactionLatencyMs = 800L,
            difficultyLevel = level,
            completed = true,
            syncStatus = SyncStatus.PENDING
        )
    }

    @Test
    fun testEmptyHistoryReturnsBaseLevel() = runBlocking {
        val next = adapter.calculateNextDifficulty(emptyList<GameSession>())
        assertEquals(1, next)
    }

    @Test
    fun testTwoConsecutiveHighScoresIncrementsDifficulty() = runBlocking {
        val history = listOf(
            createSession(85.0f, 2),
            createSession(90.0f, 2)
        )
        val next = adapter.calculateNextDifficulty(history)
        assertEquals(3, next)
    }

    @Test
    fun testLowScoreDecrementsDifficulty() = runBlocking {
        val history = listOf(
            createSession(40.0f, 3)
        )
        val next = adapter.calculateNextDifficulty(history)
        assertEquals(2, next)
    }

    @Test
    fun testDifficultyBoundedBetweenOneAndFive() = runBlocking {
        // Can't go below 1
        val lowHistory = listOf(createSession(20.0f, 1))
        assertEquals(1, adapter.calculateNextDifficulty(lowHistory))

        // Can't go above 5
        val maxHistory = listOf(createSession(95.0f, 5), createSession(95.0f, 5))
        assertEquals(5, adapter.calculateNextDifficulty(maxHistory))
    }

    @Test
    fun testNeutralScoreMaintainsDifficulty() = runBlocking {
        val history = listOf(createSession(70.0f, 3))
        assertEquals(3, adapter.calculateNextDifficulty(history))
    }
}
