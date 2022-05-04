package com.example.android.wearable.composeadvanced.presentation.ui.progressindicator

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.Text
import com.example.android.wearable.composeadvanced.R
import com.google.android.horologist.compose.navscaffold.ExperimentalComposeLayoutApi
import com.google.android.horologist.compose.navscaffold.scrollableColumn

@OptIn(ExperimentalComposeLayoutApi::class)
@Composable
fun ProgressIndicatorsScreen(
    scalingLazyListState: ScalingLazyListState,
    focusRequester: FocusRequester,
    onClickIndeterminateProgressIndicator: () -> Unit,
    onClickGapProgressIndicator: () -> Unit,
) {
    ScalingLazyColumn(
        modifier = Modifier.scrollableColumn(focusRequester, scalingLazyListState),
        state = scalingLazyListState
    ) {
        item {
            CompactChip(
                onClick = onClickIndeterminateProgressIndicator,
                label = {
                    Text(
                        stringResource(R.string.indeterminate_progress_indicator_label),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
            )
        }
        item {
            CompactChip(
                onClick = onClickGapProgressIndicator,
                label = {
                    Text(
                        stringResource(R.string.full_screen_progress_indicator_label),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
            )
        }

    }
}

@Composable
fun IndeterminateProgressIndicator() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}


@Composable
fun FullScreenProgressIndicator() {
    val transition = rememberInfiniteTransition()

    val currentRotation by transition.animateFloat(
        0f,
        1f,
        infiniteRepeatable(
            animation = tween(
                durationMillis = 5000,
                easing = LinearEasing,
                delayMillis = 1000
            )
        )
    )
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            startAngle = 315f,
            endAngle = 225f,
            progress = currentRotation,
            modifier = Modifier.fillMaxSize().padding(all = 1.dp)
        )
    }
}
