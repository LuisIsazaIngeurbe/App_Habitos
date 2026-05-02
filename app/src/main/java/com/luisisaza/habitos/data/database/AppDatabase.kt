package com.luisisaza.habitos.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.luisisaza.habitos.data.database.dao.HabitDao
import com.luisisaza.habitos.data.database.dao.HabitLogDao
import com.luisisaza.habitos.data.database.dao.UserDao
import com.luisisaza.habitos.data.database.entity.HabitEntity
import com.luisisaza.habitos.data.database.entity.HabitLogEntity
import com.luisisaza.habitos.data.database.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        HabitEntity::class,
        HabitLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habitos_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
