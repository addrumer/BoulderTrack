package com.example.bouldertrack.domain.repository

import com.example.bouldertrack.domain.model.BoulderProblem
import kotlinx.coroutines.flow.Flow

/**
 * Kontrakt repozytorium domenowego definiujący operacje utrwalania danych i zapytania statystyczne dla encji [BoulderProblem].
 */
interface BoulderRepository {

    /** Zwraca reaktywny strumień boulderów przypisanych do danej sesji ([sessionId]). */
    fun getBouldersForSession(sessionId: Long): Flow<List<BoulderProblem>>

    /** Zwraca reaktywny strumień wszystkich zapisanych boulderów ze wszystkich sesji. */
    fun getAllBoulders(): Flow<List<BoulderProblem>>

    /** Zapisuje nowy problem boulderowy w trwałej pamięci. */
    suspend fun insertBoulder(boulder: BoulderProblem)

    /** Aktualizuje istniejący rekord bouldera w bazie danych. */
    suspend fun updateBoulder(boulder: BoulderProblem)

    /** Usuwa boulder z bazy danych na podstawie unikalnego identyfikatora [id]. */
    suspend fun deleteBoulder(id: Long)

    /** Zwraca reaktywny strumień z liczbą boulderów zagregowaną według wyceny. */
    fun countBouldersByGrade(): Flow<Map<String, Long>>

    /** Zwraca reaktywny strumień z liczbą ukończonych (Flash/Top) boulderów według wyceny. */
    fun countSentBouldersByGrade(): Flow<Map<String, Long>>

    /** Zwraca reaktywny strumień z liczbą boulderów pogrupowaną według stylu (FLASH, TOP, PROJECT). */
    fun countBouldersByStyle(): Flow<Map<String, Long>>

    /** Zwraca reaktywny strumień sumujący wszystkie próby (wstawki) wykonane przez użytkownika. */
    fun getTotalAttempts(): Flow<Long>

    /** Zwraca reaktywny strumień aktualnie otwartych projektów (styl == PROJECT). */
    fun getActiveProjects(): Flow<List<BoulderProblem>>

    /** Zwraca reaktywny strumień otwartych projektów z dołączoną informacją o sesji i lokalizacji. */
    fun getActiveProjectsWithSession(): Flow<List<com.example.bouldertrack.domain.model.ProjectWithSession>>
}
