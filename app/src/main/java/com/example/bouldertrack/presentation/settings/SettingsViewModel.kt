package com.example.bouldertrack.presentation.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.FileProvider
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bouldertrack.domain.manager.TimerManager
import com.example.bouldertrack.domain.model.BoulderProblem
import com.example.bouldertrack.domain.model.ClimbStyle
import com.example.bouldertrack.domain.model.Grade
import com.example.bouldertrack.domain.model.GradeScale
import com.example.bouldertrack.domain.model.Session
import com.example.bouldertrack.domain.model.dto.BoulderDto
import com.example.bouldertrack.domain.model.dto.SessionDto
import com.example.bouldertrack.domain.model.dto.UserProfileDto
import com.example.bouldertrack.domain.repository.BoulderRepository
import com.example.bouldertrack.domain.repository.PreferencesRepository
import com.example.bouldertrack.domain.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Reprezentacja stanu interfejsu (UI State) dla ekranu Ustawień i preferencji.
 */
data class SettingsUiState(
    val currentLanguage: String = "pl",
    val defaultGradeScale: GradeScale = GradeScale.FONT,
    val defaultRestTimeSeconds: Int = 180,
    val currentTheme: com.example.bouldertrack.domain.model.AppThemeMode = com.example.bouldertrack.domain.model.AppThemeMode.SYSTEM,
    val totalSessions: Int = 0,
    val totalBoulders: Int = 0,
    val isLoading: Boolean = false
)

/**
 * ViewModel zarządzający konfiguracją aplikacji, dynamiczną zmianą języka interfejsu,
 * motywem graficznym (Jasny/Ciemny/AMOLED), domyślną skalą wycen oraz eksportem i importem bazy treningów.
 */
class SettingsViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val sessionRepository: SessionRepository,
    private val boulderRepository: BoulderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            currentLanguage = preferencesRepository.getLanguage(),
            defaultGradeScale = preferencesRepository.getDefaultGradeScale(),
            defaultRestTimeSeconds = preferencesRepository.getDefaultRestTime(),
            currentTheme = preferencesRepository.getAppTheme()
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    /**
     * Pobiera statystyki liczbowe bazy (łączna liczba sesji i boulderów) dla sekcji "O aplikacji".
     */
    private fun loadStats() {
        viewModelScope.launch {
            val sessions = sessionRepository.getAllSessions().first()
            val boulders = boulderRepository.getAllBoulders().first()
            _uiState.update {
                it.copy(
                    totalSessions = sessions.size,
                    totalBoulders = boulders.size
                )
            }
        }
    }

    /**
     * Zmienia język interfejsu aplikacji i aplikuje lokalizację w locie za pomocą [AppCompatDelegate].
     */
    fun setLanguage(languageCode: String) {
        preferencesRepository.setLanguage(languageCode)
        val appLocale = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
        _uiState.update { it.copy(currentLanguage = languageCode) }
    }

    /**
     * Aktualizuje domyślną skalę wycen w pamięci aplikacji.
     */
    fun setDefaultGradeScale(scale: GradeScale) {
        preferencesRepository.setDefaultGradeScale(scale)
        _uiState.update { it.copy(defaultGradeScale = scale) }
    }

    /**
     * Aktualizuje domyślny czas odpoczynku między wstawkami i synchronizuje stoper w [TimerManager].
     */
    fun setDefaultRestTime(seconds: Int) {
        preferencesRepository.setDefaultRestTime(seconds)
        TimerManager.setDefaultTime(seconds)
        _uiState.update { it.copy(defaultRestTimeSeconds = seconds) }
    }

    /**
     * Zmienia wybrany motyw graficzny aplikacji (Systemowy, Jasny, Ciemny, AMOLED).
     */
    fun setTheme(theme: com.example.bouldertrack.domain.model.AppThemeMode) {
        preferencesRepository.setAppTheme(theme)
        _uiState.update { it.copy(currentTheme = theme) }
    }

    /**
     * Eksportuje wszystkie sesje i przejścia użytkownika do pliku JSON oraz wywołuje systemowe okno udostępniania.
     */
    fun exportData(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val sessions = sessionRepository.getAllSessions().first()
                val sessionDtos = sessions.map { session ->
                    val boulders = boulderRepository.getBouldersForSession(session.id).first()
                    SessionDto(
                        id = session.id,
                        date = session.date.toString(),
                        location = session.location,
                        notes = session.notes,
                        boulders = boulders.map { boulder ->
                            BoulderDto(
                                grade = boulder.grade.value,
                                style = boulder.style.name,
                                attempts = boulder.attempts,
                                tags = boulder.tags,
                                note = boulder.note,
                                photoUris = boulder.photoUris
                            )
                        }
                    )
                }

                val exportDto = UserProfileDto(
                    exportDate = Clock.System.now().toString(),
                    sessions = sessionDtos
                )

                val jsonString = Json.encodeToString(UserProfileDto.serializer(), exportDto)
                val file = File(context.cacheDir, "exports/bouldertrack_export.json")
                file.parentFile?.mkdirs()
                file.writeText(jsonString)

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooser = Intent.createChooser(intent, "Eksportuj profil BoulderTrack").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Importuje kopię zapasową JSON ze wskazanego pliku [Uri] i zapisuje dane w bazie SQLDelight.
     */
    @SuppressLint("Recycle")
    fun importData(context: Context, uri: Uri, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().use { it.readText() }
                } ?: run {
                    launch(Dispatchers.Main) { onError() }
                    return@launch
                }

                val dto = Json.decodeFromString(UserProfileDto.serializer(), jsonString)
                dto.sessions.forEach { sessionDto ->
                    val session = Session(
                        id = 0,
                        date = LocalDate.parse(sessionDto.date),
                        location = sessionDto.location,
                        notes = sessionDto.notes
                    )
                    val newSessionId = sessionRepository.insertSession(session)
                    sessionDto.boulders.forEach { boulderDto ->
                        boulderRepository.insertBoulder(
                            BoulderProblem(
                                sessionId = newSessionId,
                                grade = Grade(boulderDto.grade),
                                style = ClimbStyle.valueOf(boulderDto.style),
                                attempts = boulderDto.attempts,
                                tags = boulderDto.tags,
                                note = boulderDto.note,
                                photoUris = boulderDto.photoUris
                            )
                        )
                    }
                }
                loadStats()
                launch(Dispatchers.Main) { onSuccess() }
            } catch (e: Exception) {
                e.printStackTrace()
                launch(Dispatchers.Main) { onError() }
            }
        }
    }
}
