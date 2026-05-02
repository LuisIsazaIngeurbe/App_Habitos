package com.luisisaza.habitos.data.repository

import com.luisisaza.habitos.data.database.dao.HabitDao
import com.luisisaza.habitos.data.database.entity.HabitEntity
import kotlinx.coroutines.flow.Flow

class HabitRepository(private val dao: HabitDao) {

    fun getHabitsByUser(userId: Long): Flow<List<HabitEntity>> = dao.getHabitsByUser(userId)

    fun getHabitsByType(userId: Long, type: String): Flow<List<HabitEntity>> =
        dao.getHabitsByType(userId, type)

    suspend fun getHabitById(id: Long): HabitEntity? = dao.getHabitById(id)

    suspend fun getHabitsByUserSuspend(userId: Long): List<HabitEntity> =
        dao.getHabitsByUserSuspend(userId)

    suspend fun insertHabit(habit: HabitEntity): Long = dao.insertHabit(habit)

    suspend fun updateHabit(habit: HabitEntity) = dao.updateHabit(habit)

    suspend fun softDeleteHabit(id: Long) = dao.softDeleteHabit(id)

    suspend fun updateStreakCount(id: Long, count: Int) = dao.updateStreakCount(id, count)
}
