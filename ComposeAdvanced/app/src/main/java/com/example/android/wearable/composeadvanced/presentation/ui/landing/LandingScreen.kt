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
package com.example.android.wearable.composeadvanced.presentation.ui.landing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.AnchorType
import androidx.wear.compose.foundation.CurvedRow
import androidx.wear.compose.foundation.CurvedTextStyle
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.CurvedText
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import com.example.android.wearable.composeadvanced.R

/**
 * Simple landing page with two actions, view a list of watches or toggle on/off text before the
 * time.
 *
 * A text label indicates the screen shape and places it at the bottom of the screen.
 * If it's a round device, it will curve the text along the bottom curve. Otherwise, for a square
 * device, it's a regular Text composable.
 */
@Composable
fun LandingScreen(
    onClickWatchList: () -> Unit,
    proceedingTimeTextEnabled: Boolean,
    onClickProceedingTimeText: (Boolean) -> Unit,
) {

    Box(modifier = Modifier.fillMaxSize()) {

        // Places both Chips (button and toggle) in the middle of the screen.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.Center
        ) {
            CompactChip(
                onClick = onClickWatchList,
                label = {
                    Text(
                        stringResource(R.string.list_of_watches_button_label),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )

            Spacer(modifier = Modifier.size(4.dp))

            ToggleChip(
                modifier = Modifier.height(32.dp),
                checked = proceedingTimeTextEnabled,
                onCheckedChange = onClickProceedingTimeText,
                label = {
                    Text(
                        text = stringResource(R.string.proceeding_text_toggle_chip_label),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }

        // Places curved text at the bottom of round devices and straight text at the bottom of
        // non-round devices.
        if (LocalConfiguration.current.isScreenRound) {
            CurvedRow(
                // 90 degrees is bottom (6 o'clock).
                anchor = 90F,
                anchorType = AnchorType.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                CurvedText(
                    text = stringResource(R.string.watch_shape),
                    clockwise = false,
                    style = CurvedTextStyle(
                        fontSize = 18.sp,
                        color = MaterialTheme.colors.primary,
                    )
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 2.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.primary,
                    text = stringResource(R.string.watch_shape),
                    fontSize = 18.sp
                )
            }
        }
    }
}
