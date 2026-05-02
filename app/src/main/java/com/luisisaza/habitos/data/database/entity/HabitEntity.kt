package com.luisisaza.habitos.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habits",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val name: String,
    val description: String,
    val type: String, // "GOOD" | "BAD"
    // JSON-serialized list of days: ["MON","TUE",...] — null for BAD habits
    val days: String? = null,
    val scheduleEnabled: Boolean = false,
    val scheduleStartTime: String? = null, // HH:mm
    val scheduleEndTime: String? = null,   // HH:mm
    val duration: String? = null,          // "30_MIN"|"1_HOUR"|"1_HALF"|"2_HOUR"
    val reminderEnabled: Boolean = false,
    val reminderTime: String? = null,      // HH:mm
    val reminderPhrase: String? = null,    // Only for BAD habits
    val failureUnit: String? = null,       // Only for BAD habits: e.g., "cigarrillos", "cervezas"
    val goal: String? = null,
    val streakCount: Int = 0,              // Used for BAD habits streak
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
