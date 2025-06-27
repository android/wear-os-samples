package com.example.pomodorowatch.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timer_state")
data class TimerState(
    @PrimaryKey val id: Int = 0,
    val remainingMillis: Long,
    val isRunning: Boolean
)
