/*
 * Copyright (C) 2020 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.wearable.alpha.utils

import android.content.Context
import android.view.SurfaceHolder
import androidx.wear.watchface.CanvasType
import androidx.wear.watchface.Complication
import androidx.wear.watchface.ComplicationsManager
import androidx.wear.watchface.WatchFace
import androidx.wear.watchface.WatchFaceType
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.style.UserStyleRepository
import com.example.android.wearable.alpha.AnalogWatchCanvasRenderer
import com.example.android.wearable.alpha.viewmodels.AnalogWatchFaceViewModel

// Default for how long each frame is displayed at expected frame rate.
const val FRAME_PERIOD_MS_DEFAULT: Long = 16L

// Creates a Analog [WatchFace] based on user
fun WatchFace.Companion.createAnalogWatchFace(
    surfaceHolder: SurfaceHolder,
    watchState: WatchState,
    context: Context,
    analogWatchFaceViewModel: AnalogWatchFaceViewModel,
    userStyleRepository: UserStyleRepository,
    complications: List<Complication>,
    framePeriodMillis: Long
): WatchFace {
    // Creates the [ComplicationsManager] (manages all watch faces).
    // Note: The [ComplicationsManager] also adds our complications as user styles to the user
    // style repository, so the user can edit them in the watch face settings.
    val complicationsManager = ComplicationsManager(
        complications,
        userStyleRepository
    )

    // Creates class that renders the watch face.
    val renderer = AnalogWatchCanvasRenderer(
        context = context,
        analogWatchFaceViewModel = analogWatchFaceViewModel,
        complications = complicationsManager.complications,
        surfaceHolder = surfaceHolder,
        userStyleRepository = userStyleRepository,
        watchState = watchState,
        canvasType = CanvasType.HARDWARE,
        framePeriodMs = framePeriodMillis
    )

    // Creates the watch face.
    return WatchFace(
        watchFaceType = WatchFaceType.ANALOG,
        userStyleRepository = userStyleRepository,
        complicationsManager = complicationsManager,
        renderer = renderer
    )
}
