package com.example.bouldertrack.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrDefault
import com.example.bouldertrack.data.mapper.toDatabase
import com.example.bouldertrack.data.mapper.toDomain
import com.example.bouldertrack.database.AppDatabase
import com.example.bouldertrack.domain.model.BoulderProblem
import com.example.bouldertrack.domain.repository.BoulderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Implementacja repozytorium [BoulderRepository] realizująca zapytania bazy danych SQLDelight.
 */
class BoulderRepositoryImpl(
    private val database: AppDatabase
) : BoulderRepository {
    private val queries = database.boulderTrackQueries

    override fun getBouldersForSession(sessionId: Long): Flow<List<BoulderProblem>> {
        return queries.getBouldersForSession(sessionId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }
    }

    override fun getAllBoulders(): Flow<List<BoulderProblem>> {
        return queries.getAllBoulders()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertBoulder(boulder: BoulderProblem) {
        withContext(Dispatchers.IO) {
            val dbBoulder = boulder.toDatabase()
            queries.insertBoulder(
                sessionId = dbBoulder.sessionId,
                grade = dbBoulder.grade,
                style = dbBoulder.style,
                attempts = dbBoulder.attempts,
                tags = dbBoulder.tags,
                note = dbBoulder.note,
                photoUri = dbBoulder.photoUri
            )
        }
    }

    override suspend fun updateBoulder(boulder: BoulderProblem) {
        withContext(Dispatchers.IO) {
            val dbBoulder = boulder.toDatabase()
            queries.updateBoulder(
                grade = dbBoulder.grade,
                style = dbBoulder.style,
                attempts = dbBoulder.attempts,
                tags = dbBoulder.tags,
                note = dbBoulder.note,
                photoUri = dbBoulder.photoUri,
                id = dbBoulder.id
            )
        }
    }

    override suspend fun deleteBoulder(id: Long) {
        withContext(Dispatchers.IO) {
            queries.deleteBoulder(id)
        }
    }

    override fun countBouldersByGrade(): Flow<Map<String, Long>> {
        return queries.countBouldersByGrade()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list ->
                list.associate { it.grade to it.count }
            }
    }

    override fun countSentBouldersByGrade(): Flow<Map<String, Long>> {
        return queries.countSentBouldersByGrade()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list ->
                list.associate { it.grade to it.count }
            }
    }

    override fun countBouldersByStyle(): Flow<Map<String, Long>> {
        return queries.countBouldersByStyle()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list ->
                list.associate { it.style to it.count }
            }
    }

    override fun getTotalAttempts(): Flow<Long> {
        return queries.getTotalAttempts { SUM -> SUM ?: 0L }
            .asFlow()
            .mapToOneOrDefault(0L, Dispatchers.IO)
    }

    override fun getActiveProjects(): Flow<List<BoulderProblem>> {
        return queries.getActiveProjects()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }
    }

    override fun getActiveProjectsWithSession(): Flow<List<com.example.bouldertrack.domain.model.ProjectWithSession>> {
        return queries.getActiveProjectsWithSession()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list ->
                list.map { row ->
                    com.example.bouldertrack.domain.model.ProjectWithSession(
                        boulder = BoulderProblem(
                            id = row.id,
                            sessionId = row.sessionId,
                            grade = com.example.bouldertrack.domain.model.Grade(row.grade),
                            style = com.example.bouldertrack.domain.model.ClimbStyle.valueOf(row.style),
                            attempts = row.attempts.toInt(),
                            tags = if (row.tags.isBlank()) emptyList() else row.tags.split(","),
                            note = row.note,
                            photoUris = if (row.photoUri.isNullOrBlank()) emptyList() else row.photoUri.split(",")
                        ),
                        sessionDate = kotlinx.datetime.LocalDate.parse(row.sessionDate),
                        sessionLocation = row.sessionLocation
                    )
                }
            }
    }
}

