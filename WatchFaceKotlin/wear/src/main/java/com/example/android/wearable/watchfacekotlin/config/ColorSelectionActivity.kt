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

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.content.edit
import com.example.android.wearable.watchfacekotlin.R
import com.example.android.wearable.watchfacekotlin.databinding.ActivityColorSelectionConfigBinding
import com.example.android.wearable.watchfacekotlin.watchface.AnalogWatchFace
import java.util.ArrayList

/**
 * Allows user to select a color for component on the watch face (background, highlight, etc.) and
 * saves it to [android.content.SharedPreferences] for later use.
 */
class ColorSelectionActivity : ComponentActivity() {

    private lateinit var binding: ActivityColorSelectionConfigBinding
    private lateinit var colorSelectionRecyclerViewAdapter: ColorSelectionRecyclerViewAdapter

    private lateinit var sharedPref: SharedPreferences
    private lateinit var sharedPrefString: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = getSharedPreferences(
            AnalogWatchFace.analog_complication_preference_file_key,
            Context.MODE_PRIVATE)

        sharedPrefString = intent.getStringExtra(EXTRA_SHARED_PREF) ?: ""

        binding = ActivityColorSelectionConfigBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)

        colorSelectionRecyclerViewAdapter = ColorSelectionRecyclerViewAdapter(
            ColorSelectionRecyclerViewAdapter.ColorListener { color ->

                // Value is saved in [SharedPreferences] for watch face and config access.
                if (sharedPrefString.isNotEmpty()) {
                    sharedPref.edit { putInt(sharedPrefString, color) }

                    // Lets Complication Config Activity know there was an update to colors.
                    setResult(RESULT_OK)
                } else {
                    setResult(RESULT_CANCELED)
                }

                finish()
            })

        val colorOptionsIntArray = resources.getIntArray(R.array.material_colors_array)
        val colorOptions = colorOptionsIntArray.toCollection(ArrayList())

        colorSelectionRecyclerViewAdapter.submitList(colorOptions)

        binding.wearableRecyclerView.apply {
            // Aligns the first and last items on the list vertically centered on the screen.
            isEdgeItemsCenteringEnabled = true

            // Improves performance because we know changes in content do not change the layout size of
            // the RecyclerView.
            setHasFixedSize(true)
            adapter = colorSelectionRecyclerViewAdapter
        }
    }

    companion object {
        private const val TAG = "ColorSelectionActivity"

        const val EXTRA_SHARED_PREF = "com.example.android.wearable.watchfacekotlin.config.extra.EXTRA_SHARED_PREF"
    }
}
