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
package com.example.android.wearable.watchface.config;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.complications.ComplicationProviderInfo;
import android.support.wearable.complications.ProviderChooserIntent;
import android.util.Log;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import com.example.android.wearable.watchface.R;
import com.example.android.wearable.watchface.model.AnalogComplicationConfigData;
import com.example.android.wearable.watchface.watchface.AnalogComplicationWatchFaceService;

/**
 * The watch-side config activity for {@link AnalogComplicationWatchFaceService}, which
 * allows for setting the left and right complications of watch face along with the second's marker
 * color, background color, unread notifications toggle, and background complication image.
 */
public class AnalogComplicationConfigActivity extends Activity {

    private static final String TAG = AnalogComplicationConfigActivity.class.getSimpleName();

    static final int COMPLICATION_CONFIG_REQUEST_CODE = 1001;
    static final int UPDATE_COLORS_CONFIG_REQUEST_CODE = 1002;

    private WearableRecyclerView mWearableRecyclerView;
    private AnalogComplicationConfigRecyclerViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_analog_complication_config);

        mAdapter = new AnalogComplicationConfigRecyclerViewAdapter(
                getApplicationContext(),
                AnalogComplicationConfigData.getWatchFaceServiceClass(),
                AnalogComplicationConfigData.getDataToPopulateAdapter(this));

        mWearableRecyclerView =
                (WearableRecyclerView) findViewById(R.id.wearable_recycler_view);

        // Aligns the first and last items on the list vertically centered on the screen.
        mWearableRecyclerView.setEdgeItemsCenteringEnabled(true);

        mWearableRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Improves performance because we know changes in content do not change the layout size of
        // the RecyclerView.
        mWearableRecyclerView.setHasFixedSize(true);

        mWearableRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == COMPLICATION_CONFIG_REQUEST_CODE
                && resultCode == RESULT_OK) {

            // Retrieves information for selected Complication provider.
            ComplicationProviderInfo complicationProviderInfo =
                    data.getParcelableExtra(ProviderChooserIntent.EXTRA_PROVIDER_INFO);
            Log.d(TAG, "Provider: " + complicationProviderInfo);

            // Updates preview with new complication information for selected complication id.
            // Note: complication id is saved and tracked in the adapter class.
            mAdapter.updateSelectedComplication(complicationProviderInfo);

        } else if (requestCode == UPDATE_COLORS_CONFIG_REQUEST_CODE
                && resultCode == RESULT_OK) {

            // Updates highlight and background colors based on the user preference.
            mAdapter.updatePreviewColors();
        }
    }
}