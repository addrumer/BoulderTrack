package com.example.bouldertrack.domain.repository

import com.example.bouldertrack.database.HangboardSession
import kotlinx.coroutines.flow.Flow

/**
 * Kontrakt repozytorium do rejestrowania i odczytu sesji treningowych na chwytotablicy (hangboard).
 */
interface HangboardRepository {
    /** Zwraca reaktywny strumień wszystkich zarejestrowanych sesji hangboardowych. */
    fun getAllHangboardSessions(): Flow<List<HangboardSession>>

    /** Zapisuje nową sesję treningową na chwytotablicy. */
    suspend fun insertHangboardSession(date: Long, protocolName: String, totalTime: Long, note: String?)

    /** Usuwa sesję treningową na chwytotablicy na podstawie identyfikatora [id]. */
    suspend fun deleteHangboardSession(id: Long)
}
