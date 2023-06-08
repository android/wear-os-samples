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
@file:OptIn(ExperimentalFoundationApi::class)

package com.example.android.wearable.composeadvanced.presentation.ui.map

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.SwipeToDismissBox
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.rememberSwipeToDismissBoxState
import com.example.android.wearable.composeadvanced.presentation.theme.WearAppTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

/**
 * Compose for Wear OS activity that demonstrates a Map.
 *
 * Needs to be in a separate activity because of scroll interaction issues inside
 * SwipeDismissableNavHost.
 */
class MapActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val cameraState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(singapore, 10f)
            }
            WearAppTheme {
//                val state = rememberSwipeToDismissBoxState()
//                SwipeToDismissBox(state = state, hasBackground = false) { isBackground ->
//                    if (!isBackground) {
                val pagerState = rememberPagerState {
                    3
                }
                val coroutineScope = rememberCoroutineScope()
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = pagerState.currentPage != 1
                ) { page ->
                    if (page == 1) {
                        MapScreen(
                            cameraState = cameraState,
                            onClose = {
                                coroutineScope.launch {
                                    pagerState.scrollToPage(0)
                                }
                            }
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (page == 0) {
                                Text("Before")
                            } else if (page == 2) {
                                Text("After")
                            } else {
                                Text("Page $page")
                            }
                        }
                    }
                }
            }
//                }
//            }
        }
    }
}
