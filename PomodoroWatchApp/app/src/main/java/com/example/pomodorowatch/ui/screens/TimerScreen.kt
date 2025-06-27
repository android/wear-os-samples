package com.example.pomodorowatch.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pomodorowatch.repository.TimerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.pomodorowatch.data.TimerState
import javax.inject.Inject

@Composable
fun TimerScreen(viewModel: TimerViewModel = hiltViewModel()) {
    val state by viewModel.timerState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Remaining: ${'$'}{state.remainingMillis / 1000}s")
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Button(onClick = { viewModel.start() }) { Text("Start") }
            Spacer(modifier = Modifier.width(4.dp))
            Button(onClick = { viewModel.stop() }) { Text("Stop") }
            Spacer(modifier = Modifier.width(4.dp))
            Button(onClick = { viewModel.reset() }) { Text("Reset") }
        }
    }
}

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val repository: TimerRepository
) : ViewModel() {
    private val defaultDuration = 25 * 60 * 1000L
    private var _state = MutableStateFlow(TimerState(0, defaultDuration, false))
    val timerState: StateFlow<TimerState> = _state

    fun start() {
        if (_state.value.isRunning) return
        _state.value = _state.value.copy(isRunning = true)
        tick()
    }

    fun stop() {
        _state.value = _state.value.copy(isRunning = false)
    }

    fun reset() {
        _state.value = TimerState(0, defaultDuration, false)
    }

    private fun tick() {
        viewModelScope.launch {
            while (_state.value.isRunning && _state.value.remainingMillis > 0) {
                delay(1000)
                _state.value = _state.value.copy(remainingMillis = _state.value.remainingMillis - 1000)
            }
            if (_state.value.remainingMillis <= 0) {
                _state.value = _state.value.copy(isRunning = false)
            }
        }
    }
}
