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
package com.example.wear.tiles.golden

import android.content.Context
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.DimensionBuilders.weight
import androidx.wear.protolayout.LayoutElementBuilders.CONTENT_SCALE_MODE_CROP
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.LayoutElementBuilders.Spacer
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.material3.ButtonDefaults.filledButtonColors
import androidx.wear.protolayout.material3.ButtonDefaults.filledTonalButtonColors
import androidx.wear.protolayout.material3.CardDefaults.filledVariantCardColors
import androidx.wear.protolayout.material3.backgroundImage
import androidx.wear.protolayout.material3.buttonGroup
import androidx.wear.protolayout.material3.icon
import androidx.wear.protolayout.material3.iconButton
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.material3.textButton
import androidx.wear.protolayout.material3.titleCard
import androidx.wear.protolayout.modifiers.LayoutModifier
import androidx.wear.protolayout.modifiers.clickable
import androidx.wear.protolayout.modifiers.contentDescription
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import com.example.wear.tiles.R
import com.example.wear.tiles.tools.MultiRoundDevicesWithFontScalePreviews
import com.example.wear.tiles.tools.addIdToImageMapping
import com.example.wear.tiles.tools.box
import com.example.wear.tiles.tools.column
import com.example.wear.tiles.tools.isLargeScreen
import com.example.wear.tiles.tools.resources

object Calendar {
  data class Event(
    val date: String,
    val time: String,
    val name: String,
    val location: String,
    val imageId: String? = null,
    val clickable: Clickable
  )

  fun layout(
    context: Context,
    deviceParameters: DeviceParameters,
    data: Event
  ) =
    materialScope(context, deviceParameters) {
      primaryLayout(
        mainSlot = {
          column {
            setWidth(expand())
            setHeight(expand())
            addContent(
              box {
                setWidth(expand())
                setHeight(weight(0.3f))
                addContent(
                  buttonGroup {
                    buttonGroupItem {
                      box {
                        setWidth(weight(0.6f))
                        setHeight(expand())
                        addContent(
                          textButton(
                            onClick = data.clickable,
                            labelContent = { text(data.date.layoutString) },
                            colors = filledTonalButtonColors(),
                            width = expand(),
                            height = expand()
                          )
                        )
                      }
                    }
                    buttonGroupItem {
                      box {
                        setWidth(weight(0.4f))
                        setHeight(expand())
                        addContent(
                          iconButton(
                            onClick = data.clickable,
                            iconContent = {
                              icon(context.resources.getResourceName(R.drawable.outline_add_24))
                            },
                            colors = filledButtonColors(),
                            modifier = LayoutModifier.contentDescription("Add Event"),
                            width = expand(),
                            height = expand()
                          )
                        )
                      }
                    }
                  }
                )
              }
            )
            addContent(Spacer.Builder().setHeight(dp(4f)).build())
            addContent(
              box {
                setWidth(expand())
                setHeight(weight(0.7f))
                addContent(
                  titleCard(
                    onClick = data.clickable,
                    title = {
                      text(
                        data.name.layoutString,
                        maxLines = if (isLargeScreen()) 3 else 2
                      )
                    },
                    content = {
                      column {
                        addContent(text(data.time.layoutString))
                        if (isLargeScreen()) {
                          addContent(text(data.location.layoutString, maxLines = 1))
                        }
                      }
                    },
                    colors = filledVariantCardColors(),
                    backgroundContent =
                    data.imageId?.let { id ->
                      {
                        backgroundImage(
                          protoLayoutResourceId = id,
                          contentScaleMode = CONTENT_SCALE_MODE_CROP
                        )
                      }
                    },
                    shape = shapes.extraLarge,
                    height = expand()
                  )
                )
              }
            )
          }
        }
      )
    }

  fun resources(context: Context) = resources {
    addIdToImageMapping(
      context.resources.getResourceName(R.drawable.outline_add_24),
      R.drawable.outline_add_24
    )
    addIdToImageMapping(context.resources.getResourceName(R.drawable.photo_38), R.drawable.photo_38)
  }
}

@MultiRoundDevicesWithFontScalePreviews
internal fun calendar1Preview(context: Context) = calendarPreviewX(context)

@MultiRoundDevicesWithFontScalePreviews
internal fun calendar2Preview(context: Context) =
  calendarPreviewX(context, context.resources.getResourceName(R.drawable.photo_38))

fun calendarPreviewX(context: Context, eventImageId: String? = null) =
  TilePreviewData(Calendar.resources(context)) {
    TilePreviewHelper.singleTimelineEntryTileBuilder(
      Calendar.layout(
        context,
        it.deviceConfiguration,
        Calendar.Event(
          date = "25 July",
          time = "6:30-7:30 PM",
          name = "Advanced Tennis Coaching with Christina Lloyd",
          location = "216 Market Street",
          imageId = eventImageId,
          clickable = clickable()
        )
      )
    )
      .build()
  }

class Calendar1TileService : BaseTileService() {
  override fun layout(
    context: Context,
    deviceParameters: DeviceParameters
  ): LayoutElement =
    Calendar.layout(
      context,
      deviceParameters,
      Calendar.Event(
        date = "25 July",
        time = "6:30-7:30 PM",
        name = "Advanced Tennis Coaching with Christina Lloyd",
        location = "216 Market Street",
        imageId = null,
        clickable = clickable()
      )
    )

  override fun resources(context: Context) = Calendar.resources(context)
}

class Calendar2TileService : BaseTileService() {
  override fun layout(
    context: Context,
    deviceParameters: DeviceParameters
  ): LayoutElement =
    Calendar.layout(
      context,
      deviceParameters,
      Calendar.Event(
        date = "25 July",
        time = "6:30-7:30 PM",
        name = "Advanced Tennis Coaching with Christina Lloyd",
        location = "216 Market Street",
        imageId = context.resources.getResourceName(R.drawable.photo_38),
        clickable = clickable()
      )
    )

  override fun resources(context: Context) = Calendar.resources(context)
}
