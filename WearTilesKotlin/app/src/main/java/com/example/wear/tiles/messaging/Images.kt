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
package com.example.wear.tiles.messaging

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.wear.tiles.ResourceBuilders
import androidx.wear.tiles.ResourceBuilders.ImageResource
import coil.ImageLoader
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import java.nio.ByteBuffer

suspend fun ImageLoader.loadAvatar(context: Context, contact: Contact, size: Int? = 64): Bitmap? {
    val request = ImageRequest.Builder(context)
        .data(contact.avatarUrl)
        .apply {
            if (size != null) {
                size(size)
            }
        }
        .allowRgb565(true)
        .transformations(CircleCropTransformation())
        .allowHardware(false)
        .build()
    val response = execute(request)
    return (response.drawable as? BitmapDrawable)?.bitmap
}

fun bitmapToImageResource(bitmap: Bitmap): ImageResource {
    // TODO check if needed
    val safeBitmap = bitmap.toRgb565()

    val byteBuffer = ByteBuffer.allocate(safeBitmap.byteCount)
    safeBitmap.copyPixelsToBuffer(byteBuffer)
    val bytes: ByteArray = byteBuffer.array()

    return ImageResource.Builder().setInlineResource(
        ResourceBuilders.InlineImageResource.Builder()
            .setData(bytes)
            .setWidthPx(bitmap.width)
            .setHeightPx(bitmap.height)
            .setFormat(ResourceBuilders.IMAGE_FORMAT_RGB_565)
            .build()
    )
        .build()
}

private fun Bitmap.toRgb565(): Bitmap {
    // TODO avoid copy
    return this.copy(Bitmap.Config.RGB_565, false)
}
