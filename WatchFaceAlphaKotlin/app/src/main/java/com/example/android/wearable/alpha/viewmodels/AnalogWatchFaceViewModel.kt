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
package com.example.android.wearable.alpha.viewmodels

import androidx.lifecycle.asLiveData
import androidx.wear.watchface.style.UserStyle
import com.example.android.wearable.alpha.AnalogWatchFaceService
import com.example.android.wearable.alpha.data.WatchFaceRepository
import com.example.android.wearable.alpha.data.db.AnalogWatchFaceEntity
import com.example.android.wearable.alpha.data.db.WatchFaceArmDimensionsEntity
import com.example.android.wearable.alpha.utils.COLOR_STYLE_SETTING
import com.example.android.wearable.alpha.utils.DRAW_HOUR_PIPS_STYLE_SETTING
import com.example.android.wearable.alpha.utils.WATCH_HAND_LENGTH_STYLE_SETTING
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * View model used for [AnalogWatchFaceService].
 */
class AnalogWatchFaceViewModel(private val repository: WatchFaceRepository) {

    // Used to launch coroutines (non-blocking way to insert data).
    private val scope: CoroutineScope = MainScope()

    // [AnalogWatchFaceEntity] operations:
    fun getAnalogWatchFaceAndStylesAndDimensions(analogWatchFaceId: Int) =
        repository.getAnalogWatchFaceAndStylesAndDimensions(analogWatchFaceId).asLiveData()

    private suspend fun getAnalogWatchFace(analogWatchFaceId: Int) =
        repository.getAnalogWatchFace(analogWatchFaceId)

    // Launches a new coroutine to insert the data in a non-blocking way
    private fun updateAnalogWatchFace(analogWatchFaceEntity: AnalogWatchFaceEntity) = scope.launch {
        repository.updateAnalogWatchFace(analogWatchFaceEntity)
    }

    // [WatchFaceColorStyleEntity] operations:
    suspend fun getAllWatchFaceColorStyles() =
        repository.getAllWatchFaceColorStyles()

    // [WatchFaceArmDimensionsEntity] operations:
    private suspend fun getWatchFaceArmDimensions(watchFaceArmDimensionsId: String) =
        repository.getWatchFaceArmDimensions(watchFaceArmDimensionsId)

    // Launches a new coroutine to insert the data in a non-blocking way
    private fun updateWatchFaceArmDimensions(watchFaceArmDimensionsEntity: WatchFaceArmDimensionsEntity) = scope.launch {
        repository.updateWatchFaceArmDimensions(watchFaceArmDimensionsEntity)
    }

    // Updates multiple table entries if the user changes something in the settings.
    fun updateUserStylesInDatabase(analogWatchFaceId: Int, userStyle: UserStyle) = scope.launch {
        // Returns if a valid [AnalogWatchFaceEntity] isn't returned for the id.
        val analogWatchFaceEntity = getAnalogWatchFace(analogWatchFaceId) ?: return@launch

        updateColorStyleAndHourPips(userStyle, analogWatchFaceEntity)
        updateMinuteArmLength(userStyle, analogWatchFaceEntity.minuteHandDimensionsId)
    }

    // Updates the color style and hour hand pips (ticks on the outside of the watch) in the
    // associated [AnalogWatchFaceEntity] in the database.
    private suspend fun updateColorStyleAndHourPips(
        userStyle: UserStyle,
        analogWatchFaceEntity: AnalogWatchFaceEntity
    ) {
        var revisedAnalogWatchFaceEntity = analogWatchFaceEntity

        // 1. Updates color style associated with the watch face.
        val newColorStyleSetting: String =
            userStyle.toMap()[COLOR_STYLE_SETTING]
                ?: revisedAnalogWatchFaceEntity.activeColorStyleId

        if (revisedAnalogWatchFaceEntity.activeColorStyleId != newColorStyleSetting) {
            revisedAnalogWatchFaceEntity =
                revisedAnalogWatchFaceEntity.copy(activeColorStyleId = newColorStyleSetting)
        }

        // 2. Updates Hour Pips (dashes around the outside of the watch).
        val newHoursPipsSettingStringVersion: String =
            userStyle.toMap()[DRAW_HOUR_PIPS_STYLE_SETTING] ?: ""

        val newHoursPipsSetting =
            if (newHoursPipsSettingStringVersion.isEmpty()) {
                revisedAnalogWatchFaceEntity.drawHourPips
            } else {
                newHoursPipsSettingStringVersion.toBoolean()
            }

        if (revisedAnalogWatchFaceEntity.drawHourPips != newHoursPipsSetting) {
            revisedAnalogWatchFaceEntity =
                revisedAnalogWatchFaceEntity.copy(drawHourPips = newHoursPipsSetting)
        }

        updateAnalogWatchFace(revisedAnalogWatchFaceEntity)
    }

    // Updates the length of the minute hand arm dimensions in the associated
    // [WatchFaceArmDimensionsEntity] in the database.
    private suspend fun updateMinuteArmLength(
        userStyle: UserStyle,
        minuteHandDimensionsId: String
    ) {
        val newHandLengthSettingStringVersion =
            userStyle.toMap()[WATCH_HAND_LENGTH_STYLE_SETTING] ?: ""

        if (newHandLengthSettingStringVersion.isNotEmpty()) {
            val newMinuteHandLength = newHandLengthSettingStringVersion.toFloat()

            val minuteWatchFaceArmDimensions: WatchFaceArmDimensionsEntity =
                getWatchFaceArmDimensions(minuteHandDimensionsId)

            if (minuteWatchFaceArmDimensions.lengthFraction != newMinuteHandLength) {
                updateWatchFaceArmDimensions(
                    minuteWatchFaceArmDimensions.copy(lengthFraction = newMinuteHandLength)
                )
            }
        }
    }

    fun clear() {
        scope.cancel("AnalogWatchFaceViewModel.clear() request")
    }
}
