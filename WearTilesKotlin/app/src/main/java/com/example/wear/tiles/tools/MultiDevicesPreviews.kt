package com.example.wear.tiles.tools

import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.tooling.preview.devices.WearDevices

@Preview(device = WearDevices.SMALL_ROUND, name = "Small Round")
@Preview(device = WearDevices.LARGE_ROUND, name = "Large Round")
internal annotation class MultiRoundDevicesPreviews

@Preview(device = WearDevices.SMALL_ROUND, name = "Small Round")
@Preview(device = WearDevices.SMALL_ROUND, fontScale = 1.24f, name = "Small Round 1.24f")
@Preview(device = WearDevices.LARGE_ROUND, name = "Large Round")
@Preview(device = WearDevices.LARGE_ROUND, fontScale = 0.94f, name = "Large Round 0.94f")
internal annotation class MultiRoundDevicesWithFontScalePreviews
