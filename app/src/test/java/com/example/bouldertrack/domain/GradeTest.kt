package com.example.bouldertrack.domain

import com.example.bouldertrack.domain.model.Grade
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Testy jednostkowe hierarchii i przeliczania wycen wspinaczkowych [Grade].
 */
class GradeTest {

    @Test
    fun `font scale grades are ranked in increasing difficulty`() {
        val grades = listOf("4", "5a", "5c", "6a", "6a+", "6b", "6b+", "6c", "6c+", "7a", "7b+", "8a")
        val ranks = grades.map { Grade(it).difficultyRank() }

        for (i in 0 until ranks.size - 1) {
            assertTrue(
                "Oczekiwano, że ${grades[i]} (${ranks[i]}) < ${grades[i+1]} (${ranks[i+1]})",
                ranks[i] < ranks[i + 1]
            )
        }
    }

    @Test
    fun `v-scale grades are ranked in increasing difficulty`() {
        val vGrades = listOf("VB", "V0", "V1", "V2", "V4", "V7", "V10", "V13")
        val ranks = vGrades.map { Grade(it).difficultyRank() }

        for (i in 0 until ranks.size - 1) {
            assertTrue(
                "Oczekiwano, że ${vGrades[i]} (${ranks[i]}) < ${vGrades[i+1]} (${ranks[i+1]})",
                ranks[i] < ranks[i + 1]
            )
        }
    }

    @Test
    fun `color scale matches requested difficulty progression`() {
        // Kolejność: zielony, różowy, biały, żółty, pomarańczowy, czerwony, fioletowy, niebieski, czarny
        val colors = listOf(
            "zielony",
            "różowy",
            "biały",
            "żółty",
            "pomarańczowy",
            "czerwony",
            "fioletowy",
            "niebieski",
            "czarny"
        )
        val ranks = colors.map { Grade(it).difficultyRank() }

        for (i in 0 until ranks.size - 1) {
            assertTrue(
                "Kolor '${colors[i]}' (rank ${ranks[i]}) powinien być łatwiejszy niż '${colors[i+1]}' (rank ${ranks[i+1]})",
                ranks[i] < ranks[i + 1]
            )
        }
    }

    @Test
    fun `numeric scale 1-10 is ranked in natural order`() {
        val numericGrades = (1..10).map { it.toString() }
        val ranks = numericGrades.map { Grade(it).difficultyRank() }

        for (i in 0 until ranks.size - 1) {
            assertTrue(
                "Oczekiwano, że ${numericGrades[i]} < ${numericGrades[i+1]}",
                ranks[i] < ranks[i + 1]
            )
        }
    }

    @Test
    fun `font scale rank is case-insensitive`() {
        assertEquals(
            Grade("6a+").difficultyRank(),
            Grade("6A+").difficultyRank()
        )
        assertEquals(
            Grade("zielony").difficultyRank(),
            Grade("Zielony").difficultyRank()
        )
    }

    @Test
    fun `finding max grade selects highest difficulty boulder`() {
        val sessionBoulders = listOf("5b", "6a+", "7a", "6b")
        val maxGrade = sessionBoulders.maxByOrNull { Grade(it).difficultyRank() }

        assertEquals("7a", maxGrade)
    }
}
