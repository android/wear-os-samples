/*
 * Copyright 2024 The Android Open Source Project
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

import android.content.Context
import android.os.Vibrator
import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.AppScaffold
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.audio.AudioOutput
import com.google.android.horologist.audio.AudioOutputRepository
import com.google.android.horologist.audio.VolumeRepository
import com.google.android.horologist.audio.VolumeState
import com.google.android.horologist.audio.ui.VolumeViewModel
import com.google.android.horologist.screenshots.rng.WearDevice
import com.google.android.horologist.screenshots.rng.WearScreenshotTest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment

class FakeVolumeRepository : VolumeRepository {
    override val volumeState: StateFlow<VolumeState> =
        MutableStateFlow(VolumeState(current = 5, max = 15))

    override fun increaseVolume() {}

    override fun decreaseVolume() {}

    override fun setVolume(volume: Int) {}

    override fun close() {}
}

class FakeAudioOutputRepository : AudioOutputRepository {
    override val audioOutput: StateFlow<AudioOutput> =
        MutableStateFlow(AudioOutput.Unknown("id", "name", true))
    override val available: StateFlow<List<AudioOutput>> = MutableStateFlow(emptyList())

    override fun launchOutputSelection(closeOnConnect: Boolean, clientPackageName: String?) {}

    override fun close() {}
}

@RunWith(ParameterizedRobolectricTestRunner::class)
class SpeakerPlayerScreenTest(override val device: WearDevice) : WearScreenshotTest() {
    override val tolerance = 0.02f

    @OptIn(ExperimentalHorologistApi::class)
    @Test
    fun speakerPlayerScreenTest() {
        val vibrator =
            RuntimeEnvironment.getApplication().getSystemService(
                Context.VIBRATOR_SERVICE
            ) as Vibrator
        val volumeViewModel =
            VolumeViewModel(
                FakeVolumeRepository(),
                FakeAudioOutputRepository(),
                {},
                vibrator
            )

        runTest {
            AppScaffold {
                SpeakerPlayerScreen(
                    onVolumeClick = {},
                    volumeViewModel = volumeViewModel
                )
            }
        }
    }

    @Composable
    override fun TestScaffold(content: @Composable () -> Unit) {
        content()
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun devices() = WearDevice.entries
    }
}
