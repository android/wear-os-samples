/*
 * Copyright (C) 2021 Google Inc. All Rights Reserved.
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
package com.example.android.wearable.alpha.editor

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.wear.complications.data.ComplicationData
import androidx.wear.watchface.DrawMode
import androidx.wear.watchface.RenderParameters
import androidx.wear.watchface.editor.EditorSession
import androidx.wear.watchface.style.UserStyle
import androidx.wear.watchface.style.UserStyleSetting
import androidx.wear.watchface.style.WatchFaceLayer
import com.example.android.wearable.alpha.data.watchface.MINUTE_HAND_LENGTH_FRACTION
import com.example.android.wearable.alpha.data.watchface.MINUTE_HAND_LENGTH_FRACTION_MAXIMUM
import com.example.android.wearable.alpha.data.watchface.MINUTE_HAND_LENGTH_FRACTION_MINIMUM
import com.example.android.wearable.alpha.utils.COLOR_STYLE_SETTING
import com.example.android.wearable.alpha.utils.DRAW_HOUR_PIPS_STYLE_SETTING
import com.example.android.wearable.alpha.utils.WATCH_HAND_LENGTH_STYLE_SETTING
import java.beans.PropertyChangeListener
import java.beans.PropertyChangeSupport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Maintains the [WatchFaceConfigActivity] state, i.e., handles reads and writes to the
 * [EditorSession] which is basically the watch face data layer. This allows the user to edit their
 * watch face through [WatchFaceConfigActivity].
 *
 * Note: This doesn't use an Android ViewModel because the [EditorSession]'s constructor requires a
 * ComponentActivity and Intent (needed for the library's complication editing UI which is triggered
 * through the [EditorSession]). Generally, Activities and Views shouldn't be passed to Android
 * ViewModels, so this is named StateHolder to avoid confusion.
 *
 * Also, the scope is passed in and we recommend you use the of the lifecycleScope of the Activity.
 *
 * For the [EditorSession] itself, this class uses the keys, [UserStyleSetting], for each of our
 * user styles and sets their values [UserStyleSetting.Option]. After a new value is set, creates a
 * new image preview via screenshot class and triggers a listener (which creates new data for the
 * [StateFlow] that feeds back to the Activity).
 */
class WatchFaceConfigStateHolder(
    scope: CoroutineScope,
    activity: ComponentActivity,
    editIntent: Intent
) {
    private val changeSupport = PropertyChangeSupport(this)

    private fun addPropertyChangeListener(listener: PropertyChangeListener) {
        changeSupport.addPropertyChangeListener(listener)
    }

    private fun removePropertyChangeListener(listener: PropertyChangeListener) {
        changeSupport.removePropertyChangeListener(listener)
    }

    private lateinit var editorSession: EditorSession

    // Watch Face styles the user can actively edit
    private lateinit var userStyle: UserStyle

    // Preview of complication data (needed to render screenshots)
    private lateinit var previewComplicationData: Map<Int, ComplicationData>

    // Keys from Watch Face Data Structure
    private lateinit var colorStyleKey: UserStyleSetting.ListUserStyleSetting
    private lateinit var drawPipsKey: UserStyleSetting.BooleanUserStyleSetting
    private lateinit var minuteHandLengthKey: UserStyleSetting.DoubleRangeUserStyleSetting

    // The UI collects from this StateFlow to get its state updates
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<EditWatchFaceUiState> = callbackFlow {
        val propertyChangeListener = PropertyChangeListener { event ->
            Log.d(TAG, "Change listener: ${event.propertyName}")
            trySend(event.newValue as UserStylesAndPreview)
        }
        addPropertyChangeListener(listener = propertyChangeListener)

        // The callback inside awaitClose will be executed when the flow is either closed or
        // cancelled. In this case, remove the CurrentUserStyleRepository.UserStyleChangeListener()
        // callback.
        awaitClose {
            Log.d(TAG, "awaitClose{ }")
            removePropertyChangeListener(propertyChangeListener)
        }
    }
        .buffer(Channel.CONFLATED)
        .map(EditWatchFaceUiState::Success)
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = EditWatchFaceUiState.Loading("Initializing")
        )

    init {
        scope.launch(Dispatchers.Main.immediate) {
            editorSession = EditorSession.createOnWatchEditorSession(
                activity = activity,
                editIntent = editIntent
            )

            previewComplicationData = editorSession.getComplicationsPreviewData()
            userStyle = editorSession.userStyle
            Log.d(WatchFaceConfigActivity.TAG, "userStyle: $userStyle")

            extractsUserStyles(userStyle)
            updatesWatchFacePreview()
        }
    }

    private fun extractsUserStyles(newUserStyle: UserStyle) {
        // Loops through user styles and retrieves user editable styles.
        for (options: Map.Entry<UserStyleSetting, UserStyleSetting.Option> in newUserStyle.selectedOptions) {
            when (options.key.id.toString()) {
                COLOR_STYLE_SETTING -> {
                    colorStyleKey = options.key as UserStyleSetting.ListUserStyleSetting
                }

                DRAW_HOUR_PIPS_STYLE_SETTING -> {
                    drawPipsKey = options.key as UserStyleSetting.BooleanUserStyleSetting
                }

                WATCH_HAND_LENGTH_STYLE_SETTING -> {
                    minuteHandLengthKey =
                        options.key as UserStyleSetting.DoubleRangeUserStyleSetting
                }
                // TODO (codingjeremy): Add complication change support if settings activity
                // PR doesn't cover it. Otherwise, remove comment.
            }
        }
    }

    /* Creates a new bitmap render of the updated watch face and passes it along (with all the other
     * updated values) to the Activity to render.
     */
    private fun updatesWatchFacePreview() {

        val bitmap = editorSession.renderWatchFaceToBitmap(
            RenderParameters(
                DrawMode.INTERACTIVE,
                WatchFaceLayer.ALL_WATCH_FACE_LAYERS,
                RenderParameters.HighlightLayer(
                    RenderParameters.HighlightedElement.AllComplicationSlots,
                    Color.RED, // Red complication highlight.
                    Color.argb(128, 0, 0, 0) // Darken everything else.
                )
            ),
            editorSession.previewReferenceTimeMillis,
            previewComplicationData
        )

        val colorStyle =
            userStyle[colorStyleKey] as UserStyleSetting.ListUserStyleSetting.ListOption
        val ticksEnabledStyle =
            userStyle[drawPipsKey] as UserStyleSetting.BooleanUserStyleSetting.BooleanOption
        val minuteHandStyle =
            userStyle[minuteHandLengthKey] as UserStyleSetting.DoubleRangeUserStyleSetting.DoubleRangeOption

        val watchFacePreview = UserStylesAndPreview(
            colorStyleId = colorStyle.id.toString(),
            ticksEnabled = ticksEnabledStyle.value,
            minuteHandLength = multiplyByMultipleForSlider(minuteHandStyle.value).toFloat(),
            previewImage = bitmap
        )

        // Triggers listener that's connected to the flow, so the UI gets the updated values.
        // Uses null as oldValue, as this is only triggered when something changes and there isn't
        // a reason the keep a variable of it.
        changeSupport.firePropertyChange(
            "newWatchFace",
            null,
            watchFacePreview)
    }

    fun setColorStyle(newColorStyle: UserStyleSetting.ListUserStyleSetting.ListOption) {
        setUserStyleOption(colorStyleKey, newColorStyle)
    }

    fun setDrawPips(enabled: Boolean) {
        setUserStyleOption(
            drawPipsKey,
            UserStyleSetting.BooleanUserStyleSetting.BooleanOption.from(enabled)
        )
    }

    fun setMinuteHandArmLength(newLengthRatio: Float) {
        val newMinuteHandLengthRatio = newLengthRatio.toDouble() / MULTIPLE_FOR_SLIDER

        setUserStyleOption(
            minuteHandLengthKey,
            UserStyleSetting.DoubleRangeUserStyleSetting.DoubleRangeOption(newMinuteHandLengthRatio)
        )
    }

    // Saves User Style Option change back to the back to the EditorSession.
    private fun setUserStyleOption(
        userStyleSetting: UserStyleSetting,
        userStyleOption: UserStyleSetting.Option
    ) {
        Log.d(WatchFaceConfigActivity.TAG, "setUserStyleOption()")
        Log.d(WatchFaceConfigActivity.TAG, "\tuserStyleSetting: $userStyleSetting")
        Log.d(WatchFaceConfigActivity.TAG, "\tuserStyleOption: $userStyleOption")

        // Casts as a [HashMap], so we can update values.
        val hashmap =
            userStyle.selectedOptions as HashMap<UserStyleSetting, UserStyleSetting.Option>
        hashmap[userStyleSetting] = userStyleOption

        editorSession.userStyle = userStyle
        updatesWatchFacePreview()
    }

    fun onCleared() {
        editorSession.close()
    }

    sealed class EditWatchFaceUiState {
        data class Success(val userStylesAndPreview: UserStylesAndPreview) : EditWatchFaceUiState()
        data class Loading(val message: String) : EditWatchFaceUiState()
        data class Error(val exception: Throwable) : EditWatchFaceUiState()
    }

    data class UserStylesAndPreview(
        val colorStyleId: String,
        val ticksEnabled: Boolean,
        val minuteHandLength: Float,
        val previewImage: Bitmap
    )

    companion object {
        private const val TAG = "WatchFaceConfigViewModel"

        // To convert the double representing the arm length to valid float value in the range the
        // slider can support, we need to multiply the original value times 1,000.
        private const val MULTIPLE_FOR_SLIDER: Float = 1000f

        const val MINUTE_HAND_LENGTH_MINIMUM_FOR_SLIDER =
            MINUTE_HAND_LENGTH_FRACTION_MINIMUM * MULTIPLE_FOR_SLIDER

        const val MINUTE_HAND_LENGTH_MAXIMUM_FOR_SLIDER =
            MINUTE_HAND_LENGTH_FRACTION_MAXIMUM * MULTIPLE_FOR_SLIDER

        const val MINUTE_HAND_LENGTH_DEFAULT_FOR_SLIDER =
            MINUTE_HAND_LENGTH_FRACTION * MULTIPLE_FOR_SLIDER

        private fun multiplyByMultipleForSlider(lengthFraction: Double) =
            lengthFraction * MULTIPLE_FOR_SLIDER
    }
}
