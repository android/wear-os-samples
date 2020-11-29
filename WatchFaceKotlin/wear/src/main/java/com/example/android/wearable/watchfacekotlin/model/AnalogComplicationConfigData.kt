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
package com.example.android.wearable.watchfacekotlin.model

import android.graphics.Color

import java.util.ArrayList

/**
 * List of colors for watch faces.
 */
object AnalogComplicationConfigData {
    /**
     * Returns Material Design color options.
     */
    val colorOptionsDataSet: List<Int>
        get() {
            return ArrayList<Int>().apply {
                add(Color.parseColor("#FFFFFF")) // White

                add(Color.parseColor("#FFEB3B")) // Yellow
                add(Color.parseColor("#FFC107")) // Amber
                add(Color.parseColor("#FF9800")) // Orange
                add(Color.parseColor("#FF5722")) // Deep Orange

                add(Color.parseColor("#F44336")) // Red
                add(Color.parseColor("#E91E63")) // Pink

                add(Color.parseColor("#9C27B0")) // Purple
                add(Color.parseColor("#673AB7")) // Deep Purple
                add(Color.parseColor("#3F51B5")) // Indigo
                add(Color.parseColor("#2196F3")) // Blue
                add(Color.parseColor("#03A9F4")) // Light Blue

                add(Color.parseColor("#00BCD4")) // Cyan
                add(Color.parseColor("#009688")) // Teal
                add(Color.parseColor("#4CAF50")) // Green
                add(Color.parseColor("#8BC34A")) // Lime Green
                add(Color.parseColor("#CDDC39")) // Lime

                add(Color.parseColor("#607D8B")) // Blue Grey
                add(Color.parseColor("#9E9E9E")) // Grey
                add(Color.parseColor("#795548")) // Brown
                add(Color.parseColor("#000000")) // Black
            }
        }
}
