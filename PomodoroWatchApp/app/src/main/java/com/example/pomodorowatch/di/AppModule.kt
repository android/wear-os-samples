package com.example.pomodorowatch.di

import android.content.Context
import androidx.room.Room
import com.example.pomodorowatch.data.AppDatabase
import com.example.pomodorowatch.repository.TaskRepository
import com.example.pomodorowatch.repository.TimerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "pomodoro.db"
        ).build()

    @Provides
    fun provideTaskRepository(db: AppDatabase): TaskRepository =
        TaskRepository(db.taskDao())

    @Provides
    fun provideTimerRepository(db: AppDatabase): TimerRepository =
        TimerRepository(db.timerStateDao())
}
