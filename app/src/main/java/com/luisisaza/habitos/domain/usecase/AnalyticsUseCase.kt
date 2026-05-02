package com.luisisaza.habitos.domain.usecase

import com.luisisaza.habitos.data.repository.HabitLogRepository
import com.luisisaza.habitos.data.repository.HabitRepository
import com.luisisaza.habitos.domain.model.Habit
import com.luisisaza.habitos.domain.model.HabitType
import com.luisisaza.habitos.domain.model.toDomain
import com.luisisaza.habitos.domain.utils.ColorCalculator
import com.luisisaza.habitos.domain.utils.DayColor
import com.luisisaza.habitos.domain.utils.StreakCalculator
import com.luisisaza.habitos.domain.utils.StreakInfo
import java.time.LocalDate
import java.time.YearMonth

data class DayAnalysis(
    val date: LocalDate,
    val color: DayColor,
    val completedCount: Int,
    val totalCount: Int
)

data class MonthlyAnalysis(
    val year: Int,
    val month: Int,
    val days: Map<Int, DayAnalysis>
)

data class HabitCompliance(
    val habitId: Long,
    val habitName: String,
    val habitType: HabitType,
    val compliancePercent: Double,
    val streak: StreakInfo,
    val failureCount: Int,
    val failureUnit: String?,
    val totalFailureQuantity: Int
)

data class HabitQuantityTotal(
    val habitId: Long,
    val habitName: String,
    val unit: String,
    val totalQuantity: Int
)

class AnalyticsUseCase(
    private val habitRepository: HabitRepository,
    private val habitLogRepository: HabitLogRepository
) {

    private fun isApplicableOnDay(habit: Habit, date: LocalDate): Boolean = when (habit.type) {
        HabitType.BAD -> true
        HabitType.GOOD -> {
            val dayCode = date.dayOfWeek.name.take(3)
            habit.days.any { it.code == dayCode }
        }
    }

    private suspend fun fetchHabits(habitIds: List<Long>): List<Habit> =
        habitIds.mapNotNull { habitRepository.getHabitById(it)?.toDomain() }

    suspend fun getMonthlyAnalysis(
        habitIds: List<Long>,
        year: Int,
        month: Int
    ): MonthlyAnalysis {
        val yearMonth = YearMonth.of(year, month)
        val startDate = yearMonth.atDay(1)
        val endDate = yearMonth.atEndOfMonth()

        val habits = fetchHabits(habitIds)

        val logs = habitLogRepository.getLogsInRangeSuspend(
            habitIds = habitIds,
            startEpoch = startDate.toEpochDay(),
            endEpoch = endDate.toEpochDay()
        ).map { it.toDomain() }
        val logsByDay = logs.groupBy { it.date.dayOfMonth }

        val days = (1..yearMonth.lengthOfMonth()).associate { day ->
            val date = yearMonth.atDay(day)
            val applicableIds = habits.filter { isApplicableOnDay(it, date) }.map { it.id }
            val dayLogs = logsByDay[day] ?: emptyList()
            val color = ColorCalculator.calculateDayColor(applicableIds, dayLogs, date)
            val completed = dayLogs.count { it.habitId in applicableIds && it.completed }
            day to DayAnalysis(
                date = date,
                color = color,
                completedCount = completed,
                totalCount = applicableIds.size
            )
        }

        return MonthlyAnalysis(year, month, days)
    }

    suspend fun getCompliancePercentage(
        habitIds: List<Long>,
        startDate: LocalDate,
        endDate: LocalDate
    ): Double {
        if (habitIds.isEmpty()) return 0.0
        val habits = fetchHabits(habitIds)

        var totalApplicable = 0
        var d = startDate
        while (!d.isAfter(endDate)) {
            habits.forEach { if (isApplicableOnDay(it, d)) totalApplicable++ }
            d = d.plusDays(1)
        }
        if (totalApplicable == 0) return 0.0

        val logs = habitLogRepository.getLogsInRangeSuspend(
            habitIds, startDate.toEpochDay(), endDate.toEpochDay()
        ).map { it.toDomain() }

        val completed = logs.count { log ->
            val habit = habits.firstOrNull { it.id == log.habitId } ?: return@count false
            log.completed && isApplicableOnDay(habit, log.date)
        }

        return (completed * 100.0) / totalApplicable
    }

    suspend fun getStreakInfo(habitId: Long): StreakInfo {
        val logs = habitLogRepository.getRecentLogs(habitId, 365).map { it.toDomain() }
        return StreakCalculator.calculate(logs)
    }

    suspend fun getHabitComplianceList(
        habitIds: List<Long>,
        startDate: LocalDate,
        endDate: LocalDate,
        habitNames: Map<Long, String>
    ): List<HabitCompliance> {
        return habitIds.mapNotNull { habitId ->
            val entity = habitRepository.getHabitById(habitId) ?: return@mapNotNull null
            val habit = entity.toDomain()

            var applicableDays = 0
            var d = startDate
            while (!d.isAfter(endDate)) {
                if (isApplicableOnDay(habit, d)) applicableDays++
                d = d.plusDays(1)
            }

            val rangeLogs = habitLogRepository.getLogsInRangeSuspend(
                listOf(habitId), startDate.toEpochDay(), endDate.toEpochDay()
            ).map { it.toDomain() }

            val completed = rangeLogs.count { it.completed && isApplicableOnDay(habit, it.date) }
            val percent = if (applicableDays > 0) (completed * 100.0) / applicableDays else 0.0

            val failureLogs = rangeLogs.filter { !it.completed }
            val failureCount = failureLogs.size
            val totalFailureQty = failureLogs.sumOf { it.value ?: 0 }

            val allLogs = habitLogRepository.getRecentLogs(habitId, 365).map { it.toDomain() }
            val streak = StreakCalculator.calculate(allLogs)

            HabitCompliance(
                habitId = habitId,
                habitName = habitNames[habitId] ?: habit.name,
                habitType = habit.type,
                compliancePercent = percent,
                streak = streak,
                failureCount = failureCount,
                failureUnit = habit.failureUnit,
                totalFailureQuantity = totalFailureQty
            )
        }
    }

    suspend fun getQuantityTotals(
        habitIds: List<Long>,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<HabitQuantityTotal> {
        if (habitIds.isEmpty()) return emptyList()
        val result = mutableListOf<HabitQuantityTotal>()
        habitIds.forEach { habitId ->
            val entity = habitRepository.getHabitById(habitId) ?: return@forEach
            val unit = entity.failureUnit ?: return@forEach
            val logs = habitLogRepository.getLogsInRangeSuspend(
                listOf(habitId), startDate.toEpochDay(), endDate.toEpochDay()
            )
            val total = logs.sumOf { it.value ?: 0 }
            if (total > 0) {
                result.add(
                    HabitQuantityTotal(
                        habitId = habitId,
                        habitName = entity.name,
                        unit = unit,
                        totalQuantity = total
                    )
                )
            }
        }
        return result
    }

    suspend fun getWeeklyBarData(
        habitIds: List<Long>,
        referenceDate: LocalDate
    ): List<Float> {
        if (habitIds.isEmpty()) return List(7) { 0f }
        val habits = fetchHabits(habitIds)
        val weekStart = referenceDate.minusDays(referenceDate.dayOfWeek.value.toLong() - 1)
        return (0..6).map { offset ->
            val day = weekStart.plusDays(offset.toLong())
            val applicableIds = habits.filter { isApplicableOnDay(it, day) }.map { it.id }
            if (applicableIds.isEmpty()) return@map 0f
            val dayLogs = habitLogRepository.getLogsForDaySuspend(applicableIds, day.toEpochDay())
            val done = dayLogs.count { it.habitId in applicableIds && it.completed }
            (done * 100f) / applicableIds.size
        }
    }
}
