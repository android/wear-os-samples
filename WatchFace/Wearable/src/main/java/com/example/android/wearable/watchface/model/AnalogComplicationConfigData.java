/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.example.android.wearable.watchface.model;

import android.content.Context;
import android.graphics.Color;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.example.android.wearable.watchface.R;
import com.example.android.wearable.watchface.config.AnalogComplicationConfigActivity;
import com.example.android.wearable.watchface.config.AnalogComplicationConfigRecyclerViewAdapter;
import com.example.android.wearable.watchface.config.ColorSelectionActivity;
import com.example.android.wearable.watchface.watchface.AnalogComplicationWatchFaceService;

import java.util.ArrayList;

/**
 * Data represents different views for configuring the
 * {@link AnalogComplicationWatchFaceService} watch face's appearance and complications
 * via {@link AnalogComplicationConfigActivity}.
 */
public class AnalogComplicationConfigData {


    /**
     * Interface all ConfigItems must implement so the {@link RecyclerView}'s Adapter associated
     * with the configuration activity knows what type of ViewHolder to inflate.
     */
    public interface ConfigItemType {
        int getConfigType();
    }

    /**
     * Returns Watch Face Service class associated with configuration Activity.
     */
    public static Class getWatchFaceServiceClass() {
        return AnalogComplicationWatchFaceService.class;
    }

    /**
     * Returns Material Design color options.
     */
    public static ArrayList<Integer> getColorOptionsDataSet() {
        ArrayList<Integer> colorOptionsDataSet = new ArrayList<>();
        colorOptionsDataSet.add(Color.parseColor("#FFFFFF")); // White

        colorOptionsDataSet.add(Color.parseColor("#FFEB3B")); // Yellow
        colorOptionsDataSet.add(Color.parseColor("#FFC107")); // Amber
        colorOptionsDataSet.add(Color.parseColor("#FF9800")); // Orange
        colorOptionsDataSet.add(Color.parseColor("#FF5722")); // Deep Orange

        colorOptionsDataSet.add(Color.parseColor("#F44336")); // Red
        colorOptionsDataSet.add(Color.parseColor("#E91E63")); // Pink

        colorOptionsDataSet.add(Color.parseColor("#9C27B0")); // Purple
        colorOptionsDataSet.add(Color.parseColor("#673AB7")); // Deep Purple
        colorOptionsDataSet.add(Color.parseColor("#3F51B5")); // Indigo
        colorOptionsDataSet.add(Color.parseColor("#2196F3")); // Blue
        colorOptionsDataSet.add(Color.parseColor("#03A9F4")); // Light Blue

        colorOptionsDataSet.add(Color.parseColor("#00BCD4")); // Cyan
        colorOptionsDataSet.add(Color.parseColor("#009688")); // Teal
        colorOptionsDataSet.add(Color.parseColor("#4CAF50")); // Green
        colorOptionsDataSet.add(Color.parseColor("#8BC34A")); // Lime Green
        colorOptionsDataSet.add(Color.parseColor("#CDDC39")); // Lime

        colorOptionsDataSet.add(Color.parseColor("#607D8B")); // Blue Grey
        colorOptionsDataSet.add(Color.parseColor("#9E9E9E")); // Grey
        colorOptionsDataSet.add(Color.parseColor("#795548")); // Brown
        colorOptionsDataSet.add(Color.parseColor("#000000")); // Black

        return colorOptionsDataSet;
    }

    /**
     * Includes all data to populate each of the 5 different custom
     * {@link ViewHolder} types in {@link AnalogComplicationConfigRecyclerViewAdapter}.
     */
    public static ArrayList<ConfigItemType> getDataToPopulateAdapter(Context context) {

        ArrayList<ConfigItemType> settingsConfigData = new ArrayList<>();

        // Data for watch face preview and complications UX in settings Activity.
        ConfigItemType complicationConfigItem =
                new PreviewAndComplicationsConfigItem(R.drawable.add_complication);
        settingsConfigData.add(complicationConfigItem);

        // Data for "more options" UX in settings Activity.
        ConfigItemType moreOptionsConfigItem =
                new MoreOptionsConfigItem(R.drawable.ic_expand_more_white_18dp);
        settingsConfigData.add(moreOptionsConfigItem);

        // Data for highlight/marker (second hand) color UX in settings Activity.
        ConfigItemType markerColorConfigItem =
                new ColorConfigItem(
                        context.getString(R.string.config_marker_color_label),
                        R.drawable.icn_styles,
                        context.getString(R.string.saved_marker_color),
                        ColorSelectionActivity.class);
        settingsConfigData.add(markerColorConfigItem);

        // Data for Background color UX in settings Activity.
        ConfigItemType backgroundColorConfigItem =
                new ColorConfigItem(
                        context.getString(R.string.config_background_color_label),
                        R.drawable.icn_styles,
                        context.getString(R.string.saved_background_color),
                        ColorSelectionActivity.class);
        settingsConfigData.add(backgroundColorConfigItem);

        // Data for 'Unread Notifications' UX (toggle) in settings Activity.
        ConfigItemType unreadNotificationsConfigItem =
                new UnreadNotificationConfigItem(
                        context.getString(R.string.config_unread_notifications_label),
                        R.drawable.ic_notifications_white_24dp,
                        R.drawable.ic_notifications_off_white_24dp,
                        R.string.saved_unread_notifications_pref);
        settingsConfigData.add(unreadNotificationsConfigItem);

        // Data for background complications UX in settings Activity.
        ConfigItemType backgroundImageComplicationConfigItem =
                // TODO (jewalker): Revised in another CL to support background complication.
                new BackgroundComplicationConfigItem(
                        context.getString(R.string.config_background_image_complication_label),
                        R.drawable.ic_landscape_white);
        settingsConfigData.add(backgroundImageComplicationConfigItem);

        return settingsConfigData;
    }

    /**
     * Data for Watch Face Preview with Complications Preview item in RecyclerView.
     */
    public static class PreviewAndComplicationsConfigItem implements ConfigItemType {

        private int defaultComplicationResourceId;

        PreviewAndComplicationsConfigItem(int defaultComplicationResourceId) {
            this.defaultComplicationResourceId = defaultComplicationResourceId;
        }

        public int getDefaultComplicationResourceId() {
            return defaultComplicationResourceId;
        }

        @Override
        public int getConfigType() {
            return AnalogComplicationConfigRecyclerViewAdapter.TYPE_PREVIEW_AND_COMPLICATIONS_CONFIG;
        }
    }

    /**
     * Data for "more options" item in RecyclerView.
     */
    public static class MoreOptionsConfigItem implements ConfigItemType {

        private int iconResourceId;

        MoreOptionsConfigItem(int iconResourceId) {
            this.iconResourceId = iconResourceId;
        }

        public int getIconResourceId() {
            return iconResourceId;
        }

        @Override
        public int getConfigType() {
            return AnalogComplicationConfigRecyclerViewAdapter.TYPE_MORE_OPTIONS;
        }
    }

    /**
     * Data for color picker item in RecyclerView.
     */
    public static class ColorConfigItem  implements ConfigItemType {

        private String name;
        private int iconResourceId;
        private String sharedPrefString;
        private Class<ColorSelectionActivity> activityToChoosePreference;

        ColorConfigItem(
                String name,
                int iconResourceId,
                String sharedPrefString,
                Class<ColorSelectionActivity> activity) {
            this.name = name;
            this.iconResourceId = iconResourceId;
            this.sharedPrefString = sharedPrefString;
            this.activityToChoosePreference = activity;
        }

        public String getName() {
            return name;
        }

        public int getIconResourceId() {
            return iconResourceId;
        }

        public String getSharedPrefString() {
            return sharedPrefString;
        }

        public Class<ColorSelectionActivity> getActivityToChoosePreference() {
            return activityToChoosePreference;
        }

        @Override
        public int getConfigType() {
            return AnalogComplicationConfigRecyclerViewAdapter.TYPE_COLOR_CONFIG;
        }
    }

    /**
     * Data for Unread Notification preference picker item in RecyclerView.
     */
    public static class UnreadNotificationConfigItem  implements ConfigItemType {

        private String name;
        private int iconEnabledResourceId;
        private int iconDisabledResourceId;
        private int sharedPrefId;

        UnreadNotificationConfigItem(
                String name,
                int iconEnabledResourceId,
                int iconDisabledResourceId,
                int sharedPrefId) {
            this.name = name;
            this.iconEnabledResourceId = iconEnabledResourceId;
            this.iconDisabledResourceId = iconDisabledResourceId;
            this.sharedPrefId = sharedPrefId;
        }

        public String getName() {
            return name;
        }

        public int getIconEnabledResourceId() {
            return iconEnabledResourceId;
        }

        public int getIconDisabledResourceId() {
            return iconDisabledResourceId;
        }

        public int getSharedPrefId() {
            return sharedPrefId;
        }

        @Override
        public int getConfigType() {
            return AnalogComplicationConfigRecyclerViewAdapter.TYPE_UNREAD_NOTIFICATION_CONFIG;
        }
    }

    /**
     * Data for background image complication picker item in RecyclerView.
     */
    public static class BackgroundComplicationConfigItem  implements ConfigItemType {

        private String name;
        private int iconResourceId;

        BackgroundComplicationConfigItem(
                String name,
                int iconResourceId) {

            this.name = name;
            this.iconResourceId = iconResourceId;
        }

        public String getName() {
            return name;
        }

        public int getIconResourceId() {
            return iconResourceId;
        }

        @Override
        public int getConfigType() {
            return AnalogComplicationConfigRecyclerViewAdapter.TYPE_BACKGROUND_COMPLICATION_IMAGE_CONFIG;
        }
    }
}