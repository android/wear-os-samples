/*
 * Copyright 2021 The Android Open Source Project
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
package com.example.wear.tiles.media

import androidx.core.content.ContextCompat
import androidx.wear.tiles.TileProviderService
import androidx.wear.tiles.ColorBuilders.argb
import androidx.wear.tiles.DeviceParametersBuilders
import androidx.wear.tiles.DeviceParametersBuilders.DeviceParameters
import androidx.wear.tiles.DimensionBuilders.dp
import androidx.wear.tiles.LayoutElementBuilders
import androidx.wear.tiles.LayoutElementBuilders.Column
import androidx.wear.tiles.LayoutElementBuilders.FontStyles
import androidx.wear.tiles.LayoutElementBuilders.Layout
import androidx.wear.tiles.LayoutElementBuilders.Row
import androidx.wear.tiles.LayoutElementBuilders.Spacer
import androidx.wear.tiles.LayoutElementBuilders.Text
import androidx.wear.tiles.ResourceBuilders.AndroidImageResourceByResId
import androidx.wear.tiles.ResourceBuilders.ImageResource
import androidx.wear.tiles.ResourceBuilders.Resources
import androidx.wear.tiles.TileBuilders.Tile
import androidx.wear.tiles.TimelineBuilders.Timeline
import androidx.wear.tiles.TimelineBuilders.TimelineEntry
import androidx.wear.tiles.RequestBuilders.ResourcesRequest
import androidx.wear.tiles.RequestBuilders.TileRequest
import com.example.wear.tiles.R
import com.example.wear.tiles.components.IconButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.guava.future

// Updating this version triggers a new call to onResourcesRequest(). This is useful for dynamic
// resources, the contents of which change even though their id stays the same (e.g. a graph).
// In this sample, our resources are all fixed, so we use a constant value.
private const val RESOURCES_VERSION = "1"

// Resource identifiers for images
private const val ID_IC_LIBRARY_MUSIC = "ic_library_music"
private const val ID_IC_PLAY = "ic_play"
private const val ID_AVATAR = "avatar"

// Dimensions
private val AVATAR_SIZE = dp(24f)
private val SPACING_AVATAR_ARTIST = dp(14f)
private val SPACING_ARTIST_SONG = dp(8f)
private val SPACING_LIBRARY_PLAY = dp(22f)

/**
 * Creates a Media Tile, showing the song you played last with an option to resume playback or go
 * to your library. This is a demo tile only, so the buttons don't actually work.
 *
 * The main function, [onTileRequest], is triggered when the system calls for a tile and implements
 * ListenableFuture which allows the Tile to be returned asynchronously.
 *
 * Resources are provided with the [onResourcesRequest] method, which is triggered when the tile
 * uses an Image.
 */
class PlayNextSongTileService : TileProviderService() {
    // For coroutines, use a custom scope we can cancel when the service is destroyed
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onTileRequest(request: TileRequest) = serviceScope.future {
        Tile.builder().apply {
            setResourcesVersion(RESOURCES_VERSION)
            // Creates a timeline to hold one or more tile entries for a specific time periods.
            setTimeline(
                Timeline.builder().addTimelineEntry(
                    TimelineEntry.builder().setLayout(
                        Layout.builder().setRoot(tileLayout(request.deviceParameters!!))
                    )
                )
            )
        }.build()
    }

    private fun tileLayout(deviceParameters: DeviceParameters) = Column.builder()
        .addContent(
            LayoutElementBuilders.Image.builder()
                .setResourceId(ID_AVATAR)
                .setWidth(AVATAR_SIZE)
                .setHeight(AVATAR_SIZE)
        )
        .addContent(Spacer.builder().setHeight(SPACING_AVATAR_ARTIST))
        .addContent(
            Text.builder().setText(getString(R.string.tile_media_artist_name))
                .setFontStyle(
                    FontStyles.title3(deviceParameters).setColor(
                        argb(ContextCompat.getColor(baseContext, R.color.primary))
                    )
                )
        )
        .addContent(
            Text.builder().setText(getString(R.string.tile_media_song_name))
                .setFontStyle(FontStyles.title2(deviceParameters))
        )
        .addContent(Spacer.builder().setHeight(SPACING_ARTIST_SONG))
        .addContent(
            Row.builder()
                .addContent(
                    IconButton(
                        context = this@PlayNextSongTileService,
                        resourceId = ID_IC_LIBRARY_MUSIC,
                        backgroundColor = R.color.primaryDark
                    )
                )
                .addContent(Spacer.builder().setWidth(SPACING_LIBRARY_PLAY))
                .addContent(
                    IconButton(
                        context = this@PlayNextSongTileService,
                        resourceId = ID_IC_PLAY,
                        backgroundColor = R.color.primaryDark
                    )
                )
        )

    override fun onResourcesRequest(request: ResourcesRequest) = serviceScope.future {
        val resources = listOf(
            ID_IC_LIBRARY_MUSIC to R.drawable.ic_library_music,
            ID_IC_PLAY to R.drawable.ic_play,
            ID_AVATAR to R.drawable.avatar,
        )
        Resources.builder()
            .setVersion(RESOURCES_VERSION)
            .apply {
                resources.forEach { (name, resId) ->
                    addIdToImageMapping(
                        name,
                        ImageResource.builder()
                            .setAndroidResourceByResId(
                                AndroidImageResourceByResId.builder()
                                    .setResourceId(resId)
                            )
                    )
                }
            }
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cleans up the coroutine
        serviceJob.cancel()
    }
}
