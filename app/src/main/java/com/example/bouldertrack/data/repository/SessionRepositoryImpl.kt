package com.example.bouldertrack.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.example.bouldertrack.data.mapper.toDatabase
import com.example.bouldertrack.data.mapper.toDomain
import com.example.bouldertrack.database.AppDatabase
import com.example.bouldertrack.domain.model.Session
import com.example.bouldertrack.domain.model.SessionWithStats
import com.example.bouldertrack.domain.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

/**
 * Implementacja repozytorium [SessionRepository] oparta na bazie danych SQLDelight i strumieniach Kotlin Coroutines Flow.
 */
class SessionRepositoryImpl(
    private val database: AppDatabase
) : SessionRepository {
    private val queries = database.boulderTrackQueries

    override fun getAllSessions(): Flow<List<Session>> {
        return queries.getAllSessions()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }
    }

    override fun getSessionsWithStats(): Flow<List<SessionWithStats>> {
        return queries.getSessionsWithStats()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list ->
                list.map {
                    SessionWithStats(
                        id = it.id,
                        date = LocalDate.parse(it.date),
                        location = it.location,
                        notes = it.notes,
                        totalBoulders = it.totalBoulders.toInt(),
                        maxGrade = it.maxGrade,
                        flashCount = it.flashCount.toInt(),
                        topCount = it.topCount.toInt(),
                        projectCount = it.projectCount.toInt()
                    )
                }
            }
    }

    override fun getSessionById(id: Long): Flow<Session?> {
        return queries.getSessionById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain() }
    }

    override suspend fun insertSession(session: Session): Long {
        return withContext(Dispatchers.IO) {
            val dbSession = session.toDatabase()
            queries.insertSession(
                date = dbSession.date,
                location = dbSession.location,
                notes = dbSession.notes
            )
            queries.getLastInsertRowId().executeAsOne()
        }
    }

    override suspend fun updateSession(session: Session) {
        withContext(Dispatchers.IO) {
            val dbSession = session.toDatabase()
            queries.updateSession(
                date = dbSession.date,
                location = dbSession.location,
                notes = dbSession.notes,
                id = session.id
            )
        }
    }

    override suspend fun deleteSession(id: Long) {
        withContext(Dispatchers.IO) {
            queries.deleteSession(id)
        }
    }
}
