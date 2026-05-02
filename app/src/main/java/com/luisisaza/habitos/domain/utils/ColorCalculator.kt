package com.luisisaza.habitos.domain.utils

import androidx.compose.ui.graphics.Color
import com.luisisaza.habitos.domain.model.HabitLog
import java.time.LocalDate

enum class DayColor(val color: Color) {
    GREEN(Color(0xFF4CAF50)),
    YELLOW(Color(0xFFFFC107)),
    RED(Color(0xFFF44336)),
    GRAY(Color(0xFFE0E0E0)),
    FUTURE(Color(0xFFF5F5F5))
}

object ColorCalculator {

    fun calculateDayColor(
        habitIds: List<Long>,
        logsForDay: List<HabitLog>,
        date: LocalDate
    ): DayColor {
        if (date.isAfter(LocalDate.now())) return DayColor.FUTURE
        if (habitIds.isEmpty()) return DayColor.GRAY

        val completedCount = logsForDay.count { it.habitId in habitIds && it.completed }

        if (completedCount == 0 && logsForDay.none { it.habitId in habitIds }) {
            return DayColor.GRAY
        }

        val compliancePercentage = (completedCount * 100) / habitIds.size

        return when {
            compliancePercentage >= 67 -> DayColor.GREEN
            compliancePercentage >= 34 -> DayColor.YELLOW
            compliancePercentage > 0 -> DayColor.RED
            else -> DayColor.RED
        }
    }
}
