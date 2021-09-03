package com.example.android.wearable.speaker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.wear.compose.material.ContentAlpha
import androidx.wear.compose.material.Icon

/**
 * The component responsible for drawing the main 3 controls, with their expanded and minimized states.
 *
 * The state for this class is driven by a [ControlDashboardState], which contains a [ControlDashboardButtonState]
 * for each of the three buttons.
 */
@Composable
fun ControlDashboard(
    controlDashboardState: ControlDashboardState,
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
        controlDashboardState = controlDashboardState,
        circle = circle,
        mic = mic,
        play = play,
        music = music,
    )

    ConstraintLayout(
        constraintSet = constraintSet,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .layoutId(circle)
                .size(140.dp)
                .clip(CircleShape)
                .background(colorResource(id = R.color.circle_color))
        )

        ControlDashboardButton(
            buttonState = controlDashboardState.micState,
            onClick = onMicClicked,
            layoutId = mic,
            imageVector = Icons.Filled.Mic,
            contentDescription = if (controlDashboardState.micState.expanded) {
                stringResource(id = R.string.stop_recording)
            } else {
                stringResource(id = R.string.record)
            }
        )

        ControlDashboardButton(
            buttonState = controlDashboardState.playState,
            onClick = onPlayClicked,
            layoutId = play,
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = if (controlDashboardState.playState.expanded) {
                stringResource(id = R.string.stop_playing_recording)
            } else {
                stringResource(id = R.string.play_recording)
            }
        )

        ControlDashboardButton(
            buttonState = controlDashboardState.musicState,
            onClick = onMusicClicked,
            layoutId = music,
            imageVector = Icons.Filled.MusicNote,
            contentDescription = if (controlDashboardState.musicState.expanded) {
                stringResource(id = R.string.stop_playing_music)
            } else {
                stringResource(id = R.string.play_music)
            }
        )
    }
}

/**
 * Creates the [ConstraintSet] for the [controlDashboardState].
 *
 * The [circle], [mic], [play] and [music] are used as keys for the constraints.
 */
@Composable
private fun createConstraintSet(
    controlDashboardState: ControlDashboardState,
    circle: Any,
    mic: Any,
    play: Any,
    music: Any,
): ConstraintSet {
    val iconCircleRadius = dimensionResource(id = R.dimen.icon_circle_radius)
    val iconMinimizedSize = dimensionResource(id = R.dimen.icon_minimized_size)
    val iconExpandedSize = dimensionResource(id = R.dimen.icon_expanded_size)

    return ConstraintSet {
        val circleRef = createRefFor(circle)
        val micRef = createRefFor(mic)
        val playRef = createRefFor(play)
        val musicRef = createRefFor(music)

        constrain(circleRef) { centerTo(parent) }
        constrain(micRef) {
            val size = if (controlDashboardState.micState.expanded) iconExpandedSize else iconMinimizedSize
            width = Dimension.value(size)
            height = Dimension.value(size)
            circular(circleRef, 0f, if (controlDashboardState.micState.expanded) 0.dp else iconCircleRadius)
        }
        constrain(playRef) {
            val size = if (controlDashboardState.playState.expanded) iconExpandedSize else iconMinimizedSize
            width = Dimension.value(size)
            height = Dimension.value(size)
            circular(circleRef, 240f, if (controlDashboardState.playState.expanded) 0.dp else iconCircleRadius)
        }
        constrain(musicRef) {
            val size = if (controlDashboardState.musicState.expanded) iconExpandedSize else iconMinimizedSize
            width = Dimension.value(size)
            height = Dimension.value(size)
            circular(circleRef, 120f, if (controlDashboardState.musicState.expanded) 0.dp else iconCircleRadius)
        }
    }
}

/**
 * A single control dashboard button
 */
@Composable
private fun ControlDashboardButton(
    buttonState: ControlDashboardButtonState,
    onClick: () -> Unit,
    layoutId: Any,
    imageVector: ImageVector,
    contentDescription: String
) {
    val iconPadding = dimensionResource(id = R.dimen.icon_padding)
    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }

    // TODO: Replace with a version of IconButton?
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(if (buttonState.visible) 1f else 0f)
            .layoutId(layoutId)
            .clickable(
                onClick = onClick,
                enabled = buttonState.enabled && buttonState.visible,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = false, radius = Dp.Unspecified)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .alpha(if (buttonState.enabled) ContentAlpha.high else ContentAlpha.disabled)
                .padding(iconPadding)
        )
    }
}

/**
 * The state for a single [ControlDashboardButton].
 */
data class ControlDashboardButtonState(
    val expanded: Boolean,
    val enabled: Boolean,
    val visible: Boolean
)

/**
 * The state for a [ControlDashboard].
 */
data class ControlDashboardState(
    val micState: ControlDashboardButtonState,
    val playState: ControlDashboardButtonState,
    val musicState: ControlDashboardButtonState,
) {
    init {
        // Check that at most one of the buttons is expanded
        require(
            listOf(
                micState.expanded,
                playState.expanded,
                musicState.expanded
            ).map {
                if (it) 1 else 0
            }.sum() <= 1
        )
    }
}
