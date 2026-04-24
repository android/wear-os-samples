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
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.CONTENT_SCALE_MODE_CROP
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
import androidx.wear.protolayout.material3.backgroundImage
import androidx.wear.protolayout.material3.button
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

private fun MaterialScope.playlistButton(
    deviceParameters: DeviceParameters,
    playlist: Media.Playlist
) = button(
    onClick = playlist.clickable,
    width = expand(),
    height = expand(),
    colors = filledTonalButtonColors(),
    style =
        if (isLargeScreen()) {
            defaultButtonStyle()
        } else {
            smallButtonStyle()
        },
    horizontalAlignment = LayoutElementBuilders.TEXT_ALIGN_START,
    backgroundContent =
        playlist.imageId?.let {
            {
                backgroundImage(
                    resource = imageResource(androidImageResource(it)),
                    contentScaleMode = CONTENT_SCALE_MODE_CROP
                )
            }
        },
    labelContent = { text(playlist.label.layoutString) }
)

object Media {
    data class Playlist(
        val label: String,
        @DrawableRes val imageId: Int? = null,
        val clickable: Clickable = clickable()
    )

    fun layout(
        context: Context,
        scope: ProtoLayoutScope,
        deviceParameters: DeviceParameters,
        playlist1: Playlist,
        playlist2: Playlist
    ) = materialScopeWithResources(context, scope, deviceParameters) {
        primaryLayout(
            titleSlot =
                if (isLargeScreen()) {
                    { text("Last played".layoutString) }
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
                    addContent(playlistButton(deviceParameters, playlist1))
                    addContent(ButtonGroupDefaults.DEFAULT_SPACER_BETWEEN_BUTTON_GROUPS)
                    addContent(playlistButton(deviceParameters, playlist2))
                }
            }
        )
    }
}

@MultiRoundDevicesWithFontScalePreviews
internal fun mediaPreview(context: Context) =
    TilePreviewData { request ->
        TilePreviewHelper
            .singleTimelineEntryTileBuilder(
                Media.layout(
                    context,
                    request.scope,
                    request.deviceConfiguration,
                    playlist1 =
                        Media.Playlist(
                            "Metal mix",
                            imageId = R.drawable.photo_01
                        ),
                    playlist2 =
                        Media.Playlist(
                            "Chilled mix",
                            imageId = R.drawable.photo_11
                        )
                )
            ).build()
    }

class MediaTileService : TileService() {
    override fun onTileRequest(requestParams: RequestBuilders.TileRequest) =
        Futures.immediateFuture(
            TileBuilders.Tile
                .Builder()
                .setTileTimeline(
                    TimelineBuilders.Timeline.fromLayoutElement(
                        Media.layout(
                            this,
                            requestParams.scope,
                            requestParams.deviceConfiguration,
                            playlist1 =
                                Media.Playlist(
                                    "Metal mix",
                                    imageId = R.drawable.photo_01
                                ),
                            playlist2 =
                                Media.Playlist(
                                    "Chilled mix",
                                    imageId = R.drawable.photo_11
                                )
                        )
                    )
                ).build()
        )
}
