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
import androidx.wear.tiles.ActionBuilders
import androidx.wear.tiles.ColorBuilders
import androidx.wear.tiles.DeviceParametersBuilders
import androidx.wear.tiles.DimensionBuilders
import androidx.wear.tiles.LayoutElementBuilders
import androidx.wear.tiles.ModifiersBuilders
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
        return LayoutElementBuilders.Column.Builder()
            .addContent(
                LayoutElementBuilders.Text.Builder()
                    .setText(context.resources.getString(R.string.tile_messaging_title))
                    .setFontStyle(
                        LayoutElementBuilders.FontStyles
                            .title3(deviceParameters)
                            .setColor(
                                ColorBuilders.argb(ContextCompat.getColor(context, R.color.primary))
                            )
                            .build()
                    )
                    .build()
            )
            .addContent(LayoutElementBuilders.Spacer.Builder().setHeight(SPACING_TITLE_SUBTITLE)
                .build())
            .addContent(
                LayoutElementBuilders.Text.Builder()
                    .setText(context.getString(R.string.tile_messaging_subtitle))
                    .setFontStyle(
                        LayoutElementBuilders.FontStyles
                            .caption1(deviceParameters)
                            .setColor(
                                ColorBuilders.argb(ContextCompat.getColor(context,
                                    R.color.onSecondary))
                            )
                            .build()
                    )
                    .build()
            )
            .addContent(LayoutElementBuilders.Spacer.Builder().setHeight(SPACING_SUBTITLE_CONTACTS)
                .build())
            .addContent(
                LayoutElementBuilders.Row.Builder()
                    .addContent(
                        contactLayout(
                            contact = state.contacts[0],
                            avatar = state.avatars[state.contacts[0].id],
                            clickable = ModifiersBuilders.Clickable.Builder()
                                .setOnClick(ActionBuilders.LoadAction.Builder().build())
                                .build()
                        )
                    )
                    .addContent(LayoutElementBuilders.Spacer.Builder()
                        .setWidth(SPACING_CONTACTS_HORIZONTAL).build())
                    .addContent(
                        contactLayout(
                            contact = state.contacts[1],
                            avatar = state.avatars[state.contacts[1].id],
                            clickable = ModifiersBuilders.Clickable.Builder()
                                .setOnClick(ActionBuilders.LoadAction.Builder().build())
                                .build()
                        )
                    )
                    .addContent(LayoutElementBuilders.Spacer.Builder()
                        .setWidth(SPACING_CONTACTS_HORIZONTAL).build())
                    .addContent(
                        contactLayout(
                            contact = state.contacts[2],
                            avatar = state.avatars[state.contacts[2].id],
                            clickable = ModifiersBuilders.Clickable.Builder()
                                .setOnClick(ActionBuilders.LoadAction.Builder().build())
                                .build()
                        )
                    )
                    .build()
            )
            .addContent(LayoutElementBuilders.Spacer.Builder().setHeight(SPACING_CONTACTS_VERTICAL)
                .build())
            .addContent(
                LayoutElementBuilders.Row.Builder()
                    .addContent(
                        contactLayout(
                            contact = state.contacts[3],
                            avatar = state.avatars[state.contacts[3].id],
                            clickable = ModifiersBuilders.Clickable.Builder()
                                .setOnClick(ActionBuilders.LoadAction.Builder().build())
                                .build()
                        )
                    )
                    .addContent(LayoutElementBuilders.Spacer.Builder()
                        .setWidth(SPACING_CONTACTS_HORIZONTAL).build())
                    .addContent(searchLayout())
                    .build()
            )
            .setModifiers(
                ModifiersBuilders.Modifiers.Builder()
                    .setSemantics(
                        ModifiersBuilders.Semantics.Builder()
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
        clickable: ModifiersBuilders.Clickable,
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
        ModifiersBuilders.Clickable.Builder()
            .setOnClick(ActionBuilders.LoadAction.Builder().build())
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
