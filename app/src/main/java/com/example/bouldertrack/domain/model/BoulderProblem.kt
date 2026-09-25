package com.example.bouldertrack.domain.model

/**
 * Model domenowy reprezentujący pojedynczy problem boulderowy (próbę lub udane przejście).
 *
 * @property id Unikalny identyfikator w bazie danych (wartość 0 oznacza nowy, niezapisany obiekt).
 * @property sessionId Identyfikator sesji treningowej [Session], do której przypisany jest boulder.
 * @property grade Wycena trudności opakowana w typ [Grade].
 * @property style Styl/rezultat przejścia ([ClimbStyle.FLASH], [ClimbStyle.TOP], [ClimbStyle.PROJECT]).
 * @property attempts Łączna liczba prób wykonanych na danym boulderze.
 * @property tags Charakterystyka chwytów i formacji (np. "Krawądki", "Oblaki", "Dach").
 * @property note Opcjonalna notatka o patentach, układzie rąk lub warunkach.
 * @property photoUris Lista lokalnych identyfikatorów URI (zdjęcia lub filmy) dokumentujących przejście / patent.
 */
data class BoulderProblem(
    val id: Long = 0,
    val sessionId: Long,
    val grade: Grade,
    val style: ClimbStyle,
    val attempts: Int,
    val tags: List<String> = emptyList(),
    val note: String? = null,
    val photoUris: List<String> = emptyList()
)