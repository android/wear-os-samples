package presentation

import androidx.compose.runtime.Composable
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.example.android.wearable.composestarter.presentation.SampleDialogContent
import com.google.android.horologist.compose.layout.AppScaffold
import com.google.android.horologist.compose.layout.ResponsiveTimeText

class SampleDialogScreenshots {

    @WearPreviewDevices
    @WearPreviewFontScales
    @Composable
    fun GreetingPreview() {
        AppScaffold(
            timeText = { ResponsiveTimeText(timeSource = FixedTimeSource) }
        ) {
            SampleDialogContent(onCancel = { }, onDismiss = { }, onOk = {})
        }
    }
}
