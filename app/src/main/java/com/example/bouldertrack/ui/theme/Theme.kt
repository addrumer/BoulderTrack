package com.example.bouldertrack.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.bouldertrack.domain.model.AppThemeMode

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

private val DarkTrackerColorScheme = darkColorScheme(
    primary = PrimaryOrange,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryOrangeVariant,
    onPrimaryContainer = OnPrimary,
    secondary = PrimaryOrangeVariant,
    onSecondary = OnPrimary,
    secondaryContainer = SurfaceVariantDark,
    onSecondaryContainer = OnSurfaceVariantDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark
)

private val AmoledTrackerColorScheme = darkColorScheme(
    primary = PrimaryOrange,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryOrangeVariant,
    onPrimaryContainer = OnPrimary,
    secondary = PrimaryOrangeVariant,
    onSecondary = OnPrimary,
    secondaryContainer = Color(0xFF1F1F1F),
    onSecondaryContainer = Color(0xFFE0E0E0),
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White,
    surfaceVariant = Color.Black,
    onSurfaceVariant = Color(0xFF9E9E9E),
    outline = Color(0xFF2E2E2E)
)

private val LightTrackerColorScheme = lightColorScheme(
    primary = PrimaryOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFECE5),
    onPrimaryContainer = Color(0xFF802200),
    secondary = PrimaryOrangeVariant,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1F3F5),
    onSecondaryContainer = Color(0xFF495057),
    background = Color(0xFFF8F9FA),
    onBackground = Color(0xFF212529),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF212529),
    surfaceVariant = Color(0xFFE9ECEF),
    onSurfaceVariant = Color(0xFF6C757D),
    outline = Color(0xFFCED4DA)
)

/**
 * Główny motyw aplikacji BoulderTrack z pełnym wsparciem trybów:
 * Systemowy, Jasny, Ciemny (grafitowy) oraz AMOLED (czysta czerń).
 */
@Composable
fun BoulderTrackTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val colorScheme = when (themeMode) {
        AppThemeMode.LIGHT -> LightTrackerColorScheme
        AppThemeMode.DARK -> DarkTrackerColorScheme
        AppThemeMode.AMOLED -> AmoledTrackerColorScheme
        AppThemeMode.SYSTEM -> if (isSystemDark) DarkTrackerColorScheme else LightTrackerColorScheme
    }

    val isDarkAppearance = when (themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK, AppThemeMode.AMOLED -> true
        AppThemeMode.SYSTEM -> isSystemDark
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context.findActivity()
            activity?.window?.let { window ->
                @Suppress("DEPRECATION")
                window.statusBarColor = colorScheme.background.toArgb()
                @Suppress("DEPRECATION")
                window.navigationBarColor = Color.Transparent.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !isDarkAppearance
                insetsController.isAppearanceLightNavigationBars = !isDarkAppearance
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}