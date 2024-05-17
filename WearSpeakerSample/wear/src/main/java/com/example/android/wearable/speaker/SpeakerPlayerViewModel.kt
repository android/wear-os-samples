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

import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.media.data.repository.PlayerRepositoryImpl
import com.google.android.horologist.media.model.Media
import com.google.android.horologist.media.ui.state.PlayerViewModel
import java.io.File
import kotlinx.coroutines.launch

@UnstableApi
@OptIn(ExperimentalHorologistApi::class)
class SpeakerPlayerViewModel(
    playerRepository: PlayerRepositoryImpl,
    player: Player,
    audioFile: File,
    audioFileUri: String
) : PlayerViewModel(playerRepository) {

    init {
        viewModelScope.launch {
            playerRepository.connect(player) {}
            if (audioFile.exists()) {
                val media = Media(
                    id = "",
                    uri = audioFileUri,
                    title = "Recorded audio",
                    artist = ""
                )
                playerRepository.setMedia(media)
            }
        }
    }

    val playerState = playerRepository.player

    @ExperimentalHorologistApi
    public companion object {
        private const val TAG = "SpeakerPlayerViewModel"

        public val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY]!!

                val outputFileName = "audiorecord.opus"
                val audioFile = File(application.filesDir, outputFileName)
                val audioFileUri = application.filesDir.path + "/" + outputFileName

                val player = ExoPlayer.Builder(application)
                    .setSeekForwardIncrementMs(5000L)
                    .setSeekBackIncrementMs(5000L)
                    .build()

                SpeakerPlayerViewModel(
                    PlayerRepositoryImpl(),
                    player,
                    audioFile,
                    audioFileUri
                )
            }
        }
    }
}
