package com.example.bouldertrack.data

import com.example.bouldertrack.domain.model.BoulderProblem
import com.example.bouldertrack.domain.model.ProjectWithSession
import com.example.bouldertrack.domain.model.Session
import com.example.bouldertrack.domain.model.SessionWithStats
import com.example.bouldertrack.domain.repository.BoulderRepository
import com.example.bouldertrack.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

/**
 * Atrapa repozytorium boulderów (Fake) do deterministycznych testów jednostkowych.
 */
class FakeBoulderRepository : BoulderRepository {
    val bouldersFlow = MutableStateFlow<List<BoulderProblem>>(emptyList())
    val sessionsMap = mutableMapOf<Long, Pair<LocalDate, String>>()

    override fun getBouldersForSession(sessionId: Long): Flow<List<BoulderProblem>> {
        return bouldersFlow.map { list -> list.filter { it.sessionId == sessionId } }
    }

    override fun getAllBoulders(): Flow<List<BoulderProblem>> = bouldersFlow.asStateFlow()

    override suspend fun insertBoulder(boulder: BoulderProblem) {
        val newId = if (boulder.id == 0L) (bouldersFlow.value.maxOfOrNull { it.id } ?: 0L) + 1 else boulder.id
        bouldersFlow.value = bouldersFlow.value + boulder.copy(id = newId)
    }

    override suspend fun updateBoulder(boulder: BoulderProblem) {
        bouldersFlow.value = bouldersFlow.value.map {
            if (it.id == boulder.id) boulder else it
        }
    }

    override suspend fun deleteBoulder(id: Long) {
        bouldersFlow.value = bouldersFlow.value.filterNot { it.id == id }
    }

    override fun countBouldersByGrade(): Flow<Map<String, Long>> {
        return bouldersFlow.map { list -> list.groupingBy { it.grade.value }.eachCount().mapValues { it.value.toLong() } }
    }

    override fun countSentBouldersByGrade(): Flow<Map<String, Long>> {
        return bouldersFlow.map { list ->
            list.filter { it.style.name in listOf("FLASH", "TOP") }
                .groupingBy { it.grade.value }.eachCount().mapValues { it.value.toLong() }
        }
    }

    override fun countBouldersByStyle(): Flow<Map<String, Long>> {
        return bouldersFlow.map { list -> list.groupingBy { it.style.name }.eachCount().mapValues { it.value.toLong() } }
    }

    override fun getTotalAttempts(): Flow<Long> {
        return bouldersFlow.map { list -> list.sumOf { it.attempts }.toLong() }
    }

    override fun getActiveProjects(): Flow<List<BoulderProblem>> {
        return bouldersFlow.map { list -> list.filter { it.style.name == "PROJECT" } }
    }

    override fun getActiveProjectsWithSession(): Flow<List<ProjectWithSession>> {
        return bouldersFlow.map { list ->
            list.filter { it.style.name == "PROJECT" }.map { boulder ->
                val (date, location) = sessionsMap[boulder.sessionId] ?: (LocalDate(2026, 1, 1) to "Ściana")
                ProjectWithSession(
                    boulder = boulder,
                    sessionDate = date,
                    sessionLocation = location
                )
            }
        }
    }
}

/**
 * Atrapa repozytorium sesji (Fake) do testów jednostkowych.
 */
class FakeSessionRepository : SessionRepository {
    val sessionsFlow = MutableStateFlow<List<SessionWithStats>>(emptyList())
    private val sessionsList = mutableListOf<Session>()

    override fun getSessionsWithStats(): Flow<List<SessionWithStats>> = sessionsFlow.asStateFlow()

    override fun getAllSessions(): Flow<List<Session>> {
        return sessionsFlow.map { list ->
            list.map { Session(it.id, it.date, it.location, it.notes) }
        }
    }

    override fun getSessionById(id: Long): Flow<Session?> {
        return sessionsFlow.map { list ->
            val found = list.find { it.id == id }
            if (found != null) {
                Session(found.id, found.date, found.location, found.notes)
            } else null
        }
    }

    override suspend fun insertSession(session: Session): Long {
        val newId = (sessionsList.maxOfOrNull { it.id } ?: 0L) + 1
        val newSession = session.copy(id = newId)
        sessionsList.add(newSession)
        val newStats = SessionWithStats(
            id = newId,
            date = newSession.date,
            location = newSession.location,
            notes = newSession.notes,
            totalBoulders = 0,
            maxGrade = null,
            flashCount = 0,
            topCount = 0,
            projectCount = 0
        )
        sessionsFlow.value = listOf(newStats) + sessionsFlow.value
        return newId
    }

    override suspend fun updateSession(session: Session) {
        val index = sessionsList.indexOfFirst { it.id == session.id }
        if (index != -1) {
            sessionsList[index] = session
        }
        sessionsFlow.value = sessionsFlow.value.map {
            if (it.id == session.id) {
                it.copy(
                    date = session.date,
                    location = session.location,
                    notes = session.notes
                )
            } else it
        }
    }

    override suspend fun deleteSession(id: Long) {
        sessionsList.removeAll { it.id == id }
        sessionsFlow.value = sessionsFlow.value.filterNot { it.id == id }
    }
}
