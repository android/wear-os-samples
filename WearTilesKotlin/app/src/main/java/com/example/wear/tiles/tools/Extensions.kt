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
