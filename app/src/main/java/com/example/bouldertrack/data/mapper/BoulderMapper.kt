package com.example.bouldertrack.data.mapper

import com.example.bouldertrack.database.BoulderProblem as DbBoulderProblem
import com.example.bouldertrack.domain.model.BoulderProblem
import com.example.bouldertrack.domain.model.ClimbStyle
import com.example.bouldertrack.domain.model.Grade

/**
 * Mapuje encję bazy danych SQLDelight ([DbBoulderProblem]) na model domenowy ([BoulderProblem]).
 */
fun DbBoulderProblem.toDomain(): BoulderProblem {
    return BoulderProblem(
        id = this.id,
        sessionId = this.sessionId,
        grade = Grade(this.grade),
        style = ClimbStyle.valueOf(this.style),
        attempts = this.attempts.toInt(),
        tags = if (this.tags.isBlank()) emptyList() else this.tags.split(","),
        note = this.note,
        photoUris = if (this.photoUri.isNullOrBlank()) emptyList() else this.photoUri.split(",")
    )
}

/**
 * Mapuje model domenowy ([BoulderProblem]) na encję bazy danych SQLDelight ([DbBoulderProblem]).
 */
fun BoulderProblem.toDatabase(): DbBoulderProblem {
    return DbBoulderProblem(
        id = this.id,
        sessionId = this.sessionId,
        grade = this.grade.value,
        style = this.style.name,
        attempts = this.attempts.toLong(),
        tags = this.tags.joinToString(","),
        note = this.note,
        photoUri = if (this.photoUris.isEmpty()) null else this.photoUris.joinToString(",")
    )
}
