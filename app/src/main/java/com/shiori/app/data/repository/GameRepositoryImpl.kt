package com.shiori.app.data.repository

import com.shiori.app.data.local.dao.GameSessionDao
import com.shiori.app.data.local.entity.GameSessionEntity
import com.shiori.app.domain.model.GameSession
import com.shiori.app.domain.repository.IGameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepositoryImpl @Inject constructor(
    private val dao: GameSessionDao
) : IGameRepository {
    override fun observeGameSessions(): Flow<List<GameSession>> {
        return dao.observeSessions().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertGameSession(session: GameSession) {
        dao.insertSession(session.toEntity())
    }

    private fun GameSessionEntity.toDomain() = GameSession(
        id = id,
        gameType = gameType,
        startTimeMillis = startTimeMillis,
        endTimeMillis = endTimeMillis,
        accuracyPercent = accuracyPercent,
        reactionLatencyMs = reactionLatencyMs,
        difficultyLevel = difficultyLevel,
        completed = completed,
        updatedAt = updatedAt,
        syncStatus = syncStatus
    )

    private fun GameSession.toEntity() = GameSessionEntity(
        id = id,
        gameType = gameType,
        startTimeMillis = startTimeMillis,
        endTimeMillis = endTimeMillis,
        accuracyPercent = accuracyPercent,
        reactionLatencyMs = reactionLatencyMs,
        difficultyLevel = difficultyLevel,
        completed = completed,
        updatedAt = updatedAt,
        syncStatus = syncStatus
    )
}
