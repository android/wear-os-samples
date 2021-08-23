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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private lateinit var editorSession: EditorSession

    // Preview of complication data (needed to render screenshots) and must be called in a
    // coroutine.
    private lateinit var previewComplicationData: Map<Int, ComplicationData>

    // Keys from Watch Face Data Structure
    private lateinit var colorStyleKey: UserStyleSetting.ListUserStyleSetting
    private lateinit var drawPipsKey: UserStyleSetting.BooleanUserStyleSetting
    private lateinit var minuteHandLengthKey: UserStyleSetting.DoubleRangeUserStyleSetting

    private val mutableUiState: MutableStateFlow<EditWatchFaceUiState> =
        MutableStateFlow(EditWatchFaceUiState.Loading("Initializing"))

    val uiState: StateFlow<EditWatchFaceUiState> = mutableUiState.asStateFlow()

    init {
        scope.launch(Dispatchers.Main.immediate) {
            editorSession = EditorSession.createOnWatchEditorSession(
                activity = activity,
                editIntent = editIntent
            )

            previewComplicationData = editorSession.getComplicationsPreviewData()
            Log.d(WatchFaceConfigActivity.TAG, "userStyle: $editorSession.userStyle")

            extractsUserStyles(editorSession.userStyle)
            updatesWatchFacePreview()
        }
    }

    private fun extractsUserStyles(newUserStyle: UserStyle) {
        // Loops through user styles and retrieves user editable styles.
        for (options: Map.Entry<UserStyleSetting, UserStyleSetting.Option> in newUserStyle) {
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
            editorSession.userStyle[colorStyleKey] as UserStyleSetting.ListUserStyleSetting.ListOption
        val ticksEnabledStyle =
            editorSession.userStyle[drawPipsKey] as UserStyleSetting.BooleanUserStyleSetting.BooleanOption
        val minuteHandStyle =
            editorSession.userStyle[minuteHandLengthKey] as UserStyleSetting.DoubleRangeUserStyleSetting.DoubleRangeOption

        Log.d(TAG, "/new values: $colorStyle, $ticksEnabledStyle, $minuteHandStyle")

        val watchFacePreview = UserStylesAndPreview(
            colorStyleId = colorStyle.id.toString(),
            ticksEnabled = ticksEnabledStyle.value,
            minuteHandLength = multiplyByMultipleForSlider(minuteHandStyle.value).toFloat(),
            previewImage = bitmap
        )

        mutableUiState.value = EditWatchFaceUiState.Success(watchFacePreview)
    }

    fun setColorStyle(newColorStyleId: String) {
        val userStyleSettingList = editorSession.userStyleSchema.userStyleSettings

        // Loops over all UserStyleSettings (basically the keys in the map) to find the setting for
        // the color style (which contains all the possible options for that style setting).
        for (userStyleSetting in userStyleSettingList) {
            if (userStyleSetting.id == UserStyleSetting.Id(COLOR_STYLE_SETTING)) {
                val colorUserStyleSetting =
                    userStyleSetting as UserStyleSetting.ListUserStyleSetting

                // Loops over the UserStyleSetting.Option colors (all possible values for the key)
                // to find the matching option, and if it exists, sets it as the color style.
                for (colorOptions in colorUserStyleSetting.options) {
                    if (colorOptions.id.toString() == newColorStyleId) {
                        setUserStyleOption(colorStyleKey, colorOptions)
                        return
                    }
                }
            }
        }
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

        val mutableUserStyle = editorSession.userStyle.toMutableUserStyle()
        mutableUserStyle[userStyleSetting] = userStyleOption
        editorSession.userStyle = mutableUserStyle.toUserStyle()
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
        private const val TAG = "WatchFaceConfigStateHolder"

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
