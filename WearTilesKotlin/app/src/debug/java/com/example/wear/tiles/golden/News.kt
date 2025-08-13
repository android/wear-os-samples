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
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.material3.ButtonColors
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.Typography.BODY_LARGE
import androidx.wear.protolayout.material3.Typography.BODY_MEDIUM
import androidx.wear.protolayout.material3.backgroundImage
import androidx.wear.protolayout.material3.card
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
import com.example.wear.tiles.tools.isLargeScreen
import com.example.wear.tiles.tools.resources
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneOffset

object News {
    data class ArticleData(
        val date: String,
        val headline: String,
        val clickable: Clickable,
        val imageResourceId: String
    )

    private fun MaterialScope.newsCard(
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

    fun layout(context: Context, deviceParameters: DeviceParameters, data: ArticleData) =
        materialScope(context = context, deviceConfiguration = deviceParameters) {
            primaryLayout(
                titleSlot = { text(data.date.layoutString) },
                mainSlot = { newsCard(data.clickable, data.headline, data.imageResourceId) },
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
        addIdToImageMapping(
            context.resources.getResourceName(R.drawable.photo_15),
            R.drawable.photo_15
        )
    }
}

@MultiRoundDevicesWithFontScalePreviews
internal fun newsPreview(context: Context): TilePreviewData {
    val imageResourceId = context.resources.getResourceName(R.drawable.photo_15)
    return TilePreviewData(News.resources(context)) {
        val now = LocalDateTime.of(2024, 8, 1, 0, 0).toInstant(ZoneOffset.UTC)
        Clock.fixed(now, Clock.systemUTC().zone)

        TilePreviewHelper.singleTimelineEntryTileBuilder(
            News.layout(
                context,
                it.deviceConfiguration,
                News.ArticleData(
                    headline = "Millions still without power as new storm moves across US",
                    date = "Today, 31 July",
                    clickable = clickable(),
                    imageResourceId = imageResourceId
                )
            )
        )
            .build()
    }
}

class NewsTileService : BaseTileService() {
    override fun layout(
        context: Context,
        deviceParameters: DeviceParameters
    ): LayoutElementBuilders.LayoutElement {
        val imageResourceId = context.resources.getResourceName(R.drawable.photo_15)
        return News.layout(
            context,
            deviceParameters,
            News.ArticleData(
                headline = "Millions still without power as new storm moves across US",
                date = "Today, 31 July",
                clickable = clickable(),
                imageResourceId = imageResourceId
            )
        )
    }

    override fun resources(context: Context) = News.resources(context)
}
