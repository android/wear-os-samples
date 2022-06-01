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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.tiles.TileService
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
            MaterialTheme {
                SampleTilesList(this, sampleTiles)
            }
        }
    }
}

@Composable
private fun SampleTilesList(context: Context, sampleTiles: Map<Int, Class<out TileService>>) {
    val listState = rememberScalingLazyListState()
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState
    ) {
        item {
            ListHeader {
                Text(text = context.getString(R.string.app_name))
            }
        }
        sampleTiles.forEach { (tileLabelRes, tileServiceClass) ->
            item(tileLabelRes) {
                TileSample(context, tileLabelRes, tileServiceClass)
            }
        }
    }
}

@Composable
private fun TileSample(
    context: Context,
    @StringRes tileLabelRes: Int,
    tileServiceClass: Class<out TileService>
) {
    val tileLabel = context.getString(tileLabelRes)
    Chip(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            val intent = Intent(context, TilePreviewActivity::class.java)
            intent.putExtra(TilePreviewActivity.KEY_LABEL, tileLabel)
            intent.putExtra(TilePreviewActivity.KEY_COMPONENT_NAME, tileServiceClass.name)
            context.startActivity(intent)
        },
        label = { Text(tileLabel) }
    )
}
