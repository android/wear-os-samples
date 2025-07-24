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
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.DimensionBuilders.weight
import androidx.wear.protolayout.LayoutElementBuilders.CONTENT_SCALE_MODE_CROP
import androidx.wear.protolayout.LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER
import androidx.wear.protolayout.LayoutElementBuilders.Image
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.LayoutElementBuilders.TEXT_ALIGN_CENTER
import androidx.wear.protolayout.ModifiersBuilders.Background
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.ModifiersBuilders.Modifiers
import androidx.wear.protolayout.material3.ButtonDefaults.filledButtonColors
import androidx.wear.protolayout.material3.ButtonDefaults.filledVariantButtonColors
import androidx.wear.protolayout.material3.Typography.BODY_SMALL
import androidx.wear.protolayout.material3.Typography.NUMERAL_SMALL
import androidx.wear.protolayout.material3.buttonGroup
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.material3.textButton
import androidx.wear.protolayout.material3.textEdgeButton
import androidx.wear.protolayout.modifiers.clickable
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import com.example.wear.tiles.R
import com.example.wear.tiles.tools.MultiRoundDevicesWithFontScalePreviews
import com.example.wear.tiles.tools.addIdToImageMapping
import com.example.wear.tiles.tools.box
import com.example.wear.tiles.tools.column
import com.example.wear.tiles.tools.resources

object Hike {
  data class HikeData(
    val distance: String,
    val unit: String,
    val clickable: Clickable
  )

  fun layout(
    context: Context,
    deviceParameters: DeviceParameters,
    data: HikeData
  ) =
    materialScope(context, deviceParameters) {
      primaryLayout(
        titleSlot = { text("Hike".layoutString) },
        bottomSlot = {
          textEdgeButton(onClick = data.clickable, colors = filledButtonColors(), labelContent = {
            text("Start".layoutString)
          })
        },
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
                        setWidth(weight(0.4f))
                        setHeight(expand())
                        addContent(
                          textButton(
                            width = expand(),
                            height = expand(),
                            onClick = data.clickable,
                            shape = shapes.extraLarge,
                            colors = filledVariantButtonColors(),
                            labelContent = {
                              column {
                                setHorizontalAlignment(HORIZONTAL_ALIGN_CENTER)
                                addContent(
                                  text(
                                    data.distance.layoutString,
                                    typography = NUMERAL_SMALL,
                                    alignment = TEXT_ALIGN_CENTER
                                  )
                                )
                                addContent(
                                  text(
                                    data.unit.layoutString,
                                    typography = BODY_SMALL,
                                    alignment = TEXT_ALIGN_CENTER
                                  )
                                )
                              }
                            }
                          )
                        )
                      }
                    }
                    buttonGroupItem {
                      box {
                        setWidth(weight(0.6f))
                        setHeight(expand())
                        addContent(
                          Image.Builder()
                            .setResourceId(context.resources.getResourceName(R.drawable.photo_14))
                            .setContentScaleMode(CONTENT_SCALE_MODE_CROP)
                            .setWidth(expand())
                            .setHeight(expand())
                            .setModifiers(
                              Modifiers.Builder()
                                .setBackground(Background.Builder().setCorner(shapes.large).build())
                                .setClickable(data.clickable)
                                .build()
                            )
                            .build()
                        )
                      }
                    }
                  }
                )
              }
            )
          }
        }
      )
    }

  fun resources(context: Context) = resources {
    addIdToImageMapping(context.resources.getResourceName(R.drawable.photo_14), R.drawable.photo_14)
  }
}

@MultiRoundDevicesWithFontScalePreviews
internal fun hikePreview(context: Context) =
  TilePreviewData(Hike.resources(context)) {
    TilePreviewHelper.singleTimelineEntryTileBuilder(
      Hike.layout(
        context,
        it.deviceConfiguration,
        Hike.HikeData(
          distance = "10",
          unit = "Miles",
          clickable = clickable()
        )
      )
    )
      .build()
  }

class HikeTileService : BaseTileService() {
  override fun layout(
    context: Context,
    deviceParameters: DeviceParameters
  ): LayoutElement =
    Hike.layout(
      context,
      deviceParameters,
      Hike.HikeData(
        distance = "10",
        unit = "Miles",
        clickable = clickable()
      )
    )

  override fun resources(context: Context) = Hike.resources(context)
}
