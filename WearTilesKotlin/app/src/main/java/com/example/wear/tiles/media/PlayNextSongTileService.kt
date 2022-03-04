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
import androidx.wear.tiles.ActionBuilders
import androidx.wear.tiles.ColorBuilders.argb
import androidx.wear.tiles.DeviceParametersBuilders.DeviceParameters
import androidx.wear.tiles.DimensionBuilders.dp
import androidx.wear.tiles.LayoutElementBuilders.Column
import androidx.wear.tiles.LayoutElementBuilders.FontStyles
import androidx.wear.tiles.LayoutElementBuilders.Image
import androidx.wear.tiles.LayoutElementBuilders.Layout
import androidx.wear.tiles.LayoutElementBuilders.Row
import androidx.wear.tiles.LayoutElementBuilders.Spacer
import androidx.wear.tiles.LayoutElementBuilders.Text
import androidx.wear.tiles.ModifiersBuilders.Clickable
import androidx.wear.tiles.ModifiersBuilders.Modifiers
import androidx.wear.tiles.ModifiersBuilders.Semantics
import androidx.wear.tiles.RequestBuilders.ResourcesRequest
import androidx.wear.tiles.RequestBuilders.TileRequest
import androidx.wear.tiles.ResourceBuilders.AndroidImageResourceByResId
import androidx.wear.tiles.ResourceBuilders.ImageResource
import androidx.wear.tiles.ResourceBuilders.Resources
import androidx.wear.tiles.TileBuilders.Tile
import androidx.wear.tiles.TimelineBuilders.Timeline
import androidx.wear.tiles.TimelineBuilders.TimelineEntry
import com.example.wear.tiles.CoroutinesTileService
import com.example.wear.tiles.R
import com.example.wear.tiles.components.IconButton

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
class PlayNextSongTileService : CoroutinesTileService() {

    override suspend fun tileRequest(requestParams: TileRequest): Tile {
        return Tile.Builder().apply {
            setResourcesVersion(RESOURCES_VERSION)
            // Creates a timeline to hold one or more tile entries for a specific time periods.
            setTimeline(
                Timeline.Builder()
                    .addTimelineEntry(
                        TimelineEntry.Builder()
                            .setLayout(
                                Layout.Builder()
                                    .setRoot(tileLayout(requestParams.deviceParameters!!)).build()
                            )
                            .build()
                    )
                    .build()
            )
        }.build()
    }

    private fun tileLayout(deviceParameters: DeviceParameters) = Column.Builder()
        .addContent(
            Image.Builder()
                .setResourceId(ID_AVATAR)
                .setWidth(AVATAR_SIZE)
                .setHeight(AVATAR_SIZE)
                .build()
        )
        .addContent(Spacer.Builder().setHeight(SPACING_AVATAR_ARTIST).build())
        .addContent(
            Text.Builder()
                .setText(getString(R.string.tile_media_artist_name))
                .setFontStyle(
                    FontStyles
                        .title3(deviceParameters)
                        .setColor(
                            argb(ContextCompat.getColor(baseContext, R.color.primary))
                        )
                        .build()
                )
                .build()
        )
        .addContent(
            Text.Builder()
                .setText(getString(R.string.tile_media_song_name))
                .setFontStyle(FontStyles.title2(deviceParameters).build())
                .build()
        )
        .addContent(Spacer.Builder().setHeight(SPACING_ARTIST_SONG).build())
        .addContent(
            Row.Builder()
                .addContent(
                    IconButton(
                        context = this@PlayNextSongTileService,
                        resourceId = ID_IC_LIBRARY_MUSIC,
                        backgroundColor = R.color.primaryDark,
                        contentDescription = getString(R.string.tile_media_library),
                        clickable = Clickable.Builder()
                            .setOnClick(ActionBuilders.LoadAction.Builder().build())
                            .build()
                    )
                )
                .addContent(Spacer.Builder().setWidth(SPACING_LIBRARY_PLAY).build())
                .addContent(
                    IconButton(
                        context = this@PlayNextSongTileService,
                        resourceId = ID_IC_PLAY,
                        backgroundColor = R.color.primaryDark,
                        contentDescription = getString(R.string.tile_media_play),
                        clickable = Clickable.Builder()
                            .setOnClick(ActionBuilders.LoadAction.Builder().build())
                            .build()
                    )
                )
                .build()
        )
        .setModifiers(
            Modifiers.Builder()
                .setSemantics(
                    Semantics.Builder()
                        .setContentDescription(
                            getString(
                                R.string.tile_media_content_description,
                                getString(R.string.tile_media_song_name),
                                getString(R.string.tile_media_artist_name)
                            )
                        )
                        .build()
                )
                .build()
        )
        .build()

    override suspend fun resourcesRequest(requestParams: ResourcesRequest): Resources {
        val resources = listOf(
            ID_IC_LIBRARY_MUSIC to R.drawable.ic_library_music,
            ID_IC_PLAY to R.drawable.ic_play,
            ID_AVATAR to R.drawable.avatar,
        )
        return Resources.Builder()
            .setVersion(RESOURCES_VERSION)
            .apply {
                resources.forEach { (name, resId) ->
                    addIdToImageMapping(
                        name,
                        ImageResource.Builder()
                            .setAndroidResourceByResId(
                                AndroidImageResourceByResId.Builder()
                                    .setResourceId(resId)
                                    .build()
                            )
                            .build()
                    )
                }
            }
            .build()
    }
}
