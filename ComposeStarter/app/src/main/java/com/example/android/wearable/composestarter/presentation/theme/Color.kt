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
package com.example.android.wearable.composestarter.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme

val Purple200 = Color(0xFFBB86FC)
val Purple700 = Color(0xFF3700B3)
val Teal200 = Color(0xFF03DAC5)
val Red400 = Color(0xFFCF6679)

internal val wearColorPalette: ColorScheme = ColorScheme(
    primary = Purple200,
    primaryDim = Purple700,
    secondary = Teal200,
    secondaryDim = Teal200,
    error = Red400,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onError = Color.Black
)
