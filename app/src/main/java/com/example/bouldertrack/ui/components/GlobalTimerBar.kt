package com.example.bouldertrack.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bouldertrack.R
import com.example.bouldertrack.domain.manager.TimerManager

/**
 * Globalny, pływający pasek stopera odpoczynku wyświetlany u dołu ekranu niezależnie od nawigacji.
 * Posiada zaokrągloną formę w stylu frosted glass (półprzezroczyste szkło), dzięki czemu
 * przewijana zawartość ekranu jest pod nim widoczna.
 */
@Composable
fun GlobalTimerBar() {
    val timeRemaining by TimerManager.timeRemaining.collectAsStateWithLifecycle()
    val isRunning by TimerManager.isRunning.collectAsStateWithLifecycle()
    val targetTime by TimerManager.targetTime.collectAsStateWithLifecycle()

    AnimatedVisibility(
        visible = isRunning || (timeRemaining < targetTime && timeRemaining > 0),
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        val progress = if (targetTime > 0) timeRemaining.toFloat() / targetTime.toFloat() else 0f
        val minutes = timeRemaining / 60
        val seconds = timeRemaining % 60
        val timeStr = String.format("%02d:%02d", minutes, seconds)

        val isSystemDark = isSystemInDarkTheme()
        val isAmoled = MaterialTheme.colorScheme.background == Color.Black
        val isDark = isAmoled || isSystemDark || MaterialTheme.colorScheme.background.red < 0.15f

        // Półprzezroczyste tło szkła stopera (przezroczyste, aby widoczna była przewijana zawartość)
        val timerGlassBackground = when {
            isAmoled -> Color(0xFF140802).copy(alpha = 0.72f)
            isDark -> Color(0xFF221107).copy(alpha = 0.70f)
            else -> Color(0xFFFFF0EB).copy(alpha = 0.78f)
        }

        val timerBorderBrush = Brush.verticalGradient(
            listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),
                Color.White.copy(alpha = 0.15f)
            )
        )

        Surface(
            color = timerGlassBackground,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = RoundedCornerShape(20.dp),
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, timerBorderBrush),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clickable { TimerManager.toggleTimer() }
        ) {
            Column {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(3.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.stoper_odpoczynku),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = timeStr,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { TimerManager.toggleTimer() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isRunning) "Pauza" else "Wznów"
                            )
                        }
                        IconButton(
                            onClick = { TimerManager.resetTimer() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Zatrzymaj"
                            )
                        }
                    }
                }
            }
        }
    }
}
