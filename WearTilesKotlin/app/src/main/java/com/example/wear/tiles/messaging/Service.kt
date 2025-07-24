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

import androidx.wear.protolayout.ResourceBuilders.Resources
import androidx.wear.protolayout.TimelineBuilders.Timeline
import androidx.wear.tiles.RequestBuilders.ResourcesRequest
import androidx.wear.tiles.RequestBuilders.TileRequest
import androidx.wear.tiles.TileBuilders.Tile
import coil3.ImageLoader
import coil3.SingletonImageLoader
import com.example.wear.tiles.R
import com.example.wear.tiles.tools.toImageResource
import com.google.android.horologist.tiles.SuspendingTileService
import java.util.UUID
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Creates a Messaging tile, showing up to 6 contacts, an icon button and an edge button.
 *
 * It extends [SuspendingTileService], a Coroutine-friendly wrapper around
 * [androidx.wear.tiles.TileService], and implements [tileRequest] and [resourcesRequest].
 *
 * The main function, [tileRequest], is triggered when the system calls for a tile. Resources are
 * provided with the [resourcesRequest] method, which is triggered when the tile uses an Image.
 */
class MessagingTileService : SuspendingTileService() {
  private lateinit var imageLoader: ImageLoader
  private lateinit var contacts: List<Contact>

  override fun onCreate() {
    super.onCreate()

    // For this sample, contacts is a simple static list. In a real application it might come
    // from an injected repository.
    contacts = getMockNetworkContacts()

    // For this sample, make Coil dumb by disabling all caching features.
    SingletonImageLoader.setSafe { context ->
      ImageLoader.Builder(context).memoryCache(null).diskCache(null).build()
    }

    imageLoader = SingletonImageLoader.get(this)
  }

  /** This method returns a Tile object, which describes the layout of the Tile. */
  override suspend fun tileRequest(requestParams: TileRequest): Tile {
    val layoutElement = tileLayout(this, requestParams.deviceConfiguration, contacts)

    // Resources are cached and keyed on resourcesVersion. If a Resources object with the same
    // resourcesVersion is present in the cache, resourcesRequest() is not called, and the
    // cached version is used instead. To ensure it is *always* called (e.g. for debugging), set
    // the version to a random string.
    val resourcesVersion =
      if (DEBUG_RESOURCES) {
        UUID.randomUUID().toString()
      } else {
        contacts.map { it.id }.toSortedSet().joinToString()
      }

    return Tile.Builder()
      .setResourcesVersion(resourcesVersion)
      .setTileTimeline(Timeline.fromLayoutElement(layoutElement))
      .build()
  }

  /**
   * This method returns a Tile Resources object that contains the image data required to render
   * and display the tile. It's passed a ResourcesRequest, one property of which is `resourceIds`
   * (a List<String>) which represents the resources we need to provide. (Note that this is just a
   * list of strings; these are *not* Android resource ids.)
   *
   * In this method we (1) find the Contacts that correspond to the passed resourceIds, (2)
   * asynchronously load the associated images, and then (3) add this data into a Resources
   * object.
   */
  override suspend fun resourcesRequest(requestParams: ResourcesRequest): Resources {
    // If resourceIds is empty, set resourceIds to all resources
    val resourceIds =
      requestParams.resourceIds.ifEmpty { contacts.map { it.imageResourceId() } }

    // Asynchronously load images associated with resourceIds, and create a Map<String,
    // ResourceBuilders.ImageResource> suitable for adding to the Resources object
    val resourceMap = coroutineScope {
      resourceIds
        .map { id ->
          async {
            contacts
              .find { it.imageResourceId() == id }
              ?.let { contact ->
                imageLoader.loadAvatar(this@MessagingTileService, contact)?.let {
                    image ->
                  id to image
                }
              } ?: (id to R.mipmap.offline.toImageResource())
          }
        }
        .awaitAll()
        .toMap()
    }

    // Add images to the Resources object, and return
    return Resources.Builder()
      .setVersion(requestParams.version)
      .apply {
        resourceMap.forEach { (id, imageResource) ->
          addIdToImageMapping(id, imageResource)
        }
      }
      .build()
  }
}

const val DEBUG_RESOURCES = true
