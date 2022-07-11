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
import com.example.wear.tiles.tools.WearLargeRoundDevicePreview
import com.example.wear.tiles.tools.WearSmallRoundDevicePreview
import com.google.android.horologist.compose.tools.LayoutRootPreview
import com.google.android.horologist.compose.tools.buildDeviceParameters
import com.google.android.horologist.tiles.images.drawableResToImageResource

@WearSmallRoundDevicePreview
@WearLargeRoundDevicePreview
@Composable
fun Goal() {
    val context = LocalContext.current
    LayoutRootPreview(
        Goal.layout(context, context.deviceParams(), steps = 5168, goal = 8000)
    )
}

@WearSmallRoundDevicePreview
@WearLargeRoundDevicePreview
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
@WearLargeRoundDevicePreview
@Composable
fun WorkoutLargeChip() {
    val context = LocalContext.current
    LayoutRootPreview(
        Workout.largeChipLayout(
            context,
            context.deviceParams(),
            clickable = emptyClickable,
            lastWorkoutSummary = "Last session 45m"
        )
    )
}

@WearSmallRoundDevicePreview
@WearLargeRoundDevicePreview
@Composable
fun Run() {
    val context = LocalContext.current
    LayoutRootPreview(
        Run.layout(
            context,
            context.deviceParams(),
            lastRunText = "2 days ago",
            startRunClickable = emptyClickable,
            moreChipClickable = emptyClickable
        )
    )
}

@WearSmallRoundDevicePreview
@WearLargeRoundDevicePreview
@Composable
fun Ski() {
    val context = LocalContext.current
    LayoutRootPreview(
        Ski.layout(
            context,
            stat1 = Ski.Stat("Max Spd", "46.5", "mph"),
            stat2 = Ski.Stat("Distance", "21.8", "mile")
        )
    )
}

@WearSmallRoundDevicePreview
@WearLargeRoundDevicePreview
@Composable
fun SleepTracker() {
    // TODO: yuri has an example of this one
}

/**
 * b/238548541 (internal bug - the spacing doesn't match Figma)
 */
@WearSmallRoundDevicePreview
@WearLargeRoundDevicePreview
@Composable
fun HeartRateSimple() {
    val context = LocalContext.current
    LayoutRootPreview(
        HeartRate.simpleLayout(
            context,
            context.deviceParams(),
            heartRateBpm = 86,
            clickable = emptyClickable
        )
    )
}

@WearSmallRoundDevicePreview
@WearLargeRoundDevicePreview
@Composable
fun HeartRateGraph() {
    // TODO: not trivial, ataul has dibs though please!
}

/**
 * b/238559060 (internal bug - the icon tint doesn't match Figma)
 */
@WearSmallRoundDevicePreview
@WearLargeRoundDevicePreview
@Composable
fun MeditationChips() {
    val context = LocalContext.current
    LayoutRootPreview(
        Meditation.chipsLayout(
            context,
            context.deviceParams(),
            session1 = Meditation.Session(
                label = "Breathe",
                iconId = Meditation.CHIP_1_ICON_ID,
                clickable = emptyClickable
            ),
            session2 = Meditation.Session(
                label = "Daily mindfulness",
                iconId = Meditation.CHIP_2_ICON_ID,
                clickable = emptyClickable
            ),
            browseClickable = emptyClickable
        )
    ) {
        addIdToImageMapping(
            Meditation.CHIP_1_ICON_ID,
            drawableResToImageResource(R.drawable.ic_breathe_24)
        )
        addIdToImageMapping(
            Meditation.CHIP_2_ICON_ID,
            drawableResToImageResource(R.drawable.ic_mindfulness_24)
        )
    }
}

@WearSmallRoundDevicePreview
@WearLargeRoundDevicePreview
@Composable
fun MeditationButtons() {
    val context = LocalContext.current
    LayoutRootPreview(
        Meditation.buttonsLayout(
            context,
            context.deviceParams(),
            timer1 = Meditation.Timer(minutes = 5, clickable = emptyClickable),
            timer2 = Meditation.Timer(minutes = 10, clickable = emptyClickable),
            timer3 = Meditation.Timer(minutes = 15, clickable = emptyClickable),
            clickable = emptyClickable
        )
    )
}

@WearSmallRoundDevicePreview
@WearLargeRoundDevicePreview
@Composable
fun Timer() {
    val context = LocalContext.current
    LayoutRootPreview(
        Timer.layout(
            context,
            context.deviceParams(),
            timer1 = Timer.Timer(minutes = "05", clickable = emptyClickable),
            timer2 = Timer.Timer(minutes = "10", clickable = emptyClickable),
            timer3 = Timer.Timer(minutes = "15", clickable = emptyClickable),
            timer4 = Timer.Timer(minutes = "20", clickable = emptyClickable),
            timer5 = Timer.Timer(minutes = "30", clickable = emptyClickable),
            clickable = emptyClickable
        )
    )
}

@WearSmallRoundDevicePreview
@WearLargeRoundDevicePreview
@Composable
fun Alarm() {
    val context = LocalContext.current
    LayoutRootPreview(
        Alarm.layout(
            context,
            context.deviceParams(),
            timeUntilAlarm = "Less than 1 min",
            alarmTime = "14:58",
            alarmDays = "Mon, Tue, Wed",
            clickable = emptyClickable
        )
    )
}

/**
 * b/238560022 misaligned because we can't add an offset, small preview is clipped
 */
@WearSmallRoundDevicePreview
@WearLargeRoundDevicePreview
@Composable
fun Weather() {
    val context = LocalContext.current
    LayoutRootPreview(
        Weather.layout(
            context,
            context.deviceParams(),
            location = "San Francisco",
            weatherIconId = Weather.SCATTERED_SHOWERS_ICON_ID,
            currentTemperature = "52°",
            lowTemperature = "48°",
            highTemperature = "64°",
            weatherSummary = "Showers"
        )
    ) {
        addIdToImageMapping(
            Weather.SCATTERED_SHOWERS_ICON_ID,
            drawableResToImageResource(R.drawable.scattered_showers)
        )
    }
}

/**
 * b/238556504 alignment doesn't match figma.
 */
@WearSmallRoundDevicePreview
@WearLargeRoundDevicePreview
@Composable
fun News() {
    val context = LocalContext.current
    LayoutRootPreview(
        News.layout(
            context,
            context.deviceParams(),
            headline = "Millions still without power as new storm moves across US",
            newsVendor = "The New York Times",
            clickable = emptyClickable
        )
    )
}

@WearSmallRoundDevicePreview
@WearLargeRoundDevicePreview
@Composable
fun Calendar() {
    // TODO
}

@WearSmallRoundDevicePreview
@WearLargeRoundDevicePreview
@Composable
fun Social() {
    // TODO
}

@WearSmallRoundDevicePreview
@WearLargeRoundDevicePreview
@Composable
fun Media() {
    // TODO
}

private fun Context.deviceParams() = buildDeviceParameters(resources)
