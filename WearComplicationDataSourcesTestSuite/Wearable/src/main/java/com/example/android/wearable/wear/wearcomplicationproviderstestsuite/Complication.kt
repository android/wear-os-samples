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
package com.example.android.wearable.wear.wearcomplicationproviderstestsuite

/**
 * The enumerated list of all complications provided by this app.
 */
enum class Complication(

    /**
     * A stable key that can be used to distinguish between this app's complications.
     */
    val key: String
) {
    ICON("Icon"),
    LARGE_IMAGE("LargeImage"),
    LONG_TEXT("LongText"),
    RANGED_VALUE("RangedValue"),
    SHORT_TEXT("ShortText"),
    SMALL_IMAGE("SmallImage"),
    WEIGHTED_ELEMENTS("WeightedElements")
}
