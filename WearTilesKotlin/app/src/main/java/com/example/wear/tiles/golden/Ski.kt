/*
 * Copyright 2022 The Android Open Source Project
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
package com.example.wear.tiles.golden

import android.content.Context
import androidx.wear.tiles.ColorBuilders
import androidx.wear.tiles.DimensionBuilders
import androidx.wear.tiles.LayoutElementBuilders
import androidx.wear.tiles.material.Text
import androidx.wear.tiles.material.Typography
import androidx.wear.tiles.material.layouts.MultiSlotLayout

object Ski {

    fun layout(context: Context, stat1: Stat, stat2: Stat) = LayoutElementBuilders.Box.Builder()
        .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
        .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
        .setHeight(DimensionBuilders.ExpandedDimensionProp.Builder().build())
        .setWidth(DimensionBuilders.ExpandedDimensionProp.Builder().build())
        .addContent(
            MultiSlotLayout.Builder()
                .addSlotContent(statColumn(context, stat1))
                .addSlotContent(statColumn(context, stat2))
                .build()
        )
        .build()

    private fun statColumn(context: Context, stat: Stat) = LayoutElementBuilders.Column.Builder()
        .addContent(
            Text.Builder(context, stat.label)
                .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                .setColor(ColorBuilders.argb(GoldenTilesColors.LightBlue))
                .build()
        )
        .addContent(
            Text.Builder(context, stat.value)
                .setTypography(Typography.TYPOGRAPHY_DISPLAY3)
                .setColor(ColorBuilders.argb(GoldenTilesColors.White))
                .build()
        )
        .addContent(
            Text.Builder(context, stat.unit)
                .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                .setColor(ColorBuilders.argb(GoldenTilesColors.White))
                .build()
        )
        .build()

    data class Stat(val label: String, val value: String, val unit: String)
}
