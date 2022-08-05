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
import androidx.wear.tiles.DeviceParametersBuilders
import androidx.wear.tiles.LayoutElementBuilders.LayoutElement
import androidx.wear.tiles.ResourceBuilders.Resources
import com.example.wear.tiles.R
import com.google.android.horologist.tiles.images.drawableResToImageResource
import com.google.android.horologist.tiles.render.SingleTileLayoutRenderer

class MessagingTileRenderer(context: Context) :
    SingleTileLayoutRenderer<MessagingTileState, Map<Contact, Bitmap>>(context) {

    override fun renderTile(
        state: MessagingTileState,
        deviceParameters: DeviceParametersBuilders.DeviceParameters
    ): LayoutElement {
        return messagingTileLayout(state, context, deviceParameters)
    }

    /**
     * If we want to display an image in our layout, we have to add the image resource to the
     * [Resources.Builder].
     */
    override fun Resources.Builder.produceRequestedResources(
        resourceResults: Map<Contact, Bitmap>,
        deviceParameters: DeviceParametersBuilders.DeviceParameters,
        resourceIds: MutableList<String>
    ) {
        // If `resourceIds` is empty, it means all resources are being requested so we should add
        // the Search image resource in both of these cases.
        if (resourceIds.isEmpty() || resourceIds.contains(ID_IC_SEARCH)) {
            addIdToImageMapping(
                /* id = */ ID_IC_SEARCH,
                /* image = */ drawableResToImageResource(R.drawable.ic_search_24)
            )
        }

        // We already checked `resourceIds` in `MessagingTileService` because we needed to know
        // which avatars needed to be fetched from the network; `resourceResults` was already
        // filtered so it only contains bitmaps for the requested resources.
        resourceResults.forEach { (contact, bitmap) ->
            addIdToImageMapping(
                /* id = */ "$ID_CONTACT_PREFIX${contact.id}",
                /* image = */ bitmapToImageResource(bitmap)
            )
        }
    }

    companion object {

        // Resource identifiers for images
        internal const val ID_IC_SEARCH = "ic_search"
        internal const val ID_CONTACT_PREFIX = "contact:"
    }
}
