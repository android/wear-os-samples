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
package com.example.wear.tiles.messaging

import android.graphics.Bitmap
import androidx.lifecycle.lifecycleScope
import androidx.wear.tiles.RequestBuilders.ResourcesRequest
import androidx.wear.tiles.RequestBuilders.TileRequest
import androidx.wear.tiles.ResourceBuilders.Resources
import androidx.wear.tiles.TileBuilders.Tile
import coil.Coil
import coil.ImageLoader
import com.google.android.horologist.tiles.SuspendingTileService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Creates a Messaging tile, showing up to 4 contacts, an icon button and compact chip.
 *
 * It extends [CoroutinesTileService], a Coroutine-friendly wrapper around
 * [androidx.wear.tiles.TileService], and implements [tileRequest] and [resourcesRequest].
 *
 * The main function, [tileRequest], is triggered when the system calls for a tile. Resources are
 * provided with the [resourcesRequest] method, which is triggered when the tile uses an Image.
 */
class MessagingTileService : SuspendingTileService() {
    private lateinit var repo: MessagingRepo
    private lateinit var imageLoader: ImageLoader
    private lateinit var renderer: MessagingTileRenderer
    private lateinit var tileStateFlow: StateFlow<MessagingTileState?>

    override fun onCreate() {
        super.onCreate()

        repo = MessagingRepo(this)
        imageLoader = Coil.imageLoader(this)
        renderer = MessagingTileRenderer(this)

        tileStateFlow = repo.getFavoriteContacts()
            .map { contacts -> MessagingTileState(contacts) }
            .stateIn(
                lifecycleScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    }

    /**
     * Read the latest data, delegating to the [MessagingTileRenderer] which creates a layout to
     * which it binds the state.
     */
    override suspend fun tileRequest(requestParams: TileRequest): Tile {
        val tileState: MessagingTileState = latestTileState()
        return renderer.renderTimeline(tileState, requestParams)
    }

    /**
     * Reads the latest state from the flow, and updates the data if there isn't any.
     */
    private suspend fun latestTileState(): MessagingTileState {
        var tileState = tileStateFlow.filterNotNull().first()

        // see `refreshData()` docs for more information
        if (tileState.contacts.isEmpty()) {
            refreshData()
            tileState = tileStateFlow.filterNotNull().first()
        }
        return tileState
    }

    /**
     * If our data source (the repository) is empty/has stale data, this is where we could perform
     * an update. For this sample, we're updating the repository with fake data
     * ([MessagingRepo.knownContacts]).
     *
     * In a more complete example, tiles, complications and the main app (/overlay) would
     * share a common data source so it's less likely that an initial data refresh triggered by the
     * tile would be necessary.
     */
    private suspend fun refreshData() {
        repo.updateContacts(MessagingRepo.knownContacts)
    }

    /**
     * Downloads bitmaps from the network and passes them to [MessagingTileRenderer] to add as
     * image resources (alongside any local resources).
     */
    override suspend fun resourcesRequest(requestParams: ResourcesRequest): Resources {
        val avatars = fetchAvatarsFromNetwork(requestParams)
        return renderer.produceRequestedResources(avatars, requestParams)
    }

    /**
     * Each contact in the tile state could have an avatar (represented by image url). We fetch them
     * from the network in this suspending function, and return the resulting bitmaps.
     */
    private suspend fun fetchAvatarsFromNetwork(
        requestParams: ResourcesRequest
    ): Map<Contact, Bitmap> {
        val tileState: MessagingTileState = latestTileState()
        val requestedAvatars: List<Contact> = if (requestParams.resourceIds.isEmpty()) {
            tileState.contacts
        } else {
            tileState.contacts.filter {
                requestParams.resourceIds.contains(MessagingTileRenderer.ID_CONTACT_PREFIX + it.id)
            }
        }

        val images = coroutineScope {
            requestedAvatars.map { contact ->
                async {
                    val image = imageLoader.loadAvatar(this@MessagingTileService, contact)
                    image?.let { contact to it }
                }
            }
        }.awaitAll().filterNotNull().toMap()

        return images
    }
}
