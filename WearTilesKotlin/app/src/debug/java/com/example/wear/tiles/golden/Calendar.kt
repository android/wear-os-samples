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
@file:Suppress("ktlint:standard:max-line-length")

package com.example.wear.tiles.golden

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.DimensionBuilders.weight
import androidx.wear.protolayout.LayoutElementBuilders.CONTENT_SCALE_MODE_CROP
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.ProtoLayoutScope
import androidx.wear.protolayout.TimelineBuilders.Timeline
import androidx.wear.protolayout.layout.androidImageResource
import androidx.wear.protolayout.layout.box
import androidx.wear.protolayout.layout.column
import androidx.wear.protolayout.layout.imageResource
import androidx.wear.protolayout.material3.ButtonDefaults.filledButtonColors
import androidx.wear.protolayout.material3.ButtonDefaults.filledTonalButtonColors
import androidx.wear.protolayout.material3.ButtonGroupDefaults
import androidx.wear.protolayout.material3.CardDefaults.filledVariantCardColors
import androidx.wear.protolayout.material3.backgroundImage
import androidx.wear.protolayout.material3.buttonGroup
import androidx.wear.protolayout.material3.icon
import androidx.wear.protolayout.material3.iconButton
import androidx.wear.protolayout.material3.materialScopeWithResources
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.material3.textButton
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

object Calendar {
    data class Event(
        val date: String,
        val time: String,
        val name: String,
        val location: String,
        @DrawableRes val imageId: Int? = null,
        val clickable: Clickable
    )

    fun layout(
        context: Context,
        scope: ProtoLayoutScope,
        deviceParameters: DeviceParameters,
        data: Event
    ) = materialScopeWithResources(context, scope, deviceParameters) {
        primaryLayout(
            mainSlot = {
                column(
                    box(
                        buttonGroup(height = weight(0.3f)) {
                            buttonGroupItem {
                                textButton(
                                    onClick = data.clickable,
                                    labelContent = { text(data.date.layoutString) },
                                    colors = filledTonalButtonColors(),
                                    width = weight(0.6f),
                                    height = expand()
                                )
                            }
                            buttonGroupItem {
                                iconButton(
                                    onClick = data.clickable,
                                    iconContent = {
                                        icon(
                                            imageResource(
                                                androidImageResource(
                                                    R.drawable.outline_add_24
                                                )
                                            )
                                        )
                                    },
                                    colors = filledButtonColors(),
                                    modifier =
                                        LayoutModifier.contentDescription("Add Event"),
                                    width = weight(0.4f),
                                    height = expand()
                                )
                            }
                        },
                        width = expand()
                    ),
                    ButtonGroupDefaults.DEFAULT_SPACER_BETWEEN_BUTTON_GROUPS,
                    box(
                        titleCard(
                            onClick = data.clickable,
                            title = {
                                text(
                                    data.name.layoutString,
                                    maxLines = if (isLargeScreen()) 3 else 2
                                )
                            },
                            content = {
                                column(
                                    *listOfNotNull(
                                        text(data.time.layoutString),
                                        if (isLargeScreen()) text(data.location.layoutString, maxLines = 1) else null
                                    ).toTypedArray()
                                )
                            },
                            colors = filledVariantCardColors(),
                            backgroundContent =
                                data.imageId?.let { imageResId ->
                                    {
                                        backgroundImage(
                                            resource =
                                                imageResource(
                                                    androidImageResource(imageResId)
                                                ),
                                            contentScaleMode = CONTENT_SCALE_MODE_CROP
                                        )
                                    }
                                },
                            shape = shapes.extraLarge,
                            height = expand()
                        ),
                        width = expand(),
                        height = weight(0.7f)
                    ),
                    width = expand(),
                    height = expand()
                )
            }
        )
    }
}

@MultiRoundDevicesWithFontScalePreviews
internal fun calendar1Preview(context: Context) = calendarPreviewX(context)

@MultiRoundDevicesWithFontScalePreviews
internal fun calendar2Preview(context: Context) = calendarPreviewX(context, R.drawable.photo_38)

fun calendarPreviewX(
    context: Context,
    @DrawableRes eventImageId: Int? = null
) = TilePreviewData { request ->
    TilePreviewHelper
        .singleTimelineEntryTileBuilder(
            Calendar.layout(
                context,
                request.scope,
                request.deviceConfiguration,
                Calendar.Event(
                    date = "25 July",
                    time = "6:30-7:30 PM",
                    name = "Advanced Tennis Coaching with Christina Lloyd",
                    location = "216 Market Street",
                    imageId = eventImageId,
                    clickable = clickable()
                )
            )
        ).build()
}

class Calendar1TileService : TileService() {
    override fun onTileRequest(requestParams: RequestBuilders.TileRequest) =
        Futures.immediateFuture(
            tile(
                Timeline.fromLayoutElement(
                    Calendar.layout(
                        this,
                        requestParams.scope,
                        requestParams.deviceConfiguration,
                        Calendar.Event(
                            date = "25 July",
                            time = "6:30-7:30 PM",
                            name = "Advanced Tennis Coaching with Christina Lloyd",
                            location = "216 Market Street",
                            imageId = null,
                            clickable = clickable()
                        )
                    )
                )
            )
        )
}

class Calendar2TileService : TileService() {
    override fun onTileRequest(requestParams: RequestBuilders.TileRequest) =
        Futures.immediateFuture(
            tile(
                Timeline.fromLayoutElement(
                    Calendar.layout(
                        this,
                        requestParams.scope,
                        requestParams.deviceConfiguration,
                        Calendar.Event(
                            date = "25 July",
                            time = "6:30-7:30 PM",
                            name = "Advanced Tennis Coaching with Christina Lloyd",
                            location = "216 Market Street",
                            imageId = R.drawable.photo_38,
                            clickable = clickable()
                        )
                    )
                )
            )
        )
}
