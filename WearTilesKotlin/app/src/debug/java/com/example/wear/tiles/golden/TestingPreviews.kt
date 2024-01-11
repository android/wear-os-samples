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
package com.example.wear.tiles.golden

import android.content.Context
import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import com.example.wear.tiles.R
import com.example.wear.tiles.tools.emptyClickable
import com.google.android.horologist.tiles.images.drawableResToImageResource

/**
 * b/238560022 misaligned because we can't add an offset, small preview is clipped
 */
@Preview
fun JustATest(context: Context) = TilePreviewData(resources {
}) {
    TilePreviewHelper.singleTimelineEntryTileBuilder(
        Weather.layout(
            context,
            it.deviceConfiguration,
            location = "San Francisco",
            weatherIconId = Weather.SCATTERED_SHOWERS_ICON_ID,
            currentTemperature = "52°",
            lowTemperature = "48°",
            highTemperature = "64°",
            weatherSummary = "Showers"
        )
    ).build()
}
