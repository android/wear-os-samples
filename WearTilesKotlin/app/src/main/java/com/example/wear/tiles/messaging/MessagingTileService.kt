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

import androidx.core.content.ContextCompat
import androidx.wear.tiles.ActionBuilders
import androidx.wear.tiles.ColorBuilders.argb
import androidx.wear.tiles.DeviceParametersBuilders.DeviceParameters
import androidx.wear.tiles.DimensionBuilders.dp
import androidx.wear.tiles.LayoutElementBuilders.Column
import androidx.wear.tiles.LayoutElementBuilders.FontStyles
import androidx.wear.tiles.LayoutElementBuilders.Layout
import androidx.wear.tiles.LayoutElementBuilders.LayoutElement
import androidx.wear.tiles.LayoutElementBuilders.Row
import androidx.wear.tiles.LayoutElementBuilders.Spacer
import androidx.wear.tiles.LayoutElementBuilders.Text
import androidx.wear.tiles.ModifiersBuilders.Clickable
import androidx.wear.tiles.ModifiersBuilders.Modifiers
import androidx.wear.tiles.ModifiersBuilders.Semantics
import androidx.wear.tiles.RequestBuilders.ResourcesRequest
import androidx.wear.tiles.RequestBuilders.TileRequest
import androidx.wear.tiles.ResourceBuilders.AndroidImageResourceByResId
import androidx.wear.tiles.ResourceBuilders.ImageResource
import androidx.wear.tiles.ResourceBuilders.Resources
import androidx.wear.tiles.TileBuilders.Tile
import androidx.wear.tiles.TimelineBuilders.Timeline
import androidx.wear.tiles.TimelineBuilders.TimelineEntry
import androidx.wear.tiles.material.Button
import androidx.wear.tiles.material.ButtonColors
import com.example.wear.tiles.CoroutinesTileService
import com.example.wear.tiles.R

// Updating this version triggers a new call to onResourcesRequest(). This is useful for dynamic
// resources, the contents of which change even though their id stays the same (e.g. a graph).
// In this sample, our resources are all fixed, so we use a constant value.
private const val RESOURCES_VERSION = "1"

// Dimensions
private val SPACING_TITLE_SUBTITLE = dp(4f)
private val SPACING_SUBTITLE_CONTACTS = dp(12f)
private val SPACING_CONTACTS_HORIZONTAL = dp(8f)
private val SPACING_CONTACTS_VERTICAL = dp(4f)

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
class MessagingTileService : CoroutinesTileService() {
    override suspend fun tileRequest(requestParams: TileRequest): Tile {
        val contacts = MessagingRepo.getFavoriteContacts().take(4)
        return Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            // Creates a timeline to hold one or more tile entries for a specific time periods.
            .setTimeline(
                Timeline.Builder()
                    .addTimelineEntry(
                        TimelineEntry.Builder()
                            .setLayout(
                                Layout.Builder()
                                    .setRoot(layout(contacts, requestParams.deviceParameters!!))
                                    .build()
                            )
                            .build()
                    )
                    .build()
            ).build()
    }

    override suspend fun resourcesRequest(requestParams: ResourcesRequest): Resources {
        val contacts = MessagingRepo.getFavoriteContacts()
        return Resources.Builder()
            .setVersion(RESOURCES_VERSION)
            .apply {
                // Add the scaled & cropped avatar images
                contacts
                    .mapNotNull { contact ->
                        // Only create a resource for contacts with an associated avatar
                        contact.avatarRes?.let {
                            contact.id to
                                ImageResource.Builder().setAndroidResourceByResId(
                                    AndroidImageResourceByResId.Builder()
                                        .setResourceId(contact.avatarRes).build()
                                ).build()
                        }
                    }.forEach { (id, imageResource) ->
                        // Add each created image resource to the list
                        addIdToImageMapping("$ID_CONTACT_PREFIX$id", imageResource)
                    }
            }
            .addIdToImageMapping(
                ID_IC_SEARCH,
                ImageResource.Builder()
                    .setAndroidResourceByResId(
                        AndroidImageResourceByResId.Builder()
                            .setResourceId(R.drawable.ic_search)
                            .build()
                    )
                    .build()
            )
            .build()
    }

    private fun layout(
        contacts: List<Contact>,
        deviceParameters: DeviceParameters
    ): LayoutElement = Column.Builder()
        .addContent(
            Text.Builder()
                .setText(resources.getString(R.string.tile_messaging_title))
                .setFontStyle(
                    FontStyles
                        .title3(deviceParameters)
                        .setColor(
                            argb(ContextCompat.getColor(baseContext, R.color.primary))
                        )
                        .build()
                )
                .build()
        )
        .addContent(Spacer.Builder().setHeight(SPACING_TITLE_SUBTITLE).build())
        .addContent(
            Text.Builder()
                .setText(resources.getString(R.string.tile_messaging_subtitle))
                .setFontStyle(
                    FontStyles
                        .caption1(deviceParameters)
                        .setColor(
                            argb(ContextCompat.getColor(baseContext, R.color.onSecondary))
                        )
                        .build()
                )
                .build()
        )
        .addContent(Spacer.Builder().setHeight(SPACING_SUBTITLE_CONTACTS).build())
        .addContent(
            Row.Builder()
                .addContent(
                    contactLayout(
                        contact = contacts[0],
                        deviceParameters = deviceParameters,
                        clickable = Clickable.Builder()
                            .setOnClick(ActionBuilders.LoadAction.Builder().build())
                            .build()
                    )
                )
                .addContent(Spacer.Builder().setWidth(SPACING_CONTACTS_HORIZONTAL).build())
                .addContent(
                    contactLayout(
                        contact = contacts[1],
                        deviceParameters = deviceParameters,
                        clickable = Clickable.Builder()
                            .setOnClick(ActionBuilders.LoadAction.Builder().build())
                            .build()
                    )
                )
                .addContent(Spacer.Builder().setWidth(SPACING_CONTACTS_HORIZONTAL).build())
                .addContent(
                    contactLayout(
                        contact = contacts[2],
                        deviceParameters = deviceParameters,
                        clickable = Clickable.Builder()
                            .setOnClick(ActionBuilders.LoadAction.Builder().build())
                            .build()
                    )
                )
                .build()
        )
        .addContent(Spacer.Builder().setHeight(SPACING_CONTACTS_VERTICAL).build())
        .addContent(
            Row.Builder()
                .addContent(
                    contactLayout(
                        contact = contacts[3],
                        deviceParameters = deviceParameters,
                        clickable = Clickable.Builder()
                            .setOnClick(ActionBuilders.LoadAction.Builder().build())
                            .build()
                    )
                )
                .addContent(Spacer.Builder().setWidth(SPACING_CONTACTS_HORIZONTAL).build())
                .addContent(searchLayout())
                .build()
        )
        .setModifiers(
            Modifiers.Builder()
                .setSemantics(
                    Semantics.Builder()
                        .setContentDescription(getString(R.string.tile_messaging_label))
                        .build()
                )
                .build()
        )
        .build()

    private fun contactLayout(
        contact: Contact,
        deviceParameters: DeviceParameters,
        clickable: Clickable
    ) = Button.Builder(clickable.onClick!!, clickable.id).apply {
        setContentDescription(contact.name)
        setButtonColors(
            ButtonColors(
                ContextCompat.getColor(baseContext, R.color.secondary),
                ContextCompat.getColor(baseContext, R.color.primary)
            )
        )
        if (contact.avatarRes == null) {
            setTextContent(contact.initials)
        } else {
            setImageContent("$ID_CONTACT_PREFIX${contact.id}")
        }
    }
        .build()

    private fun searchLayout() = Button.Builder(ActionBuilders.LoadAction.Builder().build(), "")
        .setButtonColors(
            ButtonColors(
                ContextCompat.getColor(baseContext, R.color.primaryDark),
                ContextCompat.getColor(baseContext, R.color.primary)
            )
        )
        .setContentDescription(getString(R.string.tile_messaging_search))
        .setIconContent(ID_IC_SEARCH)
        .build()
}
