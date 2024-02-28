/*
 * Copyright 2024 The Android Open Source Project
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
package presentation

import kotlin.math.roundToInt

enum class WearDevice(
    val id: String,
    val modelName: String,
    val screenSizePx: Int,
    val density: Float
) {
    MobvoiTicWatchPro5(
        id = "ticwatch_pro_5",
        modelName = "Mobvoi TicWatch Pro 5",
        screenSizePx = 466,
        density = 2.0f,
    ),
    SamsungGalaxyWatch5(
        id = "galaxy_watch_5",
        modelName = "Samsung Galaxy Watch 5",
        screenSizePx = 396,
        density = 2.0f,
    ),
    SamsungGalaxyWatch6Large(
        id = "galaxy_watch_6",
        modelName = "Samsung Galaxy Watch 6 Large",
        screenSizePx = 480,
        density = 2.125f,
    ),
    GooglePixelWatch(
        id = "pixel_watch",
        modelName = "Google Pixel Watch",
        screenSizePx = 384,
        density = 2.0f,
    ),
    GenericSmallRound(
        id = "small_round",
        modelName = "Generic Small Round",
        screenSizePx = 384,
        density = 2.0f,
    ),
    GenericLargeRound(
        id = "large_round",
        modelName = "Generic Large Round",
        screenSizePx = 454,
        density = 2.0f,
    );

    val dp: Int = (screenSizePx / density).roundToInt()
}
