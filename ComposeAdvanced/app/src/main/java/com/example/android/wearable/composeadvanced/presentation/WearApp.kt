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
package com.example.android.wearable.composeadvanced.presentation

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.VignettePosition
import com.example.android.wearable.composeadvanced.R
import com.example.android.wearable.composeadvanced.data.WatchRepository
import com.example.android.wearable.composeadvanced.presentation.components.CustomTimeText
import com.example.android.wearable.composeadvanced.presentation.navigation.Screen
import com.example.android.wearable.composeadvanced.presentation.navigation.WATCH_ID_NAV_ARGUMENT
import com.example.android.wearable.composeadvanced.presentation.theme.WearAppTheme
import com.example.android.wearable.composeadvanced.presentation.ui.landing.LandingScreen
import com.example.android.wearable.composeadvanced.presentation.ui.userinput.SliderScreen
import com.example.android.wearable.composeadvanced.presentation.ui.userinput.StepperScreen
import com.example.android.wearable.composeadvanced.presentation.ui.userinput.UserInputComponentsScreen
import com.example.android.wearable.composeadvanced.presentation.ui.watch.WatchDetailScreen
import com.example.android.wearable.composeadvanced.presentation.ui.watchlist.WatchListScreen
import com.google.android.horologist.compose.navscaffold.ExperimentalComposeLayoutApi
import com.google.android.horologist.compose.navscaffold.NavScaffoldViewModel
import com.google.android.horologist.compose.navscaffold.WearNavScaffold
import com.google.android.horologist.compose.navscaffold.scalingLazyColumnComposable
import com.google.android.horologist.compose.navscaffold.scrollStateComposable
import com.google.android.horologist.compose.navscaffold.wearNavComposable

@OptIn(ExperimentalComposeLayoutApi::class)
@Composable
fun WearApp(
    navController: NavHostController,
    watchRepository: WatchRepository,
) {
    WearAppTheme {
        // Allows user to disable the text before the time.
        var showProceedingTextBeforeTime by rememberSaveable { mutableStateOf(false) }

        // Allows user to show/hide the vignette on appropriate screens.
        // IMPORTANT NOTE: Usually you want to show the vignette all the time on screens with
        // scrolling content, a rolling side button, or a rotating bezel. This preference is just
        // to visually demonstrate the vignette for the developer to see it on and off.
        var vignetteVisiblePreference by rememberSaveable { mutableStateOf(true) }

        // TODO: consider moving to ViewModel
        // Display value is passed down to various user input screens, for the slider and stepper
        // components specifically, to demonstrate how they work.
        val defaultSelectedValue = 2
        var displayValueForUserInput by remember { mutableStateOf(defaultSelectedValue) }

        WearNavScaffold(
            startDestination = Screen.Landing.route,
            navController = navController,
            // Scaffold places time at top of screen to follow Material Design guidelines.
            // (Time is hidden while scrolling.)
            timeText = {
                // TODO investigate this bug, without key,
                // the prefix doesn't show
                key(showProceedingTextBeforeTime) {
                    CustomTimeText(
                        modifier = it,
                        showLeadingText = showProceedingTextBeforeTime,
                        leadingText = stringResource(R.string.leading_time_text)
                    )
                }
            },
        ) {
            // Main Window
            wearNavComposable(Screen.Landing.route) { _, _ ->
                LandingScreen(
                    onClickWatchList = {
                        navController.navigate(Screen.WatchList.route)
                    },
                    onClickDemoUserInputComponents = {
                        navController.navigate(Screen.UserInputComponents.route)
                    },
                    proceedingTimeTextEnabled = showProceedingTextBeforeTime,
                    onClickProceedingTimeText = {
                        showProceedingTextBeforeTime = !showProceedingTextBeforeTime
                    }
                )
            }

            wearNavComposable(Screen.UserInputComponents.route) { _, _ ->
                UserInputComponentsScreen(
                    value = displayValueForUserInput,
                    onClickStepper = {
                        navController.navigate(Screen.Stepper.route)
                    },
                    onClickSlider = {
                        navController.navigate(Screen.Slider.route)
                    }
                )
            }

            wearNavComposable(Screen.Stepper.route) { _, _ ->
                StepperScreen(
                    displayValue = displayValueForUserInput,
                    onValueChange = {
                        displayValueForUserInput = it
                    }
                )
            }

            wearNavComposable(Screen.Slider.route) { _, _ ->
                SliderScreen(
                    displayValue = displayValueForUserInput,
                    onValueChange = {
                        displayValueForUserInput = it
                    }
                )
            }

            scalingLazyColumnComposable(
                route = Screen.WatchList.route,
                scrollStateBuilder = { ScalingLazyListState() }
            ) {
                it.viewModel.vignettePosition = if (vignetteVisiblePreference) {
                    NavScaffoldViewModel.VignetteMode.On(VignettePosition.TopAndBottom)
                } else {
                    NavScaffoldViewModel.VignetteMode.Off
                }

                WatchListScreen(
                    scalingLazyListState = it.scrollableState,
                    focusRequester = it.viewModel.focusRequester,
                    showVignette = vignetteVisiblePreference,
                    onClickVignetteToggle = { showVignette ->
                        vignetteVisiblePreference = showVignette
                    },
                    onClickWatch = { id ->
                        navController.navigate(
                            route = Screen.WatchDetail.route + "/" + id,
                        )
                    },
                    watchRepository = watchRepository
                )
            }

            scrollStateComposable(
                route = Screen.WatchDetail.route + "/{$WATCH_ID_NAV_ARGUMENT}",
                arguments = listOf(
                    navArgument(WATCH_ID_NAV_ARGUMENT) {
                        type = NavType.IntType
                    },
                ),
                scrollStateBuilder = { ScrollState(0) }
            ) {
                WatchDetailScreen(
                    scrollState = it.scrollableState,
                    focusRequester = it.viewModel.focusRequester,
                    watchRepository = watchRepository
                )
            }
        }
    }
}
