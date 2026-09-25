package com.example.bouldertrack.presentation

import com.example.bouldertrack.data.FakeBoulderRepository
import com.example.bouldertrack.data.FakeSessionRepository
import com.example.bouldertrack.domain.model.BoulderProblem
import com.example.bouldertrack.domain.model.ClimbStyle
import com.example.bouldertrack.domain.model.Grade
import com.example.bouldertrack.domain.model.SessionWithStats
import com.example.bouldertrack.domain.model.StatsTimeRange
import com.example.bouldertrack.presentation.stats.StatsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Testy jednostkowe dla [StatsViewModel] weryfikujące filtry czasowe oraz kalendarz aktywności.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeSessionRepository: FakeSessionRepository
    private lateinit var fakeBoulderRepository: FakeBoulderRepository
    private lateinit var viewModel: StatsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeSessionRepository = FakeSessionRepository()
        fakeBoulderRepository = FakeBoulderRepository()
        viewModel = StatsViewModel(fakeBoulderRepository, fakeSessionRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `stats aggregates activity map and total training days`() = runTest(testDispatcher) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val yesterday = LocalDate.fromEpochDays(today.toEpochDays() - 1)

        val s1 = SessionWithStats(
            id = 1L,
            date = today,
            location = "Murall",
            notes = null,
            totalBoulders = 4,
            maxGrade = "6B",
            flashCount = 2,
            topCount = 2,
            projectCount = 0
        )
        val s2 = SessionWithStats(
            id = 2L,
            date = yesterday,
            location = "Makak",
            notes = null,
            totalBoulders = 6,
            maxGrade = "7A",
            flashCount = 3,
            topCount = 3,
            projectCount = 0
        )
        fakeSessionRepository.sessionsFlow.value = listOf(s1, s2)

        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.totalTrainingDays)
        assertTrue("Passa powinna wynosić co najmniej 2 dni", state.currentStreakDays >= 2)
        assertEquals(true, state.activityMap.containsKey(today))
        assertEquals(true, state.activityMap.containsKey(yesterday))

        collectJob.cancel()
    }

    @Test
    fun `time range filtering filters out older sessions and boulders`() = runTest(testDispatcher) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val oldDate = LocalDate.fromEpochDays(today.toEpochDays() - 100) // 100 dni temu

        val sRecent = SessionWithStats(
            id = 1L,
            date = today,
            location = "Murall",
            notes = null,
            totalBoulders = 3,
            maxGrade = "6A",
            flashCount = 1,
            topCount = 2,
            projectCount = 0
        )
        val sOld = SessionWithStats(
            id = 2L,
            date = oldDate,
            location = "Makak",
            notes = null,
            totalBoulders = 5,
            maxGrade = "7C",
            flashCount = 2,
            topCount = 3,
            projectCount = 0
        )
        fakeSessionRepository.sessionsFlow.value = listOf(sRecent, sOld)

        val bRecent = BoulderProblem(id = 1L, sessionId = 1L, grade = Grade("6A"), style = ClimbStyle.TOP, attempts = 1)
        val bOld = BoulderProblem(id = 2L, sessionId = 2L, grade = Grade("7C"), style = ClimbStyle.TOP, attempts = 4)
        fakeBoulderRepository.bouldersFlow.value = listOf(bRecent, bOld)

        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        // Przy ALL_TIME widoczne są 2 baldy
        assertEquals(2L, viewModel.uiState.value.totalBoulders)

        // Wybór LAST_30_DAYS
        viewModel.onTimeRangeSelected(StatsTimeRange.LAST_30_DAYS)
        advanceUntilIdle()

        val filteredState = viewModel.uiState.value
        assertEquals(1L, filteredState.totalBoulders)
        assertEquals(1, filteredState.progressionData.size)
        assertEquals("6A", filteredState.progressionData.first().second)

        collectJob.cancel()
    }
}
