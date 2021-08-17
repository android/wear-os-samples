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
import androidx.wear.watchface.style.UserStyleSchema
import androidx.wear.watchface.style.UserStyleSetting
import androidx.wear.watchface.style.WatchFaceLayer
import com.example.android.wearable.alpha.R
import com.example.android.wearable.alpha.data.watchface.ColorStyleIdAndResourceIds
import com.example.android.wearable.alpha.data.watchface.DRAW_HOUR_PIPS_DEFAULT
import com.example.android.wearable.alpha.data.watchface.MINUTE_HAND_LENGTH_FRACTION
import com.example.android.wearable.alpha.data.watchface.MINUTE_HAND_LENGTH_FRACTION_MAXIMUM
import com.example.android.wearable.alpha.data.watchface.MINUTE_HAND_LENGTH_FRACTION_MINIMUM

// Keys to matched content in the  the user style settings. We listen for changes to these
// values in the renderer and if new, we will update the database and update the watch face
// being rendered.
const val COLOR_STYLE_SETTING = "color_style_setting"
const val DRAW_HOUR_PIPS_STYLE_SETTING = "draw_hour_pips_style_setting"
const val WATCH_HAND_LENGTH_STYLE_SETTING = "watch_hand_length_style_setting"

// Setting key for complication display options.
const val COMPLICATIONS_STYLE_SETTING = "complication_style_setting"

// Complication constants for which complications to display.
const val NO_COMPLICATIONS = "NO_COMPLICATIONS"
const val LEFT_COMPLICATION = "LEFT_COMPLICATION"
const val RIGHT_COMPLICATION = "RIGHT_COMPLICATION"
const val LEFT_AND_RIGHT_COMPLICATIONS = "LEFT_AND_RIGHT_COMPLICATIONS"

/*
 * Creates user styles in the settings activity associated with the watch face, so users can
 * edit different parts of the watch face. In the renderer (after something has changed), the
 * watch face listens for a flow from the watch face API data layer and updates the watch face.
 */
fun createUserStyleSchema(context: Context): UserStyleSchema {
    // 1. Allows user to change the color styles of the watch face (if any are available).
    val colorStyleSetting =
        UserStyleSetting.ListUserStyleSetting(
            id = UserStyleSetting.Id(COLOR_STYLE_SETTING),
            displayName = context.getString(R.string.colors_style_setting),
            description = context.getString(R.string.colors_style_setting_description),
            icon = null,
            options = ColorStyleIdAndResourceIds.toOptionList(context),
            affectsWatchFaceLayers = listOf(
                WatchFaceLayer.BASE,
                WatchFaceLayer.COMPLICATIONS,
                WatchFaceLayer.COMPLICATIONS_OVERLAY
            )
        )

    // 2. Allows user to toggle on/off the hour pips (dashes around the outer edge of the watch
    // face).
    val drawHourPipsStyleSetting = UserStyleSetting.BooleanUserStyleSetting(
        id = UserStyleSetting.Id(DRAW_HOUR_PIPS_STYLE_SETTING),
        displayName = context.getString(R.string.watchface_pips_setting),
        description = context.getString(R.string.watchface_pips_setting_description),
        icon = null,
        defaultValue = DRAW_HOUR_PIPS_DEFAULT,
        affectsWatchFaceLayers = listOf(WatchFaceLayer.BASE)
    )

    // 3. Allows user to change the length of the minute hand.
    val watchHandLengthStyleSetting = UserStyleSetting.DoubleRangeUserStyleSetting(
        id = UserStyleSetting.Id(WATCH_HAND_LENGTH_STYLE_SETTING),
        displayName = context.getString(R.string.watchface_hand_length_setting),
        description = context.getString(R.string.watchface_hand_length_setting_description),
        icon = null,
        minimumValue = MINUTE_HAND_LENGTH_FRACTION_MINIMUM.toDouble(),
        defaultValue = MINUTE_HAND_LENGTH_FRACTION.toDouble(),
        maximumValue = MINUTE_HAND_LENGTH_FRACTION_MAXIMUM.toDouble(),
        affectsWatchFaceLayers = listOf(WatchFaceLayer.COMPLICATIONS_OVERLAY)
    )

    // 4. These are style overrides applied on top of the complicationSlots passed into
    // complicationSlotsManager below.
    val complicationsStyleSetting =
        UserStyleSetting.ComplicationSlotsUserStyleSetting(
            UserStyleSetting.Id(COMPLICATIONS_STYLE_SETTING),
            context.getString(R.string.watchface_complications_setting),
            context.getString(R.string.watchface_complications_setting_description),
            icon = null,
            complicationConfig = listOf(
                UserStyleSetting.ComplicationSlotsUserStyleSetting.ComplicationSlotsOption(
                    UserStyleSetting.Option.Id(LEFT_AND_RIGHT_COMPLICATIONS),
                    context.getString(R.string.watchface_complications_setting_both),
                    null,
                    // NB this list is empty because each [ComplicationSlotOverlay] is applied on
                    // top of the initial config.
                    listOf()
                ),
                UserStyleSetting.ComplicationSlotsUserStyleSetting.ComplicationSlotsOption(
                    UserStyleSetting.Option.Id(NO_COMPLICATIONS),
                    context.getString(R.string.watchface_complications_setting_none),
                    null,
                    listOf(
                        UserStyleSetting.ComplicationSlotsUserStyleSetting.ComplicationSlotOverlay(
                            LEFT_COMPLICATION_ID,
                            enabled = false
                        ),
                        UserStyleSetting.ComplicationSlotsUserStyleSetting.ComplicationSlotOverlay(
                            RIGHT_COMPLICATION_ID,
                            enabled = false
                        )
                    )
                ),
                UserStyleSetting.ComplicationSlotsUserStyleSetting.ComplicationSlotsOption(
                    UserStyleSetting.Option.Id(LEFT_COMPLICATION),
                    context.getString(R.string.watchface_complications_setting_left),
                    null,
                    listOf(
                        UserStyleSetting.ComplicationSlotsUserStyleSetting.ComplicationSlotOverlay(
                            RIGHT_COMPLICATION_ID,
                            enabled = false
                        )
                    )
                ),
                UserStyleSetting.ComplicationSlotsUserStyleSetting.ComplicationSlotsOption(
                    UserStyleSetting.Option.Id(RIGHT_COMPLICATION),
                    context.getString(R.string.watchface_complications_setting_right),
                    null,
                    listOf(
                        UserStyleSetting.ComplicationSlotsUserStyleSetting.ComplicationSlotOverlay(
                            LEFT_COMPLICATION_ID,
                            enabled = false
                        )
                    )
                )
            ),
            listOf(WatchFaceLayer.COMPLICATIONS)
        )

    // 5. Create style settings to hold all options.
    return UserStyleSchema(
        listOf(
            colorStyleSetting,
            drawHourPipsStyleSetting,
            watchHandLengthStyleSetting,
            complicationsStyleSetting
        )
    )
}
