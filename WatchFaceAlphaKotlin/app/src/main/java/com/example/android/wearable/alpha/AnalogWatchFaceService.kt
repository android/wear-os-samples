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
package com.example.android.wearable.alpha

import android.view.SurfaceHolder
import androidx.wear.watchface.Complication
import androidx.wear.watchface.WatchFace
import androidx.wear.watchface.WatchFaceService
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.style.UserStyleRepository
import androidx.wear.watchface.style.UserStyleSchema
import androidx.wear.watchface.style.UserStyleSetting
import com.example.android.wearable.alpha.data.db.WatchFaceColorStyleEntity
import com.example.android.wearable.alpha.utils.FRAME_PERIOD_MS_DEFAULT
import com.example.android.wearable.alpha.utils.createAnalogWatchFace
import com.example.android.wearable.alpha.utils.createComplicationList
import com.example.android.wearable.alpha.utils.createUserStyleSettingsList
import com.example.android.wearable.alpha.viewmodels.AnalogWatchFaceViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Handles much of the boilerplate needed to implement a watch face (minus rendering code; see
 * [AnalogWatchCanvasRenderer]) including the complications and settings (styles user can change on
 * the watch face).
 */
class AnalogWatchFaceService : WatchFaceService() {
    // Used to launch coroutines (non-blocking way to pull watch face color styles data [only
    // needed on the initial load of the user color style options]).
    private val scope: CoroutineScope = MainScope()

    private lateinit var analogWatchFaceViewModel: AnalogWatchFaceViewModel

    private var colorStylesList: List<WatchFaceColorStyleEntity>? = null

    override fun onCreate() {
        super.onCreate()

        analogWatchFaceViewModel =
            AnalogWatchFaceViewModel((application as MainApplication).repository)

        // Preloading color styles from database.
        // TODO: Move to createWatchFace() once it's a suspend function (future alpha).
        scope.launch {
            colorStylesList =
                analogWatchFaceViewModel.getAllWatchFaceColorStyles()
        }
    }

    override fun onDestroy() {
        analogWatchFaceViewModel.clear()
        // Cancels scope used for retrieving watch face styles.
        scope.cancel("AnalogWatchFaceService.onDestroy()")
        super.onDestroy()
    }

    override fun createWatchFace(surfaceHolder: SurfaceHolder, watchState: WatchState): WatchFace {
        // Creates user styles. User styles are in memory storage for user style choices which
        // allows listeners to be registered to observe style changes.
        // In our case, we have a list of color styles (populated by the database), toggling the
        // hour ticks around the watch face on/off, and adjusting the length of the minute hand.
        // Edit the util method if you want to change the UserStyleSettings.
        val userStyleSettings: List<UserStyleSetting> = createUserStyleSettingsList(
            context = this,
            colorStylesList
        )

        // Creates a set of complications for the watch face.
        // Edit the util method if you want to change the complications.
        val complications: List<Complication> = createComplicationList(
            context = applicationContext,
            watchState = watchState
        )

        return WatchFace.createAnalogWatchFace(
            surfaceHolder,
            watchState,
            applicationContext,
            analogWatchFaceViewModel,
            UserStyleRepository(UserStyleSchema(userStyleSettings.toList())),
            complications,
            FRAME_PERIOD_MS_DEFAULT
        )
    }

    companion object {
        private const val TAG = "AnalogWatchFaceService"
    }
}
