package com.example.bouldertrack.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.bouldertrack.domain.model.GradeScale
import com.example.bouldertrack.domain.repository.PreferencesRepository

/**
 * Implementacja repozytorium [PreferencesRepository] oparta na mechanizmie [SharedPreferences] systemu Android.
 */
class PreferencesRepositoryImpl(context: Context) : PreferencesRepository {
    private val prefs: SharedPreferences = context.getSharedPreferences("bouldertrack_prefs", Context.MODE_PRIVATE)

    override fun getDefaultGradeScale(): GradeScale {
        val saved = prefs.getString("default_grade_scale", GradeScale.FONT.name) ?: GradeScale.FONT.name
        return try {
            GradeScale.valueOf(saved)
        } catch (e: Exception) {
            GradeScale.FONT
        }
    }

    override fun setDefaultGradeScale(scale: GradeScale) {
        prefs.edit().putString("default_grade_scale", scale.name).apply()
    }

    override fun getLanguage(): String {
        return prefs.getString("app_language", "pl") ?: "pl"
    }

    override fun setLanguage(language: String) {
        prefs.edit().putString("app_language", language).apply()
    }

    override fun getDefaultRestTime(): Int {
        return prefs.getInt("default_rest_time", 180)
    }

    override fun setDefaultRestTime(seconds: Int) {
        prefs.edit().putInt("default_rest_time", seconds).apply()
    }

    private val _appThemeFlow = kotlinx.coroutines.flow.MutableStateFlow(getAppTheme())
    override val appThemeFlow: kotlinx.coroutines.flow.Flow<com.example.bouldertrack.domain.model.AppThemeMode> = _appThemeFlow

    override fun getAppTheme(): com.example.bouldertrack.domain.model.AppThemeMode {
        val saved = prefs.getString("app_theme", com.example.bouldertrack.domain.model.AppThemeMode.SYSTEM.name)
            ?: com.example.bouldertrack.domain.model.AppThemeMode.SYSTEM.name
        return try {
            com.example.bouldertrack.domain.model.AppThemeMode.valueOf(saved)
        } catch (_: Exception) {
            com.example.bouldertrack.domain.model.AppThemeMode.SYSTEM
        }
    }

    override fun setAppTheme(theme: com.example.bouldertrack.domain.model.AppThemeMode) {
        prefs.edit().putString("app_theme", theme.name).apply()
        _appThemeFlow.value = theme
    }
}
