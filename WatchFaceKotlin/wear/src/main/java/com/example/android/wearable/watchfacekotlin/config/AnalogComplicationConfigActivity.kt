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

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.support.wearable.complications.ComplicationHelperActivity
import android.support.wearable.complications.ComplicationProviderInfo
import android.support.wearable.complications.ProviderChooserIntent
import android.support.wearable.complications.ProviderInfoRetriever
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.edit
import com.example.android.wearable.watchfacekotlin.R
import com.example.android.wearable.watchfacekotlin.databinding.ActivityAnalogComplicationConfigBinding

import com.example.android.wearable.watchfacekotlin.watchface.AnalogComplicationWatchFaceService
import java.util.concurrent.Executors

/**
 * The watch-side config activity for [AnalogComplicationWatchFaceService], which
 * allows for setting the left and right complications of watch face along with the second's marker
 * color, background color, unread notifications toggle, and background complication image.
 */
class AnalogComplicationConfigActivity : ComponentActivity() {

    private lateinit var binding: ActivityAnalogComplicationConfigBinding

    private var backgroundComplicationEnabled = false

    // Selected complication id by user (default value is invalid [only changed when user taps to
    // change complication]).
    private var selectedComplicationId: Int = -1

    // Required to retrieve complication data from watch face for preview.
    private lateinit var providerInfoRetriever: ProviderInfoRetriever

    // ComponentName associated with watch face service (service that renders watch face). Used
    // to retrieve complication information.
    private lateinit var watchFaceComponentName: ComponentName


    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAnalogComplicationConfigBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)

        /**
         * Returns Watch Face Service class associated with configuration Activity.
         */
        watchFaceComponentName =
            ComponentName(applicationContext, AnalogComplicationWatchFaceService::class.java)

        sharedPref = getSharedPreferences(
            getString(R.string.analog_complication_preference_file_key),
            Context.MODE_PRIVATE
        )

        providerInfoRetriever =
            ProviderInfoRetriever(applicationContext, Executors.newCachedThreadPool())

        // Initialization of code to retrieve active complication data for the watch face.
        providerInfoRetriever.init()

        initializesColorsAndComplications()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == COMPLICATION_CONFIG_REQUEST_CODE && resultCode == RESULT_OK) {
            // Retrieves information for selected Complication provider.
            val complicationProviderInfo: ComplicationProviderInfo? =
                data?.getParcelableExtra(ProviderChooserIntent.EXTRA_PROVIDER_INFO)
            Log.d(TAG, "Provider: $complicationProviderInfo")

            // Updates preview with new complication information for selected complication id.
            // Note: complication id is saved and tracked in the adapter class.
            updateSelectedComplication(complicationProviderInfo)

        } else if (requestCode == UPDATE_COLORS_CONFIG_REQUEST_CODE && resultCode == RESULT_OK) {
            // Updates highlight and background colors based on the user preference.
            updateWatchFaceColors()
        }
    }

    /** Updates the selected complication id saved earlier with the new information.  */
    private fun updateSelectedComplication(complicationProviderInfo: ComplicationProviderInfo?) {
        Log.d(TAG, "updateSelectedComplication()")

        // Checks if view is inflated and complication id is valid.
        if (selectedComplicationId >= 0) {
            updateComplicationViews(
                selectedComplicationId,
                complicationProviderInfo
            )
        }
    }

    fun updateComplicationViews(
        watchFaceComplicationId: Int, complicationProviderInfo: ComplicationProviderInfo?
    ) {
        Log.d(TAG, "updateComplicationViews(): id: $watchFaceComplicationId")
        Log.d(TAG, "\tinfo: $complicationProviderInfo")


        if (watchFaceComplicationId ==
            AnalogComplicationWatchFaceService.Companion.ComplicationConfig.Background.id) {
            if (complicationProviderInfo != null) {
                backgroundComplicationEnabled = true

                // Since we can't get the background complication image outside of the
                // watch face, we set the icon for that provider instead with a gray background.
                val backgroundColorFilter =
                    PorterDuffColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP)
                binding.preview.watchFaceBackground.background.colorFilter = backgroundColorFilter
                binding.preview.watchFaceBackground.setImageIcon(
                    complicationProviderInfo.providerIcon
                )
            } else {
                backgroundComplicationEnabled = false

                // Clears icon for background if it was present before.
                binding.preview.watchFaceBackground.setImageResource(
                    android.R.color.transparent
                )
                val backgroundSharedPrefString = getString(R.string.saved_background_color_pref)
                val currentBackgroundColor =
                    sharedPref.getInt(backgroundSharedPrefString, Color.BLACK)
                val backgroundColorFilter = PorterDuffColorFilter(
                    currentBackgroundColor, PorterDuff.Mode.SRC_ATOP
                )
                binding.preview.watchFaceBackground.background.colorFilter = backgroundColorFilter
            }
        } else if (watchFaceComplicationId ==
            AnalogComplicationWatchFaceService.Companion.ComplicationConfig.Left.id) {

            updateComplicationView(
                complicationProviderInfo,
                binding.preview.leftComplication,
                binding.preview.leftComplicationBackground
            )
        } else if (watchFaceComplicationId ==
            AnalogComplicationWatchFaceService.Companion.ComplicationConfig.Right.id) {

            updateComplicationView(
                complicationProviderInfo,
                binding.preview.rightComplication,
                binding.preview.rightComplicationBackground
            )
        }
    }

    private fun updateComplicationView(
        complicationProviderInfo: ComplicationProviderInfo?,
        button: ImageButton,
        background: ImageView
    ) {
        if (complicationProviderInfo != null) {
            button.setImageIcon(complicationProviderInfo.providerIcon)
            button.contentDescription = getString(
                R.string.edit_complication,
                complicationProviderInfo.appName + " " + complicationProviderInfo.providerName
            )
            background.visibility = View.VISIBLE

        } else {
            button.setImageDrawable(getDrawable(R.drawable.add_complication))
            button.contentDescription = getString(R.string.add_complication)
            background.visibility = View.INVISIBLE
        }
    }

    private fun initializesColorsAndComplications() {

        // Initializes highlight color (just second arm and part of complications).
        val highlightSharedPrefString = getString(R.string.saved_marker_color_pref)
        val currentHighlightColor = sharedPref.getInt(highlightSharedPrefString, Color.RED)

        val highlightColorFilter =
            PorterDuffColorFilter(currentHighlightColor, PorterDuff.Mode.SRC_ATOP)

        binding.preview.watchFaceBackground.background.colorFilter = highlightColorFilter


        // Initializes background color to gray (updates to color or complication icon based
        // on whether the background complication is live or not.
        val backgroundColorFilter = PorterDuffColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP)
        binding.preview.watchFaceBackground.background.colorFilter = backgroundColorFilter

        // Since user clicked on a switch, new state should be opposite of current state.
        val newState = sharedPref.getBoolean(
            getString(R.string.saved_unread_notifications_pref),
            true
        )

        binding.unreadNotificationSwitch.isChecked = newState

        if (newState) {
            binding.unreadNotificationSwitch.setCompoundDrawablesWithIntrinsicBounds(
                getDrawable(R.drawable.ic_notifications_white_24dp), null, null, null
            )

        } else {
            binding.unreadNotificationSwitch.setCompoundDrawablesWithIntrinsicBounds(
                getDrawable(R.drawable.ic_notifications_off_white_24dp), null, null, null
            )
        }

        val complicationIds: IntArray = AnalogComplicationWatchFaceService.complicationIds

        providerInfoRetriever.retrieveProviderInfo(
            object : ProviderInfoRetriever.OnProviderInfoReceivedCallback() {
                override fun onProviderInfoReceived(
                    watchFaceComplicationId: Int,
                    complicationProviderInfo: ComplicationProviderInfo?
                ) {
                    Log.d(TAG, "onProviderInfoReceived: $complicationProviderInfo")
                    updateComplicationViews(watchFaceComplicationId, complicationProviderInfo)
                }
            },
            watchFaceComponentName,
            *complicationIds
        )
    }

    private fun updateWatchFaceColors() {

        // Only update background colors for preview if background complications are disabled.
        if (!backgroundComplicationEnabled) {
            // Updates background color.
            val backgroundSharedPrefString = getString(R.string.saved_background_color_pref)
            val currentBackgroundColor =
                sharedPref.getInt(backgroundSharedPrefString, Color.BLACK)
            val backgroundColorFilter =
                PorterDuffColorFilter(currentBackgroundColor, PorterDuff.Mode.SRC_ATOP)
            binding.preview.watchFaceBackground.background.colorFilter = backgroundColorFilter

        } else {
            // Inform user that they need to disable background image for color to work.
            val text: CharSequence = "Selected image overrides background color."
            val duration = Toast.LENGTH_LONG
            val toast = Toast.makeText(this, text, duration)
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
        }

        // Updates highlight color (just second arm).
        val highlightSharedPrefString = getString(R.string.saved_marker_color_pref)
        val currentHighlightColor = sharedPref.getInt(highlightSharedPrefString, Color.RED)
        val highlightColorFilter =
            PorterDuffColorFilter(currentHighlightColor, PorterDuff.Mode.SRC_ATOP)
        binding.preview.watchFaceHighlight.background.colorFilter = highlightColorFilter
    }

    fun onClickLeftComplication(view: View) {
        Log.d(TAG, "onClickLeftComplication")
        launchComplicationHelperActivity(
            AnalogComplicationWatchFaceService.Companion.ComplicationConfig.Left
        )
    }

    fun onClickRightComplication(view: View) {
        Log.d(TAG, "onClickRightComplication")

        launchComplicationHelperActivity(
            AnalogComplicationWatchFaceService.Companion.ComplicationConfig.Right
        )
    }

    fun onClickMarkerColorButton(view: View) {
        Log.d(TAG, "onClickMarkerColorButton")

        val launchIntent = Intent(this, ColorSelectionActivity::class.java).apply {
            // Pass shared preference name to save color value to.
            putExtra(
                ColorSelectionActivity.EXTRA_SHARED_PREF,
                getString(R.string.saved_marker_color_pref)
            )
        }

        startActivityForResult(
            launchIntent,
            UPDATE_COLORS_CONFIG_REQUEST_CODE
        )
    }

    fun onClickBackgroundColorButton(view: View) {
        Log.d(TAG, "onClickBackgroundColorButton")

        val launchIntent = Intent(this, ColorSelectionActivity::class.java).apply {
            // Pass shared preference name to save color value to.
            putExtra(
                ColorSelectionActivity.EXTRA_SHARED_PREF,
                getString(R.string.saved_background_color_pref)
            )
        }

        startActivityForResult(
            launchIntent,
            UPDATE_COLORS_CONFIG_REQUEST_CODE
        )
    }


    fun onClickNotificationSwitch(view: View) {
        Log.d(TAG, "onClickNotificationSwitch")

        val newState = binding.unreadNotificationSwitch.isChecked

        if (newState) {
            binding.unreadNotificationSwitch.setCompoundDrawablesWithIntrinsicBounds(
                getDrawable(R.drawable.ic_notifications_white_24dp), null, null, null
            )

        } else {
            binding.unreadNotificationSwitch.setCompoundDrawablesWithIntrinsicBounds(
                getDrawable(R.drawable.ic_notifications_off_white_24dp), null, null, null
            )
        }

        // Since user clicked on a switch, new state should be opposite of current state.
        val sharedPrefResourceString = getString(R.string.saved_unread_notifications_pref)
        sharedPref.edit { putBoolean(sharedPrefResourceString, newState) }
    }

    fun onClickBackgroundComplication(view: View) {
        Log.d(TAG, "onClickBackgroundComplication")

        launchComplicationHelperActivity(
            AnalogComplicationWatchFaceService.Companion.ComplicationConfig.Background
        )
    }

    // Verifies the watch face supports the complication location, then launches the helper
    // class, so user can choose their complication data provider.
    private fun launchComplicationHelperActivity(
        selectedComplication: AnalogComplicationWatchFaceService.Companion.ComplicationConfig
    ) {

        if (selectedComplication.id >= 0) {

            selectedComplicationId = selectedComplication.id

            startActivityForResult(
                ComplicationHelperActivity.createProviderChooserHelperIntent(
                    this,
                    watchFaceComponentName,
                    selectedComplication.id,
                    *selectedComplication.supportedTypes
                ),
                COMPLICATION_CONFIG_REQUEST_CODE
            )
        } else {
            Log.d(TAG, "Complication not supported by watch face.")
        }
    }

    companion object {
        private const val TAG = "AnalogConfigActivity"
        const val COMPLICATION_CONFIG_REQUEST_CODE = 1001
        const val UPDATE_COLORS_CONFIG_REQUEST_CODE = 1002
    }
}
