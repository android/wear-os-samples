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

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val DATABASE_NAME = "analog_watch_face_database"

/**
 * Stores all watch face data.
 */
@Database(
    entities = [
        AnalogWatchFaceEntity::class,
        WatchFaceColorStyleEntity::class,
        WatchFaceArmDimensionsEntity::class],
    version = 1,
    exportSchema = false)
abstract class WatchFaceDatabase : RoomDatabase() {

    abstract fun analogWatchFaceDao(): AnalogWatchFaceDao
    abstract fun watchFaceColorStyleDao(): WatchFaceColorStyleDao
    abstract fun watchFaceArmDimensionsDao(): WatchFaceArmDimensionsDao

    /**
     * Populates the database on first use.
     */
    private class WatchFaceDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateAnalogDatabase(
                            analogWatchFaceDao = database.analogWatchFaceDao(),
                            watchFaceColorStyleDao = database.watchFaceColorStyleDao(),
                            watchFaceArmDimensionsDao = database.watchFaceArmDimensionsDao()
                    )
                }
            }
        }
    }

    companion object {
        // For Singleton instantiation
        @Volatile
        private var INSTANCE: WatchFaceDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): WatchFaceDatabase {

            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WatchFaceDatabase::class.java,
                    DATABASE_NAME
                )
                        // Wipes and rebuilds instead of migrating if no Migration object.
                        // Migration is not part of this sample.
                        .fallbackToDestructiveMigration()
                        .addCallback(WatchFaceDatabaseCallback(scope))
                        .build()

                INSTANCE = instance
                return instance
            }
        }
    }
}
