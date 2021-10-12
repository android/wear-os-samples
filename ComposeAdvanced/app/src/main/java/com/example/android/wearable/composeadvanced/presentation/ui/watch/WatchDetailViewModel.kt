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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.wearable.composeadvanced.data.WatchModel
import com.example.android.wearable.composeadvanced.data.WatchRepository

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

class WatchDetailViewModelFactory(
    private val watchId: Int,
    private val watchRepository: WatchRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WatchDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WatchDetailViewModel(
                watchId = watchId,
                watchRepository = watchRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
