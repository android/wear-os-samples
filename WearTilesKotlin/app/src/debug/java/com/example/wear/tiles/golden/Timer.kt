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
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.ProtoLayoutScope
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.layout.androidImageResource
import androidx.wear.protolayout.layout.imageResource
import androidx.wear.protolayout.material3.ButtonDefaults.filledButtonColors
import androidx.wear.protolayout.material3.ButtonDefaults.filledVariantButtonColors
import androidx.wear.protolayout.material3.ButtonGroupDefaults
import androidx.wear.protolayout.material3.ButtonGroupDefaults.DEFAULT_SPACER_BETWEEN_BUTTON_GROUPS
import androidx.wear.protolayout.material3.CardDefaults.filledVariantCardColors
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.Typography.BODY_SMALL
import androidx.wear.protolayout.material3.Typography.DISPLAY_SMALL
import androidx.wear.protolayout.material3.Typography.NUMERAL_SMALL
import androidx.wear.protolayout.material3.buttonGroup
import androidx.wear.protolayout.material3.icon
import androidx.wear.protolayout.material3.iconEdgeButton
import androidx.wear.protolayout.material3.materialScopeWithResources
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.material3.textButton
import androidx.wear.protolayout.material3.textDataCard
import androidx.wear.protolayout.modifiers.LayoutModifier
import androidx.wear.protolayout.modifiers.clickable
import androidx.wear.protolayout.modifiers.contentDescription
import androidx.wear.protolayout.modifiers.padding
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

object Timer {
    fun timer1Layout(
        context: Context,
        scope: ProtoLayoutScope,
        deviceParameters: DeviceParameters
    ) = materialScopeWithResources(context, scope, deviceParameters) {
        primaryLayout(
            titleSlot = { text("Minutes".layoutString) },
            mainSlot = {
                if (isLargeScreen()) {
                    column {
                        setWidth(expand())
                        setHeight(expand())
                        addContent(
                            buttonGroup {
                                buttonGroupItem { timerTextButton1("5") }
                                buttonGroupItem { timerTextButton1("10") }
                            }
                        )
                        addContent(ButtonGroupDefaults.DEFAULT_SPACER_BETWEEN_BUTTON_GROUPS)
                        addContent(
                            buttonGroup {
                                buttonGroupItem { timerTextButton1("15") }
                                buttonGroupItem { timerTextButton1("20") }
                                buttonGroupItem { timerTextButton1("30") }
                            }
                        )
                    }
                } else {
                    buttonGroup {
                        buttonGroupItem { timerTextButton1("5") }
                        buttonGroupItem { timerTextButton1("10") }
                        buttonGroupItem { timerTextButton1("15") }
                    }
                }
            },
            bottomSlot = {
                iconEdgeButton(
                    onClick = clickable(),
                    colors = filledButtonColors(),
                    modifier = LayoutModifier.contentDescription("Add"),
                    iconContent = {
                        icon(imageResource(androidImageResource(R.drawable.outline_add_2_24)))
                    }
                )
            }
        )
    }

    fun timer2Layout(
        context: Context,
        scope: ProtoLayoutScope,
        deviceParameters: DeviceParameters,
        clickable: Clickable
    ) = materialScopeWithResources(context, scope, deviceParameters) {
        primaryLayout(
            mainSlot = {
                column {
                    setWidth(expand())
                    setHeight(expand())
                    addContent(
                        buttonGroup {
                            buttonGroupItem { timerButton("1:00", "Hour") }
                            buttonGroupItem { timerButton("5", "Mins") }
                        }
                    )
                    addContent(DEFAULT_SPACER_BETWEEN_BUTTON_GROUPS)
                    addContent(
                        buttonGroup {
                            buttonGroupItem { timerButton("15", "Mins") }
                            buttonGroupItem { timerButton("20", "Mins") }
                            buttonGroupItem { timerButton("25", "Mins") }
                        }
                    )
                    build()
                }
            },
            bottomSlot = {
                iconEdgeButton(
                    onClick = clickable,
                    colors =
                        filledButtonColors()
                            .copy(
                                containerColor = colorScheme.tertiary,
                                labelColor = colorScheme.onTertiary
                            ),
                    modifier = LayoutModifier.contentDescription("Plus"),
                    iconContent = {
                        icon(imageResource(androidImageResource(R.drawable.outline_add_2_24)))
                    }
                )
            }
        )
    }

    private fun MaterialScope.timerTextButton1(text: String) =
        textButton(
            width = expand(),
            height = expand(),
            onClick = clickable(),
            shape = shapes.large,
            colors = filledVariantButtonColors(),
            labelContent = { text(text.layoutString, typography = NUMERAL_SMALL) }
        )
}

private fun MaterialScope.timerButton(
    firstLine: String?,
    secondLine: String? = null
) = textDataCard(
    onClick = clickable(),
    colors = filledVariantCardColors(),
    width = expand(),
    height = expand(),
    title = {
        text(
            text = firstLine?.layoutString ?: "".layoutString,
            typography = if (isLargeScreen()) DISPLAY_SMALL else DISPLAY_SMALL
        )
    },
    content = {
        text(
            text = secondLine?.layoutString ?: "".layoutString,
            typography = if (isLargeScreen()) BODY_SMALL else BODY_SMALL
        )
    },
    contentPadding = padding(all = 4f)
)

@MultiRoundDevicesWithFontScalePreviews
fun timer1LayoutPreview(context: Context) =
    TilePreviewData { request ->
        TilePreviewHelper
            .singleTimelineEntryTileBuilder(
                Timer.timer1Layout(context, request.scope, request.deviceConfiguration)
            ).build()
    }

@MultiRoundDevicesWithFontScalePreviews
fun timer2LayoutPreview(context: Context) =
    TilePreviewData { request ->
        TilePreviewHelper
            .singleTimelineEntryTileBuilder(
                Timer.timer2Layout(context, request.scope, request.deviceConfiguration, clickable())
            ).build()
    }

class Timer1TileService : TileService() {
    override fun onTileRequest(requestParams: RequestBuilders.TileRequest) =
        Futures.immediateFuture(
            TileBuilders.Tile
                .Builder()
                .setTileTimeline(
                    TimelineBuilders.Timeline.fromLayoutElement(
                        Timer.timer1Layout(
                            this,
                            requestParams.scope,
                            requestParams.deviceConfiguration
                        )
                    )
                ).build()
        )
}

class Timer2TileService : TileService() {
    override fun onTileRequest(requestParams: RequestBuilders.TileRequest) =
        Futures.immediateFuture(
            TileBuilders.Tile
                .Builder()
                .setTileTimeline(
                    TimelineBuilders.Timeline.fromLayoutElement(
                        Timer.timer2Layout(
                            this,
                            requestParams.scope,
                            requestParams.deviceConfiguration,
                            clickable()
                        )
                    )
                ).build()
        )
}
