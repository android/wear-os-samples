/*
 * Copyright (C) 2020 The Android Open Source Project
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
package com.example.android.wearable.alpha.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Defines [AnalogWatchFaceEntity] database operations.
 */
@Dao
interface AnalogWatchFaceDao {
    @Query("SELECT * FROM analog_watch_face_table ORDER BY name ASC")
    suspend fun getAll(): List<AnalogWatchFaceEntity>

    @Query("SELECT * FROM analog_watch_face_table WHERE id=(:id) LIMIT 1")
    suspend fun get(id: Int): AnalogWatchFaceEntity

    @Transaction
    @Query("SELECT * FROM analog_watch_face_table WHERE id=(:id) LIMIT 1")
    fun getAnalogWatchFaceAndStylesAndDimensions(id: Int): Flow<AnalogWatchFaceAndStylesAndDimensions>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(analogWatchFace: AnalogWatchFaceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(analogWatchFace: List<AnalogWatchFaceEntity>)

    @Update
    suspend fun update(analogWatchFace: AnalogWatchFaceEntity)

    @Delete
    suspend fun delete(analogWatchFace: AnalogWatchFaceEntity)

    @Query("DELETE FROM analog_watch_face_table")
    suspend fun deleteAll()
}
