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
package com.example.wear.tiles.tools

import android.content.res.Resources
import android.graphics.Color
import android.util.DisplayMetrics
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.wear.tiles.ColorBuilders.argb
import androidx.wear.tiles.DeviceParametersBuilders
import androidx.wear.tiles.DimensionBuilders.ExpandedDimensionProp
import androidx.wear.tiles.LayoutElementBuilders
import androidx.wear.tiles.LayoutElementBuilders.Box
import androidx.wear.tiles.LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER
import androidx.wear.tiles.LayoutElementBuilders.LayoutElement
import androidx.wear.tiles.LayoutElementBuilders.VERTICAL_ALIGN_CENTER
import androidx.wear.tiles.ModifiersBuilders.Background
import androidx.wear.tiles.ModifiersBuilders.Modifiers
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.ResourceBuilders
import androidx.wear.tiles.StateBuilders.State
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TimelineBuilders
import com.example.wear.tiles.messaging.MessagingTileRenderer
import com.example.wear.tiles.util.TileRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlin.math.roundToInt

@Composable
fun <T> TileRendererPreview(state: T, renderer: TileRenderer<T>) {
    val context = LocalContext.current
    val resources = context.resources

    val requestParams = remember { requestParams(resources) }

    val tile = remember(state) { renderer.tileRequest(state, requestParams) }
    val resourceParams =
        remember(tile.resourcesVersion) { resourceParams(resources, tile.resourcesVersion) }
    val tileResources = remember(state) { renderer.resourcesRequest(state, resourceParams) }

    TilePreview(tile, tileResources)
}

@Composable
fun TilePreview(
    tile: TileBuilders.Tile,
    tileResources: ResourceBuilders.Resources
) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            FrameLayout(it).apply {
                this.setBackgroundColor(android.graphics.Color.DKGRAY)
            }
        },
        update = {

            val tileRenderer = androidx.wear.tiles.renderer.TileRenderer(
                /* uiContext = */ it.context,
                /* layout = */ tile.timeline?.timelineEntries?.first()?.layout!!,
                /* resources = */ tileResources,
                /* loadActionExecutor = */ Dispatchers.IO.asExecutor(),
                /* loadActionListener = */ {}
            )

            tileRenderer.inflate(it)
        }
    )
}

@Composable
fun LayoutPreview(
    layout: LayoutElement,
    tileResources: ResourceBuilders.Resources = ResourceBuilders.Resources.Builder().build()
) {
    val tile = remember {
        TileBuilders.Tile.Builder().setResourcesVersion(MessagingTileRenderer.RESOURCES_VERSION)
            // Creates a timeline to hold one or more tile entries for a specific time periods.
            .setTimeline(
                TimelineBuilders.Timeline.Builder().addTimelineEntry(
                    TimelineBuilders.TimelineEntry.Builder().setLayout(
                        LayoutElementBuilders.Layout.Builder().setRoot(
                            Box.Builder()
                                .setHorizontalAlignment(HORIZONTAL_ALIGN_CENTER)
                                .setVerticalAlignment(VERTICAL_ALIGN_CENTER)
                                .setHeight(ExpandedDimensionProp.Builder().build())
                                .setWidth(ExpandedDimensionProp.Builder().build())
                                .setModifiers(
                                    Modifiers.Builder().setBackground(
                                        Background.Builder()
                                            .setColor(argb(Color.DKGRAY))
                                            .build()
                                    ).build()
                                ).addContent(
                                    Box.Builder()
                                        .setHorizontalAlignment(HORIZONTAL_ALIGN_CENTER)
                                        .setVerticalAlignment(VERTICAL_ALIGN_CENTER)
                                        .setModifiers(
                                            Modifiers.Builder().setBackground(
                                                Background.Builder().setColor(
                                                    argb(Color.BLACK)
                                                ).build()
                                            ).build()
                                        )
                                        .addContent(layout).build()
                                ).build()
                        ).build()
                    ).build()
                ).build()
            ).build()
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            FrameLayout(it)
        },
        update = {

            val tileRenderer = androidx.wear.tiles.renderer.TileRenderer(
                /* uiContext = */ it.context,
                /* layout = */ tile.timeline?.timelineEntries?.first()?.layout!!,
                /* resources = */ tileResources,
                /* loadActionExecutor = */ Dispatchers.IO.asExecutor(),
                /* loadActionListener = */ {}
            )

            tileRenderer.inflate(it)
        }
    )
}

@Preview(
    backgroundColor = 0xff000000, showBackground = true, widthDp = 100, heightDp = 100
)
public annotation class IconSizePreview

private fun requestParams(resources: Resources) =
    RequestBuilders.TileRequest.Builder().setDeviceParameters(buildDeviceParameters(resources))
        .setState(State.Builder().build()).build()

private fun resourceParams(resources: Resources, version: String) =
    RequestBuilders.ResourcesRequest.Builder().setDeviceParameters(buildDeviceParameters(resources))
        .setVersion(version).build()

fun buildDeviceParameters(resources: Resources): DeviceParametersBuilders.DeviceParameters {
    val displayMetrics: DisplayMetrics = resources.displayMetrics
    val isScreenRound: Boolean = resources.configuration.isScreenRound
    return DeviceParametersBuilders.DeviceParameters.Builder()
        .setScreenWidthDp((displayMetrics.widthPixels / displayMetrics.density).roundToInt())
        .setScreenHeightDp((displayMetrics.heightPixels / displayMetrics.density).roundToInt())
        .setScreenDensity(displayMetrics.density).setScreenShape(
            if (isScreenRound) DeviceParametersBuilders.SCREEN_SHAPE_ROUND
            else DeviceParametersBuilders.SCREEN_SHAPE_RECT
        ).setDevicePlatform(DeviceParametersBuilders.DEVICE_PLATFORM_WEAR_OS).build()
}
