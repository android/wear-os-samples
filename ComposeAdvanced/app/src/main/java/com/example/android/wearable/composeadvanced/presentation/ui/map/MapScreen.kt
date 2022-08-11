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
package com.example.android.wearable.composeadvanced.presentation.ui.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.android.wearable.composeadvanced.BuildConfig
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

/**
 * Displays a Google Map focused on Singapore.
 *
 * Get a key with https://developers.google.com/maps/documentation/android-sdk/get-api-key
 * and put it in local.properties with key `mapsApiKey`.
 */
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    onClose: (() -> Unit)? = null
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (BuildConfig.mapsApiKey.isEmpty()) {
            Text(
                modifier = Modifier.fillMaxWidth(0.7f),
                text = "Set mapsApiKey in local.properties"
            )
        } else {
            val singapore = LatLng(1.35, 103.87)
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(singapore, 10f)
            }
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                Marker(
                    state = MarkerState(position = singapore),
                    title = "Singapore",
                    snippet = "Marker in Singapore"
                )
            }
            if (onClose != null) {
                CompactChip(
                    modifier = Modifier
                        .padding(bottom = 5.dp)
                        .size(width = 50.dp, height = 24.dp)
                        .align(Alignment.BottomCenter),
                    onClick = onClose,
                    label = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "X",
                                color = MaterialTheme.colors.onSecondary,
                                style = MaterialTheme.typography.button
                            )
                        }
                    },
                    colors = ChipDefaults
                        .chipColors(backgroundColor = MaterialTheme.colors.secondary),
                    contentPadding = PaddingValues(vertical = 0.dp)
                )
            }
        }
    }
}
