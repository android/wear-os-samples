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
package com.example.wear.tiles.messaging

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import androidx.core.content.ContextCompat
import androidx.wear.tiles.TileProviderService
import androidx.wear.tiles.ColorBuilders.argb
import androidx.wear.tiles.DeviceParametersBuilders
import androidx.wear.tiles.DeviceParametersBuilders.DeviceParameters
import androidx.wear.tiles.DimensionBuilders.dp
import androidx.wear.tiles.LayoutElementBuilders.Box
import androidx.wear.tiles.LayoutElementBuilders.Column
import androidx.wear.tiles.LayoutElementBuilders.FontStyles
import androidx.wear.tiles.LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER
import androidx.wear.tiles.LayoutElementBuilders.Image
import androidx.wear.tiles.LayoutElementBuilders.Layout
import androidx.wear.tiles.LayoutElementBuilders.LayoutElement
import androidx.wear.tiles.LayoutElementBuilders.Row
import androidx.wear.tiles.LayoutElementBuilders.Spacer
import androidx.wear.tiles.LayoutElementBuilders.TEXT_ALIGN_CENTER
import androidx.wear.tiles.LayoutElementBuilders.Text
import androidx.wear.tiles.ModifiersBuilders.Background
import androidx.wear.tiles.ModifiersBuilders.Corner
import androidx.wear.tiles.ModifiersBuilders.Modifiers
import androidx.wear.tiles.ResourceBuilders
import androidx.wear.tiles.ResourceBuilders.AndroidImageResourceByResId
import androidx.wear.tiles.ResourceBuilders.ImageResource
import androidx.wear.tiles.ResourceBuilders.InlineImageResource
import androidx.wear.tiles.ResourceBuilders.Resources
import androidx.wear.tiles.TileBuilders.Tile
import androidx.wear.tiles.TimelineBuilders.Timeline
import androidx.wear.tiles.TimelineBuilders.TimelineEntry
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.RequestBuilders.*
import com.example.wear.tiles.R
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.guava.future
import java.nio.ByteBuffer
import kotlin.math.roundToInt

// Updating this version triggers a new call to onResourcesRequest(). This is useful for dynamic
// resources, the contents of which change even though their id stays the same (e.g. a graph).
// In this sample, our resources are all fixed, so we use a constant value.
private const val RESOURCES_VERSION = "1"

// Dimensions
private const val CIRCLE_SIZE = 48f
private val SPACING_TITLE_SUBTITLE = dp(4f)
private val SPACING_SUBTITLE_CONTACTS = dp(12f)
private val SPACING_CONTACTS = dp(8f)
private val ICON_SIZE = dp(24f)

// Resource identifiers for images
private const val ID_IC_SEARCH = "ic_search"
private const val ID_CONTACT_PREFIX = "contact_"

/**
 * Creates a Messaging Tile, showing your favorite contacts and a button to search other contacts.
 * This is a demo tile only, so the buttons don't actually work.
 *
 * The main function, [onTileRequest], is triggered when the system calls for a tile and implements
 * ListenableFuture which allows the Tile to be returned asynchronously.
 *
 * Resources are provided with the [onResourcesRequest] method, which is triggered when the tile
 * uses an Image.
 */
class MessagingTileService : TileProviderService() {
    // For coroutines, use a custom scope we can cancel when the service is destroyed
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onTileRequest(
        requestParams: TileRequest
    ): ListenableFuture<Tile> = serviceScope.future {
        val contacts = MessagingRepo.getFavoriteContacts().take(4)
        Tile.builder()
            .setResourcesVersion(RESOURCES_VERSION)
            // Creates a timeline to hold one or more tile entries for a specific time periods.
            .setTimeline(
                Timeline.builder().addTimelineEntry(
                    TimelineEntry.builder().setLayout(
                        Layout.builder().setRoot(layout(contacts, requestParams.deviceParameters!!))
                    )
                )
            ).build()
    }

    override fun onResourcesRequest(
        requestParams: ResourcesRequest
    ): ListenableFuture<Resources> = serviceScope.future {
        val density = requestParams.deviceParameters!!.screenDensity
        val circleSizePx = (CIRCLE_SIZE * density).roundToInt()
        val contacts = MessagingRepo.getFavoriteContacts()
        Resources.builder()
            .setVersion(RESOURCES_VERSION)
            .apply {
                // Add the scaled & cropped avatar images
                contacts
                    .mapNotNull { contact ->
                        // Only create a resource for contacts with an associated avatar
                        contact.avatarRes?.let {
                            val drawable = ContextCompat.getDrawable(
                                this@MessagingTileService,
                                contact.avatarRes
                            )
                            // Create a small cropped avatar
                            val bitmap = (drawable as BitmapDrawable).bitmap.croppedCircle(
                                circleSizePx
                            )
                            val bitmapData = ByteBuffer.allocate(bitmap.byteCount).apply {
                                bitmap.copyPixelsToBuffer(this)
                            }.array()
                            // Link the contact's identifier to an image resource
                            contact.id to ImageResource.builder()
                                .setInlineResource(
                                    InlineImageResource.builder()
                                        .setData(bitmapData)
                                        .setWidthPx(circleSizePx)
                                        .setHeightPx(circleSizePx)
                                        .setFormat(ResourceBuilders.IMAGE_FORMAT_RGB_565)
                                )
                        }
                    }.forEach { (id, imageResource) ->
                        // Add each created image resource to the list
                        addIdToImageMapping("$ID_CONTACT_PREFIX$id", imageResource)
                    }
            }
            .addIdToImageMapping(
                ID_IC_SEARCH,
                ImageResource.builder().setAndroidResourceByResId(
                    AndroidImageResourceByResId.builder()
                        .setResourceId(R.drawable.ic_search)
                )
            )
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cleans up the coroutine
        serviceJob.cancel()
    }

    private fun layout(
        contacts: List<Contact>,
        deviceParameters: DeviceParameters
    ): LayoutElement = Column.builder()
        .addContent(
            Text.builder()
                .setText(resources.getString(R.string.messaging_tile_title))
                .setFontStyle(
                    FontStyles.title3(deviceParameters).setColor(
                        argb(ContextCompat.getColor(baseContext, R.color.primary))
                    )
                )
        )
        .addContent(Spacer.builder().setHeight(SPACING_TITLE_SUBTITLE))
        .addContent(
            Text.builder()
                .setText(resources.getString(R.string.messaging_tile_subtitle))
                .setFontStyle(
                    FontStyles.caption1(deviceParameters).setColor(
                        argb(ContextCompat.getColor(baseContext, R.color.onSecondary))
                    )
                )
        )
        .addContent(Spacer.builder().setHeight(SPACING_SUBTITLE_CONTACTS))
        .addContent(
            Row.builder()
                .addContent(contactLayout(contacts[0], deviceParameters))
                .addContent(Spacer.builder().setWidth(SPACING_CONTACTS))
                .addContent(contactLayout(contacts[1], deviceParameters))
                .addContent(Spacer.builder().setWidth(SPACING_CONTACTS))
                .addContent(contactLayout(contacts[2], deviceParameters))
        )
        .addContent(
            Row.builder()
                .addContent(contactLayout(contacts[3], deviceParameters))
                .addContent(Spacer.builder().setWidth(SPACING_CONTACTS))
                .addContent(searchLayout())
        )
        .build()

    private fun contactLayout(contact: Contact, deviceParameters: DeviceParameters) = Box.builder().apply {
        if (contact.avatarRes == null) {
            // Create an avatar based on the contact's initials
            setWidth(dp(CIRCLE_SIZE))
            setHeight(dp(CIRCLE_SIZE))
            setHorizontalAlignment(HORIZONTAL_ALIGN_CENTER)
            setModifiers(
                Modifiers.builder()
                    .setBackground(
                        Background.builder()
                            .setColor(
                                argb(ContextCompat.getColor(baseContext, R.color.secondary))
                            )
                            .setCorner(
                                Corner.builder()
                                    .setRadius(dp(CIRCLE_SIZE / 2))
                            )
                    )
            )
            addContent(
                Text.builder()
                    .setText(contact.initials)
                    .setFontStyle(
                        FontStyles.button(deviceParameters).setColor(
                            argb(ContextCompat.getColor(baseContext, R.color.primary))
                        )
                    )
            )
        } else {
            // Create an avatar based on the contact's avatar
            addContent(
                Image.builder()
                    .setResourceId("$ID_CONTACT_PREFIX${contact.id}")
                    .setWidth(dp(CIRCLE_SIZE))
                    .setHeight(dp(CIRCLE_SIZE))
            )
        }
    }

    private fun searchLayout() = Box.builder()
        .setWidth(dp(CIRCLE_SIZE))
        .setHeight(dp(CIRCLE_SIZE))
        .setModifiers(
            Modifiers.builder()
                .setBackground(
                    Background.builder()
                        .setColor(
                            argb(ContextCompat.getColor(baseContext, R.color.primaryDark))
                        )
                        .setCorner(
                            Corner.builder().setRadius(dp(CIRCLE_SIZE / 2))
                        )
                )
        )
        .addContent(
            Image.builder()
                .setWidth(ICON_SIZE)
                .setHeight(ICON_SIZE)
                .setResourceId(ID_IC_SEARCH)
        )
}

// Create a scaled and cropped circular image
private fun Bitmap.croppedCircle(circleSizePx: Int): Bitmap {
    val scaled = Bitmap.createScaledBitmap(this, circleSizePx, circleSizePx, false)
    val output = Bitmap.createBitmap(circleSizePx, circleSizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    // Use a random color for the paint that will be used as the mask to crop against
    val color = -0xbdbdbe
    val paint = Paint()
    val rect = Rect(0, 0, circleSizePx, circleSizePx)
    paint.isAntiAlias = true
    canvas.drawARGB(0, 0, 0, 0)
    paint.color = color
    canvas.drawCircle(circleSizePx / 2f, circleSizePx / 2f, circleSizePx / 2f, paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(scaled, rect, rect, paint)
    return output.copy(Bitmap.Config.RGB_565, false)
}
