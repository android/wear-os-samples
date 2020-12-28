/*
* Copyright (C) 2020 The Android Open Source Project
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
package com.example.android.wearable.watchfacekotlin.config

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.Icon
import android.support.wearable.complications.ComplicationHelperActivity
import android.support.wearable.complications.ComplicationProviderInfo
import android.support.wearable.complications.ProviderInfoRetriever
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.android.wearable.watchfacekotlin.watchface.AnalogComplicationWatchFaceService
import com.example.android.wearable.watchfacekotlin.watchface.AnalogWatchFace
import java.util.concurrent.Executors

/*
 * Pulls watch face user preferences from SharedPreferences and Complication data
 * [ComplicationProviderInfo] to allow preview/configuration in [AnalogComplicationConfigActivity].
 * This changes the watch face [AnalogComplicationWatchFaceService].
 */
class AnalogComplicationConfigViewModel(application: Application) : AndroidViewModel(application) {

    private var _backgroundComplicationEnabled = false

    val backgroundComplicationEnabled: Boolean
        get() = _backgroundComplicationEnabled

    // Selected complication id by user (default value is invalid [only changed when user taps to
    // change a complication]). We have to track this because when the complication service returns
    // the complication data selected, it doesn't give us the unique id we associate with the
    // location of the complication on our watch face.
    private var selectedComplicationId: Int = -1

    // Required to retrieve watch face complication data for preview.
    private val providerInfoRetriever: ProviderInfoRetriever =
        ProviderInfoRetriever(application.applicationContext, Executors.newCachedThreadPool())

    // ComponentName associated with watch face service, [AnalogComplicationWatchFaceService], that
    // renders the watch face. Used to retrieve complication information.
    private val watchFaceComponentName: ComponentName = ComponentName(
        application.applicationContext,
        AnalogComplicationWatchFaceService::class.java
    )

    // Used to retrieve the user's highlight color, background color, and unread notification icon
    // preferences.
    private val sharedPref: SharedPreferences = application.getSharedPreferences(
        AnalogWatchFace.analog_complication_preference_file_key,
        Context.MODE_PRIVATE
    )

    private val _leftComplication: MutableLiveData<ComplicationProviderInfo?> =
        MutableLiveData<ComplicationProviderInfo?>()

    val leftComplication: LiveData<ComplicationProviderInfo?>
        get() = _leftComplication

    private val _rightComplication: MutableLiveData<ComplicationProviderInfo?> =
        MutableLiveData<ComplicationProviderInfo?>()

    val rightComplication: LiveData<ComplicationProviderInfo?>
        get() = _rightComplication

    private val _highlightColor: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>().also {
            it.value = sharedPref.getInt(
                AnalogWatchFace.saved_highlight_color_pref,
                Color.RED
            )
        }
    }

    val highlightColor: LiveData<Int>
        get() = _highlightColor

    // We don't retrieve the background color from SharedPreferences because displaying it depends
    // on whether the background complication has data, that is, the user has selected an image
    // for the background. We check that (and all complication data) in the init{} block.
    private val _background: MutableLiveData<WatchFaceBackground> =
        MutableLiveData<WatchFaceBackground>()

    fun background(): LiveData<WatchFaceBackground> {
        return _background
    }

    private val _displayUnreadNotifications: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().also {
            it.value = sharedPref.getBoolean(
                AnalogWatchFace.saved_unread_notifications_pref,
                true
            )
        }
    }

    val displayUnreadNotifications: LiveData<Boolean>
        get() = _displayUnreadNotifications

    fun displayUnreadNotificationsIconToggled(isChecked: Boolean) {
        sharedPref.edit {
            putBoolean(
                AnalogWatchFace.saved_unread_notifications_pref,
                isChecked
            )
            apply()
        }
        _displayUnreadNotifications.value = isChecked
    }

    init {
        // Initialization of code to retrieve active complication data for the watch face.
        providerInfoRetriever.init()

        // Retrieves all unique complication Int ids associated with
        // [AnalogComplicationWatchFaceService].
        val complicationIds: IntArray = AnalogWatchFace.complicationIds

        // Loads Complication Data based on the ids above.
        providerInfoRetriever.retrieveProviderInfo(
            object : ProviderInfoRetriever.OnProviderInfoReceivedCallback() {
                override fun onProviderInfoReceived(
                    watchFaceComplicationId: Int,
                    complicationProviderInfo: ComplicationProviderInfo?
                ) {
                    Log.d(TAG, "onProviderInfoReceived: $complicationProviderInfo")
                    // Updates our MutableLiveData so the UI is updated in the Activity.
                    updateComplicationData(watchFaceComplicationId, complicationProviderInfo)
                }
            },
            watchFaceComponentName,
            *complicationIds
        )
    }

    /*
     * Called in activity when returning from [ColorSelectionActivity], that is,
     * when the user has selected a different color.
     */
    fun loadWatchFaceColors() {
        _highlightColor.value = sharedPref.getInt(
            AnalogWatchFace.saved_highlight_color_pref,
            Color.RED
        )

        // The background color only needs to be updated in UI when there isn't a background
        // complication active, that is, there isn't an image selected for the background of the
        // watch face.
        if (!_backgroundComplicationEnabled) {
            _background.value = WatchFaceBackground(
                sharedPref.getInt(
                    AnalogWatchFace.saved_background_color_pref,
                    Color.BLACK
                ),
                null
            )
        }
    }

    fun createHighlightColorLaunchIntent(context: Context) =
        createColorLaunchIntent(
            context,
            AnalogWatchFace.saved_highlight_color_pref
        )

    fun createBackgroundColorLaunchIntent(context: Context) =
        createColorLaunchIntent(
            context,
            AnalogWatchFace.saved_background_color_pref
        )

    private fun createColorLaunchIntent(context: Context, extraValue: String): Intent {
        return Intent(context, ColorSelectionActivity::class.java).apply {
            // Pass shared preference name to save color value.
            putExtra(
                ColorSelectionActivity.EXTRA_SHARED_PREF,
                extraValue
            )
        }
    }

    /** Updates the selected complication id saved earlier with the new information.  */
    fun updateSelectedComplicationData(complicationProviderInfo: ComplicationProviderInfo?) {
        Log.d(TAG, "updateComplicationData(): id: $selectedComplicationId")
        Log.d(TAG, "\tinfo: $complicationProviderInfo")

        updateComplicationData(selectedComplicationId, complicationProviderInfo)
    }

    /*
     * Updates the [MutableLiveData] data associated with the complication with new information.
     *
     * Triggered by initialization of complications via the class init{} block or when the user
     * chooses an individual complication to change.
     */
    private fun updateComplicationData(
        complicationId: Int,
        complicationProviderInfo: ComplicationProviderInfo?
    ) {

        Log.d(TAG, "updateComplicationData(): id: $complicationId")
        Log.d(TAG, "\tinfo: $complicationProviderInfo")

        when (complicationId) {
            AnalogWatchFace.Companion.ComplicationConfig.Left.id -> {
                _leftComplication.value = complicationProviderInfo
            }

            AnalogWatchFace.Companion.ComplicationConfig.Right.id -> {
                _rightComplication.value = complicationProviderInfo
            }

            AnalogWatchFace.Companion.ComplicationConfig.Background.id -> {
                if (complicationProviderInfo == null) {
                    _backgroundComplicationEnabled = false

                    // Since the background color covers the entire canvas, we clear the icon via
                    // setting it to null.
                    _background.value = WatchFaceBackground(
                        sharedPref.getInt(
                            AnalogWatchFace.saved_background_color_pref,
                            Color.BLACK),
                        null
                    )
                } else {
                    _backgroundComplicationEnabled = true

                    // Since we can't get the background image outside of the watch face as a live
                    // preview, we instead use the icon associated with the background complication
                    // and set the background to gray.
                    _background.value = WatchFaceBackground(
                        Color.GRAY,
                        complicationProviderInfo.providerIcon
                    )
                }
            }
            else -> {
                Log.d(TAG, "Complication id is invalid!")
            }
        }
    }

    fun createComplicationLaunchIntent(
        context: Context,
        selectedComplication: AnalogWatchFace.Companion.ComplicationConfig
    ): Intent {

        // Used by callback to update complication in the preview.
        // We need this because the returned complication data will not include the unique id
        // associated with the location of the complication.
        selectedComplicationId = selectedComplication.id

        return ComplicationHelperActivity.createProviderChooserHelperIntent(
            context,
            watchFaceComponentName,
            selectedComplication.id,
            *selectedComplication.supportedTypes
        )
    }

    // Used to cover both the background being a solid color only (color set but icon is null) or
    // an image set as the background via the complication data (color set with an icon).
    data class WatchFaceBackground(var color: Int, var icon: Icon?)

    companion object {
        private const val TAG = "AnalogConfigViewModel"
    }
}
