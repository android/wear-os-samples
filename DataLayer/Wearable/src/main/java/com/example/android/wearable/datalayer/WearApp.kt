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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.navigation3.rememberSwipeDismissableSceneStrategy
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import kotlinx.serialization.Serializable

@Serializable
sealed interface WearAppKey : NavKey

@Serializable
data object MainScreenKey : WearAppKey

@Serializable
data object NodesListScreenKey : WearAppKey

@Serializable
data object CameraNodesListScreenKey : WearAppKey

@Composable
fun WearApp(mainViewModel: MainViewModel) {
    val backStack = rememberNavBackStack(MainScreenKey)
    AppScaffold {
        val entryProvider = remember {
            entryProvider<NavKey> {
                entry<MainScreenKey> {
                    MainScreen(
                        onShowNodesList = { backStack.add(NodesListScreenKey) },
                        onShowCameraNodesList = { backStack.add(CameraNodesListScreenKey) },
                        mainViewModel = mainViewModel
                    )
                }
                entry<NodesListScreenKey> {
                    ConnectedNodesScreen()
                }
                entry<CameraNodesListScreenKey> {
                    CameraNodesScreen()
                }
            }
        }
        val swipeDismissableSceneStrategy = rememberSwipeDismissableSceneStrategy<NavKey>()
        NavDisplay(
            backStack = backStack,
            entryProvider = entryProvider,
            sceneStrategies = listOf(swipeDismissableSceneStrategy)
        )
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

@Composable
fun MainScreen(
    image: Bitmap?,
    events: List<Event>,
    onShowNodesList: () -> Unit,
    onShowCameraNodesList: () -> Unit
) {
    val listState = rememberTransformingLazyColumnState()

    ScreenScaffold(
        scrollState = listState
    ) { padding ->
        TransformingLazyColumn(
            state = listState,
            contentPadding = padding
        ) {
            item {
                Button(
                    label = {
                        Text(
                            text = stringResource(id = R.string.query_other_devices)
                        )
                    },
                    onClick = onShowNodesList,
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumVerticalContentPadding(
                            ButtonDefaults.minimumVerticalListContentPadding
                        )
                )
            }
            item {
                Button(
                    label = {
                        Text(
                            text = stringResource(id = R.string.query_mobile_camera)
                        )
                    },
                    onClick = onShowCameraNodesList,
                    modifier = Modifier.fillMaxWidth()
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumVerticalContentPadding(
                                ButtonDefaults.minimumVerticalListContentPadding
                            ),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                items(events) { event ->
                    Card(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.minimumVerticalContentPadding(
                            CardDefaults.minimumVerticalListContentPadding
                        )
                    ) {
                        Column {
                            Text(
                                stringResource(id = event.title),
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                event.text,
                                style = MaterialTheme.typography.bodyMedium
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
        image = createBitmap(100, 100).apply {
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
