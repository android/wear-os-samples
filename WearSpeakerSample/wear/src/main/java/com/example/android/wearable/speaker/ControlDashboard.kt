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

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon

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
    onMusicClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val circle = Any()
    val mic = Any()
    val play = Any()
    val music = Any()

    val constraintSet = createConstraintSet(
        controlDashboardUiState = controlDashboardUiState,
        circle = circle,
        mic = mic,
        play = play,
        music = music,
    )

    // We are using ConstraintLayout here for the circular constraints
    // In general, ConstraintLayout is less necessary for Compose than it was for Views
    ConstraintLayout(
        constraintSet = constraintSet,
        modifier = modifier
    ) {
        Spacer(
            modifier = Modifier.layoutId(circle)
        )

        ControlDashboardButton(
            buttonState = controlDashboardUiState.micState,
            onClick = onMicClicked,
            layoutId = mic,
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
            layoutId = play,
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = if (controlDashboardUiState.playState.expanded) {
                stringResource(id = R.string.stop_playing_recording)
            } else {
                stringResource(id = R.string.play_recording)
            }
        )

        ControlDashboardButton(
            buttonState = controlDashboardUiState.musicState,
            onClick = onMusicClicked,
            layoutId = music,
            imageVector = Icons.Filled.MusicNote,
            contentDescription = if (controlDashboardUiState.musicState.expanded) {
                stringResource(id = R.string.stop_playing_music)
            } else {
                stringResource(id = R.string.play_music)
            }
        )
    }
}

/**
 * Creates the [ConstraintSet] for the [controlDashboardUiState].
 *
 * The [circle], [mic], [play] and [music] are used as keys for the constraints.
 */
@Composable
private fun createConstraintSet(
    controlDashboardUiState: ControlDashboardUiState,
    circle: Any,
    mic: Any,
    play: Any,
    music: Any,
): ConstraintSet {
    val iconCircleRadius = 32.dp
    val iconMinimizedSize = 48.dp
    val iconExpandedSize = 136.dp

    val micSize by animateDpAsState(
        targetValue = if (controlDashboardUiState.micState.expanded) {
            iconExpandedSize
        } else {
            iconMinimizedSize
        }
    )
    val micRadius by animateDpAsState(
        targetValue = if (controlDashboardUiState.micState.expanded) 0.dp else iconCircleRadius
    )

    val playSize by animateDpAsState(
        targetValue = if (controlDashboardUiState.playState.expanded) {
            iconExpandedSize
        } else {
            iconMinimizedSize
        }
    )
    val playRadius by animateDpAsState(
        targetValue = if (controlDashboardUiState.playState.expanded) 0.dp else iconCircleRadius
    )

    val musicSize by animateDpAsState(
        targetValue = if (controlDashboardUiState.musicState.expanded) {
            iconExpandedSize
        } else {
            iconMinimizedSize
        }
    )
    val musicRadius by animateDpAsState(
        targetValue = if (controlDashboardUiState.musicState.expanded) 0.dp else iconCircleRadius
    )

    return ConstraintSet {
        val circleRef = createRefFor(circle)
        val micRef = createRefFor(mic)
        val playRef = createRefFor(play)
        val musicRef = createRefFor(music)

        constrain(circleRef) { centerTo(parent) }
        constrain(micRef) {
            width = Dimension.value(micSize)
            height = Dimension.value(micSize)
            circular(circleRef, 0f, micRadius)
        }
        constrain(playRef) {
            width = Dimension.value(playSize)
            height = Dimension.value(playSize)
            circular(circleRef, 240f, playRadius)
        }
        constrain(musicRef) {
            width = Dimension.value(musicSize)
            height = Dimension.value(musicSize)
            circular(circleRef, 120f, musicRadius)
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
    layoutId: Any,
    imageVector: ImageVector,
    contentDescription: String
) {
    val iconPadding = 8.dp
    // TODO: Replace with a version of IconButton?
    //       https://issuetracker.google.com/issues/203123015

    val alpha by animateFloatAsState(
        targetValue = if (buttonState.visible) 1f else 0f,
    )

    Button(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
            .layoutId(layoutId),
        enabled = buttonState.enabled && buttonState.visible,
        onClick = onClick
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .padding(iconPadding)
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
    val playState: ControlDashboardButtonUiState,
    val musicState: ControlDashboardButtonUiState,
) {
    init {
        // Check that at most one of the buttons is expanded
        require(
            listOf(
                micState.expanded,
                playState.expanded,
                musicState.expanded
            ).count { it } <= 1
        )
    }
}
