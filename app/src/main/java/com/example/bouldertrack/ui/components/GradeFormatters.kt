package com.example.bouldertrack.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.bouldertrack.R

/**
 * Zwraca zlokalizowaną nazwę wyceny w zależności od aktualnego języka aplikacji.
 * Dla skali kolorów (np. Zielony / Green) tłumaczy na bieżący język,
 * dla pozostałych skal (Fontainebleau, V-Scale, 1-10) zachowuje format zapisu.
 */
@Composable
fun formatGradeDisplayName(gradeValue: String): String {
    val lower = gradeValue.lowercase().trim()
    return when {
        lower.contains("zielon") || lower.contains("green") -> stringResource(R.string.kolor_zielony)
        lower.contains("różow") || lower.contains("rozow") || lower.contains("pink") -> stringResource(R.string.kolor_rozowy)
        lower.contains("biał") || lower.contains("bial") || lower.contains("white") -> stringResource(R.string.kolor_bialy)
        lower.contains("żółt") || lower.contains("zolt") || lower.contains("yellow") -> stringResource(R.string.kolor_zolty)
        lower.contains("pomarańcz") || lower.contains("pomarancz") || lower.contains("orange") -> stringResource(R.string.kolor_pomaranczowy)
        lower.contains("czerwon") || lower.contains("red") -> stringResource(R.string.kolor_czerwony)
        lower.contains("fiolet") || lower.contains("purple") -> stringResource(R.string.kolor_fioletowy)
        lower.contains("niebiesk") || lower.contains("blue") -> stringResource(R.string.kolor_niebieski)
        lower.contains("czarn") || lower.contains("black") -> stringResource(R.string.kolor_czarny)
        else -> gradeValue.uppercase()
    }
}

/**
 * Zwraca zlokalizowaną nazwę tagu rodzaju chwytu / formacji wspinaczkowej.
 */
@Composable
fun formatTagDisplayName(tag: String): String {
    val lower = tag.lowercase().trim()
    return when {
        lower.contains("krawąd") || lower.contains("krawad") || lower.contains("crimp") -> stringResource(R.string.tag_krawadki)
        lower.contains("oblak") || lower.contains("sloper") -> stringResource(R.string.tag_oblaki)
        lower.contains("dach") || lower.contains("roof") -> stringResource(R.string.tag_dach)
        lower.contains("połóg") || lower.contains("polog") || lower.contains("slab") -> stringResource(R.string.tag_polog)
        lower.contains("rys") || lower.contains("crack") -> stringResource(R.string.tag_rysa)
        lower.contains("skok") || lower.contains("dyno") -> stringResource(R.string.tag_skok)
        else -> tag
    }
}
