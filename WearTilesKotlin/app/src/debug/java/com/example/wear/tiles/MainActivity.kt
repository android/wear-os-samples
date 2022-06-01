/*
 * Copyright 2021 The Android Open Source Project
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
package com.example.wear.tiles

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.wear.tiles.fitness.FitnessTileService
import com.example.wear.tiles.media.PlayNextSongTileService
import com.example.wear.tiles.messaging.MessagingTileService

/**
 * Lists the Tile samples.
 */
class MainActivity : ComponentActivity() {

    private val sampleTiles = mapOf(
        R.string.tile_fitness_label to FitnessTileService::class.java,
        R.string.tile_messaging_label to MessagingTileService::class.java,
        R.string.tile_media_label to PlayNextSongTileService::class.java
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SampleTilesList(this, sampleTiles)
        }
    }
}
