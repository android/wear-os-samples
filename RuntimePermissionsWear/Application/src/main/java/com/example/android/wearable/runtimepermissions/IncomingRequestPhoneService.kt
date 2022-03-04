/*
 * Copyright 2021 The Android Open Source Project
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

import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.android.wearable.runtimepermissions.common.Constants
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
 * Handles all incoming requests for phone data (and permissions) from wear devices.
 */
class IncomingRequestPhoneService : WearableListenerService() {
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
            Constants.MESSAGE_PATH_PHONE -> {
                val dataMap = DataMap.fromByteArray(messageEvent.data)
                when (dataMap.getInt(Constants.KEY_COMM_TYPE)) {
                    Constants.COMM_TYPE_REQUEST_PROMPT_PERMISSION -> {
                        promptUserForPhonePermission(messageEvent.sourceNodeId)
                    }
                    Constants.COMM_TYPE_REQUEST_DATA -> {
                        respondWithPhoneInformation(messageEvent.sourceNodeId)
                    }
                }
            }
        }
    }

    private suspend fun promptUserForPhonePermission(nodeId: String) {
        val phoneInfoPermissionApproved =
            ActivityCompat.checkSelfPermission(
                this,
                phoneSummaryPermission
            ) == PackageManager.PERMISSION_GRANTED
        if (phoneInfoPermissionApproved) {
            sendMessage(
                nodeId,
                DataMap().apply {
                    putInt(
                        Constants.KEY_COMM_TYPE,
                        Constants.COMM_TYPE_RESPONSE_USER_APPROVED_PERMISSION
                    )
                }
            )
        } else {
            // Launch Phone Activity to grant phone information permissions.
            startActivity(
                Intent(this, MainPhoneActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                    // This extra is included to alert MainPhoneActivity to send back the permission
                    // results after the user has made their decision in PhonePermissionRequestActivity
                    // and it finishes.
                    putExtra(
                        MainPhoneActivity.EXTRA_PROMPT_PERMISSION_FROM_WEAR,
                        true
                    )
                }
            )
        }
    }

    private suspend fun respondWithPhoneInformation(nodeId: String) {
        val phoneInfoPermissionApproved =
            ActivityCompat.checkSelfPermission(this, phoneSummaryPermission) ==
                PackageManager.PERMISSION_GRANTED
        if (!phoneInfoPermissionApproved) {
            sendMessage(
                nodeId = nodeId,
                dataMap = DataMap().apply {
                    putInt(
                        Constants.KEY_COMM_TYPE,
                        Constants.COMM_TYPE_RESPONSE_PERMISSION_REQUIRED
                    )
                }
            )
        } else {
            // Send valid results
            sendMessage(
                nodeId = nodeId,
                dataMap = DataMap().apply {
                    putInt(
                        Constants.KEY_COMM_TYPE,
                        Constants.COMM_TYPE_RESPONSE_DATA
                    )
                    // To keep the sample simple, we are just trying to return the phone number.
                    putString(Constants.KEY_PAYLOAD, getPhoneSummary())
                }
            )
        }
    }

    private suspend fun sendMessage(nodeId: String, dataMap: DataMap) {
        Log.d(TAG, "sendMessage() Node: $nodeId")

        try {
            // Clients are inexpensive to create, so in this case we aren't creating member
            // variables. (They are cached and shared between GoogleApi instances.)
            Wearable.getMessageClient(applicationContext)
                .sendMessage(
                    nodeId,
                    Constants.MESSAGE_PATH_WEAR,
                    dataMap.toByteArray()
                )
                .await()
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
