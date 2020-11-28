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

import android.content.Context
import android.graphics.Color

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

import com.example.android.wearable.watchfacekotlin.R
import com.example.android.wearable.watchfacekotlin.config.AnalogComplicationConfigActivity
import com.example.android.wearable.watchfacekotlin.config.AnalogComplicationConfigRecyclerViewAdapter
import com.example.android.wearable.watchfacekotlin.config.ColorSelectionActivity
import com.example.android.wearable.watchfacekotlin.watchface.AnalogComplicationWatchFaceService

import java.util.ArrayList

/**
 * Data represents different views for configuring the [AnalogComplicationWatchFaceService] watch
 * face's appearance and complications via [AnalogComplicationConfigActivity].
 */
object AnalogComplicationConfigData {
    /**
     * Returns Watch Face Service class associated with configuration Activity.
     */
    val watchFaceServiceClass: Class<*>
        get() = AnalogComplicationWatchFaceService::class.java

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

    /**
     * Includes all data to populate each of the 5 different custom
     * [ViewHolder] types in [AnalogComplicationConfigRecyclerViewAdapter].
     */
    fun dataToPopulateAdapter(context: Context): ArrayList<ConfigItemType> {
        val settingsConfigData = ArrayList<ConfigItemType>()

        // Data for watch face preview and complications UX in settings Activity.
        val complicationConfigItem: ConfigItemType =
            PreviewAndComplicationsConfigItem(R.drawable.add_complication)
        settingsConfigData.add(complicationConfigItem)

        // Data for "more options" UX in settings Activity.
        val moreOptionsConfigItem: ConfigItemType =
            MoreOptionsConfigItem(R.drawable.ic_expand_more_white_18dp)
        settingsConfigData.add(moreOptionsConfigItem)

        // Data for highlight/marker (second hand) color UX in settings Activity.
        val markerColorConfigItem: ConfigItemType = ColorConfigItem(
            context.getString(R.string.config_marker_color_label),
            R.drawable.icn_styles,
            context.getString(R.string.saved_marker_color),
            ColorSelectionActivity::class.java
        )
        settingsConfigData.add(markerColorConfigItem)

        // Data for Background color UX in settings Activity.
        val backgroundColorConfigItem: ConfigItemType = ColorConfigItem(
            context.getString(R.string.config_background_color_label),
            R.drawable.icn_styles,
            context.getString(R.string.saved_background_color),
            ColorSelectionActivity::class.java
        )
        settingsConfigData.add(backgroundColorConfigItem)

        // Data for 'Unread Notifications' UX (toggle) in settings Activity.
        val unreadNotificationsConfigItem: ConfigItemType = UnreadNotificationConfigItem(
            context.getString(R.string.config_unread_notifications_label),
            R.drawable.ic_notifications_white_24dp,
            R.drawable.ic_notifications_off_white_24dp,
            R.string.saved_unread_notifications_pref
        )
        settingsConfigData.add(unreadNotificationsConfigItem)

        // Data for background complications UX in settings Activity.
        val backgroundImageComplicationConfigItem: ConfigItemType =
            BackgroundComplicationConfigItem(
                context.getString(R.string.config_background_image_complication_label),
                R.drawable.ic_landscape_white
            )
        settingsConfigData.add(backgroundImageComplicationConfigItem)
        return settingsConfigData
    }

    /**
     * [AnalogComplicationConfigActivity] allows users to configure various items in
     * [AnalogComplicationWatchFaceService] watch face, e.g., background color, highlight color,
     * complications, etc.
     *
     * Each of these items are a row/[ViewHolder] in a [RecyclerView] powered by
     * [AnalogComplicationConfigRecyclerViewAdapter].
     *
     * The [ConfigItemType] interface is required to allow us to return the correct config item type
     * supported by the adapter with the correct data.
     */
    interface ConfigItemType {
        val configType: Int
    }

    /**
     * Data for Watch Face Preview with Complications Preview item in RecyclerView.
     */
    class PreviewAndComplicationsConfigItem internal constructor(val defaultComplicationResourceId: Int) :
        ConfigItemType {
        override val configType: Int
            get() = AnalogComplicationConfigRecyclerViewAdapter.TYPE_PREVIEW_AND_COMPLICATIONS_CONFIG

    }

    /**
     * Data for "more options" item in RecyclerView.
     */
    class MoreOptionsConfigItem constructor(val iconResourceId: Int) : ConfigItemType {
        override val configType: Int
            get() = AnalogComplicationConfigRecyclerViewAdapter.TYPE_MORE_OPTIONS

    }

    /**
     * Data for color picker item in RecyclerView.
     */
    class ColorConfigItem internal constructor(
        val name: String,
        val iconResourceId: Int,
        val sharedPrefString: String,
        activity: Class<ColorSelectionActivity>
    ) : ConfigItemType {
        private val activityToChoosePreference: Class<ColorSelectionActivity> = activity
        fun getActivityToChoosePreference(): Class<ColorSelectionActivity> {
            return activityToChoosePreference
        }

        override val configType: Int
            get() = AnalogComplicationConfigRecyclerViewAdapter.TYPE_COLOR_CONFIG
    }

    /**
     * Data for Unread Notification preference picker item in RecyclerView.
     */
    class UnreadNotificationConfigItem internal constructor(
        val name: String,
        val iconEnabledResourceId: Int,
        val iconDisabledResourceId: Int,
        val sharedPrefId: Int
    ) : ConfigItemType {
        override val configType: Int
            get() = AnalogComplicationConfigRecyclerViewAdapter.TYPE_UNREAD_NOTIFICATION_CONFIG

    }

    /**
     * Data for background image complication picker item in RecyclerView.
     */
    class BackgroundComplicationConfigItem internal constructor(
        val name: String,
        val iconResourceId: Int
    ) : ConfigItemType {
        override val configType: Int
            get() = AnalogComplicationConfigRecyclerViewAdapter.TYPE_BACKGROUND_COMPLICATION_IMAGE_CONFIG

    }
}
