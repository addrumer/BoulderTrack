package com.example.bouldertrack.domain.model

/**
 * Przedziały czasowe do filtrowania analityki na ekranie statystyk.
 */
enum class StatsTimeRange {
    /** Wszystkie zapisane sesje w historii. */
    ALL_TIME,
    /** Ostatnie 30 dni. */
    LAST_30_DAYS,
    /** Ostatnie 90 dni (3 miesiące). */
    LAST_90_DAYS,
    /** Aktualny rok kalendarzowy. */
    THIS_YEAR
}
