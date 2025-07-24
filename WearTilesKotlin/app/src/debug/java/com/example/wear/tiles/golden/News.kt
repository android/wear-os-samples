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
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.DimensionBuilders.weight
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.CONTENT_SCALE_MODE_CROP
import androidx.wear.protolayout.LayoutElementBuilders.Spacer
import androidx.wear.protolayout.ModifiersBuilders.Background
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.ModifiersBuilders.Modifiers
import androidx.wear.protolayout.material3.ButtonColors
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.Typography.BODY_LARGE
import androidx.wear.protolayout.material3.Typography.BODY_MEDIUM
import androidx.wear.protolayout.material3.Typography.BODY_SMALL
import androidx.wear.protolayout.material3.Typography.LABEL_SMALL
import androidx.wear.protolayout.material3.backgroundImage
import androidx.wear.protolayout.material3.card
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.material3.textEdgeButton
import androidx.wear.protolayout.modifiers.LayoutModifier
import androidx.wear.protolayout.modifiers.background
import androidx.wear.protolayout.modifiers.clickable
import androidx.wear.protolayout.modifiers.padding
import androidx.wear.protolayout.modifiers.toProtoLayoutModifiers
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import com.example.wear.tiles.R
import com.example.wear.tiles.tools.MultiRoundDevicesWithFontScalePreviews
import com.example.wear.tiles.tools.addIdToImageMapping
import com.example.wear.tiles.tools.column
import com.example.wear.tiles.tools.image
import com.example.wear.tiles.tools.isLargeScreen
import com.example.wear.tiles.tools.resources
import com.example.wear.tiles.tools.row
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

object News {
  data class ArticleData(
    val date: String,
    val headline: String,
    val newsVendor: String,
    val clickable: Clickable,
    val imageResourceId: String
  )

  private fun MaterialScope.newsImage(resourceId: String): LayoutElementBuilders.LayoutElement {
    return image {
      setResourceId(resourceId)
      setContentScaleMode(CONTENT_SCALE_MODE_CROP)
      setWidth(expand())
      setHeight(expand())
      setModifiers(
        Modifiers.Builder()
          .setBackground(Background.Builder().setCorner(shapes.large).build())
          .build()
      )
    }
  }

  private fun MaterialScope.newsText(
    headline: String,
    newsVendor: String
  ): LayoutElementBuilders.LayoutElement {
    return column {
      setWidth(expand())
      setHeight(expand())
      addContent(
        text(
          text = headline.layoutString,
          typography = LABEL_SMALL,
          maxLines = if (isLargeScreen()) 4 else 3,
          alignment = LayoutElementBuilders.TEXT_ALIGN_START
        )
      )
      addContent(Spacer.Builder().setHeight(dp(3f)).build())
      addContent(
        text(
          text = newsVendor.layoutString,
          typography = BODY_SMALL,
          color = colorScheme.onSurfaceVariant,
          maxLines = 1,
          alignment = LayoutElementBuilders.TEXT_ALIGN_START
        )
      )
    }
  }

  private fun MaterialScope.newsCard1(
    clickable: Clickable,
    headline: String,
    newsVendor: String,
    imageResourceId: String
  ) =
    card(
      onClick = clickable,
      width = expand(),
      height = expand(),
      modifier = LayoutModifier.background(colorScheme.surfaceContainer),
      content = {
        row {
          setWidth(expand())
          setHeight(expand())
          setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
          addContent(
            LayoutElementBuilders.Box.Builder()
              .setWidth(weight(0.4f))
              .setHeight(expand())
              .setModifiers(LayoutModifier.padding(2f).toProtoLayoutModifiers())
              .addContent(
                newsImage(imageResourceId)
              )
              .build()
          )
          addContent(Spacer.Builder().setWidth(dp(12f)).build())
          addContent(
            LayoutElementBuilders.Box.Builder()
              .setWidth(weight(0.6f))
              .setHeight(expand())
              .setModifiers(
                LayoutModifier.padding(horizontal = 0f, vertical = 10f)
                  .toProtoLayoutModifiers()
              )
              .addContent(newsText(headline, newsVendor))
              .build()
          )
        }
      }
    )

  fun layout1(
    context: Context,
    deviceParameters: DeviceParameters,
    data: ArticleData
  ) =
    materialScope(
      context = context,
      deviceConfiguration = deviceParameters
    ) {
      primaryLayout(
        titleSlot = { text(data.date.layoutString) },
        mainSlot = {
          newsCard1(
            data.clickable,
            data.headline,
            data.newsVendor,
            data.imageResourceId
          )
        },
        bottomSlot = {
          textEdgeButton(
            colors =
            ButtonColors(
              labelColor = colorScheme.onSurface,
              containerColor = colorScheme.surfaceContainer
            ),
            onClick = data.clickable,
            labelContent = { text("News".layoutString) }
          )
        }
      )
    }

  private fun MaterialScope.newsCard2(
    clickable: Clickable,
    headline: String,
    imageResourceId: String
  ) =
    card(
      onClick = clickable,
      width = expand(),
      height = expand(),
      backgroundContent = {
        backgroundImage(
          protoLayoutResourceId = imageResourceId,
          contentScaleMode = CONTENT_SCALE_MODE_CROP
        )
      },
      content = {
        text(
          text = headline.layoutString,
          maxLines = 3,
          typography = if (isLargeScreen()) BODY_LARGE else BODY_MEDIUM
        )
      }
    )

  fun layout2(
    context: Context,
    deviceParameters: DeviceParameters,
    data: ArticleData
  ) =
    materialScope(
      context = context,
      deviceConfiguration = deviceParameters
    ) {
      primaryLayout(
        titleSlot = { text(data.date.layoutString) },
        mainSlot = { newsCard2(data.clickable, data.headline, data.imageResourceId) },
        bottomSlot = {
          textEdgeButton(
            colors =
            ButtonColors(
              labelColor = colorScheme.onSurface,
              containerColor = colorScheme.surfaceContainer
            ),
            onClick = data.clickable,
            labelContent = { text("News".layoutString) }
          )
        }
      )
    }

  fun resources(context: Context) = resources {
    addIdToImageMapping(context.resources.getResourceName(R.drawable.photo_15), R.drawable.photo_15)
  }

  internal fun LocalDate.formatLocalDateTime(today: LocalDate = LocalDate.now()): String {
    val yesterday = today.minusDays(1)

    return when {
      this == yesterday -> "yesterday ${format(DateTimeFormatter.ofPattern("MMM d"))}"
      this == today -> "today ${format(DateTimeFormatter.ofPattern("MMM d"))}"
      else -> format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))
    }
  }
}

@MultiRoundDevicesWithFontScalePreviews
internal fun news1Preview(context: Context): TilePreviewData {
  val imageResourceId = context.resources.getResourceName(R.drawable.photo_15)
  return TilePreviewData(
    News.resources(context)
  ) {
    val now = LocalDateTime.of(2024, 8, 1, 0, 0).toInstant(ZoneOffset.UTC)
    Clock.fixed(now, Clock.systemUTC().zone)

    TilePreviewHelper.singleTimelineEntryTileBuilder(
      News.layout1(
        context,
        it.deviceConfiguration,
        News.ArticleData(
          headline = "Millions still without power as new storm moves across US",
          newsVendor = "The New York Times",
          date = "Today, 31 July",
          clickable = clickable(),
          imageResourceId = imageResourceId
        )
      )
    )
      .build()
  }
}

@MultiRoundDevicesWithFontScalePreviews
internal fun news2Preview(context: Context): TilePreviewData {
  val imageResourceId = context.resources.getResourceName(R.drawable.photo_15)
  return TilePreviewData(
    News.resources(context)
  ) {
    val now = LocalDateTime.of(2024, 8, 1, 0, 0).toInstant(ZoneOffset.UTC)
    Clock.fixed(now, Clock.systemUTC().zone)

    TilePreviewHelper.singleTimelineEntryTileBuilder(
      News.layout2(
        context,
        it.deviceConfiguration,
        News.ArticleData(
          headline = "Millions still without power as new storm moves across US",
          newsVendor = "The New York Times",
          date = "Today, 31 July",
          clickable = clickable(),
          imageResourceId = imageResourceId
        )
      )
    )
      .build()
  }
}

class News1TileService : BaseTileService() {
  override fun layout(
    context: Context,
    deviceParameters: DeviceParameters
  ): LayoutElementBuilders.LayoutElement {
    val imageResourceId = context.resources.getResourceName(R.drawable.photo_15)
    return News.layout1(
      context,
      deviceParameters,
      News.ArticleData(
        headline = "Millions still without power as new storm moves across US",
        newsVendor = "The New York Times",
        date = "Today, 31 July",
        clickable = clickable(),
        imageResourceId = imageResourceId
      )
    )
  }

  override fun resources(context: Context) = News.resources(context)
}

class News2TileService : BaseTileService() {
  override fun layout(
    context: Context,
    deviceParameters: DeviceParameters
  ): LayoutElementBuilders.LayoutElement {
    val imageResourceId = context.resources.getResourceName(R.drawable.photo_15)
    return News.layout2(
      context,
      deviceParameters,
      News.ArticleData(
        headline = "Millions still without power as new storm moves across US",
        newsVendor = "The New York Times",
        date = "Today, 31 July",
        clickable = clickable(),
        imageResourceId = imageResourceId
      )
    )
  }

  override fun resources(context: Context) = News.resources(context)
}
