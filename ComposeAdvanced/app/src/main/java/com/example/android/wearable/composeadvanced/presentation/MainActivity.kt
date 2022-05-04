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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.android.wearable.composeadvanced.data.WatchRepository
import com.example.android.wearable.composeadvanced.presentation.navigation.WATCH_ID_NAV_ARGUMENT
import com.example.android.wearable.composeadvanced.presentation.ui.watch.WatchDetailViewModel
import com.example.android.wearable.composeadvanced.presentation.ui.watchlist.WatchListViewModel
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

        jankPrinter = JankPrinter()

        setContent {
            navController = rememberSwipeDismissableNavController()

            WearApp(
                swipeDismissableNavController = navController,
                viewModelFactory = defaultViewModelProviderFactory
            )

            LaunchedEffect(Unit) {
                navController.currentBackStackEntryFlow.collect {
                    jankPrinter.setRouteState(route = it.destination.route)
                }
            }
        }

        jankPrinter.installJankStats(activity = this)
    }

    override fun getDefaultViewModelCreationExtras(): CreationExtras {
        return MutableCreationExtras(super.getDefaultViewModelCreationExtras()).apply {
            set(
                WatchRepository.WATCH_REPOSITORY_KEY,
                (application as BaseApplication).watchRepository
            )
        }
    }

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        return viewModelFactory {
            initializer {
                WatchListViewModel(
                    watchRepository = (application as BaseApplication).watchRepository
                )
            }
            initializer {
                val savedStateHandle = createSavedStateHandle()
                val watchId: Int = savedStateHandle[WATCH_ID_NAV_ARGUMENT]!!
                WatchDetailViewModel(
                    watchId = watchId,
                    watchRepository = (application as BaseApplication).watchRepository
                )
            }
        }
    }
}
