package com.example.bouldertrack.domain.model

/**
 * Skale wyceniania trudności boulderów obsługiwane w aplikacji.
 */
enum class GradeScale {
    /** Francuska skala Fontainebleau (np. 6A, 7B+). */
    FONT,

    /** Północnoamerykańska skala Hueco (np. V3, V10). */
    V_SCALE,

    /** Obwodowa skala kolorystyczna stosowana na sztucznych ścianach komercyjnych (np. Żółty, Zielony, Czerwony). */
    COLORS,

    /** Skala numeryczna trudności od 1 do 10 (stosowana na ścianach wspinaczkowych). */
    SCALE_1_10
}
