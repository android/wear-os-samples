/*
 * Copyright 2025 The Android Open Source Project
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
@file:SuppressLint("RestrictedApi")

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

import android.annotation.SuppressLint
import android.content.Context
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.material3.ButtonDefaults.filledTonalButtonColors
import androidx.wear.protolayout.material3.ButtonGroupDefaults
import androidx.wear.protolayout.material3.ButtonGroupDefaults.DEFAULT_SPACER_BETWEEN_BUTTON_GROUPS
import androidx.wear.protolayout.material3.ButtonStyle.Companion.defaultButtonStyle
import androidx.wear.protolayout.material3.ButtonStyle.Companion.smallButtonStyle
import androidx.wear.protolayout.material3.button
import androidx.wear.protolayout.material3.icon
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

object Meditation {

  fun listLayout(context: Context, deviceParameters: DeviceParameters, tasksLeft: Int) =
    materialScope(context, deviceParameters) {
      primaryLayout(
        titleSlot =
        if (isLargeScreen()) {
          { text("$tasksLeft mindful tasks left".layoutString) }
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
            addContent(
              button(
                onClick = clickable(),
                width = expand(),
                height = expand(),
                colors = filledTonalButtonColors(),
                style =
                if (isLargeScreen()) {
                  defaultButtonStyle()
                } else {
                  smallButtonStyle()
                },
                iconContent = {
                  icon(context.resources.getResourceName(R.drawable.outline_air_24))
                },
                labelContent = { text("Breath".layoutString) }
              )
            )
            addContent(ButtonGroupDefaults.DEFAULT_SPACER_BETWEEN_BUTTON_GROUPS)
            addContent(
              button(
                onClick = clickable(),
                width = expand(),
                height = expand(),
                colors = filledTonalButtonColors(),
                style =
                if (isLargeScreen()) {
                  defaultButtonStyle()
                } else {
                  smallButtonStyle()
                },
                iconContent = { icon(context.resources.getResourceName(R.drawable.ic_yoga_24)) },
                labelContent = { text("Daily mindfulness".layoutString, maxLines = 2) }
              )
            )
          }
        }
      )
    }

  fun resources(context: Context) = resources {
    addIdToImageMapping(
      context.resources.getResourceName(R.drawable.ic_yoga_24),
      R.drawable.ic_yoga_24
    )
    addIdToImageMapping(
      context.resources.getResourceName(R.drawable.outline_air_24),
      R.drawable.outline_air_24
    )
    addIdToImageMapping(
      context.resources.getResourceName(R.drawable.ic_breathe_24),
      R.drawable.ic_breathe_24
    )
    addIdToImageMapping(
      context.resources.getResourceName(R.drawable.ic_mindfulness_24),
      R.drawable.ic_mindfulness_24
    )
    addIdToImageMapping(
      context.resources.getResourceName(R.drawable.outline_add_2_24),
      R.drawable.outline_add_2_24
    )
  }
}

@MultiRoundDevicesWithFontScalePreviews
fun mindfulnessPreview(context: Context) =
  TilePreviewData(Meditation.resources(context)) {
    TilePreviewHelper.singleTimelineEntryTileBuilder(
      Meditation.listLayout(context, it.deviceConfiguration, 3)
    )
      .build()
  }

class MindfulnessTileService : BaseTileService() {
  override fun layout(context: Context, deviceParameters: DeviceParameters): LayoutElement =
    Meditation.listLayout(context, deviceParameters, 2)

  override fun resources(context: Context) = Meditation.resources(context)
}
