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
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.wear.ambient.AmbientModeSupport
import com.example.android.wearable.speaker.SoundRecorder.OnVoicePlaybackStateChangedListener
import com.example.android.wearable.speaker.UIAnimation.UIState
import com.example.android.wearable.speaker.UIAnimation.UIStateListener
import java.util.concurrent.TimeUnit

/**
 * We first get the required permission to use the MIC. If it is granted, then we continue with
 * the application and present the UI with three icons: a MIC icon (if pressed, user can record up
 * to 10 seconds), a Play icon (if clicked, it wil playback the recorded audio file) and a music
 * note icon (if clicked, it plays an MP3 file that is included in the app).
 */
class MainActivity : FragmentActivity(), AmbientModeSupport.AmbientCallbackProvider, UIStateListener,
                     OnVoicePlaybackStateChangedListener {
    private var mMediaPlayer: MediaPlayer? = null
    private var mState = AppState.READY
    private var mUiState = UIState.HOME
    private var mSoundRecorder: SoundRecorder? = null
    private var mOuterCircle: RelativeLayout? = null
    private var mInnerCircle: View? = null
    private var mUIAnimation: UIAnimation? = null
    private var mProgressBar: ProgressBar? = null
    private var mCountDownTimer: CountDownTimer? = null

    /**
     * Ambient mode controller attached to this display. Used by Activity to see if it is in
     * ambient mode.
     */
    private var mAmbientController: AmbientModeSupport.AmbientController? = null

    internal enum class AppState {
        READY, PLAYING_VOICE, PLAYING_MUSIC, RECORDING
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        mOuterCircle = findViewById(R.id.outer_circle)
        mInnerCircle = findViewById(R.id.inner_circle)
        mProgressBar = findViewById(R.id.progress_bar)

        // Enables Ambient mode.
        mAmbientController = AmbientModeSupport.attach(this)
    }

    private fun setProgressBar(progressInMillis: Long) {
        mProgressBar!!.progress = (progressInMillis / MILLIS_IN_SECOND).toInt()
    }

    override fun onUIStateChanged(state: UIState?) {
        Log.d(TAG, "UI State is: $state")
        if (mUiState == state) {
            return
        }
        when (state) {
            UIState.MUSIC_UP -> {
                mState = AppState.PLAYING_MUSIC
                mUiState = state
                playMusic()
            }
            UIState.MIC_UP -> {
                mState = AppState.RECORDING
                mUiState = state
                mSoundRecorder!!.startRecording()
                setProgressBar(COUNT_DOWN_MS)
                mCountDownTimer = object : CountDownTimer(COUNT_DOWN_MS, MILLIS_IN_SECOND) {
                    override fun onTick(millisUntilFinished: Long) {
                        mProgressBar!!.visibility = View.VISIBLE
                        setProgressBar(millisUntilFinished)
                        Log.d(TAG, "Time Left: " + millisUntilFinished / MILLIS_IN_SECOND)
                    }

                    override fun onFinish() {
                        mProgressBar!!.progress = 0
                        mProgressBar!!.visibility = View.INVISIBLE
                        mSoundRecorder!!.stopRecording()
                        mUIAnimation!!.transitionToHome()
                        mUiState = UIState.HOME
                        mState = AppState.READY
                        mCountDownTimer = null
                    }
                }.apply {
                    start()
                }
            }
            UIState.SOUND_UP -> {
                mState = AppState.PLAYING_VOICE
                mUiState = state
                mSoundRecorder!!.startPlay()
            }
            UIState.HOME -> when (mState) {
                AppState.PLAYING_MUSIC -> {
                    mState = AppState.READY
                    mUiState = state
                    stopMusic()
                }
                AppState.PLAYING_VOICE -> {
                    mState = AppState.READY
                    mUiState = state
                    mSoundRecorder!!.stopPlaying()
                }
                AppState.RECORDING -> {
                    mState = AppState.READY
                    mUiState = state
                    mSoundRecorder!!.stopRecording()
                    if (mCountDownTimer != null) {
                        mCountDownTimer!!.cancel()
                        mCountDownTimer = null
                    }
                    mProgressBar!!.visibility = View.INVISIBLE
                    setProgressBar(COUNT_DOWN_MS)
                }
            }
        }
    }

    /**
     * Plays back the MP3 file embedded in the application
     */
    private fun playMusic() {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(this, R.raw.sound).apply {
                setOnCompletionListener(OnCompletionListener { // we need to transition to the READY/Home state
                    Log.d(TAG, "Music Finished")
                    mUIAnimation!!.transitionToHome()
                })
            }
        }
        mMediaPlayer!!.start()
    }

    /**
     * Stops the playback of the MP3 file.
     */
    private fun stopMusic() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.stop()
            mMediaPlayer!!.release()
            mMediaPlayer = null
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
        mSoundRecorder = SoundRecorder(this, VOICE_FILE_NAME, this)
        val thumbResources = intArrayOf(R.id.mic, R.id.play, R.id.music)
        val thumbs = arrayOfNulls<ImageView>(3)
        for (i in 0..2) {
            thumbs[i] = findViewById(thumbResources[i])
        }
        val containerView = findViewById<View>(R.id.container)
        val expandedView = findViewById<ImageView>(R.id.expanded)
        val animationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
        mUIAnimation = UIAnimation(containerView, thumbs, expandedView, animationDuration,
                                   this)
    }

    override fun onStart() {
        super.onStart()
        if (speakerIsSupported()) {
            checkPermissions()
        } else {
            mOuterCircle!!.setOnClickListener {
                Toast.makeText(this@MainActivity, R.string.no_speaker_supported,
                               Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStop() {
        if (mSoundRecorder != null) {
            mSoundRecorder!!.cleanup()
            mSoundRecorder = null
        }
        if (mCountDownTimer != null) {
            mCountDownTimer!!.cancel()
        }
        if (mMediaPlayer != null) {
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
        super.onStop()
    }

    override fun onPlaybackStopped() {
        mUIAnimation!!.transitionToHome()
        mUiState = UIState.HOME
        mState = AppState.READY
    }

    /**
     * Determines if the wear device has a built-in speaker and if it is supported. Speaker, even if
     * physically present, is only supported in Android M+ on a wear device..
     */
    fun speakerIsSupported(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val packageManager = packageManager
            // The results from AudioManager.getDevices can't be trusted unless the device
            // advertises FEATURE_AUDIO_OUTPUT.
            if (!packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT)) {
                return false
            }
            val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            for (device in devices) {
                if (device.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) {
                    return true
                }
            }
        }
        return false
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
            mOuterCircle!!.setBackgroundColor(
                ContextCompat.getColor(context, R.color.light_grey))
            mInnerCircle!!.background = ContextCompat.getDrawable(context, R.drawable.grey_circle)
            mProgressBar!!.progressTintList = resources.getColorStateList(R.color.white, context.theme)
            mProgressBar!!.progressBackgroundTintList = resources.getColorStateList(R.color.black, context.theme)
        }

        /** Restores the UI to active (non-ambient) mode.  */
        override fun onExitAmbient() {
            super.onExitAmbient()
            Log.d(TAG, "onExitAmbient()")

            // Changes views to color.
            val context = applicationContext
            val resources = context.resources
            mOuterCircle!!.setBackgroundColor(
                ContextCompat.getColor(context, R.color.background_color))
            mInnerCircle!!.background = ContextCompat.getDrawable(context, R.drawable.color_circle)
            mProgressBar!!.progressTintList = resources.getColorStateList(R.color.progressbar_tint, context.theme)
            mProgressBar!!.progressBackgroundTintList = resources.getColorStateList(
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