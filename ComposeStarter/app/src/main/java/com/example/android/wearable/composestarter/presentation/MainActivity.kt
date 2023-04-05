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
@file:OptIn(ExperimentalPagerApi::class, ExperimentalHorologistComposeLayoutApi::class)

package com.example.android.wearable.composestarter.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.scrollAway
import com.example.android.wearable.composestarter.R
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import com.google.android.horologist.compose.pager.PagerScreen

/**
 * Simple "Hello, World" app meant as a starting point for a new project using Compose for Wear OS.
 *
 * Displays only a centered [Text] composable, and the actual text varies based on the shape of the
 * device (round vs. square/rectangular).
 *
 * If you plan to have multiple screens, use the Wear version of Compose Navigation. You can carry
 * over your knowledge from mobile and it supports the swipe-to-dismiss gesture (Wear OS's
 * back action). For more information, go here:
 * https://developer.android.com/reference/kotlin/androidx/wear/compose/navigation/package-summary
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WearApp()
        }
    }
}

@Composable
fun WearApp() {
    PagerScreen(count = 5) { pageNumber ->
        val columnState = ScalingLazyColumnDefaults.scalingLazyColumnDefaults().create()
        Scaffold(
            vignette = {
                Vignette(vignettePosition = VignettePosition.TopAndBottom)
            },
            timeText = {
                TimeText(
                    modifier = Modifier.scrollAway(
                        columnState.state,
                        columnState.initialScrollPosition.index,
                        columnState.initialScrollPosition.offsetPx.dp
                    )
                )
            },
            positionIndicator = { PositionIndicator(columnState.state) },
        ) {
            ScalingLazyColumn(columnState = columnState) {
                item {
                    ListHeader {
                        Text(text = "Page $pageNumber")
                    }
                }
                items(10) {
                    Card(onClick = { /*TODO*/ }) {
                        Column(modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "This is card element")
                            Image(painter = painterResource(
                                id = R.drawable.abc_vector_test),
                                contentDescription = "",
                                modifier = Modifier.size(48.dp))
                            Text(text = "And number is $it")
                        }
                    }
                }
            }
        }
    }
}
