/*
 * Copyright 2021 The Android Open Source Project
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
package com.example.android.wearable.composeadvanced.data

/**
 * Simple Data source for a list of fake watch models.
 */
class WatchLocalDataSource {
    val watches = listOf(
        WatchModel(
            modelId = 100001,
            name = "Power Watch 1",
            description = "The Power Watch 1 was fictional watch that was released in lorem ipsum dolor sit amet."
        ),
        WatchModel(
            modelId = 100002,
            name = "Watch 2K",
            description = "The 2K Watch was fictional watch that was first conceived in lorem ipsum dolor sit amet."
        ),
        WatchModel(
            modelId = 100003,
            name = "Watch Z3",
            description = "The Watch Z3 is a fictional square watch in the early days of lorem ipsum dolor sit amet."
        ),
        WatchModel(
            modelId = 100004,
            name = "Super S Watch 4",
            description = "The Super S Watch 4 was fictional watch that was, well, super compared to its predecessor."
        ),
        WatchModel(
            modelId = 100005,
            name = "Watch C5",
            description = "The Watch C5 was fictional watch that was first released in lorem ipsum dolor sit amet."
        ),
        WatchModel(
            modelId = 100006,
            name = "Super T6 Watch",
            description = "The Super T6 Watch was fictional watch that was and upgrade over the T5."
        ),
        WatchModel(
            modelId = 100007,
            name = "Wear 7",
            description = "The Wear 7 was a fictional watch that was first released in lorem ipsum dolor sit amet."
        ),
        WatchModel(
            modelId = 100008,
            name = "Final Watch 8",
            description = "The Final Watch 8 was fictional watch that was first released in lorem ipsum dolor sit amet."
        )
    )
}
