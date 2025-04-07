package com.google.samples.marketplace.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.google.samples.marketplace.data.WatchFaceData
import com.google.samples.marketplace.data.WatchFaceSlotInfo
import com.google.samples.marketplace.viewmodel.WatchFaceMarketplaceViewModel

/** Renders a list of watch face items. */
@Composable
fun WatchFacesPage(
    viewModel: WatchFaceMarketplaceViewModel,
    onWatchFaceSelected: (String) -> Unit = {},
) {
    val watchFaceState by viewModel.watchFaceState.collectAsStateWithLifecycle()

    WatchFacesPage(
        watchFaces = watchFaceState.watchFaces,
        onWatchFaceSelected = onWatchFaceSelected,
        isLoading = watchFaceState.isLoading
    )
}

@Composable
fun WatchFacesPage(
    watchFaces: List<WatchFaceData>,
    isLoading: Boolean = false,
    onWatchFaceSelected: (String) -> Unit = {},
) {
    val listState = rememberTransformingLazyColumnState()
    // TODO: Add contentPadding parameter to screen scaffold once available in M3
    ScreenScaffold(
        scrollState = listState
    ) { contentPadding ->
        TransformingLazyColumn(
            state = listState,
            contentPadding = contentPadding
        ) {
            items(watchFaces) { watchFaceData ->
                WatchFaceButton(
                    watchFaceData = watchFaceData,
                    onWatchFaceSelected = onWatchFaceSelected,
                    isLoading = isLoading
                )
            }
        }
    }
}

@WearPreviewDevices
@Composable
fun WatchFacesPageSameVersionPreview() {
    WatchFacesPage(
        watchFaces = createWatchFaceData(10),
        onWatchFaceSelected = {}
    )
}

@WearPreviewDevices
@Composable
fun WatchFacesPageUpgradablePreview() {
    WatchFacesPage(
        watchFaces = createWatchFaceData(1),
        onWatchFaceSelected = {}
    )
}

@WearPreviewDevices
@Composable
fun WatchFacesPageDowngradePreview() {
    WatchFacesPage(
        watchFaces = createWatchFaceData(15),
        onWatchFaceSelected = {}
    )
}

private fun createWatchFaceData(currentVersion: Long) =
    listOf(
        WatchFaceData(
            name = "Watch Face 1",
            assetPath = "",
            packageName = "com.example.watchface1",
            versionCode = 10,
            slotInfo = WatchFaceSlotInfo(
                packageName = "com.example.watchface1",
                slotId = "123",
                versionCode = currentVersion,
                isActive = true,
            )
        ),
        WatchFaceData(
            name = "Watch Face 2",
            assetPath = "",
            packageName = "com.example.watchface2",
            versionCode = 10
        ),
        WatchFaceData(
            name = "Watch Face 3",
            assetPath = "",
            packageName = "com.example.watchface3",
            versionCode = 10
        ),
    )
