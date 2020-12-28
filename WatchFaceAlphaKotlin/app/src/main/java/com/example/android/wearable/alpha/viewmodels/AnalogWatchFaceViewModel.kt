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

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.android.wearable.alpha.data.WatchFaceRepository
import com.example.android.wearable.alpha.data.db.AnalogWatchFaceEntity
import com.example.android.wearable.alpha.data.db.WatchFaceArmDimensionsEntity
import com.example.android.wearable.alpha.data.db.WatchFaceColorStyleEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * View model used for [AnalogWatchFaceService].
 */
class AnalogWatchFaceViewModel(private val repository: WatchFaceRepository) {

    // Used to launch coroutines (non-blocking way to insert data).
    private val scope: CoroutineScope = MainScope()

    // [AnalogWatchFaceEntity] operations:
    // Using LiveData to cache what's returned has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val allAnalogWatchFaces: LiveData<List<AnalogWatchFaceEntity>> =
        repository.allAnalogWatchFaces.asLiveData()

    // Launches a new coroutine to insert the data in a non-blocking way
    fun insertAnalogWatchFace(analogWatchFaceEntity: AnalogWatchFaceEntity) = scope.launch {
        repository.insertAnalogWatchFace(analogWatchFaceEntity)
    }

    fun deleteAllAnalogWatchFaces() = scope.launch {
        repository.deleteAllAnalogWatchFaces()
    }

    // [WatchFaceColorStyleEntity] operations:
    val allWatchFaceColorStyles: LiveData<List<WatchFaceColorStyleEntity>> =
            repository.watchFaceColorStyle.asLiveData()

    fun insertWatchFaceColorStyle(watchFaceColorStyle: WatchFaceColorStyleEntity) = scope.launch {
        repository.insertWatchFaceColorStyle(watchFaceColorStyle)
    }

    fun deleteAllWatchFaceColorStyles() = scope.launch {
        repository.deleteAllWatchFaceColorStyles()
    }

    // [WatchFaceArmDimensionsEntity] operations:
    val allWatchFaceArmDimensions: LiveData<List<WatchFaceArmDimensionsEntity>> =
            repository.watchFaceArmDimensions.asLiveData()

    fun insertWatchFaceArmDimensions(watchFaceArmDimensionsEntity: WatchFaceArmDimensionsEntity) = scope.launch {
        repository.insertWatchFaceArmDimensions(watchFaceArmDimensionsEntity)
    }

    fun deleteAllWatchFaceArmDimensions() = scope.launch {
        repository.deleteAllWatchFaceArmDimensions()
    }
}
