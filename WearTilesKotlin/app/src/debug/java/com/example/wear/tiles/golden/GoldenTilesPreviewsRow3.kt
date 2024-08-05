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
import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.wear.tiles.R
import com.example.wear.tiles.tools.emptyClickable
import com.google.android.horologist.tiles.images.drawableResToImageResource
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * b/238560022 misaligned because we can't add an offset, small preview is clipped
 */
@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
fun Weather(context: Context) = TilePreviewData(resources {
    addIdToImageMapping(
        Weather.SCATTERED_SHOWERS_ICON_ID,
        drawableResToImageResource(R.drawable.scattered_showers)
    )
}) {
    TilePreviewHelper.singleTimelineEntryTileBuilder(
        Weather.layout(
            context,
            it.deviceConfiguration,
            location = "San Francisco",
            weatherIconId = Weather.SCATTERED_SHOWERS_ICON_ID,
            currentTemperature = "52°",
            lowTemperature = "48°",
            highTemperature = "64°",
            weatherSummary = "Showers"
        )
    ).build()
}

/**
 * b/238556504 alignment doesn't match figma.
 */
@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
fun News(context: Context) = TilePreviewData {
    val now = LocalDateTime.of(2024, 8, 1, 0, 0).toInstant(ZoneOffset.UTC)
    val clock = Clock.fixed(now, Clock.systemUTC().zone)

    TilePreviewHelper.singleTimelineEntryTileBuilder(
        News.layout(
            context,
            it.deviceConfiguration,
            headline = "Millions still without power as new storm moves across US",
            newsVendor = "The New York Times",
            date = LocalDate.now(clock).minusDays(1),
            clock = clock,
            clickable = emptyClickable
        )
    ).build()
}

/**
 * b/238571095 Alignment doesn't match Figma
 */
@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
fun Calendar(context: Context) = TilePreviewData {
    TilePreviewHelper.singleTimelineEntryTileBuilder(
        Calendar.layout(
            context,
            it.deviceConfiguration,
            eventTime = "6:30-7:30 PM",
            eventName = "Morning Pilates with Christina Lloyd",
            eventLocation = "216 Market Street",
            clickable = emptyClickable
        )
    ).build()
}

@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
fun Social(context: Context) = TilePreviewData(resources {
    addIdToImageMapping(Social.AVATAR_ID_1, drawableResToImageResource(R.drawable.avatar1))
    addIdToImageMapping(Social.AVATAR_ID_2, drawableResToImageResource(R.drawable.avatar2))
}) {
    TilePreviewHelper.singleTimelineEntryTileBuilder(
        Social.layout(
            context,
            it.deviceConfiguration,
            Social.Contact(
                initials = "AC",
                clickable = emptyClickable,
                avatarId = Social.AVATAR_ID_1
            ),
            Social.Contact(initials = "AD", clickable = emptyClickable, avatarId = null),
            Social.Contact(
                initials = "BD",
                color = GoldenTilesColors.Purple,
                clickable = emptyClickable,
                avatarId = null
            ),
            Social.Contact(
                initials = "DC",
                clickable = emptyClickable,
                avatarId = Social.AVATAR_ID_2
            )
        )
    ).build()
}

@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
fun Media(context: Context) = TilePreviewData(resources {
    addIdToImageMapping(
        Media.CHIP_1_ICON_ID,
        drawableResToImageResource(R.drawable.ic_music_queue_24)
    )
    addIdToImageMapping(
        Media.CHIP_2_ICON_ID,
        drawableResToImageResource(R.drawable.ic_podcasts_24)
    )
}) {
    TilePreviewHelper.singleTimelineEntryTileBuilder(
        Media.layout(
            context,
            it.deviceConfiguration,
            playlist1 = Media.Playlist(
                label = "Liked songs",
                iconId = Media.CHIP_1_ICON_ID,
                clickable = emptyClickable
            ),
            playlist2 = Media.Playlist(
                label = "Podcasts",
                iconId = Media.CHIP_2_ICON_ID,
                clickable = emptyClickable
            ),
            browseClickable = emptyClickable
        )
    ).build()
}
