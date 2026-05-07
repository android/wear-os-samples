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
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.DimensionBuilders.weight
import androidx.wear.protolayout.LayoutElementBuilders.TEXT_ALIGN_CENTER
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.ProtoLayoutScope
import androidx.wear.protolayout.layout.androidImageResource
import androidx.wear.protolayout.layout.imageResource
import androidx.wear.protolayout.material3.ButtonDefaults.filledButtonColors
import androidx.wear.protolayout.material3.CardDefaults.filledVariantCardColors
import androidx.wear.protolayout.material3.Typography.BODY_SMALL
import androidx.wear.protolayout.material3.Typography.NUMERAL_SMALL
import androidx.wear.protolayout.material3.backgroundImage
import androidx.wear.protolayout.material3.buttonGroup
import androidx.wear.protolayout.material3.imageButton
import androidx.wear.protolayout.material3.materialScopeWithResources
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.material3.textDataCard
import androidx.wear.protolayout.material3.textEdgeButton
import androidx.wear.protolayout.modifiers.LayoutModifier
import androidx.wear.protolayout.modifiers.clickable
import androidx.wear.protolayout.modifiers.clip
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.RequestBuilders.TileRequest
import androidx.wear.tiles.TileBuilders.Tile
import androidx.wear.tiles.TileService
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import com.example.wear.tiles.R
import com.example.wear.tiles.tools.MultiRoundDevicesWithFontScalePreviews
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

object Hike {
    data class HikeData(
        val distance: String,
        val unit: String,
        val clickable: Clickable
    )

    fun layout(
        context: Context,
        protoLayoutScope: ProtoLayoutScope,
        deviceParameters: DeviceParameters,
        data: HikeData
    ) = materialScopeWithResources(context, protoLayoutScope, deviceParameters) {
        primaryLayout(
            titleSlot = { text("Hike".layoutString) },
            bottomSlot = {
                textEdgeButton(
                    onClick = data.clickable,
                    colors = filledButtonColors(),
                    labelContent = { text("Start".layoutString) }
                )
            },
            mainSlot = {
                buttonGroup {
                    buttonGroupItem {
                        textDataCard(
                            width = weight(0.4f),
                            height = expand(),
                            onClick = data.clickable,
                            colors = filledVariantCardColors(),
                            title = {
                                text(
                                    data.distance.layoutString,
                                    typography = NUMERAL_SMALL,
                                    alignment = TEXT_ALIGN_CENTER
                                )
                            },
                            content = {
                                text(
                                    data.unit.layoutString,
                                    typography = BODY_SMALL,
                                    alignment = TEXT_ALIGN_CENTER
                                )
                            }
                        )
                    }
                    buttonGroupItem {
                        imageButton(
                            width = weight(0.6f),
                            height = expand(),
                            onClick = data.clickable,
                            modifier = LayoutModifier.clip(shapes.large),
                            backgroundContent = {
                                backgroundImage(
                                    resource =
                                        imageResource(
                                            androidImageResource(R.drawable.photo_14)
                                        ),
                                    overlayColor = null
                                )
                            }
                        )
                    }
                }
            }
        )
    }
}

@MultiRoundDevicesWithFontScalePreviews
internal fun hikePreview(context: Context) =
    TilePreviewData { request ->
        TilePreviewHelper
            .singleTimelineEntryTileBuilder(
                Hike.layout(
                    context = context,
                    protoLayoutScope = request.scope,
                    deviceParameters = request.deviceConfiguration,
                    data =
                        Hike.HikeData(
                            distance = "10",
                            unit = "Miles",
                            clickable = clickable()
                        )
                )
            ).build()
    }

class HikeTileService : TileService() {
    override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<Tile> {
        val layout =
            Hike.layout(
                this,
                requestParams.scope,
                requestParams.deviceConfiguration,
                Hike.HikeData(distance = "10", unit = "Miles", clickable = clickable())
            )
        val tile =
            TilePreviewHelper
                .singleTimelineEntryTileBuilder(layout)
                .build()
        return Futures.immediateFuture(tile)
    }
}
