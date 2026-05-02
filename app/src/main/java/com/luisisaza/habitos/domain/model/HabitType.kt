package com.luisisaza.habitos.domain.model

enum class HabitType(val value: String) {
    GOOD("GOOD"),
    BAD("BAD");

    companion object {
        fun fromValue(value: String): HabitType =
            entries.firstOrNull { it.value == value } ?: GOOD
    }
}
