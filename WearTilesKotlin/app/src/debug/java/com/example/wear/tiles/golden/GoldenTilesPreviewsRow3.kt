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
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.wear.tiles.R
import com.example.wear.tiles.tools.WearLargeRoundDevicePreview
import com.example.wear.tiles.tools.WearSmallRoundDevicePreview
import com.example.wear.tiles.tools.emptyClickable
import com.google.android.horologist.compose.tools.LayoutRootPreview
import com.google.android.horologist.compose.tools.buildDeviceParameters
import com.google.android.horologist.tiles.images.drawableResToImageResource

/**
 * b/238560022 misaligned because we can't add an offset, small preview is clipped
 */
@WearSmallRoundDevicePreview
@WearLargeRoundDevicePreview
@Composable
fun Weather() {
    val context = LocalContext.current
    LayoutRootPreview(
        Weather.layout(
            context,
            context.deviceParams(),
            location = "San Francisco",
            weatherIconId = Weather.SCATTERED_SHOWERS_ICON_ID,
            currentTemperature = "52°",
            lowTemperature = "48°",
            highTemperature = "64°",
            weatherSummary = "Showers"
        )
    ) {
        addIdToImageMapping(
            Weather.SCATTERED_SHOWERS_ICON_ID,
            drawableResToImageResource(R.drawable.scattered_showers)
        )
    }
}

/**
 * b/238556504 alignment doesn't match figma.
 */
@WearSmallRoundDevicePreview
@WearLargeRoundDevicePreview
@Composable
fun News() {
    val context = LocalContext.current
    LayoutRootPreview(
        News.layout(
            context,
            context.deviceParams(),
            headline = "Millions still without power as new storm moves across US",
            newsVendor = "The New York Times",
            clickable = emptyClickable
        )
    )
}

/**
 * b/238571095 Alignment doesn't match Figma
 */
@WearSmallRoundDevicePreview
@WearLargeRoundDevicePreview
@Composable
fun Calendar() {
    val context = LocalContext.current
    LayoutRootPreview(
        Calendar.layout(
            context,
            context.deviceParams(),
            eventTime = "6:30-7:30 PM",
            eventName = "Morning Pilates with Christina Lloyd",
            eventLocation = "216 Market Street",
            clickable = emptyClickable
        )
    )
}

@WearSmallRoundDevicePreview
@WearLargeRoundDevicePreview
@Composable
fun Social() {
    val context = LocalContext.current
    LayoutRootPreview(
        Social.layout(
            context,
            context.deviceParams(),
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
    ) {
        addIdToImageMapping(Social.AVATAR_ID_1, drawableResToImageResource(R.drawable.avatar1))
        addIdToImageMapping(Social.AVATAR_ID_2, drawableResToImageResource(R.drawable.avatar2))
    }
}

@WearSmallRoundDevicePreview
@WearLargeRoundDevicePreview
@Composable
fun Media() {
    val context = LocalContext.current
    LayoutRootPreview(
        Media.layout(
            context,
            context.deviceParams(),
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
    ) {
        addIdToImageMapping(
            Media.CHIP_1_ICON_ID,
            drawableResToImageResource(R.drawable.ic_music_queue_24)
        )
        addIdToImageMapping(
            Media.CHIP_2_ICON_ID,
            drawableResToImageResource(R.drawable.ic_podcasts_24)
        )
    }
}

private fun Context.deviceParams() = buildDeviceParameters(resources)
