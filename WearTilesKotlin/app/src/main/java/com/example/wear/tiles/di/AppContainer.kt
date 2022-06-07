/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.wear.tiles.di

import android.content.Context
import coil.ImageLoader
import com.example.wear.tiles.messaging.MessagingRepo
import com.example.wear.tiles.messaging.MessagingTileRenderer
import com.example.wear.tiles.messaging.Updates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.io.Closeable

// https://developer.android.com/training/dependency-injection/manual
class AppContainer(application: Context): Closeable {
    val imageLoader: ImageLoader = ImageLoader.Builder(application)
        .respectCacheHeaders(false)
        .build()
    val repo: MessagingRepo = MessagingRepo(application)
    val renderer: MessagingTileRenderer = MessagingTileRenderer(application)
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val updates: Updates = Updates(application = application, scope = scope, repo = repo)

    override fun close() {
        scope.cancel()
    }
}
