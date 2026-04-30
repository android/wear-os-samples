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
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.ProtoLayoutScope
import androidx.wear.protolayout.TimelineBuilders.Timeline
import androidx.wear.protolayout.layout.androidImageResource
import androidx.wear.protolayout.layout.imageResource
import androidx.wear.protolayout.layout.row
import androidx.wear.protolayout.material3.ButtonDefaults.filledTonalButtonColors
import androidx.wear.protolayout.material3.CardDefaults.filledVariantCardColors
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.TitleCardStyle
import androidx.wear.protolayout.material3.Typography.DISPLAY_LARGE
import androidx.wear.protolayout.material3.Typography.DISPLAY_MEDIUM
import androidx.wear.protolayout.material3.Typography.TITLE_LARGE
import androidx.wear.protolayout.material3.Typography.TITLE_MEDIUM
import androidx.wear.protolayout.material3.icon
import androidx.wear.protolayout.material3.iconEdgeButton
import androidx.wear.protolayout.material3.materialScopeWithResources
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.material3.titleCard
import androidx.wear.protolayout.modifiers.LayoutModifier
import androidx.wear.protolayout.modifiers.clickable
import androidx.wear.protolayout.modifiers.contentDescription
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileService
import androidx.wear.tiles.tile
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import com.example.wear.tiles.R
import com.example.wear.tiles.tools.MultiRoundDevicesWithFontScalePreviews
import com.example.wear.tiles.tools.isLargeScreen
import com.google.common.util.concurrent.Futures
import java.time.LocalTime

fun MaterialScope.styledTime(time: LocalTime): LayoutElement {
    val hour24 = time.hour
    val minute = time.minute

    val amPm = if (hour24 < 12) "AM" else "PM"

    var hour12 = hour24 % 12
    if (hour12 == 0) {
        hour12 = 12
    }

    val timeString = "$hour12:${String.format("%02d", minute)}" // TODO: Localize

    return row(
        text(
            text = timeString.layoutString,
            typography = if (isLargeScreen()) DISPLAY_LARGE else DISPLAY_MEDIUM
        ),
        text(
            text = " $amPm".layoutString,
            typography = if (isLargeScreen()) TITLE_LARGE else TITLE_MEDIUM
        ),
        verticalAlignment = LayoutElementBuilders.VERTICAL_ALIGN_BOTTOM
    )
}

object Alarm {
    data class AlarmData(
        val alarmTime: LocalTime,
        val alarmDays: String,
        val clickable: Clickable
    )

    fun layout(
        context: Context,
        scope: ProtoLayoutScope,
        deviceParameters: DeviceParameters,
        data: AlarmData
    ) = materialScopeWithResources(context, scope, deviceParameters) {
        primaryLayout(
            titleSlot = { text("Alarm".layoutString) },
            mainSlot = {
                titleCard(
                    onClick = data.clickable,
                    title = {
                        text(
                            data.alarmDays.layoutString,
                            typography = if (isLargeScreen()) TITLE_LARGE else TITLE_MEDIUM,
                            color = colorScheme.onSurfaceVariant
                        )
                    },
                    content = { styledTime(data.alarmTime) },
                    height = expand(),
                    colors = filledVariantCardColors(),
                    style =
                        if (isLargeScreen()) {
                            TitleCardStyle.extraLargeTitleCardStyle()
                        } else {
                            TitleCardStyle.defaultTitleCardStyle()
                        }
                )
            },
            bottomSlot = {
                iconEdgeButton(
                    onClick = data.clickable,
                    colors = filledTonalButtonColors(),
                    modifier = LayoutModifier.contentDescription("Plus"),
                    iconContent = {
                        icon(
                            imageResource(
                                androidImageResource(R.drawable.outline_add_2_24)
                            )
                        )
                    }
                )
            }
        )
    }
}

@MultiRoundDevicesWithFontScalePreviews
internal fun alarmPreview(context: Context) =
    TilePreviewData { request ->
        TilePreviewHelper
            .singleTimelineEntryTileBuilder(
                Alarm.layout(
                    context,
                    request.scope,
                    request.deviceConfiguration,
                    Alarm.AlarmData(
                        alarmTime = LocalTime.parse("14:58"),
                        alarmDays = "Mon—Fri",
                        clickable = clickable()
                    )
                )
            ).build()
    }

class AlarmTileService : TileService() {
    override fun onTileRequest(requestParams: RequestBuilders.TileRequest) =
        Futures.immediateFuture(
            tile(
                Timeline.fromLayoutElement(
                    Alarm.layout(
                        this,
                        requestParams.scope,
                        requestParams.deviceConfiguration,
                        Alarm.AlarmData(
                            alarmTime = LocalTime.parse("14:58"),
                            alarmDays = "Mon—Fri",
                            clickable = clickable()
                        )
                    )
                )
            )
        )
}
