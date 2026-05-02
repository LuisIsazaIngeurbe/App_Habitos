package com.luisisaza.habitos.domain.model

enum class HabitDuration(val code: String, val displayLabel: String) {
    MIN_30("30_MIN", "30 min"),
    HOUR_1("1_HOUR", "1 hora"),
    HOUR_1_30("1_HALF", "1h 30min"),
    HOUR_2("2_HOUR", "2 horas");

    companion object {
        fun fromCode(code: String): HabitDuration? = entries.firstOrNull { it.code == code }
        fun all(): List<HabitDuration> = entries.toList()
    }
}
