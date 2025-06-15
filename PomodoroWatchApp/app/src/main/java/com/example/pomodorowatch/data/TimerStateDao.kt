package com.example.pomodorowatch.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerStateDao {
    @Query("SELECT * FROM timer_state WHERE id = 0")
    fun observeState(): Flow<TimerState>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(state: TimerState)

    @Update
    suspend fun update(state: TimerState)
}
