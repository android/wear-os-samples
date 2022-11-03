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
package com.example.wear.tiles.messaging

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.wear.compose.material.Colors

object MessagingTileTheme {
    val colors = composeColors.toTileColors()
}

/**
 * A Compose-based Colors object.
 *
 * This would typically be used in your Wear app too (and include more color overrides). Since it's
 * being used only for Tiles here, only the primary/surface colors are defined.
 */
private val composeColors = Colors(
    primary = ColorPalette.purple,
    onPrimary = ColorPalette.darkBlue,
    surface = ColorPalette.darkBlue,
    onSurface = ColorPalette.purple
)

private object ColorPalette {
    val purple = Color(0xFFC58AF9)
    val darkBlue = Color(0xFF202124)
}

private fun Colors.toTileColors() = androidx.wear.tiles.material.Colors(
    /* primary = */ primary.toArgb(),
    /* onPrimary = */ onPrimary.toArgb(),
    /* surface = */ surface.toArgb(),
    /* onSurface = */ onSurface.toArgb()
)
