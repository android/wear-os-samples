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
import androidx.annotation.DrawableRes
import androidx.wear.tiles.ActionBuilders.LoadAction
import androidx.wear.tiles.ColorBuilders
import androidx.wear.tiles.DeviceParametersBuilders
import androidx.wear.tiles.DimensionBuilders
import androidx.wear.tiles.DimensionBuilders.ExpandedDimensionProp
import androidx.wear.tiles.DimensionBuilders.WrappedDimensionProp
import androidx.wear.tiles.LayoutElementBuilders.Box
import androidx.wear.tiles.LayoutElementBuilders.Column
import androidx.wear.tiles.LayoutElementBuilders.FontStyles
import androidx.wear.tiles.LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER
import androidx.wear.tiles.LayoutElementBuilders.LayoutElement
import androidx.wear.tiles.LayoutElementBuilders.Row
import androidx.wear.tiles.LayoutElementBuilders.Spacer
import androidx.wear.tiles.LayoutElementBuilders.Text
import androidx.wear.tiles.LayoutElementBuilders.VERTICAL_ALIGN_CENTER
import androidx.wear.tiles.ModifiersBuilders.Clickable
import androidx.wear.tiles.ModifiersBuilders.Modifiers
import androidx.wear.tiles.ModifiersBuilders.Semantics
import androidx.wear.tiles.ResourceBuilders
import androidx.wear.tiles.ResourceBuilders.ImageResource
import androidx.wear.tiles.ResourceBuilders.Resources
import androidx.wear.tiles.material.Button
import androidx.wear.tiles.material.ButtonColors
import com.example.wear.tiles.R
import com.google.android.horologist.tiles.render.SingleTileLayoutRenderer

class MessagingTileRenderer(context: Context) :
    SingleTileLayoutRenderer<MessagingTileState, Map<Contact, Bitmap>>(context) {

    override fun renderTile(
        state: MessagingTileState,
        deviceParameters: DeviceParametersBuilders.DeviceParameters
    ): LayoutElement {
        return tileLayout(state, deviceParameters)
    }

    override fun Resources.Builder.produceRequestedResources(
        resourceResults: Map<Contact, Bitmap>,
        deviceParameters: DeviceParametersBuilders.DeviceParameters,
        resourceIds: MutableList<String>
    ) {
        if (resourceIds.isEmpty() || resourceIds.contains(ID_IC_SEARCH)) {
            addIdToImageMapping(
                ID_IC_SEARCH,
                imageResourceFrom(R.drawable.ic_search)
            )
        }

        // Add the scaled & cropped avatar images
        resourceResults.forEach { (contact, bitmap) ->
            val imageResource = bitmapToImageResource(bitmap)
            // Add each created image resource to the list
            addIdToImageMapping(
                "$ID_CONTACT_PREFIX${contact.id}",
                imageResource
            )
        }
    }

    private fun imageResourceFrom(@DrawableRes resourceId: Int) = ImageResource.Builder()
        .setAndroidResourceByResId(
            ResourceBuilders.AndroidImageResourceByResId.Builder()
                .setResourceId(resourceId)
                .build()
        )
        .build()

    private fun tileLayout(
        state: MessagingTileState,
        deviceParameters: DeviceParametersBuilders.DeviceParameters,
    ): LayoutElement {
        return Box.Builder()
            .setWidth(ExpandedDimensionProp.Builder().build())
            .setHeight(ExpandedDimensionProp.Builder().build())
            .setVerticalAlignment(VERTICAL_ALIGN_CENTER)
            .setHorizontalAlignment(HORIZONTAL_ALIGN_CENTER)
            .addContent(
                Column.Builder()
                    .setWidth(WrappedDimensionProp.Builder().build())
                    .setHeight(WrappedDimensionProp.Builder().build())
                    .setHorizontalAlignment(HORIZONTAL_ALIGN_CENTER)
                    .addContent(
                        Text.Builder()
                            .setText(context.resources.getString(R.string.tile_messaging_title))
                            .setFontStyle(
                                FontStyles
                                    .title3(deviceParameters)
                                    .setColor(
                                        ColorBuilders.argb(
                                            context.getColor(
                                                R.color.primary
                                            )
                                        )
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
                                            context.getColor(
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
                            .apply {
                                if (state.contacts.size >= 1) {
                                    addContent(
                                        contactLayout(
                                            contact = state.contacts[0],
                                            clickable = Clickable.Builder()
                                                .setOnClick(LoadAction.Builder().build())
                                                .build()
                                        )
                                    )
                                }
                                if (state.contacts.size >= 2) {
                                    addContent(
                                        Spacer.Builder()
                                            .setWidth(SPACING_CONTACTS_HORIZONTAL).build()
                                    )
                                    addContent(
                                        contactLayout(
                                            contact = state.contacts[1],
                                            clickable = Clickable.Builder()
                                                .setOnClick(LoadAction.Builder().build())
                                                .build()
                                        )
                                    )
                                }
                                if (state.contacts.size >= 3) {
                                    addContent(
                                        Spacer.Builder()
                                            .setWidth(SPACING_CONTACTS_HORIZONTAL).build()
                                    )
                                    addContent(
                                        contactLayout(
                                            contact = state.contacts[2],
                                            clickable = Clickable.Builder()
                                                .setOnClick(LoadAction.Builder().build())
                                                .build()
                                        )
                                    )
                                }
                            }
                            .build()
                    )
                    .addContent(
                        Spacer.Builder().setHeight(SPACING_CONTACTS_VERTICAL)
                            .build()
                    )
                    .addContent(
                        Row.Builder()
                            .apply {
                                if (state.contacts.size >= 4) {
                                    addContent(
                                        contactLayout(
                                            contact = state.contacts[3],
                                            clickable = Clickable.Builder()
                                                .setOnClick(LoadAction.Builder().build())
                                                .build()
                                        )
                                    )
                                    addContent(
                                        Spacer.Builder()
                                            .setWidth(SPACING_CONTACTS_HORIZONTAL).build()
                                    )
                                }
                            }
                            .addContent(searchLayout())
                            .build()
                    )
                    .setModifiers(
                        Modifiers.Builder()
                            .setSemantics(
                                Semantics.Builder()
                                    .setContentDescription(
                                        context.getString(R.string.tile_messaging_label)
                                    )
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build()
    }

    internal fun contactLayout(
        contact: Contact,
        clickable: Clickable,
    ) = Button.Builder(context, clickable).apply {
        setContentDescription(contact.name)
        setButtonColors(
            ButtonColors(
                context.getColor(R.color.secondary),
                context.getColor(R.color.primary)
            )
        )
        if (contact.avatarUrl == null) {
            setTextContent(contact.initials)
        } else {
            setImageContent("$ID_CONTACT_PREFIX${contact.id}")
        }
    }
        .build()

    internal fun searchLayout() = Button.Builder(
        context,
        Clickable.Builder()
            .setOnClick(LoadAction.Builder().build())
            .setId("")
            .build()
    )
        .setButtonColors(
            ButtonColors(
                context.getColor(R.color.primaryDark),
                context.getColor(R.color.primary)
            )
        )
        .setContentDescription(context.getString(R.string.tile_messaging_search))
        .setIconContent(ID_IC_SEARCH)
        .build()

    companion object {
        // Dimensions
        private val SPACING_TITLE_SUBTITLE = DimensionBuilders.dp(4f)
        private val SPACING_SUBTITLE_CONTACTS = DimensionBuilders.dp(12f)
        private val SPACING_CONTACTS_HORIZONTAL = DimensionBuilders.dp(8f)
        private val SPACING_CONTACTS_VERTICAL = DimensionBuilders.dp(4f)

        // Resource identifiers for images
        internal const val ID_IC_SEARCH = "ic_search"
        internal const val ID_CONTACT_PREFIX = "contact:"
    }
}
