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

import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.wear.tiles.manager.TileUiClient
import com.example.wear.tiles.databinding.ActivityMainBinding
import com.example.wear.tiles.fitness.FitnessTileService
import com.example.wear.tiles.media.PlayNextSongTileService
import com.example.wear.tiles.messaging.MessagingTileService

/**
 * Renders a tile. It uses the wear-tiles-renderer library to render the tile within
 * the activity. You can change the tile class name to render another tile instead.
 */
class MainActivity : ComponentActivity() {
    private val sampleTiles = listOf(
        FitnessTileService::class.java,
        MessagingTileService::class.java,
        PlayNextSongTileService::class.java
    )

    // Change into 1 or 2 to render another sample tile.
    private val tileToShow = sampleTiles[0]

    private lateinit var binding: ActivityMainBinding
    private lateinit var tileUiClient: TileUiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tileUiClient = TileUiClient(
            context = this,
            component = ComponentName(this, tileToShow),
            parentView = binding.root
        )
        tileUiClient.connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        tileUiClient.close()
    }
}
