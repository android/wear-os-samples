package com.example.android.wearable.speaker

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.LinearProgressIndicator
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
import androidx.wear.compose.material.Scaffold

/**
 * The composable responsible for displaying the main UI.
 *
 * This composable is stateless, and simply displays the state given to it.
 */
@Composable
fun SpeakerScreen(
    appState: MainStateHolder.AppState,
    isPermissionDenied: Boolean,
    recordingProgress: Float,
    onMicClicked: () -> Unit,
    onPlayClicked: () -> Unit,
    onMusicClicked: () -> Unit
) {
    Scaffold(
        modifier = Modifier.background(colorResource(id = R.color.black))
    ) {
        // Determine the control dashboard state.
        // This converts the main app state into a
        val controlDashboardState by derivedStateOf {
            when (appState) {
                MainStateHolder.AppState.PlayingMusic -> ControlDashboardState(
                    micState = ControlDashboardButtonState(expanded = false, enabled = false, visible = false),
                    playState = ControlDashboardButtonState(expanded = false, enabled = false, visible = false),
                    musicState = ControlDashboardButtonState(expanded = true, enabled = true, visible = true),
                )
                MainStateHolder.AppState.PlayingVoice -> ControlDashboardState(
                    micState = ControlDashboardButtonState(expanded = false, enabled = false, visible = false),
                    playState = ControlDashboardButtonState(expanded = true, enabled = true, visible = true),
                    musicState = ControlDashboardButtonState(expanded = false, enabled = false, visible = false),
                )
                is MainStateHolder.AppState.Ready -> ControlDashboardState(
                    micState = ControlDashboardButtonState(
                        expanded = false,
                        enabled = !isPermissionDenied,
                        visible = true
                    ),
                    playState = ControlDashboardButtonState(expanded = false, enabled = true, visible = true),
                    musicState = ControlDashboardButtonState(expanded = false, enabled = true, visible = true),
                )
                MainStateHolder.AppState.Recording -> ControlDashboardState(
                    micState = ControlDashboardButtonState(expanded = true, enabled = true, visible = true),
                    playState = ControlDashboardButtonState(expanded = false, enabled = false, visible = false),
                    musicState = ControlDashboardButtonState(expanded = false, enabled = false, visible = false),
                )
            }
        }

        // The progress bar should only be visible when actively recording
        val isProgressVisible by derivedStateOf {
            when (appState) {
                MainStateHolder.AppState.PlayingMusic,
                MainStateHolder.AppState.PlayingVoice,
                is MainStateHolder.AppState.Ready -> false
                MainStateHolder.AppState.Recording -> true
            }
        }

        ConstraintLayout(
            modifier = Modifier.fillMaxSize()
        ) {
            val (controlDashboard, progressBar) = createRefs()

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
                    color = colorResource(id = R.color.progressbar_tint),
                    backgroundColor = colorResource(id = R.color.progressbar_background_tint),
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

class AppStatePreviewProvider : CollectionPreviewParameterProvider<MainStateHolder.AppState>(
    listOf(
        MainStateHolder.AppState.Ready(
            transitionInstantly = true
        ),
        MainStateHolder.AppState.Recording,
        MainStateHolder.AppState.PlayingVoice,
        MainStateHolder.AppState.PlayingMusic
    )
)

@Preview(widthDp = 200, heightDp = 200, uiMode = Configuration.UI_MODE_TYPE_WATCH)
@Composable
fun SpeakerScreenPreview(
    @PreviewParameter(AppStatePreviewProvider::class) appState: MainStateHolder.AppState
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
