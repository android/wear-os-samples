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
package com.example.android.wearable.wear.alwayson

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toComposeRect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.window.layout.WindowMetricsCalculator
import java.time.Instant
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Number of pixels to offset the content rendered in the display to prevent screen burn-in.
 */
const val BURN_IN_OFFSET_PX = 10

@Composable
fun AlwaysOnScreen(
    ambientState: AmbientState,
    ambientUpdateTimestamp: Instant,
    drawCount: Int,
    currentInstant: Instant,
    currentTime: LocalTime
) {
    val dateFormat = remember { DateTimeFormatter.ofPattern("HH:mm:ss", Locale.US) }

    val stateColor = when (ambientState) {
        AmbientState.Interactive -> Color.Green
        is AmbientState.Ambient -> Color.White
    }

    // TODO: Compose doesn't support disabling antialiasing on Text
    //       https://issuetracker.google.com/issues/206701013
    // val isAntiAlias = when (ambientState) {
    //     AmbientState.Interactive -> true
    //     is AmbientState.Ambient -> false
    // }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxRectangle()
            .padding(with(LocalDensity.current) { BURN_IN_OFFSET_PX.toDp() })
            .burnInTranslation(ambientState, ambientUpdateTimestamp)
    ) {
        Text(
            modifier = Modifier.testTag("time"),
            text = dateFormat.format(currentTime),
            style = MaterialTheme.typography.title1,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.testTag("timestamp"),
            text = stringResource(id = R.string.timestamp_label, currentInstant.toEpochMilli()),
            style = MaterialTheme.typography.body2,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.testTag("mode"),
            text = stringResource(
                id = when (ambientState) {
                    AmbientState.Interactive -> R.string.mode_active_label
                    is AmbientState.Ambient -> R.string.mode_ambient_label
                }
            ),
            style = MaterialTheme.typography.body2,
            color = stateColor,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.testTag("rate"),
            text = stringResource(
                id = R.string.update_rate_label,
                when (ambientState) {
                    AmbientState.Interactive -> ACTIVE_INTERVAL.seconds
                    is AmbientState.Ambient -> AMBIENT_INTERVAL.seconds
                }
            ),
            style = MaterialTheme.typography.body2,
            color = stateColor,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.testTag("drawCount"),
            text = stringResource(id = R.string.draw_count_label, drawCount),
            style = MaterialTheme.typography.body2,
            color = stateColor,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * A [Modifier] for adding padding for round devices for rectangular content.
 *
 * If the device is round, an equal amount of padding required to inset the content inside the
 * circle.
 *
 * This method assumes that the layout will fill the entire screen, and that there are no oval
 * devices.
 */
private fun Modifier.fillMaxRectangle(): Modifier = composed {
    val currentWindowMetrics = rememberCurrentWindowMetrics()

    val padding = if (LocalConfiguration.current.isScreenRound) {
        currentWindowMetrics.width / 2f * (1f - 1f / sqrt(2f))
    } else {
        0f
    }

    this
        .fillMaxSize()
        .padding(with(LocalDensity.current) { padding.toDp() })
}

@Composable
private fun rememberCurrentWindowMetrics(): Rect {
    val activity = LocalContext.current.findActivity()
    val configuration = LocalConfiguration.current
    return remember(activity, configuration) {
        WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(activity)
    }.bounds.toComposeRect()
}

/**
 * If the screen requires burn-in protection, items must be shifted around periodically
 * in ambient mode. To ensure that content isn't shifted off the screen, avoid placing
 * content within 10 pixels of the edge of the screen.
 *
 * Activities should also avoid solid white areas to prevent pixel burn-in. Both of
 * these requirements only apply in ambient mode, and only when
 * [AmbientState.Ambient.doBurnInProtection] is set to true.
 */
private fun Modifier.burnInTranslation(
    ambientState: AmbientState,
    ambientUpdateTimestamp: Instant
): Modifier = composed {
    val translationX = rememberBurnInTranslation(ambientState, ambientUpdateTimestamp)
    val translationY = rememberBurnInTranslation(ambientState, ambientUpdateTimestamp)

    this
        .graphicsLayer {
            this.translationX = translationX
            this.translationY = translationY
        }
}

@Composable
private fun rememberBurnInTranslation(
    ambientState: AmbientState,
    ambientUpdateTimestamp: Instant
): Float =
    remember(ambientState, ambientUpdateTimestamp) {
        when (ambientState) {
            AmbientState.Interactive -> 0f
            is AmbientState.Ambient -> if (ambientState.doBurnInProtection) {
                Random.nextInt(-BURN_IN_OFFSET_PX, BURN_IN_OFFSET_PX + 1).toFloat()
            } else {
                0f
            }
        }
    }

private tailrec fun Context.findActivity(): Activity =
    when (this) {
        is Activity -> this
        is ContextWrapper -> this.baseContext.findActivity()
        else -> throw IllegalStateException(
            "findActivity must be called in the context of an activity!"
        )
    }

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun AlwaysOnScreenInteractivePreview() {
    MaterialTheme {
        AlwaysOnScreen(
            ambientState = AmbientState.Interactive,
            ambientUpdateTimestamp = Instant.now(),
            drawCount = 4,
            currentInstant = Instant.now(),
            currentTime = LocalTime.now()
        )
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun AlwaysOnScreenAmbientPreview() {
    MaterialTheme {
        AlwaysOnScreen(
            ambientState = AmbientState.Ambient(
                isLowBitAmbient = true,
                doBurnInProtection = true
            ),
            ambientUpdateTimestamp = Instant.now(),
            drawCount = 4,
            currentInstant = Instant.now(),
            currentTime = LocalTime.now()
        )
    }
}
