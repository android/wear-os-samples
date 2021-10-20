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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
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
    appState: AppState,
    isPermissionDenied: Boolean,
    recordingProgress: Float,
    onMicClicked: () -> Unit,
    onPlayClicked: () -> Unit,
    onMusicClicked: () -> Unit
) {
    Scaffold(
        modifier = Modifier.background(colorResource(id = R.color.black)),
        timeText = {
            TimeText()
        }
    ) {
        // Determine the control dashboard state.
        // This converts the main app state into a control dashboard state for rendering
        val controlDashboardState by derivedStateOf {
            when (appState) {
                AppState.PlayingMusic -> ControlDashboardState(
                    micState = ControlDashboardButtonState(expanded = false, enabled = false, visible = false),
                    playState = ControlDashboardButtonState(expanded = false, enabled = false, visible = false),
                    musicState = ControlDashboardButtonState(expanded = true, enabled = true, visible = true),
                )
                AppState.PlayingVoice -> ControlDashboardState(
                    micState = ControlDashboardButtonState(expanded = false, enabled = false, visible = false),
                    playState = ControlDashboardButtonState(expanded = true, enabled = true, visible = true),
                    musicState = ControlDashboardButtonState(expanded = false, enabled = false, visible = false),
                )
                is AppState.Ready -> ControlDashboardState(
                    micState = ControlDashboardButtonState(
                        expanded = false,
                        enabled = !isPermissionDenied,
                        visible = true
                    ),
                    playState = ControlDashboardButtonState(expanded = false, enabled = true, visible = true),
                    musicState = ControlDashboardButtonState(expanded = false, enabled = true, visible = true),
                )
                AppState.Recording -> ControlDashboardState(
                    micState = ControlDashboardButtonState(expanded = true, enabled = true, visible = true),
                    playState = ControlDashboardButtonState(expanded = false, enabled = false, visible = false),
                    musicState = ControlDashboardButtonState(expanded = false, enabled = false, visible = false),
                )
            }
        }

        // The progress bar should only be visible when actively recording
        val isProgressVisible by derivedStateOf {
            when (appState) {
                AppState.PlayingMusic,
                AppState.PlayingVoice,
                is AppState.Ready -> false
                AppState.Recording -> true
            }
        }

        ConstraintLayout(
            modifier = Modifier.fillMaxSize()
        ) {
            val (controlDashboard, progressBar) = createRefs()

            // TODO: Add animation between dashboard states when MotionLayout in Compose supports more
            //       than two constraint sets.
            ControlDashboard(
                controlDashboardState = controlDashboardState,
                onMicClicked = onMicClicked,
                onPlayClicked = onPlayClicked,
                onMusicClicked = onMusicClicked,
                modifier = Modifier
                    .constrainAs(controlDashboard) {
                        centerTo(parent)
                    }
            )

            if (isProgressVisible) {
                LinearProgressIndicator(
                    progress = recordingProgress,
                    modifier = Modifier
                        .constrainAs(progressBar) {
                            width = Dimension.fillToConstraints
                            top.linkTo(controlDashboard.bottom, 5.dp)
                            start.linkTo(controlDashboard.start)
                            end.linkTo(controlDashboard.end)
                        }
                )
            }
        }
    }
}

class AppStatePreviewProvider : CollectionPreviewParameterProvider<AppState>(
    listOf(
        AppState.Ready(
            transitionInstantly = true
        ),
        AppState.Recording,
        AppState.PlayingVoice,
        AppState.PlayingMusic
    )
)

@Preview(
    widthDp = 200,
    heightDp = 200,
    uiMode = Configuration.UI_MODE_TYPE_WATCH,
)
@Composable
fun SpeakerScreenPreview(
    @PreviewParameter(AppStatePreviewProvider::class) appState: AppState
) {
    SpeakerScreen(
        appState = appState,
        isPermissionDenied = true,
        recordingProgress = 0.25f,
        onMicClicked = {},
        onPlayClicked = {},
        onMusicClicked = {}
    )
}
