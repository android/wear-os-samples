package com.example.pomodorowatch.repository

import com.example.pomodorowatch.data.Task
import com.example.pomodorowatch.data.TaskDao
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val dao: TaskDao) {
    fun getTasks(): Flow<List<Task>> = dao.getTasks()
    suspend fun addTask(task: Task) = dao.insert(task)
    suspend fun updateTask(task: Task) = dao.update(task)
    suspend fun deleteTask(task: Task) = dao.delete(task)
}
