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
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.repeatOnLifecycle
import androidx.wear.compose.material.MaterialTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * The logic for the speaker sample.
 *
 * The stateful logic is kept by a [MainStateHolder].
 */
@Composable
fun SpeakerApp() {
    MaterialTheme {
        lateinit var requestPermissionLauncher: ManagedActivityResultLauncher<String, Boolean>

        val context = LocalContext.current
        val activity = context.findActivity()
        val scope = rememberCoroutineScope()

        val stateHolder = remember(activity) {
            MainStateHolder(
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
                raceOf(
                    { stateHolder.onAction(AppAction.PermissionResultReturned) },
                    { lifecycleOwner.lifecycle.awaitEvent(Lifecycle.Event.ON_STOP) }
                )
            }
        }

        // Notify the state holder whenever we become started to reset the state
        LaunchedEffect(stateHolder) {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                stateHolder.onAction(AppAction.Started)
            }
        }

        SpeakerScreen(
            appState = stateHolder.appState,
            isPermissionDenied = stateHolder.isPermissionDenied,
            recordingProgress = stateHolder.recordingProgress,
            onMicClicked = {
                scope.launch {
                    raceOf(
                        { stateHolder.onAction(AppAction.MicClicked) },
                        { lifecycleOwner.lifecycle.awaitEvent(Lifecycle.Event.ON_STOP) }
                    )
                }
            },
            onPlayClicked = {
                scope.launch {
                    raceOf(
                        { stateHolder.onAction(AppAction.PlayClicked) },
                        { lifecycleOwner.lifecycle.awaitEvent(Lifecycle.Event.ON_STOP) }
                    )
                }
            },
            onMusicClicked = {
                scope.launch {
                    raceOf(
                        { stateHolder.onAction(AppAction.MusicClicked) },
                        { lifecycleOwner.lifecycle.awaitEvent(Lifecycle.Event.ON_STOP) }
                    )
                }
            },
        )
    }
}

/**
 * A helper method to "race" coroutines.
 *
 * Each of the racing coroutines is started immediately.
 *
 * The result of whichever coroutines finishes first (in other words, whichever one wins the race) will be returned,
 * and all other racers will be cancelled.
 */
private suspend fun <T> raceOf(vararg racers: suspend CoroutineScope.() -> T): T {
    require(racers.isNotEmpty()) { "Nothing to race!" }
    return coroutineScope {
        select {
            val deferredRacers = racers.map { racer ->
                async(start = CoroutineStart.UNDISPATCHED) { racer() }
            }
            deferredRacers.map { deferred ->
                deferred.onAwait { result ->
                    // Cancel all other racing coroutines
                    deferredRacers.forEach { it.cancel() }
                    result
                }
            }
        }
    }
}

/**
 * A helper method that waits for given lifecycle [event].
 *
 * Note that this method may return immediately for lifecycle up events, if transitioning to the current state requires
 * it.
 */
private suspend fun Lifecycle.awaitEvent(event: Lifecycle.Event?) {
    var observer: LifecycleEventObserver? = null

    try {
        suspendCancellableCoroutine<Unit> { cont ->
            observer = LifecycleEventObserver { _, lifecycleEvent ->
                if (lifecycleEvent == event) {
                    cont.resume(Unit)
                }
            }
            addObserver(observer as LifecycleEventObserver)
        }
    } finally {
        observer?.let { removeObserver(it) }
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
