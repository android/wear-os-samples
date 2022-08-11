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
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.wear.tiles.DeviceParametersBuilders
import androidx.wear.tiles.ModifiersBuilders
import androidx.wear.tiles.material.Button
import androidx.wear.tiles.material.ButtonColors
import androidx.wear.tiles.material.ChipColors
import androidx.wear.tiles.material.CompactChip
import androidx.wear.tiles.material.layouts.MultiButtonLayout
import androidx.wear.tiles.material.layouts.PrimaryLayout
import com.example.wear.tiles.R
import com.example.wear.tiles.tools.IconSizePreview
import com.example.wear.tiles.tools.WearSmallRoundDevicePreview
import com.example.wear.tiles.tools.emptyClickable
import com.google.android.horologist.compose.tools.LayoutElementPreview
import com.google.android.horologist.compose.tools.LayoutRootPreview
import com.google.android.horologist.compose.tools.buildDeviceParameters
import com.google.android.horologist.tiles.images.drawableResToImageResource

/**
 * Layout definition for the Messaging Tile.
 *
 * By separating the layout completely, we can pass fake data for the [MessageTilePreview] so it can
 * be rendered in Android Studio (use the "Split" or "Design" editor modes).
 */
internal fun messagingTileLayout(
    state: MessagingTileState,
    context: Context,
    deviceParameters: DeviceParametersBuilders.DeviceParameters
) = PrimaryLayout.Builder(deviceParameters)
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
    ).setPrimaryChipContent(
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

@WearSmallRoundDevicePreview
@Composable
private fun MessageTilePreview() {
    val context = LocalContext.current
    val state = MessagingTileState(MessagingRepo.knownContacts)
    LayoutRootPreview(
        messagingTileLayout(
            state,
            context,
            buildDeviceParameters(context.resources)
        )
    ) {
        addIdToImageMapping(
            state.contacts[1].imageResourceId(),
            bitmapToImageResource(
                BitmapFactory.decodeResource(context.resources, R.drawable.ali)
            )
        )
        addIdToImageMapping(
            state.contacts[2].imageResourceId(),
            bitmapToImageResource(
                BitmapFactory.decodeResource(context.resources, R.drawable.taylor)
            )
        )
        addIdToImageMapping(
            MessagingTileRenderer.ID_IC_SEARCH,
            drawableResToImageResource(R.drawable.ic_search_24)
        )
    }
}

@IconSizePreview
@Composable
private fun ContactPreview() {
    LayoutElementPreview(
        contactLayout(
            context = LocalContext.current,
            contact = MessagingRepo.knownContacts[0],
            clickable = emptyClickable
        )
    )
}

@IconSizePreview
@Composable
private fun ContactWithImagePreview() {
    val context = LocalContext.current
    val contact = MessagingRepo.knownContacts[1]
    val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ali)

    val layout = contactLayout(
        context = context,
        contact = contact,
        clickable = emptyClickable
    )

    LayoutElementPreview(layout) {
        addIdToImageMapping(
            "${MessagingTileRenderer.ID_CONTACT_PREFIX}${contact.id}",
            bitmapToImageResource(bitmap)
        )
    }
}

@IconSizePreview
@Composable
private fun SearchButtonPreview() {
    LayoutElementPreview(
        searchLayout(
            context = LocalContext.current,
            clickable = emptyClickable
        )
    ) {
        addIdToImageMapping(
            MessagingTileRenderer.ID_IC_SEARCH,
            drawableResToImageResource(R.drawable.ic_search_24)
        )
    }
}
