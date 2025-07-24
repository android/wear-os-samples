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
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.CONTENT_SCALE_MODE_CROP
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.material3.ButtonDefaults.filledTonalButtonColors
import androidx.wear.protolayout.material3.ButtonGroupDefaults
import androidx.wear.protolayout.material3.ButtonStyle.Companion.defaultButtonStyle
import androidx.wear.protolayout.material3.ButtonStyle.Companion.smallButtonStyle
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.backgroundImage
import androidx.wear.protolayout.material3.button
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.material3.textEdgeButton
import androidx.wear.protolayout.modifiers.clickable
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import com.example.wear.tiles.R
import com.example.wear.tiles.tools.MultiRoundDevicesWithFontScalePreviews
import com.example.wear.tiles.tools.addIdToImageMapping
import com.example.wear.tiles.tools.column
import com.example.wear.tiles.tools.isLargeScreen
import com.example.wear.tiles.tools.resources

private fun MaterialScope.playlistButton(
  deviceParameters: DeviceParameters,
  playlist: Media.Playlist
) =
  button(
    onClick = playlist.clickable ?: clickable(),
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
    playlist.imageId?.let { id ->
      {
        backgroundImage(
          protoLayoutResourceId = id,
          contentScaleMode = CONTENT_SCALE_MODE_CROP
        )
      }
    },
    labelContent = { text(playlist.label.layoutString) }
  )

object Media {

  data class Playlist(
    val label: String,
    val imageId: String? = null,
    val clickable: Clickable? = clickable()
  )

  fun layout(
    context: Context,
    deviceParameters: DeviceParameters,
    playlist1: Playlist,
    playlist2: Playlist
  ) =
    materialScope(context, deviceParameters) {
      primaryLayout(
        titleSlot =
        if (isLargeScreen()) {
          { text("Last played".layoutString) }
        } else {
          null
        },
        bottomSlot = {
          textEdgeButton(onClick = clickable(), labelContent = { text("Browse".layoutString) })
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

  fun resources(context: Context) = resources {
    addIdToImageMapping(context.resources.getResourceName(R.drawable.photo_01), R.drawable.photo_01)
    addIdToImageMapping(context.resources.getResourceName(R.drawable.photo_11), R.drawable.photo_11)
  }
}

@MultiRoundDevicesWithFontScalePreviews
internal fun mediaPreview(context: Context) =
  TilePreviewData(Media.resources(context)) {
    TilePreviewHelper.singleTimelineEntryTileBuilder(
      Media.layout(
        context,
        it.deviceConfiguration,
        playlist1 =
        Media.Playlist(
          "Metal mix",
          imageId = context.resources.getResourceName(R.drawable.photo_01)
        ),
        playlist2 =
        Media.Playlist(
          "Chilled mix",
          imageId = context.resources.getResourceName(R.drawable.photo_11)
        )
      )
    )
      .build()
  }

class MediaTileService : BaseTileService() {
  override fun layout(
    context: Context,
    deviceParameters: DeviceParameters
  ): LayoutElement =
    Media.layout(
      context,
      deviceParameters,
      playlist1 =
      Media.Playlist(
        "Metal mix",
        imageId = context.resources.getResourceName(R.drawable.photo_01)
      ),
      playlist2 =
      Media.Playlist(
        "Chilled mix",
        imageId = context.resources.getResourceName(R.drawable.photo_11)
      )
    )

  override fun resources(context: Context) = Media.resources(context)
}
