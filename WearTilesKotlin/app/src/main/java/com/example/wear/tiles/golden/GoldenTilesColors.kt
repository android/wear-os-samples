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

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

object GoldenTilesColors {
    val Black = Color.Black.toArgb()
    val Blue = android.graphics.Color.parseColor("#AECBFA")
    val BlueGray = android.graphics.Color.parseColor("#2B333E")
    val DarkGray = android.graphics.Color.parseColor("#1C1B1F")
    val White = Color.White.toArgb()
    val White10Pc = Color(1f, 1f, 1f, 0.1f).toArgb()
    val Yellow = android.graphics.Color.parseColor("#FDE293")
}
