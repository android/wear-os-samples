package presentation

import androidx.compose.runtime.Composable
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.example.android.wearable.composestarter.presentation.GreetingScreen
import com.google.android.horologist.compose.layout.AppScaffold
import com.google.android.horologist.compose.layout.ResponsiveTimeText

class GreetingScreenScreenshots {

    @WearPreviewDevices
    @WearPreviewFontScales
    @Composable
    fun GreetingPreview() {
        AppScaffold(
            timeText = { ResponsiveTimeText(timeSource = FixedTimeSource) }
        ) {
            GreetingScreen(greetingName = "screenshot", onShowList = {})
        }
    }
}
