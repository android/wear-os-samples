/*
 * Copyright (C) 2015 Google Inc. All Rights Reserved.
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

package com.example.android.wearable.runtimepermissions;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.example.android.wearable.runtimepermissions.common.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;
import java.util.Set;

/**
 * Handles all incoming requests for wear data (and permissions) from phone devices.
 */
public class IncomingRequestWearService extends WearableListenerService {

    private static final String TAG = "IncomingRequestService";

    public IncomingRequestWearService() {
        Log.d(TAG, "IncomingRequestWearService()");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, "onMessageReceived(): " + messageEvent);

        String messagePath = messageEvent.getPath();

        if (messagePath.equals(Constants.MESSAGE_PATH_WEAR)) {
            DataMap dataMap = DataMap.fromByteArray(messageEvent.getData());

            int requestType = dataMap.getInt(Constants.KEY_COMM_TYPE);

            if (requestType == Constants.COMM_TYPE_REQUEST_PROMPT_PERMISSION) {
                promptUserForSensorPermission();

            } else if (requestType == Constants.COMM_TYPE_REQUEST_DATA) {
                respondWithSensorInformation();
            }
        }
    }

    private void promptUserForSensorPermission() {
        Log.d(TAG, "promptUserForSensorPermission()");

        boolean sensorPermissionApproved =
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)
                        == PackageManager.PERMISSION_GRANTED;

        if (sensorPermissionApproved) {
            DataMap dataMap = new DataMap();
            dataMap.putInt(Constants.KEY_COMM_TYPE,
                    Constants.COMM_TYPE_RESPONSE_USER_APPROVED_PERMISSION);
            sendMessage(dataMap);
        } else {
            // Launch Activity to grant sensor permissions.
            Intent startIntent = new Intent(this, MainWearActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startIntent.putExtra(MainWearActivity.EXTRA_PROMPT_PERMISSION_FROM_PHONE, true);
            startActivity(startIntent);
        }
    }

    private void respondWithSensorInformation() {
        Log.d(TAG, "respondWithSensorInformation()");

        boolean sensorPermissionApproved =
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)
                        == PackageManager.PERMISSION_GRANTED;

        if (!sensorPermissionApproved) {
            DataMap dataMap = new DataMap();
            dataMap.putInt(Constants.KEY_COMM_TYPE,
                    Constants.COMM_TYPE_RESPONSE_PERMISSION_REQUIRED);
            sendMessage(dataMap);
        } else {
            /* To keep the sample simple, we are only displaying the number of sensors. You could do
             * something much more complicated.
             */
            SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
            int numberOfSensorsOnDevice = sensorList.size();

            String sensorSummary = numberOfSensorsOnDevice + " sensors on wear device(s)!";
            DataMap dataMap = new DataMap();
            dataMap.putInt(Constants.KEY_COMM_TYPE,
                    Constants.COMM_TYPE_RESPONSE_DATA);
            dataMap.putString(Constants.KEY_PAYLOAD, sensorSummary);
            sendMessage(dataMap);
        }
    }

    private void sendMessage(final DataMap dataMap) {

        Log.d(TAG, "sendMessage(): " + dataMap);

        // Initial check of capabilities to find the phone.
        Task<CapabilityInfo> capabilityInfoTask = Wearable.getCapabilityClient(this)
                .getCapability(Constants.CAPABILITY_PHONE_APP, CapabilityClient.FILTER_REACHABLE);

        capabilityInfoTask.addOnCompleteListener(new OnCompleteListener<CapabilityInfo>() {
             @Override
             public void onComplete(Task<CapabilityInfo> task) {

                 if (task.isSuccessful()) {
                     Log.d(TAG, "Capability request succeeded.");

                     CapabilityInfo capabilityInfo = task.getResult();
                     String phoneNodeId = pickBestNodeId(capabilityInfo.getNodes());

                     if (phoneNodeId != null) {
                         // Instantiates clients without member variables, as clients are inexpensive to
                         // create. (They are cached and shared between GoogleApi instances.)
                         Task<Integer> sendMessageTask =
                                 Wearable.getMessageClient(
                                         getApplicationContext()).sendMessage(
                                         phoneNodeId,
                                         Constants.MESSAGE_PATH_PHONE,
                                         dataMap.toByteArray());

                         sendMessageTask.addOnCompleteListener(new OnCompleteListener<Integer>() {
                             @Override
                             public void onComplete(Task<Integer> task) {
                                 if (task.isSuccessful()) {
                                     Log.d(TAG, "Message sent successfully");
                                 } else {
                                     Log.d(TAG, "Message failed.");
                                 }
                             }
                         });
                     } else {
                         Log.d(TAG, "No phone node available.");
                     }
                 } else {
                     Log.d(TAG, "Capability request failed to return any results.");
                 }
             }
         });
    }

    /*
     * There should only ever be one phone in a node set (much less w/ the correct capability), so
     * I am just grabbing the first one (which should be the only one).
     */
    private String pickBestNodeId(Set<Node> nodes) {

        Log.d(TAG, "pickBestNodeId: " + nodes);


        String bestNodeId = null;
        /* Find a nearby node or pick one arbitrarily. There should be only one phone connected
         * that supports this sample.
         */
        for (Node node : nodes) {
            if (node.isNearby()) {
                return node.getId();
            }
            bestNodeId = node.getId();
        }
        return bestNodeId;
    }
}
