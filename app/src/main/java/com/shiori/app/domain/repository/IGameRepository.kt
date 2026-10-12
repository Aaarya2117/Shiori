package com.shiori.app.domain.repository

import com.shiori.app.domain.model.GameSession
import kotlinx.coroutines.flow.Flow

interface IGameRepository {
    fun observeGameSessions(): Flow<List<GameSession>>
    suspend fun insertGameSession(session: GameSession)
}
