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
package com.example.wear.tiles

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.wear.tiles.messaging.Contact
import com.example.wear.tiles.messaging.MessagingRepo
import com.example.wear.tiles.messaging.MessagingTileRenderer
import com.example.wear.tiles.messaging.MessagingTileState
import com.example.wear.tiles.tools.TileRendererPreview
import com.example.wear.tiles.tools.WearLargeRoundDevicePreview
import com.example.wear.tiles.tools.WearPreviewDevices
import com.example.wear.tiles.tools.WearPreviewFontSizes
import com.example.wear.tiles.tools.WearPreviewLocales

@WearPreviewDevices
@WearPreviewFontSizes
@WearPreviewLocales
@Composable
fun MessagingPreviews() {
    val context = LocalContext.current
    val renderer = remember { MessagingTileRenderer(context) }

    val avatars = mapOf<Long, Bitmap>(
        1L to BitmapFactory.decodeResource(context.resources, R.drawable.ali),
        3L to BitmapFactory.decodeResource(context.resources, R.drawable.taylor),
    )
    val state = MessagingTileState(MessagingRepo.knownContacts, avatars)

    TileRendererPreview(state, renderer)
}

@WearLargeRoundDevicePreview
@Composable
fun MessagingPreviewsDifferentNumber(
    @PreviewParameter(SampleContactsProvider::class) people: List<Contact>
) {
    val context = LocalContext.current
    val renderer = remember { MessagingTileRenderer(context) }

    val state = MessagingTileState(people, mapOf())

    TileRendererPreview(state, renderer)
}

class SampleContactsProvider : PreviewParameterProvider<List<Contact>> {
    override val values = (1..4).map { MessagingRepo.knownContacts.take(it) }.asSequence()
}
