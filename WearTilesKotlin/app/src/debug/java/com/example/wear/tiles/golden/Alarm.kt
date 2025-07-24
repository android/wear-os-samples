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
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.material3.ButtonDefaults.filledTonalButtonColors
import androidx.wear.protolayout.material3.CardDefaults.filledVariantCardColors
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.TitleCardStyle
import androidx.wear.protolayout.material3.Typography.DISPLAY_LARGE
import androidx.wear.protolayout.material3.Typography.DISPLAY_MEDIUM
import androidx.wear.protolayout.material3.Typography.TITLE_LARGE
import androidx.wear.protolayout.material3.Typography.TITLE_MEDIUM
import androidx.wear.protolayout.material3.icon
import androidx.wear.protolayout.material3.iconEdgeButton
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
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
import com.example.wear.tiles.tools.isLargeScreen
import com.example.wear.tiles.tools.resources
import java.time.LocalTime
import java.util.Locale

fun MaterialScope.styledTime(time: LocalTime): LayoutElement {
  val hour24 = time.hour
  val minute = time.minute

  val amPm = if (hour24 < 12) "AM" else "PM"

  var hour12 = hour24 % 12
  if (hour12 == 0) {
    hour12 = 12
  }

  val timeString = "$hour12:${String.format(Locale.US, "%02d", minute)}"

  return LayoutElementBuilders.Row.Builder()
    .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_BOTTOM)
    .addContent(
      text(
        text = timeString.layoutString,
        typography = if (isLargeScreen()) DISPLAY_LARGE else DISPLAY_MEDIUM
      )
    )
    .addContent(
      text(
        text = " $amPm".layoutString,
        typography = if (isLargeScreen()) TITLE_LARGE else TITLE_MEDIUM
      )
    )
    .build()
}

object Alarm {
  data class AlarmData(
    val timeUntilAlarm: String,
    val alarmTime: String,
    val alarmDays: String,
    val clickable: Clickable
  )

  fun layout(context: Context, deviceParameters: DeviceParameters, data: AlarmData) =
    materialScope(context, deviceParameters) {
      primaryLayout(
        titleSlot = { text("Alarm".layoutString) },
        mainSlot = {
          titleCard(
            onClick = data.clickable,
            title = {
              text(
                "Mon—Fri".layoutString,
                typography = if (isLargeScreen()) TITLE_LARGE else TITLE_MEDIUM,
                color = colorScheme.onSurfaceVariant
              )
            },
            content = { styledTime(LocalTime.parse(data.alarmTime)) },
            height = expand(),
            colors = filledVariantCardColors(),
            style =
            if (isLargeScreen()) {
              TitleCardStyle.extraLargeTitleCardStyle()
            } else {
              TitleCardStyle.defaultTitleCardStyle()
            }
          )
        },
        bottomSlot = {
          iconEdgeButton(
            onClick = data.clickable,
            colors = filledTonalButtonColors(),
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
}

@MultiRoundDevicesWithFontScalePreviews
internal fun alarmPreview(context: Context) =
  TilePreviewData(Alarm.resources(context)) {
    TilePreviewHelper.singleTimelineEntryTileBuilder(
      Alarm.layout(
        context,
        it.deviceConfiguration,
        Alarm.AlarmData(
          timeUntilAlarm = "Less than 1 min",
          alarmTime = "14:58",
          alarmDays = "Mon, Tue, Wed, Thu, Fri, Sat",
          clickable = clickable()
        )
      )
    )
      .build()
  }

class AlarmTileService : BaseTileService() {
  override fun layout(context: Context, deviceParameters: DeviceParameters): LayoutElement =
    Alarm.layout(
      context,
      deviceParameters,
      Alarm.AlarmData("Less than 1 min", "14:58", "Mon, Tue, Wed, Thu, Fri, Sat", clickable())
    )

  override fun resources(context: Context) = Alarm.resources(context)
}
