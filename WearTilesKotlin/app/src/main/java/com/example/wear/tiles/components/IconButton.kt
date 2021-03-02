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
package com.example.wear.tiles.components

import android.content.Context
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.wear.tiles.builders.ColorBuilders
import androidx.wear.tiles.builders.ColorBuilders.argb
import androidx.wear.tiles.builders.DimensionBuilders
import androidx.wear.tiles.builders.LayoutElementBuilders
import androidx.wear.tiles.builders.LayoutElementBuilders.CONTENT_SCALE_MODE_FILL_BOUNDS
import androidx.wear.tiles.builders.LayoutElementBuilders.Image
import androidx.wear.tiles.builders.ModifiersBuilders
import androidx.wear.tiles.builders.ModifiersBuilders.Background
import androidx.wear.tiles.builders.ModifiersBuilders.Corner
import androidx.wear.tiles.builders.ModifiersBuilders.Modifiers
import androidx.wear.tiles.builders.ModifiersBuilders.Padding

private val CIRCLE_SIZE = DimensionBuilders.dp(48f)
private val PADDING = DimensionBuilders.dp(12f)

fun IconButton(
    context: Context,
    resourceId: String,
    @ColorRes backgroundColor: Int
) = Image.builder()
    .setResourceId(resourceId)
    .setWidth(CIRCLE_SIZE)
    .setHeight(CIRCLE_SIZE)
    .setContentScaleMode(CONTENT_SCALE_MODE_FILL_BOUNDS)
    .setModifiers(
        Modifiers.builder()
            .setPadding(
                Padding.builder()
                    .setBottom(PADDING)
                    .setTop(PADDING)
                    .setStart(PADDING)
                    .setEnd(PADDING)
            )
            .setBackground(
                Background.builder()
                    .setColor(argb(ContextCompat.getColor(context, backgroundColor)))
                    .setCorner(Corner.builder().setRadius(CIRCLE_SIZE))
            )
    )
