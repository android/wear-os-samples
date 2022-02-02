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
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.File

/**
 * A helper class to provide methods to record audio input from the MIC to the internal storage
 * and to playback the same recorded audio file.
 */
class SoundRecorder(
    private val context: Context,
    private val outputFileName: String,
) {
    private var state = State.IDLE

    private enum class State {
        IDLE, RECORDING, PLAYING
    }

    /**
     * Plays the recorded file, if any.
     *
     * Returns when playing the file is finished.
     *
     * This is cancellable, and cancelling it will stop playback.
     */
    suspend fun play() {
        if (state != State.IDLE) {
            Log.w(TAG, "Requesting to play while state was not IDLE")
            return
        }

        // Check if there isn't a recording to play
        if (!File(context.filesDir, outputFileName).exists()) return

        state = State.PLAYING

        val intSize = AudioTrack.getMinBufferSize(RECORDING_RATE, CHANNELS_OUT, FORMAT)

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            .setBufferSizeInBytes(intSize)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(RECORDING_RATE)
                    .setChannelMask(CHANNELS_OUT)
                    .setEncoding(FORMAT)
                    .build()
            )
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack.setVolume(AudioTrack.getMaxVolume())
        audioTrack.play()

        try {
            withContext(Dispatchers.IO) {
                context.openFileInput(outputFileName).buffered().use { bufferedInputStream ->
                    val buffer = ByteArray(intSize * 2)
                    while (isActive) {
                        val read = bufferedInputStream.read(buffer, 0, buffer.size)
                        if (read < 0) break
                        audioTrack.write(buffer, 0, read)
                    }
                }
            }
        } finally {
            audioTrack.release()
            state = State.IDLE
        }
    }

    /**
     * Records from the microphone.
     *
     * This method is cancellable, and cancelling it will stop recording.
     */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    suspend fun record() {
        if (state != State.IDLE) {
            Log.w(TAG, "Requesting to start recording while state was not IDLE")
            return
        }

        state = State.RECORDING

        val intSize = AudioRecord.getMinBufferSize(RECORDING_RATE, CHANNEL_IN, FORMAT)

        val audioRecord = AudioRecord.Builder()
            .setAudioSource(MediaRecorder.AudioSource.MIC)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(RECORDING_RATE)
                    .setChannelMask(CHANNEL_IN)
                    .setEncoding(FORMAT)
                    .build()
            )
            .setBufferSizeInBytes(intSize * 3)
            .build()

        audioRecord.startRecording()

        try {
            withContext(Dispatchers.IO) {
                context.openFileOutput(outputFileName, Context.MODE_PRIVATE)
                    .buffered()
                    .use { bufferedOutputStream ->
                        val buffer = ByteArray(intSize)
                        while (isActive) {
                            val read = audioRecord.read(buffer, 0, buffer.size)
                            bufferedOutputStream.write(buffer, 0, read)
                        }
                    }
            }
        } finally {
            audioRecord.release()
            state = State.IDLE
        }
    }

    companion object {
        private const val TAG = "SoundRecorder"
        private const val RECORDING_RATE = 8000 // can go up to 44K, if needed
        private const val CHANNEL_IN = AudioFormat.CHANNEL_IN_MONO
        private const val CHANNELS_OUT = AudioFormat.CHANNEL_OUT_MONO
        private const val FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }
}
