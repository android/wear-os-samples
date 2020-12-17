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
data class WatchFaceColorStyleEntity (
    @PrimaryKey
    val id: String,
    val name: String,

    @ColumnInfo(name = "primary_color")
    var primaryColor: Int,

    @ColumnInfo(name = "secondary_color")
    var secondaryColor: Int,

    @ColumnInfo(name = "background_color")
    var backgroundColor: Int,

    @ColumnInfo(name = "outer_element_color")
    var outerElementColor: Int
)
