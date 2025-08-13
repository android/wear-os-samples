/*
 * Copyright 2025 The Android Open Source Project
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
import androidx.wear.protolayout.DeviceParametersBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ResourceBuilders.Resources
import androidx.wear.protolayout.TimelineBuilders.Timeline
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.RequestBuilders.ResourcesRequest
import androidx.wear.tiles.TileBuilders.Tile
import androidx.wear.tiles.TileService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

// This constant serves as a cache key for the tile's resources. The Tiles library
// uses this string to determine if the resources for this tile are current or if they
// need to be refetched.
//
// This value MUST be changed whenever any of the underlying image assets (e.g., drawables)
// for the tile are updated or added. If this version string remains the same after resource
// have been changed, the system will continue to use the old, cached set of resources.
//
// In a production app, this would ideally be a hash of the resources, automatically
// generated from the set of resources necessary to build the tile. In this sample, it's a
// manually-updated constant for simplicity.
const val RESOURCES_VERSION = "6ba4c6f1dc6f03516c8784397484bb0c4a63423f"

abstract class BaseTileService : TileService() {

    override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<Tile> =
        Futures.immediateFuture(
            Tile.Builder()
                .setResourcesVersion(RESOURCES_VERSION)
                .setTileTimeline(
                    Timeline.fromLayoutElement(layout(this, requestParams.deviceConfiguration))
                )
                .build()
        )

    override fun onTileResourcesRequest(
        requestParams: ResourcesRequest
    ): ListenableFuture<Resources> = Futures.immediateFuture(resources(this)(requestParams))

    abstract fun layout(
        context: Context,
        deviceParameters: DeviceParametersBuilders.DeviceParameters
    ): LayoutElementBuilders.LayoutElement

    abstract fun resources(context: Context): (ResourcesRequest) -> Resources
}
