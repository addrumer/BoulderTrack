package com.example.bouldertrack.presentation

import com.example.bouldertrack.data.FakeBoulderRepository
import com.example.bouldertrack.data.FakeSessionRepository
import com.example.bouldertrack.domain.model.SessionWithStats
import com.example.bouldertrack.presentation.sessionlist.SessionListUiState
import com.example.bouldertrack.presentation.sessionlist.SessionListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Testy jednostkowe dla [SessionListViewModel].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SessionListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeSessionRepository: FakeSessionRepository
    private lateinit var fakeBoulderRepository: FakeBoulderRepository
    private lateinit var viewModel: SessionListViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeSessionRepository = FakeSessionRepository()
        fakeBoulderRepository = FakeBoulderRepository()
        viewModel = SessionListViewModel(fakeSessionRepository, fakeBoulderRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `createSession inserts session into repository`() = runTest(testDispatcher) {
        viewModel.createSession("Murall Annopol")
        advanceUntilIdle()

        val sessions = fakeSessionRepository.sessionsFlow.value
        assertEquals(1, sessions.size)
        assertEquals("Murall Annopol", sessions.first().location)
    }

    @Test
    fun `createSession falls back to default name when empty`() = runTest(testDispatcher) {
        viewModel.createSession("   ")
        advanceUntilIdle()

        val sessions = fakeSessionRepository.sessionsFlow.value
        assertEquals("Trening", sessions.first().location)
    }

    @Test
    fun `deleteSession removes session from repository`() = runTest(testDispatcher) {
        val s1 = SessionWithStats(
            id = 1L,
            date = LocalDate(2026, 9, 20),
            location = "Makak",
            notes = null,
            totalBoulders = 5,
            maxGrade = "6B",
            flashCount = 2,
            topCount = 3,
            projectCount = 0
        )
        fakeSessionRepository.sessionsFlow.value = listOf(s1)

        viewModel.deleteSession(1L)
        advanceUntilIdle()

        assertTrue(fakeSessionRepository.sessionsFlow.value.isEmpty())
    }

    @Test
    fun `search and filter logic correctly filters sessions`() = runTest(testDispatcher) {
        val s1 = SessionWithStats(
            id = 1L,
            date = LocalDate(2026, 9, 20),
            location = "Murall",
            notes = "Ciężki trening palców",
            totalBoulders = 5,
            maxGrade = "6B",
            flashCount = 2,
            topCount = 3,
            projectCount = 0
        )
        val s2 = SessionWithStats(
            id = 2L,
            date = LocalDate(2026, 9, 21),
            location = "Makak",
            notes = "Dachy i oblaki",
            totalBoulders = 8,
            maxGrade = "7A",
            flashCount = 4,
            topCount = 4,
            projectCount = 0
        )
        fakeSessionRepository.sessionsFlow.value = listOf(s1, s2)

        // Subskrybujemy uiState
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        // Filtr po nazwie lokacji "Makak"
        viewModel.onGymFilterChange("Makak")
        advanceUntilIdle()

        val successState = viewModel.uiState.value as SessionListUiState.Success
        assertEquals(1, successState.sessions.size)
        assertEquals("Makak", successState.sessions.first().location)

        // Reset filtra i wyszukiwanie notatki "palców"
        viewModel.onGymFilterChange(null)
        viewModel.onSearchQueryChange("palców")
        advanceUntilIdle()

        val searchState = viewModel.uiState.value as SessionListUiState.Success
        assertEquals(1, searchState.sessions.size)
        assertEquals("Murall", searchState.sessions.first().location)

        collectJob.cancel()
    }

    @Test
    fun `updateSession updates location, date and notes`() = runTest(testDispatcher) {
        viewModel.createSession("Początkowa ściana")
        advanceUntilIdle()

        val sessionId = fakeSessionRepository.sessionsFlow.value.first().id
        val newDate = LocalDate(2026, 9, 25)
        viewModel.updateSession(sessionId, "Nowa Ściana", newDate, "Świetna sesja")
        advanceUntilIdle()

        val updated = fakeSessionRepository.sessionsFlow.value.first()
        assertEquals("Nowa Ściana", updated.location)
        assertEquals(newDate, updated.date)
        assertEquals("Świetna sesja", updated.notes)
    }
}
