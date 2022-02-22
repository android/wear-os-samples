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
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.currentBackStackEntryAsState
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.android.wearable.composeadvanced.R
import com.example.android.wearable.composeadvanced.data.WatchRepository
import com.example.android.wearable.composeadvanced.presentation.components.CustomTimeText
import com.example.android.wearable.composeadvanced.presentation.components.CustomVignette
import com.example.android.wearable.composeadvanced.presentation.navigation.DestinationScrollType
import com.example.android.wearable.composeadvanced.presentation.navigation.SCROLL_TYPE_NAV_ARGUMENT
import com.example.android.wearable.composeadvanced.presentation.navigation.Screen
import com.example.android.wearable.composeadvanced.presentation.navigation.WATCH_ID_NAV_ARGUMENT
import com.example.android.wearable.composeadvanced.presentation.theme.WearAppTheme
import com.example.android.wearable.composeadvanced.presentation.ui.ScalingLazyListStateViewModel
import com.example.android.wearable.composeadvanced.presentation.ui.ScrollStateViewModel
import com.example.android.wearable.composeadvanced.presentation.ui.landing.LandingScreen
import com.example.android.wearable.composeadvanced.presentation.ui.userinput.SliderScreen
import com.example.android.wearable.composeadvanced.presentation.ui.userinput.StepperScreen
import com.example.android.wearable.composeadvanced.presentation.ui.userinput.UserInputComponentsScreen
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

@Composable
fun WearApp(watchRepository: WatchRepository) {
    WearAppTheme {
        // Navigation controller for navigating the screens.
        val swipeDismissableNavController = rememberSwipeDismissableNavController()

        // Allows user to disable the text before the time.
        var showProceedingTextBeforeTime by rememberSaveable { mutableStateOf(false) }

        // Allows user to show/hide the vignette on appropriate screens.
        // IMPORTANT NOTE: Usually you want to show the vignette all the time on screens with
        // scrolling content, a rolling side button, or a rotating bezel. This preference is just
        // to visually demonstrate the vignette for the developer to see it on and off.
        var vignetteVisiblePreference by rememberSaveable { mutableStateOf(true) }

        // Observes the current back stack entry to pull information and determine if the screen
        // is scrollable and the scrollable state.
        //
        // The main reason the state for any scrollable screen is hoisted to this level is so the
        // Scaffold can properly place the position indicator (also known as the scroll indicator).
        //
        // We save the above scrollable states in the SavedStateHandle and retrieve them
        // when needed from custom view models (see ScrollingViewModels class).
        //
        // Screens with scrollable content:
        //  1. The watch list screen uses ScalingLazyColumn (backed by ScalingLazyListState)
        //  2. The watch detail screens uses Column with scrolling enabled (backed by ScrollState).
        //
        // We also use these scrolling states for various other things (like hiding the time
        // when the user is scrolling and only showing the vignette when the screen is
        // scrollable).
        //
        // Remember, mobile guidelines specify that if you back navigate out of a screen and then
        // later navigate into it again, it should be in its initial scroll state (not the last
        // scroll location it was in before you backed out).
        val currentBackStackEntry by swipeDismissableNavController.currentBackStackEntryAsState()

        val scrollType =
            currentBackStackEntry?.arguments?.getSerializable(SCROLL_TYPE_NAV_ARGUMENT)
                ?: DestinationScrollType.NONE

        // TODO: consider moving to ViewModel
        // Display value is passed down to various user input screens, for the slider and stepper
        // components specifically, to demonstrate how they work.
        val defaultSelectedValue = 2
        var displayValueForUserInput by remember { mutableStateOf(defaultSelectedValue) }

        Scaffold(
            timeText = {
                // Scaffold places time at top of screen to follow Material Design guidelines.
                // (Time is hidden while scrolling.)
                val activelyScrolling =
                    when (scrollType) {
                        DestinationScrollType.SCALING_LAZY_COLUMN_SCROLLING -> {
                            val viewModel: ScalingLazyListStateViewModel =
                                viewModel(currentBackStackEntry!!)
                            viewModel.scrollState.isScrollInProgress
                        }
                        DestinationScrollType.COLUMN_SCROLLING -> {
                            val viewModel: ScrollStateViewModel =
                                viewModel(currentBackStackEntry!!)
                            viewModel.scrollState.isScrollInProgress
                        }
                        else -> {
                            false
                        }
                    }

                CustomTimeText(
                    visible = !activelyScrolling,
                    showLeadingText = showProceedingTextBeforeTime,
                    leadingText = stringResource(R.string.leading_time_text)
                )
            },
            vignette = {
                // Only show vignette for screens with scrollable content.
                if (scrollType == DestinationScrollType.SCALING_LAZY_COLUMN_SCROLLING ||
                    scrollType == DestinationScrollType.COLUMN_SCROLLING
                ) {
                    CustomVignette(
                        visible = vignetteVisiblePreference,
                        vignettePosition = VignettePosition.TopAndBottom
                    )
                }
            },
            positionIndicator = {
                // Only displays the position indicator for scrollable content.
                when (scrollType) {
                    DestinationScrollType.SCALING_LAZY_COLUMN_SCROLLING -> {
                        // Get or create the ViewModel associated with the current back stack entry
                        val viewModel: ScalingLazyListStateViewModel =
                            viewModel(currentBackStackEntry!!)
                        PositionIndicator(scalingLazyListState = viewModel.scrollState)
                    }
                    DestinationScrollType.COLUMN_SCROLLING -> {
                        // Get or create the ViewModel associated with the current back stack entry
                        val viewModel: ScrollStateViewModel = viewModel(currentBackStackEntry!!)
                        PositionIndicator(scrollState = viewModel.scrollState)
                    }
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
                        onClickDemoUserInputComponents = {
                            swipeDismissableNavController.navigate(Screen.UserInputComponents.route)
                        },
                        proceedingTimeTextEnabled = showProceedingTextBeforeTime,
                        onClickProceedingTimeText = {
                            showProceedingTextBeforeTime = !showProceedingTextBeforeTime
                        }
                    )
                }

                composable(Screen.UserInputComponents.route) {
                    UserInputComponentsScreen(
                        value = displayValueForUserInput,
                        onClickStepper = {
                            swipeDismissableNavController.navigate(Screen.Stepper.route)
                        },
                        onClickSlider = {
                            swipeDismissableNavController.navigate(Screen.Slider.route)
                        }
                    )
                }

                composable(Screen.Stepper.route) {
                    StepperScreen(
                        displayValue = displayValueForUserInput,
                        onValueChange = {
                            displayValueForUserInput = it
                        }
                    )
                }

                composable(Screen.Slider.route) {
                    SliderScreen(
                        displayValue = displayValueForUserInput,
                        onValueChange = {
                            displayValueForUserInput = it
                        }
                    )
                }

                composable(
                    route = Screen.WatchList.route,
                    arguments = listOf(
                        // In this case, the argument isn't part of the route, it's just attached
                        // as information for the destination.
                        navArgument(SCROLL_TYPE_NAV_ARGUMENT) {
                            type = NavType.EnumType(DestinationScrollType::class.java)
                            defaultValue = DestinationScrollType.SCALING_LAZY_COLUMN_SCROLLING
                        }
                    )
                ) {
                    val passedScrollType = it.arguments?.getSerializable(SCROLL_TYPE_NAV_ARGUMENT)

                    check(
                        passedScrollType == DestinationScrollType.SCALING_LAZY_COLUMN_SCROLLING
                    ) {
                        "Scroll type must be DestinationScrollType.SCALING_LAZY_COLUMN_SCROLLING"
                    }

                    val viewModel: ScalingLazyListStateViewModel = viewModel(it)

                    WatchListScreen(
                        scalingLazyListState = viewModel.scrollState,
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
                        },
                        // In this case, the argument isn't part of the route, it's just attached
                        // as information for the destination.
                        navArgument(SCROLL_TYPE_NAV_ARGUMENT) {
                            type = NavType.EnumType(DestinationScrollType::class.java)
                            defaultValue = DestinationScrollType.COLUMN_SCROLLING
                        }
                    )
                ) {
                    val watchId = it.arguments?.getInt(WATCH_ID_NAV_ARGUMENT)!!
                    val passedScrollType = it.arguments?.getSerializable(SCROLL_TYPE_NAV_ARGUMENT)

                    check(passedScrollType == DestinationScrollType.COLUMN_SCROLLING) {
                        "Scroll type must be DestinationScrollType.COLUMN_SCROLLING"
                    }

                    val viewModel: ScrollStateViewModel = viewModel(it)

                    WatchDetailScreen(
                        id = watchId,
                        scrollState = viewModel.scrollState,
                        watchRepository = watchRepository
                    )
                }
            }
        }
    }
}
