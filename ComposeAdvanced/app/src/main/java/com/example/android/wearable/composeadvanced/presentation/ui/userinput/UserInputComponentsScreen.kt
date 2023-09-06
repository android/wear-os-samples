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
package com.example.android.wearable.composeadvanced.presentation.ui.userinput

import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.inputmethod.EditorInfo
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.wear.input.RemoteInputIntentHelper
import androidx.wear.input.wearableExtender
import com.example.android.wearable.composeadvanced.R
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.material.Chip
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Shows different input options like Pickers, Steppers and Sliders
 */
@Composable
fun UserInputComponentsScreen(
    columnState: ScalingLazyColumnState,
    value: Int,
    dateTime: LocalDateTime,
    onClickStepper: () -> Unit,
    onClickSlider: () -> Unit,
    onClickDemoDatePicker: () -> Unit,
    onClickDemo12hTimePicker: () -> Unit,
    onClickDemo24hTimePicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textForUserInput by remember { mutableStateOf("") }
    var textForVoiceInput by remember { mutableStateOf("") }

    val inputTextKey = "input_text"

    val launcher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            it.data?.let { data ->
                val results: Bundle = RemoteInput.getResultsFromIntent(data)
                val newInputText: CharSequence? = results.getCharSequence(inputTextKey)
                textForUserInput = newInputText as String
            }
        }

    val voiceLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            it.data?.let { data ->
                val results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                textForVoiceInput = results?.get(0) ?: "None"
            }
        }

    ScalingLazyColumn(
        columnState = columnState,
        modifier = modifier.fillMaxSize()
    ) {
        item {
            Chip(
                onClick = onClickStepper,
                label = stringResource(id = R.string.stepper_label),
                secondaryLabel = value.toString()
            )
        }

        item {
            Chip(
                onClick = onClickSlider,
                label = stringResource(id = R.string.slider_label),
                secondaryLabel = value.toString()
            )
        }

        item {
            Chip(
                onClick = onClickDemoDatePicker,
                label = stringResource(R.string.date_picker_label),
                secondaryLabel = dateTime.toLocalDate().toString()
            )
        }

        item {
            val formatter = remember { DateTimeFormatter.ofPattern("hh:mm a") }
            Chip(
                onClick = onClickDemo12hTimePicker,
                label = stringResource(R.string.time_12h_picker_label),
                secondaryLabel = dateTime.toLocalTime().format(formatter)
            )
        }

        item {
            val formatter = remember { DateTimeFormatter.ofPattern("HH:mm:ss") }

            Chip(
                onClick = onClickDemo24hTimePicker,
                label = stringResource(R.string.time_24h_picker_label),
                secondaryLabel = dateTime.toLocalTime().format(formatter)
            )
        }

        item {
            val intent: Intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
            val remoteInputs: List<RemoteInput> = listOf(
                RemoteInput.Builder(inputTextKey)
                    .setLabel(stringResource(R.string.manual_text_entry_label))
                    .wearableExtender {
                        setEmojisAllowed(true)
                        setInputActionType(EditorInfo.IME_ACTION_DONE)
                    }.build()
            )

            RemoteInputIntentHelper.putRemoteInputsExtra(intent, remoteInputs)

            Chip(
                onClick = {
                    launcher.launch(intent)
                },
                label = stringResource(R.string.text_input_label),
                secondaryLabel = textForUserInput
            )
        }

        item {
            val voiceIntent: Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )

                putExtra(
                    RecognizerIntent.EXTRA_PROMPT,
                    stringResource(R.string.voice_text_entry_label)
                )
            }

            Chip(
                onClick = {
                    voiceLauncher.launch(voiceIntent)
                },
                label = stringResource(R.string.voice_input_label),
                secondaryLabel = textForVoiceInput
            )
        }
    }
}
