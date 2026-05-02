package com.luisisaza.habitos.data.repository

import com.luisisaza.habitos.data.database.dao.HabitLogDao
import com.luisisaza.habitos.data.database.entity.HabitLogEntity
import kotlinx.coroutines.flow.Flow

class HabitLogRepository(private val dao: HabitLogDao) {

    fun getLogsByHabit(habitId: Long): Flow<List<HabitLogEntity>> = dao.getLogsByHabit(habitId)

    fun getLogsForDay(habitIds: List<Long>, epochDay: Long): Flow<List<HabitLogEntity>> =
        dao.getLogsForDay(habitIds, epochDay)

    suspend fun getLogsForDaySuspend(habitIds: List<Long>, epochDay: Long): List<HabitLogEntity> =
        dao.getLogsForDaySuspend(habitIds, epochDay)

    fun getLogsInRange(
        habitIds: List<Long>,
        startEpoch: Long,
        endEpoch: Long
    ): Flow<List<HabitLogEntity>> = dao.getLogsInRange(habitIds, startEpoch, endEpoch)

    suspend fun getLogsInRangeSuspend(
        habitIds: List<Long>,
        startEpoch: Long,
        endEpoch: Long
    ): List<HabitLogEntity> = dao.getLogsInRangeSuspend(habitIds, startEpoch, endEpoch)

    suspend fun getRecentLogs(habitId: Long, limit: Int): List<HabitLogEntity> =
        dao.getRecentLogs(habitId, limit)

    suspend fun getLogForHabitOnDay(habitId: Long, epochDay: Long): HabitLogEntity? =
        dao.getLogForHabitOnDay(habitId, epochDay)

    suspend fun upsertLog(log: HabitLogEntity): Long = dao.insertLog(log)

    suspend fun updateLog(log: HabitLogEntity) = dao.updateLog(log)
}
