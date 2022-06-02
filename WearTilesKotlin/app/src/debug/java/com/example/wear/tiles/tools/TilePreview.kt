package com.example.wear.tiles.tools

import android.content.res.Resources
import android.util.DisplayMetrics
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.wear.tiles.DeviceParametersBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.StateBuilders.State
import com.example.wear.tiles.util.TileRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlin.math.roundToInt

@Composable
fun <T> TilePreview(state: T, renderer: TileRenderer<T>) {
    val context = LocalContext.current
    val resources = context.resources

    val requestParams = remember { requestParams(resources) }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            FrameLayout(it)
        }, update = {
            val tile = renderer.tileRequest(state, requestParams)
            val resourceParams = resourceParams(resources, tile.resourcesVersion)
            val tileResources = renderer.resourcesRequest(state, resourceParams)

            val tileRenderer = androidx.wear.tiles.renderer.TileRenderer(
                /* uiContext = */ it.context,
                /* layout = */ tile.timeline?.timelineEntries?.first()?.layout!!,
                /* resources = */ tileResources,
                /* loadActionExecutor = */ Dispatchers.IO.asExecutor(),
                /* loadActionListener = */ {}
            )

            tileRenderer.inflate(it)
        })
}

private fun requestParams(resources: Resources) = RequestBuilders.TileRequest.Builder()
    .setDeviceParameters(buildDeviceParameters(resources))
    .setState(State.Builder().build())
    .build()

private fun resourceParams(resources: Resources, version: String) =
    RequestBuilders.ResourcesRequest.Builder()
        .setDeviceParameters(buildDeviceParameters(resources))
        .setVersion(version)
        .build()

fun buildDeviceParameters(resources: Resources): DeviceParametersBuilders.DeviceParameters {
    val displayMetrics: DisplayMetrics = resources.displayMetrics
    val isScreenRound: Boolean = resources.configuration.isScreenRound
    return DeviceParametersBuilders.DeviceParameters.Builder()
        .setScreenWidthDp((displayMetrics.widthPixels / displayMetrics.density).roundToInt())
        .setScreenHeightDp((displayMetrics.heightPixels / displayMetrics.density).roundToInt())
        .setScreenDensity(displayMetrics.density)
        .setScreenShape(
            if (isScreenRound) DeviceParametersBuilders.SCREEN_SHAPE_ROUND
            else DeviceParametersBuilders.SCREEN_SHAPE_RECT
        )
        .setDevicePlatform(DeviceParametersBuilders.DEVICE_PLATFORM_WEAR_OS)
        .build()
}
