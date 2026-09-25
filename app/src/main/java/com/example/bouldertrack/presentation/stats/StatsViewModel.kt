package com.example.bouldertrack.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bouldertrack.domain.model.ClimbStyle
import com.example.bouldertrack.domain.model.StatsTimeRange
import com.example.bouldertrack.domain.repository.BoulderRepository
import com.example.bouldertrack.domain.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * Reprezentacja stanu interfejsu (UI State) dla ekranu statystyk, analityki oraz kalendarza aktywności.
 */
data class StatsUiState(
    val selectedTimeRange: StatsTimeRange = StatsTimeRange.ALL_TIME,
    val bouldersByGrade: Map<String, Long> = emptyMap(),
    val progressionData: List<Pair<LocalDate, String>> = emptyList(),
    val tagCounts: Map<String, Int> = emptyMap(),
    val flashCount: Long = 0,
    val topCount: Long = 0,
    val projectCount: Long = 0,
    val totalBoulders: Long = 0,
    val totalAttempts: Long = 0,
    val badges: List<String> = emptyList(),
    val activityMap: Map<LocalDate, Int> = emptyMap(),
    val totalTrainingDays: Int = 0,
    val currentStreakDays: Int = 0,
    val isLoading: Boolean = true
)

/**
 * ViewModel agregujący analitykę treningową, filtry okresu, odznaki osiągnięć,
 * piramidę trudności, radar stylów oraz kalendarz regularności w stylu GitHub (heatmapa).
 */
class StatsViewModel(
    boulderRepository: BoulderRepository,
    sessionRepository: SessionRepository
) : ViewModel() {

    private val _timeRange = MutableStateFlow(StatsTimeRange.ALL_TIME)

    /** Zmienia wybrany zakres czasowy statystyk. */
    fun onTimeRangeSelected(range: StatsTimeRange) {
        _timeRange.value = range
    }

    val uiState: StateFlow<StatsUiState> = combine(
        sessionRepository.getSessionsWithStats(),
        boulderRepository.getAllBoulders(),
        _timeRange
    ) { allSessions, allBoulders, timeRange ->
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        // Obliczenie aktywności treningowej dla kalendarza (heatmapy w stylu GitHub)
        val activityMap = mutableMapOf<LocalDate, Int>()
        allSessions.forEach { session ->
            activityMap[session.date] = (activityMap[session.date] ?: 0) + session.totalBoulders.toInt().coerceAtLeast(1)
        }
        val trainingDaysCount = activityMap.keys.size

        // Obliczanie bieżącej passy dni treningowych (streak)
        var streak = 0
        var checkEpoch = today.toEpochDays()
        if (!activityMap.containsKey(today) && activityMap.containsKey(LocalDate.fromEpochDays(checkEpoch - 1))) {
            checkEpoch -= 1
        }
        while (activityMap.containsKey(LocalDate.fromEpochDays(checkEpoch))) {
            streak++
            checkEpoch--
        }

        // Filtrowanie sesji według wybranego przedziału czasowego
        val filteredSessions = when (timeRange) {
            StatsTimeRange.ALL_TIME -> allSessions
            StatsTimeRange.LAST_30_DAYS -> {
                val cutoff = LocalDate.fromEpochDays(today.toEpochDays() - 30)
                allSessions.filter { it.date >= cutoff }
            }
            StatsTimeRange.LAST_90_DAYS -> {
                val cutoff = LocalDate.fromEpochDays(today.toEpochDays() - 90)
                allSessions.filter { it.date >= cutoff }
            }
            StatsTimeRange.THIS_YEAR -> {
                allSessions.filter { it.date.year == today.year }
            }
        }

        val sessionIds = filteredSessions.map { it.id }.toSet()
        val filteredBoulders = allBoulders.filter { it.sessionId in sessionIds }

        val flash = filteredBoulders.count { it.style == ClimbStyle.FLASH }.toLong()
        val top = filteredBoulders.count { it.style == ClimbStyle.TOP }.toLong()
        val project = filteredBoulders.count { it.style == ClimbStyle.PROJECT }.toLong()
        val total = flash + top + project
        val attempts = filteredBoulders.sumOf { it.attempts }.toLong()

        // Piramida wycen dla ukończonych baldów (FLASH, TOP)
        val gradesMap = filteredBoulders
            .filter { it.style in listOf(ClimbStyle.FLASH, ClimbStyle.TOP) }
            .groupingBy { it.grade.value }
            .eachCount()
            .mapValues { it.value.toLong() }

        // Oś czasu: maksymalna pokonana wycena w każdej sesji
        val progression = filteredSessions
            .filter { it.maxGrade != null }
            .sortedBy { it.date }
            .map { it.date to it.maxGrade!! }

        // Obliczanie odznak i osiągnięć
        val badgesList = mutableListOf<String>()
        if (flash >= 5) badgesList.add("Flash King 👑")
        if (attempts >= 50) badgesList.add("Walczak 💪")
        if (total >= 100) badgesList.add("Maszyna Wspinaczkowa 🚀")
        if (allSessions.size >= 10) badgesList.add("Miesiąc na ścianie 🧗‍♂️")
        if (streak >= 3) badgesList.add("W ogniu 🔥")

        // Częstotliwość występowania tagów (chwyty i formacje)
        val tagFreq = mutableMapOf<String, Int>()
        filteredBoulders.forEach { boulder ->
            boulder.tags.forEach { tag ->
                tagFreq[tag] = tagFreq.getOrDefault(tag, 0) + 1
            }
        }

        StatsUiState(
            selectedTimeRange = timeRange,
            bouldersByGrade = gradesMap,
            progressionData = progression,
            tagCounts = tagFreq,
            flashCount = flash,
            topCount = top,
            projectCount = project,
            totalBoulders = total,
            totalAttempts = attempts,
            badges = badgesList,
            activityMap = activityMap,
            totalTrainingDays = trainingDaysCount,
            currentStreakDays = streak,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsUiState(isLoading = true)
    )
}
