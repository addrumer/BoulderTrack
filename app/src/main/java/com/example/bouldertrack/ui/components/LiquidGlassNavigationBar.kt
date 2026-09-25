package com.example.bouldertrack.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.example.bouldertrack.R

/**
 * Nowoczesny, pływający dolny pasek nawigacyjny w stylu "liquid glass" (półprzezroczyste szkło / frosted glass).
 *
 * Charakteryzuje się:
 * - Zaokrągloną kapsułową formą (pill) unoszącą się nad zawartością ekranu,
 * - Półprzezroczystym tłem z subtelną refleksyjną krawędzią (specular gradient border),
 * - Pełnym wsparciem trybu AMOLED (głęboka czerń dopasowana do ekranów OLED),
 * - Płynnymi animacjami przejścia i akcentu wybranej zakładki.
 */
@Composable
fun LiquidGlassNavigationBar(
    currentDestination: NavDestination?,
    onNavigateToHome: () -> Unit,
    onNavigateToProjects: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToBetaLibrary: () -> Unit,
    isAmoled: Boolean,
    modifier: Modifier = Modifier
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = isAmoled || isSystemDark || MaterialTheme.colorScheme.background == Color.Black ||
            MaterialTheme.colorScheme.background.red < 0.15f

    // Prawdziwa przezroczystość szkła (liquid frosted glass)
    // Zapewnia widoczność przewijanych elementów interfejsu (kart, kolorów, zdjęć) bezpośrednio pod paskiem
    val glassBackgroundColor = when {
        isAmoled -> Color(0xFF000000).copy(alpha = 0.68f)
        isDark -> Color(0xFF14161E).copy(alpha = 0.65f)
        else -> Color(0xFFFFFFFF).copy(alpha = 0.72f)
    }

    // Subtelny gradient krawędzi symulujący załamanie światła na szkle
    val glassBorderBrush = when {
        isAmoled -> Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.35f),
                Color.White.copy(alpha = 0.08f)
            )
        )
        isDark -> Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.28f),
                Color.White.copy(alpha = 0.07f)
            )
        )
        else -> Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.95f),
                Color.Black.copy(alpha = 0.12f)
            )
        )
    }

    val shadowElevation = when {
        isAmoled -> 6.dp
        isDark -> 10.dp
        else -> 12.dp
    }

    val spotShadowColor = when {
        isAmoled -> Color.Black
        isDark -> Color.Black.copy(alpha = 0.35f)
        else -> Color.Black.copy(alpha = 0.12f)
    }

    val barShape = RoundedCornerShape(32.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = shadowElevation,
                    shape = barShape,
                    spotColor = spotShadowColor,
                    ambientColor = spotShadowColor
                ),
            shape = barShape,
            color = glassBackgroundColor,
            border = BorderStroke(1.dp, glassBorderBrush)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Górny refleks świetlny charakterystyczny dla iPhone Liquid Glass
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = if (isDark) 0.08f else 0.35f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isHomeSelected = currentDestination?.hierarchy?.any { it.route?.endsWith("SessionList") == true } == true &&
                            currentDestination?.hierarchy?.any { it.route?.contains("SessionDetail") == true || it.route?.contains("Settings") == true } != true
                    val isProjectsSelected = currentDestination?.hierarchy?.any { it.route?.contains("Projects") == true } == true
                    val isStatsSelected = currentDestination?.hierarchy?.any { it.route?.contains("Stats") == true } == true
                    val isBetaSelected = currentDestination?.hierarchy?.any { it.route?.contains("BetaLibrary") == true } == true

                    LiquidGlassNavItem(
                        icon = Icons.Default.Home,
                        label = stringResource(R.string.nav_home),
                        selected = isHomeSelected,
                        onClick = onNavigateToHome,
                        isAmoled = isAmoled
                    )

                    LiquidGlassNavItem(
                        icon = Icons.Default.FitnessCenter,
                        label = stringResource(R.string.nav_projekty),
                        selected = isProjectsSelected,
                        onClick = onNavigateToProjects,
                        isAmoled = isAmoled
                    )

                    LiquidGlassNavItem(
                        icon = Icons.Default.BarChart,
                        label = stringResource(R.string.nav_statystyki),
                        selected = isStatsSelected,
                        onClick = onNavigateToStats,
                        isAmoled = isAmoled
                    )

                    LiquidGlassNavItem(
                        icon = Icons.Default.VideoLibrary,
                        label = stringResource(R.string.nav_patenty),
                        selected = isBetaSelected,
                        onClick = onNavigateToBetaLibrary,
                        isAmoled = isAmoled
                    )
                }
            }
        }
    }
}

/**
 * Pojedynczy element nawigacyjny z dynamicznym efektem zaznaczenia w stylu kapsułki iOS.
 */
@Composable
private fun RowScope.LiquidGlassNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    isAmoled: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.06f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "navTabScale"
    )

    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)

    val contentColor by animateColorAsState(
        targetValue = if (selected) activeColor else inactiveColor,
        animationSpec = tween(200),
        label = "navContentColor"
    )

    val pillColor by animateColorAsState(
        targetValue = if (selected) {
            activeColor.copy(alpha = if (isAmoled) 0.22f else 0.16f)
        } else {
            Color.Transparent
        },
        animationSpec = tween(200),
        label = "navPillColor"
    )

    Box(
        modifier = Modifier
            .weight(1f)
            .height(52.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = activeColor)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .background(pillColor, RoundedCornerShape(18.dp))
                .padding(horizontal = 8.dp, vertical = 5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = contentColor,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}
