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
import android.os.Bundle
import android.support.wearable.complications.ComplicationProviderInfo
import android.support.wearable.complications.ProviderChooserIntent
import android.util.Log

import androidx.activity.ComponentActivity

import com.example.android.wearable.watchfacekotlin.databinding.ActivityAnalogComplicationConfigBinding
import com.example.android.wearable.watchfacekotlin.model.AnalogComplicationConfigData
import com.example.android.wearable.watchfacekotlin.watchface.AnalogComplicationWatchFaceService

/**
 * The watch-side config activity for [AnalogComplicationWatchFaceService], which
 * allows for setting the left and right complications of watch face along with the second's marker
 * color, background color, unread notifications toggle, and background complication image.
 */
class AnalogComplicationConfigActivity : ComponentActivity() {

    private lateinit var binding: ActivityAnalogComplicationConfigBinding
    private lateinit var adapter: AnalogComplicationConfigRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAnalogComplicationConfigBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)

        adapter = AnalogComplicationConfigRecyclerViewAdapter(
            applicationContext,
            AnalogComplicationConfigData.watchFaceServiceClass,
            AnalogComplicationConfigData.dataToPopulateAdapter(this)
        )

        binding.wearableRecyclerView.apply {
            // Aligns the first and last items on the list vertically centered on the screen.
            isEdgeItemsCenteringEnabled = true

            // Improves performance because we know changes in content do not change the layout size of
            // the RecyclerView.
            setHasFixedSize(true)
            adapter = this@AnalogComplicationConfigActivity.adapter
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == COMPLICATION_CONFIG_REQUEST_CODE && resultCode == RESULT_OK) {
            // Retrieves information for selected Complication provider.
            val complicationProviderInfo: ComplicationProviderInfo? =
                data?.getParcelableExtra(ProviderChooserIntent.EXTRA_PROVIDER_INFO)
            Log.d(TAG, "Provider: $complicationProviderInfo")

            // Updates preview with new complication information for selected complication id.
            // Note: complication id is saved and tracked in the adapter class.
            adapter.updateSelectedComplication(complicationProviderInfo)

        } else if (requestCode == UPDATE_COLORS_CONFIG_REQUEST_CODE && resultCode == RESULT_OK) {
            // Updates highlight and background colors based on the user preference.
            adapter.updatePreviewColors()
        }
    }

    companion object {
        private const val TAG = "AnalogConfigActivity"
        const val COMPLICATION_CONFIG_REQUEST_CODE = 1001
        const val UPDATE_COLORS_CONFIG_REQUEST_CODE = 1002
    }
}
