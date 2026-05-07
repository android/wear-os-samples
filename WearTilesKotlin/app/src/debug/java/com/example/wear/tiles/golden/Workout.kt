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
import androidx.annotation.DrawableRes
import androidx.wear.protolayout.DeviceParametersBuilders
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.DimensionBuilders.weight
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ProtoLayoutScope
import androidx.wear.protolayout.TimelineBuilders.Timeline
import androidx.wear.protolayout.layout.androidImageResource
import androidx.wear.protolayout.layout.column
import androidx.wear.protolayout.layout.imageResource
import androidx.wear.protolayout.material3.ButtonDefaults.filledButtonColors
import androidx.wear.protolayout.material3.ButtonDefaults.filledTonalButtonColors
import androidx.wear.protolayout.material3.ButtonDefaults.filledVariantButtonColors
import androidx.wear.protolayout.material3.ButtonGroupDefaults.DEFAULT_SPACER_BETWEEN_BUTTON_GROUPS
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.PrimaryLayoutMargins
import androidx.wear.protolayout.material3.Typography.DISPLAY_MEDIUM
import androidx.wear.protolayout.material3.Typography.LABEL_SMALL
import androidx.wear.protolayout.material3.Typography.TITLE_LARGE
import androidx.wear.protolayout.material3.Typography.TITLE_MEDIUM
import androidx.wear.protolayout.material3.button
import androidx.wear.protolayout.material3.buttonGroup
import androidx.wear.protolayout.material3.icon
import androidx.wear.protolayout.material3.iconButton
import androidx.wear.protolayout.material3.iconDataCard
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

object Workout {
    data class WorkoutData(
        val titleText: String,
        val contentText: String
    )

    fun layout1(
        context: Context,
        scope: ProtoLayoutScope,
        deviceParameters: DeviceParametersBuilders.DeviceParameters,
        data: WorkoutData
    ) = materialScopeWithResources(context, scope, deviceParameters) {
        primaryLayout(
            titleSlot = { text(data.titleText.layoutString) },
            mainSlot = {
                buttonGroup {
                    buttonGroupItem {
                        iconButton(
                            onClick = clickable(),
                            width = expand(),
                            height = if (isLargeScreen()) dp(90f) else expand(),
                            colors = filledVariantButtonColors(),
                            iconContent = {
                                icon(
                                    imageResource(
                                        androidImageResource(
                                            R.drawable.self_improvement_24px
                                        )
                                    )
                                )
                            }
                        )
                    }
                    buttonGroupItem {
                        if (isLargeScreen()) {
                            iconDataCard(
                                onClick = clickable(),
                                width = weight(1.5f),
                                height = expand(),
                                shape = shapes.large,
                                title = {
                                    text("30".layoutString, typography = DISPLAY_MEDIUM)
                                },
                                content = {
                                    text("Mins".layoutString, typography = TITLE_MEDIUM)
                                },
                                secondaryIcon = {
                                    icon(
                                        imageResource(
                                            androidImageResource(
                                                R.drawable.ic_run_24
                                            )
                                        )
                                    )
                                }
                            )
                        } else {
                            iconButton(
                                onClick = clickable(),
                                width = expand(),
                                height = expand(),
                                shape = shapes.large,
                                colors = filledButtonColors(),
                                iconContent = {
                                    icon(
                                        imageResource(
                                            androidImageResource(
                                                R.drawable.ic_run_24
                                            )
                                        )
                                    )
                                }
                            )
                        }
                    }
                    buttonGroupItem {
                        iconButton(
                            onClick = clickable(),
                            width = expand(),
                            height = if (isLargeScreen()) dp(90f) else expand(),
                            colors = filledVariantButtonColors(),
                            iconContent = {
                                icon(
                                    imageResource(
                                        androidImageResource(
                                            R.drawable.ic_cycling_24
                                        )
                                    )
                                )
                            }
                        )
                    }
                }
            },
            bottomSlot = {
                textEdgeButton(onClick = clickable()) { text("More".layoutString) }
            }
        )
    }

    fun layout2(
        context: Context,
        scope: ProtoLayoutScope,
        deviceParameters: DeviceParameters,
        data: WorkoutData
    ) = materialScopeWithResources(context, scope, deviceParameters) {
        primaryLayout(
            titleSlot = { text(data.titleText.layoutString) },
            margins = PrimaryLayoutMargins.MID_PRIMARY_LAYOUT_MARGIN,
            mainSlot = {
                if (isLargeScreen()) {
                    column(
                        workoutGraphicDataCard(
                            titleText = "Start Run",
                            contentText = data.contentText,
                            iconId = R.drawable.ic_run_24
                        ),
                        DEFAULT_SPACER_BETWEEN_BUTTON_GROUPS,
                        buttonGroup {
                            buttonGroupItem {
                                iconButton(
                                    onClick = clickable(),
                                    height = expand(),
                                    width = expand(),
                                    colors = filledTonalButtonColors(),
                                    iconContent = {
                                        icon(
                                            imageResource(
                                                androidImageResource(
                                                    R.drawable.self_improvement_24px
                                                )
                                            )
                                        )
                                    }
                                )
                            }
                            buttonGroupItem {
                                iconButton(
                                    onClick = clickable(),
                                    width = expand(),
                                    height = expand(),
                                    colors = filledTonalButtonColors(),
                                    iconContent = {
                                        icon(
                                            imageResource(
                                                androidImageResource(
                                                    R.drawable.ic_run_24
                                                )
                                            )
                                        )
                                    }
                                )
                            }
                            buttonGroupItem {
                                iconButton(
                                    onClick = clickable(),
                                    width = expand(),
                                    height = expand(),
                                    colors = filledTonalButtonColors(),
                                    iconContent = {
                                        icon(
                                            imageResource(
                                                androidImageResource(
                                                    R.drawable.ic_cycling_24
                                                )
                                            )
                                        )
                                    }
                                )
                            }
                        },
                        width = expand(),
                        height = expand()
                    )
                } else {
                    workoutGraphicDataCard(
                        titleText = "Start Run",
                        contentText = data.contentText,
                        iconId = R.drawable.ic_run_24
                    )
                }
            },
            bottomSlot = {
                textEdgeButton(onClick = clickable(), colors = filledVariantButtonColors()) {
                    text("More".layoutString)
                }
            }
        )
    }

    private fun MaterialScope.workoutGraphicDataCard(
        titleText: String,
        contentText: String,
        @DrawableRes iconId: Int
    ) = button(
        onClick = clickable(),
        height = expand(),
        width = expand(),
        labelContent = { text(titleText.layoutString, typography = TITLE_LARGE) },
        secondaryLabelContent = { text(contentText.layoutString, typography = LABEL_SMALL) },
        horizontalAlignment = LayoutElementBuilders.HORIZONTAL_ALIGN_START,
        iconContent = {
            icon(
                imageResource(androidImageResource(iconId)),
                width = dp(36f),
                height = dp(36f)
            )
        }
    )
}

@MultiRoundDevicesWithFontScalePreviews
internal fun workoutLayout1Preview(context: Context) =
    TilePreviewData { request ->
        singleTimelineEntryTileBuilder(
            Workout.layout1(
                context,
                request.scope,
                request.deviceConfiguration,
                Workout.WorkoutData("Exercise", "30 min goal")
            )
        ).build()
    }

@MultiRoundDevicesWithFontScalePreviews
internal fun workoutLayout2Preview(context: Context) =
    TilePreviewData { request ->
        singleTimelineEntryTileBuilder(
            Workout.layout2(
                context,
                request.scope,
                request.deviceConfiguration,
                Workout.WorkoutData("Exercise", "30 min goal")
            )
        ).build()
    }

class WorkoutTileService1 : TileService() {
    override fun onTileRequest(requestParams: RequestBuilders.TileRequest) =
        Futures.immediateFuture(
            tile(
                Timeline.fromLayoutElement(
                    Workout.layout1(
                        this,
                        requestParams.scope,
                        requestParams.deviceConfiguration,
                        Workout.WorkoutData("Exercise", "30 min goal")
                    )
                )
            )
        )
}

class WorkoutTileService2 : TileService() {
    override fun onTileRequest(requestParams: RequestBuilders.TileRequest) =
        Futures.immediateFuture(
            tile(
                Timeline.fromLayoutElement(
                    Workout.layout2(
                        this,
                        requestParams.scope,
                        requestParams.deviceConfiguration,
                        Workout.WorkoutData("Exercise", "30 min goal")
                    )
                )
            )
        )
}
