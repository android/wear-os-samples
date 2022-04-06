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
package com.example.android.wearable.composeadvanced.presentation.ui.watch

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.android.wearable.composeadvanced.data.WatchModel
import com.example.android.wearable.composeadvanced.data.WatchRepository
import com.example.android.wearable.composeadvanced.presentation.navigation.WATCH_ID_NAV_ARGUMENT

/**
 * ViewModel for the Watch Detail Screen (only needs watch id).
 */
class WatchDetailViewModel(
    watchId: Int,
    watchRepository: WatchRepository
) : ViewModel() {
    private val _watch: MutableState<WatchModel?> =
        mutableStateOf(watchRepository.getWatch(watchId))
    val watch: State<WatchModel?>
        get() = _watch
}
