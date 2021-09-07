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
import android.content.pm.PackageManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.MutatorMutex
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.Duration
import kotlin.coroutines.resume

/**
 * A state holder driving the logic of the app.
 */
class MainStateHolder(
    private val activity: Activity,
    private val requestPermission: () -> Unit,
    private val showPermissionRationale: () -> Unit,
    private val showSpeakerNotSupported: () -> Unit
) {
    /**
     * The [MutatorMutex] that guards the state of the app.
     *
     * Due to being a [MutatorMutex], this automatically handles cleanup of any ongoing asynchronous work,
     * like playing music or recording, ensuring that only one operation is occurring at a time.
     */
    private val appStateMutatorMutex = MutatorMutex()

    /**
     * The primary app state.
     */
    var appState by mutableStateOf<AppState>(AppState.Ready(transitionInstantly = true))
        private set

    /**
     * The progress of an ongoing recording.
     */
    var recordingProgress by mutableStateOf(0f)
        private set

    /**
     * `true` if we know the user has denied the record audio permission.
     */
    var isPermissionDenied by mutableStateOf(false)
        private set

    /**
     * The [SoundRecorder] for recording and playing audio captured on-device.
     */
    private val soundRecorder = SoundRecorder(activity, "audiorecord.pcm")

    /**
     * The primary state machine for the app, determining the new [AppState] based on the incoming [AppAction],
     * updating other state and sending out events as appropriate.
     */
    suspend fun onAction(action: AppAction) {
        appStateMutatorMutex.mutate {
            val oldState = appState
            when (action) {
                AppAction.Started -> {
                    // Upon the view starting, simply reset the state to ready
                    appState = AppState.Ready(transitionInstantly = true)
                }
                is AppAction.MicClicked ->
                    when (oldState) {
                        is AppState.Ready,
                        AppState.PlayingVoice,
                        AppState.PlayingMusic ->
                            // If we weren't recording, check our permission to start recording.
                            when {
                                ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) ==
                                    PackageManager.PERMISSION_GRANTED -> {
                                    // We have the permission, we can start recording now
                                    recordUpdatingState()
                                }
                                activity.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                                    // If we should show the rationale prior to requesting the permission, send that
                                    // event
                                    showPermissionRationale()
                                    appState = AppState.Ready(transitionInstantly = false)
                                }
                                else -> {
                                    // Request the permission
                                    requestPermission()
                                    appState = AppState.Ready(transitionInstantly = false)
                                }
                            }
                        // If we were already recording, transition back to ready
                        AppState.Recording -> {
                            appState = AppState.Ready(transitionInstantly = false)
                        }
                    }
                AppAction.MusicClicked ->
                    when (oldState) {
                        is AppState.Ready,
                        AppState.PlayingVoice,
                        AppState.Recording ->
                            if (speakerIsSupported()) {
                                playMusicUpdatingState()
                            } else {
                                showSpeakerNotSupported()
                                appState = AppState.Ready(transitionInstantly = false)
                            }
                        // If we were already playing, transition back to ready
                        AppState.PlayingMusic -> {
                            appState = AppState.Ready(transitionInstantly = false)
                        }
                    }
                AppAction.PlayClicked ->
                    when (oldState) {
                        is AppState.Ready,
                        AppState.PlayingMusic,
                        AppState.Recording -> {
                            if (speakerIsSupported()) {
                                appState = AppState.PlayingVoice

                                soundRecorder.play()

                                appState = AppState.Ready(transitionInstantly = false)
                            } else {
                                showSpeakerNotSupported()
                                appState = AppState.Ready(transitionInstantly = false)
                            }
                        }
                        // If we were already playing, transition back to ready
                        AppState.PlayingVoice -> {
                            appState = AppState.Ready(transitionInstantly = false)
                        }
                    }
                AppAction.PermissionResultReturned ->
                    // Check if the user granted the permission
                    if (
                        ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) ==
                        PackageManager.PERMISSION_GRANTED
                    ) {
                        // The user granted the permission, continue on to start recording
                        recordUpdatingState()
                    } else {
                        // We have confirmation now that the user denied the permission
                        isPermissionDenied = true
                        appState = AppState.Ready(transitionInstantly = false)
                    }
            }
        }
    }

    /**
     * A helper function to record, updating the progress state while recording.
     *
     * This requires the [Manifest.permission.RECORD_AUDIO] permission to run.
     */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private suspend fun recordUpdatingState() {
        // We have the permission, we can start recording now
        appState = AppState.Recording

        coroutineScope {
            // Kick off a parallel job to record
            val recordingJob = launch { soundRecorder.record() }

            val delayPerTickMs = MAX_RECORDING_DURATION.toMillis() / NUMBER_TICKS
            val startTime = System.currentTimeMillis()

            repeat(NUMBER_TICKS) { index ->
                recordingProgress = index.toFloat() / NUMBER_TICKS
                delay(startTime + delayPerTickMs * (index + 1) - System.currentTimeMillis())
            }
            // Update the progress to be complete
            recordingProgress = 1f

            // Stop recording
            recordingJob.cancel()
        }

        appState = AppState.Ready(transitionInstantly = false)
    }

    /**
     * Plays the MP3 file embedded in the application, updating the state to reflect the playing.
     */
    private suspend fun playMusicUpdatingState() {
        appState = AppState.PlayingMusic

        val mediaPlayer = MediaPlayer.create(activity, R.raw.sound)

        try {
            suspendCancellableCoroutine<Unit> { cont ->
                mediaPlayer.setOnCompletionListener {
                    cont.resume(Unit)
                }
                mediaPlayer.start()
            }
        } finally {
            mediaPlayer.stop()
            mediaPlayer.release()
        }

        appState = AppState.Ready(transitionInstantly = false)
    }

    /**
     * Determines if the device has a way to output audio and if it is supported.
     *
     * This could be an on-device speaker, or a connected bluetooth device.
     */
    private fun speakerIsSupported(): Boolean {
        val hasAudioOutputFeature =
            activity.packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT)
        val devices = activity.getSystemService<AudioManager>()!!
            .getDevices(AudioManager.GET_DEVICES_OUTPUTS)

        // We can only trust AudioDeviceInfo.TYPE_BUILTIN_SPEAKER if the device advertises FEATURE_AUDIO_OUTPUT
        return devices.any { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER } && hasAudioOutputFeature ||
            devices.any { it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP }
    }

    companion object {
        private val MAX_RECORDING_DURATION = Duration.ofSeconds(10)
        private const val NUMBER_TICKS = 10
    }
}

/**
 * One of the actions the app interprets. This can either be direct user actions (clicking one of the buttons),
 * or programmatic actions (finishing playback or recording).
 */
sealed class AppAction {
    object Started : AppAction()
    object MicClicked : AppAction()
    object PlayClicked : AppAction()
    object MusicClicked : AppAction()
    object PermissionResultReturned : AppAction()
}

/**
 * The four "resting" states of the application, corresponding to the 4 constraint sets of the [MotionLayout].
 */
sealed class AppState {
    data class Ready(
        /**
         * If true, don't animate to this position
         */
        val transitionInstantly: Boolean,
    ) : AppState()

    object PlayingVoice : AppState()
    object PlayingMusic : AppState()
    object Recording : AppState()
}
