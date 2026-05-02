package com.luisisaza.habitos.data.database.dao

import androidx.room.*
import com.luisisaza.habitos.data.database.entity.HabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits WHERE userId = :userId AND isActive = 1 ORDER BY createdAt ASC")
    fun getHabitsByUser(userId: Long): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE userId = :userId AND type = :type AND isActive = 1 ORDER BY createdAt ASC")
    fun getHabitsByType(userId: Long, type: String): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Long): HabitEntity?

    @Query("SELECT * FROM habits WHERE userId = :userId AND isActive = 1 ORDER BY createdAt ASC")
    suspend fun getHabitsByUserSuspend(userId: Long): List<HabitEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Query("UPDATE habits SET isActive = 0 WHERE id = :id")
    suspend fun softDeleteHabit(id: Long)

    @Query("UPDATE habits SET streakCount = :count WHERE id = :id")
    suspend fun updateStreakCount(id: Long, count: Int)
}
