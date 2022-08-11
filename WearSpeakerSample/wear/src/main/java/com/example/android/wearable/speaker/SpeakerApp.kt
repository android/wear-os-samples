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
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Confirmation
import kotlinx.coroutines.launch

/**
 * The logic for the speaker sample.
 *
 * The stateful logic is kept by a [MainState].
 */
@Composable
fun SpeakerApp() {
    MaterialTheme {
        lateinit var requestPermissionLauncher: ManagedActivityResultLauncher<String, Boolean>

        val context = LocalContext.current
        val activity = context.findActivity()
        val scope = rememberCoroutineScope()

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

        SpeakerScreen(
            playbackState = mainState.playbackState,
            isPermissionDenied = mainState.isPermissionDenied,
            recordingProgress = mainState.recordingProgress,
            onMicClicked = {
                scope.launch {
                    mainState.onMicClicked()
                }
            },
            onPlayClicked = {
                scope.launch {
                    mainState.onPlayClicked()
                }
            },
            onMusicClicked = {
                scope.launch {
                    mainState.onMusicClicked()
                }
            }
        )

        if (mainState.showPermissionRationale) {
            Alert(
                title = {
                    Text(text = stringResource(id = R.string.rationale_for_microphone_permission))
                },
                positiveButton = {
                    Button(
                        onClick = {
                            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            mainState.showPermissionRationale = false
                        }
                    ) {
                        Text(text = stringResource(id = R.string.ok))
                    }
                },
                negativeButton = {
                    Button(
                        onClick = {
                            mainState.showPermissionRationale = false
                        }
                    ) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                }
            )
        }

        if (mainState.showSpeakerNotSupported) {
            Confirmation(
                onTimeout = { mainState.showSpeakerNotSupported = false }
            ) {
                Text(text = stringResource(id = R.string.no_speaker_supported))
            }
        }
    }
}

/**
 * Find the closest Activity in a given Context.
 */
private tailrec fun Context.findActivity(): Activity =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> throw IllegalStateException(
            "findActivity should be called in the context of an Activity"
        )
    }
