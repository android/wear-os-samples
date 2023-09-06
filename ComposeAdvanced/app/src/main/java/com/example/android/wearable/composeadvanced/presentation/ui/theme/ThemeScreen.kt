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
package com.example.android.wearable.composeadvanced.presentation.ui.theme

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChipDefaults
import com.example.android.wearable.composeadvanced.presentation.theme.ThemeValues
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.material.ToggleChip
import com.google.android.horologist.compose.material.ToggleChipToggleControl

@Composable
internal fun ThemeScreen(
    columnState: ScalingLazyColumnState,
    currentlySelectedColors: Colors,
    availableThemes: List<ThemeValues>,
    onValueChange: (Colors) -> Unit
) {
    ScalingLazyColumn(
        columnState = columnState,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            ListHeader {
                Text("Color Schemes")
            }
        }
        for (listItem in availableThemes) {
            val checked = listItem.colors == currentlySelectedColors
            item {
                ToggleChip(
                    checked = checked,
                    toggleControl = ToggleChipToggleControl.Radio,
                    onCheckedChanged = { onValueChange(listItem.colors) },
                    // Override the default toggle control color to show the user the current
                    // primary selected color.
                    colors = ToggleChipDefaults.toggleChipColors(
                        checkedToggleControlColor = currentlySelectedColors.primary
                    ),
                    label = listItem.description
                )
            }
        }
    }
}
