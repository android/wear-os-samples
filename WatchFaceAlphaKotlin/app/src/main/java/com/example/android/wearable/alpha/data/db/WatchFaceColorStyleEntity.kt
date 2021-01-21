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
import androidx.room.PrimaryKey

/**
 * Data class for watch face color styles.
 */
@Entity(tableName = "watch_face_color_style_table")
data class WatchFaceColorStyleEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "icon_drawable_id")
    val iconDrawableId: Int,

    @ColumnInfo(name = "complication_style_drawable_id")
    val complicationStyleDrawableId: Int,

    @ColumnInfo(name = "primary_color")
    val primaryColor: Int,

    @ColumnInfo(name = "secondary_color")
    val secondaryColor: Int,

    @ColumnInfo(name = "background_color")
    val backgroundColor: Int,

    @ColumnInfo(name = "outer_element_color")
    val outerElementColor: Int
)
