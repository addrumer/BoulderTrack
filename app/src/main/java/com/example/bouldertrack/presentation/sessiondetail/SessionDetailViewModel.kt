package com.example.bouldertrack.presentation.sessiondetail

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bouldertrack.domain.model.BoulderProblem
import com.example.bouldertrack.domain.model.ClimbStyle
import com.example.bouldertrack.domain.model.Grade
import com.example.bouldertrack.domain.model.GradeScale
import com.example.bouldertrack.domain.model.Session
import com.example.bouldertrack.domain.repository.BoulderRepository
import com.example.bouldertrack.domain.repository.PreferencesRepository
import com.example.bouldertrack.domain.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import java.io.File
import java.util.UUID

/**
 * Reprezentacja stanu interfejsu (UI State) dla ekranu szczegółów sesji.
 *
 * @property session Dane wybranej sesji wspinaczkowej (lub null podczas ładowania).
 * @property boulders Przefiltrowana lista boulderów pasująca do wybranych filtrów stylu i tagów.
 * @property flashCount Liczba przejść w stylu Flash w tej sesji.
 * @property topCount Liczba przejść w stylu Top w tej sesji.
 * @property projectCount Liczba nierozwiązanych projektów w tej sesji.
 * @property selectedStyleFilter Aktualny filtr stylu przejścia (null oznacza wszystkie style).
 * @property selectedTagFilter Aktualny filtr tagów chwytów/formacji (null oznacza wszystkie tagi).
 * @property allGyms Lista unikalnych nazw ścianek do podpowiedzi autouzupełniania.
 * @property isLoading Flaga informująca o trwającym początkowym pobieraniu danych sesji.
 */
data class SessionDetailUiState(
    val session: Session? = null,
    val boulders: List<BoulderProblem> = emptyList(),
    val flashCount: Int = 0,
    val topCount: Int = 0,
    val projectCount: Int = 0,
    val selectedStyleFilter: ClimbStyle? = null,
    val selectedTagFilter: String? = null,
    val defaultGradeScale: GradeScale = GradeScale.FONT,
    val allGyms: List<String> = emptyList(),
    val isLoading: Boolean = true
)

/**
 * ViewModel zarządzający szczegółami konkretnej sesji, logowaniem boulderów,
 * filtrowaniem oraz bezpiecznym zapisem multimediów.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SessionDetailViewModel(
    private val sessionRepository: SessionRepository,
    private val boulderRepository: BoulderRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val sessionIdFlow = MutableStateFlow<Long?>(null)
    private val styleFilterFlow = MutableStateFlow<ClimbStyle?>(null)
    private val tagFilterFlow = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SessionDetailUiState> = sessionIdFlow
        .filterNotNull()
        .flatMapLatest { id ->
            combine(
                sessionRepository.getSessionById(id),
                boulderRepository.getBouldersForSession(id),
                sessionRepository.getAllSessions(),
                styleFilterFlow,
                tagFilterFlow
            ) { session, allBoulders, allSessions, styleFilter, tagFilter ->
                val filteredBoulders = allBoulders.filter { boulder ->
                    val matchesStyle = styleFilter == null || boulder.style == styleFilter
                    val matchesTag = tagFilter == null || boulder.tags.contains(tagFilter)
                    matchesStyle && matchesTag
                }
                val gyms = allSessions.map { it.location }.distinct().filter { it.isNotBlank() }
                SessionDetailUiState(
                    session = session,
                    boulders = filteredBoulders,
                    flashCount = allBoulders.count { it.style == ClimbStyle.FLASH },
                    topCount = allBoulders.count { it.style == ClimbStyle.TOP },
                    projectCount = allBoulders.count { it.style == ClimbStyle.PROJECT },
                    selectedStyleFilter = styleFilter,
                    selectedTagFilter = tagFilter,
                    defaultGradeScale = preferencesRepository.getDefaultGradeScale(),
                    allGyms = gyms,
                    isLoading = false
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SessionDetailUiState(isLoading = true)
        )

    /**
     * Inicjalizuje obserwację sesji o zadanym identyfikatorze [sessionId].
     */
    fun loadSession(sessionId: Long) {
        sessionIdFlow.value = sessionId
    }

    /**
     * Ustawia lub usuwa filtr stylu przejścia ([ClimbStyle]).
     */
    fun setStyleFilter(style: ClimbStyle?) {
        styleFilterFlow.value = style
    }

    /**
     * Ustawia lub usuwa filtr według tagu formacji/chwytów.
     */
    fun setTagFilter(tag: String?) {
        tagFilterFlow.value = tag
    }

    /**
     * Dodaje nowy problem boulderowy do bieżącej sesji.
     * Wymusza reguły domenowe dotyczące minimalnej liczby prób dla poszczególnych stylów.
     */
    fun addBoulder(
        context: Context,
        grade: String,
        style: ClimbStyle,
        attempts: Int,
        tags: List<String>,
        note: String?,
        photoUris: List<String> = emptyList()
    ) {
        val id = sessionIdFlow.value ?: return

        // Reguła domenowa: Flash to zawsze dokładnie 1 próba, Top min. 2, Projekt min. 1
        val finalAttempts = when (style) {
            ClimbStyle.FLASH -> 1
            ClimbStyle.TOP -> maxOf(2, attempts)
            ClimbStyle.PROJECT -> maxOf(1, attempts)
        }

        viewModelScope.launch(Dispatchers.IO) {
            val localUris = persistMediaUris(context, photoUris)

            boulderRepository.insertBoulder(
                BoulderProblem(
                    sessionId = id,
                    grade = Grade(grade),
                    style = style,
                    attempts = finalAttempts,
                    tags = tags,
                    note = note?.takeIf { it.isNotBlank() },
                    photoUris = localUris
                )
            )
        }
    }

    /**
     * Aktualizuje parametry zapisanego bouldera oraz utrwala ewentualne nowe zdjęcia/wideo.
     */
    fun updateBoulder(
        context: Context,
        boulderId: Long,
        grade: String,
        style: ClimbStyle,
        attempts: Int,
        tags: List<String>,
        note: String?,
        photoUris: List<String> = emptyList()
    ) {
        val id = sessionIdFlow.value ?: return

        val finalAttempts = when (style) {
            ClimbStyle.FLASH -> 1
            ClimbStyle.TOP -> maxOf(2, attempts)
            ClimbStyle.PROJECT -> maxOf(1, attempts)
        }

        viewModelScope.launch(Dispatchers.IO) {
            val localUris = persistMediaUris(context, photoUris)

            boulderRepository.updateBoulder(
                BoulderProblem(
                    id = boulderId,
                    sessionId = id,
                    grade = Grade(grade),
                    style = style,
                    attempts = finalAttempts,
                    tags = tags,
                    note = note?.takeIf { it.isNotBlank() },
                    photoUris = localUris
                )
            )
        }
    }

    /**
     * Usuwa problem boulderowy na podstawie identyfikatora.
     */
    fun deleteBoulder(boulderId: Long) {
        viewModelScope.launch {
            boulderRepository.deleteBoulder(boulderId)
        }
    }

    /**
     * Aktualizuje parametry sesji (nazwę lokalizacji, datę, notatki).
     */
    fun updateSession(location: String, date: LocalDate, notes: String?) {
        val currentSession = uiState.value.session ?: return
        viewModelScope.launch(Dispatchers.IO) {
            sessionRepository.updateSession(
                currentSession.copy(
                    location = location.trim().ifBlank { "Trening" },
                    date = date,
                    notes = notes?.trim()?.takeIf { it.isNotBlank() }
                )
            )
        }
    }

    /** Zwraca bieżącą domyślną skalę wycen z preferencji użytkownika. */
    fun getDefaultGradeScale(): GradeScale {
        return preferencesRepository.getDefaultGradeScale()
    }

    /** Zapisuje preferowaną skalę wycen w pamięci podręcznej. */
    fun setDefaultGradeScale(scale: GradeScale) {
        preferencesRepository.setDefaultGradeScale(scale)
    }

    /**
     * Kopiuje tymczasowe pliki ze wskaźników Content Resolver do prywatnego katalogu aplikacji,
     * gwarantując trwały dostęp do zdjęć i filmów po ponownym uruchomieniu urządzenia.
     */
    private fun persistMediaUris(context: Context, uris: List<String>): List<String> {
        return uris.mapNotNull { uriString ->
            if (uriString.startsWith("file://")) return@mapNotNull uriString
            try {
                val uri = Uri.parse(uriString)
                val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                val extension = if (mimeType.startsWith("video/")) ".mp4" else ".jpg"
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@mapNotNull null
                val dir = File(context.filesDir, "boulder_media").apply { mkdirs() }
                val dest = File(dir, "media_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}$extension")
                inputStream.use { input ->
                    dest.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                Uri.fromFile(dest).toString()
            } catch (e: Exception) {
                uriString // W razie błędu fallback do oryginalnego URI
            }
        }
    }
}
