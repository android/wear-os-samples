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
import com.example.wear.tiles.tools.WearLargeRoundDevicePreview
import com.example.wear.tiles.tools.WearSmallRoundDevicePreview
import com.example.wear.tiles.tools.emptyClickable
import com.google.android.horologist.compose.tools.LayoutRootPreview
import com.google.android.horologist.compose.tools.buildDeviceParameters
import com.google.android.horologist.tiles.images.drawableResToImageResource

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

private fun Context.deviceParams() = buildDeviceParameters(resources)
