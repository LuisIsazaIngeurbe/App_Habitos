package com.luisisaza.habitos.workers

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.luisisaza.habitos.HabitosApp
import com.luisisaza.habitos.MainActivity
import com.luisisaza.habitos.R
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class HabitReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val habitName = inputData.getString(KEY_HABIT_NAME) ?: return Result.failure()
        val habitId = inputData.getLong(KEY_HABIT_ID, -1L)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to_daily", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            habitId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, HabitosApp.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(habitName)
            .setContentText(context.getString(R.string.notification_habit_body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(habitId.toInt(), notification)
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS permission not granted
        }

        return Result.success()
    }

    companion object {
        const val KEY_HABIT_NAME = "habit_name"
        const val KEY_HABIT_ID = "habit_id"

        fun schedule(
            context: Context,
            habitId: Long,
            habitName: String,
            reminderTime: String
        ) {
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            val targetTime = LocalTime.parse(reminderTime, formatter)
            val now = LocalDateTime.now()
            var scheduledDateTime = now.toLocalDate().atTime(targetTime)

            if (!scheduledDateTime.isAfter(now)) {
                scheduledDateTime = scheduledDateTime.plusDays(1)
            }

            val delay = Duration.between(now, scheduledDateTime).toMinutes()

            val data = workDataOf(
                KEY_HABIT_ID to habitId,
                KEY_HABIT_NAME to habitName
            )

            val request = PeriodicWorkRequestBuilder<HabitReminderWorker>(1, TimeUnit.DAYS)
                .setInputData(data)
                .setInitialDelay(delay, TimeUnit.MINUTES)
                .addTag("habit_$habitId")
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "habit_reminder_$habitId",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context, habitId: Long) {
            WorkManager.getInstance(context).cancelUniqueWork("habit_reminder_$habitId")
        }
    }
}
