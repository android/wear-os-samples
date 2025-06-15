package com.example.pomodorowatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import com.example.pomodorowatch.ui.theme.PomodoroTheme
import com.example.pomodorowatch.ui.screens.TimerScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PomodoroTheme {
                TimerScreen()
            }
        }
    }
}
