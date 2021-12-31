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
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.rememberScalingLazyListState
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.android.wearable.composeadvanced.R
import com.example.android.wearable.composeadvanced.data.WatchRepository
import com.example.android.wearable.composeadvanced.presentation.components.CustomPositionIndicator
import com.example.android.wearable.composeadvanced.presentation.components.CustomTimeText
import com.example.android.wearable.composeadvanced.presentation.components.CustomVignette
import com.example.android.wearable.composeadvanced.presentation.navigation.Screen
import com.example.android.wearable.composeadvanced.presentation.navigation.WATCH_ID_NAV_ARGUMENT
import com.example.android.wearable.composeadvanced.presentation.theme.WearAppTheme
import com.example.android.wearable.composeadvanced.presentation.ui.landing.LandingScreen
import com.example.android.wearable.composeadvanced.presentation.ui.watch.WatchDetailScreen
import com.example.android.wearable.composeadvanced.presentation.ui.watchlist.WatchListScreen

/**
 * Compose for Wear OS app that demonstrates how to use Wear specific Scaffold, Navigation,
 * curved text, Chips, and many other composables.
 *
 * Displays different text at the bottom of the landing screen depending on shape of the device
 * (round vs. square/rectangular).
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Used to power various ViewModels for different screens.
        val watchRepository = (application as BaseApplication).watchRepository

        setContent {
            WearApp(watchRepository = watchRepository)
        }
    }
}

@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun WearApp(watchRepository: WatchRepository) {
    WearAppTheme {
        // The state for any scrollable content in the composable screens is hoisted to this level,
        // so the Scaffold can properly place the position indicator (also known as the
        // scroll indicator).
        // In our case, the watch list screen uses a ScalingLazyColumn and the watch details
        // screen uses a Column with scrolling enabled.
        // We also uses the states for various other things (like hiding the time when the user is
        // scrolling).
        // ----------
        // State for the ScalingLazyColumn (watch list screen)
        val scalingLazyListState: ScalingLazyListState = rememberScalingLazyListState()
        // State for the Column's vertical scroll modifier (watch detail screen).
        val watchDetailScrollState: ScrollState = rememberScrollState()

        val swipeDismissableNavController = rememberSwipeDismissableNavController()

        // Allows user to disable the text before the time.
        var showProceedingTextBeforeTime by rememberSaveable { mutableStateOf(false) }

        // Allows user to show/hide the vignette on appropriate screens.
        // IMPORTANT NOTE: Usually you want to show the vignette all the time on screens with
        // scrolling content, a rolling side button, or a rotating bezel. This preference is just
        // to visually demonstrate the vignette for the developer to see it on and off.
        var vignetteVisiblePreference by rememberSaveable { mutableStateOf(false) }

        // Observes the current back stack entry to show the vignette for the appropriate screens by
        // checking the destination + route.
        // TODO: Replace with .currentBackStackEntryAsState() once added to Compose for Wear OS
        // Navigation Library: https://issuetracker.google.com/issues/212739653
        val currentBackStackEntry by swipeDismissableNavController
            .currentBackStackEntryFlow.collectAsState(null)

        val currentRoute = currentBackStackEntry?.destination?.route

        Scaffold(
            // Scaffold places time at top of screen to follow Material Design guidelines.
            timeText = {
                CustomTimeText(
                    visible = !scalingLazyListState.isScrollInProgress &&
                        !watchDetailScrollState.isScrollInProgress,
                    showLeadingText = showProceedingTextBeforeTime,
                    leadingText = stringResource(R.string.leading_time_text)
                )
            },
            vignette = {
                // Only show vignette for screens with scrollable content.
                if (Screen.WatchList.route in currentRoute) {
                    CustomVignette(
                        visible = vignetteVisiblePreference,
                        vignettePosition = VignettePosition.TopAndBottom
                    )
                } else if (Screen.WatchDetail.route in currentRoute) {
                    CustomVignette(
                        visible = true,
                        vignettePosition = VignettePosition.TopAndBottom
                    )
                }
            },
            positionIndicator = {
                if (Screen.WatchList.route in currentRoute) {
                    CustomPositionIndicator(
                        visible = scalingLazyListState.isScrollInProgress,
                        scalingLazyListState = scalingLazyListState
                    )
                } else if (Screen.WatchDetail.route in currentRoute) {
                    CustomPositionIndicator(
                        visible = watchDetailScrollState.isScrollInProgress,
                        scrollState = watchDetailScrollState
                    )
                }
            }
        ) {
            /*
             * Wear OS's version of NavHost supports swipe-to-dismiss (similar to back
             * gesture on mobile). Otherwise, the code looks very similar.
             */
            SwipeDismissableNavHost(
                navController = swipeDismissableNavController,
                startDestination = Screen.Landing.route,
                modifier = Modifier.background(MaterialTheme.colors.background)
            ) {

                // Main Window
                composable(Screen.Landing.route) {
                    LandingScreen(
                        onClickWatchList = {
                            swipeDismissableNavController.navigate(Screen.WatchList.route)
                        },
                        proceedingTimeTextEnabled = showProceedingTextBeforeTime,
                        onClickProceedingTimeText = {
                            showProceedingTextBeforeTime = !showProceedingTextBeforeTime
                        }
                    )
                }

                composable(Screen.WatchList.route) {
                    WatchListScreen(
                        scalingLazyListState = scalingLazyListState,
                        watchRepository = watchRepository,
                        showVignette = vignetteVisiblePreference,
                        onClickVignetteToggle = { showVignette ->
                            vignetteVisiblePreference = showVignette
                        },
                        onClickWatch = { id ->
                            swipeDismissableNavController.navigate(
                                route = Screen.WatchDetail.route + "/" + id,
                            )
                        }
                    )
                }

                composable(
                    route = Screen.WatchDetail.route + "/{$WATCH_ID_NAV_ARGUMENT}",
                    arguments = listOf(
                        navArgument(WATCH_ID_NAV_ARGUMENT) {
                            type = NavType.IntType
                        }
                    )
                ) { navBackStackEntry ->
                    val watchId =
                        navBackStackEntry.arguments?.getInt(WATCH_ID_NAV_ARGUMENT)!!
                    WatchDetailScreen(
                        id = watchId,
                        scrollState = watchDetailScrollState,
                        watchRepository = watchRepository
                    )
                }
            }
        }
    }
}

/**
 * Used for the 'in' comparisons in the Scaffold section for determining which screen is visible
 * and what to show (vignette and position indicator). Note, 'in' is translated to the operator
 * 'contains' fun in Kotlin.
 */
operator fun String?.contains(substring: String): Boolean {
    return if (this is String) {
        // Need to convert to CharSequence, otherwise keeps calling this 'contains()' in an endless
        // loop.
        val charSequence: CharSequence = this
        charSequence.contains(substring)
    } else {
        false
    }
}
