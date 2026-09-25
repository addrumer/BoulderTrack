package com.example.bouldertrack.presentation.sessionlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bouldertrack.domain.model.BoulderProblem
import com.example.bouldertrack.domain.model.Session
import com.example.bouldertrack.domain.model.SessionWithStats
import com.example.bouldertrack.domain.repository.BoulderRepository
import com.example.bouldertrack.domain.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * Reprezentacja stanu interfejsu (UI State) dla ekranu głównego (Dashboard / Lista sesji).
 */
sealed interface SessionListUiState {
    /** Stan początkowego ładowania danych z bazy. */
    data object Loading : SessionListUiState

    /** Stan pusty, gdy w bazie nie ma jeszcze zapisanych sesji wspinaczkowych. */
    data class Empty(
        val activeProjects: List<BoulderProblem> = emptyList()
    ) : SessionListUiState

    /** Stan z załadowaną listą sesji oraz aktywnymi projektami. */
    data class Success(
        val sessions: List<SessionWithStats>,
        val allSessions: List<SessionWithStats>,
        val activeProjects: List<BoulderProblem> = emptyList(),
        val allGyms: List<String> = emptyList(),
        val searchQuery: String = "",
        val selectedGym: String? = null
    ) : SessionListUiState
}

/**
 * ViewModel zarządzający stanem listy sesji, tworzeniem nowych treningów, wyszukiwaniem oraz ich usuwaniem.
 *
 * Łączy strumienie [SessionRepository.getSessionsWithStats] oraz [BoulderRepository.getActiveProjects]
 * wraz z parametrami filtrowania i wyszukiwania w jeden spójny stan [uiState].
 */
class SessionListViewModel(
    private val sessionRepository: SessionRepository,
    private val boulderRepository: BoulderRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow(value = "")
    private val _selectedGym = MutableStateFlow<String?>(value = null)

    val uiState: StateFlow<SessionListUiState> = combine(
        sessionRepository.getSessionsWithStats(),
        boulderRepository.getActiveProjects(),
        _searchQuery,
        _selectedGym
    ) { sessions, projects, query, selectedGym ->
        if (sessions.isEmpty()) {
            SessionListUiState.Empty(projects)
        } else {
            val gyms = sessions.map { it.location }.distinct().sorted()
            val filteredSessions = sessions.filter { session ->
                val matchesGym = selectedGym == null || session.location.equals(selectedGym, ignoreCase = true)
                val matchesQuery = query.isBlank() || (
                    session.location.contains(query, ignoreCase = true) ||
                    (session.notes?.contains(query, ignoreCase = true) == true) ||
                    session.date.toString().contains(query, ignoreCase = true)
                )
                matchesGym && matchesQuery
            }
            SessionListUiState.Success(
                sessions = filteredSessions,
                allSessions = sessions,
                activeProjects = projects,
                allGyms = gyms,
                searchQuery = query,
                selectedGym = selectedGym
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SessionListUiState.Loading
    )

    /** Aktualizuje zapytanie wyszukiwania sesji. */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    /** Przełącza lub resetuje filtr ściany wspinaczkowej. */
    fun onGymFilterChange(gym: String?) {
        _selectedGym.value = if (_selectedGym.value == gym) null else gym
    }

    /**
     * Tworzy i zapisuje nową sesję wspinaczkową z dzisiejszą datą.
     *
     * @param location Nazwa ściany wspinaczkowej lub rejonu (domyślnie "Trening", jeśli puste).
     */
    fun createSession(location: String) {
        viewModelScope.launch {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            sessionRepository.insertSession(
                Session(
                    date = today,
                    location = location.ifBlank { "Trening" },
                    notes = null
                )
            )
        }
    }

    /**
     * Usuwa sesję wspinaczkową wraz ze wszystkimi powiązanymi z nią boulderami.
     */
    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            sessionRepository.deleteSession(sessionId)
        }
    }

    /**
     * Aktualizuje parametry sesji wspinaczkowej (nazwę, datę, notatki).
     */
    fun updateSession(sessionId: Long, location: String, date: LocalDate, notes: String?) {
        viewModelScope.launch {
            sessionRepository.updateSession(
                Session(
                    id = sessionId,
                    date = date,
                    location = location.trim().ifBlank { "Trening" },
                    notes = notes?.trim()?.takeIf { it.isNotBlank() }
                )
            )
        }
    }
}
