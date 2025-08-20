/*
 * Copyright 2022 The Android Open Source Project
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
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.material3.CardDefaults.filledVariantCardColors
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.Typography
import androidx.wear.protolayout.material3.buttonGroup
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.material3.textDataCard
import androidx.wear.protolayout.modifiers.clickable
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import com.example.wear.tiles.tools.MultiRoundDevicesWithFontScalePreviews
import com.example.wear.tiles.tools.isLargeScreen
import com.example.wear.tiles.tools.resources

object Ski {

    fun layout(
        context: Context,
        deviceParameters: DeviceParametersBuilders.DeviceParameters,
        stat1: Stat,
        stat2: Stat
    ) =
        materialScope(context, deviceParameters) {
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

    data class Stat(val label: String, val value: String, val unit: String)
}

@MultiRoundDevicesWithFontScalePreviews
internal fun skiPreview(context: Context) = TilePreviewData {
    TilePreviewHelper.singleTimelineEntryTileBuilder(
        Ski.layout(
            context,
            it.deviceConfiguration,
            stat1 = Ski.Stat("Max Spd", "46.5", "mph"),
            stat2 = Ski.Stat("Distance", "21.8", "mile")
        )
    )
        .build()
}

class SkiTileService : BaseTileService() {
    override fun layout(
        context: Context,
        deviceParameters: DeviceParameters
    ): LayoutElement =
        Ski.layout(
            context,
            deviceParameters,
            stat1 = Ski.Stat("Max Spd", "46.5", "mph"),
            stat2 = Ski.Stat("Distance", "21.8", "mile")
        )

    override fun resources(context: Context) = resources {}
}
