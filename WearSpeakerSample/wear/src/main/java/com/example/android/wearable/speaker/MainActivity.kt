/*
 * Copyright (C) 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.wearable.speaker

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.wear.ambient.AmbientModeSupport
import com.example.android.wearable.speaker.databinding.MainActivityBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.Duration
import kotlin.coroutines.resume

/**
 * We first get the required permission to use the MIC. If it is granted, then we continue with
 * the application and present the UI with three icons: a MIC icon (if pressed, user can record up
 * to 10 seconds), a Play icon (if clicked, it wil playback the recorded audio file) and a music
 * note icon (if clicked, it plays an MP3 file that is included in the app).
 */
class MainActivity : FragmentActivity(), AmbientModeSupport.AmbientCallbackProvider {

    /**
     * Ambient mode controller attached to this display. Used by Activity to see if it is in
     * ambient mode.
     */
    private lateinit var ambientController: AmbientModeSupport.AmbientController

    private lateinit var binding: MainActivityBinding

    private var soundRecorder: SoundRecorder? = null

    private var appState: AppState = AppState.AtSteadyState(SteadyState.READY)

    private var ongoingWork: Job = Job().apply { complete() }

    /**
     * The four "resting" states of the application, corresponding to the 4 constraint sets of the [MotionLayout].
     */
    enum class SteadyState {
        READY,
        PLAYING_VOICE,
        PLAYING_MUSIC,
        RECORDING,
    }

    /**
     * The overall state of the application.
     *
     * This either consists of being at a steady state, or transitioning to one of the steady states.
     */
    sealed class AppState {

        /**
         * The app is not transitioning, and is at the given [SteadyState].
         */
        data class AtSteadyState(
            val steadyState: SteadyState,
        ) : AppState()

        /**
         * The app is transitioning to the given [SteadyState].
         */
        data class TransitioningTo(
            val steadyState: SteadyState,
        ) : AppState()
    }

    /**
     * One of the actions the app interprets. This can either be direct user actions (clicking one of the buttons),
     * or programmatic actions (finishing playback or recording, or transition finishing).
     */
    sealed class AppAction {
        object MicClicked : AppAction()
        object PlayClicked : AppAction()
        object MusicClicked : AppAction()
        object RecordingFinished : AppAction()
        object PlayRecordingFinished : AppAction()
        object PlayMusicFinished : AppAction()
        data class TransitionedTo(val steadyState: SteadyState) : AppAction()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enables Ambient mode.
        ambientController = AmbientModeSupport.attach(this)

        binding.mic.setOnClickListener { onAction(AppAction.MicClicked) }
        binding.play.setOnClickListener { onAction(AppAction.PlayClicked) }
        binding.music.setOnClickListener { onAction(AppAction.MusicClicked) }
    }

    private fun onAction(action: AppAction) {
        val oldState = appState
        val newState = when (action) {
            AppAction.MicClicked -> when (oldState) {
                is AppState.AtSteadyState -> when (oldState.steadyState) {
                    SteadyState.READY,
                    SteadyState.PLAYING_VOICE,
                    SteadyState.PLAYING_MUSIC -> AppState.TransitioningTo(SteadyState.RECORDING)
                    SteadyState.RECORDING -> AppState.TransitioningTo(SteadyState.READY)
                }
                is AppState.TransitioningTo -> AppState.TransitioningTo(SteadyState.READY)
            }
            AppAction.MusicClicked -> when (oldState) {
                is AppState.AtSteadyState -> when (oldState.steadyState) {
                    SteadyState.READY,
                    SteadyState.PLAYING_VOICE,
                    SteadyState.RECORDING -> AppState.TransitioningTo(SteadyState.PLAYING_MUSIC)
                    SteadyState.PLAYING_MUSIC -> AppState.TransitioningTo(SteadyState.READY)
                }
                is AppState.TransitioningTo -> AppState.TransitioningTo(SteadyState.READY)
            }
            AppAction.PlayClicked -> when (oldState) {
                is AppState.AtSteadyState -> when (oldState.steadyState) {
                    SteadyState.READY,
                    SteadyState.RECORDING,
                    SteadyState.PLAYING_MUSIC -> AppState.TransitioningTo(SteadyState.PLAYING_VOICE)
                    SteadyState.PLAYING_VOICE -> AppState.TransitioningTo(SteadyState.READY)
                }
                is AppState.TransitioningTo -> AppState.TransitioningTo(SteadyState.READY)
            }
            is AppAction.TransitionedTo -> AppState.AtSteadyState(action.steadyState)
            AppAction.PlayMusicFinished,
            AppAction.PlayRecordingFinished,
            AppAction.RecordingFinished -> when (oldState) {
                is AppState.AtSteadyState -> when (oldState.steadyState) {
                    SteadyState.READY -> AppState.AtSteadyState(SteadyState.READY)
                    SteadyState.RECORDING,
                    SteadyState.PLAYING_MUSIC,
                    SteadyState.PLAYING_VOICE -> AppState.TransitioningTo(SteadyState.READY)
                }
                is AppState.TransitioningTo -> AppState.TransitioningTo(SteadyState.READY)
            }
        }

        onNewState(newState)
    }

    private fun onNewState(newState: AppState) {
        val oldState = appState

        Log.d(TAG, "New app state is: $newState")

        // Short-circuit if we're in the same state
        if (oldState == newState) {
            return
        }

        appState = newState

        // Clean up any ongoing work (playing, recording, transitioning)
        ongoingWork.cancel()
        ongoingWork = lifecycleScope.launch {
            when (newState) {
                is AppState.AtSteadyState -> when (newState.steadyState) {
                    SteadyState.READY -> Unit
                    SteadyState.PLAYING_VOICE -> {
                        soundRecorder!!.play()

                        // Don't call onAction directly here or below, since we are in the job that ongoingWork is
                        // tracking.
                        // If we call onAction directly (or with Dispatchers.Main.immediate), we will cancel ourselves,
                        // causing strange effects.
                        lifecycleScope.launch(Dispatchers.Main) { onAction(AppAction.PlayRecordingFinished) }
                    }
                    SteadyState.PLAYING_MUSIC -> {
                        playMusic()

                        lifecycleScope.launch(Dispatchers.Main) { onAction(AppAction.PlayMusicFinished) }
                    }
                    SteadyState.RECORDING -> {
                        coroutineScope {
                            val recordingJob = launch { soundRecorder!!.record() }

                            val delayPerTickMs = MAX_RECORDING_DURATION.toMillis() / NUMBER_TICKS
                            val startTime = System.currentTimeMillis()

                            repeat(NUMBER_TICKS) { index ->
                                binding.progressBar.progress = (100f * index / NUMBER_TICKS).toInt()
                                delay(startTime + delayPerTickMs * (index + 1) - System.currentTimeMillis())
                            }
                            // Update the progress bar to be complete
                            binding.progressBar.progress = 100

                            recordingJob.cancel()
                        }

                        lifecycleScope.launch(Dispatchers.Main) { onAction(AppAction.RecordingFinished) }
                    }
                }
                is AppState.TransitioningTo -> {
                    // Only reset the progress bar if we are transitioning to recording. This allows for a nicer
                    // transition away from recording
                    if (newState.steadyState == SteadyState.RECORDING) {
                        binding.progressBar.progress = 0
                    }

                    val motionLayoutState = when (newState.steadyState) {
                        SteadyState.READY -> R.id.allMinimized
                        SteadyState.PLAYING_VOICE -> R.id.playExpanded
                        SteadyState.PLAYING_MUSIC -> R.id.musicExpanded
                        SteadyState.RECORDING -> R.id.micExpanded
                    }

                    binding.outerCircle.transitionToState(motionLayoutState)
                    binding.outerCircle.awaitState(motionLayoutState)

                    lifecycleScope.launch(Dispatchers.Main) { onAction(AppAction.TransitionedTo(newState.steadyState)) }
                }
            }
        }
    }

    /**
     * Plays the MP3 file embedded in the application.
     */
    private suspend fun playMusic() {
        val mediaPlayer = MediaPlayer.create(this, R.raw.sound)

        try {
            suspendCancellableCoroutine<Unit> { cont ->
                mediaPlayer.setOnCompletionListener { // we need to transition to the READY/Home state
                    Log.d(TAG, "Music Finished")
                    cont.resume(Unit)
                }
                mediaPlayer.start()
            }
        } finally {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
    }

    /**
     * Checks the permission that this app needs and if it has not been granted, it will
     * prompt the user to grant it, otherwise it shuts down the app.
     */
    private fun checkPermissions() {
        val recordAudioPermissionGranted = (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED)
        if (recordAudioPermissionGranted) {
            start()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO),
                                              PERMISSIONS_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                start()
            } else {
                // Permission has been denied before. At this point we should show a dialog to
                // user and explain why this permission is needed and direct them to go to the
                // Permissions settings for the app in the System settings. For this sample, we
                // simply exit to get to the important part.
                Toast.makeText(this, R.string.exiting_for_permissions, Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    /**
     * Starts the main flow of the application.
     */
    private fun start() {
        soundRecorder = SoundRecorder(this, VOICE_FILE_NAME)
    }

    override fun onStart() {
        super.onStart()
        if (speakerIsSupported()) {
            checkPermissions()
        } else {
            binding.outerCircle.setOnClickListener {
                Toast.makeText(this@MainActivity, R.string.no_speaker_supported,
                               Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStop() {
        ongoingWork.cancel()
        soundRecorder = null

        super.onStop()
    }

    /**
     * Determines if the wear device has a built-in speaker and if it is supported.
     */
    private fun speakerIsSupported(): Boolean {
        // The results from AudioManager.getDevices can't be trusted unless the device
        // advertises FEATURE_AUDIO_OUTPUT.
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT)) {
            return false
        }
        return getSystemService<AudioManager>()!!
            .getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            .any { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
    }

    override fun getAmbientCallback(): AmbientModeSupport.AmbientCallback = MyAmbientCallback()

    private inner class MyAmbientCallback : AmbientModeSupport.AmbientCallback() {
        /** Prepares the UI for ambient mode.  */
        override fun onEnterAmbient(ambientDetails: Bundle) {
            super.onEnterAmbient(ambientDetails)
            Log.d(TAG, "onEnterAmbient() $ambientDetails")

            // Changes views to grey scale.
            binding.outerCircle.setBackgroundColor(
                ContextCompat.getColor(this@MainActivity, R.color.light_grey)
            )
            binding.innerCircle.background = ContextCompat.getDrawable(this@MainActivity, R.drawable.grey_circle)
            binding.progressBar.progressTintList =
                AppCompatResources.getColorStateList(this@MainActivity, R.color.white)
            binding.progressBar.progressBackgroundTintList =
                AppCompatResources.getColorStateList(this@MainActivity, R.color.black)
        }

        /** Restores the UI to active (non-ambient) mode.  */
        override fun onExitAmbient() {
            super.onExitAmbient()
            Log.d(TAG, "onExitAmbient()")

            // Changes views to color.
            binding.outerCircle.setBackgroundColor(
                ContextCompat.getColor(this@MainActivity, R.color.background_color)
            )
            binding.innerCircle.background = ContextCompat.getDrawable(this@MainActivity, R.drawable.color_circle)
            binding.progressBar.progressTintList =
                AppCompatResources.getColorStateList(this@MainActivity, R.color.progressbar_tint)
            binding.progressBar.progressBackgroundTintList =
                AppCompatResources.getColorStateList(this@MainActivity, R.color.progressbar_background_tint)
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val PERMISSIONS_REQUEST_CODE = 100
        private val MAX_RECORDING_DURATION = Duration.ofSeconds(10)
        private val NUMBER_TICKS = 10
        private const val VOICE_FILE_NAME = "audiorecord.pcm"
    }
}