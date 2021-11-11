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
import android.app.AlertDialog
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.wear.compose.material.MaterialTheme
import kotlinx.coroutines.awaitCancellation
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
                },
                showPermissionRationale = {
                    // TODO: Refactor away from normal AlertDialog to a Compose-specific dialog
                    AlertDialog.Builder(activity)
                        .setMessage(R.string.rationale_for_microphone_permission)
                        .setPositiveButton(R.string.ok) { _, _ ->
                            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                        .setNegativeButton(R.string.cancel) { _, _ -> }
                        .create()
                        .show()
                },
                showSpeakerNotSupported = {
                    AlertDialog.Builder(activity)
                        .setMessage(R.string.no_speaker_supported)
                        .setPositiveButton(R.string.ok) { _, _ -> }
                        .create()
                        .show()
                }
            )
        }

        val lifecycleOwner = LocalLifecycleOwner.current

        requestPermissionLauncher = rememberLauncherForActivityResult(RequestPermission()) {
            // We ignore the direct result here, since we're going to check anyway.
            scope.launch {
                mainState.permissionResultReturned()
            }
        }

        // Notify the state holder whenever we become stopped to reset the state
        LaunchedEffect(mainState, scope) {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    awaitCancellation()
                } finally {
                    scope.launch {
                        mainState.onStopped()
                    }
                }
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
            },
        )
    }
}

/**
 * Find the closest Activity in a given Context.
 */
private tailrec fun Context.findActivity(): Activity =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> throw IllegalStateException("findActivity should be called in the context of an Activity")
    }
