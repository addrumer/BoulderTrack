package com.example.bouldertrack.data.mapper

import com.example.bouldertrack.database.ClimbingSession
import com.example.bouldertrack.domain.model.Session
import kotlinx.datetime.LocalDate

/**
 * Mapuje encję bazy danych SQLDelight ([ClimbingSession]) na model domenowy ([Session]).
 */
fun ClimbingSession.toDomain(): Session {
    return Session(
        id = this.id,
        date = LocalDate.parse(this.date),
        location = this.location,
        notes = this.notes
    )
}

/**
 * Mapuje model domenowy ([Session]) na encję bazy danych SQLDelight ([ClimbingSession]).
 */
fun Session.toDatabase(): ClimbingSession {
    return ClimbingSession(
        id = this.id,
        date = this.date.toString(),
        location = this.location,
        notes = this.notes
    )
}