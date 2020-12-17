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
package com.example.android.wearable.alpha.data

import androidx.annotation.WorkerThread
import com.example.android.wearable.alpha.data.db.AnalogWatchFaceDao
import com.example.android.wearable.alpha.data.db.AnalogWatchFaceEntity
import com.example.android.wearable.alpha.data.db.WatchFaceArmDimensionsDao
import com.example.android.wearable.alpha.data.db.WatchFaceArmDimensionsEntity
import com.example.android.wearable.alpha.data.db.WatchFaceColorStyleDao
import com.example.android.wearable.alpha.data.db.WatchFaceColorStyleEntity
import kotlinx.coroutines.flow.Flow

/**
 * Manages queries to backend.
 */
class WatchFaceRepository (
    private val analogWatchFaceDao: AnalogWatchFaceDao,
    private val watchFaceColorStyleDao: WatchFaceColorStyleDao,
    private val watchFaceArmDimensionsDao: WatchFaceArmDimensionsDao) {

    val allAnalogWatchFaces: Flow<List<AnalogWatchFaceEntity>> = analogWatchFaceDao.getAll()
    val watchFaceColorStyle: Flow<List<WatchFaceColorStyleEntity>> = watchFaceColorStyleDao.getAll()
    val watchFaceArmDimensions: Flow<List<WatchFaceArmDimensionsEntity>> =
            watchFaceArmDimensionsDao.getAll()

    // [AnalogWatchFaceEntity] operations:
    fun getAnalogWatchFaces(): Flow<List<AnalogWatchFaceEntity>> = analogWatchFaceDao.getAll()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertAnalogWatchFace(analogWatchFace: AnalogWatchFaceEntity) {
        analogWatchFaceDao.insert(analogWatchFace)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteAllAnalogWatchFaces() {
        analogWatchFaceDao.deleteAll()
    }

    // [WatchFaceColorStyleEntity] operations:
    fun getWatchFaceColorStyles(): Flow<List<WatchFaceColorStyleEntity>> =
            watchFaceColorStyleDao.getAll()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertWatchFaceColorStyle(colorStyle: WatchFaceColorStyleEntity) {
        watchFaceColorStyleDao.insert(colorStyle)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteAllWatchFaceColorStyles() {
        watchFaceColorStyleDao.deleteAll()
    }

    // [WatchFaceArmDimensionsEntity] operations:
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertWatchFaceArmDimensions(armDimensions: WatchFaceArmDimensionsEntity) {
        watchFaceArmDimensionsDao.insert(armDimensions)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteAllWatchFaceArmDimensions() {
        watchFaceArmDimensionsDao.deleteAll()
    }
}
