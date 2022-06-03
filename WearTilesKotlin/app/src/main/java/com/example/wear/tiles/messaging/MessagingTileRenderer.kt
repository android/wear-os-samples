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
import androidx.core.content.ContextCompat
import androidx.wear.tiles.ActionBuilders.LoadAction
import androidx.wear.tiles.ColorBuilders
import androidx.wear.tiles.DeviceParametersBuilders
import androidx.wear.tiles.DimensionBuilders
import androidx.wear.tiles.LayoutElementBuilders
import androidx.wear.tiles.LayoutElementBuilders.Column
import androidx.wear.tiles.LayoutElementBuilders.FontStyles
import androidx.wear.tiles.LayoutElementBuilders.Row
import androidx.wear.tiles.LayoutElementBuilders.Spacer
import androidx.wear.tiles.LayoutElementBuilders.Text
import androidx.wear.tiles.ModifiersBuilders.Clickable
import androidx.wear.tiles.ModifiersBuilders.Modifiers
import androidx.wear.tiles.ModifiersBuilders.Semantics
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.ResourceBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TimelineBuilders
import androidx.wear.tiles.material.Button
import androidx.wear.tiles.material.ButtonColors
import com.example.wear.tiles.R
import com.example.wear.tiles.util.TileRenderer

class MessagingTileRenderer(context: Context) :
    TileRenderer<MessagingTileState>(context) {
    override fun tileRequest(
        tileState: MessagingTileState,
        requestParams: RequestBuilders.TileRequest,
    ): TileBuilders.Tile {
        return TileBuilders.Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            // Creates a timeline to hold one or more tile entries for a specific time periods.
            .setTimeline(
                TimelineBuilders.Timeline.Builder()
                    .addTimelineEntry(
                        TimelineBuilders.TimelineEntry.Builder()
                            .setLayout(
                                LayoutElementBuilders.Layout.Builder()
                                    .setRoot(layout(tileState, requestParams.deviceParameters!!))
                                    .build()
                            )
                            .build()
                    )
                    .build()
            ).build()
    }

    override fun resourcesRequest(
        tileState: MessagingTileState,
        requestParams: RequestBuilders.ResourcesRequest,
    ): ResourceBuilders.Resources {
        return ResourceBuilders.Resources.Builder()
            .setVersion(RESOURCES_VERSION)
            .apply {
                // Add the scaled & cropped avatar images
                tileState.avatars.map { (id, bitmap) ->
                    id to bitmapToImageResource(bitmap)
                }.forEach { (id, imageResource) ->
                    // Add each created image resource to the list
                    addIdToImageMapping("$ID_CONTACT_PREFIX$id", imageResource)
                }
            }
            .addIdToImageMapping(
                ID_IC_SEARCH,
                ResourceBuilders.ImageResource.Builder()
                    .setAndroidResourceByResId(
                        ResourceBuilders.AndroidImageResourceByResId.Builder()
                            .setResourceId(R.drawable.ic_search)
                            .build()
                    )
                    .build()
            )
            .build()
    }

    private fun layout(
        state: MessagingTileState,
        deviceParameters: DeviceParametersBuilders.DeviceParameters,
    ): LayoutElementBuilders.LayoutElement {
        return Column.Builder()
            .addContent(
                Text.Builder()
                    .setText(context.resources.getString(R.string.tile_messaging_title))
                    .setFontStyle(
                        FontStyles
                            .title3(deviceParameters)
                            .setColor(
                                ColorBuilders.argb(ContextCompat.getColor(context, R.color.primary))
                            )
                            .build()
                    )
                    .build()
            )
            .addContent(
                Spacer.Builder().setHeight(SPACING_TITLE_SUBTITLE)
                    .build()
            )
            .addContent(
                Text.Builder()
                    .setText(context.getString(R.string.tile_messaging_subtitle))
                    .setFontStyle(
                        FontStyles
                            .caption1(deviceParameters)
                            .setColor(
                                ColorBuilders.argb(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.onSecondary
                                    )
                                )
                            )
                            .build()
                    )
                    .build()
            )
            .addContent(
                Spacer.Builder().setHeight(SPACING_SUBTITLE_CONTACTS)
                    .build()
            )
            .addContent(
                Row.Builder()
                    .addContact(state, 0)
                    .addContent(
                        Spacer.Builder()
                            .setWidth(SPACING_CONTACTS_HORIZONTAL).build()
                    )
                    .addContact(state, 1)
                    .addContent(
                        Spacer.Builder()
                            .setWidth(SPACING_CONTACTS_HORIZONTAL).build()
                    )
                    .addContact(state, 2)
                    .build()
            )
            .addContent(
                Spacer.Builder().setHeight(SPACING_CONTACTS_VERTICAL)
                    .build()
            )
            .addContent(
                Row.Builder()
                    .addContact(state, 3)
                    .addContent(
                        Spacer.Builder()
                            .setWidth(SPACING_CONTACTS_HORIZONTAL).build()
                    )
                    .addContent(searchLayout())
                    .build()
            )
            .setModifiers(
                Modifiers.Builder()
                    .setSemantics(
                        Semantics.Builder()
                            .setContentDescription(context.getString(R.string.tile_messaging_label))
                            .build()
                    )
                    .build()
            )
            .build()
    }

    private fun contactLayout(
        contact: Contact,
        avatar: Bitmap?,
        clickable: Clickable,
    ) = Button.Builder(context, clickable).apply {
        setContentDescription(contact.name)
        setButtonColors(
            ButtonColors(
                ContextCompat.getColor(context, R.color.secondary),
                ContextCompat.getColor(context, R.color.primary)
            )
        )
        if (avatar == null) {
            setTextContent(contact.initials)
        } else {
            setImageContent("$ID_CONTACT_PREFIX${contact.id}")
        }
    }
        .build()

    private fun searchLayout() = Button.Builder(
        context,
        Clickable.Builder()
            .setOnClick(LoadAction.Builder().build())
            .setId("")
            .build()
    )
        .setButtonColors(
            ButtonColors(
                ContextCompat.getColor(context, R.color.primaryDark),
                ContextCompat.getColor(context, R.color.primary)
            )
        )
        .setContentDescription(context.getString(R.string.tile_messaging_search))
        .setIconContent(ID_IC_SEARCH)
        .build()

    private fun Row.Builder.addContact(
        state: MessagingTileState,
        i: Int
    ): Row.Builder = if (state.contacts.size > i) {
        addContent(
            contactLayout(
                contact = state.contacts[i],
                avatar = state.avatars[state.contacts[i].id],
                clickable = Clickable.Builder()
                    .setOnClick(LoadAction.Builder().build())
                    .build()
            )
        )
    } else {
        this
    }

    companion object {
        // Updating this version triggers a new call to onResourcesRequest(). This is useful for dynamic
        // resources, the contents of which change even though their id stays the same (e.g. a graph).
        // In this sample, our resources are all fixed, so we use a constant value.
        private const val RESOURCES_VERSION = "2"

        // Dimensions
        private val SPACING_TITLE_SUBTITLE = DimensionBuilders.dp(4f)
        private val SPACING_SUBTITLE_CONTACTS = DimensionBuilders.dp(12f)
        private val SPACING_CONTACTS_HORIZONTAL = DimensionBuilders.dp(8f)
        private val SPACING_CONTACTS_VERTICAL = DimensionBuilders.dp(4f)

        // Resource identifiers for images
        private const val ID_IC_SEARCH = "ic_search"
        private const val ID_CONTACT_PREFIX = "contact_"
    }
}
