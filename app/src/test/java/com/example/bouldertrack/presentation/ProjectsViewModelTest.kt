package com.example.bouldertrack.presentation

import com.example.bouldertrack.data.FakeBoulderRepository
import com.example.bouldertrack.domain.model.BoulderProblem
import com.example.bouldertrack.domain.model.ClimbStyle
import com.example.bouldertrack.domain.model.Grade
import com.example.bouldertrack.domain.model.ProjectWithSession
import com.example.bouldertrack.presentation.projects.ProjectsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
 * Testy jednostkowe dla [ProjectsViewModel].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProjectsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeBoulderRepository
    private lateinit var viewModel: ProjectsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeBoulderRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `incrementAttempts updates boulder attempts in repository`() = runTest(testDispatcher) {
        val boulder = BoulderProblem(
            id = 1L,
            sessionId = 10L,
            grade = Grade("6B"),
            style = ClimbStyle.PROJECT,
            attempts = 3
        )
        fakeRepository.bouldersFlow.value = listOf(boulder)
        viewModel = ProjectsViewModel(fakeRepository)

        val project = ProjectWithSession(
            boulder = boulder,
            sessionDate = LocalDate(2026, 9, 21),
            sessionLocation = "Murall"
        )

        viewModel.incrementAttempts(project)
        advanceUntilIdle()

        val updatedBoulder = fakeRepository.bouldersFlow.value.first { it.id == 1L }
        assertEquals(4, updatedBoulder.attempts)
    }

    @Test
    fun `markAsSent updates boulder style to TOP`() = runTest(testDispatcher) {
        val boulder = BoulderProblem(
            id = 2L,
            sessionId = 10L,
            grade = Grade("7A"),
            style = ClimbStyle.PROJECT,
            attempts = 5
        )
        fakeRepository.bouldersFlow.value = listOf(boulder)
        viewModel = ProjectsViewModel(fakeRepository)

        val project = ProjectWithSession(
            boulder = boulder,
            sessionDate = LocalDate(2026, 9, 21),
            sessionLocation = "Makak"
        )

        viewModel.markAsSent(project, ClimbStyle.TOP)
        advanceUntilIdle()

        val updatedBoulder = fakeRepository.bouldersFlow.value.first { it.id == 2L }
        assertEquals(ClimbStyle.TOP, updatedBoulder.style)
    }

    @Test
    fun `deleteProject removes boulder from repository`() = runTest(testDispatcher) {
        val boulder = BoulderProblem(
            id = 3L,
            sessionId = 10L,
            grade = Grade("6C"),
            style = ClimbStyle.PROJECT,
            attempts = 2
        )
        fakeRepository.bouldersFlow.value = listOf(boulder)
        viewModel = ProjectsViewModel(fakeRepository)

        val project = ProjectWithSession(
            boulder = boulder,
            sessionDate = LocalDate(2026, 9, 21),
            sessionLocation = "Murall"
        )

        viewModel.deleteProject(project)
        advanceUntilIdle()

        assertTrue(fakeRepository.bouldersFlow.value.none { it.id == 3L })
    }

    @Test
    fun `search query matches color grades across languages`() = runTest(testDispatcher) {
        val boulderPl = BoulderProblem(
            id = 4L,
            sessionId = 10L,
            grade = Grade("Zielony"),
            style = ClimbStyle.PROJECT,
            attempts = 1
        )
        val boulderEn = BoulderProblem(
            id = 5L,
            sessionId = 10L,
            grade = Grade("Pink"),
            style = ClimbStyle.PROJECT,
            attempts = 2
        )
        fakeRepository.bouldersFlow.value = listOf(boulderPl, boulderEn)
        viewModel = ProjectsViewModel(fakeRepository)

        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        // Szukanie angielskiego słowa "green" powinno dopasować polską wycenę "Zielony"
        viewModel.onSearchQueryChange("green")
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.projects.size)
        assertEquals(4L, viewModel.uiState.value.projects.first().boulder.id)

        // Szukanie polskiego słowa "różowy" powinno dopasować angielską wycenę "Pink"
        viewModel.onSearchQueryChange("różowy")
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.projects.size)
        assertEquals(5L, viewModel.uiState.value.projects.first().boulder.id)

        job.cancel()
    }

    @Test
    fun `search query matches tags across languages`() = runTest(testDispatcher) {
        val boulderWithTags = BoulderProblem(
            id = 6L,
            sessionId = 10L,
            grade = Grade("6B"),
            style = ClimbStyle.PROJECT,
            attempts = 1,
            tags = listOf("Krawądki", "Dach")
        )
        fakeRepository.bouldersFlow.value = listOf(boulderWithTags)
        viewModel = ProjectsViewModel(fakeRepository)

        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        // Szukanie angielskiego terminu "crimps" powinno znaleźć projekt z tagiem "Krawądki"
        viewModel.onSearchQueryChange("crimp")
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.projects.size)

        // Szukanie angielskiego terminu "roof" powinno znaleźć projekt z tagiem "Dach"
        viewModel.onSearchQueryChange("roof")
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.projects.size)

        job.cancel()
    }
}

