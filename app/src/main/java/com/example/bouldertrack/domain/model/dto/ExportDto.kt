package com.example.bouldertrack.domain.model.dto

import kotlinx.serialization.Serializable

/**
 * Obiekt transferu danych (DTO) reprezentujący problem boulderowy w formacie JSON kopii zapasowej.
 */
@Serializable
data class BoulderDto(
    val grade: String,
    val style: String,
    val attempts: Int,
    val tags: List<String> = emptyList(),
    val note: String? = null,
    val photoUris: List<String> = emptyList()
)

/**
 * Obiekt transferu danych (DTO) reprezentujący sesję wspinaczkową i jej bouldery.
 */
@Serializable
data class SessionDto(
    val id: Long,
    val date: String,
    val location: String,
    val notes: String? = null,
    val boulders: List<BoulderDto> = emptyList()
)

/**
 * Główny kontener danych JSON dla pełnego profilu i historii treningów użytkownika.
 *
 * @property exportDate Znacznik czasu ISO-8601 utworzenia kopii zapasowej.
 * @property sessions Pełna historia zapisanych sesji wspinaczkowych.
 */
@Serializable
data class UserProfileDto(
    val exportDate: String,
    val sessions: List<SessionDto>
)
