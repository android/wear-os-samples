/*
 * Copyright (C) 2020 The Android Open Source Project
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
package com.example.android.wearable.alpha.data.db

import android.content.Context
import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.android.wearable.alpha.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// All constants are used for populating the database only.
private const val CENTER_CIRCLE_DIAMETER_FRACTION = 0.03738f
private const val OUTER_CIRCLE_STROKE_WIDTH_FRACTION = 0.00467f
private const val NUMBER_STYLE_OUTER_CIRCLE_RADIUS_FRACTION = 0.00584f

private const val GAP_BETWEEN_OUTER_CIRCLE_AND_BORDER_FRACTION = 0.03738f
private const val GAP_BETWEEN_HAND_AND_CENTER_FRACTION =
    0.01869f + CENTER_CIRCLE_DIAMETER_FRACTION / 2.0f

private const val NUMBER_RADIUS_FRACTION = 0.45f

private const val DRAW_HOUR_PIPS = true

private const val HOUR_HAND_LENGTH_FRACTION = 0.21028f
private const val HOUR_HAND_WIDTH_FRACTION = 0.02336f

private const val MINUTE_HAND_LENGTH_FRACTION = 0.3783f
private const val MINUTE_HAND_WIDTH_FRACTION = 0.0163f

private const val SECOND_HAND_LENGTH_FRACTION = 0.37383f
private const val SECOND_HAND_WIDTH_FRACTION = 0.00934f

// Used for corner roundness of the arms.
private const val ROUNDED_RECTANGLE_CORNERS_RADIUS = 1.5f
private const val SQUARE_RECTANGLE_CORNERS_RADIUS = 0.0f

// Primary key ids assigned for various style and dimension elements in the database.
// These are accessible to the watch face, so it can assign the element the user chooses in
// the watch face settings.
const val analogWatchFaceKeyId = 1

const val hourHandDimensionsKeyId = "hour_key_id"
const val minuteHandDimensionsKeyId = "minute_key_id"
const val secondHandDimensionsKeyId = "second_key_id"

const val ambientColorStyleKeyId = "ambient_style_key_id"
const val redColorStyleKeyId = "red_style_key_id"
const val greenColorStyleKeyId = "green_style_key_id"
const val blueColorStyleKeyId = "blue_style_key_id"
const val whiteColorStyleKeyId = "white_style_key_id"

private const val TAG = "WatchFaceDatabaseCallback"

/**
 * Callback used when the database is created. It's used in our case to populate the database on
 * first use with all the information you need to render a watch face.
 */
class WatchFaceDatabaseCallback(
    private val context: Context,
    private val scope: CoroutineScope
) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        WatchFaceDatabase.getDatabase(context, scope).let { database ->
            scope.launch {
                populateAnalogDatabase(
                    context = context,
                    analogWatchFaceDao = database.analogWatchFaceDao(),
                    watchFaceColorStyleDao = database.watchFaceColorStyleDao(),
                    watchFaceArmDimensionsDao = database.watchFaceArmDimensionsDao()
                )
            }
        }
    }
}

/**
 * Utility method to populate the database with default values for an analog watch face.
 */
private suspend fun populateAnalogDatabase(
    context: Context,
    analogWatchFaceDao: AnalogWatchFaceDao,
    watchFaceColorStyleDao: WatchFaceColorStyleDao,
    watchFaceArmDimensionsDao: WatchFaceArmDimensionsDao
) {

    Log.d(TAG, "populateAnalogDatabase()")

    // Deletes all content.
    deleteAllData(analogWatchFaceDao, watchFaceColorStyleDao, watchFaceArmDimensionsDao)

    // Populate Arm dimensions
    watchFaceArmDimensionsDao.insert(
        WatchFaceArmDimensionsEntity(
            id = hourHandDimensionsKeyId,
            name = context.getString(R.string.hour_arm_dimension_name),
            widthFraction = HOUR_HAND_WIDTH_FRACTION,
            lengthFraction = HOUR_HAND_LENGTH_FRACTION,
            xRadiusRoundedCorners = ROUNDED_RECTANGLE_CORNERS_RADIUS,
            yRadiusRoundedCorners = ROUNDED_RECTANGLE_CORNERS_RADIUS
        )
    )

    watchFaceArmDimensionsDao.insert(
        WatchFaceArmDimensionsEntity(
            id = minuteHandDimensionsKeyId,
            name = context.getString(R.string.minute_arm_dimension_name),
            widthFraction = MINUTE_HAND_WIDTH_FRACTION,
            lengthFraction = MINUTE_HAND_LENGTH_FRACTION,
            xRadiusRoundedCorners = ROUNDED_RECTANGLE_CORNERS_RADIUS,
            yRadiusRoundedCorners = ROUNDED_RECTANGLE_CORNERS_RADIUS
        )
    )

    watchFaceArmDimensionsDao.insert(
        WatchFaceArmDimensionsEntity(
            id = secondHandDimensionsKeyId,
            name = context.getString(R.string.second_arm_dimension_name),
            widthFraction = SECOND_HAND_WIDTH_FRACTION,
            lengthFraction = SECOND_HAND_LENGTH_FRACTION,
            xRadiusRoundedCorners = SQUARE_RECTANGLE_CORNERS_RADIUS,
            yRadiusRoundedCorners = SQUARE_RECTANGLE_CORNERS_RADIUS
        )
    )

    // Populates Ambient style.
    watchFaceColorStyleDao.insert(
        WatchFaceColorStyleEntity(
            id = ambientColorStyleKeyId,
            name = context.getString(R.string.ambient_style_name),
            iconDrawableId = R.drawable.white_style,
            complicationStyleDrawableId = R.drawable.complication_white_style,
            primaryColor = context.getColor(R.color.ambient_primary_color),
            secondaryColor = context.getColor(R.color.ambient_secondary_color),
            backgroundColor = context.getColor(R.color.ambient_background_color),
            outerElementColor = context.getColor(R.color.ambient_outer_element_color))
    )

    // Populates Styles:
    // Red Style (will be default for watch face color style).
    watchFaceColorStyleDao.insert(
        WatchFaceColorStyleEntity(
            id = redColorStyleKeyId,
            name = context.getString(R.string.red_style_name),
            iconDrawableId = R.drawable.red_style,
            complicationStyleDrawableId = R.drawable.complication_red_style,
            primaryColor = context.getColor(R.color.red_primary_color),
            secondaryColor = context.getColor(R.color.red_secondary_color),
            backgroundColor = context.getColor(R.color.red_background_color),
            outerElementColor = context.getColor(R.color.red_outer_element_color))
    )

    // Green Style
    watchFaceColorStyleDao.insert(
        WatchFaceColorStyleEntity(
            id = greenColorStyleKeyId,
            name = context.getString(R.string.green_style_name),
            iconDrawableId = R.drawable.green_style,
            complicationStyleDrawableId = R.drawable.complication_green_style,
            primaryColor = context.getColor(R.color.green_primary_color),
            secondaryColor = context.getColor(R.color.green_secondary_color),
            backgroundColor = context.getColor(R.color.green_background_color),
            outerElementColor = context.getColor(R.color.green_outer_element_color))
    )

    // Blue Style
    watchFaceColorStyleDao.insert(
        WatchFaceColorStyleEntity(
            id = blueColorStyleKeyId,
            name = context.getString(R.string.blue_style_name),
            iconDrawableId = R.drawable.blue_style,
            complicationStyleDrawableId = R.drawable.complication_blue_style,
            primaryColor = context.getColor(R.color.blue_primary_color),
            secondaryColor = context.getColor(R.color.blue_secondary_color),
            backgroundColor = context.getColor(R.color.blue_background_color),
            outerElementColor = context.getColor(R.color.blue_outer_element_color))
    )

    // White Style
    watchFaceColorStyleDao.insert(
        WatchFaceColorStyleEntity(
            id = whiteColorStyleKeyId,
            name = context.getString(R.string.white_style_name),
            iconDrawableId = R.drawable.white_style,
            complicationStyleDrawableId = R.drawable.complication_white_style,
            primaryColor = context.getColor(R.color.white_primary_color),
            secondaryColor = context.getColor(R.color.white_secondary_color),
            backgroundColor = context.getColor(R.color.white_background_color),
            outerElementColor = context.getColor(R.color.white_outer_element_color))
    )

    analogWatchFaceDao.insert(
        AnalogWatchFaceEntity(
            id = analogWatchFaceKeyId,
            name = "Analog Watch Face",
            activeColorStyleId = redColorStyleKeyId,
            ambientColorStyleId = ambientColorStyleKeyId,
            drawHourPips = DRAW_HOUR_PIPS,
            hourHandDimensionsId = hourHandDimensionsKeyId,
            minuteHandDimensionsId = minuteHandDimensionsKeyId,
            secondHandDimensionsId = secondHandDimensionsKeyId,
            centerCircleDiameterFraction = CENTER_CIRCLE_DIAMETER_FRACTION,
            numberRadiusFraction = NUMBER_RADIUS_FRACTION,
            outerCircleStokeWidthFraction = OUTER_CIRCLE_STROKE_WIDTH_FRACTION,
            numberStyleOuterCircleRadiusFraction = NUMBER_STYLE_OUTER_CIRCLE_RADIUS_FRACTION,
            gapBetweenOuterCircleAndBorderFraction = GAP_BETWEEN_OUTER_CIRCLE_AND_BORDER_FRACTION,
            gapBetweenHandAndCenterFraction = GAP_BETWEEN_HAND_AND_CENTER_FRACTION
        )
    )
}

private suspend fun deleteAllData(
    analogWatchFaceDao: AnalogWatchFaceDao,
    watchFaceColorStyleDao: WatchFaceColorStyleDao,
    watchFaceArmDimensionsDao: WatchFaceArmDimensionsDao
) {
    analogWatchFaceDao.deleteAll()
    watchFaceColorStyleDao.deleteAll()
    watchFaceArmDimensionsDao.deleteAll()
}
