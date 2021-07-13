/*
 * Copyright (C) 2020 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.wearable.alpha.viewmodels

import com.example.android.wearable.alpha.AnalogWatchFaceService
import com.example.android.wearable.alpha.data.WatchFaceUserSettingChangesDataSource
import com.example.android.wearable.alpha.data.WatchFaceUserSettingChangesDataSource.WatchFaceUserChanges
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * View model used for [AnalogWatchFaceService]. Uses flow to receive changes in the data source and
 * because the renderer is a service without LifeCycle support, handles canceling scope when no
 * longer needed.
 */
@ExperimentalCoroutinesApi
class AnalogWatchFaceViewModel(
    watchFaceUserSettingChangesDataSource: WatchFaceUserSettingChangesDataSource
) {
    // Used to launch coroutines/flow.
    private val scope: CoroutineScope = MainScope()

    private val _uiState = MutableStateFlow(UserChangesUiState.Success(WatchFaceUserChanges()))
    // The UI collects from this StateFlow to get its state updates
    val uiState: StateFlow<UserChangesUiState> = _uiState.asStateFlow()

    init {
        scope.launch {
            watchFaceUserSettingChangesDataSource.getUserWatchFaceChanges().collect {
                    watchFaceUserChanges: WatchFaceUserChanges ->
                _uiState.value = UserChangesUiState.Success(watchFaceUserChanges)
            }
        }
    }

    fun clear() {
        scope.cancel("AnalogWatchFaceViewModel scope.clear() request")
    }

    sealed class UserChangesUiState {
        data class Success(
            val userChangesUserSettingChanges: WatchFaceUserChanges
        ) : UserChangesUiState()
        data class Error(val exception: Throwable) : UserChangesUiState()
    }
}
