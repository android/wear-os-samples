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
package com.example.android.wearable.runtimepermissions

import android.Manifest
import android.content.pm.PackageManager
import android.content.Intent
import android.hardware.SensorManager
import android.hardware.Sensor
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.android.wearable.runtimepermissions.common.Constants
import com.google.android.gms.wearable.*

/**
 * Handles all incoming requests for wear data (and permissions) from phone devices.
 */
class IncomingRequestWearService : WearableListenerService() {
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate()")
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG, "onMessageReceived(): $messageEvent")
        val messagePath = messageEvent.path
        if (messagePath == Constants.MESSAGE_PATH_WEAR) {
            val dataMap = DataMap.fromByteArray(messageEvent.data)
            val requestType = dataMap.getInt(Constants.KEY_COMM_TYPE)
            if (requestType == Constants.COMM_TYPE_REQUEST_PROMPT_PERMISSION) {
                promptUserForSensorPermission()
            } else if (requestType == Constants.COMM_TYPE_REQUEST_DATA) {
                respondWithSensorInformation()
            }
        }
    }

    private fun promptUserForSensorPermission() {
        Log.d(TAG, "promptUserForSensorPermission()")
        val sensorPermissionApproved =
            (ActivityCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)
                    == PackageManager.PERMISSION_GRANTED)
        if (sensorPermissionApproved) {
            val dataMap = DataMap()
            dataMap.putInt(
                Constants.KEY_COMM_TYPE,
                Constants.COMM_TYPE_RESPONSE_USER_APPROVED_PERMISSION
            )
            sendMessage(dataMap)
        } else {
            // Launch Activity to grant sensor permissions.
            val startIntent = Intent(this, MainWearActivity::class.java)
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startIntent.putExtra(
                MainWearActivity.Companion.EXTRA_PROMPT_PERMISSION_FROM_PHONE,
                true
            )
            startActivity(startIntent)
        }
    }

    private fun respondWithSensorInformation() {
        Log.d(TAG, "respondWithSensorInformation()")
        val sensorPermissionApproved =
            (ActivityCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)
                    == PackageManager.PERMISSION_GRANTED)
        if (!sensorPermissionApproved) {
            val dataMap = DataMap()
            dataMap.putInt(
                Constants.KEY_COMM_TYPE,
                Constants.COMM_TYPE_RESPONSE_PERMISSION_REQUIRED
            )
            sendMessage(dataMap)
        } else {
            /* To keep the sample simple, we are only displaying the number of sensors. You could do
             * something much more complicated.
             */
            val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
            val sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL)
            val numberOfSensorsOnDevice = sensorList.size
            val sensorSummary = "$numberOfSensorsOnDevice sensors on wear device(s)!"
            val dataMap = DataMap()
            dataMap.putInt(
                Constants.KEY_COMM_TYPE,
                Constants.COMM_TYPE_RESPONSE_DATA
            )
            dataMap.putString(Constants.KEY_PAYLOAD, sensorSummary)
            sendMessage(dataMap)
        }
    }

    private fun sendMessage(dataMap: DataMap) {
        Log.d(TAG, "sendMessage(): $dataMap")

        // Initial check of capabilities to find the phone.
        val capabilityInfoTask = Wearable.getCapabilityClient(this)
            .getCapability(Constants.CAPABILITY_PHONE_APP, CapabilityClient.FILTER_REACHABLE)
        capabilityInfoTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Capability request succeeded.")
                val capabilityInfo = task.result
                val phoneNodeId = capabilityInfo!!.nodes.firstOrNull()?.id
                if (phoneNodeId != null) {
                    // Instantiates clients without member variables, as clients are inexpensive to
                    // create. (They are cached and shared between GoogleApi instances.)
                    val sendMessageTask = Wearable.getMessageClient(
                        applicationContext
                    ).sendMessage(
                        phoneNodeId,
                        Constants.MESSAGE_PATH_PHONE,
                        dataMap.toByteArray()
                    )
                    sendMessageTask.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "Message sent successfully")
                        } else {
                            Log.d(TAG, "Message failed.")
                        }
                    }
                } else {
                    Log.d(TAG, "No phone node available.")
                }
            } else {
                Log.d(TAG, "Capability request failed to return any results.")
            }
        }
    }

    companion object {
        private const val TAG = "IncomingRequestService"
    }

    init {
        Log.d(TAG, "IncomingRequestWearService()")
    }
}