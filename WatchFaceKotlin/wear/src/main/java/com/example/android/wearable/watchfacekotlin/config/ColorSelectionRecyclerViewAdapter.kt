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

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.support.wearable.view.CircledImageView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit

import androidx.recyclerview.widget.RecyclerView

import com.example.android.wearable.watchfacekotlin.R

/**
 * Provides a binding from color selection data set to views that are displayed within
 * [ColorSelectionActivity].
 * Color options change appearance for the item specified on the watch face. Value is saved to a
 * [SharedPreferences] value passed to the class.
 */
class ColorSelectionRecyclerViewAdapter(
    private val sharedPrefString: String?,
    private val colorOptionsDataSet: List<Int>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.d(TAG, "onCreateViewHolder(): viewType: $viewType")
        return ColorViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.color_config_list_item,
                parent,
                false)
        )
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        Log.d(TAG, "Element $position set.")
        val color = colorOptionsDataSet[position]
        val colorViewHolder: ColorViewHolder = viewHolder as ColorViewHolder
        colorViewHolder.setColor(color)
    }

    override fun getItemCount(): Int {
        return colorOptionsDataSet.size
    }

    /**
     * Displays color options for an item on the watch face and saves value to the
     * SharedPreference associated with it.
     */
    inner class ColorViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        private val colorCircleImageView: CircledImageView

        init {
            colorCircleImageView = view.findViewById<View>(R.id.color) as CircledImageView
            view.setOnClickListener(this)
        }

        fun setColor(color: Int) {
            colorCircleImageView.setCircleColor(color)
        }

        override fun onClick(view: View) {
            val position = adapterPosition
            val color = colorOptionsDataSet[position]
            Log.d(TAG, "Color: $color onClick() position: $position")

            val activity = view.context as Activity
            if (sharedPrefString != null && !sharedPrefString.isEmpty()) {
                val sharedPref = activity.getSharedPreferences(
                    activity.getString(R.string.analog_complication_preference_file_key),
                    Context.MODE_PRIVATE
                )
                sharedPref.edit { putInt(sharedPrefString, color) }

                // Let's Complication Config Activity know there was an update to colors.
                activity.setResult(Activity.RESULT_OK)
            }
            activity.finish()
        }

    }

    companion object {
        private val TAG: String = ColorSelectionRecyclerViewAdapter::class.java.getSimpleName()
    }
}
