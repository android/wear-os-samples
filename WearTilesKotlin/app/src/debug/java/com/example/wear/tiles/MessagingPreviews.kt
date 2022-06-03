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


