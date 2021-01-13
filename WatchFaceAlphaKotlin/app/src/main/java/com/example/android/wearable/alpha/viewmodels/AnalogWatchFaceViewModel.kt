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

import androidx.lifecycle.asLiveData
import com.example.android.wearable.alpha.data.WatchFaceRepository
import com.example.android.wearable.alpha.data.db.AnalogWatchFaceEntity
import com.example.android.wearable.alpha.data.db.WatchFaceArmDimensionsEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * View model used for [AnalogWatchFaceService].
 */
class AnalogWatchFaceViewModel(private val repository: WatchFaceRepository) {

    // Used to launch coroutines (non-blocking way to insert data).
    private val scope: CoroutineScope = MainScope()

    // [AnalogWatchFaceEntity] operations:
    suspend fun getAnalogWatchFace(analogWatchFaceId: Int) =
        repository.getAnalogWatchFace(analogWatchFaceId)

    fun getAnalogWatchFaceAndStylesAndDimensions(analogWatchFaceId: Int) =
        repository.getAnalogWatchFaceAndStylesAndDimensions(analogWatchFaceId).asLiveData()

    // Launches a new coroutine to insert the data in a non-blocking way
    fun updateAnalogWatchFace(analogWatchFaceEntity: AnalogWatchFaceEntity) = scope.launch {
        repository.updateAnalogWatchFace(analogWatchFaceEntity)
    }

    // [WatchFaceColorStyleEntity] operations:
    suspend fun getAllWatchFaceColorStyles() =
        repository.getAllWatchFaceColorStyles()

    suspend fun getWatchFaceColorStyles(watchFaceColorStylesId: String) =
        repository.getWatchFaceColorStyles(watchFaceColorStylesId)


    // [WatchFaceArmDimensionsEntity] operations:
    suspend fun getWatchFaceArmDimensions(watchFaceArmDimensionsId: String) =
        repository.getWatchFaceArmDimensions(watchFaceArmDimensionsId)

    // Launches a new coroutine to insert the data in a non-blocking way
    fun updateWatchFaceArmDimensions(watchFaceArmDimensionsEntity: WatchFaceArmDimensionsEntity) = scope.launch {
        repository.updateWatchFaceArmDimensions(watchFaceArmDimensionsEntity)
    }

    fun clear() {
        scope.cancel("AnalogWatchFaceViewModel.clear() request")
    }
}
