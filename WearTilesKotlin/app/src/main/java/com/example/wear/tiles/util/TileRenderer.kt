package com.example.wear.tiles.util

import android.content.Context
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.ResourceBuilders
import androidx.wear.tiles.TileBuilders

abstract class TileRenderer<T>(val context: Context) {
    abstract fun tileRequest(
        tileState: T,
        requestParams: RequestBuilders.TileRequest,
    ): TileBuilders.Tile

    abstract fun resourcesRequest(
        tileState: T,
        requestParams: RequestBuilders.ResourcesRequest,
    ): ResourceBuilders.Resources
}
