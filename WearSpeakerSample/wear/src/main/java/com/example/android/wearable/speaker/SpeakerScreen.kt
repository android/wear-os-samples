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

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText

/**
 * The composable responsible for displaying the main UI.
 *
 * This composable is stateless, and simply displays the state given to it.
 */
@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun SpeakerScreen(
    playbackState: PlaybackState,
    isPermissionDenied: Boolean,
    recordingProgress: Float,
    onMicClicked: () -> Unit,
    onPlayClicked: () -> Unit,
    onMusicClicked: () -> Unit
) {
    Scaffold(
        timeText = {
            TimeText()
        }
    ) {
        // Determine the control dashboard state.
        // This converts the main app state into a control dashboard state for rendering
        val controlDashboardUiState = computeControlDashboardUiState(
            playbackState = playbackState,
            isPermissionDenied = isPermissionDenied
        )

        // The progress bar should only be visible when actively recording
        val isProgressVisible =
            when (playbackState) {
                PlaybackState.PlayingMusic,
                PlaybackState.PlayingVoice,
                is PlaybackState.Ready -> false
                PlaybackState.Recording -> true
            }

        // We are using ConstraintLayout here to center the ControlDashboard, and align the progress
        // indicator to it.
        // In general, ConstraintLayout is less necessary for Compose than it was for Views
        ConstraintLayout(
            modifier = Modifier.fillMaxSize()
        ) {
            val (controlDashboard, progressBar) = createRefs()

            ControlDashboard(
                controlDashboardUiState = controlDashboardUiState,
                onMicClicked = onMicClicked,
                onPlayClicked = onPlayClicked,
                onMusicClicked = onMusicClicked,
                modifier = Modifier
                    .constrainAs(controlDashboard) {
                        centerTo(parent)
                    }
            )

            AnimatedVisibility(
                visible = isProgressVisible,
                modifier = Modifier
                    .constrainAs(progressBar) {
                        width = Dimension.fillToConstraints
                        top.linkTo(controlDashboard.bottom, 5.dp)
                        start.linkTo(controlDashboard.start)
                        end.linkTo(controlDashboard.end)
                    }
            ) {
                LinearProgressIndicator(
                    progress = recordingProgress
                )
            }
        }
    }
}

private fun computeControlDashboardUiState(
    playbackState: PlaybackState,
    isPermissionDenied: Boolean
): ControlDashboardUiState =
    when (playbackState) {
        PlaybackState.PlayingMusic -> ControlDashboardUiState(
            micState = ControlDashboardButtonUiState(
                expanded = false,
                enabled = false,
                visible = false
            ),
            playState = ControlDashboardButtonUiState(
                expanded = false,
                enabled = false,
                visible = false
            ),
            musicState = ControlDashboardButtonUiState(
                expanded = true,
                enabled = true,
                visible = true
            )
        )
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
            ),
            musicState = ControlDashboardButtonUiState(
                expanded = false,
                enabled = false,
                visible = false
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
            ),
            musicState = ControlDashboardButtonUiState(
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
            ),
            musicState = ControlDashboardButtonUiState(
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
        PlaybackState.PlayingVoice,
        PlaybackState.PlayingMusic
    )
)

@Preview(
    device = Devices.WEAR_OS_SMALL_ROUND,
    showSystemUi = true,
    widthDp = 200,
    heightDp = 200,
    uiMode = Configuration.UI_MODE_TYPE_WATCH
)
@Composable
fun SpeakerScreenPreview(
    @PreviewParameter(PlaybackStatePreviewProvider::class) playbackState: PlaybackState
) {
    SpeakerScreen(
        playbackState = playbackState,
        isPermissionDenied = true,
        recordingProgress = 0.25f,
        onMicClicked = {},
        onPlayClicked = {},
        onMusicClicked = {}
    )
}
