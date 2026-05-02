package com.luisisaza.habitos.domain.model

import com.luisisaza.habitos.data.database.entity.HabitLogEntity
import java.time.LocalDate

data class HabitLog(
    val id: Long,
    val habitId: Long,
    val date: LocalDate,
    val completed: Boolean,
    val value: Int?,
    val notes: String?,
    val timestamp: Long
)

fun HabitLogEntity.toDomain() = HabitLog(
    id = id,
    habitId = habitId,
    date = LocalDate.ofEpochDay(date),
    completed = completed,
    value = value,
    notes = notes,
    timestamp = timestamp
)

fun HabitLog.toEntity() = HabitLogEntity(
    id = id,
    habitId = habitId,
    date = date.toEpochDay(),
    completed = completed,
    value = value,
    notes = notes,
    timestamp = timestamp
)
