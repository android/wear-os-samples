/*
 * Copyright (C) 2021 Google Inc. All Rights Reserved.
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
package com.example.android.wearable.alpha.data

import android.util.Log
import androidx.wear.watchface.style.CurrentUserStyleRepository
import androidx.wear.watchface.style.UserStyle
import androidx.wear.watchface.style.UserStyleSetting
import com.example.android.wearable.alpha.data.watchface.DRAW_HOUR_PIPS_DEFAULT
import com.example.android.wearable.alpha.data.watchface.MINUTE_HAND_LENGTH_FRACTION
import com.example.android.wearable.alpha.data.watchface.RED_COLOR_STYLE_ID
import com.example.android.wearable.alpha.utils.COLOR_STYLE_SETTING
import com.example.android.wearable.alpha.utils.DRAW_HOUR_PIPS_STYLE_SETTING
import com.example.android.wearable.alpha.utils.WATCH_HAND_LENGTH_STYLE_SETTING
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Data Source for all changes by the user in settings related to the watch face styles. Coverts
 * the watch face api callback to a flow.
 */
class WatchFaceUserSettingChangesDataSource(
    private val currentUserStyleRepository: CurrentUserStyleRepository
) {
    @ExperimentalCoroutinesApi
    fun getUserWatchFaceChanges(): Flow<WatchFaceUserChanges> = callbackFlow {
        val userStyleChangeListener = object : CurrentUserStyleRepository.UserStyleChangeListener {
            override fun onUserStyleChanged(userStyle: UserStyle) {
                Log.d(TAG, "onUserStyleChanged(): userStyle: $userStyle")

                var newWatchFaceUserChangesChanges: WatchFaceUserChanges = WatchFaceUserChanges()

                for (options in userStyle.selectedOptions) {
                    when (options.key.id.toString()) {
                        COLOR_STYLE_SETTING -> {
                            val listOption = options.value as
                                    UserStyleSetting.ListUserStyleSetting.ListOption

                            newWatchFaceUserChangesChanges = newWatchFaceUserChangesChanges.copy(
                                colorId = listOption.id.toString()
                            )
                        }
                        DRAW_HOUR_PIPS_STYLE_SETTING -> {
                            val booleanValue = options.value as
                                    UserStyleSetting.BooleanUserStyleSetting.BooleanOption

                            newWatchFaceUserChangesChanges = newWatchFaceUserChangesChanges.copy(
                                pipsEnabled = booleanValue.value
                            )
                        }
                        WATCH_HAND_LENGTH_STYLE_SETTING -> {
                            val doubleValue = options.value as
                                    UserStyleSetting.DoubleRangeUserStyleSetting.DoubleRangeOption

                            newWatchFaceUserChangesChanges = newWatchFaceUserChangesChanges.copy(
                                minuteHandLength = doubleValue.value.toFloat()
                            )
                        }
                        // TODO (codingjeremy): Add complication change support if settings activity
                        // PR doesn't cover it. Otherwise, remove comment.
                    }
                }

                try {
                    trySend(newWatchFaceUserChangesChanges).isSuccess
                } catch (error: Throwable) {
                    Log.d(TAG, "error sending Flow: $error")
                }
            }
        }

        currentUserStyleRepository.addUserStyleChangeListener(userStyleChangeListener)

        // The callback inside awaitClose will be executed when the flow is
        // either closed or cancelled.
        // In this case, remove the CurrentUserStyleRepository.UserStyleChangeListener() callback.
        awaitClose {
            Log.d(TAG, "awaitClose{ }")
            currentUserStyleRepository.removeUserStyleChangeListener(userStyleChangeListener)
        }
    }

    data class WatchFaceUserChanges(
        val colorId: String = RED_COLOR_STYLE_ID,
        val pipsEnabled: Boolean = DRAW_HOUR_PIPS_DEFAULT,
        val minuteHandLength: Float = MINUTE_HAND_LENGTH_FRACTION
    )

    companion object {
        const val TAG = "UserConfigurableWatchFaceDataSource"
    }
}
