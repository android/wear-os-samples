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
package com.example.android.wearable.alpha.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Defines [WatchFaceColorStyleEntity] database operations.
 */
@Dao
interface WatchFaceColorStyleDao {

    @Query("SELECT * FROM watch_face_color_style_table ORDER BY id ASC")
    fun getAll(): Flow<List<WatchFaceColorStyleEntity>>

    @Query("SELECT * FROM watch_face_color_style_table WHERE id=(:id)")
    fun get(id: String): Flow<WatchFaceColorStyleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(colorStyle: WatchFaceColorStyleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(colorStyle: List<WatchFaceColorStyleEntity>)

    @Update
    suspend fun update(colorStyle: WatchFaceColorStyleEntity)

    @Delete
    suspend fun delete(colorStyle: WatchFaceColorStyleEntity)

    @Query("DELETE FROM watch_face_color_style_table")
    suspend fun deleteAll()
}
