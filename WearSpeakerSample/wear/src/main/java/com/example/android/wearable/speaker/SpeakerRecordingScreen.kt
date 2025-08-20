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

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales

/**
 * The composable responsible for displaying the main UI.
 *
 * This composable is stateless, and simply displays the state given to it.
 */
@Composable
fun SpeakerRecordingScreen(
    playbackState: PlaybackState,
    isPermissionDenied: Boolean,
    recordingProgress: Float,
    onMicClicked: () -> Unit,
    onPlayClicked: () -> Unit
) {
    ScreenScaffold {
        // Determine the control dashboard state.
        // This converts the main app state into a control dashboard state for rendering
        val controlDashboardUiState = computeControlDashboardUiState(
            playbackState = playbackState,
            isPermissionDenied = isPermissionDenied
        )
        ControlDashboard(
            controlDashboardUiState = controlDashboardUiState,
            onMicClicked = onMicClicked,
            onPlayClicked = onPlayClicked,
            recordingProgress = recordingProgress
        )
    }
}

private fun computeControlDashboardUiState(
    playbackState: PlaybackState,
    isPermissionDenied: Boolean
): ControlDashboardUiState =
    when (playbackState) {
        PlaybackState.PlayingVoice -> ControlDashboardUiState(
            micState = ControlDashboardButtonUiState(
                expanded = false,
                enabled = false,
                visible = false
            ),
            playState = ControlDashboardButtonUiState(
                expanded = true,
                enabled = true,
                visible = true
            )
        )
        PlaybackState.Ready -> ControlDashboardUiState(
            micState = ControlDashboardButtonUiState(
                expanded = false,
                enabled = !isPermissionDenied,
                visible = true
            ),
            playState = ControlDashboardButtonUiState(
                expanded = false,
                enabled = true,
                visible = true
            )
        )
        PlaybackState.Recording -> ControlDashboardUiState(
            micState = ControlDashboardButtonUiState(
                expanded = true,
                enabled = true,
                visible = true
            ),
            playState = ControlDashboardButtonUiState(
                expanded = false,
                enabled = false,
                visible = false
            )
        )
    }

private class PlaybackStatePreviewProvider : CollectionPreviewParameterProvider<PlaybackState>(
    listOf(
        PlaybackState.Ready,
        PlaybackState.Recording,
        PlaybackState.PlayingVoice
    )
)

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun SpeakerScreenPreview(
    @PreviewParameter(PlaybackStatePreviewProvider::class) playbackState: PlaybackState
) {
    SpeakerRecordingScreen(
        playbackState = playbackState,
        isPermissionDenied = true,
        recordingProgress = 0.25f,
        onMicClicked = {},
        onPlayClicked = {}
    )
}
