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
import android.graphics.drawable.Icon
import androidx.wear.watchface.style.Layer
import androidx.wear.watchface.style.UserStyleSetting
import com.example.android.wearable.alpha.R
import com.example.android.wearable.alpha.data.db.WatchFaceColorStyleEntity

// Keys to matched content in the  the user style settings. We listen for changes to these
// values in the renderer and if new, we will update the database and update the watch face
// being rendered.
const val COLOR_STYLE_SETTING = "color_style_setting"
const val DRAW_HOUR_PIPS_STYLE_SETTING = "draw_hour_pips_style_setting"
const val WATCH_HAND_LENGTH_STYLE_SETTING = "watch_hand_length_style_setting"

/*
 * Creates user styles in the settings activity associated with the watch face, so users can
 * edit different parts of the watch face. In the renderer (after something has changed), the
 * watch face is updated and the data is saved to the database.
 */
fun createUserStyleSettingsList(
    context: Context,
    styles: List<WatchFaceColorStyleEntity>?
): List<UserStyleSetting> {
    // 1. Allows user to change the color styles of the watch face (if any are available).
    var colorStyleSetting: UserStyleSetting.ListUserStyleSetting? = null
    val options: MutableList<UserStyleSetting.ListUserStyleSetting.ListOption> =
        mutableListOf()

    // If color styles are available, add them as options in the settings.
    if (styles is List<WatchFaceColorStyleEntity>) {
        for (style in styles) {
            options.add(
                UserStyleSetting.ListUserStyleSetting.ListOption(
                    id = style.id,
                    displayName = style.name,
                    icon = Icon.createWithResource(context, style.iconDrawableId)
                )
            )
        }

        if (options.size > 0) {
            colorStyleSetting = UserStyleSetting.ListUserStyleSetting(
                id = COLOR_STYLE_SETTING,
                displayName = context.getString(R.string.colors_style_setting),
                description = context.getString(R.string.colors_style_setting_description),
                icon = null,
                options = options,
                affectsLayers = listOf(Layer.BASE_LAYER, Layer.COMPLICATIONS, Layer.TOP_LAYER)
            )
        }
    }

    // 2. Allows user to toggle on/off the hour pips (dashes around the outer edge of the watch
    // face).
    val drawHourPipsStyleSetting = UserStyleSetting.BooleanUserStyleSetting(
        id = DRAW_HOUR_PIPS_STYLE_SETTING,
        displayName = context.getString(R.string.watchface_pips_setting),
        description = context.getString(R.string.watchface_pips_setting_description),
        icon = null,
        defaultValue = true,
        affectsLayers = listOf(Layer.BASE_LAYER)
    )

    // 3. Allows user to change the length of the minute hand.
    val watchHandLengthStyleSetting = UserStyleSetting.DoubleRangeUserStyleSetting(
        id = WATCH_HAND_LENGTH_STYLE_SETTING,
        displayName = context.getString(R.string.watchface_hand_length_setting),
        description = context.getString(R.string.watchface_hand_length_setting_description),
        icon = null,
        minimumValue = 0.10000,
        defaultValue = 0.37383,
        maximumValue = 0.40000,
        affectsLayers = listOf(Layer.TOP_LAYER)
    )

    // Create style settings to hold all options.
    val userStyleSettings: MutableList<UserStyleSetting> = mutableListOf()

    if (options.isNotEmpty()) {
        userStyleSettings.add(colorStyleSetting!!)
    }

    userStyleSettings.add(drawHourPipsStyleSetting)
    userStyleSettings.add(watchHandLengthStyleSetting)

    return userStyleSettings
}
