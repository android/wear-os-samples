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

import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.support.wearable.complications.ComplicationProviderInfo
import android.support.wearable.complications.ProviderChooserIntent
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider

import com.example.android.wearable.watchfacekotlin.R
import com.example.android.wearable.watchfacekotlin.databinding.ActivityAnalogComplicationConfigBinding

import com.example.android.wearable.watchfacekotlin.watchface.AnalogComplicationWatchFaceService

/**
 * The watch-side config activity for [AnalogComplicationWatchFaceService] allows users to
 * set the left and right complications of watch face along with the highlight color (second hand +
 * complications), background color, unread notifications icon indicator, and background
 * complication image.
 */
class AnalogComplicationConfigActivity : ComponentActivity() {

    private lateinit var binding: ActivityAnalogComplicationConfigBinding
    private lateinit var viewModel:AnalogComplicationConfigViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAnalogComplicationConfigBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)

        viewModel =
            ViewModelProvider(this).get(AnalogComplicationConfigViewModel::class.java)

        viewModel.leftComplication.observe(this, {
            updateComplicationUI(
                it,
                binding.preview.leftComplication,
                binding.preview.leftComplicationBackground
            )
        })

        viewModel.rightComplication.observe(this, {
            updateComplicationUI(
                it,
                binding.preview.rightComplication,
                binding.preview.rightComplicationBackground
            )
        })

        viewModel.highlightColor.observe(this, {
            updateHighlightColorUI(it)
        })

        viewModel.background().observe(this, {
            updateBackgroundUI(it)
        })

        viewModel.displayUnreadNotifications.observe(this, {
            updateUnreadNotificationsIconUI(it)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == COMPLICATION_CONFIG_REQUEST_CODE && resultCode == RESULT_OK) {
            // Retrieves information for selected Complication provider.
            val complicationProviderInfo: ComplicationProviderInfo? =
                data?.getParcelableExtra(ProviderChooserIntent.EXTRA_PROVIDER_INFO)
            Log.d(TAG, "Provider: $complicationProviderInfo")

            // Updates preview with new complication information for selected complication id.
            // Note: complication id is saved and tracked in the ViewModel class.
            viewModel.updateSelectedComplicationData(complicationProviderInfo)

        } else if (requestCode == UPDATE_COLORS_CONFIG_REQUEST_CODE && resultCode == RESULT_OK) {
            // Updates highlight and background colors based on the user preference.
            viewModel.loadWatchFaceColors()
        }
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

    fun onClickHighlightColorButton(view: View) {
        Log.d(TAG, "onClickHighlightColorButton")

        val intent = viewModel.createHighlightColorLaunchIntent(this)

        startActivityForResult(
            intent,
            UPDATE_COLORS_CONFIG_REQUEST_CODE
        )
    }

    fun onClickBackgroundColorButton(view: View) {
        Log.d(TAG, "onClickBackgroundColorButton")

        if (viewModel.backgroundComplicationEnabled) {
            // Inform user that they need to disable background image to select a color.
            val text: CharSequence = "Disable background image before selecting a background color."
            val duration = Toast.LENGTH_LONG
            val toast = Toast.makeText(this, text, duration)
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()

        } else {
            val intent = viewModel.createBackgroundColorLaunchIntent(this)

            startActivityForResult(
                intent,
                UPDATE_COLORS_CONFIG_REQUEST_CODE
            )
        }
    }

    fun onClickUnreadNotificationIconSwitch(view: View) {
        Log.d(TAG, "onClickNotificationSwitch")

        viewModel.displayUnreadNotificationsIconToggled(
            binding.unreadNotificationIconSwitch.isChecked
        )
    }

    fun onClickBackgroundComplication(view: View) {
        Log.d(TAG, "onClickBackgroundComplication")

        launchComplicationHelperActivity(
            AnalogComplicationWatchFaceService.Companion.ComplicationConfig.Background
        )
    }

    private fun updateComplicationUI(
        complicationProviderInfo: ComplicationProviderInfo?,
        imageButton: ImageButton,
        background:ImageView) {

        if (complicationProviderInfo == null) {
            imageButton.setImageDrawable(getDrawable(R.drawable.add_complication))
            imageButton.contentDescription = getString(R.string.add_complication)
            background.visibility = View.INVISIBLE

        } else {
            imageButton.setImageIcon(complicationProviderInfo.providerIcon)
            imageButton.contentDescription = getString(
                R.string.edit_complication,
                complicationProviderInfo.appName + " " +
                        complicationProviderInfo.providerName
            )
            background.visibility = View.VISIBLE
        }
    }

    private fun updateHighlightColorUI(highlightColor: Int) {
        // Updates highlight color (just second arm but complications use this color as well in
        // live watch face).
        val highlightColorFilter = PorterDuffColorFilter(highlightColor, PorterDuff.Mode.SRC_ATOP)
        binding.preview.watchFaceHighlight.background.colorFilter = highlightColorFilter
    }

    private fun updateBackgroundUI(background: AnalogComplicationConfigViewModel.WatchFaceBackground) {
        if (background.icon == null) {
            // Clears icon for background if it was present before.
            binding.preview.watchFaceBackground.setImageResource(android.R.color.transparent)
        } else {
            binding.preview.watchFaceBackground.setImageIcon(background.icon)
        }

        val backgroundColorFilter =
            PorterDuffColorFilter(background.color, PorterDuff.Mode.SRC_ATOP)
        binding.preview.watchFaceBackground.background.colorFilter = backgroundColorFilter
    }

    private fun updateUnreadNotificationsIconUI(isChecked: Boolean) {
        binding.unreadNotificationIconSwitch.isChecked = isChecked

        if (isChecked) {
            binding.unreadNotificationIconSwitch.setCompoundDrawablesWithIntrinsicBounds(
                getDrawable(R.drawable.ic_notifications_white_24dp), null, null, null
            )

        } else {
            binding.unreadNotificationIconSwitch.setCompoundDrawablesWithIntrinsicBounds(
                getDrawable(R.drawable.ic_notifications_off_white_24dp), null, null, null
            )
        }
    }

    // After retrieving the specific intent for the complication the user selected, launches UI to
    // allow user to choose the data that will be displayed in that complication.
    private fun launchComplicationHelperActivity(
        selectedComplication: AnalogComplicationWatchFaceService.Companion.ComplicationConfig
    ) {
        val intent = viewModel.createComplicationLaunchIntent(
            this,
            selectedComplication
        )

        startActivityForResult(
            intent,
            COMPLICATION_CONFIG_REQUEST_CODE
        )
    }

    companion object {
        private const val TAG = "AnalogConfigActivity"
        const val COMPLICATION_CONFIG_REQUEST_CODE = 1001
        const val UPDATE_COLORS_CONFIG_REQUEST_CODE = 1002
    }
}
