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

import androidx.wear.watchface.style.UserStyle
import com.example.android.wearable.alpha.AnalogWatchFaceService
import com.example.android.wearable.alpha.data.WatchFaceUserSettingChangesDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * View model used for [AnalogWatchFaceService]. Uses flow to receive changes in the data source and
 * because the renderer is a service without LifeCycle support, handles canceling scope when no
 * longer needed.
 */
class AnalogWatchFaceViewModel(
    watchFaceUserSettingChangesDataSource: WatchFaceUserSettingChangesDataSource
) {
    // Used to launch coroutines/flow.
    // SupervisorJob
    private val scope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    // The UI collects from this StateFlow to get its state updates
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<UserChangesUiState> =
        watchFaceUserSettingChangesDataSource.getUserWatchFaceChanges()
            .map(UserChangesUiState::Success)
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = UserChangesUiState.Loading("Initializing..."))

    fun clear() {
        scope.cancel("AnalogWatchFaceViewModel scope.clear() request")
    }

    sealed class UserChangesUiState {
        data class Success(val userStyle: UserStyle) : UserChangesUiState()
        data class Loading(val message: String) : UserChangesUiState()
        data class Error(val exception: Throwable) : UserChangesUiState()
    }
}
