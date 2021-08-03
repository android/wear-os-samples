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
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.android.wearable.runtimepermissions.common.Constants
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import java.lang.StringBuilder

/**
 * Displays data that requires runtime permissions both locally (READ_EXTERNAL_STORAGE) and
 * remotely on wear (BODY_SENSORS).
 *
 * The class also handles sending back the results of a permission request from a remote wear device
 * when the permission has not been approved yet on the phone (uses EXTRA as trigger). In that case,
 * the IncomingRequestPhoneService launches the splash Activity (PhonePermissionRequestActivity) to
 * inform user of permission request. After the user decides what to do, it falls back to this
 * Activity (which has all the GoogleApiClient code) to handle sending data across and keeps user
 * in app experience.
 */
class MainPhoneActivity : AppCompatActivity(), CapabilityClient.OnCapabilityChangedListener,
    MessageClient.OnMessageReceivedListener, OnCompleteListener<Int?> {
    private var mWearBodySensorsPermissionApproved = false
    private var mPhoneStoragePermissionApproved = false
    private var mWearRequestingPhoneStoragePermission = false
    private var mWearBodySensorsPermissionButton: Button? = null
    private var mPhoneStoragePermissionButton: Button? = null
    private var mOutputTextView: TextView? = null
    private var mWearNodeIds: Set<Node>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate()")
        super.onCreate(savedInstanceState)

        /*
         * Since this is a remote permission, we initialize it to false and then check the remote
         * permission once the GoogleApiClient is connected.
         */mWearBodySensorsPermissionApproved = false
        setContentView(R.layout.activity_main)

        // Checks if wear app requested phone permission (permission request opens later if true).
        mWearRequestingPhoneStoragePermission = intent.getBooleanExtra(
            EXTRA_PROMPT_PERMISSION_FROM_WEAR, false
        )
        mPhoneStoragePermissionButton = findViewById(R.id.phoneStoragePermissionButton)
        mWearBodySensorsPermissionButton = findViewById(R.id.wearBodySensorsPermissionButton)
        mOutputTextView = findViewById(R.id.output)
    }

    fun onClickWearBodySensors(view: View?) {
        logToUi("Requested info from wear device(s). New approval may be required.")
        val dataMap = DataMap()
        dataMap.putInt(Constants.KEY_COMM_TYPE, Constants.COMM_TYPE_REQUEST_DATA)
        sendMessage(dataMap)
    }

    fun onClickPhoneStorage(view: View?) {
        if (mPhoneStoragePermissionApproved) {
            logToUi(phoneStorageInformation)
        } else {
            // On 23+ (M+) devices, Storage permission not granted. Request permission.
            val startIntent = Intent(this, PhonePermissionRequestActivity::class.java)
            startActivity(startIntent)
        }
    }

    override fun onPause() {
        Log.d(TAG, "onPause()")
        super.onPause()
        Wearable.getMessageClient(this).removeListener(this)
        Wearable.getCapabilityClient(this).removeListener(this)
    }

    override fun onResume() {
        Log.d(TAG, "onResume()")
        super.onResume()

        /* Enables app to handle 23+ (M+) style permissions. It also covers user changing
         * permission in settings and coming back to the app.
         */mPhoneStoragePermissionApproved =
            (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED)
        if (mPhoneStoragePermissionApproved) {
            mPhoneStoragePermissionButton!!.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_permission_approved, 0, 0, 0
            )
        }

        // Clients are inexpensive to create, so in this case we aren't creating member variables.
        // (They are cached and shared between GoogleApi instances.)
        Wearable.getMessageClient(this).addListener(this)
        Wearable.getCapabilityClient(this).addListener(
            this, Constants.CAPABILITY_WEAR_APP
        )

        // Initial check of capabilities to find the phone.
        val capabilityInfoTask = Wearable.getCapabilityClient(this)
            .getCapability(Constants.CAPABILITY_WEAR_APP, CapabilityClient.FILTER_REACHABLE)
        capabilityInfoTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Capability request succeeded.")
                val capabilityInfo = task.result
                val capabilityName = capabilityInfo!!.name
                val wearSupportsSampleApp = capabilityName == Constants.CAPABILITY_WEAR_APP
                if (wearSupportsSampleApp) {
                    mWearNodeIds = capabilityInfo.nodes

                    /*
                             * Upon getting all wear nodes, we now need to check if the original
                             * request to launch this activity (and PhonePermissionRequestActivity) was
                             * initiated by a wear device. If it was, we need to send back the
                             * permission results (data or rejection of permission) to the wear device.
                             *
                             * Also, note we set variable to false, this enables the user to continue
                             * changing permissions without sending updates to the wear every time.
                             */if (mWearRequestingPhoneStoragePermission) {
                        mWearRequestingPhoneStoragePermission = false
                        sendWearPermissionResults()
                    }
                }
            } else {
                Log.d(TAG, "Capability request failed to return any results.")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult()")
        if (requestCode == REQUEST_WEAR_PERMISSION_RATIONALE) {
            if (resultCode == RESULT_OK) {
                logToUi("Requested permission on wear device(s).")
                val dataMap = DataMap()
                dataMap.putInt(
                    Constants.KEY_COMM_TYPE,
                    Constants.COMM_TYPE_REQUEST_PROMPT_PERMISSION
                )
                sendMessage(dataMap)
            }
        }
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        Log.d(TAG, "onCapabilityChanged(): $capabilityInfo")
        mWearNodeIds = capabilityInfo.nodes
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG, "onMessageReceived(): $messageEvent")
        val messagePath = messageEvent.path
        if (messagePath == Constants.MESSAGE_PATH_PHONE) {
            val dataMap = DataMap.fromByteArray(messageEvent.data)
            val commType = dataMap.getInt(Constants.KEY_COMM_TYPE, 0)
            if (commType == Constants.COMM_TYPE_RESPONSE_PERMISSION_REQUIRED) {
                mWearBodySensorsPermissionApproved = false
                updateWearButtonOnUiThread()

                /* Because our request for remote data requires a remote permission, we now launch
                 * a splash activity informing the user we need those permissions (along with
                 * other helpful information to approve).
                 */
                val wearPermissionRationale =
                    Intent(this, WearPermissionRequestActivity::class.java)
                startActivityForResult(wearPermissionRationale, REQUEST_WEAR_PERMISSION_RATIONALE)
            } else if (commType == Constants.COMM_TYPE_RESPONSE_USER_APPROVED_PERMISSION) {
                mWearBodySensorsPermissionApproved = true
                updateWearButtonOnUiThread()
                logToUi("User approved permission on remote device, requesting data again.")
                val outgoingDataRequestDataMap = DataMap()
                outgoingDataRequestDataMap.putInt(
                    Constants.KEY_COMM_TYPE,
                    Constants.COMM_TYPE_REQUEST_DATA
                )
                sendMessage(outgoingDataRequestDataMap)
            } else if (commType == Constants.COMM_TYPE_RESPONSE_USER_DENIED_PERMISSION) {
                mWearBodySensorsPermissionApproved = false
                updateWearButtonOnUiThread()
                logToUi("User denied permission on remote device.")
            } else if (commType == Constants.COMM_TYPE_RESPONSE_DATA) {
                mWearBodySensorsPermissionApproved = true
                val storageDetails = dataMap.getString(Constants.KEY_PAYLOAD)
                updateWearButtonOnUiThread()
                logToUi(storageDetails)
            } else {
                Log.d(TAG, "Unrecognized communication type received.")
            }
        }
    }

    override fun onComplete(task: Task<Int?>) {
        if (!task.isSuccessful) {
            Log.d(TAG, "Sending message failed, onComplete.")
            updateWearButtonOnUiThread()
            logToUi("Sending message failed.")
        } else {
            Log.d(TAG, "Message sent.")
        }
    }

    private fun sendMessage(dataMap: DataMap) {
        Log.d(TAG, "sendMessage(): $mWearNodeIds")
        if (mWearNodeIds != null && !mWearNodeIds!!.isEmpty()) {
            var sendMessageTask: Task<Int?>
            for (node in mWearNodeIds!!) {
                sendMessageTask = Wearable.getMessageClient(this).sendMessage(
                    node.id,
                    Constants.MESSAGE_PATH_WEAR,
                    dataMap.toByteArray()
                )
                sendMessageTask.addOnCompleteListener(this)
            }
        } else {
            // Unable to retrieve node with proper capability
            mWearBodySensorsPermissionApproved = false
            updateWearButtonOnUiThread()
            logToUi("Wear devices not available to send message.")
        }
    }

    private fun updateWearButtonOnUiThread() {
        runOnUiThread {
            if (mWearBodySensorsPermissionApproved) {
                mWearBodySensorsPermissionButton!!.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_permission_approved, 0, 0, 0
                )
            } else {
                mWearBodySensorsPermissionButton!!.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_permission_denied, 0, 0, 0
                )
            }
        }
    }

    /*
     * Handles all messages for the UI coming on and off the main thread. Not all callbacks happen
     * on the main thread.
     */
    private fun logToUi(message: String) {
        val mainUiThread = Looper.myLooper() == Looper.getMainLooper()
        if (mainUiThread) {
            if (!message.isEmpty()) {
                Log.d(TAG, message)
                mOutputTextView!!.text = message
            }
        } else {
            if (!message.isEmpty()) {
                runOnUiThread {
                    Log.d(TAG, message)
                    mOutputTextView!!.text = message
                }
            }
        }
    }

    private val phoneStorageInformation: String
        private get() {
            val stringBuilder = StringBuilder()
            val state = Environment.getExternalStorageState()
            val isExternalStorageReadable =
                Environment.MEDIA_MOUNTED == state || Environment.MEDIA_MOUNTED_READ_ONLY == state
            if (isExternalStorageReadable) {
                val externalStorageDirectory = Environment.getExternalStorageDirectory()
                val fileList = externalStorageDirectory.list()
                if (fileList.size > 0) {
                    stringBuilder.append("List of files\n")
                    for (file in fileList) {
                        stringBuilder.append(" - $file\n")
                    }
                } else {
                    stringBuilder.append("No files in external storage.")
                }
            } else {
                stringBuilder.append("No external media is available.")
            }
            return stringBuilder.toString()
        }

    private fun sendWearPermissionResults() {
        Log.d(TAG, "sendWearPermissionResults()")
        val dataMap = DataMap()
        if (mPhoneStoragePermissionApproved) {
            dataMap.putInt(
                Constants.KEY_COMM_TYPE,
                Constants.COMM_TYPE_RESPONSE_USER_APPROVED_PERMISSION
            )
        } else {
            dataMap.putInt(
                Constants.KEY_COMM_TYPE,
                Constants.COMM_TYPE_RESPONSE_USER_DENIED_PERMISSION
            )
        }
        sendMessage(dataMap)
    }

    companion object {
        private const val TAG = "MainPhoneActivity"

        /*
     * Alerts Activity that the initial request for permissions came from wear, and the Activity
     * needs to send back the results (data or permission rejection).
     */
        const val EXTRA_PROMPT_PERMISSION_FROM_WEAR =
            "com.example.android.wearable.runtimepermissions.extra.PROMPT_PERMISSION_FROM_WEAR"
        private const val REQUEST_WEAR_PERMISSION_RATIONALE = 1
    }
}