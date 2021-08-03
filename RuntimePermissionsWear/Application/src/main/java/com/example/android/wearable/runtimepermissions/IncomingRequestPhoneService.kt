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
import com.example.android.wearable.runtimepermissions.IncomingRequestPhoneService
import android.content.pm.PackageManager
import android.content.Intent
import com.example.android.wearable.runtimepermissions.PhonePermissionRequestActivity
import com.example.android.wearable.runtimepermissions.MainPhoneActivity
import android.os.Environment
import android.widget.TextView
import android.os.Bundle
import com.example.android.wearable.runtimepermissions.R
import android.app.Activity
import com.example.android.wearable.runtimepermissions.WearPermissionRequestActivity
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.android.wearable.runtimepermissions.common.Constants
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import java.lang.StringBuilder

/**
 * Handles all incoming requests for phone data (and permissions) from wear devices.
 */
class IncomingRequestPhoneService : WearableListenerService() {
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate()")
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        Log.d(TAG, "onMessageReceived(): $messageEvent")
        val messagePath = messageEvent.path
        if (messagePath == Constants.MESSAGE_PATH_PHONE) {
            val dataMap = DataMap.fromByteArray(messageEvent.data)
            val requestType = dataMap.getInt(Constants.KEY_COMM_TYPE, 0)
            if (requestType == Constants.COMM_TYPE_REQUEST_PROMPT_PERMISSION) {
                promptUserForStoragePermission(messageEvent.sourceNodeId)
            } else if (requestType == Constants.COMM_TYPE_REQUEST_DATA) {
                respondWithStorageInformation(messageEvent.sourceNodeId)
            }
        }
    }

    private fun promptUserForStoragePermission(nodeId: String) {
        val storagePermissionApproved =
            (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED)
        if (storagePermissionApproved) {
            val dataMap = DataMap()
            dataMap.putInt(
                Constants.KEY_COMM_TYPE,
                Constants.COMM_TYPE_RESPONSE_USER_APPROVED_PERMISSION
            )
            sendMessage(nodeId, dataMap)
        } else {
            // Launch Phone Activity to grant storage permissions.
            val startIntent = Intent(this, PhonePermissionRequestActivity::class.java)
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            /* This extra is included to alert MainPhoneActivity to send back the permission
             * results after the user has made their decision in PhonePermissionRequestActivity
             * and it finishes.
             */startIntent.putExtra(
                MainPhoneActivity.Companion.EXTRA_PROMPT_PERMISSION_FROM_WEAR,
                true
            )
            startActivity(startIntent)
        }
    }

    private fun respondWithStorageInformation(nodeId: String) {
        val storagePermissionApproved =
            (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED)
        if (!storagePermissionApproved) {
            val dataMap = DataMap()
            dataMap.putInt(
                Constants.KEY_COMM_TYPE,
                Constants.COMM_TYPE_RESPONSE_PERMISSION_REQUIRED
            )
            sendMessage(nodeId, dataMap)
        } else {
            /* To keep the sample simple, we are only displaying the top level list of directories.
             * Otherwise, it will return a message that the media wasn't available.
             */
            val stringBuilder = StringBuilder()
            if (isExternalStorageReadable) {
                val externalStorageDirectory = Environment.getExternalStorageDirectory()
                val fileList = externalStorageDirectory.list()
                if (fileList.size > 0) {
                    stringBuilder.append("List of directories on phone:\n")
                    for (file in fileList) {
                        stringBuilder.append(" - $file\n")
                    }
                } else {
                    stringBuilder.append("No files in external storage.")
                }
            } else {
                stringBuilder.append("No external media is available.")
            }

            // Send valid results
            val dataMap = DataMap()
            dataMap.putInt(
                Constants.KEY_COMM_TYPE,
                Constants.COMM_TYPE_RESPONSE_DATA
            )
            dataMap.putString(Constants.KEY_PAYLOAD, stringBuilder.toString())
            sendMessage(nodeId, dataMap)
        }
    }

    private fun sendMessage(nodeId: String, dataMap: DataMap) {
        Log.d(TAG, "sendMessage() Node: $nodeId")


        // Clients are inexpensive to create, so in this case we aren't creating member variables.
        // (They are cached and shared between GoogleApi instances.)
        val sendMessageTask = Wearable.getMessageClient(
            applicationContext
        ).sendMessage(
            nodeId,
            Constants.MESSAGE_PATH_WEAR,
            dataMap.toByteArray()
        )
        sendMessageTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Message sent successfully")
            } else {
                Log.d(TAG, "Message failed.")
            }
        }
    }

    private val isExternalStorageReadable: Boolean
        private get() {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state || Environment.MEDIA_MOUNTED_READ_ONLY == state
        }

    companion object {
        private const val TAG = "IncomingRequestService"
    }
}