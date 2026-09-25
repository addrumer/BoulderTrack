package com.example.bouldertrack.domain.model

/**
 * Klasa wartości domeny (inline value class) reprezentująca wycenę wspinaczkową.
 * Zapewnia bezpieczeństwo typów w czasie kompilacji bez narzutu pamięciowego
 * w czasie wykonania (dzięki adnotacji `@JvmInline`).
 *
 * Obsługiwane skale:
 * - Francuska / Fontainebleau: np. "4", "6a", "6b+", "7a", "7c+", "8b"
 * - Skala V (Hueco): np. "VB", "V0", "V3", "V10", "V17"
 * - Skala kolorów ściankowych: np. "Żółty", "Zielony", "Czerwony" itp.
 * - Skala numeryczna trudności 1–10: np. "1", "5", "10"
 *
 * @throws IllegalArgumentException jeśli podany ciąg znaków jest pusty.
 */
@JvmInline
value class Grade(val value: String) {
    init {
        require(value.isNotBlank()) {
            "Wycena wspinaczkowa nie może być pusta."
        }
    }

    /**
     * Zwraca numeryczną wagę trudności wyceny na potrzeby porównywania i wyznaczania maksymalnej wyceny.
     *
     * Skala kolorów (uszeregowana od najłatwiejszego do najtrudniejszego):
     * 1. Zielony, 2. Różowy, 3. Biały, 4. Żółty, 5. Pomarańczowy, 6. Czerwony, 7. Fioletowy, 8. Niebieski, 9. Czarny.
     */
    fun difficultyRank(): Int {
        val lower = value.lowercase().trim()

        // 1. Skala obwodowa kolorów ściankowych
        when {
            lower.contains("zielon") || lower.contains("green") -> return 10
            lower.contains("różow") || lower.contains("rozow") || lower.contains("pink") -> return 20
            lower.contains("biał") || lower.contains("bial") || lower.contains("white") -> return 30
            lower.contains("żółt") || lower.contains("zolt") || lower.contains("yellow") -> return 40
            lower.contains("pomarańcz") || lower.contains("pomarancz") || lower.contains("orange") -> return 50
            lower.contains("czerwon") || lower.contains("red") -> return 60
            lower.contains("fiolet") || lower.contains("purple") -> return 70
            lower.contains("niebiesk") || lower.contains("blue") -> return 80
            lower.contains("czarn") || lower.contains("black") -> return 90
        }

        // 2. Skala V (Hueco)
        if (lower.startsWith("v")) {
            val vPart = lower.removePrefix("v")
            if (vPart.equals("b", ignoreCase = true)) return 5
            vPart.toIntOrNull()?.let { return 100 + it * 10 }
        }

        // 3. Skala francuska / Fontainebleau
        val fontNumber = lower.filter { it.isDigit() }.toIntOrNull()
        if (fontNumber != null) {
            var rank = fontNumber * 100
            if (lower.contains("a+")) rank += 25
            else if (lower.contains("a")) rank += 10
            else if (lower.contains("b+")) rank += 45
            else if (lower.contains("b")) rank += 35
            else if (lower.contains("c+")) rank += 65
            else if (lower.contains("c")) rank += 55
            else if (lower.contains("+")) rank += 20
            return rank
        }

        return 0
    }

    override fun toString(): String = value.uppercase()
}