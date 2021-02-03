/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.example.android.wearable.watchface;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.wear.remote.interactions.WatchFaceConfigIntentHelper;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.Wearable;

/**
 * The phone-side config activity for {@code DigitalWatchFaceService}. Like the watch-side config
 * activity ({@code DigitalWatchFaceWearableConfigActivity}), allows for setting the background
 * color. Additionally, enables setting the color for hour, minute and second digits.
 */
public class DigitalWatchFaceCompanionConfigActivity extends Activity
        implements OnCompleteListener<DataItem> {
    private static final String TAG = "DigitalWatchFaceConfig";

    // TODO: use the shared constants (needs covering all the samples with Gradle build model)
    private static final String KEY_BACKGROUND_COLOR = "BACKGROUND_COLOR";
    private static final String KEY_HOURS_COLOR = "HOURS_COLOR";
    private static final String KEY_MINUTES_COLOR = "MINUTES_COLOR";
    private static final String KEY_SECONDS_COLOR = "SECONDS_COLOR";
    private static final String PATH_WITH_FEATURE = "/watch_face_config/Digital";

    private String mPeerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_digital_watch_face_config);

        mPeerId = WatchFaceConfigIntentHelper.getPeerIdExtra(getIntent());

        ComponentName name = WatchFaceConfigIntentHelper.getWatchFaceComponentExtra(getIntent());
        TextView label = (TextView)findViewById(R.id.label);
        label.setText(label.getText() + " (" + name.getClassName() + ")");

        if (mPeerId != null) {
            DataClient dataClient = Wearable.getDataClient(getApplicationContext());

            Uri.Builder builder = new Uri.Builder();
            // Build request for DataItem with local node as authority.
            Uri uri = new Uri.Builder()
                    .scheme("wear")
                    .path(PATH_WITH_FEATURE)
                    .authority(mPeerId)
                    .build();

            Task<DataItem> getDataItemResponseTask = dataClient.getDataItem(uri);
            getDataItemResponseTask.addOnCompleteListener(this);

        } else {
            displayNoConnectedDeviceDialog();
        }
    }

    private void displayNoConnectedDeviceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String messageText = getResources().getString(R.string.title_no_device_connected);
        String okText = getResources().getString(R.string.ok_no_device_connected);
        builder.setMessage(messageText)
                .setCancelable(false)
                .setPositiveButton(okText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Sets up selected items for all pickers according to given {@code config} and sets up their
     * item selection listeners.
     *
     * @param config the {@code DigitalWatchFaceService} config {@link DataMap}. If null, the
     *         default items are selected.
     */
    private void setUpAllPickers(DataMap config) {
        setUpColorPickerSelection(R.id.background, KEY_BACKGROUND_COLOR, config,
                R.string.color_black);
        setUpColorPickerSelection(R.id.hours, KEY_HOURS_COLOR, config, R.string.color_white);
        setUpColorPickerSelection(R.id.minutes, KEY_MINUTES_COLOR, config, R.string.color_white);
        setUpColorPickerSelection(R.id.seconds, KEY_SECONDS_COLOR, config, R.string.color_gray);

        setUpColorPickerListener(R.id.background, KEY_BACKGROUND_COLOR);
        setUpColorPickerListener(R.id.hours, KEY_HOURS_COLOR);
        setUpColorPickerListener(R.id.minutes, KEY_MINUTES_COLOR);
        setUpColorPickerListener(R.id.seconds, KEY_SECONDS_COLOR);
    }

    private void setUpColorPickerSelection(int spinnerId, final String configKey, DataMap config,
            int defaultColorNameResId) {
        String defaultColorName = getString(defaultColorNameResId);
        int defaultColor = Color.parseColor(defaultColorName);
        int color;
        if (config != null) {
            color = config.getInt(configKey, defaultColor);
        } else {
            color = defaultColor;
        }
        Spinner spinner = (Spinner) findViewById(spinnerId);
        String[] colorNames = getResources().getStringArray(R.array.color_array);
        for (int i = 0; i < colorNames.length; i++) {
            if (Color.parseColor(colorNames[i]) == color) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void setUpColorPickerListener(int spinnerId, final String configKey) {
        Spinner spinner = (Spinner) findViewById(spinnerId);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                final String colorName = (String) adapterView.getItemAtPosition(pos);
                sendConfigUpdateMessageToWatch(configKey, Color.parseColor(colorName));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });
    }

    private void sendConfigUpdateMessageToWatch(String configKey, int color) {
        if (mPeerId != null) {
            DataMap config = new DataMap();
            config.putInt(configKey, color);
            byte[] rawData = config.toByteArray();

            MessageClient messageClient = Wearable.getMessageClient(getApplicationContext());
            messageClient.sendMessage(mPeerId, PATH_WITH_FEATURE, rawData);

            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Sent watch face config message: " + configKey + " -> "
                        + Integer.toHexString(color));
            }
        }
    }

    @Override
    public void onComplete(@NonNull Task<DataItem> dataItemTask) {
        if (dataItemTask.isSuccessful() && (dataItemTask.getResult() != null)) {

            DataItem configDataItem = dataItemTask.getResult();
            DataMapItem dataMapItem = DataMapItem.fromDataItem(configDataItem);
            DataMap config = dataMapItem.getDataMap();
            setUpAllPickers(config);

        } else {
            // If DataItem with the current config can't be retrieved, select the default items on
            // each picker.
            setUpAllPickers(null);
        }

    }
}
