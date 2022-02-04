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
package com.example.android.wearable.composeadvanced.presentation.ui

import androidx.compose.foundation.ScrollState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.wear.compose.material.ScalingLazyListState
import com.example.android.wearable.composeadvanced.util.saveable

const val SCROLL_STATE_KEY = "scrollState"

// Saves scroll state through process death; inspired by https://issuetracker.google.com/195689777
class ScalingLazyListStateViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    val scrollState = savedStateHandle.saveable(
        key = SCROLL_STATE_KEY,
        saver = ScalingLazyListState.Saver
    ) {
        ScalingLazyListState()
    }
}

class ScrollStateViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    val scrollState = savedStateHandle.saveable(
        key = SCROLL_STATE_KEY,
        saver = ScrollState.Saver
    ) {
        ScrollState(0)
    }
}
