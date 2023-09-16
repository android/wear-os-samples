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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.withSaveLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.ambient.AmbientLifecycleObserver
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewSmallRound
import com.google.android.horologist.compose.ambient.AmbientState
import com.google.android.horologist.compose.ambient.AmbientStateUpdate
import com.google.android.horologist.compose.layout.fillMaxRectangle
import java.time.Instant
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.random.Random

/**
 * Number of pixels to offset the content rendered in the display to prevent screen burn-in.
 */
const val BURN_IN_OFFSET_PX = 10

@Composable
fun AlwaysOnScreen(
    ambientStateUpdate: AmbientStateUpdate,
    drawCount: Int,
    currentInstant: Instant,
    currentTime: LocalTime
) {
    val dateFormat = remember { DateTimeFormatter.ofPattern("HH:mm:ss", Locale.US) }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxRectangle()
            .padding(with(LocalDensity.current) { BURN_IN_OFFSET_PX.toDp() })
            .ambientMode(ambientStateUpdate)
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
                id = when (ambientStateUpdate.ambientState) {
                    AmbientState.Interactive -> R.string.mode_active_label
                    is AmbientState.Ambient -> R.string.mode_ambient_label
                }
            ),
            style = MaterialTheme.typography.body2,
            color = Color.Green,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.testTag("rate"),
            text = stringResource(
                id = R.string.update_rate_label,
                when (ambientStateUpdate.ambientState) {
                    AmbientState.Interactive -> ACTIVE_INTERVAL.seconds
                    is AmbientState.Ambient -> AMBIENT_INTERVAL.seconds
                }
            ),
            style = MaterialTheme.typography.body2,
            color = Color.Green,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.testTag("drawCount"),
            text = stringResource(id = R.string.draw_count_label, drawCount),
            style = MaterialTheme.typography.body2,
            color = Color.Green,
            textAlign = TextAlign.Center
        )
    }
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
private fun Modifier.ambientMode(
    ambientStateUpdate: AmbientStateUpdate
): Modifier = composed {
    val translationX = rememberBurnInTranslation(ambientStateUpdate)
    val translationY = rememberBurnInTranslation(ambientStateUpdate)

    this
        .graphicsLayer {
            this.translationX = translationX
            this.translationY = translationY
        }.ambientGray(ambientStateUpdate.ambientState)
}

@Composable
private fun rememberBurnInTranslation(
    ambientStateUpdate: AmbientStateUpdate
): Float =
    remember(ambientStateUpdate) {
        when (val state = ambientStateUpdate.ambientState) {
            AmbientState.Interactive -> 0f
            is AmbientState.Ambient -> if (state.ambientDetails?.burnInProtectionRequired == true) {
                Random.nextInt(-BURN_IN_OFFSET_PX, BURN_IN_OFFSET_PX + 1).toFloat()
            } else {
                0f
            }
        }
    }

private val grayscale = Paint().apply {
    colorFilter = ColorFilter.colorMatrix(
        ColorMatrix().apply {
            setToSaturation(0f)
        }
    )
    isAntiAlias = false
}

internal fun Modifier.ambientGray(ambientState: AmbientState): Modifier =
    if (ambientState is AmbientState.Ambient) {
        graphicsLayer {
            scaleX = 0.9f
            scaleY = 0.9f
        }.drawWithContent {
            drawIntoCanvas {
                it.withSaveLayer(size.toRect(), grayscale) {
                    drawContent()
                }
            }
        }
    } else {
        this
    }

@WearPreviewSmallRound
@Composable
fun AlwaysOnScreenInteractivePreview() {
    MaterialTheme {
        AlwaysOnScreen(
            ambientStateUpdate = AmbientStateUpdate(AmbientState.Interactive),
            drawCount = 4,
            currentInstant = Instant.now(),
            currentTime = LocalTime.now()
        )
    }
}

@WearPreviewSmallRound
@Composable
fun AlwaysOnScreenAmbientPreview() {
    MaterialTheme {
        AlwaysOnScreen(
            ambientStateUpdate = AmbientStateUpdate(
                AmbientState.Ambient(
                    AmbientLifecycleObserver.AmbientDetails(
                        burnInProtectionRequired = true,
                        deviceHasLowBitAmbient = true
                    )
                ),
                changeTimeMillis = System.currentTimeMillis()
            ),
            drawCount = 4,
            currentInstant = Instant.now(),
            currentTime = LocalTime.now()
        )
    }
}
