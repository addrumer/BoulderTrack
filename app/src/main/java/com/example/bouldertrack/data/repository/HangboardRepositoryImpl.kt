package com.example.bouldertrack.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.bouldertrack.database.AppDatabase
import com.example.bouldertrack.database.HangboardSession
import com.example.bouldertrack.domain.repository.HangboardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Implementacja repozytorium [HangboardRepository] rejestrująca sesje hangboardowe w bazie SQLDelight.
 */
class HangboardRepositoryImpl(
    db: AppDatabase
) : HangboardRepository {
    private val queries = db.boulderTrackQueries

    override fun getAllHangboardSessions(): Flow<List<HangboardSession>> {
        return queries.getAllHangboardSessions()
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    override suspend fun insertHangboardSession(date: Long, protocolName: String, totalTime: Long, note: String?) {
        withContext(Dispatchers.IO) {
            queries.insertHangboardSession(
                date = date,
                protocolName = protocolName,
                totalTime = totalTime,
                note = note
            )
        }
    }

    override suspend fun deleteHangboardSession(id: Long) {
        withContext(Dispatchers.IO) {
            queries.deleteHangboardSession(id)
        }
    }
}

