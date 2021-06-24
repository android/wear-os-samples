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
import android.media.MediaPlayer.OnCompletionListener
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.wear.ambient.AmbientModeSupport
import com.example.android.wearable.speaker.SoundRecorder.OnVoicePlaybackStateChangedListener
import com.example.android.wearable.speaker.UIAnimation.UIState
import com.example.android.wearable.speaker.UIAnimation.UIStateListener
import com.example.android.wearable.speaker.databinding.MainActivityBinding
import java.util.concurrent.TimeUnit

/**
 * We first get the required permission to use the MIC. If it is granted, then we continue with
 * the application and present the UI with three icons: a MIC icon (if pressed, user can record up
 * to 10 seconds), a Play icon (if clicked, it wil playback the recorded audio file) and a music
 * note icon (if clicked, it plays an MP3 file that is included in the app).
 */
class MainActivity : FragmentActivity(), AmbientModeSupport.AmbientCallbackProvider, UIStateListener,
                     OnVoicePlaybackStateChangedListener {

    /**
     * Ambient mode controller attached to this display. Used by Activity to see if it is in
     * ambient mode.
     */
    private lateinit var ambientController: AmbientModeSupport.AmbientController

    private lateinit var binding: MainActivityBinding

    private var mediaPlayer: MediaPlayer? = null
    private var state = AppState.READY
    private var uiState = UIState.HOME
    private var soundRecorder: SoundRecorder? = null
    private var uiAnimation: UIAnimation? = null
    private var countDownTimer: CountDownTimer? = null

    internal enum class AppState {
        READY, PLAYING_VOICE, PLAYING_MUSIC, RECORDING
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enables Ambient mode.
        ambientController = AmbientModeSupport.attach(this)
    }

    private fun setProgressBar(progressInMillis: Long) {
        binding.progressBar.progress = (progressInMillis / MILLIS_IN_SECOND).toInt()
    }

    override fun onUIStateChanged(state: UIState?) {
        Log.d(TAG, "UI State is: $state")
        if (uiState == state) {
            return
        }
        when (state) {
            UIState.MUSIC_UP -> {
                this.state = AppState.PLAYING_MUSIC
                uiState = state
                playMusic()
            }
            UIState.MIC_UP -> {
                this.state = AppState.RECORDING
                uiState = state
                soundRecorder!!.startRecording()
                setProgressBar(COUNT_DOWN_MS)
                countDownTimer = object : CountDownTimer(COUNT_DOWN_MS, MILLIS_IN_SECOND) {
                    override fun onTick(millisUntilFinished: Long) {
                        binding.progressBar.visibility = View.VISIBLE
                        setProgressBar(millisUntilFinished)
                        Log.d(TAG, "Time Left: " + millisUntilFinished / MILLIS_IN_SECOND)
                    }

                    override fun onFinish() {
                        binding.progressBar.progress = 0
                        binding.progressBar.visibility = View.INVISIBLE
                        soundRecorder!!.stopRecording()
                        uiAnimation!!.transitionToHome()
                        uiState = UIState.HOME
                        this@MainActivity.state = AppState.READY
                        countDownTimer = null
                    }
                }.apply {
                    start()
                }
            }
            UIState.SOUND_UP -> {
                this.state = AppState.PLAYING_VOICE
                uiState = state
                soundRecorder!!.startPlay()
            }
            UIState.HOME -> when (this.state) {
                AppState.PLAYING_MUSIC -> {
                    this.state = AppState.READY
                    uiState = state
                    stopMusic()
                }
                AppState.PLAYING_VOICE -> {
                    this.state = AppState.READY
                    uiState = state
                    soundRecorder!!.stopPlaying()
                }
                AppState.RECORDING -> {
                    this.state = AppState.READY
                    uiState = state
                    soundRecorder!!.stopRecording()
                    if (countDownTimer != null) {
                        countDownTimer!!.cancel()
                        countDownTimer = null
                    }
                    binding.progressBar.visibility = View.INVISIBLE
                    setProgressBar(COUNT_DOWN_MS)
                }
            }
        }
    }

    /**
     * Plays back the MP3 file embedded in the application
     */
    private fun playMusic() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.sound).apply {
                setOnCompletionListener(OnCompletionListener { // we need to transition to the READY/Home state
                    Log.d(TAG, "Music Finished")
                    uiAnimation!!.transitionToHome()
                })
            }
        }
        mediaPlayer!!.start()
    }

    /**
     * Stops the playback of the MP3 file.
     */
    private fun stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
            mediaPlayer = null
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
                // user and explain why this permission is needed and direct him to go to the
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
        soundRecorder = SoundRecorder(this, VOICE_FILE_NAME, this)
        val animationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
        uiAnimation = UIAnimation(
            binding.container,
            arrayOf(binding.mic, binding.play, binding.music),
            binding.expanded,
            animationDuration,
            this
        )
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
        soundRecorder?.cleanup()
        soundRecorder = null
        countDownTimer?.cancel()
        countDownTimer = null
        mediaPlayer?.release()
        mediaPlayer = null

        super.onStop()
    }

    override fun onPlaybackStopped() {
        uiAnimation!!.transitionToHome()
        uiState = UIState.HOME
        state = AppState.READY
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
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        return devices.any { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
    }

    override fun getAmbientCallback(): AmbientModeSupport.AmbientCallback {
        return MyAmbientCallback()
    }

    private inner class MyAmbientCallback : AmbientModeSupport.AmbientCallback() {
        /** Prepares the UI for ambient mode.  */
        override fun onEnterAmbient(ambientDetails: Bundle) {
            super.onEnterAmbient(ambientDetails)
            Log.d(TAG, "onEnterAmbient() $ambientDetails")

            // Changes views to grey scale.
            val context = applicationContext
            val resources = context.resources
            binding.outerCircle.setBackgroundColor(
                ContextCompat.getColor(context, R.color.light_grey))
            binding.innerCircle.background = ContextCompat.getDrawable(context, R.drawable.grey_circle)
            binding.progressBar.progressTintList = resources.getColorStateList(R.color.white, context.theme)
            binding.progressBar.progressBackgroundTintList = resources.getColorStateList(R.color.black, context.theme)
        }

        /** Restores the UI to active (non-ambient) mode.  */
        override fun onExitAmbient() {
            super.onExitAmbient()
            Log.d(TAG, "onExitAmbient()")

            // Changes views to color.
            val context = applicationContext
            val resources = context.resources
            binding.outerCircle.setBackgroundColor(
                ContextCompat.getColor(context, R.color.background_color))
            binding.innerCircle.background = ContextCompat.getDrawable(context, R.drawable.color_circle)
            binding.progressBar.progressTintList = resources.getColorStateList(R.color.progressbar_tint, context.theme)
            binding.progressBar.progressBackgroundTintList = resources.getColorStateList(
                R.color.progressbar_background_tint, context.theme)
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val PERMISSIONS_REQUEST_CODE = 100
        private val COUNT_DOWN_MS = TimeUnit.SECONDS.toMillis(10)
        private val MILLIS_IN_SECOND = TimeUnit.SECONDS.toMillis(1)
        private const val VOICE_FILE_NAME = "audiorecord.pcm"
    }
}