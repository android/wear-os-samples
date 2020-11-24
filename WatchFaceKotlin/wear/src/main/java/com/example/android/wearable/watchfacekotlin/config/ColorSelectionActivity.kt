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

import android.os.Bundle
import androidx.activity.ComponentActivity

import com.example.android.wearable.watchfacekotlin.databinding.ActivityColorSelectionConfigBinding
import com.example.android.wearable.watchfacekotlin.model.AnalogComplicationConfigData

/**
 * Allows user to select color for something on the watch face (background, highlight,etc.) and
 * saves it to [android.content.SharedPreferences] in RecyclerView.Adapter.
 */
class ColorSelectionActivity : ComponentActivity() {

    private lateinit var binding: ActivityColorSelectionConfigBinding
    private lateinit var colorSelectionRecyclerViewAdapter: ColorSelectionRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityColorSelectionConfigBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)

        // Assigns SharedPreference String used to save color selected.
        val sharedPrefString = intent.getStringExtra(EXTRA_SHARED_PREF)
        colorSelectionRecyclerViewAdapter = ColorSelectionRecyclerViewAdapter(
            sharedPrefString,
            AnalogComplicationConfigData.colorOptionsDataSet
        )

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
        const val EXTRA_SHARED_PREF =
            "com.example.android.wearable.watchfacekotlin.config.extra.EXTRA_SHARED_PREF"
    }
}
