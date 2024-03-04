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

@file:OptIn(ExperimentalHorologistApi::class)

package com.example.android.wearable.composestarter.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.TitleCard
import androidx.wear.compose.material.scrollAway
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.example.android.wearable.composestarter.R
import com.example.android.wearable.composestarter.presentation.theme.WearAppTheme
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll

/**
 * Simple "Hello, World" app meant as a starting point for a new project using Compose for Wear OS.
 *
 * Displays only a centered [Text] composable, and the actual text varies based on the shape of the
 * device (round vs. square/rectangular).
 *
 * Use the Wear version of Compose Navigation. You can carry
 * over your knowledge from mobile and it supports the swipe-to-dismiss gesture (Wear OS's
 * back action). For more information, go here:
 * https://developer.android.com/reference/kotlin/androidx/wear/compose/navigation/package-summary
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            installSplashScreen()

            WearApp()

            setTheme(android.R.style.Theme_DeviceDefault)
        }
    }
}

@Composable
fun WearApp() {
    val navController = rememberSwipeDismissableNavController()

    WearAppTheme {
        SwipeDismissableNavHost(navController = navController, startDestination = "menu") {
            composable("menu") {
                GreetingScreen("Android", onShowList = { navController.navigate("list") })
            }
            composable("list") {
                ListScreen()
            }
        }
    }
}

@Composable
fun GreetingScreen(greetingName: String, onShowList: () -> Unit) {
    val scrollState = ScrollState(0)

    // Wear design guidelines specify a 5.2% horizontal padding on each side of the list.
    val horizontalPadding = (0.052f * LocalConfiguration.current.screenWidthDp).dp

    /* If you have enough items in your list, use [ScalingLazyColumn] which is an optimized
     * version of LazyColumn for wear devices with some added features. For more information,
     * see d.android.com/wear/compose.
     */
    Scaffold(
        timeText = { TimeText(modifier = Modifier.scrollAway(scrollState)) },
        positionIndicator = { PositionIndicator(scrollState) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .rotaryWithScroll(scrollState)
                .padding(horizontal = horizontalPadding),
            verticalArrangement = Arrangement.Center
        ) {
            Greeting(greetingName = greetingName)
            Chip(label = { Text("Show List") }, onClick = onShowList, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun ListScreen() {
    val listState =
        rememberScalingLazyListState(initialCenterItemIndex = 1, initialCenterItemScrollOffset = 0)

    Scaffold(
        timeText = {
            TimeText(
                modifier = Modifier.scrollAway(
                    listState,
                    itemIndex = 1,
                    offset = 0.dp
                )
            )
        },
        positionIndicator = { PositionIndicator(listState) }
    ) {
        /*
         * Using the Horologist [ScalingLazyColumn] here takes care of the horizontal and vertical
         * padding for the list, so there is no need to specify it, as in the [GreetingScreen]
         * composable.
         */
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .rotaryWithScroll(listState)
        ) {
            item {
                ListHeader {
                    Text("Header")
                }
            }
            item {
                TitleCard(title = { Text("Example Title") }, onClick = { }) {
                    Text("Example Content\nMore Lines\nAnd More")
                }
            }
            item {
                Chip(label = { Text("Example Chip") }, onClick = { }, modifier = Modifier.fillMaxWidth())
            }
            item {
                Button(
                    onClick = { }
                ) {
                    Icon(imageVector = Icons.Default.Build, contentDescription = "Example Button")
                }
            }
        }
    }
}

@Composable
fun Greeting(greetingName: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = stringResource(R.string.hello_world, greetingName)
    )
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun GreetingScreenPreview() {
    GreetingScreen("Preview Android", onShowList = {})
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun ListScreenPreview() {
    ListScreen()
}
