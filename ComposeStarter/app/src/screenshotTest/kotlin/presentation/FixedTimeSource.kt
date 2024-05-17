package presentation

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.TimeSource

public object FixedTimeSource : TimeSource {
    override val currentTime: String
        @Composable get() = "10:10"
}
