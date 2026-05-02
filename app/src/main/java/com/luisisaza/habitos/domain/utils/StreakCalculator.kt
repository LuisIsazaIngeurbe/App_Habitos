package com.luisisaza.habitos.domain.utils

import com.luisisaza.habitos.domain.model.HabitLog
import java.time.LocalDate

data class StreakInfo(
    val currentStreak: Int,
    val bestStreak: Int,
    val totalCompleted: Int
)

object StreakCalculator {

    fun calculate(logs: List<HabitLog>): StreakInfo {
        if (logs.isEmpty()) return StreakInfo(0, 0, 0)

        val sortedCompleted = logs
            .filter { it.completed }
            .sortedBy { it.date }

        val totalCompleted = sortedCompleted.size

        if (totalCompleted == 0) return StreakInfo(0, 0, 0)

        var bestStreak = 1
        var tempStreak = 1

        for (i in 1 until sortedCompleted.size) {
            val prev = sortedCompleted[i - 1].date
            val curr = sortedCompleted[i].date
            if (curr == prev.plusDays(1)) {
                tempStreak++
                if (tempStreak > bestStreak) bestStreak = tempStreak
            } else {
                tempStreak = 1
            }
        }

        val currentStreak = calculateCurrentStreak(sortedCompleted)

        return StreakInfo(
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            totalCompleted = totalCompleted
        )
    }

    private fun calculateCurrentStreak(sortedCompleted: List<HabitLog>): Int {
        if (sortedCompleted.isEmpty()) return 0

        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        val lastLog = sortedCompleted.last()
        if (lastLog.date != today && lastLog.date != yesterday) return 0

        var streak = 0
        var expectedDate = lastLog.date

        for (log in sortedCompleted.reversed()) {
            if (log.date == expectedDate) {
                streak++
                expectedDate = expectedDate.minusDays(1)
            } else {
                break
            }
        }

        return streak
    }

    // For bad habits: consecutive days WITHOUT failing (i.e., completed = true means "didn't do it")
    fun calculateBadHabitStreak(logs: List<HabitLog>, storedStreak: Int): Int {
        if (logs.isEmpty()) return storedStreak
        val today = LocalDate.now()
        val todayLog = logs.firstOrNull { it.date == today }
        return when {
            todayLog == null -> storedStreak
            todayLog.completed -> storedStreak
            else -> 0
        }
    }
}
