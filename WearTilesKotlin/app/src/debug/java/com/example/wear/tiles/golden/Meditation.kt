/*
 * Copyright 2025-2026 The Android Open Source Project
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
package com.example.wear.tiles.golden

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.ProtoLayoutScope
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.layout.androidImageResource
import androidx.wear.protolayout.layout.imageResource
import androidx.wear.protolayout.material3.ButtonDefaults.filledTonalButtonColors
import androidx.wear.protolayout.material3.ButtonGroupDefaults
import androidx.wear.protolayout.material3.ButtonStyle.Companion.defaultButtonStyle
import androidx.wear.protolayout.material3.ButtonStyle.Companion.smallButtonStyle
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.button
import androidx.wear.protolayout.material3.icon
import androidx.wear.protolayout.material3.materialScopeWithResources
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.material3.textEdgeButton
import androidx.wear.protolayout.modifiers.clickable
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import com.example.wear.tiles.R
import com.example.wear.tiles.tools.MultiRoundDevicesWithFontScalePreviews
import com.example.wear.tiles.tools.column
import com.example.wear.tiles.tools.isLargeScreen
import com.google.common.util.concurrent.Futures

private fun MaterialScope.meditationButton(task: Meditation.MeditationTask) =
    button(
        onClick = task.clickable,
        width = expand(),
        height = expand(),
        colors = filledTonalButtonColors(),
        style =
            if (isLargeScreen()) {
                defaultButtonStyle()
            } else {
                smallButtonStyle()
            },
        iconContent = { icon(imageResource(androidImageResource(task.iconId))) },
        labelContent = { text(task.label.layoutString, maxLines = task.maxLines) }
    )

object Meditation {
    data class MeditationTask(
        val label: String,
        @DrawableRes val iconId: Int,
        val maxLines: Int = 1,
        val clickable: Clickable = clickable()
    )

    fun listLayout(
        context: Context,
        scope: ProtoLayoutScope,
        deviceParameters: DeviceParameters,
        tasksLeft: Int
    ) = materialScopeWithResources(context, scope, deviceParameters) {
        primaryLayout(
            titleSlot =
                if (isLargeScreen()) {
                    { text("$tasksLeft mindful tasks left".layoutString) }
                } else {
                    null
                },
            bottomSlot = {
                textEdgeButton(
                    onClick = clickable(),
                    labelContent = { text("Browse".layoutString) }
                )
            },
            mainSlot = {
                column {
                    setWidth(expand())
                    setHeight(expand())
                    addContent(
                        meditationButton(
                            MeditationTask(
                                label = "Breath",
                                iconId = R.drawable.outline_air_24
                            )
                        )
                    )
                    addContent(ButtonGroupDefaults.DEFAULT_SPACER_BETWEEN_BUTTON_GROUPS)
                    addContent(
                        meditationButton(
                            MeditationTask(
                                label = "Daily mindfulness",
                                iconId = R.drawable.ic_yoga_24,
                                maxLines = 2
                            )
                        )
                    )
                }
            }
        )
    }
}

@MultiRoundDevicesWithFontScalePreviews
fun mindfulnessPreview(context: Context) =
    TilePreviewData { request ->
        TilePreviewHelper
            .singleTimelineEntryTileBuilder(
                Meditation.listLayout(context, request.scope, request.deviceConfiguration, 3)
            ).build()
    }

class MindfulnessTileService : TileService() {
    override fun onTileRequest(requestParams: RequestBuilders.TileRequest) =
        Futures.immediateFuture(
            TileBuilders.Tile
                .Builder()
                .setTileTimeline(
                    TimelineBuilders.Timeline.fromLayoutElement(
                        Meditation.listLayout(
                            this,
                            requestParams.scope,
                            requestParams.deviceConfiguration,
                            2
                        )
                    )
                ).build()
        )
}
