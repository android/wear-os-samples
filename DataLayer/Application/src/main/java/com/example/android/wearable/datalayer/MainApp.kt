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
package com.example.android.wearable.datalayer

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * The UI affording the actions the user can take, along with a list of the events and the image
 * to be sent to the wearable devices.
 */
@Composable
fun MainApp(
    events: List<Event>,
    image: Bitmap?,
    isCameraSupported: Boolean,
    onTakePhotoClick: () -> Unit,
    onSendPhotoClick: () -> Unit,
    onStartWearableActivityClick: () -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Button(
                        onClick = onTakePhotoClick,
                        enabled = isCameraSupported
                    ) {
                        Text(stringResource(id = R.string.take_photo))
                    }
                    Button(
                        onClick = onSendPhotoClick,
                        enabled = image != null
                    ) {
                        Text(stringResource(id = R.string.send_photo))
                    }
                }

                Box(modifier = Modifier.size(100.dp)) {
                    if (image == null) {
                        Image(
                            painterResource(id = R.drawable.ic_content_picture),
                            contentDescription = stringResource(
                                id = R.string.photo_placeholder
                            ),
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Image(
                            image.asImageBitmap(),
                            contentDescription = stringResource(
                                id = R.string.captured_photo
                            ),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
            Divider()
        }
        item {
            Button(onClick = onStartWearableActivityClick) {
                Text(stringResource(id = R.string.start_wearable_activity))
            }
            Divider()
        }
        items(events) { event ->
            Column {
                Text(
                    stringResource(id = event.title),
                    style = MaterialTheme.typography.subtitle1
                )
                Text(
                    event.text,
                    style = MaterialTheme.typography.body2
                )
            }
            Divider()
        }
    }
}

@Preview
@Composable
fun MainAppPreview() {
    MainApp(
        events = listOf(
            Event(
                title = R.string.data_item_changed,
                text = "Event 1"
            ),
            Event(
                title = R.string.data_item_deleted,
                text = "Event 2"
            ),
            Event(
                title = R.string.data_item_unknown,
                text = "Event 3"
            ),
            Event(
                title = R.string.message_from_watch,
                text = "Event 4"
            ),
            Event(
                title = R.string.data_item_changed,
                text = "Event 5"
            ),
            Event(
                title = R.string.data_item_deleted,
                text = "Event 6"
            )
        ),
        image = null,
        isCameraSupported = true,
        onTakePhotoClick = {},
        onSendPhotoClick = {},
        onStartWearableActivityClick = {}
    )
}
