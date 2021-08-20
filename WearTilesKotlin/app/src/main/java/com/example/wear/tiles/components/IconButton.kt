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
import androidx.wear.tiles.ColorBuilders.argb
import androidx.wear.tiles.DimensionBuilders
import androidx.wear.tiles.LayoutElementBuilders.CONTENT_SCALE_MODE_FILL_BOUNDS
import androidx.wear.tiles.LayoutElementBuilders.Image
import androidx.wear.tiles.ModifiersBuilders
import androidx.wear.tiles.ModifiersBuilders.Background
import androidx.wear.tiles.ModifiersBuilders.Clickable
import androidx.wear.tiles.ModifiersBuilders.Corner
import androidx.wear.tiles.ModifiersBuilders.Modifiers
import androidx.wear.tiles.ModifiersBuilders.Padding

private val CIRCLE_SIZE = DimensionBuilders.dp(48f)
private val PADDING = DimensionBuilders.dp(12f)

fun IconButton(
    context: Context,
    resourceId: String,
    @ColorRes backgroundColor: Int,
    contentDescription: String,
    clickable: Clickable,
) = Image.Builder()
    .setResourceId(resourceId)
    .setWidth(CIRCLE_SIZE)
    .setHeight(CIRCLE_SIZE)
    .setContentScaleMode(CONTENT_SCALE_MODE_FILL_BOUNDS)
    .setModifiers(
        Modifiers.Builder()
            .setPadding(
                Padding.Builder()
                    .setBottom(PADDING)
                    .setTop(PADDING)
                    .setStart(PADDING)
                    .setEnd(PADDING)
                    .build()
            )
            .setBackground(
                Background.Builder()
                    .setColor(argb(ContextCompat.getColor(context, backgroundColor)))
                    .setCorner(Corner.Builder().setRadius(CIRCLE_SIZE).build())
                    .build()
            )
            .setClickable(clickable)
            .setSemantics(
                ModifiersBuilders.Semantics.Builder()
                    .setContentDescription(contentDescription)
                    .build()
            )
            .build()
    )
    .build()
