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

import android.support.wearable.view.CircledImageView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.wearable.watchfacekotlin.R

/**
 * Provides color options for [RecyclerView] displayed within [ColorSelectionActivity].
 * Color options change appearance for the item specified on the watch face.
 */
class ColorSelectionRecyclerViewAdapter(
    private val clickListener: ColorListener
) : ListAdapter<Int, ColorSelectionRecyclerViewAdapter.ColorViewHolder>(IntItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        Log.d(TAG, "onCreateViewHolder(): viewType: $viewType")

        return ColorViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.color_config_list_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(viewHolder: ColorViewHolder, position: Int) {
        Log.d(TAG, "Element $position set.")

        viewHolder.setColor(getItem(position))
    }

    // Displays color option for an item on the watch face.
    inner class ColorViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        private val colorCircleImageView: CircledImageView =
            view.findViewById<View>(R.id.color) as CircledImageView

        init {
            view.setOnClickListener(this)
        }

        fun setColor(color: Int) {
            colorCircleImageView.setCircleColor(color)
        }

        override fun onClick(view: View) {
            val position = adapterPosition
            val color = getItem(position)

            Log.d(TAG, "Color: $color onClick() position: $position")

            clickListener.onClick(color)
        }
    }

    class IntItemCallback : DiffUtil.ItemCallback<Int>() {
        override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean {
            return oldItem == newItem
        }
    }

    class ColorListener(val clickListener: (color: Int) -> Unit) {
        fun onClick(color: Int) = clickListener(color)
    }

    companion object {
        private const val TAG: String = "ColorSelectionAdapter"
    }
}
