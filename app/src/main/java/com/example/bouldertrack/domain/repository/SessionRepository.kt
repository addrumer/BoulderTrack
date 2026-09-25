package com.example.bouldertrack.domain.repository

import com.example.bouldertrack.domain.model.Session
import com.example.bouldertrack.domain.model.SessionWithStats
import kotlinx.coroutines.flow.Flow

/**
 * Kontrakt repozytorium domenowego zarządzający operacjami na sesjach wspinaczkowych [Session].
 */
interface SessionRepository {

    /** Zwraca reaktywny strumień wszystkich sesji posortowanych chronologicznie malejąco. */
    fun getAllSessions(): Flow<List<Session>>

    /** Zwraca reaktywny strumień sesji wzbogaconych o zagregowane statystyki (liczba topów, max wycena). */
    fun getSessionsWithStats(): Flow<List<SessionWithStats>>

    /** Zwraca reaktywny strumień dla konkretnej sesji o wskazanym [id] (lub null, jeśli nie istnieje). */
    fun getSessionById(id: Long): Flow<Session?>

    /** Zapisuje nową sesję i zwraca wygenerowany identyfikator rekordu w bazie danych. */
    suspend fun insertSession(session: Session): Long

    /** Aktualizuje istniejący rekord sesji w bazie danych. */
    suspend fun updateSession(session: Session)

    /** Usuwa sesję o wskazanym [id] wraz z powiązanymi z nią boulderami. */
    suspend fun deleteSession(id: Long)
}