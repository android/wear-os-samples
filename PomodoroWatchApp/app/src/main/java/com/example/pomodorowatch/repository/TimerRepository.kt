package com.example.pomodorowatch.repository

import com.example.pomodorowatch.data.TimerState
import com.example.pomodorowatch.data.TimerStateDao
import kotlinx.coroutines.flow.Flow

class TimerRepository(private val dao: TimerStateDao) {
    fun observeState(): Flow<TimerState> = dao.observeState()
    suspend fun saveState(state: TimerState) = dao.insert(state)
    suspend fun updateState(state: TimerState) = dao.update(state)
}
