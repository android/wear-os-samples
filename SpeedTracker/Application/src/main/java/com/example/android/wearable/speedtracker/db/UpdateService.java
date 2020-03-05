/*
 * Copyright (C) 2014 Google Inc. All Rights Reserved.
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

package com.example.android.wearable.speedtracker.db;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import android.net.Uri;
import android.util.Log;

import com.example.android.wearable.speedtracker.LocationDataManager;
import com.example.android.wearable.speedtracker.PhoneApplication;
import com.example.android.wearable.speedtracker.common.Constants;
import com.example.android.wearable.speedtracker.common.LocationEntry;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/**
 * A {@link com.google.android.gms.wearable.WearableListenerService} that is responsible for
 * reading location data that gets added to the Data Layer storage.
 */
public class UpdateService extends WearableListenerService {

    private static final String TAG = "UpdateService";
    private LocationDataManager mDataManager;

    private final Set<Uri> mToBeDeletedUris = new HashSet<>();

    @Override
    public void onCreate() {
        super.onCreate();
        mDataManager = ((PhoneApplication) getApplicationContext()).getDataManager();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

        // First check if any data items need to be deleted.
        synchronized (mToBeDeletedUris) {
            if (!mToBeDeletedUris.isEmpty()) {
                for (Uri dataItemUri : mToBeDeletedUris) {
                    removeWearableData(dataItemUri);
                }
            }
        }

        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {

                Uri dataItemUri = dataEvent.getDataItem().getUri();

                Log.d(TAG, "Received a data item with uri: " + dataItemUri.getPath());

                if (dataItemUri.getPath().startsWith(Constants.PATH)) {

                    DataMap dataMap =
                            DataMapItem.fromDataItem(dataEvent.getDataItem()).getDataMap();

                    double longitude = dataMap.getDouble(Constants.KEY_LONGITUDE);
                    double latitude = dataMap.getDouble(Constants.KEY_LATITUDE);
                    long time = dataMap.getLong(Constants.KEY_TIME);

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(time);

                    mDataManager.addPoint(new LocationEntry(calendar, latitude, longitude));

                    removeWearableData(dataItemUri);
                }
            }
        }
    }

    private void removeWearableData(Uri dataItemUri) {

        Task<Integer> dataDeleteTask =
                Wearable.getDataClient(getApplicationContext()).deleteDataItems(dataItemUri);

        dataDeleteTask.addOnSuccessListener(getIntegerOnSuccessListener());
        dataDeleteTask.addOnFailureListener(getOnFailureListener(dataItemUri));
    }

    private OnFailureListener getOnFailureListener(Uri dataItemUri) {

        return exception -> {
            Log.e(TAG, "Failed to delete item, exception: " + exception);

            synchronized (mToBeDeletedUris) {
                mToBeDeletedUris.add(dataItemUri);
            }
        };
    }

    private OnSuccessListener<Integer> getIntegerOnSuccessListener() {
        return resultItem -> {
            Log.d(TAG, "Data successfully deleted; Result: " + resultItem);
        };
    }
}
