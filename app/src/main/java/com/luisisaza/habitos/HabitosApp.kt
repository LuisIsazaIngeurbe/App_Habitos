package com.luisisaza.habitos

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.luisisaza.habitos.data.database.AppDatabase
import com.luisisaza.habitos.data.preferences.SessionManager
import com.luisisaza.habitos.data.repository.HabitLogRepository
import com.luisisaza.habitos.data.repository.HabitRepository
import com.luisisaza.habitos.data.repository.UserRepository
import com.luisisaza.habitos.domain.usecase.AnalyticsUseCase
import com.luisisaza.habitos.domain.usecase.AuthUseCase
import com.luisisaza.habitos.domain.usecase.HabitUseCase

class HabitosApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }

    val sessionManager by lazy { SessionManager(this) }

    val userRepository by lazy { UserRepository(database.userDao()) }
    val habitRepository by lazy { HabitRepository(database.habitDao()) }
    val habitLogRepository by lazy { HabitLogRepository(database.habitLogDao()) }

    val authUseCase by lazy { AuthUseCase(userRepository, sessionManager) }
    val habitUseCase by lazy { HabitUseCase(habitRepository, habitLogRepository) }
    val analyticsUseCase by lazy { AnalyticsUseCase(habitRepository, habitLogRepository) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_habits),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.notification_channel_habits_desc)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "habitos_reminders"
    }
}
