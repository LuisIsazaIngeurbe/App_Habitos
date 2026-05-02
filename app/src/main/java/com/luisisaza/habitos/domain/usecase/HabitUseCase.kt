package com.luisisaza.habitos.domain.usecase

import com.luisisaza.habitos.data.repository.HabitRepository
import com.luisisaza.habitos.data.repository.HabitLogRepository
import com.luisisaza.habitos.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class HabitUseCase(
    private val habitRepository: HabitRepository,
    private val habitLogRepository: HabitLogRepository
) {

    fun getAllHabits(userId: Long): Flow<List<Habit>> =
        habitRepository.getHabitsByUser(userId).map { list -> list.map { it.toDomain() } }

    fun getHabitsByType(userId: Long, type: HabitType): Flow<List<Habit>> =
        habitRepository.getHabitsByType(userId, type.value).map { list -> list.map { it.toDomain() } }

    suspend fun addHabit(habit: Habit): Long = habitRepository.insertHabit(habit.toEntity())

    suspend fun updateHabit(habit: Habit) = habitRepository.updateHabit(habit.toEntity())

    suspend fun deleteHabit(id: Long) = habitRepository.softDeleteHabit(id)

    suspend fun logHabitCompletion(habitId: Long, date: LocalDate, completed: Boolean) {
        val epochDay = date.toEpochDay()
        val existing = habitLogRepository.getLogForHabitOnDay(habitId, epochDay)

        val habit = habitRepository.getHabitById(habitId)

        if (existing != null) {
            habitLogRepository.updateLog(existing.copy(completed = completed))
        } else {
            habitLogRepository.upsertLog(
                com.luisisaza.habitos.data.database.entity.HabitLogEntity(
                    habitId = habitId,
                    date = epochDay,
                    completed = completed
                )
            )
        }

        // Update bad-habit streak counter
        if (habit?.type == "BAD") {
            val newStreak = if (completed) (habit.streakCount + 1) else 0
            habitRepository.updateStreakCount(habitId, newStreak)
        }
    }

    suspend fun getTodayHabits(userId: Long): List<Habit> =
        getApplicableHabits(userId, LocalDate.now())

    suspend fun getApplicableHabits(userId: Long, date: LocalDate): List<Habit> {
        val dayCode = date.dayOfWeek.name.take(3)
        return habitRepository.getHabitsByUserSuspend(userId).map { it.toDomain() }.filter { habit ->
            when (habit.type) {
                HabitType.BAD -> true
                HabitType.GOOD -> habit.days.any { it.code == dayCode }
            }
        }
    }
}
