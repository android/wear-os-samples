/*
 * Copyright 2022-2026 The Android Open Source Project
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
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ProtoLayoutScope
import androidx.wear.protolayout.TimelineBuilders.Timeline
import androidx.wear.protolayout.expression.DynamicBuilders.DynamicFloat
import androidx.wear.protolayout.expression.PlatformEventSources
import androidx.wear.protolayout.layout.androidImageResource
import androidx.wear.protolayout.layout.imageResource
import androidx.wear.protolayout.material3.CardDefaults.filledTonalCardColors
import androidx.wear.protolayout.material3.CircularProgressIndicatorDefaults
import androidx.wear.protolayout.material3.GraphicDataCardDefaults.constructGraphic
import androidx.wear.protolayout.material3.PrimaryLayoutMargins
import androidx.wear.protolayout.material3.Typography
import androidx.wear.protolayout.material3.circularProgressIndicator
import androidx.wear.protolayout.material3.graphicDataCard
import androidx.wear.protolayout.material3.icon
import androidx.wear.protolayout.material3.materialScopeWithResources
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.material3.textEdgeButton
import androidx.wear.protolayout.modifiers.clickable
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileService
import androidx.wear.tiles.tile
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper.singleTimelineEntryTileBuilder
import com.example.wear.tiles.R
import com.example.wear.tiles.tools.MultiRoundDevicesWithFontScalePreviews
import com.example.wear.tiles.tools.isLargeScreen
import com.google.common.util.concurrent.Futures
import java.text.NumberFormat

object Goal {
    data class GoalData(
        val steps: Int,
        val goal: Int
    )

    fun layout(
        context: Context,
        scope: ProtoLayoutScope,
        deviceParameters: DeviceParameters,
        data: GoalData
    ) = materialScopeWithResources(context, scope, deviceParameters) {
        val stepsString = NumberFormat.getNumberInstance().format(data.steps)
        val goalString = NumberFormat.getNumberInstance().format(data.goal)
        primaryLayout(
            titleSlot = { text("Steps".layoutString) },
            margins = PrimaryLayoutMargins.MIN_PRIMARY_LAYOUT_MARGIN,
            mainSlot = {
                graphicDataCard(
                    onClick = clickable(),
                    height = expand(),
                    colors = filledTonalCardColors(),
                    title = {
                        text(
                            stepsString.layoutString,
                            typography =
                                if (isLargeScreen()) {
                                    Typography.DISPLAY_LARGE
                                } else {
                                    Typography.DISPLAY_SMALL
                                }
                        )
                    },
                    content = {
                        text(
                            "of $goalString".layoutString,
                            typography =
                                if (isLargeScreen()) {
                                    Typography.TITLE_LARGE
                                } else {
                                    Typography.TITLE_SMALL
                                }
                        )
                    },
                    horizontalAlignment = LayoutElementBuilders.HORIZONTAL_ALIGN_END,
                    graphic = {
                        constructGraphic(
                            mainContent = {
                                circularProgressIndicator(
                                    staticProgress = 1F * data.steps / data.goal,
                                    // On supported devices, animate the arc
                                    dynamicProgress =
                                        DynamicFloat
                                            .onCondition(
                                                PlatformEventSources.isLayoutVisible()
                                            ).use(1F * data.steps / data.goal)
                                            .elseUse(0F)
                                            .animate(
                                                CircularProgressIndicatorDefaults
                                                    .recommendedAnimationSpec
                                            ),
                                    startAngleDegrees = 200F,
                                    endAngleDegrees = 520F
                                )
                            },
                            iconContent = {
                                icon(
                                    imageResource(
                                        androidImageResource(
                                            R.drawable.outline_directions_walk_24
                                        )
                                    )
                                )
                            }
                        )
                    }
                )
            },
            bottomSlot = {
                textEdgeButton(onClick = clickable()) { text("Track".layoutString) }
            }
        )
    }
}

@MultiRoundDevicesWithFontScalePreviews
internal fun goalPreview(context: Context) =
    TilePreviewData { request ->
        singleTimelineEntryTileBuilder(
            Goal.layout(
                context,
                request.scope,
                request.deviceConfiguration,
                data = Goal.GoalData(steps = 5168, goal = 8000)
            )
        ).build()
    }

class GoalTileService : TileService() {
    override fun onTileRequest(requestParams: RequestBuilders.TileRequest) =
        Futures.immediateFuture(
            tile(
                Timeline.fromLayoutElement(
                    Goal.layout(
                        this,
                        requestParams.scope,
                        requestParams.deviceConfiguration,
                        data = Goal.GoalData(steps = 5168, goal = 8000)
                    )
                )
            )
        )
}
