package com.example.bouldertrack.domain.model

import kotlinx.datetime.LocalDate

/**
 * Model domenowy reprezentujący sesję wspinaczkową (trening na ścianie lub wyjazd w skały).
 *
 * @property id Unikalny identyfikator w bazie danych (0 oznacza nowy rekord).
 * @property date Data przeprowadzenia sesji.
 * @property location Nazwa ściany wspinaczkowej, rejonu lub sektora.
 * @property notes Opcjonalne notatki podsumowujące sesję, samopoczucie czy partnerów wspinaczkowych.
 */
data class Session(
    val id: Long = 0,
    val date: LocalDate,
    val location: String,
    val notes: String? = null
)