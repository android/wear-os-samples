/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.marketplace.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.placeholder
import androidx.wear.compose.material3.placeholderShimmer
import androidx.wear.compose.material3.rememberPlaceholderState
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.google.samples.marketplace.data.WatchFaceData

@Composable
fun WatchFaceButton(
    watchFaceData: WatchFaceData,
    onWatchFaceSelected: (String) -> Unit,
    isLoading: Boolean
) {
    val buttonPlaceholderState = rememberPlaceholderState(isLoading)
    val slotInfo = watchFaceData.slotInfo
    val (buttonColor, textColor) =
        if (slotInfo != null) {
            if (slotInfo.versionCode == watchFaceData.versionCode) {
                // watch face is installed and on the same version code as the slot.
                Pair(
                    MaterialTheme.colorScheme.secondaryContainer,
                    MaterialTheme.colorScheme.onSecondaryContainer
                )
            } else if (slotInfo.versionCode > watchFaceData.versionCode) {
                // watch face is a downgrade over what is on the slot.
                Pair(
                    MaterialTheme.colorScheme.errorContainer,
                    MaterialTheme.colorScheme.onErrorContainer
                )
            } else {
                // watch face is an upgrade over what is on the slot.
                Pair(
                    MaterialTheme.colorScheme.tertiary,
                    MaterialTheme.colorScheme.onTertiary
                )

            }
        } else {
            // watch face is not installed. Neutral color.
            Pair(
                MaterialTheme.colorScheme.surfaceContainer,
                MaterialTheme.colorScheme.onSurface
            )
        }
    FilledTonalButton(
        modifier = Modifier
            .fillMaxWidth()
            .placeholderShimmer(buttonPlaceholderState),
        onClick = { onWatchFaceSelected(watchFaceData.packageName) },
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
    ) {
        Text(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .placeholder(buttonPlaceholderState),
            text = watchFaceData.name,
            color = textColor
        )
    }
}

@WearPreviewDevices
@Composable
private fun WatchFaceButtonPreview() {
    val watchFaceData = WatchFaceData(
        name = "Watch Face 1",
        assetPath = "",
        packageName = "com.example.watchface1",
        versionCode = 10
    )
    WatchFaceButton(watchFaceData = watchFaceData, onWatchFaceSelected = {}, isLoading = true)
}
