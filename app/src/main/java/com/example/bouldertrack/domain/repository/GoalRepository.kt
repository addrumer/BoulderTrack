package com.example.bouldertrack.domain.repository

import com.example.bouldertrack.database.Goal
import kotlinx.coroutines.flow.Flow

/**
 * Kontrakt repozytorium do zarządzania celami wspinaczkowymi użytkownika.
 */
interface GoalRepository {
    /** Zwraca reaktywny strumień wszystkich zdefiniowanych celów. */
    fun getAllGoals(): Flow<List<Goal>>

    /** Tworzy i zapisuje nowy cel wspinaczkowy. */
    suspend fun insertGoal(title: String, targetGrade: String?, targetCount: Long?, deadline: Long?)

    /** Aktualizuje status realizacji wskazanego celu. */
    suspend fun updateGoalStatus(id: Long, isCompleted: Boolean)

    /** Usuwa cel o wskazanym identyfikatorze [id]. */
    suspend fun deleteGoal(id: Long)
}
