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