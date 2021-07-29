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
import com.google.android.gms.wearable.CapabilityClient.OnCapabilityChangedListener
import com.google.android.gms.wearable.MessageClient.OnMessageReceivedListener
import android.widget.TextView
import android.os.Bundle
import android.hardware.Sensor
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.example.android.wearable.runtimepermissions.common.Constants
import com.google.android.gms.wearable.*

/**
 * Displays data that requires runtime permissions both locally (BODY_SENSORS) and remotely on
 * the phone (READ_EXTERNAL_STORAGE).
 *
 * The class is also launched by IncomingRequestWearService when the permission for the data the
 * phone is trying to access hasn't been granted (wear's sensors). If granted in that scenario,
 * this Activity also sends back the results of the permission request to the phone device (and
 * the sensor data if approved).
 */
class MainWearActivity : FragmentActivity(),
    OnCapabilityChangedListener,
    OnMessageReceivedListener,
    ActivityCompat.OnRequestPermissionsResultCallback {

    private var mWearBodySensorsPermissionApproved = false
    private var mPhoneStoragePermissionApproved = false
    private var mPhoneRequestingWearSensorPermission = false
    private var mWearBodySensorsPermissionButton: Button? = null
    private var mPhoneStoragePermissionButton: Button? = null
    private var mOutputTextView: TextView? = null
    private var mPhoneNodeId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate()")
        super.onCreate(savedInstanceState)

        /*
         * Since this is a remote permission, we initialize it to false and then check the remote
         * permission once the GoogleApiClient is connected.
         */mPhoneStoragePermissionApproved = false
        setContentView(R.layout.activity_main)

        // Checks if phone app requested wear permission (permission request opens later if true).
        mPhoneRequestingWearSensorPermission = intent.getBooleanExtra(
            EXTRA_PROMPT_PERMISSION_FROM_PHONE, false
        )
        mWearBodySensorsPermissionButton = findViewById(R.id.wear_body_sensors_permission_button)
        if (mWearBodySensorsPermissionApproved) {
            mWearBodySensorsPermissionButton!!.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_permission_approved, 0, 0, 0
            )
        }
        mPhoneStoragePermissionButton = findViewById(R.id.phone_storage_permission_button)
        mOutputTextView = findViewById(R.id.output)
        if (mPhoneRequestingWearSensorPermission) {
            launchPermissionDialogForPhone()
        }
    }

    fun onClickWearBodySensors(view: View?) {
        if (mWearBodySensorsPermissionApproved) {

            // To keep the sample simple, we are only displaying the number of sensors.
            val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
            val sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL)
            val numberOfSensorsOnDevice = sensorList.size
            logToUi("$numberOfSensorsOnDevice sensors on device(s)!")
        } else {
            logToUi("Requested local permission.")
            // On 23+ (M+) devices, GPS permission not granted. Request permission.
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.BODY_SENSORS),
                PERMISSION_REQUEST_READ_BODY_SENSORS
            )
        }
    }

    fun onClickPhoneStorage(view: View?) {
        logToUi("Requested info from phone. New approval may be required.")
        val dataMap = DataMap()
        dataMap.putInt(
            Constants.KEY_COMM_TYPE,
            Constants.COMM_TYPE_REQUEST_DATA
        )
        sendMessage(dataMap)
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

        // Instantiates clients without member variables, as clients are inexpensive to create and
        // won't lose their listeners. (They are cached and shared between GoogleApi instances.)
        Wearable.getMessageClient(this).addListener(this)
        Wearable.getCapabilityClient(this).addListener(
            this, Constants.CAPABILITY_PHONE_APP
        )

        // Initial check of capabilities to find the phone.
        val capabilityInfoTask = Wearable.getCapabilityClient(this)
            .getCapability(Constants.CAPABILITY_PHONE_APP, CapabilityClient.FILTER_REACHABLE)
        capabilityInfoTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Capability request succeeded.")
                val capabilityInfo = task.result
                mPhoneNodeId = pickBestNodeId(capabilityInfo!!.nodes)
            } else {
                Log.d(TAG, "Capability request failed to return any results.")
            }
        }

        // Enables app to handle 23+ (M+) style permissions.
        mWearBodySensorsPermissionApproved =
            (ActivityCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)
                    == PackageManager.PERMISSION_GRANTED)
    }

    /*
      * Because this wear activity is marked "android:launchMode='singleInstance'" in the manifest,
      * we need to allow the permissions dialog to be opened up from the phone even if the wear app
      * is in the foreground. By overriding onNewIntent, we can cover that use case.
      */
    override fun onNewIntent(intent: Intent) {
        Log.d(TAG, "onNewIntent()")
        super.onNewIntent(intent)

        // Checks if phone app requested wear permissions (opens up permission request if true).
        mPhoneRequestingWearSensorPermission = intent.getBooleanExtra(
            EXTRA_PROMPT_PERMISSION_FROM_PHONE, false
        )
        if (mPhoneRequestingWearSensorPermission) {
            launchPermissionDialogForPhone()
        }
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        Log.d(TAG, "onCapabilityChanged(): $capabilityInfo")
        mPhoneNodeId = pickBestNodeId(capabilityInfo.nodes)
    }

    /*
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        val permissionResult = ("Request code: " + requestCode + ", Permissions: " + permissions
                + ", Results: " + grantResults)
        Log.d(TAG, "onRequestPermissionsResult(): $permissionResult")
        if (requestCode == PERMISSION_REQUEST_READ_BODY_SENSORS) {
            if (grantResults.size == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                mWearBodySensorsPermissionApproved = true
                mWearBodySensorsPermissionButton!!.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_permission_approved, 0, 0, 0
                )

                // To keep the sample simple, we are only displaying the number of sensors.
                val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
                val sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL)
                val numberOfSensorsOnDevice = sensorList.size
                val sensorSummary = "$numberOfSensorsOnDevice sensors on this device!"
                logToUi(sensorSummary)
                if (mPhoneRequestingWearSensorPermission) {
                    // Resets so this isn't triggered every time permission is changed in app.
                    mPhoneRequestingWearSensorPermission = false

                    // Send 'approved' message to remote phone since it started Activity.
                    val dataMap = DataMap()
                    dataMap.putInt(
                        Constants.KEY_COMM_TYPE,
                        Constants.COMM_TYPE_RESPONSE_USER_APPROVED_PERMISSION
                    )
                    sendMessage(dataMap)
                }
            } else {
                mWearBodySensorsPermissionApproved = false
                mWearBodySensorsPermissionButton!!.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_permission_denied, 0, 0, 0
                )
                if (mPhoneRequestingWearSensorPermission) {
                    // Resets so this isn't triggered every time permission is changed in app.
                    mPhoneRequestingWearSensorPermission = false
                    // Send 'denied' message to remote phone since it started Activity.
                    val dataMap = DataMap()
                    dataMap.putInt(
                        Constants.KEY_COMM_TYPE,
                        Constants.COMM_TYPE_RESPONSE_USER_DENIED_PERMISSION
                    )
                    sendMessage(dataMap)
                }
            }
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG, "onMessageReceived(): $messageEvent")
        val messagePath = messageEvent.path
        if (messagePath == Constants.MESSAGE_PATH_WEAR) {
            val dataMap = DataMap.fromByteArray(messageEvent.data)
            val commType = dataMap.getInt(Constants.KEY_COMM_TYPE, 0)
            if (commType == Constants.COMM_TYPE_RESPONSE_PERMISSION_REQUIRED) {
                mPhoneStoragePermissionApproved = false
                updatePhoneButtonOnUiThread()

                /* Because our request for remote data requires a remote permission, we now launch
                 * a splash activity informing the user we need those permissions (along with
                 * other helpful information to approve).
                 */
                val phonePermissionRationaleIntent =
                    Intent(this, RequestPermissionOnPhoneActivity::class.java)
                startActivityForResult(phonePermissionRationaleIntent, REQUEST_PHONE_PERMISSION)
            } else if (commType == Constants.COMM_TYPE_RESPONSE_USER_APPROVED_PERMISSION) {
                mPhoneStoragePermissionApproved = true
                updatePhoneButtonOnUiThread()
                logToUi("User approved permission on remote device, requesting data again.")
                val outgoingDataRequestDataMap = DataMap()
                outgoingDataRequestDataMap.putInt(
                    Constants.KEY_COMM_TYPE,
                    Constants.COMM_TYPE_REQUEST_DATA
                )
                sendMessage(outgoingDataRequestDataMap)
            } else if (commType == Constants.COMM_TYPE_RESPONSE_USER_DENIED_PERMISSION) {
                mPhoneStoragePermissionApproved = false
                updatePhoneButtonOnUiThread()
                logToUi("User denied permission on remote device.")
            } else if (commType == Constants.COMM_TYPE_RESPONSE_DATA) {
                mPhoneStoragePermissionApproved = true
                val storageDetails = dataMap.getString(Constants.KEY_PAYLOAD)
                updatePhoneButtonOnUiThread()
                logToUi(storageDetails)
            }
        }
    }

    private fun sendMessage(dataMap: DataMap) {
        Log.d(TAG, "sendMessage(): $mPhoneNodeId")
        if (mPhoneNodeId != null) {
            // Clients are inexpensive to create, so in this case we aren't creating member variables.
            // (They are cached and shared between GoogleApi instances.)
            val sendMessageTask = Wearable.getMessageClient(this)
                .sendMessage(mPhoneNodeId!!, Constants.MESSAGE_PATH_PHONE, dataMap.toByteArray())
            sendMessageTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Message sent successfully")
                } else {
                    Log.d(TAG, "Message failed.")
                }
            }
        } else {
            // Unable to retrieve node with proper capability
            mPhoneStoragePermissionApproved = false
            updatePhoneButtonOnUiThread()
            logToUi("Phone not available to send message.")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Check which request we're responding to
        if (requestCode == REQUEST_PHONE_PERMISSION) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                logToUi("Requested permission on phone.")
                val dataMap = DataMap()
                dataMap.putInt(
                    Constants.KEY_COMM_TYPE,
                    Constants.COMM_TYPE_REQUEST_PROMPT_PERMISSION
                )
                sendMessage(dataMap)
            }
        }
    }

    /*
     * There should only ever be one phone in a node set (much less w/ the correct capability), so
     * I am just grabbing the first one (which should be the only one).
     */
    private fun pickBestNodeId(nodes: Set<Node>): String? {
        var bestNodeId: String? = null
        // Find a nearby node or pick one arbitrarily.
        for (node in nodes) {
            if (node.isNearby) {
                return node.id
            }
            bestNodeId = node.id
        }
        return bestNodeId
    }

    /*
     * If Phone triggered the wear app for permissions, we open up the permission
     * dialog after inflation.
     */
    private fun launchPermissionDialogForPhone() {
        Log.d(TAG, "launchPermissionDialogForPhone()")
        if (!mWearBodySensorsPermissionApproved) {
            // On 23+ (M+) devices, GPS permission not granted. Request permission.
            ActivityCompat.requestPermissions(
                this@MainWearActivity, arrayOf(Manifest.permission.BODY_SENSORS),
                PERMISSION_REQUEST_READ_BODY_SENSORS
            )
        }
    }

    private fun updatePhoneButtonOnUiThread() {
        runOnUiThread {
            if (mPhoneStoragePermissionApproved) {
                mPhoneStoragePermissionButton!!.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_permission_approved, 0, 0, 0
                )
            } else {
                mPhoneStoragePermissionButton!!.setCompoundDrawablesWithIntrinsicBounds(
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
            runOnUiThread {
                if (!message.isEmpty()) {
                    Log.d(TAG, message)
                    mOutputTextView!!.text = message
                }
            }
        }
    }

    companion object {
        private const val TAG = "MainWearActivity"

        /* Id to identify local permission request for body sensors. */
        private const val PERMISSION_REQUEST_READ_BODY_SENSORS = 1

        /* Id to identify starting/closing RequestPermissionOnPhoneActivity (startActivityForResult). */
        private const val REQUEST_PHONE_PERMISSION = 1
        const val EXTRA_PROMPT_PERMISSION_FROM_PHONE =
            "com.example.android.wearable.runtimepermissions.extra.PROMPT_PERMISSION_FROM_PHONE"
    }
}