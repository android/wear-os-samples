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
import androidx.wear.protolayout.LayoutElementBuilders.CONTENT_SCALE_MODE_CROP
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.material3.PrimaryLayoutMargins.Companion.MIN_PRIMARY_LAYOUT_MARGIN
import androidx.wear.protolayout.material3.TitleCardStyle
import androidx.wear.protolayout.material3.backgroundImage
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.material3.titleCard
import androidx.wear.protolayout.modifiers.clickable
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import com.example.wear.tiles.R
import com.example.wear.tiles.tools.MultiRoundDevicesWithFontScalePreviews
import com.example.wear.tiles.tools.addIdToImageMapping
import com.example.wear.tiles.tools.noOpElement
import com.example.wear.tiles.tools.resources

object HeartRate {

  fun layout(context: Context, deviceParameters: DeviceParameters, data: HeartRateData) =
    materialScope(context, deviceParameters) {
      primaryLayout(
        margins = MIN_PRIMARY_LAYOUT_MARGIN,
        titleSlot = { text("Heart rate".layoutString) },
        mainSlot = {
          titleCard(
            onClick = clickable(),
            height = expand(),
            backgroundContent = {
              backgroundImage(
                protoLayoutResourceId = data.imageResourceId,
                overlayColor = null,
                contentScaleMode = CONTENT_SCALE_MODE_CROP
              )
            },
            title = { noOpElement() },
            content = { noOpElement() },
            shape = shapes.full,
            style = TitleCardStyle.smallTitleCardStyle()
          )
        },
        bottomSlot = { text("${data.value} bpm".layoutString) }
      )
    }

  fun resources(context: Context) = resources {
    addIdToImageMapping(context.resources.getResourceName(R.drawable.photo_38), R.drawable.photo_38)
  }

  data class HeartRateData(val imageResourceId: String, val value: Int)
}

@MultiRoundDevicesWithFontScalePreviews
fun heartRatePreview(context: Context) =
  TilePreviewData(HeartRate.resources(context)) {
    TilePreviewHelper.singleTimelineEntryTileBuilder(
      HeartRate.layout(
        context,
        it.deviceConfiguration,
        HeartRate.HeartRateData(
          imageResourceId = context.resources.getResourceName(R.drawable.photo_38),
          value = 72
        )
      )
    )
      .build()
  }

class HeartRateTileService : BaseTileService() {
  override fun layout(context: Context, deviceParameters: DeviceParameters): LayoutElement =
    HeartRate.layout(
      context,
      deviceParameters,
      HeartRate.HeartRateData(
        imageResourceId = context.resources.getResourceName(R.drawable.photo_38),
        value = 72
      )
    )

  override fun resources(context: Context) = HeartRate.resources(context)
}
