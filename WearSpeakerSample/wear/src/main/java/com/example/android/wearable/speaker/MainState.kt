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
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.MutatorMutex
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import java.time.Duration
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A state holder driving the logic of the app.
 *
 * This state holder is scoped to the composition (and therefore can be provided an [Activity]) to
 * handle permission-related logic.
 */
class MainState(
    private val activity: Activity,
    private val requestPermission: () -> Unit
) {
    /**
     * The [MutatorMutex] that guards the playback state of the app.
     *
     * Due to being a [MutatorMutex], this automatically handles cleanup of any ongoing asynchronous
     * work, like playing music or recording, ensuring that only one operation is occurring at a
     * time.
     *
     * For example, if the user is currently recording, and they hit the mic button again, the
     * second [onMicClicked] will cancel the previous [onMicClicked] that was doing the recording,
     * waiting for everything to be cleaned up, before running its own code.
     */
    private val playbackStateMutatorMutex = MutatorMutex()

    /**
     * The primary playback state.
     */
    var playbackState by mutableStateOf<PlaybackState>(PlaybackState.Ready)
        private set

    /**
     * The progress of an ongoing recording.
     *
     * Note that this value can be read even when recording is not occurring, in which case it
     * corresponds to the last known value of recording progress (or 0), where that value is useful
     * for animations.
     */
    var recordingProgress by mutableStateOf(0f)
        private set

    /**
     * `true` if we know the user has denied the record audio permission.
     */
    var isPermissionDenied by mutableStateOf(false)
        private set

    /**
     * `true` if we the permission rationale should be shown.
     */
    var showPermissionRationale by mutableStateOf(false)

    /**
     * `true` if we the speaker not supported rationale should be shown.
     */
    var showSpeakerNotSupported by mutableStateOf(false)

    /**
     * The [SoundRecorder] for recording and playing audio captured on-device.
     */
    private val soundRecorder = SoundRecorder(activity, "audiorecord.opus")

    suspend fun onStopped() {
        playbackStateMutatorMutex.mutate {
            playbackState = PlaybackState.Ready
        }
    }

    suspend fun onMicClicked() {
        playbackStateMutatorMutex.mutate {
            when (playbackState) {
                is PlaybackState.Ready,
                PlaybackState.PlayingVoice ->
                    // If we weren't recording, check our permission to start recording.
                    when {
                        ContextCompat.checkSelfPermission(
                            activity,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED -> {
                            // We have the permission, we can start recording now
                            playbackState = PlaybackState.Recording
                            record(
                                soundRecorder = soundRecorder,
                                setProgress = { progress ->
                                    recordingProgress = progress
                                }
                            )
                            playbackState = PlaybackState.Ready
                        }
                        activity.shouldShowRequestPermissionRationale(
                            Manifest.permission.RECORD_AUDIO
                        ) -> {
                            // If we should show the rationale prior to requesting the permission,
                            // send that event
                            showPermissionRationale = true
                            playbackState = PlaybackState.Ready
                        }
                        else -> {
                            // Request the permission
                            requestPermission()
                            playbackState = PlaybackState.Ready
                        }
                    }
                // If we were already recording, transition back to ready
                PlaybackState.Recording -> {
                    playbackState = PlaybackState.Ready
                }
            }
        }
    }

    suspend fun permissionResultReturned() {
        playbackStateMutatorMutex.mutate {
            // Check if the user granted the permission
            if (
                ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // The user granted the permission, continue on to start recording
                playbackState = PlaybackState.Recording
                record(
                    soundRecorder = soundRecorder,
                    setProgress = { progress ->
                        recordingProgress = progress
                    }
                )
                playbackState = PlaybackState.Ready
            } else {
                // We have confirmation now that the user denied the permission
                isPermissionDenied = true
                playbackState = PlaybackState.Ready
            }
        }
    }
}

/**
 * The three playback states of the application.
 */
sealed class PlaybackState {
    object Ready : PlaybackState()
    object PlayingVoice : PlaybackState()
    object Recording : PlaybackState()
}

/**
 * Determines if the device has a way to output audio and if it is supported.
 *
 * This could be an on-device speaker, or a connected bluetooth device.
 */
private fun speakerIsSupported(activity: Activity): Boolean {
    val hasAudioOutputFeature =
        activity.packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT)
    val devices = activity.getSystemService<AudioManager>()!!
        .getDevices(AudioManager.GET_DEVICES_OUTPUTS)

    // We can only trust AudioDeviceInfo.TYPE_BUILTIN_SPEAKER if the device advertises
    // FEATURE_AUDIO_OUTPUT
    val hasBuiltInSpeaker = devices.any { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER } &&
        hasAudioOutputFeature

    // Check all the Wear supported BT devices
    // https://developer.android.com/training/wearables/apps/audio
    val hasBluetoothSpeaker = devices.any {
        it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
            it.type == AudioDeviceInfo.TYPE_BLE_BROADCAST ||
            it.type == AudioDeviceInfo.TYPE_BLE_SPEAKER ||
            it.type == AudioDeviceInfo.TYPE_BLE_HEADSET
    }

    return hasBuiltInSpeaker || hasBluetoothSpeaker
}

/**
 * A helper function to record, updating the progress state while recording.
 *
 * This requires the [Manifest.permission.RECORD_AUDIO] permission to run.
 */
@RequiresPermission(Manifest.permission.RECORD_AUDIO)
private suspend fun record(
    soundRecorder: SoundRecorder,
    setProgress: (progress: Float) -> Unit,
    maxRecordingDuration: Duration = Duration.ofSeconds(10),
    numberTicks: Int = 10
) {
    coroutineScope {
        // Kick off a parallel job to record
        val recordingJob = launch { soundRecorder.record() }

        val delayPerTickMs = maxRecordingDuration.toMillis() / numberTicks
        val startTime = System.currentTimeMillis()

        repeat(numberTicks) { index ->
            setProgress(index.toFloat() / numberTicks)
            delay(startTime + delayPerTickMs * (index + 1) - System.currentTimeMillis())
        }
        // Update the progress to be complete
        setProgress(1f)

        // Stop recording
        recordingJob.cancel()
    }
}
