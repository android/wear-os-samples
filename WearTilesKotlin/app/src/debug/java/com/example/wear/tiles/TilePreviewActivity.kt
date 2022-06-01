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

/**
 * Previews a Tile in an activity, using the TileUiClient from the wear-tiles-renderer library.
 */
class TilePreviewActivity : ComponentActivity(R.layout.activity_tile_preview) {

    private lateinit var tileUiClient: TileUiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = intent.getStringExtra(KEY_LABEL)!!

        tileUiClient = TileUiClient(
            context = this,
            component = ComponentName(this, intent.getStringExtra(KEY_COMPONENT_NAME)!!),
            parentView = findViewById(R.id.tile_container)
        )
        tileUiClient.connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        tileUiClient.close()
    }

    companion object {
        const val KEY_LABEL = "KEY_LABEL"
        const val KEY_COMPONENT_NAME = "KEY_COMPONENT_NAME"
    }
}
