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
import androidx.annotation.DrawableRes
import androidx.wear.protolayout.DeviceParametersBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.ResourceBuilders.Resources
import androidx.wear.protolayout.material.Button
import androidx.wear.protolayout.material.ButtonColors
import androidx.wear.protolayout.material.ChipColors
import androidx.wear.protolayout.material.CompactChip
import androidx.wear.protolayout.material.layouts.MultiButtonLayout
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.wear.tiles.R
import com.example.wear.tiles.golden.resources
import com.example.wear.tiles.tools.emptyClickable

/**
 * Layout definition for the Messaging Tile.
 *
 * By separating the layout completely, we can pass fake data for the [messagingTilePreview] so it
 * can be rendered in Android Studio (use the "Split" or "Design" editor modes).
 */
internal fun messagingTileLayout(
    state: MessagingTileState,
    context: Context,
    deviceParameters: DeviceParametersBuilders.DeviceParameters
) = PrimaryLayout.Builder(deviceParameters)
    .setResponsiveContentInsetEnabled(true)
    .setContent(
        MultiButtonLayout.Builder()
            .apply {
                // In a PrimaryLayout with a compact chip at the bottom, we can fit 5 buttons.
                // We're only taking the first 4 contacts so that we can fit a Search button too.
                state.contacts.take(4).forEach { contact ->
                    addButtonContent(contactLayout(context, contact, emptyClickable))
                }
            }
            .addButtonContent(searchLayout(context, emptyClickable))
            .build()
    )
    .setPrimaryChipContent(
        CompactChip.Builder(
            context,
            context.getString(R.string.tile_messaging_create_new),
            emptyClickable,
            deviceParameters
        )
            .setChipColors(ChipColors.primaryChipColors(MessagingTileTheme.colors))
            .build()
    )
    .build()

private fun contactLayout(
    context: Context,
    contact: Contact,
    clickable: ModifiersBuilders.Clickable
) = Button.Builder(context, clickable)
    .setContentDescription(contact.name)
    .apply {
        if (contact.avatarUrl != null) {
            setImageContent(contact.imageResourceId())
        } else {
            setTextContent(contact.initials)
            setButtonColors(ButtonColors.secondaryButtonColors(MessagingTileTheme.colors))
        }
    }
    .build()

private fun Contact.imageResourceId() = "${MessagingTileRenderer.ID_CONTACT_PREFIX}$id"

private fun searchLayout(
    context: Context,
    clickable: ModifiersBuilders.Clickable
) = Button.Builder(context, clickable)
    .setContentDescription(context.getString(R.string.tile_messaging_search))
    .setIconContent(MessagingTileRenderer.ID_IC_SEARCH)
    .setButtonColors(ButtonColors.secondaryButtonColors(MessagingTileTheme.colors))
    .build()

@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
private fun messagingTilePreview(context: Context): TilePreviewData {
    val state = MessagingTileState(MessagingRepo.knownContacts)
    return TilePreviewData(
        onTileResourceRequest = resources {
            addIdToImageMapping(
                state.contacts[1].imageResourceId(),
                R.drawable.ali
            )
            addIdToImageMapping(
                state.contacts[2].imageResourceId(),
                R.drawable.taylor
            )
            addIdToImageMapping(
                MessagingTileRenderer.ID_IC_SEARCH,
                R.drawable.ic_search_24
            )
        },
        onTileRequest = { request ->
            TilePreviewHelper.singleTimelineEntryTileBuilder(
                messagingTileLayout(
                    state,
                    context,
                    request.deviceConfiguration
                )
            ).build()
        })
}

@Preview
private fun contactPreview(context: Context) = TilePreviewData(
    onTileRequest = {
        TilePreviewHelper.singleTimelineEntryTileBuilder(
            contactLayout(
                context = context,
                contact = MessagingRepo.knownContacts[0],
                clickable = emptyClickable
            )
        ).build()
    }
)

@Preview
private fun contactWithImagePreview(context: Context): TilePreviewData {
    val contact = MessagingRepo.knownContacts[1]

    return TilePreviewData(
        onTileResourceRequest = {
            Resources.Builder().addIdToImageMapping(
                "${MessagingTileRenderer.ID_CONTACT_PREFIX}${contact.id}",
                R.drawable.ali
            ).build()
        },
        onTileRequest = {
            TilePreviewHelper.singleTimelineEntryTileBuilder(
                contactLayout(context = context, contact = contact, clickable = emptyClickable)
            ).build()
        })
}

@Preview
private fun searchButtonPreview(context: Context) = TilePreviewData(
    onTileResourceRequest = {
        Resources.Builder().addIdToImageMapping(
            MessagingTileRenderer.ID_IC_SEARCH,
            R.drawable.ic_search_24
        ).build()
    },
    onTileRequest = {
        TilePreviewHelper.singleTimelineEntryTileBuilder(
            searchLayout(context, emptyClickable)
        ).build()
    })

fun Resources.Builder.addIdToImageMapping(
    id: String,
    @DrawableRes resId: Int
): Resources.Builder = addIdToImageMapping(
    id, ResourceBuilders.ImageResource.Builder()
        .setAndroidResourceByResId(
            ResourceBuilders.AndroidImageResourceByResId.Builder()
                .setResourceId(resId)
                .build()
        )
        .build()
)
