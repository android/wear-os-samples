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
package com.example.android.wearable.alpha

import android.app.Application
import com.example.android.wearable.alpha.data.WatchFaceRepository
import com.example.android.wearable.alpha.data.db.WatchFaceDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * Sets up repository for all app watch faces (services).
 */
class MainApplication: Application() {

    // Required for pre-population of empty database.
    // No need to cancel this scope as it'll be torn down with the process.
    private val applicationScope = CoroutineScope(SupervisorJob())

    // Both database and repository use lazy so they aren't created when the app starts, but only
    // when repository is first needed.
    private val database by lazy {
        WatchFaceDatabase.getDatabase(this, applicationScope)
    }

    val repository by lazy {
        WatchFaceRepository(
            analogWatchFaceDao = database.analogWatchFaceDao(),
            watchFaceColorStyleDao = database.watchFaceColorStyleDao(),
            watchFaceArmDimensionsDao = database.watchFaceArmDimensionsDao()
        )
    }
}
