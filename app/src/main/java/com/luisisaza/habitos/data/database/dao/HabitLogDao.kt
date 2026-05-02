package com.luisisaza.habitos.data.database.dao

import androidx.room.*
import com.luisisaza.habitos.data.database.entity.HabitLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitLogDao {

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY date DESC")
    fun getLogsByHabit(habitId: Long): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habitId IN (:habitIds) AND date = :epochDay")
    fun getLogsForDay(habitIds: List<Long>, epochDay: Long): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habitId IN (:habitIds) AND date = :epochDay")
    suspend fun getLogsForDaySuspend(habitIds: List<Long>, epochDay: Long): List<HabitLogEntity>

    @Query("SELECT * FROM habit_logs WHERE habitId IN (:habitIds) AND date >= :startEpoch AND date <= :endEpoch ORDER BY date ASC")
    fun getLogsInRange(habitIds: List<Long>, startEpoch: Long, endEpoch: Long): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habitId IN (:habitIds) AND date >= :startEpoch AND date <= :endEpoch ORDER BY date ASC")
    suspend fun getLogsInRangeSuspend(habitIds: List<Long>, startEpoch: Long, endEpoch: Long): List<HabitLogEntity>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentLogs(habitId: Long, limit: Int): List<HabitLogEntity>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND date = :epochDay LIMIT 1")
    suspend fun getLogForHabitOnDay(habitId: Long, epochDay: Long): HabitLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HabitLogEntity): Long

    @Update
    suspend fun updateLog(log: HabitLogEntity)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId")
    suspend fun deleteLogsForHabit(habitId: Long)
}
