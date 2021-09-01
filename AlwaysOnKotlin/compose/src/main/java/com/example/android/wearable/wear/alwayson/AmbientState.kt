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
package com.example.android.wearable.wear.alwayson

/**
 * A description of the current ambient state of the app.
 */
sealed interface AmbientState {

    /**
     * The app is interactive.
     */
    object Interactive : AmbientState

    /**
     * The app is in ambient mode, with the given parameters.
     */
    data class Ambient(
        /**
         * If the display is low-bit in ambient mode. i.e. it requires anti-aliased fonts.
         */
        val isLowBitAmbient: Boolean,

        /**
         * If the display requires burn-in protection in ambient mode, rendered pixels need to be
         * intermittently offset to avoid screen burn-in.
         */
        val doBurnInProtection: Boolean
    ) : AmbientState
}
