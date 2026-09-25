package com.example.bouldertrack.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.bouldertrack.database.AppDatabase
import com.example.bouldertrack.database.Goal
import com.example.bouldertrack.domain.repository.GoalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

/**
 * Implementacja repozytorium [GoalRepository] zarządzająca celami wspinaczkowymi w bazie SQLDelight.
 */
class GoalRepositoryImpl(
    db: AppDatabase
) : GoalRepository {
    private val queries = db.boulderTrackQueries

    override fun getAllGoals(): Flow<List<Goal>> {
        return queries.getAllGoals()
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    override suspend fun insertGoal(title: String, targetGrade: String?, targetCount: Long?, deadline: Long?) {
        withContext(Dispatchers.IO) {
            queries.insertGoal(
                title = title,
                targetGrade = targetGrade,
                targetCount = targetCount,
                deadline = deadline,
                createdAt = Clock.System.now().toEpochMilliseconds()
            )
        }
    }

    override suspend fun updateGoalStatus(id: Long, isCompleted: Boolean) {
        withContext(Dispatchers.IO) {
            queries.updateGoalStatus(
                isCompleted = if (isCompleted) 1L else 0L,
                id = id
            )
        }
    }

    override suspend fun deleteGoal(id: Long) {
        withContext(Dispatchers.IO) {
            queries.deleteGoal(id)
        }
    }
}

