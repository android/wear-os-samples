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
package com.example.android.wearable.composeadvanced.presentation.ui.userinput

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import com.example.android.wearable.composeadvanced.R

/**
 * Displays a value by using a Stepper or a Slider
 */
@Composable
fun UserInputComponentsScreen(
    value: Int,
    onClickStepper: () -> Unit,
    onClickSlider: () -> Unit,
    onClickDemoDatePicker: () -> Unit,
    onClickDemo12hTimePicker: () -> Unit,
    onClickDemo24hTimePicker: () -> Unit,
) {
    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        item {
            CompactChip(
                onClick = onClickStepper,
                label = {
                    Text(
                        stringResource(R.string.stepper_label, value),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }

        item {
            CompactChip(
                onClick = onClickSlider,
                label = {
                    Text(
                        stringResource(R.string.slider_label, value),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }

        item {
            CompactChip(
                onClick = onClickDemoDatePicker,
                label = {
                    Text(
                        stringResource(R.string.date_picker_label),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }

        item {
            CompactChip(
                onClick = onClickDemo12hTimePicker,
                label = {
                    Text(
                        stringResource(R.string.time_12h_picker_label),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }

        item {
            CompactChip(
                onClick = onClickDemo24hTimePicker,
                label = {
                    Text(
                        stringResource(R.string.time_24h_picker_label),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }
}
