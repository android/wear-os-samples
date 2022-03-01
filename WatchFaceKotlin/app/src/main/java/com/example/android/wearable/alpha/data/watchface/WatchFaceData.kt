/*
 * Copyright 2021 The Android Open Source Project
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
package com.example.android.wearable.alpha.data.watchface

// Defaults for the watch face. All private values aren't editable by the user, so they don't need
// to be exposed as settings defaults.
const val DRAW_HOUR_PIPS_DEFAULT = true

private const val HOUR_HAND_LENGTH_FRACTION = 0.21028f
private const val HOUR_HAND_WIDTH_FRACTION = 0.02336f

// Because the minute length is something the user can edit, we make it publicly
// accessible as a default. We also specify the minimum and maximum values for the user
// settings as well.
const val MINUTE_HAND_LENGTH_FRACTION_DEFAULT = 0.3783f
const val MINUTE_HAND_LENGTH_FRACTION_MINIMUM = 0.10000f
const val MINUTE_HAND_LENGTH_FRACTION_MAXIMUM = 0.40000f
private const val MINUTE_HAND_WIDTH_FRACTION = 0.0163f

private const val SECOND_HAND_LENGTH_FRACTION = 0.37383f
private const val SECOND_HAND_WIDTH_FRACTION = 0.00934f

// Used for corner roundness of the arms.
private const val ROUNDED_RECTANGLE_CORNERS_RADIUS = 1.5f
private const val SQUARE_RECTANGLE_CORNERS_RADIUS = 0.0f

private const val CENTER_CIRCLE_DIAMETER_FRACTION = 0.03738f
private const val OUTER_CIRCLE_STROKE_WIDTH_FRACTION = 0.00467f
private const val NUMBER_STYLE_OUTER_CIRCLE_RADIUS_FRACTION = 0.00584f

private const val GAP_BETWEEN_OUTER_CIRCLE_AND_BORDER_FRACTION = 0.03738f
private const val GAP_BETWEEN_HAND_AND_CENTER_FRACTION =
    0.01869f + CENTER_CIRCLE_DIAMETER_FRACTION / 2.0f

private const val NUMBER_RADIUS_FRACTION = 0.45f

/**
 * Represents all data needed to render an analog watch face.
 */
data class WatchFaceData(
    val activeColorStyle: ColorStyleIdAndResourceIds = ColorStyleIdAndResourceIds.RED,
    val ambientColorStyle: ColorStyleIdAndResourceIds = ColorStyleIdAndResourceIds.AMBIENT,
    val drawHourPips: Boolean = DRAW_HOUR_PIPS_DEFAULT,
    val hourHandDimensions: ArmDimensions = ArmDimensions(
        lengthFraction = HOUR_HAND_LENGTH_FRACTION,
        widthFraction = HOUR_HAND_WIDTH_FRACTION,
        xRadiusRoundedCorners = ROUNDED_RECTANGLE_CORNERS_RADIUS,
        yRadiusRoundedCorners = ROUNDED_RECTANGLE_CORNERS_RADIUS
    ),
    val minuteHandDimensions: ArmDimensions = ArmDimensions(
        lengthFraction = MINUTE_HAND_LENGTH_FRACTION_DEFAULT,
        widthFraction = MINUTE_HAND_WIDTH_FRACTION,
        xRadiusRoundedCorners = ROUNDED_RECTANGLE_CORNERS_RADIUS,
        yRadiusRoundedCorners = ROUNDED_RECTANGLE_CORNERS_RADIUS
    ),
    val secondHandDimensions: ArmDimensions = ArmDimensions(
        lengthFraction = SECOND_HAND_LENGTH_FRACTION,
        widthFraction = SECOND_HAND_WIDTH_FRACTION,
        xRadiusRoundedCorners = ROUNDED_RECTANGLE_CORNERS_RADIUS,
        yRadiusRoundedCorners = ROUNDED_RECTANGLE_CORNERS_RADIUS
    ),
    val centerCircleDiameterFraction: Float = CENTER_CIRCLE_DIAMETER_FRACTION,
    val numberRadiusFraction: Float = NUMBER_RADIUS_FRACTION,
    val outerCircleStokeWidthFraction: Float = OUTER_CIRCLE_STROKE_WIDTH_FRACTION,
    val numberStyleOuterCircleRadiusFraction: Float = NUMBER_STYLE_OUTER_CIRCLE_RADIUS_FRACTION,
    val gapBetweenOuterCircleAndBorderFraction: Float =
        GAP_BETWEEN_OUTER_CIRCLE_AND_BORDER_FRACTION,
    val gapBetweenHandAndCenterFraction: Float = GAP_BETWEEN_HAND_AND_CENTER_FRACTION
)
