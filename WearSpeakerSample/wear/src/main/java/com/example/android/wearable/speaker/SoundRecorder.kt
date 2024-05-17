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
import android.media.MediaRecorder
import android.util.Log
import androidx.annotation.RequiresPermission
import java.io.File
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * A helper class to provide methods to record audio input from the MIC to the internal storage.
 */
class SoundRecorder(
    context: Context,
    outputFileName: String
) {
    private val audioFile = File(context.filesDir, outputFileName)

    private var state = State.IDLE

    private enum class State {
        IDLE, RECORDING
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

        suspendCancellableCoroutine<Unit> { cont ->
            @Suppress("DEPRECATION")
            val mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.OGG)
                setAudioEncoder(MediaRecorder.AudioEncoder.OPUS)
                setOutputFile(audioFile.path)
                setOnInfoListener { mr, what, extra ->
                    println("info: $mr $what $extra")
                }
                setOnErrorListener { mr, what, extra ->
                    println("error: $mr $what $extra")
                }
            }

            cont.invokeOnCancellation {
                mediaRecorder.stop()
                state = State.IDLE
            }

            mediaRecorder.prepare()
            mediaRecorder.start()

            state = State.RECORDING
        }
    }

    companion object {
        private const val TAG = "SoundRecorder"
    }
}
