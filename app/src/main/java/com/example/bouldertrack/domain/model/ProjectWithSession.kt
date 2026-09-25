package com.example.bouldertrack.domain.model

import kotlinx.datetime.LocalDate

/**
 * Model domenowy reprezentujący aktywny projekt boulderowy powiązany z danymi sesji wspinaczkowej.
 *
 * @property boulder Dane konkretnego problemu boulderowego.
 * @property sessionDate Data sesji, na której rozpoczęto projekt.
 * @property sessionLocation Nazwa ścianki / rejonu, gdzie znajduje się projekt.
 */
data class ProjectWithSession(
    val boulder: BoulderProblem,
    val sessionDate: LocalDate,
    val sessionLocation: String
)
