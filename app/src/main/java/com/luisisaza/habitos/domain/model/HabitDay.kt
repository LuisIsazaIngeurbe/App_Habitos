package com.luisisaza.habitos.domain.model

enum class HabitDay(val code: String, val displayLabel: String) {
    MON("MON", "L"),
    TUE("TUE", "M"),
    WED("WED", "X"),
    THU("THU", "J"),
    FRI("FRI", "V"),
    SAT("SAT", "S"),
    SUN("SUN", "D");

    companion object {
        fun fromCode(code: String): HabitDay? = entries.firstOrNull { it.code == code }
    }
}
