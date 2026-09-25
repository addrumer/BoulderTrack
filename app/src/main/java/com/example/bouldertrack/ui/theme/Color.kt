package com.example.bouldertrack.ui.theme

import androidx.compose.ui.graphics.Color

// Modern Fitness / Tracker Palette (Dark Mode Only)
val PrimaryOrange = Color(0xFFFF5E3A)
val PrimaryOrangeVariant = Color(0xFFFF8235)
val OnPrimary = Color(0xFFFFFFFF)

val BackgroundDark = Color(0xFF0D0E11)
val OnBackgroundDark = Color(0xFFFFFFFF)

val SurfaceDark = Color(0xFF18191E)
val SurfaceDarkElevated = Color(0xFF1F2128)
val OnSurfaceDark = Color(0xFFFFFFFF)

val SurfaceVariantDark = Color(0xFF262931)
val OnSurfaceVariantDark = Color(0xFF8F94A3) // Text Secondary

val SuccessGreen = Color(0xFF4ADE80)
val FlashBadge = Color(0xFFFFC107)
val TopBadge = Color(0xFF4CAF50)
val ProjectBadge = Color(0xFF9E9E9E)

val OutlineDark = Color(0xFF3B3E46)

/**
 * Zwraca odpowiadający kolor interfejsu dla wyceny w skali obwodowej.
 *
 * Uszeregowanie trudności:
 * 1. Zielony, 2. Różowy, 3. Biały, 4. Żółty, 5. Pomarańczowy, 6. Czerwony, 7. Fioletowy, 8. Niebieski, 9. Czarny.
 */
fun getClimbingGradeColor(colorName: String): Color? {
    val lower = colorName.lowercase().trim()
    return when {
        lower.contains("zielon") || lower.contains("green") -> Color(0xFF4CAF50)
        lower.contains("różow") || lower.contains("rozow") || lower.contains("pink") -> Color(0xFFE91E63)
        lower.contains("biał") || lower.contains("bial") || lower.contains("white") -> Color(0xFFF5F5F5)
        lower.contains("żółt") || lower.contains("zolt") || lower.contains("yellow") -> Color(0xFFFFEB3B)
        lower.contains("pomarańcz") || lower.contains("pomarancz") || lower.contains("orange") -> Color(0xFFFF9800)
        lower.contains("czerwon") || lower.contains("red") -> Color(0xFFF44336)
        lower.contains("fiolet") || lower.contains("purple") -> Color(0xFF9C27B0)
        lower.contains("niebiesk") || lower.contains("blue") -> Color(0xFF2196F3)
        lower.contains("czarn") || lower.contains("black") -> Color(0xFF212121)
        else -> null
    }
}