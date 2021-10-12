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
package com.example.android.wearable.composeadvanced.presentation.ui.watch_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.wearable.composeadvanced.data.WatchModel
import com.example.android.wearable.composeadvanced.data.WatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for the Watch List Screen.
 */
class WatchListViewModel(watchRepository: WatchRepository) : ViewModel() {

    private val _watches = MutableStateFlow<List<WatchModel>>(emptyList())
    val watches: StateFlow<List<WatchModel>>
        get() = _watches

    init {
        _watches.value = watchRepository.watches
    }
}

class WatchListViewModelFactory(
    private val watchRepository: WatchRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WatchListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WatchListViewModel(
                watchRepository = watchRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
