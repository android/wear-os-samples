/*
 * Copyright 2024 The Android Open Source Project
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

import androidx.activity.compose.ReportDrawnAfter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.audio.ui.VolumeViewModel
import com.google.android.horologist.audio.ui.components.actions.SetVolumeButton
import com.google.android.horologist.media.ui.material3.components.PodcastControlButtons
import com.google.android.horologist.media.ui.material3.screens.player.DefaultMediaInfoDisplay
import com.google.android.horologist.media.ui.material3.screens.player.PlayerScreen
import com.google.android.horologist.media.ui.state.PlayerUiController
import com.google.android.horologist.media.ui.state.PlayerUiState
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalHorologistApi::class)
@Composable
fun SpeakerPlayerScreen(
    onVolumeClick: () -> Unit,
    modifier: Modifier = Modifier,
    volumeViewModel: VolumeViewModel = viewModel(factory = VolumeViewModel.Factory),
    playerViewModel: SpeakerPlayerViewModel = viewModel(factory = SpeakerPlayerViewModel.Factory)
) {
    val volumeUiState by volumeViewModel.volumeUiState.collectAsStateWithLifecycle()

    PlayerScreen(
        modifier = modifier,
        background = {},
        playerViewModel = playerViewModel,
        volumeViewModel = volumeViewModel,
        mediaDisplay = { playerUiState ->
            DefaultMediaInfoDisplay(playerUiState)
        },
        buttons = { state ->
            SetVolumeButton(
                volumeUiState = volumeUiState,
                onVolumeClick = onVolumeClick,
                enabled = state.connected && state.media != null
            )
        },
        controlButtons = { playerUiController, playerUiState ->
            PlayerScreenPodcastControlButtons(playerUiController, playerUiState)
        }
    )

    ReportDrawnAfter {
        playerViewModel.playerState.filterNotNull().first()
    }
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun PlayerScreenPodcastControlButtons(
    playerUiController: PlayerUiController,
    playerUiState: PlayerUiState,
    modifier: Modifier = Modifier
) {
    PodcastControlButtons(
        modifier = modifier,
        playerController = playerUiController,
        playerUiState = playerUiState
    )
}
