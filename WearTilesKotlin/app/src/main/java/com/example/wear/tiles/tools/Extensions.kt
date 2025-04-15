/*
 * Copyright 2025 The Android Open Source Project
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
package com.example.wear.tiles.tools

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.Column
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.ResourceBuilders.ImageResource
import androidx.wear.protolayout.ResourceBuilders.Resources
import java.nio.ByteBuffer

// Resources extensions

fun Resources.Builder.addIdToImageMapping(id: String, @DrawableRes resId: Int): Resources.Builder =
    addIdToImageMapping(id, resId.toImageResource())

fun Resources.Builder.addIdToImageMapping(id: String, bitmap: Bitmap): Resources.Builder =
    addIdToImageMapping(id, bitmap.toImageResource())

// DeviceParameters extensions

fun DeviceParameters.isLargeScreen() = screenWidthDp >= 225

// Column extensions

fun column(builder: Column.Builder.() -> Unit) = Column.Builder().apply(builder).build()

// LayoutElementBuilders extensions

fun image(builder: LayoutElementBuilders.Image.Builder.() -> Unit) =
    LayoutElementBuilders.Image.Builder().apply(builder).build()

// Image extensions

fun @receiver:DrawableRes Int.toImageResource(): ImageResource {
    return ImageResource.Builder()
        .setAndroidResourceByResId(
            ResourceBuilders.AndroidImageResourceByResId.Builder().setResourceId(this).build()
        )
        .build()
}

fun Bitmap.toImageResource(): ImageResource {
    val safeBitmap = this.copy(Bitmap.Config.RGB_565, false)

    val byteBuffer = ByteBuffer.allocate(safeBitmap.byteCount)
    safeBitmap.copyPixelsToBuffer(byteBuffer)
    val bytes: ByteArray = byteBuffer.array()

    return ImageResource.Builder()
        .setInlineResource(
            ResourceBuilders.InlineImageResource.Builder()
                .setData(bytes)
                .setWidthPx(this.width)
                .setHeightPx(this.height)
                .setFormat(ResourceBuilders.IMAGE_FORMAT_RGB_565)
                .build()
        )
        .build()
}
