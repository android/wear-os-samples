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
import android.content.res.Resources
import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.android.wearable.alpha.R
import kotlin.random.Random
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

private const val HOUR_HAND_LENGTH_FRACTION = 0.21028f
private const val HOUR_HAND_WIDTH_FRACTION = 0.02336f

private const val MINUTE_HAND_LENGTH_FRACTION = 0.3783f
private const val MINUTE_HAND_WIDTH_FRACTION = 0.0163f

private const val SECOND_HAND_LENGTH_FRACTION = 0.37383f
private const val SECOND_HAND_WIDTH_FRACTION = 0.00934f

// Primary key ids assigned for various style and dimension elements in the database.
// These are accessible to the watch face, so it can assign the element the user chooses in
// the watch face settings.
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
suspend fun populateAnalogDatabase(
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
            name = Resources.getSystem().getString(R.string.minute_arm_dimension_name),
            widthFraction = HOUR_HAND_WIDTH_FRACTION,
            lengthFraction = HOUR_HAND_LENGTH_FRACTION
        )
    )

    watchFaceArmDimensionsDao.insert(
        WatchFaceArmDimensionsEntity(
            id = minuteHandDimensionsKeyId,
            name = Resources.getSystem().getString(R.string.minute_arm_dimension_name),
            widthFraction = MINUTE_HAND_WIDTH_FRACTION,
            lengthFraction = MINUTE_HAND_LENGTH_FRACTION
        )
    )

    watchFaceArmDimensionsDao.insert(
        WatchFaceArmDimensionsEntity(
            id = secondHandDimensionsKeyId,
            name = Resources.getSystem().getString(R.string.minute_arm_dimension_name),
            widthFraction = SECOND_HAND_WIDTH_FRACTION,
            lengthFraction = SECOND_HAND_LENGTH_FRACTION
        )
    )

    // Populates Ambient style.
    watchFaceColorStyleDao.insert(
        WatchFaceColorStyleEntity(
            id = ambientColorStyleKeyId,
            name = Resources.getSystem().getString(R.string.ambient_style_name),
            primaryColor = R.color.ambient_primary_color,
            secondaryColor = R.color.ambient_secondary_color,
            backgroundColor = R.color.ambient_background_color,
            outerElementColor = R.color.ambient_outer_element_color)
    )

    // Populates Styles:
    // Red Style (will be default for watch face color style).
    watchFaceColorStyleDao.insert(
        WatchFaceColorStyleEntity(
            id = redColorStyleKeyId,
            name = Resources.getSystem().getString(R.string.red_style_name),
            primaryColor = R.color.red_primary_color,
            secondaryColor = R.color.red_secondary_color,
            backgroundColor = R.color.red_background_color,
            outerElementColor = R.color.red_outer_element_color)
    )

    // Green Style
    watchFaceColorStyleDao.insert(
        WatchFaceColorStyleEntity(
            id = greenColorStyleKeyId,
            name = Resources.getSystem().getString(R.string.green_style_name),
            primaryColor = R.color.green_primary_color,
            secondaryColor = R.color.green_secondary_color,
            backgroundColor = R.color.green_background_color,
            outerElementColor = R.color.green_outer_element_color)
    )

    // Blue Style
    watchFaceColorStyleDao.insert(
        WatchFaceColorStyleEntity(
            id = blueColorStyleKeyId,
            name = Resources.getSystem().getString(R.string.blue_style_name),
            primaryColor = R.color.blue_primary_color,
            secondaryColor = R.color.blue_secondary_color,
            backgroundColor = R.color.blue_background_color,
            outerElementColor = R.color.blue_outer_element_color)
    )

    // White Style
    watchFaceColorStyleDao.insert(
        WatchFaceColorStyleEntity(
            id = whiteColorStyleKeyId,
            name = Resources.getSystem().getString(R.string.white_style_name),
            primaryColor = R.color.white_primary_color,
            secondaryColor = R.color.white_secondary_color,
            backgroundColor = R.color.white_background_color,
            outerElementColor = R.color.white_outer_element_color)
    )

    analogWatchFaceDao.insert(
        AnalogWatchFaceEntity(
            id = Random.nextInt(0, 100000),
            name = "Analog Watch Face",
            activeColorStyleId = redColorStyleKeyId,
            ambientColorStyleId = ambientColorStyleKeyId,
            complicationDrawableStyleId = R.drawable.complication_red_style,
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
