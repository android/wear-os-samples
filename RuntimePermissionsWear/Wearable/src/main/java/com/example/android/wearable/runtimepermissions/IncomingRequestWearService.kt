/*
 * Copyright 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.wearable.runtimepermissions

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.android.wearable.runtimepermissions.common.Constants
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Handles all incoming requests for wear data (and permissions) from phone devices.
 */
class IncomingRequestWearService : WearableListenerService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG, "onMessageReceived(): $messageEvent")
        // Switch to handling the message event asynchronously for any additional work
        scope.launch {
            handleMessageEvent(messageEvent)
        }
    }

    private suspend fun handleMessageEvent(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            Constants.MESSAGE_PATH_WEAR -> {
                val dataMap = DataMap.fromByteArray(messageEvent.data)
                when (dataMap.getInt(Constants.KEY_COMM_TYPE)) {
                    Constants.COMM_TYPE_REQUEST_PROMPT_PERMISSION -> {
                        promptUserForSensorPermission()
                    }
                    Constants.COMM_TYPE_REQUEST_DATA -> {
                        respondWithSensorInformation()
                    }
                }
            }
        }
    }

    private suspend fun promptUserForSensorPermission() {
        Log.d(TAG, "promptUserForSensorPermission()")
        val sensorPermissionApproved =
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) ==
                PackageManager.PERMISSION_GRANTED
        if (sensorPermissionApproved) {
            sendMessage(
                DataMap().apply {
                    putInt(
                        Constants.KEY_COMM_TYPE,
                        Constants.COMM_TYPE_RESPONSE_USER_APPROVED_PERMISSION
                    )
                }
            )
        } else {
            // Launch Activity to grant sensor permissions.
            startActivity(
                Intent(this, MainWearActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra(MainWearActivity.EXTRA_PROMPT_PERMISSION_FROM_PHONE, true)
                }
            )
        }
    }

    private suspend fun respondWithSensorInformation() {
        Log.d(TAG, "respondWithSensorInformation()")
        val sensorPermissionApproved =
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) ==
                PackageManager.PERMISSION_GRANTED
        if (!sensorPermissionApproved) {
            sendMessage(
                DataMap().apply {
                    putInt(
                        Constants.KEY_COMM_TYPE,
                        Constants.COMM_TYPE_RESPONSE_PERMISSION_REQUIRED
                    )
                }
            )
        } else {
            val sensorSummary = this.sensorSummary()
            sendMessage(
                DataMap().apply {
                    putInt(
                        Constants.KEY_COMM_TYPE,
                        Constants.COMM_TYPE_RESPONSE_DATA
                    )
                    putString(Constants.KEY_PAYLOAD, sensorSummary)
                }
            )
        }
    }

    private suspend fun sendMessage(dataMap: DataMap) {
        Log.d(TAG, "sendMessage(): $dataMap")

        // Initial check of capabilities to find the phone.
        val capabilityInfo = try {
            Wearable.getCapabilityClient(this)
                .getCapability(Constants.CAPABILITY_PHONE_APP, CapabilityClient.FILTER_REACHABLE)
                .await()
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Log.d(TAG, "Capability request failed to return any results.")
            return
        }

        val phoneNodeId = capabilityInfo.nodes.firstOrNull()?.id

        if (phoneNodeId == null) {
            Log.d(TAG, "No phone node available.")
            return
        }

        try {
            Wearable.getMessageClient(
                applicationContext
            ).sendMessage(
                phoneNodeId,
                Constants.MESSAGE_PATH_PHONE,
                dataMap.toByteArray()
            ).await()

            Log.d(TAG, "Message sent successfully")
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Log.d(TAG, "Message failed.")
        }
    }

    companion object {
        private const val TAG = "IncomingRequestService"
    }
}
