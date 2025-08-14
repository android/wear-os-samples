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
// need to be refetched. You may need to set this dynamically to ensure the correct
// resources are available, for example if either resources or layouts are dynamically
// generated and change independently of app updates.
const val RESOURCES_VERSION = "1"

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
