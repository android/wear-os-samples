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
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.material3.ButtonDefaults.filledButtonColors
import androidx.wear.protolayout.material3.ButtonDefaults.filledVariantButtonColors
import androidx.wear.protolayout.material3.ButtonGroupDefaults
import androidx.wear.protolayout.material3.ButtonGroupDefaults.DEFAULT_SPACER_BETWEEN_BUTTON_GROUPS
import androidx.wear.protolayout.material3.ButtonStyle
import androidx.wear.protolayout.material3.ButtonStyle.Companion.smallButtonStyle
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.TextButtonStyle.Companion.smallTextButtonStyle
import androidx.wear.protolayout.material3.Typography
import androidx.wear.protolayout.material3.Typography.BODY_EXTRA_SMALL
import androidx.wear.protolayout.material3.Typography.BODY_MEDIUM
import androidx.wear.protolayout.material3.Typography.LABEL_SMALL
import androidx.wear.protolayout.material3.Typography.NUMERAL_EXTRA_SMALL
import androidx.wear.protolayout.material3.Typography.NUMERAL_SMALL
import androidx.wear.protolayout.material3.button
import androidx.wear.protolayout.material3.buttonGroup
import androidx.wear.protolayout.material3.icon
import androidx.wear.protolayout.material3.iconEdgeButton
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.material3.textButton
import androidx.wear.protolayout.modifiers.LayoutModifier
import androidx.wear.protolayout.modifiers.clickable
import androidx.wear.protolayout.modifiers.contentDescription
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import com.example.wear.tiles.R
import com.example.wear.tiles.tools.MultiRoundDevicesWithFontScalePreviews
import com.example.wear.tiles.tools.addIdToImageMapping
import com.example.wear.tiles.tools.column
import com.example.wear.tiles.tools.isLargeScreen
import com.example.wear.tiles.tools.resources

object Timer {

  fun timer1Layout(context: Context, deviceParameters: DeviceParameters) =
    materialScope(context, deviceParameters) {
      primaryLayout(
        titleSlot = { text("Minutes".layoutString) },
        mainSlot = {
          if (isLargeScreen()) {
            column {
              setWidth(expand())
              setHeight(expand())
              addContent(
                buttonGroup {
                  buttonGroupItem { timerTextButton1("5") }
                  buttonGroupItem { timerTextButton1("10") }
                }
              )
              addContent(ButtonGroupDefaults.DEFAULT_SPACER_BETWEEN_BUTTON_GROUPS)
              addContent(
                buttonGroup {
                  buttonGroupItem { timerTextButton1("15") }
                  buttonGroupItem { timerTextButton1("20") }
                  buttonGroupItem { timerTextButton1("30") }
                }
              )
            }
          } else {
            buttonGroup {
              buttonGroupItem { timerTextButton1("5") }
              buttonGroupItem { timerTextButton1("10") }
              buttonGroupItem { timerTextButton1("15") }
            }
          }
        },
        bottomSlot = {
          iconEdgeButton(
            onClick = clickable(),
            colors = filledButtonColors(),
            modifier = LayoutModifier.contentDescription("Plus"),
            iconContent = { icon(context.resources.getResourceName(R.drawable.outline_add_2_24)) }
          )
        }
      )
    }

  fun timer2Layout(context: Context, deviceParameters: DeviceParameters, clickable: Clickable) =
    materialScope(context = context, deviceConfiguration = deviceParameters) {
      primaryLayout(
        mainSlot = {
          column {
            setWidth(expand())
            setHeight(expand())
            addContent(
              buttonGroup {
                buttonGroupItem { timerButton("1:00", "Hour") }
                buttonGroupItem { timerButton("5", "Mins") }
              }
            )
            addContent(DEFAULT_SPACER_BETWEEN_BUTTON_GROUPS)
            addContent(
              buttonGroup {
                buttonGroupItem { timerButton("15", "Mins") }
                buttonGroupItem { timerButton("20", "Mins") }
                buttonGroupItem { timerButton("25", "Mins") }
              }
            )
            build()
          }
        },
        bottomSlot = {
          iconEdgeButton(
            onClick = clickable,
            colors =
            filledButtonColors()
              .copy(containerColor = colorScheme.tertiary, labelColor = colorScheme.onTertiary),
            modifier = LayoutModifier.contentDescription("Plus"),
            iconContent = { icon(context.resources.getResourceName(R.drawable.outline_add_2_24)) }
          )
        }
      )
    }

  fun resources(context: Context) = resources {
    addIdToImageMapping(
      context.resources.getResourceName(R.drawable.outline_add_2_24),
      R.drawable.outline_add_2_24
    )
  }

  private fun MaterialScope.timerTextButton1(text: String) =
    textButton(
      width = expand(),
      height = expand(),
      onClick = clickable(),
      shape = shapes.large,
      colors = filledVariantButtonColors(),
      labelContent = { text(text.layoutString, typography = NUMERAL_SMALL) }
    )
}

private fun MaterialScope.timerButton(firstLine: String?, secondLine: String? = null) =
  timerButton3(firstLine, secondLine)

private fun MaterialScope.timerButton1(firstLine: String?, secondLine: String? = null) =
  button(
    onClick = clickable(),
    width = expand(),
    height = expand(),
    colors = filledVariantButtonColors(),
    style = smallButtonStyle(),
    horizontalAlignment = HORIZONTAL_ALIGN_CENTER,
    labelContent = { text(firstLine?.layoutString ?: "".layoutString) },
    secondaryLabelContent = { text(secondLine?.layoutString ?: "".layoutString) }
  )

private fun MaterialScope.timerButton2(mainNumber: String?, secondaryText: String?): LayoutElement {
  // We must use an existing ButtonStyle from its companion object.
  // Choose the one that provides a good base, then customize padding.
  val baseButtonStyle = ButtonStyle.defaultButtonStyle() // or ButtonStyle.smallButtonStyle()

  return button(
    onClick = clickable(),
    width = DimensionBuilders.wrap(),
    height = DimensionBuilders.wrap(),
    // Pass the base style directly. The typography will be overridden by the text composables.
    style = baseButtonStyle,
    horizontalAlignment = LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER,
    //    contentPadding = ModifiersBuilders.Padding.Builder()
    //      .setStart(DimensionBuilders.dp(8f))
    //      .setEnd(DimensionBuilders.dp(8f))
    //      .setTop(DimensionBuilders.dp(4f))
    //      .setBottom(DimensionBuilders.dp(4f))
    //      .setRtlAware(true)
    //      .build(),
    labelContent = {
      text(
        text = mainNumber?.layoutString ?: "".layoutString,
        typography =
        androidx.wear.protolayout.material3.Typography
          .NUMERAL_EXTRA_SMALL // Apply large typography to the first line
        // Consider setting scalable = false if you want fixed size regardless of user font settings
        // scalable = false
      )
    },
    secondaryLabelContent = {
      text(
        text = secondaryText?.layoutString ?: "".layoutString,
        typography =
        androidx.wear.protolayout.material3.Typography
          .LABEL_SMALL // Apply small typography to the second line
        // scalable = false
      )
    }
  )
}

private fun MaterialScope.timerButton3(firstLine: String?, secondLine: String? = null) =
  textButton(
    onClick = clickable(),
    colors = filledVariantButtonColors(),
    width = expand(),
    height = expand(),
    style = smallTextButtonStyle(),
    labelContent = {
      column {
        setWidth(expand())
        setHorizontalAlignment(HORIZONTAL_ALIGN_CENTER)
        addContent(
          text(
            text = firstLine?.layoutString ?: "".layoutString,
            typography = if (isLargeScreen()) NUMERAL_EXTRA_SMALL else BODY_MEDIUM
          )
        )
        addContent(
          text(
            text = secondLine?.layoutString ?: "".layoutString,
            typography = if (isLargeScreen()) LABEL_SMALL else BODY_EXTRA_SMALL
          )
        )
      }
    }
  )

@MultiRoundDevicesWithFontScalePreviews
fun timer1LayoutPreview(context: Context) =
  TilePreviewData(Timer.resources(context)) {
    TilePreviewHelper.singleTimelineEntryTileBuilder(
      Timer.timer1Layout(context, it.deviceConfiguration)
    )
      .build()
  }

@MultiRoundDevicesWithFontScalePreviews
fun timer2LayoutPreview(context: Context) =
  TilePreviewData(Meditation.resources(context)) {
    TilePreviewHelper.singleTimelineEntryTileBuilder(
      Timer.timer2Layout(context, it.deviceConfiguration, clickable())
    )
      .build()
  }

class Timer1TileService : BaseTileService() {
  override fun layout(context: Context, deviceParameters: DeviceParameters): LayoutElement =
    Timer.timer1Layout(context, deviceParameters)

  override fun resources(context: Context) = Timer.resources(context)
}

class Timer2TileService : BaseTileService() {
  override fun layout(
    context: Context,
    deviceParameters: DeviceParameters
  ): LayoutElement =
    Timer.timer2Layout(context, deviceParameters, clickable())

  override fun resources(context: Context) = Timer.resources(context)
}
