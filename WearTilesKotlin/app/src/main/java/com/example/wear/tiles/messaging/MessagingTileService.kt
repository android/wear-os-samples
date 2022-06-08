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
import coil.ImageLoader
import com.example.wear.tiles.TilesApplication
import com.google.android.horologist.tiles.CoroutinesTileService
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
 * Creates a Messaging Tile, showing your favorite contacts and a button to search other contacts.
 * This is a demo tile only, so the buttons don't actually work.
 *
 * The main function, [onTileRequest], is triggered when the system calls for a tile and implements
 * ListenableFuture which allows the Tile to be returned asynchronously.
 *
 * Resources are provided with the [onResourcesRequest] method, which is triggered when the tile
 * uses an Image.
 */
class MessagingTileService : CoroutinesTileService() {
    private lateinit var tileStateFlow: StateFlow<MessagingTileState?>
    private lateinit var renderer: MessagingTileRenderer
    private lateinit var repo: MessagingRepo
    private lateinit var imageLoader: ImageLoader

    override fun onCreate() {
        super.onCreate()

        val appContainer = (application as TilesApplication).appContainer
        renderer = appContainer.renderer
        repo = appContainer.repo
        imageLoader = ImageLoader(this)

        tileStateFlow = repo.getFavoriteContacts()
            .map {
                buildState(it)
            }
            .stateIn(
                lifecycleScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    }

    private fun buildState(contacts: List<Contact>): MessagingTileState {
        return MessagingTileState(contacts)
    }

    override suspend fun tileRequest(requestParams: TileRequest): Tile {
        val tileState = readTileState()

        return renderer.renderTile(tileState, requestParams)
    }

    private suspend fun readTileState(): MessagingTileState {
        var tileState = tileStateFlow.filterNotNull().first()

        if (tileState.contacts.isEmpty()) {
            updateContacts()
            tileState = tileStateFlow.filterNotNull().first()
        }
        return tileState
    }

    private suspend fun updateContacts() {
        repo.updateContacts(MessagingRepo.knownContacts)
    }

    override suspend fun resourcesRequest(requestParams: ResourcesRequest): Resources {
        val tileState = readTileState()

        val images = generateRequestedResources(tileState, requestParams)

        return renderer.produceRequestedResources(images, requestParams)
    }

    suspend fun generateRequestedResources(
        tileState: MessagingTileState,
        requestParams: ResourcesRequest,
    ): Map<Contact, Bitmap> {
        val requestedAvatars = if (requestParams.resourceIds.isEmpty()) {
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
