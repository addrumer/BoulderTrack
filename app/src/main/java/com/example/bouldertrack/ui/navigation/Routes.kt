package com.example.bouldertrack.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Trasa nawigacji do ekranu głównego (Dashboard / Lista sesji).
 */
@Serializable
object SessionList

/**
 * Trasa nawigacji do ekranu szczegółów konkretnej sesji wspinaczkowej o identyfikatorze [sessionId].
 */
@Serializable
data class SessionDetail(val sessionId: Long)

/**
 * Trasa nawigacji do ekranu statystyk i analityki wspinaczkowej.
 */
@Serializable
object Stats

/**
 * Trasa nawigacji do biblioteki patentów (multimediów).
 */
@Serializable
object BetaLibrary

/**
 * Trasa nawigacji do ekranu projektów wspinaczkowych (Project Book).
 */
@Serializable
object Projects

/**
 * Trasa nawigacji do ekranu ustawień i konfiguracji aplikacji.
 */
@Serializable
object Settings