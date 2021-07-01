/*
 * Copyright (C) 2021 The Android Open Source Project
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
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.android.wearable.speaker.MainViewModel.AppAction
import com.example.android.wearable.speaker.MainViewModel.AppState
import com.example.android.wearable.speaker.databinding.MainActivityBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.Duration
import kotlin.coroutines.resume

/**
 * We first get the required permission to use the MIC. If it is granted, then we continue with
 * the application and present the UI with three icons: a MIC icon (if pressed, user can record up
 * to 10 seconds), a Play icon (if clicked, it will playback the recorded audio file) and a music
 * note icon (if clicked, it plays an MP3 file that is included in the app).
 */
class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<MainViewModel>()

    private lateinit var binding: MainActivityBinding

    private var soundRecorder: SoundRecorder? = null

    private var ongoingWork: Job = Job().apply { complete() }

    private val requestPermissionLauncher = registerForActivityResult(RequestPermission()) {
        // We ignore the direct result here, since we're going to check anyway.
        viewModel.onAction(AppAction.PermissionResultReturned)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mic.setOnClickListener {
            viewModel.onAction(
                AppAction.MicClicked(shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO))
            )
        }
        binding.play.setOnClickListener { viewModel.onAction(AppAction.PlayClicked) }
        binding.music.setOnClickListener { viewModel.onAction(AppAction.MusicClicked) }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.appState
                    .onEach { appState ->
                        // Handle each appState by canceling work being done to handle the old state (playing,
                        // recording, transitioning), and then handling the new state.
                        // It is important that MainViewModel.appState is a StateFlow, which means that we won't
                        // restart work if the new state is the same as the old state.

                        ongoingWork.cancel()
                        ongoingWork = launch {
                            onAppState(appState = appState)
                        }
                    }
                    .collect()
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.isPermissionDenied
                    .onEach { isPermissionDenied ->
                        binding.mic.isEnabled = !isPermissionDenied
                    }
                    .collect()
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.appEvents
                    .onEach { appEvent ->
                        when (appEvent) {
                            MainViewModel.AppEvent.RequestPermission ->
                                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            MainViewModel.AppEvent.ShowPermissionRationale -> AlertDialog.Builder(this@MainActivity)
                                .setMessage(R.string.rationale_for_microphone_permission)
                                .setPositiveButton(R.string.ok) { _, _ ->
                                    requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                                .setNegativeButton(R.string.cancel) { _, _ -> }
                                .create()
                                .show()
                            MainViewModel.AppEvent.ShowSpeakerNotSupported -> AlertDialog.Builder(this@MainActivity)
                                .setMessage(R.string.no_speaker_supported)
                                .setPositiveButton(R.string.ok) { _, _ -> }
                                .create()
                                .show()
                        }
                    }
                    .collect()
            }
        }
    }

    /**
     * The primary update method based on the [AppState].
     *
     * This method suspends, as we do suspending work based on the [appState].
     */
    private suspend fun onAppState(appState: AppState) {
        Log.d(TAG, "New app state is: $appState")

        // Only reset the progress bar if we are transitioning to recording. This allows for a nicer
        // transition away from recording
        if (appState == AppState.Recording) {
            binding.progressBar.progress = 0
        }

        @IdRes val motionLayoutState: Int
        val transitionInstantly: Boolean

        when (appState) {
            is AppState.Ready -> {
                motionLayoutState = R.id.allMinimized
                transitionInstantly = appState.transitionInstantly
            }
            AppState.PlayingVoice -> {
                motionLayoutState = R.id.playExpanded
                transitionInstantly = false
            }
            AppState.PlayingMusic -> {
                motionLayoutState = R.id.musicExpanded
                transitionInstantly = false
            }
            AppState.Recording -> {
                motionLayoutState = R.id.micExpanded
                transitionInstantly = false
            }
        }

        binding.outerCircle.transitionToState(motionLayoutState, transitionInstantly)
        binding.outerCircle.awaitState(motionLayoutState)

        when (appState) {
            is AppState.Ready -> {
                binding.mic.contentDescription = getString(R.string.record)
                binding.play.contentDescription = getString(R.string.play_recording)
                binding.music.contentDescription = getString(R.string.play_music)
            }
            AppState.PlayingVoice -> {
                binding.play.contentDescription = getString(R.string.stop_playing_recording)

                soundRecorder!!.play()

                // Don't call onAction directly here or below, since we are in the job that ongoingWork is
                // tracking.
                // If we call onAction directly (or with Dispatchers.Main.immediate), we will cancel ourselves,
                // causing strange effects.
                lifecycleScope.launch(Dispatchers.Main) { viewModel.onAction(AppAction.PlayRecordingFinished) }
            }
            AppState.PlayingMusic -> {
                binding.music.contentDescription = getString(R.string.stop_playing_music)

                playMusic()

                lifecycleScope.launch(Dispatchers.Main) { viewModel.onAction(AppAction.PlayMusicFinished) }
            }
            AppState.Recording -> {
                // This condition is guaranteed via the logic in the view model
                check(
                    ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.RECORD_AUDIO) ==
                        PackageManager.PERMISSION_GRANTED
                )

                binding.mic.contentDescription = getString(R.string.stop_recording)

                coroutineScope {
                    // Kick off a parallel job to record
                    val recordingJob = launch { soundRecorder!!.record() }

                    val delayPerTickMs = MAX_RECORDING_DURATION.toMillis() / NUMBER_TICKS
                    val startTime = System.currentTimeMillis()

                    repeat(NUMBER_TICKS) { index ->
                        binding.progressBar.progress = (100f * index / NUMBER_TICKS).toInt()
                        delay(startTime + delayPerTickMs * (index + 1) - System.currentTimeMillis())
                    }
                    // Update the progress bar to be complete
                    binding.progressBar.progress = 100

                    // Stop recording
                    recordingJob.cancel()
                }

                lifecycleScope.launch(Dispatchers.Main) { viewModel.onAction(AppAction.RecordingFinished) }
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
                mediaPlayer.setOnCompletionListener {
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

    override fun onStart() {
        super.onStart()
        soundRecorder = SoundRecorder(this, VOICE_FILE_NAME)
        viewModel.onAction(AppAction.ViewStarted)
    }

    override fun onStop() {
        ongoingWork.cancel()
        soundRecorder = null
        super.onStop()
    }

    companion object {
        private const val TAG = "MainActivity"
        private val MAX_RECORDING_DURATION = Duration.ofSeconds(10)
        private const val NUMBER_TICKS = 10
        private const val VOICE_FILE_NAME = "audiorecord.pcm"
    }
}
