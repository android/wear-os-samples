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
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.AppScaffold
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults.ItemType
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.Chip

@Composable
fun WearApp(mainViewModel: MainViewModel) {
    AppScaffold {
        val navController = rememberSwipeDismissableNavController()
        SwipeDismissableNavHost(navController = navController, startDestination = "main") {
            composable("main") {
                MainScreen(
                    onShowNodesList = { navController.navigate("nodeslist") },
                    onShowCameraNodesList = { navController.navigate("cameraNodeslist") },
                    mainViewModel = mainViewModel
                )
            }
            composable("nodeslist") {
                ConnectedNodesScreen()
            }
            composable("cameraNodeslist") {
                CameraNodesScreen()
            }
        }
    }
}

@Composable
fun MainScreen(
    onShowNodesList: () -> Unit,
    onShowCameraNodesList: () -> Unit,
    mainViewModel: MainViewModel
) {
    MainScreen(
        mainViewModel.image,
        mainViewModel.events,
        onShowNodesList,
        onShowCameraNodesList
    )
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun MainScreen(
    image: Bitmap?,
    events: List<Event>,
    onShowNodesList: () -> Unit,
    onShowCameraNodesList: () -> Unit
) {
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ItemType.Chip,
            last = ItemType.Text
        )
    )

    ScreenScaffold(scrollState = columnState) {
            /*
             * The Horologist [ScalingLazyColumn] takes care of the horizontal and vertical
             * padding for the list, so there is no need to specify it.
             */
        ScalingLazyColumn(
            columnState = columnState,
            modifier = Modifier
        ) {
            item {
                Chip(
                    label = stringResource(id = R.string.query_other_devices),
                    onClick = onShowNodesList
                )
            }
            item {
                Chip(
                    label = stringResource(id = R.string.query_mobile_camera),
                    onClick = onShowCameraNodesList
                )
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(32.dp)
                ) {
                    if (image == null) {
                        Image(
                            painterResource(id = R.drawable.photo_placeholder),
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

            if (events.isEmpty()) {
                item {
                    Text(
                        stringResource(id = R.string.waiting),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                items(events) { event ->
                    Card(
                        onClick = {},
                        enabled = false
                    ) {
                        Column {
                            Text(
                                stringResource(id = event.title),
                                style = MaterialTheme.typography.title3
                            )
                            Text(
                                event.text,
                                style = MaterialTheme.typography.body2
                            )
                        }
                    }
                }
            }
        }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun MainScreenPreviewEvents() {
    MainScreen(
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
                title = R.string.message,
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
        image = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.WHITE)
        },
        onShowCameraNodesList = {},
        onShowNodesList = {}
    )
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun MainScreenPreviewEmpty() {
    MainScreen(
        events = emptyList(),
        image = null,
        onShowCameraNodesList = {},
        onShowNodesList = {}
    )
}
