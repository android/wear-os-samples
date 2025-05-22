/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.marketplace.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnScope
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.google.samples.marketplace.R
import com.google.samples.marketplace.data.WatchFaceData
import com.google.samples.marketplace.data.WatchFaceSlotInfo

fun TransformingLazyColumnScope.watchFaceItem(
    name: String,
    watchFaceData: WatchFaceData,
    isUpdateable: Boolean,
    isUnusedSlotAvailable: Boolean,
    onSetActiveButtonClicked: () -> Unit,
    onInstallClick: () -> Unit,
    onUpdateClick: () -> Unit,
    onUninstallClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isInstalled = watchFaceData.slotInfo != null
    val isActive = watchFaceData.slotInfo?.isActive == true
    val customVersion = watchFaceData.slotInfo?.customVersion
    item { Text(name) }
    item { Text(watchFaceData.packageName) }
    item { Text(stringResource(R.string.version_code, watchFaceData.versionCode)) }
    item { Text(stringResource(R.string.installed, isInstalled)) }
    item { Text(stringResource(R.string.active, isActive)) }
    customVersion?.let {
        item { Text(stringResource(R.string.custom_version, it)) }
    }
    item {
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = isInstalled && !isActive,
            onClick = onSetActiveButtonClicked,
        ) {
            Text(stringResource(R.string.set_active))
        }
    }
    item {
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !isInstalled && isUnusedSlotAvailable,
            onClick = onInstallClick,
        ) {
            Text(stringResource(R.string.install))
        }
    }
    item {
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = isUpdateable,
            onClick = onUpdateClick,
        ) {
            Text(stringResource(R.string.update))
        }
    }
    item {
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = isInstalled == true,
            onClick = onUninstallClick,
        ) {
            Text(stringResource(R.string.uninstall))
        }
    }
}

@WearPreviewDevices
@Composable
private fun WatchFaceItemPreview() {
    val scrollState = rememberTransformingLazyColumnState()
    TransformingLazyColumn(
        state = scrollState
    ) {
        val watchFaceData = WatchFaceData(
            name = "Watch Face 1",
            assetPath = "",
            packageName = "com.example.watchface1",
            versionCode = 10,
            slotInfo = WatchFaceSlotInfo(
                packageName = "com.example.watchface1",
                slotId = "123",
                versionCode = 10,
                isActive = true,
                customVersion = "1.2.3"
            )
        )
        watchFaceItem(
            name = "Watchface 1",
            watchFaceData = watchFaceData,
            isUpdateable = false,
            isUnusedSlotAvailable = false,
            onSetActiveButtonClicked = { },
            onInstallClick = { },
            onUpdateClick = { },
            onUninstallClick = { }
        )
    }
}
