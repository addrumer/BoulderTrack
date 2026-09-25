package com.example.bouldertrack.domain.repository

import com.example.bouldertrack.domain.model.GradeScale

/**
 * Kontrakt repozytorium domenowego zarządzający preferencjami użytkownika i ustawieniami domyślnymi.
 */
interface PreferencesRepository {

    /** Zwraca preferowaną domyślną skalę wycen (np. FONT, V_SCALE, COLORS). */
    fun getDefaultGradeScale(): GradeScale

    /** Zapisuje wybraną domyślną skalę wycen. */
    fun setDefaultGradeScale(scale: GradeScale)

    /** Zwraca aktualny kod języka interfejsu aplikacji ("pl" lub "en"). */
    fun getLanguage(): String

    /** Zapisuje preferowany język interfejsu aplikacji. */
    fun setLanguage(language: String)

    /** Zwraca domyślny czas odpoczynku na stoperze między wstawkami (w sekundach). */
    fun getDefaultRestTime(): Int

    /** Zapisuje domyślny czas odpoczynku na stoperze (w sekundach). */
    fun setDefaultRestTime(seconds: Int)

    /** Zwraca aktualnie wybrany tryb motywu (SYSTEM, LIGHT, DARK, AMOLED). */
    fun getAppTheme(): com.example.bouldertrack.domain.model.AppThemeMode

    /** Zapisuje wybrany tryb motywu aplikacji. */
    fun setAppTheme(theme: com.example.bouldertrack.domain.model.AppThemeMode)

    /** Reaktywny strumień aktualnego trybu motywu aplikacji. */
    val appThemeFlow: kotlinx.coroutines.flow.Flow<com.example.bouldertrack.domain.model.AppThemeMode>
}
