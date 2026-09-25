package com.example.bouldertrack.presentation.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bouldertrack.domain.model.ClimbStyle
import com.example.bouldertrack.domain.model.ProjectWithSession
import com.example.bouldertrack.domain.repository.BoulderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Stan widoku projektów wspinaczkowych.
 *
 * @property projects Lista projektów po zastosowaniu filtrów wyszukiwania i lokalizacji.
 * @property allGyms Lista unikalnych lokalizacji ścianek do wyboru w filtrach.
 * @property searchQuery Wprowadzona fraza wyszukiwania (przeszukuje nazwę ścianki, wycenę, tagi, notatki).
 * @property selectedGym Wybrany filtr ściany (null oznacza brak filtra).
 * @property isLoading Flaga ładowania danych z bazy.
 */
data class ProjectsUiState(
    val projects: List<ProjectWithSession> = emptyList(),
    val allGyms: List<String> = emptyList(),
    val searchQuery: String = "",
    val selectedGym: String? = null,
    val isLoading: Boolean = true
)

/**
 * ViewModel zarządzający listą aktywnych projektów wspinaczkowych (Project Book).
 *
 * Umożliwia filtrowanie, wyszukiwanie, rejestrowanie kolejnych prób (+1 wstawka),
 * oznaczanie projektów jako pokonane (TOP/FLASH) oraz usuwanie.
 */
class ProjectsViewModel(
    private val boulderRepository: BoulderRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedGym = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ProjectsUiState> = combine(
        boulderRepository.getActiveProjectsWithSession(),
        _searchQuery,
        _selectedGym
    ) { allProjects, query, selectedGym ->
        val gyms = allProjects.map { it.sessionLocation }.distinct().sorted()

        val filtered = allProjects.filter { project ->
            val matchesGym = selectedGym == null || project.sessionLocation.equals(selectedGym, ignoreCase = true)
            val matchesQuery = query.isBlank() || (
                matchesGrade(project.boulder.grade.value, query) ||
                project.sessionLocation.contains(query, ignoreCase = true) ||
                (project.boulder.note?.contains(query, ignoreCase = true) == true) ||
                project.boulder.tags.any { matchesTag(it, query) }
            )
            matchesGym && matchesQuery
        }

        ProjectsUiState(
            projects = filtered,
            allGyms = gyms,
            searchQuery = query,
            selectedGym = selectedGym,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProjectsUiState()
    )

    /** Aktualizuje wpisaną frazę wyszukiwania. */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    /** Przełącza lub czyści filtr wybranej ściany wspinaczkowej. */
    fun onGymFilterChange(gym: String?) {
        _selectedGym.value = if (_selectedGym.value == gym) null else gym
    }

    /** Zwiększa liczbę prób na wybranym projekcie o 1. */
    fun incrementAttempts(project: ProjectWithSession) {
        viewModelScope.launch {
            val updatedBoulder = project.boulder.copy(
                attempts = project.boulder.attempts + 1
            )
            boulderRepository.updateBoulder(updatedBoulder)
        }
    }

    /** Oznacza projekt jako ukończony (domyślnie [ClimbStyle.TOP]). */
    fun markAsSent(project: ProjectWithSession, style: ClimbStyle = ClimbStyle.TOP) {
        viewModelScope.launch {
            val updatedBoulder = project.boulder.copy(
                style = style
            )
            boulderRepository.updateBoulder(updatedBoulder)
        }
    }

    /** Usuwa projekt z bazy danych. */
    fun deleteProject(project: ProjectWithSession) {
        viewModelScope.launch {
            boulderRepository.deleteBoulder(project.boulder.id)
        }
    }

    private fun matchesGrade(gradeValue: String, query: String): Boolean {
        if (gradeValue.contains(query, ignoreCase = true)) return true
        val gLower = gradeValue.lowercase().trim()
        val qLower = query.lowercase().trim()
        val colorPairs = listOf(
            listOf("zielon", "green"),
            listOf("różow", "rozow", "pink"),
            listOf("biał", "bial", "white"),
            listOf("żółt", "zolt", "yellow"),
            listOf("pomarańcz", "pomarancz", "orange"),
            listOf("czerwon", "red"),
            listOf("fiolet", "purple"),
            listOf("niebiesk", "blue"),
            listOf("czarn", "black")
        )
        for (synonyms in colorPairs) {
            if (synonyms.any { gLower.contains(it) } && synonyms.any { qLower.contains(it) }) {
                return true
            }
        }
        return false
    }

    private fun matchesTag(tag: String, query: String): Boolean {
        if (tag.contains(query, ignoreCase = true)) return true
        val tLower = tag.lowercase().trim()
        val qLower = query.lowercase().trim()
        val tagPairs = listOf(
            listOf("krawąd", "krawad", "crimp"),
            listOf("oblak", "sloper"),
            listOf("dach", "roof"),
            listOf("połóg", "polog", "slab"),
            listOf("rys", "crack"),
            listOf("skok", "dyno")
        )
        for (synonyms in tagPairs) {
            if (synonyms.any { tLower.contains(it) } && synonyms.any { qLower.contains(it) }) {
                return true
            }
        }
        return false
    }
}

