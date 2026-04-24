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
import androidx.wear.protolayout.DeviceParametersBuilders
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.ProtoLayoutScope
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material3.CardDefaults.filledVariantCardColors
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.Typography
import androidx.wear.protolayout.material3.buttonGroup
import androidx.wear.protolayout.material3.materialScopeWithResources
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.material3.textDataCard
import androidx.wear.protolayout.modifiers.clickable
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import com.example.wear.tiles.tools.MultiRoundDevicesWithFontScalePreviews
import com.example.wear.tiles.tools.isLargeScreen
import com.google.common.util.concurrent.Futures

object Ski {
    fun layout(
        context: Context,
        scope: ProtoLayoutScope,
        deviceParameters: DeviceParametersBuilders.DeviceParameters,
        stat1: Stat,
        stat2: Stat
    ) = materialScopeWithResources(context, scope, deviceParameters) {
        primaryLayout(
            titleSlot = { text("Latest run".layoutString) },
            mainSlot = {
                buttonGroup {
                    buttonGroupItem { statTextButton(stat1) }
                    buttonGroupItem { statTextButton(stat2) }
                }
            }
        )
    }

    private fun MaterialScope.statTextButton(stat: Stat) =
        textDataCard(
            onClick = clickable(),
            width = expand(),
            height = expand(),
            shape = shapes.extraLarge,
            colors =
                filledVariantCardColors().copy(
                    backgroundColor = colorScheme.secondaryContainer,
                    titleColor = colorScheme.onSecondaryContainer,
                    contentColor = colorScheme.onSecondaryContainer,
                    secondaryTextColor = colorScheme.onSecondaryContainer
                ),
            title = {
                text(
                    stat.value.layoutString,
                    typography =
                        if (isLargeScreen()) {
                            Typography.NUMERAL_SMALL
                        } else {
                            Typography.NUMERAL_EXTRA_SMALL
                        }
                )
            },
            content = {
                text(
                    stat.unit.layoutString,
                    typography =
                        if (isLargeScreen()) {
                            Typography.TITLE_MEDIUM
                        } else {
                            Typography.TITLE_SMALL
                        }
                )
            },
            secondaryText = {
                text(
                    stat.label.layoutString,
                    typography =
                        if (isLargeScreen()) {
                            Typography.TITLE_MEDIUM
                        } else {
                            Typography.TITLE_SMALL
                        }
                )
            }
        )

    data class Stat(
        val label: String,
        val value: String,
        val unit: String
    )
}

@MultiRoundDevicesWithFontScalePreviews
internal fun skiPreview(context: Context) =
    TilePreviewData { request ->
        TilePreviewHelper
            .singleTimelineEntryTileBuilder(
                Ski.layout(
                    context,
                    request.scope,
                    request.deviceConfiguration,
                    stat1 = Ski.Stat("Max Spd", "46.5", "mph"),
                    stat2 = Ski.Stat("Distance", "21.8", "mile")
                )
            ).build()
    }

class SkiTileService : TileService() {
    override fun onTileRequest(requestParams: RequestBuilders.TileRequest) =
        Futures.immediateFuture(
            TileBuilders.Tile
                .Builder()
                .setTileTimeline(
                    TimelineBuilders.Timeline.fromLayoutElement(
                        Ski.layout(
                            this,
                            requestParams.scope,
                            requestParams.deviceConfiguration,
                            stat1 = Ski.Stat("Max Spd", "46.5", "mph"),
                            stat2 = Ski.Stat("Distance", "21.8", "mile")
                        )
                    )
                ).build()
        )
}
