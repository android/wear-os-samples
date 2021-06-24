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

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.lang.ref.WeakReference

/**
 * A helper class to provide methods to record audio input from the MIC to the internal storage
 * and to playback the same recorded audio file.
 */
class SoundRecorder(
    context: Context,
    private val mOutputFileName: String,
    private val mListener: OnVoicePlaybackStateChangedListener?
) {
    private val mAudioManager: AudioManager
    private val mHandler: Handler
    private val mContext: Context
    private var mState = State.IDLE
    private var mRecordingAsyncTask: AsyncTask<Void?, Void?, Void?>? = null
    private var mPlayingAsyncTask: AsyncTask<Void?, Void?, Void?>? = null

    internal enum class State {
        IDLE, RECORDING, PLAYING
    }

    /**
     * Starts recording from the MIC.
     */
    fun startRecording() {
        if (mState != State.IDLE) {
            Log.w(TAG, "Requesting to start recording while state was not IDLE")
            return
        }
        mRecordingAsyncTask = RecordAudioAsyncTask(this).apply {
            execute()
        }
    }

    fun stopRecording() {
        if (mRecordingAsyncTask != null) {
            mRecordingAsyncTask!!.cancel(true)
        }
    }

    fun stopPlaying() {
        if (mPlayingAsyncTask != null) {
            mPlayingAsyncTask!!.cancel(true)
        }
    }

    /**
     * Starts playback of the recorded audio file.
     */
    fun startPlay() {
        if (mState != State.IDLE) {
            Log.w(TAG, "Requesting to play while state was not IDLE")
            return
        }
        if (!File(mContext.filesDir, mOutputFileName).exists()) {
            // there is no recording to play
            if (mListener != null) {
                mHandler.post { mListener.onPlaybackStopped() }
            }
            return
        }
        val intSize = AudioTrack.getMinBufferSize(RECORDING_RATE, CHANNELS_OUT, FORMAT)
        mPlayingAsyncTask = PlayAudioAsyncTask(this, intSize).apply {
            execute()
        }
    }

    interface OnVoicePlaybackStateChangedListener {
        /**
         * Called when the playback of the audio file ends. This should be called on the UI thread.
         */
        fun onPlaybackStopped()
    }

    /**
     * Cleans up some resources related to [AudioTrack] and [AudioRecord]
     */
    fun cleanup() {
        Log.d(TAG, "cleanup() is called")
        stopPlaying()
        stopRecording()
    }

    private class PlayAudioAsyncTask(context: SoundRecorder, intSize: Int) :
        AsyncTask<Void?, Void?, Void?>() {
        private val mSoundRecorderWeakReference: WeakReference<SoundRecorder>
        private var mAudioTrack: AudioTrack? = null
        private val mIntSize: Int
        override fun onPreExecute() {
            val soundRecorder = mSoundRecorderWeakReference.get()
            if (soundRecorder != null) {
                soundRecorder.mAudioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    soundRecorder.mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                    0 /* flags */)
                soundRecorder.mState = State.PLAYING
            }
        }

        override fun doInBackground(vararg params: Void?): Void? {
            val soundRecorder = mSoundRecorderWeakReference.get()
            try {
                mAudioTrack = AudioTrack(AudioManager.STREAM_MUSIC, RECORDING_RATE,
                                         CHANNELS_OUT, FORMAT, mIntSize, AudioTrack.MODE_STREAM)
                val buffer = ByteArray(mIntSize * 2)
                var `in`: FileInputStream? = null
                var bis: BufferedInputStream? = null
                mAudioTrack!!.setVolume(AudioTrack.getMaxVolume())
                mAudioTrack!!.play()
                try {
                    `in` = soundRecorder!!.mContext.openFileInput(soundRecorder.mOutputFileName)
                    bis = BufferedInputStream(`in`)
                    var read = 0
                    while (!isCancelled && bis.read(buffer, 0, buffer.size).also { read = it } > 0) {
                        mAudioTrack!!.write(buffer, 0, read)
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "Failed to read the sound file into a byte array", e)
                } finally {
                    try {
                        `in`?.close()
                        bis?.close()
                    } catch (e: IOException) { /* ignore */
                    }
                    mAudioTrack!!.release()
                }
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Failed to start playback", e)
            }
            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            cleanup()
        }

        override fun onCancelled() {
            cleanup()
        }

        private fun cleanup() {
            val soundRecorder = mSoundRecorderWeakReference.get()
            if (soundRecorder != null) {
                if (soundRecorder.mListener != null) {
                    soundRecorder.mListener.onPlaybackStopped()
                }
                soundRecorder.mState = State.IDLE
                soundRecorder.mPlayingAsyncTask = null
            }
        }

        init {
            mSoundRecorderWeakReference = WeakReference(context)
            mIntSize = intSize
        }
    }

    private class RecordAudioAsyncTask(context: SoundRecorder) : AsyncTask<Void?, Void?, Void?>() {
        private val mSoundRecorderWeakReference: WeakReference<SoundRecorder>
        private var mAudioRecord: AudioRecord? = null
        override fun onPreExecute() {
            val soundRecorder = mSoundRecorderWeakReference.get()
            if (soundRecorder != null) {
                soundRecorder.mState = State.RECORDING
            }
        }

        override fun doInBackground(vararg params: Void?): Void? {
            val soundRecorder = mSoundRecorderWeakReference.get()
            mAudioRecord = AudioRecord(MediaRecorder.AudioSource.MIC,
                                       RECORDING_RATE, CHANNEL_IN, FORMAT, BUFFER_SIZE * 3)
            var bufferedOutputStream: BufferedOutputStream? = null
            try {
                bufferedOutputStream = BufferedOutputStream(
                    soundRecorder!!.mContext.openFileOutput(
                        soundRecorder.mOutputFileName,
                        Context.MODE_PRIVATE))
                val buffer = ByteArray(BUFFER_SIZE)
                mAudioRecord!!.startRecording()
                while (!isCancelled) {
                    val read = mAudioRecord!!.read(buffer, 0, buffer.size)
                    bufferedOutputStream.write(buffer, 0, read)
                }
            } catch (e: IOException) {
                Log.e(TAG, "Failed to record data: $e")
            } catch (e: NullPointerException) {
                Log.e(TAG, "Failed to record data: $e")
            } catch (e: IndexOutOfBoundsException) {
                Log.e(TAG, "Failed to record data: $e")
            } finally {
                if (bufferedOutputStream != null) {
                    try {
                        bufferedOutputStream.close()
                    } catch (e: IOException) {
                        // ignore
                    }
                }
                mAudioRecord!!.release()
                mAudioRecord = null
            }
            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            val soundRecorder = mSoundRecorderWeakReference.get()
            if (soundRecorder != null) {
                soundRecorder.mState = State.IDLE
                soundRecorder.mRecordingAsyncTask = null
            }
        }

        override fun onCancelled() {
            val soundRecorder = mSoundRecorderWeakReference.get()
            if (soundRecorder != null) {
                if (soundRecorder.mState == State.RECORDING) {
                    Log.d(TAG, "Stopping the recording ...")
                    soundRecorder.mState = State.IDLE
                } else {
                    Log.w(TAG, "Requesting to stop recording while state was not RECORDING")
                }
                soundRecorder.mRecordingAsyncTask = null
            }
        }

        init {
            mSoundRecorderWeakReference = WeakReference(context)
        }
    }

    companion object {
        private const val TAG = "SoundRecorder"
        private const val RECORDING_RATE = 8000 // can go up to 44K, if needed
        private const val CHANNEL_IN = AudioFormat.CHANNEL_IN_MONO
        private const val CHANNELS_OUT = AudioFormat.CHANNEL_OUT_MONO
        private const val FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private val BUFFER_SIZE = AudioRecord
            .getMinBufferSize(RECORDING_RATE, CHANNEL_IN, FORMAT)
    }

    init {
        mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mHandler = Handler(Looper.getMainLooper())
        mContext = context
    }
}