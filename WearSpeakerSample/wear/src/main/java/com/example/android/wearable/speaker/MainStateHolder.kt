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
import android.app.Application
import android.content.pm.PackageManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A state holder driving the logic of [MainActivity].
 */
class MainStateHolder(
    private val application: Application,
    private val requestPermission: () -> Unit,
    private val showPermissionRationale: () -> Unit,
    private val showSpeakerNotSupported: () -> Unit
) {

    /**
     * The backing [MutableStateFlow] for the [appState].
     */
    private val appStateMutableStateFlow = MutableStateFlow<AppState>(AppState.Ready(transitionInstantly = true))

    /**
     * A [StateFlow] of the primary app state.
     */
    val appState = appStateMutableStateFlow.asStateFlow()

    /**
     * The backing [MutableStateFlow] for the [isPermissionDenied] flow.
     */
    private val isPermissionDeniedMutableStateFlow = MutableStateFlow(false)

    /**
     * A [StateFlow] of whether we know the user has denied the record audio permission.
     */
    val isPermissionDenied = isPermissionDeniedMutableStateFlow.asStateFlow()

    /**
     * One of the actions the app interprets. This can either be direct user actions (clicking one of the buttons),
     * or programmatic actions (finishing playback or recording).
     */
    sealed class AppAction {
        object ViewStarted : AppAction()
        data class MicClicked(
            val shouldShowRationale: Boolean,
        ) : AppAction()
        object PlayClicked : AppAction()
        object MusicClicked : AppAction()
        object RecordingFinished : AppAction()
        object PlayRecordingFinished : AppAction()
        object PlayMusicFinished : AppAction()
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

    /**
     * The primary state machine for the app, determining the new [AppState] based on the incoming [AppAction],
     * updating other state and sending out events as appropriate.
     */
    fun onAction(action: AppAction) {
        val oldState = appState.value
        val newState = when (action) {
            AppAction.ViewStarted -> AppState.Ready(transitionInstantly = true)
            is AppAction.MicClicked ->
                when (oldState) {
                    is AppState.Ready,
                    AppState.PlayingVoice,
                    AppState.PlayingMusic ->
                        when {
                            ContextCompat.checkSelfPermission(application, Manifest.permission.RECORD_AUDIO) ==
                                PackageManager.PERMISSION_GRANTED -> {
                                // We have the permission, we can start recording now
                                AppState.Recording
                            }
                            action.shouldShowRationale -> {
                                // If we should show the rationale prior to requesting the permission, send that event
                                showPermissionRationale()
                                AppState.Ready(transitionInstantly = false)
                            }
                            else -> {
                                // Request the permission
                                requestPermission()
                                AppState.Ready(transitionInstantly = false)
                            }
                        }
                    // If we were already recording, transition back to ready
                    AppState.Recording -> AppState.Ready(transitionInstantly = false)
                }
            AppAction.MusicClicked ->
                when (oldState) {
                    is AppState.Ready,
                    AppState.PlayingVoice,
                    AppState.Recording ->
                        if (speakerIsSupported()) {
                            AppState.PlayingMusic
                        } else {
                            showSpeakerNotSupported()
                            AppState.Ready(transitionInstantly = false)
                        }
                    // If we were already playing, transition back to ready
                    AppState.PlayingMusic -> AppState.Ready(transitionInstantly = false)
                }
            AppAction.PlayClicked ->
                when (oldState) {
                    is AppState.Ready,
                    AppState.PlayingMusic,
                    AppState.Recording -> {
                        if (speakerIsSupported()) {
                            AppState.PlayingVoice
                        } else {
                            showSpeakerNotSupported()
                            AppState.Ready(transitionInstantly = false)
                        }
                    }
                    // If we were already playing, transition back to ready
                    AppState.PlayingVoice -> AppState.Ready(transitionInstantly = false)
                }
            // We finished doing something, transition back to ready
            AppAction.PlayMusicFinished,
            AppAction.PlayRecordingFinished,
            AppAction.RecordingFinished -> AppState.Ready(transitionInstantly = false)
            AppAction.PermissionResultReturned ->
                // Check if the user granted the permission
                if (
                    ContextCompat.checkSelfPermission(application, Manifest.permission.RECORD_AUDIO) ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    // The user granted the permission, continue on to start recording
                    AppState.Recording
                } else {
                    // We have confirmation now that the user denied the permission
                    isPermissionDeniedMutableStateFlow.value = true
                    AppState.Ready(transitionInstantly = false)
                }
        }
        appStateMutableStateFlow.value = newState
    }

    /**
     * Determines if the device has a way to output audio and if it is supported.
     *
     * This could be an on-device speaker, or a connected bluetooth device.
     */
    private fun speakerIsSupported(): Boolean {
        val hasAudioOutputFeature =
            application.packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT)
        val devices = application.getSystemService<AudioManager>()!!
            .getDevices(AudioManager.GET_DEVICES_OUTPUTS)

        // We can only trust AudioDeviceInfo.TYPE_BUILTIN_SPEAKER if the device advertises FEATURE_AUDIO_OUTPUT
        return devices.any { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER } && hasAudioOutputFeature ||
            devices.any { it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP }
    }
}
