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

/**
 * Manages queries to backend.
 */
class WatchFaceRepository(
    private val analogWatchFaceDao: AnalogWatchFaceDao,
    private val watchFaceColorStyleDao: WatchFaceColorStyleDao,
    private val watchFaceArmDimensionsDao: WatchFaceArmDimensionsDao
) {

    // [AnalogWatchFaceEntity] properties/operations:
    suspend fun getAnalogWatchFace(analogWatchFaceId: Int) =
        analogWatchFaceDao.get(analogWatchFaceId)

    fun getAnalogWatchFaceAndStylesAndDimensions(analogWatchFaceId: Int) =
        analogWatchFaceDao.getWithStylesAndDimensions(analogWatchFaceId)

    suspend fun updateAnalogWatchFace(analogWatchFace: AnalogWatchFaceEntity) =
        analogWatchFaceDao.update(analogWatchFace)

    // [WatchFaceColorStyleEntity] properties/operations:
    suspend fun getAllWatchFaceColorStyles(): List<WatchFaceColorStyleEntity> =
        watchFaceColorStyleDao.getAll()

    suspend fun getWatchFaceColorStyles(watchFaceColorStylesId: String): WatchFaceColorStyleEntity =
        watchFaceColorStyleDao.get(watchFaceColorStylesId)

    // [WatchFaceArmDimensionsEntity] properties/operations:
    suspend fun getWatchFaceArmDimensions(watchFaceArmDimensionsId: String) =
        watchFaceArmDimensionsDao.get(watchFaceArmDimensionsId)

    suspend fun updateWatchFaceArmDimensions(armDimensions: WatchFaceArmDimensionsEntity) =
        watchFaceArmDimensionsDao.update(armDimensions)

}
