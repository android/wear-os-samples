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
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.material3.ButtonDefaults.filledTonalButtonColors
import androidx.wear.protolayout.material3.ButtonDefaults.filledVariantButtonColors
import androidx.wear.protolayout.material3.ButtonGroupDefaults.DEFAULT_SPACER_BETWEEN_BUTTON_GROUPS
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.PrimaryLayoutMargins
import androidx.wear.protolayout.material3.Typography.DISPLAY_MEDIUM
import androidx.wear.protolayout.material3.Typography.LABEL_SMALL
import androidx.wear.protolayout.material3.Typography.TITLE_LARGE
import androidx.wear.protolayout.material3.Typography.TITLE_MEDIUM
import androidx.wear.protolayout.material3.button
import androidx.wear.protolayout.material3.buttonGroup
import androidx.wear.protolayout.material3.icon
import androidx.wear.protolayout.material3.iconButton
import androidx.wear.protolayout.material3.iconDataCard
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.material3.textEdgeButton
import androidx.wear.protolayout.modifiers.clickable
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper.singleTimelineEntryTileBuilder
import com.example.wear.tiles.R
import com.example.wear.tiles.tools.MultiRoundDevicesWithFontScalePreviews
import com.example.wear.tiles.tools.addIdToImageMapping
import com.example.wear.tiles.tools.column
import com.example.wear.tiles.tools.isLargeScreen
import com.example.wear.tiles.tools.noOpElement
import com.example.wear.tiles.tools.resources
import com.google.android.horologist.tiles.images.drawableResToImageResource

object Workout {
  data class WorkoutData(val titleText: String, val contentText: String)

  fun layout1(
    context: Context,
    deviceParameters: DeviceParametersBuilders.DeviceParameters,
    data: WorkoutData
  ) =
    materialScope(context = context, deviceConfiguration = deviceParameters) {
      primaryLayout(
        titleSlot = { text(data.titleText.layoutString) },
        mainSlot = {
          buttonGroup {
            buttonGroupItem {
              iconButton(
                onClick = clickable(),
                width = expand(),
                height = if (isLargeScreen()) dp(90f) else expand(),
                colors = filledVariantButtonColors(),
                iconContent = {
                  icon(
                    protoLayoutResourceId =
                    context.resources.getResourceName(
                      R.drawable.self_improvement_24px
                    )
                  )
                }
              )
            }
            buttonGroupItem {
              iconDataCard(
                onClick = clickable(),
                width = if (isLargeScreen()) dp(80f) else expand(),
                height = expand(),
                shape = shapes.large,
                title = {
                  if (isLargeScreen()) {
                    text("30".layoutString, typography = DISPLAY_MEDIUM)
                  } else {
                    noOpElement()
                  }
                },
                content = {
                  if (isLargeScreen()) {
                    text("Mins".layoutString, typography = TITLE_MEDIUM)
                  } else {
                    noOpElement()
                  }
                },
                secondaryIcon = {
                  icon(
                    protoLayoutResourceId =
                    context.resources.getResourceName(R.drawable.ic_run_24)
                  )
                }
              )
            }
            buttonGroupItem {
              iconButton(
                onClick = clickable(),
                width = expand(),
                height = if (isLargeScreen()) dp(90f) else expand(),
                colors = filledVariantButtonColors(),
                iconContent = {
                  icon(
                    protoLayoutResourceId =
                    context.resources.getResourceName(
                      R.drawable.ic_cycling_24
                    )
                  )
                }
              )
            }
          }
        },
        bottomSlot = {
          textEdgeButton(onClick = clickable()) { text("More".layoutString) }
        }
      )
    }

  fun layout2(
    context: Context,
    deviceParameters: DeviceParameters,
    data: WorkoutData
  ) =
    materialScope(context = context, deviceConfiguration = deviceParameters) {
      primaryLayout(
        titleSlot = { text(data.titleText.layoutString) },
        margins = PrimaryLayoutMargins.MID_PRIMARY_LAYOUT_MARGIN,
        mainSlot = {
          if (isLargeScreen()) {
            column {
              setWidth(expand())
              setHeight(expand())
              addContent(
                workoutGraphicDataCard(
                  titleText = "Start Run",
                  contentText = data.contentText,
                  iconResourceName =
                  context.resources.getResourceName(R.drawable.ic_run_24)
                )
              )
              addContent(DEFAULT_SPACER_BETWEEN_BUTTON_GROUPS)
              addContent(
                buttonGroup {
                  buttonGroupItem {
                    iconButton(
                      onClick = clickable(),
                      height = expand(),
                      width = expand(),
                      colors = filledTonalButtonColors(),
                      iconContent = {
                        icon(
                          protoLayoutResourceId =
                          context.resources.getResourceName(
                            R.drawable.self_improvement_24px
                          )
                        )
                      }
                    )
                  }
                  buttonGroupItem {
                    iconButton(
                      onClick = clickable(),
                      width = expand(),
                      height = expand(),
                      colors = filledTonalButtonColors(),
                      iconContent = {
                        icon(
                          protoLayoutResourceId =
                          context.resources.getResourceName(
                            R.drawable.ic_run_24
                          )
                        )
                      }
                    )
                  }
                  buttonGroupItem {
                    iconButton(
                      onClick = clickable(),
                      width = expand(),
                      height = expand(),
                      colors = filledTonalButtonColors(),
                      iconContent = {
                        icon(
                          protoLayoutResourceId =
                          context.resources.getResourceName(
                            R.drawable.ic_cycling_24
                          )
                        )
                      }
                    )
                  }
                }
              )
            }
          } else {
            workoutGraphicDataCard(
              titleText = "Start Run",
              contentText = data.contentText,
              iconResourceName =
              context.resources.getResourceName(R.drawable.ic_run_24)
            )
          }
        },
        bottomSlot = {
          textEdgeButton(
            onClick = clickable(),
            colors = filledVariantButtonColors()
          ) {
            text("More".layoutString)
          }
        }
      )
    }

  private fun MaterialScope.workoutGraphicDataCard(
    titleText: String,
    contentText: String,
    iconResourceName: String
  ) =
    button(
      onClick = clickable(),
      height = expand(),
      width = expand(),
      labelContent = {
        text(
          titleText.layoutString,
          typography = TITLE_LARGE
        )
      },
      secondaryLabelContent = {
        text(
          contentText.layoutString,
          typography = LABEL_SMALL
        )
      },
      horizontalAlignment = LayoutElementBuilders.HORIZONTAL_ALIGN_START,
      iconContent = {
        icon(
          protoLayoutResourceId = iconResourceName,
          width = dp(36f),
          height = dp(36f)
        )
      }
    )

  fun resources(context: Context) = resources {
    addIdToImageMapping(
      context.resources.getResourceName(R.drawable.ic_run_24),
      R.drawable.ic_run_24
    )
    addIdToImageMapping(
      context.resources.getResourceName(R.drawable.self_improvement_24px),
      R.drawable.self_improvement_24px
    )
    addIdToImageMapping(
      context.resources.getResourceName(R.drawable.ic_cycling_24),
      R.drawable.ic_cycling_24
    )
    addIdToImageMapping(
      context.resources.getResourceName(R.drawable.ic_yoga_24),
      drawableResToImageResource(R.drawable.ic_yoga_24)
    )
    addIdToImageMapping(
      context.resources.getResourceName(R.drawable.outline_directions_walk_24),
      drawableResToImageResource(R.drawable.outline_directions_walk_24)
    )
  }
}

@MultiRoundDevicesWithFontScalePreviews
internal fun workoutLayout1Preview(context: Context) =
  TilePreviewData(onTileResourceRequest = Workout.resources(context)) {
    singleTimelineEntryTileBuilder(
      Workout.layout1(
        context,
        it.deviceConfiguration,
        Workout.WorkoutData("Exercise", "30 min goal")
      )
    )
      .build()
  }

@MultiRoundDevicesWithFontScalePreviews
internal fun workoutLayout2Preview(context: Context) =
  TilePreviewData(onTileResourceRequest = Workout.resources(context)) {
    singleTimelineEntryTileBuilder(
      Workout.layout2(
        context,
        it.deviceConfiguration,
        Workout.WorkoutData("Exercise", "30 min goal")
      )
    )
      .build()
  }

class WorkoutTileService1 : BaseTileService() {
  override fun layout(
    context: Context,
    deviceParameters: DeviceParameters
  ): LayoutElementBuilders.LayoutElement =
    Workout.layout1(
      context,
      deviceParameters,
      Workout.WorkoutData("Exercise", "30 min goal")
    )

  override fun resources(context: Context) = Workout.resources(context)
}

class WorkoutTileService2 : BaseTileService() {
  override fun layout(
    context: Context,
    deviceParameters: DeviceParameters
  ): LayoutElementBuilders.LayoutElement =
    Workout.layout2(
      context,
      deviceParameters,
      Workout.WorkoutData("Exercise", "30 min goal")
    )

  override fun resources(context: Context) = Workout.resources(context)
}
