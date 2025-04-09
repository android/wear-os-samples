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

package com.google.samples.marketplace.data

/**
 * Represents a watch face APK that is embedded in the assets.
 *
 * @property name The watch face name.
 * @property assetPath The path to the watch face in the assets.
 * @property packageName The watch face package name.
 * @property versionCode The watch face version code.
 * @property slotInfo The slot information associated with the watch face. It is null if the watch
 *   face is not installed.
 */
data class WatchFaceData(
    val name: String,
    val assetPath: String,
    val packageName: String,
    val versionCode: Long,
    val slotInfo: WatchFaceSlotInfo? = null
)

/**
 * Information about the watch face slot that may be occupied by a watch face in the Watch Face Push
 * API.
 *
 * @property slotId The ID of the slot.
 * @property versionCode The version code of the watch face that is currently occupying the slot.
 * @property customVersion The custom version associated with a watch face. This is read from the
 *   watch face properties in the manifest.
 */
data class WatchFaceSlotInfo(
    val packageName: String,
    val slotId: String,
    val versionCode: Long,
    val customVersion: String? = null,
    val isActive: Boolean
)

data class WatchFaceSlots(
    val installedWatchFaces: List<WatchFaceSlotInfo>,
    val unusedSlots: Int
)
