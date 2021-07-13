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
package com.example.android.wearable.alpha.data.watchface

import com.example.android.wearable.alpha.R

// Defaults for all styles.
// X_COLOR_STYLE_ID - id in watch face database for each style id.
// X_COLOR_STYLE_NAME_RESOURCE_ID - String name to display in the user settings UI for the style.
// X_COLOR_STYLE_ICON_ID - Icon to display in the user settings UI for the style.
const val AMBIENT_COLOR_STYLE_ID = "ambient_style_id"
const val AMBIENT_COLOR_STYLE_NAME_RESOURCE_ID = R.string.ambient_style_name
const val AMBIENT_COLOR_STYLE_ICON_ID = R.drawable.white_style

const val RED_COLOR_STYLE_ID = "red_style_id"
const val RED_COLOR_STYLE_NAME_RESOURCE_ID = R.string.red_style_name
const val RED_COLOR_STYLE_ICON_ID = R.drawable.red_style

const val GREEN_COLOR_STYLE_ID = "green_style_id"
const val GREEN_COLOR_STYLE_NAME_RESOURCE_ID = R.string.green_style_name
const val GREEN_COLOR_STYLE_ICON_ID = R.drawable.green_style

const val BLUE_COLOR_STYLE_ID = "blue_style_id"
const val BLUE_COLOR_STYLE_NAME_RESOURCE_ID = R.string.blue_style_name
const val BLUE_COLOR_STYLE_ICON_ID = R.drawable.blue_style

const val WHITE_COLOR_STYLE_ID = "white_style_id"
const val WHITE_COLOR_STYLE_NAME_RESOURCE_ID = R.string.white_style_name
const val WHITE_COLOR_STYLE_ICON_ID = R.drawable.white_style

/**
 * Represents the id (in the watch face database) and the resource ids for the color styles in the
 * watch face (including the complication color style).
 *
 * The watch face renderer (with the context), will translate these into the actual colors and
 * ComplicationDrawables to use for rendering.
 */
sealed class ColorStyleIdAndResourceIds(
    val id: String,
    val name: Int,
    val iconResourceId: Int,
    val complicationStyleDrawableId: Int,
    val primaryColorId: Int,
    val secondaryColorId: Int,
    val backgroundColorId: Int,
    val outerElementColorId: Int
) {
    object Ambient : ColorStyleIdAndResourceIds(
        id = AMBIENT_COLOR_STYLE_ID,
        name = AMBIENT_COLOR_STYLE_NAME_RESOURCE_ID,
        iconResourceId = AMBIENT_COLOR_STYLE_ICON_ID,
        complicationStyleDrawableId = R.drawable.complication_white_style,
        primaryColorId = R.color.ambient_primary_color,
        secondaryColorId = R.color.ambient_secondary_color,
        backgroundColorId = R.color.ambient_background_color,
        outerElementColorId = R.color.ambient_outer_element_color
    )

    object Red : ColorStyleIdAndResourceIds(
        id = RED_COLOR_STYLE_ID,
        name = RED_COLOR_STYLE_NAME_RESOURCE_ID,
        iconResourceId = RED_COLOR_STYLE_ICON_ID,
        complicationStyleDrawableId = R.drawable.complication_red_style,
        primaryColorId = R.color.red_primary_color,
        secondaryColorId = R.color.red_secondary_color,
        backgroundColorId = R.color.red_background_color,
        outerElementColorId = R.color.red_outer_element_color
    )

    object Green : ColorStyleIdAndResourceIds(
        id = GREEN_COLOR_STYLE_ID,
        name = GREEN_COLOR_STYLE_NAME_RESOURCE_ID,
        iconResourceId = GREEN_COLOR_STYLE_ICON_ID,
        complicationStyleDrawableId = R.drawable.complication_green_style,
        primaryColorId = R.color.green_primary_color,
        secondaryColorId = R.color.green_secondary_color,
        backgroundColorId = R.color.green_background_color,
        outerElementColorId = R.color.green_outer_element_color
    )

    object Blue : ColorStyleIdAndResourceIds(
        id = BLUE_COLOR_STYLE_ID,
        name = BLUE_COLOR_STYLE_NAME_RESOURCE_ID,
        iconResourceId = BLUE_COLOR_STYLE_ICON_ID,
        complicationStyleDrawableId = R.drawable.complication_blue_style,
        primaryColorId = R.color.blue_primary_color,
        secondaryColorId = R.color.blue_secondary_color,
        backgroundColorId = R.color.blue_background_color,
        outerElementColorId = R.color.blue_outer_element_color
    )

    object White : ColorStyleIdAndResourceIds(
        id = WHITE_COLOR_STYLE_ID,
        name = WHITE_COLOR_STYLE_NAME_RESOURCE_ID,
        iconResourceId = WHITE_COLOR_STYLE_ICON_ID,
        complicationStyleDrawableId = R.drawable.complication_white_style,
        primaryColorId = R.color.white_primary_color,
        secondaryColorId = R.color.white_secondary_color,
        backgroundColorId = R.color.white_background_color,
        outerElementColorId = R.color.white_outer_element_color
    )

    companion object {
        // Translates the string id to the correct ColorStyleIdAndResourceIds object.
        fun getColorStyleConfig(id: String): ColorStyleIdAndResourceIds {
            return when (id) {
                Ambient.id -> Ambient
                Red.id -> Red
                Green.id -> Green
                Blue.id -> Blue
                White.id -> White
                else -> White
            }
        }
    }
}
