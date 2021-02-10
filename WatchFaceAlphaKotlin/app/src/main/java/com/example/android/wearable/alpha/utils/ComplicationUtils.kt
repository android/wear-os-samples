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
import android.graphics.RectF
import androidx.wear.complications.ComplicationBounds
import androidx.wear.complications.DefaultComplicationProviderPolicy
import androidx.wear.complications.SystemProviders
import androidx.wear.complications.data.ComplicationType
import androidx.wear.watchface.CanvasComplicationDrawable
import androidx.wear.watchface.Complication
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.complications.rendering.ComplicationDrawable
import com.example.android.wearable.alpha.R

// Information needed for complications.
// Creates bounds for the locations of both right and left complications. (This is the
// location from 0.0 - 1.0.)
// Both left and right complications use the same top and bottom bounds.
private const val LEFT_AND_RIGHT_COMPLICATIONS_TOP_BOUND = 0.4f
private const val LEFT_AND_RIGHT_COMPLICATIONS_BOTTOM_BOUND = 0.6f

private const val LEFT_COMPLICATION_LEFT_BOUND = 0.2f
private const val LEFT_COMPLICATION_RIGHT_BOUND = 0.4f

private const val RIGHT_COMPLICATION_LEFT_BOUND = 0.6f
private const val RIGHT_COMPLICATION_RIGHT_BOUND = 0.8f

private const val DEFAULT_COMPLICATION_STYLE_DRAWABLE_ID = R.drawable.complication_white_style

// Unique IDs for each complication. The settings activity that supports allowing users
// to select their complication data provider requires numbers to be >= 0.
internal const val LEFT_COMPLICATION_ID = 100
internal const val RIGHT_COMPLICATION_ID = 101

/**
 * Represents the unique id associated with a complication and the complication types it supports.
 */
sealed class ComplicationConfig(val id: Int, val supportedTypes: List<ComplicationType>) {
    object Left : ComplicationConfig(
        LEFT_COMPLICATION_ID,
        listOf(
            ComplicationType.RANGED_VALUE,
            ComplicationType.MONOCHROMATIC_IMAGE,
            ComplicationType.SHORT_TEXT,
            ComplicationType.SMALL_IMAGE
        )
    )
    object Right : ComplicationConfig(
        RIGHT_COMPLICATION_ID,
        listOf(
            ComplicationType.RANGED_VALUE,
            ComplicationType.MONOCHROMATIC_IMAGE,
            ComplicationType.SHORT_TEXT,
            ComplicationType.SMALL_IMAGE
        )
    )
}

// Utility function that initializes default complication slots (left and right).
fun createComplicationList(
    context: Context,
    watchState: WatchState,
    drawableId: Int = DEFAULT_COMPLICATION_STYLE_DRAWABLE_ID
): List<Complication> {
    // Create left Complication:
    // If not a valid drawable (XML complication color style), return empty list.
    val leftComplicationDrawable: ComplicationDrawable =
        ComplicationDrawable.getDrawable(context, drawableId) ?: return emptyList()

    val leftCanvasComplicationDrawable = CanvasComplicationDrawable(
        leftComplicationDrawable,
        watchState
    )

    val leftComplication = Complication.createRoundRectComplicationBuilder(
        id = ComplicationConfig.Left.id,
        renderer = leftCanvasComplicationDrawable,
        supportedTypes = ComplicationConfig.Left.supportedTypes,
        defaultProviderPolicy = DefaultComplicationProviderPolicy(SystemProviders.DAY_OF_WEEK),
        complicationBounds = ComplicationBounds(
            RectF(
                LEFT_COMPLICATION_LEFT_BOUND,
                LEFT_AND_RIGHT_COMPLICATIONS_TOP_BOUND,
                LEFT_COMPLICATION_RIGHT_BOUND,
                LEFT_AND_RIGHT_COMPLICATIONS_BOTTOM_BOUND
            )
        )
    ).setDefaultProviderType(ComplicationType.SHORT_TEXT)
        .build()

    // Create right Complication:
    // If not a valid drawable (XML complication color style), return empty list (want both or
    // none).
    val rightComplicationDrawable: ComplicationDrawable =
        ComplicationDrawable.getDrawable(context, drawableId) ?: return emptyList()

    val rightCanvasComplicationDrawable = CanvasComplicationDrawable(
        rightComplicationDrawable,
        watchState
    )
    val rightComplication = Complication.createRoundRectComplicationBuilder(
        id = ComplicationConfig.Right.id,
        renderer = rightCanvasComplicationDrawable,
        supportedTypes = ComplicationConfig.Right.supportedTypes,
        defaultProviderPolicy = DefaultComplicationProviderPolicy(SystemProviders.STEP_COUNT),
        complicationBounds = ComplicationBounds(
            RectF(
                RIGHT_COMPLICATION_LEFT_BOUND,
                LEFT_AND_RIGHT_COMPLICATIONS_TOP_BOUND,
                RIGHT_COMPLICATION_RIGHT_BOUND,
                LEFT_AND_RIGHT_COMPLICATIONS_BOTTOM_BOUND
            )
        )
    ).setDefaultProviderType(ComplicationType.SHORT_TEXT)
        .build()

    return listOf(leftComplication, rightComplication)
}
