package com.example.bouldertrack.domain.model

import kotlinx.datetime.LocalDate

/**
 * Zagregowana projekcja domenowa łącząca sesję [Session] ze statystykami podsumowującymi.
 * Zoptymalizowana pod kątem widoków listowych (zapobiega problemowi zapytań N+1 do bazy danych).
 *
 * @property id Identyfikator sesji wspinaczkowej.
 * @property date Data sesji.
 * @property location Nazwa ściany lub rejonu.
 * @property notes Notatki użytkownika dotyczące sesji.
 * @property totalBoulders Łączna liczba boulderów zalogowanych w tej sesji.
 * @property maxGrade Najwyższa pokonana lub próbowana wycena w danej sesji (null, jeśli brak).
 * @property flashCount Liczba boulderów pokonanych w stylu Flash (w 1. próbie).
 * @property topCount Liczba boulderów ukończonych w stylu Top (w 2. lub kolejnej próbie).
 * @property projectCount Liczba otwartych projektów (boulderów nieukończonych) w sesji.
 */
data class SessionWithStats(
    val id: Long,
    val date: LocalDate,
    val location: String,
    val notes: String?,
    val totalBoulders: Int,
    val maxGrade: String?,
    val flashCount: Int,
    val topCount: Int,
    val projectCount: Int
)