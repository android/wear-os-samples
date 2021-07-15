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

import android.content.Context
import androidx.wear.watchface.complications.rendering.ComplicationDrawable

/**
 * Color and drawable resources needed to render the watch face. Translated from
 * [ColorStyleIdAndResourceIds] constant ids to actual resources with context at run time.
 *
 * This is only needed when the watch face is active.
 */
data class WatchFaceColorPalette(
    val activePrimaryColor: Int,
    val activeSecondaryColor: Int,
    val activeBackgroundColor: Int,
    val activeOuterElementColor: Int,
    val complicationStyleDrawable: ComplicationDrawable,
    val ambientPrimaryColor: Int,
    val ambientSecondaryColor: Int,
    val ambientBackgroundColor: Int,
    val ambientOuterElementColor: Int
) {
    companion object {
        /**
         * Converts [ColorStyleIdAndResourceIds] to [WatchFaceColorPalette].
         */
        fun convertToWatchFaceColorPalette(
            context: Context,
            activeColorStyle: ColorStyleIdAndResourceIds,
            ambientColorStyle: ColorStyleIdAndResourceIds
        ): WatchFaceColorPalette {

            return WatchFaceColorPalette(
                // Active colors
                activePrimaryColor = context.getColor(activeColorStyle.primaryColorId),
                activeSecondaryColor = context.getColor(activeColorStyle.secondaryColorId),
                activeBackgroundColor = context.getColor(activeColorStyle.backgroundColorId),
                activeOuterElementColor = context.getColor(activeColorStyle.outerElementColorId),
                // Complication color style
                complicationStyleDrawable = ComplicationDrawable.getDrawable(
                    context,
                    activeColorStyle.complicationStyleDrawableId
                )!!,
                // Ambient colors
                ambientPrimaryColor = context.getColor(ambientColorStyle.primaryColorId),
                ambientSecondaryColor = context.getColor(ambientColorStyle.secondaryColorId),
                ambientBackgroundColor = context.getColor(ambientColorStyle.backgroundColorId),
                ambientOuterElementColor = context.getColor(ambientColorStyle.outerElementColorId)
            )
        }
    }
}
