package com.shiori.app.ui.patient

import com.shiori.app.domain.model.GameSession
import com.shiori.app.domain.model.SyncStatus
import com.shiori.app.domain.repository.IGameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class GameSessionRecordingTest {
    private val sessions = mutableListOf<GameSession>()
    private val fakeRepo = object : IGameRepository {
        override fun observeGameSessions(): Flow<List<GameSession>> = flowOf(sessions)
        override suspend fun insertGameSession(session: GameSession) {
            sessions.add(session)
        }
    }

    @Test
    fun testRecordSessionSavesCorrectMetrics() = runBlocking {
        val now = System.currentTimeMillis()
        val session = GameSession(
            id = "test-session-1",
            gameType = "MEMORY_MATCH",
            startTimeMillis = now - 45000,
            endTimeMillis = now,
            accuracyPercent = 85.0f,
            reactionLatencyMs = 1200L,
            difficultyLevel = 2,
            completed = true,
            syncStatus = SyncStatus.PENDING
        )
        fakeRepo.insertGameSession(session)
        assertEquals(1, sessions.size)
        assertEquals("MEMORY_MATCH", sessions[0].gameType)
        assertEquals(85.0f, sessions[0].accuracyPercent, 0.01f)
        assertEquals(2, sessions[0].difficultyLevel)
    }
}
