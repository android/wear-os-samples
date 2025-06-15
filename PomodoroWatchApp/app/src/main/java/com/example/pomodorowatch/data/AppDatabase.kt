package com.example.pomodorowatch.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Task::class, TimerState::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun timerStateDao(): TimerStateDao
}
