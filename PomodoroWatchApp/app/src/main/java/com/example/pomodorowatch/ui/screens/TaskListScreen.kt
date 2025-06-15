package com.example.pomodorowatch.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pomodorowatch.data.Task
import com.example.pomodorowatch.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun TaskListScreen(viewModel: TaskListViewModel = hiltViewModel()) {
    val tasks by viewModel.tasks.collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        var text by remember { mutableStateOf("") }
        Row {
            TextField(value = text, onValueChange = { text = it }, modifier = Modifier.weight(1f))
            Button(onClick = { viewModel.addTask(text); text = "" }) { Text("Add") }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn {
            items(tasks) { task ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = task.completed,
                        onCheckedChange = { viewModel.toggle(task) }
                    )
                    Text(task.name)
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { viewModel.delete(task) }) {
                        Icon(Icons.Filled.Delete, contentDescription = null)
                    }
                }
            }
        }
    }
}

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    init {
        viewModelScope.launch {
            repository.getTasks().collect { _tasks.value = it }
        }
    }

    fun addTask(name: String) {
        if (name.isNotBlank()) {
            viewModelScope.launch { repository.addTask(Task(name = name)) }
        }
    }

    fun toggle(task: Task) {
        viewModelScope.launch { repository.updateTask(task.copy(completed = !task.completed)) }
    }

    fun delete(task: Task) {
        viewModelScope.launch { repository.deleteTask(task) }
    }
}
