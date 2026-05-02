package com.luisisaza.habitos.domain.model

import com.luisisaza.habitos.data.database.entity.HabitEntity
import org.json.JSONArray

data class Habit(
    val id: Long,
    val userId: Long,
    val name: String,
    val description: String,
    val type: HabitType,
    val days: List<HabitDay>,
    val scheduleEnabled: Boolean,
    val scheduleStartTime: String?,
    val scheduleEndTime: String?,
    val duration: HabitDuration?,
    val reminderEnabled: Boolean,
    val reminderTime: String?,
    val reminderPhrase: String?,
    val failureUnit: String?,
    val goal: String?,
    val streakCount: Int,
    val isActive: Boolean,
    val createdAt: Long
)

fun HabitEntity.toDomain(): Habit {
    val dayList = if (days != null) {
        val arr = JSONArray(days)
        (0 until arr.length()).mapNotNull { HabitDay.fromCode(arr.getString(it)) }
    } else emptyList()

    return Habit(
        id = id,
        userId = userId,
        name = name,
        description = description,
        type = HabitType.fromValue(type),
        days = dayList,
        scheduleEnabled = scheduleEnabled,
        scheduleStartTime = scheduleStartTime,
        scheduleEndTime = scheduleEndTime,
        duration = duration?.let { HabitDuration.fromCode(it) },
        reminderEnabled = reminderEnabled,
        reminderTime = reminderTime,
        reminderPhrase = reminderPhrase,
        failureUnit = failureUnit,
        goal = goal,
        streakCount = streakCount,
        isActive = isActive,
        createdAt = createdAt
    )
}

fun Habit.toEntity(): HabitEntity {
    val daysJson = if (days.isNotEmpty()) {
        JSONArray(days.map { it.code }).toString()
    } else null

    return HabitEntity(
        id = id,
        userId = userId,
        name = name,
        description = description,
        type = type.value,
        days = daysJson,
        scheduleEnabled = scheduleEnabled,
        scheduleStartTime = scheduleStartTime,
        scheduleEndTime = scheduleEndTime,
        duration = duration?.code,
        reminderEnabled = reminderEnabled,
        reminderTime = reminderTime,
        reminderPhrase = reminderPhrase,
        failureUnit = failureUnit,
        goal = goal,
        streakCount = streakCount,
        isActive = isActive,
        createdAt = createdAt
    )
}
