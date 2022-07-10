package com.example.wear.tiles.golden

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.wear.tiles.R
import com.example.wear.tiles.emptyClickable
import com.example.wear.tiles.tools.WearSmallRoundDevicePreview
import com.google.android.horologist.compose.tools.LayoutRootPreview
import com.google.android.horologist.compose.tools.buildDeviceParameters
import com.google.android.horologist.tiles.images.drawableResToImageResource

@WearSmallRoundDevicePreview
@Composable
fun Goal() {
    val context = LocalContext.current
    LayoutRootPreview(
        Goal.layout(context, context.deviceParams(), steps = 5168, goal = 8000)
    )
}

@WearSmallRoundDevicePreview
@Composable
fun WorkoutButtons() {
    val context = LocalContext.current
    LayoutRootPreview(
        Workout.buttonsLayout(
            context,
            context.deviceParams(),
            weekSummary = "1 run this week",
            button1Clickable = emptyClickable,
            button2Clickable = emptyClickable,
            button3Clickable = emptyClickable,
            chipClickable = emptyClickable,
        )
    ) {
        addIdToImageMapping(
            Workout.BUTTON_1_ICON_ID,
            drawableResToImageResource(R.drawable.ic_run_24)
        )
        addIdToImageMapping(
            Workout.BUTTON_2_ICON_ID,
            drawableResToImageResource(R.drawable.ic_yoga_24)
        )
        addIdToImageMapping(
            Workout.BUTTON_3_ICON_ID,
            drawableResToImageResource(R.drawable.ic_cycling_24)
        )
    }
}

@WearSmallRoundDevicePreview
@Composable
fun WorkoutLargeChip() {
    val context = LocalContext.current
    LayoutRootPreview(
        Workout.largeChipLayout(
            context,
            context.deviceParams(),
            emptyClickable,
            title = "Power Yoga",
            chipText = "Start",
            lastWorkoutSummary = "Last session 45m"
        )
    )
}

private fun Context.deviceParams() = buildDeviceParameters(resources)
