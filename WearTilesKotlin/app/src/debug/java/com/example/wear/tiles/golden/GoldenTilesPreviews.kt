/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

@WearSmallRoundDevicePreview
@Composable
fun Run() {
    // TODO
}

@WearSmallRoundDevicePreview
@Composable
fun Ski() {
    // TODO
}

@WearSmallRoundDevicePreview
@Composable
fun SleepTracker() {
    // TODO
}

@WearSmallRoundDevicePreview
@Composable
fun HeartRateSimple() {
    // TODO
}

@WearSmallRoundDevicePreview
@Composable
fun HeartRateGraph() {
    // TODO
}

@WearSmallRoundDevicePreview
@Composable
fun MeditationChips() {
    // TODO
}

@WearSmallRoundDevicePreview
@Composable
fun MeditationButtons() {
    // TODO
}

@WearSmallRoundDevicePreview
@Composable
fun Timer() {
    // TODO
}

@WearSmallRoundDevicePreview
@Composable
fun Alarm() {
    // TODO
}

@WearSmallRoundDevicePreview
@Composable
fun Weather() {
    // TODO
}

@WearSmallRoundDevicePreview
@Composable
fun News() {
    // TODO
}

@WearSmallRoundDevicePreview
@Composable
fun Calendar() {
    // TODO
}

@WearSmallRoundDevicePreview
@Composable
fun Social() {
    // TODO
}

@WearSmallRoundDevicePreview
@Composable
fun Media() {
    // TODO
}

private fun Context.deviceParams() = buildDeviceParameters(resources)
