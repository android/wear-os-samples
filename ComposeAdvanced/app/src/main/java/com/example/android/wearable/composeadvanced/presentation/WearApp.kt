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

import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.activity
import androidx.navigation.navArgument
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.android.wearable.composeadvanced.R
import com.example.android.wearable.composeadvanced.presentation.components.CustomTimeText
import com.example.android.wearable.composeadvanced.presentation.navigation.Screen
import com.example.android.wearable.composeadvanced.presentation.navigation.WATCH_ID_NAV_ARGUMENT
import com.example.android.wearable.composeadvanced.presentation.theme.WearAppTheme
import com.example.android.wearable.composeadvanced.presentation.theme.initialThemeValues
import com.example.android.wearable.composeadvanced.presentation.theme.themeValues
import com.example.android.wearable.composeadvanced.presentation.ui.dialog.Dialogs
import com.example.android.wearable.composeadvanced.presentation.ui.landing.LandingScreen
import com.example.android.wearable.composeadvanced.presentation.ui.map.MapActivity
import com.example.android.wearable.composeadvanced.presentation.ui.progressindicator.FullScreenProgressIndicator
import com.example.android.wearable.composeadvanced.presentation.ui.progressindicator.IndeterminateProgressIndicator
import com.example.android.wearable.composeadvanced.presentation.ui.progressindicator.ProgressIndicatorsScreen
import com.example.android.wearable.composeadvanced.presentation.ui.theme.ThemeScreen
import com.example.android.wearable.composeadvanced.presentation.ui.userinput.SliderScreen
import com.example.android.wearable.composeadvanced.presentation.ui.userinput.StepperScreen
import com.example.android.wearable.composeadvanced.presentation.ui.userinput.UserInputComponentsScreen
import com.example.android.wearable.composeadvanced.presentation.ui.watch.WatchDetailScreen
import com.example.android.wearable.composeadvanced.presentation.ui.watchlist.WatchListScreen
import com.google.android.horologist.composables.DatePicker
import com.google.android.horologist.composables.TimePicker
import com.google.android.horologist.composables.TimePickerWith12HourClock
import com.google.android.horologist.compose.layout.AppScaffold
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberColumnState
import java.time.LocalDateTime

@Composable
fun WearApp(
    modifier: Modifier = Modifier,
    swipeDismissableNavController: NavHostController = rememberSwipeDismissableNavController()
) {
    var themeColors by remember { mutableStateOf(initialThemeValues.colors) }
    WearAppTheme(colors = themeColors) {
        // Allows user to disable the text before the time.
        var showProceedingTextBeforeTime by rememberSaveable { mutableStateOf(false) }

        // Display value is passed down to various user input screens, for the slider and stepper
        // components specifically, to demonstrate how they work.
        var displayValueForUserInput by remember { mutableIntStateOf(5) }
        var dateTimeForUserInput by remember { mutableStateOf(LocalDateTime.now()) }

        AppScaffold(timeText = {
            CustomTimeText(
                startText = if (showProceedingTextBeforeTime) {
                    stringResource(R.string.leading_time_text)
                } else {
                    null
                }
            )
        }) {
            SwipeDismissableNavHost(
                modifier = modifier,
                navController = swipeDismissableNavController,
                startDestination = Screen.Landing.route

            ) {
                // Main Window
                composable(
                    route = Screen.Landing.route
                ) {
                    val columnState = rememberColumnState()

                    ScreenScaffold(scrollState = columnState) {
                        LandingScreen(
                            columnState = columnState,
                            onClickWatchList = {
                                swipeDismissableNavController.navigate(Screen.WatchList.route)
                            },
                            proceedingTimeTextEnabled = showProceedingTextBeforeTime,
                            onClickProceedingTimeText = {
                                showProceedingTextBeforeTime = !showProceedingTextBeforeTime
                            },
                            onNavigate = { swipeDismissableNavController.navigate(it) }
                        )
                    }
                }

                composable(
                    route = Screen.UserInputComponents.route
                ) {
                    val columnState = rememberColumnState()

                    ScreenScaffold(scrollState = columnState) {
                        UserInputComponentsScreen(
                            columnState = columnState,
                            value = displayValueForUserInput,
                            dateTime = dateTimeForUserInput,
                            onClickStepper = {
                                swipeDismissableNavController.navigate(Screen.Stepper.route)
                            },
                            onClickSlider = {
                                swipeDismissableNavController.navigate(Screen.Slider.route)
                            },
                            onClickDemoDatePicker = {
                                swipeDismissableNavController.navigate(Screen.DatePicker.route)
                            },
                            onClickDemo12hTimePicker = {
                                swipeDismissableNavController.navigate(Screen.Time12hPicker.route)
                            },
                            onClickDemo24hTimePicker = {
                                swipeDismissableNavController.navigate(Screen.Time24hPicker.route)
                            }
                        )
                    }
                }

                composable(route = Screen.Stepper.route) {
                    StepperScreen(displayValue = displayValueForUserInput, onValueChange = {
                        displayValueForUserInput = it
                    })
                }

                composable(route = Screen.Slider.route) {
                    SliderScreen(displayValue = displayValueForUserInput, onValueChange = {
                        displayValueForUserInput = it
                    })
                }

                composable(
                    route = Screen.WatchList.route
                ) {
                    val columnState = rememberColumnState()

                    ScreenScaffold(scrollState = columnState) {
                        var vignetteVisible by rememberSaveable { mutableStateOf(true) }

                        WatchListScreen(
                            columnState = columnState,
                            showVignette = vignetteVisible,
                            onClickVignetteToggle = { showVignette ->
                                vignetteVisible = showVignette
                            },
                            onClickWatch = { id ->
                                swipeDismissableNavController.navigate(
                                    route = Screen.WatchDetail.route + "/" + id
                                )
                            }
                        )

                        if (vignetteVisible) {
                            Vignette(vignettePosition = VignettePosition.TopAndBottom)
                        }
                    }
                }

                composable(
                    route = Screen.WatchDetail.route + "/{$WATCH_ID_NAV_ARGUMENT}",
                    arguments = listOf(
                        navArgument(WATCH_ID_NAV_ARGUMENT) {
                            type = NavType.IntType
                        }
                    )
                ) {
                    val watchId: Int = it.arguments!!.getInt(WATCH_ID_NAV_ARGUMENT)

                    val columnState = rememberScrollState()

                    ScreenScaffold(scrollState = columnState) {
                        WatchDetailScreen(
                            watchId = watchId,
                            scrollState = columnState
                        )
                    }
                }

                composable(Screen.DatePicker.route) {
                    DatePicker(
                        onDateConfirm = {
                            swipeDismissableNavController.popBackStack()
                            dateTimeForUserInput = it.atTime(dateTimeForUserInput.toLocalTime())
                        },
                        date = dateTimeForUserInput.toLocalDate()
                    )
                }

                composable(Screen.Time24hPicker.route) {
                    TimePicker(
                        onTimeConfirm = {
                            swipeDismissableNavController.popBackStack()
                            dateTimeForUserInput = it.atDate(dateTimeForUserInput.toLocalDate())
                        },
                        time = dateTimeForUserInput.toLocalTime()
                    )
                }

                composable(Screen.Time12hPicker.route) {
                    TimePickerWith12HourClock(
                        onTimeConfirm = {
                            swipeDismissableNavController.popBackStack()
                            dateTimeForUserInput = it.atDate(dateTimeForUserInput.toLocalDate())
                        },
                        time = dateTimeForUserInput.toLocalTime()
                    )
                }

                composable(Screen.Dialogs.route) {
                    Dialogs()
                }

                composable(
                    route = Screen.ProgressIndicators.route
                ) {
                    val columnState = rememberColumnState()

                    ScreenScaffold(scrollState = columnState) {
                        ProgressIndicatorsScreen(
                            columnState = columnState,
                            onNavigate = { swipeDismissableNavController.navigate(it) }
                        )
                    }
                }

                composable(Screen.IndeterminateProgressIndicator.route) {
                    IndeterminateProgressIndicator()
                }

                composable(
                    route = Screen.FullScreenProgressIndicator.route
                ) {
                    FullScreenProgressIndicator()
                }

                composable(
                    route = Screen.Theme.route
                ) {
                    val columnState = rememberColumnState()

                    ScreenScaffold(scrollState = columnState) {}
                    ThemeScreen(
                        columnState = columnState,
                        currentlySelectedColors = themeColors,
                        availableThemes = themeValues
                    ) { colors -> themeColors = colors }
                }

                activity(
                    route = Screen.Map.route
                ) {
                    this.activityClass = MapActivity::class
                }
            }
        }
    }
}

@Composable
internal fun menuNameAndCallback(
    onNavigate: (String) -> Unit,
    menuNameResource: Int,
    screen: Screen
) = MenuItem(stringResource(menuNameResource)) { onNavigate(screen.route) }

data class MenuItem(val name: String, val clickHander: () -> Unit)
