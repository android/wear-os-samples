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
package com.example.android.wearable.speaker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.Icon

/**
 * The component responsible for drawing the main 3 controls, with their expanded and minimized
 * states.
 *
 * The state for this class is driven by a [ControlDashboardUiState], which contains a
 * [ControlDashboardButtonUiState] for each of the three buttons.
 */
@Composable
fun ControlDashboard(
    controlDashboardUiState: ControlDashboardUiState,
    onMicClicked: () -> Unit,
    onPlayClicked: () -> Unit,
    recordingProgress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        // Show the progress indicator only when recording
        if (controlDashboardUiState.micState.expanded) {
            CircularProgressIndicator(
                progress = { recordingProgress },
                modifier = modifier.fillMaxSize()
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ControlDashboardButton(
                buttonState = controlDashboardUiState.micState,
                onClick = onMicClicked,
                imageVector = Icons.Filled.Mic,
                contentDescription = if (controlDashboardUiState.micState.expanded) {
                    stringResource(id = R.string.stop_recording)
                } else {
                    stringResource(id = R.string.record)
                }
            )

            ControlDashboardButton(
                buttonState = controlDashboardUiState.playState,
                onClick = onPlayClicked,
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = stringResource(id = R.string.play_recording)
            )
        }
    }
}

/**
 * A single control dashboard button
 */
@Composable
private fun ControlDashboardButton(
    buttonState: ControlDashboardButtonUiState,
    onClick: () -> Unit,
    imageVector: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Button(
        modifier = modifier,
        enabled = buttonState.enabled && buttonState.visible,
        onClick = onClick
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription
        )
    }
}

/**
 * The state for a single [ControlDashboardButton].
 */
data class ControlDashboardButtonUiState(
    val expanded: Boolean,
    val enabled: Boolean,
    val visible: Boolean
)

/**
 * The state for a [ControlDashboard].
 */
data class ControlDashboardUiState(
    val micState: ControlDashboardButtonUiState,
    val playState: ControlDashboardButtonUiState
) {
    init {
        // Check that at most one of the buttons is expanded
        require(
            listOf(
                micState.expanded,
                playState.expanded
            ).count { it } <= 1
        )
    }
}
