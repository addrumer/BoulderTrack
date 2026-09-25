package com.example.bouldertrack.domain

import com.example.bouldertrack.domain.manager.TimerManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Testy jednostkowe logiki stopera odpoczynku [TimerManager].
 */
class TimerManagerTest {

    @Test
    fun `setDefaultTime updates target and remaining time when not running`() {
        TimerManager.setDefaultTime(120)

        assertEquals(120, TimerManager.targetTime.value)
        assertEquals(120, TimerManager.timeRemaining.value)
    }

    @Test
    fun `addTime increases target and remaining time`() {
        TimerManager.setDefaultTime(120)
        TimerManager.addTime(30)

        assertEquals(150, TimerManager.targetTime.value)
        assertEquals(150, TimerManager.timeRemaining.value)
    }

    @Test
    fun `addTime respects minimum time bound of 30 seconds`() {
        TimerManager.setDefaultTime(60)
        TimerManager.addTime(-100) // Próba zmniejszenia poniżej 30s

        assertTrue(
            "Czas docelowy nie może spaść poniżej 30 sekund",
            TimerManager.targetTime.value >= 30
        )
    }

    @Test
    fun `resetTimer restores remaining time to target time`() {
        TimerManager.setDefaultTime(180)
        TimerManager.resetTimer()

        assertEquals(180, TimerManager.timeRemaining.value)
        assertEquals(false, TimerManager.isRunning.value)
    }
}
