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
package com.example.android.wearable.composeadvanced.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.android.wearable.composeadvanced.util.JankPrinter

/**
 * Compose for Wear OS app that demonstrates how to use Wear specific Scaffold, Navigation,
 * curved text, Chips, and many other composables.
 *
 * Displays different text at the bottom of the landing screen depending on shape of the device
 * (round vs. square/rectangular).
 */
class MainActivity : ComponentActivity() {
    private lateinit var jankPrinter: JankPrinter
    internal lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Used to power various ViewModels for different screens.
        val watchRepository = (application as BaseApplication).watchRepository

        jankPrinter = JankPrinter()

        setContent {
            navController = rememberSwipeDismissableNavController()

            WearApp(
                watchRepository = watchRepository,
                swipeDismissableNavController = navController
            )

//            MapScreen()

            LaunchedEffect(Unit) {
                navController.currentBackStackEntryFlow.collect {
                    jankPrinter.setRouteState(route = it.destination.route)
                }
            }
        }

        jankPrinter.installJankStats(activity = this)
    }
}
