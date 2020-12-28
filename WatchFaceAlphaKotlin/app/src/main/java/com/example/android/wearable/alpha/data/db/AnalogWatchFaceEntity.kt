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

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.NO_ACTION
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Data class for analog watch faces.
 */
@Entity(
    tableName = "analog_watch_face_table",
    foreignKeys = [
        ForeignKey(
            entity = WatchFaceColorStyleEntity::class,
            parentColumns = ["id"],
            childColumns = ["active_color_style_id"],
            onDelete = NO_ACTION
        ),
        ForeignKey(
            entity = WatchFaceColorStyleEntity::class,
            parentColumns = ["id"],
            childColumns = ["ambient_color_style_id"],
            onDelete = NO_ACTION
        ),
        ForeignKey(
            entity = WatchFaceArmDimensionsEntity::class,
            parentColumns = ["id"],
            childColumns = ["hour_hand_dimensions_id"],
            onDelete = NO_ACTION
        ),
        ForeignKey(
            entity = WatchFaceArmDimensionsEntity::class,
            parentColumns = ["id"],
            childColumns = ["minute_hand_dimensions_id"],
            onDelete = NO_ACTION
        ),
        ForeignKey(
            entity = WatchFaceArmDimensionsEntity::class,
            parentColumns = ["id"],
            childColumns = ["second_hand_dimensions_id"],
            onDelete = NO_ACTION
        )
    ],
    indices = [
        Index("active_color_style_id"),
        Index("ambient_color_style_id"),
        Index("hour_hand_dimensions_id"),
        Index("minute_hand_dimensions_id"),
        Index("second_hand_dimensions_id")
    ]
)
data class AnalogWatchFaceEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "active_color_style_id")
    val activeColorStyleId: String,

    @ColumnInfo(name = "ambient_color_style_id")
    val ambientColorStyleId: String,

    @ColumnInfo(name = "complication_drawable_style_id")
    val complicationDrawableStyleId: Int,

    @ColumnInfo(name = "hour_hand_dimensions_id")
    val hourHandDimensionsId: String,

    @ColumnInfo(name = "minute_hand_dimensions_id")
    val minuteHandDimensionsId: String,

    @ColumnInfo(name = "second_hand_dimensions_id")
    val secondHandDimensionsId: String,

    @ColumnInfo(name = "center_circle_diameter_fraction")
    val centerCircleDiameterFraction: Float,

    @ColumnInfo(name = "number_radius_fraction")
    val numberRadiusFraction: Float,

    @ColumnInfo(name = "outer_circle_stoke_width_fraction")
    val outerCircleStokeWidthFraction: Float,

    @ColumnInfo(name = "number_style_outer_circle_radius_fraction")
    val numberStyleOuterCircleRadiusFraction: Float,

    @ColumnInfo(name = "gap_between_outer_circle_and_border_fraction")
    val gapBetweenOuterCircleAndBorderFraction: Float,

    @ColumnInfo(name = "gap_between_hand_and_center_fraction")
    val gapBetweenHandAndCenterFraction: Float
)
