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
package com.example.android.wearable.speaker

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.ConfirmationDialog
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.audio.ui.VolumeViewModel
import com.google.android.horologist.audio.ui.material3.VolumeScreen
import com.google.android.horologist.compose.material.AlertContent
import kotlinx.coroutines.launch

/**
 * The logic for the speaker sample.
 *
 * The stateful logic is kept by a [MainState].
 */
@OptIn(ExperimentalHorologistApi::class)
@Composable
fun SpeakerApp() {
    MaterialTheme {
        lateinit var requestPermissionLauncher: ManagedActivityResultLauncher<String, Boolean>

        val context = LocalContext.current
        val activity = context.findActivity()
        val scope = rememberCoroutineScope()

        val volumeViewModel: VolumeViewModel = viewModel(factory = VolumeViewModel.Factory)

        val navController = rememberSwipeDismissableNavController()

        val mainState = remember(activity) {
            MainState(
                activity = activity,
                requestPermission = {
                    requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            )
        }

        requestPermissionLauncher = rememberLauncherForActivityResult(RequestPermission()) {
            // We ignore the direct result here, since we're going to check anyway.
            scope.launch {
                mainState.permissionResultReturned()
            }
        }

        val lifecycleOwner = LocalLifecycleOwner.current
        AppScaffold {
            // Notify the state holder whenever we become stopped to reset the state
            DisposableEffect(mainState, scope, lifecycleOwner) {
                val lifecycleObserver = object : DefaultLifecycleObserver {
                    override fun onStop(owner: LifecycleOwner) {
                        super.onStop(owner)
                        scope.launch { mainState.onStopped() }
                    }
                }

                lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
                }
            }
            SwipeDismissableNavHost(navController = navController, startDestination = "speaker") {
                composable("speaker") {
                    SpeakerRecordingScreen(
                        playbackState = mainState.playbackState,
                        isPermissionDenied = mainState.isPermissionDenied,
                        recordingProgress = mainState.recordingProgress,
                        onMicClicked = {
                            scope.launch {
                                mainState.onMicClicked()
                            }
                        },
                        onPlayClicked = {
                            navController.navigate("player")
                        }
                    )

                    if (mainState.showPermissionRationale) {
                        AlertContent(
                            onOk = {
                                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                mainState.showPermissionRationale = false
                            },
                            onCancel = {
                                mainState.showPermissionRationale = false
                            },
                            title = stringResource(
                                id = R.string.rationale_for_microphone_permission
                            )
                        )
                    }

                    ConfirmationDialog(
                        visible = mainState.showSpeakerNotSupported,
                        onDismissRequest = { mainState.showSpeakerNotSupported = false },
                        curvedText = null
                    ) {
                        Text(text = stringResource(id = R.string.no_speaker_supported))
                    }
                }
                composable("player") {
                    SpeakerPlayerScreen(
                        volumeViewModel = volumeViewModel,
                        onVolumeClick = { navController.navigate("volume") }
                    )
                }
                composable("volume") {
                    ScreenScaffold {
                        VolumeScreen(volumeViewModel = volumeViewModel)
                    }
                }
            }
        }
    }
}

/**
 * Find the closest Activity in a given Context.
 */
tailrec fun Context.findActivity(): Activity =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> throw IllegalStateException(
            "findActivity should be called in the context of an Activity"
        )
    }
