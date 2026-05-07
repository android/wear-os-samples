/*
 * Copyright 2022-2026 The Android Open Source Project
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
@file:Suppress("ktlint:standard:max-line-length")

package com.example.wear.tiles.golden

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.LayoutElementBuilders.CONTENT_SCALE_MODE_CROP
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.ProtoLayoutScope
import androidx.wear.protolayout.TimelineBuilders.Timeline
import androidx.wear.protolayout.layout.androidImageResource
import androidx.wear.protolayout.layout.imageResource
import androidx.wear.protolayout.material3.ButtonColors
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.Typography.BODY_LARGE
import androidx.wear.protolayout.material3.Typography.BODY_MEDIUM
import androidx.wear.protolayout.material3.backgroundImage
import androidx.wear.protolayout.material3.card
import androidx.wear.protolayout.material3.materialScopeWithResources
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.material3.textEdgeButton
import androidx.wear.protolayout.modifiers.clickable
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileService
import androidx.wear.tiles.tile
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import com.example.wear.tiles.R
import com.example.wear.tiles.tools.MultiRoundDevicesWithFontScalePreviews
import com.example.wear.tiles.tools.isLargeScreen
import com.google.common.util.concurrent.Futures
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneOffset

object News {
    data class ArticleData(
        val date: String,
        val headline: String,
        val clickable: Clickable,
        @DrawableRes val imageResourceId: Int
    )

    private fun MaterialScope.newsCard(
        clickable: Clickable,
        headline: String,
        @DrawableRes imageResourceId: Int
    ) = card(
        onClick = clickable,
        width = expand(),
        height = expand(),
        backgroundContent = {
            backgroundImage(
                resource = imageResource(androidImageResource(imageResourceId)),
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

    fun layout(
        context: Context,
        scope: ProtoLayoutScope,
        deviceParameters: DeviceParameters,
        data: ArticleData
    ) = materialScopeWithResources(context, scope, deviceParameters) {
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
}

@MultiRoundDevicesWithFontScalePreviews
internal fun newsPreview(context: Context): TilePreviewData =
    TilePreviewData { request ->
        val now = LocalDateTime.of(2024, 8, 1, 0, 0).toInstant(ZoneOffset.UTC)
        Clock.fixed(now, Clock.systemUTC().zone)

        TilePreviewHelper
            .singleTimelineEntryTileBuilder(
                News.layout(
                    context,
                    request.scope,
                    request.deviceConfiguration,
                    News.ArticleData(
                        headline = "Millions still without power as new storm moves across US",
                        date = "Today, 31 July",
                        clickable = clickable(),
                        imageResourceId = R.drawable.photo_15
                    )
                )
            ).build()
    }

class NewsTileService : TileService() {
    override fun onTileRequest(requestParams: RequestBuilders.TileRequest) =
        Futures.immediateFuture(
            tile(
                Timeline.fromLayoutElement(
                    News.layout(
                        this,
                        requestParams.scope,
                        requestParams.deviceConfiguration,
                        News.ArticleData(
                            headline = "Millions still without power as new storm moves across US",
                            date = "Today, 31 July",
                            clickable = clickable(),
                            imageResourceId = R.drawable.photo_15
                        )
                    )
                )
            )
        )
}
