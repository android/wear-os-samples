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

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.setPadding
import androidx.lifecycle.lifecycleScope
import com.example.android.wearable.runtimepermissions.common.Constants
import com.example.android.wearable.runtimepermissions.databinding.ActivityMainBinding
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityClient.OnCapabilityChangedListener
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.MessageClient.OnMessageReceivedListener
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Displays data that requires runtime permissions both locally (BODY_SENSORS) and remotely on
 * the phone (READ_PHONE).
 *
 * The class is also launched by IncomingRequestWearService when the permission for the data the
 * phone is trying to access hasn't been granted (wear's sensors). If granted in that scenario,
 * this Activity also sends back the results of the permission request to the phone device (and
 * the sensor data if approved).
 */
class MainWearActivity :
    AppCompatActivity(),
    OnCapabilityChangedListener,
    OnMessageReceivedListener {

    private lateinit var binding: ActivityMainBinding

    /**
     * True if the remote phone info permission is approved.
     */
    private var phoneInfoPermissionApproved = false

    /**
     * True if the phone is remotely requesting sensor permissions.
     */
    private var isPhoneRequestingPermission = false

    /**
     * True if we have handled the phone's remote request for sensor permissions.
     */
    private var askedForPermissionOnBehalfOfPhone = false

    /**
     * The id of the phone node, if available.
     */
    private var phoneNodeId: String? = null

    /**
     * The [ActivityResultLauncher] for requesting permissions locally.
     */
    private val requestPermissionLauncher = registerForActivityResult(
        RequestPermission()
    ) {
        onPermissionResult()
    }

    /**
     * The [ActivityResultLauncher] for informing the user that we are going to make a permission
     * request remotely on the phone.
     */
    private val requestPermissionOnPhoneLauncher = registerForActivityResult(
        RequestPermissionOnPhoneActivity.Companion.RequestPermissionOnPhone
    ) { requestMade ->
        if (requestMade) {
            logToUi(getString(R.string.requested_remote_permission))
            lifecycleScope.launch {
                sendMessage(
                    DataMap().apply {
                        putInt(
                            Constants.KEY_COMM_TYPE,
                            Constants.COMM_TYPE_REQUEST_PROMPT_PERMISSION
                        )
                    }
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Since this is a remote permission, we initialize it to false and then check the remote
        // permission once the GoogleApiClient is connected.
        phoneInfoPermissionApproved = false

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            binding.wearBodySensorsPermissionButton.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_very_satisfied,
                0,
                0,
                0
            )
        }

        // Restore whether we've already asked the user for permission on behalf of the phone.
        askedForPermissionOnBehalfOfPhone =
            savedInstanceState?.getBoolean(
                ASKED_PERMISSION_ON_BEHALF_OF_PHONE,
                askedForPermissionOnBehalfOfPhone
            ) ?: askedForPermissionOnBehalfOfPhone

        checkForRemotePermissionRequest()

        binding.wearBodySensorsPermissionButton.setOnClickListener {
            logToUi(getString(R.string.requested_local_permission))
            requestPermissionLauncher.launch(Manifest.permission.BODY_SENSORS)
        }

        binding.phoneInfoPermissionButton.setOnClickListener {
            logToUi(getString(R.string.requested_info_from_phone))
            lifecycleScope.launch {
                sendMessage(
                    DataMap().apply {
                        putInt(
                            Constants.KEY_COMM_TYPE,
                            Constants.COMM_TYPE_REQUEST_DATA
                        )
                    }
                )
            }
        }

        if (resources.configuration.isScreenRound) {
            binding.scrollingContentContainer.doOnPreDraw {
                // Calculate the padding necessary to make the scrolling content fit in a square
                // inscribed on a round screen.
                it.setPadding((it.width / 2.0 * (1.0 - 1.0 / sqrt(2.0))).roundToInt())
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Wearable.getMessageClient(this).removeListener(this)
        Wearable.getCapabilityClient(this).removeListener(this)
    }

    override fun onResume() {
        super.onResume()

        // Instantiates clients without member variables, as clients are inexpensive to create and
        // won't lose their listeners. (They are cached and shared between GoogleApi instances.)
        Wearable.getMessageClient(this).addListener(this)
        Wearable.getCapabilityClient(this).addListener(
            this,
            Constants.CAPABILITY_PHONE_APP
        )

        lifecycleScope.launch {
            // Initial check of capabilities to find the phone.
            try {
                val capabilityInfo = Wearable.getCapabilityClient(this@MainWearActivity)
                    .getCapability(
                        Constants.CAPABILITY_PHONE_APP,
                        CapabilityClient.FILTER_REACHABLE
                    )
                    .await()
                phoneNodeId = capabilityInfo.nodes.firstOrNull()?.id
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (throwable: Throwable) {
                Log.d(TAG, "Capability request failed to return any results.")
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(ASKED_PERMISSION_ON_BEHALF_OF_PHONE, askedForPermissionOnBehalfOfPhone)
    }

    /**
     * Because this wear activity is marked "android:launchMode='singleInstance'" in the manifest,
     * we need to allow the permissions dialog to be opened up from the phone even if the wear app
     * is in the foreground. By overriding onNewIntent, we can cover that use case.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Update the "origination" intent
        setIntent(intent)

        // This must be a new request, mark that we haven't asked for permission on behalf of the
        // phone yet.
        askedForPermissionOnBehalfOfPhone = false

        checkForRemotePermissionRequest()
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        Log.d(TAG, "onCapabilityChanged(): $capabilityInfo")
        phoneNodeId = capabilityInfo.nodes.firstOrNull()?.id
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG, "onMessageReceived(): $messageEvent")
        val messagePath = messageEvent.path
        if (messagePath == Constants.MESSAGE_PATH_WEAR) {
            val dataMap = DataMap.fromByteArray(messageEvent.data)
            val commType = dataMap.getInt(Constants.KEY_COMM_TYPE, 0)
            when (commType) {
                Constants.COMM_TYPE_RESPONSE_PERMISSION_REQUIRED -> {
                    phoneInfoPermissionApproved = false
                    updatePhoneButtonOnUiThread()

                    // Because our request for remote data requires a remote permission, we now
                    // launch a splash activity informing the user we need those permissions (along
                    // with other helpful information to approve).
                    requestPermissionOnPhoneLauncher.launch(Unit)
                }
                Constants.COMM_TYPE_RESPONSE_USER_APPROVED_PERMISSION -> {
                    phoneInfoPermissionApproved = true
                    updatePhoneButtonOnUiThread()
                    logToUi(getString(R.string.user_approved_remote_permission))
                    lifecycleScope.launch {
                        sendMessage(
                            DataMap().apply {
                                putInt(
                                    Constants.KEY_COMM_TYPE,
                                    Constants.COMM_TYPE_REQUEST_DATA
                                )
                            }
                        )
                    }
                }
                Constants.COMM_TYPE_RESPONSE_USER_DENIED_PERMISSION -> {
                    phoneInfoPermissionApproved = false
                    updatePhoneButtonOnUiThread()
                    logToUi(getString(R.string.user_denied_remote_permission))
                }
                Constants.COMM_TYPE_RESPONSE_DATA -> {
                    phoneInfoPermissionApproved = true
                    val phoneSummary = dataMap.getString(Constants.KEY_PAYLOAD)!!
                    updatePhoneButtonOnUiThread()
                    logToUi(phoneSummary)
                }
            }
        }
    }

    /**
     * A helper function to launch the permission dialog on behalf of the phone.
     */
    private fun checkForRemotePermissionRequest() {
        isPhoneRequestingPermission = intent.getBooleanExtra(
            EXTRA_PROMPT_PERMISSION_FROM_PHONE,
            false
        )

        // If we've already asked the user on behalf of the phone, don't ask again
        if (isPhoneRequestingPermission && !askedForPermissionOnBehalfOfPhone) {
            launchPermissionDialogForPhone()
        }
    }

    private fun onPermissionResult() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            // To keep the sample simple, we are only displaying the number of sensors.
            val sensorSummary = this.sensorSummary()

            binding.wearBodySensorsPermissionButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.ic_very_satisfied,
                0,
                0,
                0
            )

            logToUi(sensorSummary)
            if (isPhoneRequestingPermission) {
                // Resets so this isn't triggered every time permission is changed in app.
                isPhoneRequestingPermission = false
                askedForPermissionOnBehalfOfPhone = true

                // Send 'approved' message to remote phone since it started Activity.
                lifecycleScope.launch {
                    sendMessage(
                        DataMap().apply {
                            putInt(
                                Constants.KEY_COMM_TYPE,
                                Constants.COMM_TYPE_RESPONSE_USER_APPROVED_PERMISSION
                            )
                        }
                    )
                }
            }
        } else {
            binding.wearBodySensorsPermissionButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.ic_very_dissatisfied,
                0,
                0,
                0
            )
            if (isPhoneRequestingPermission) {
                // Resets so this isn't triggered every time permission is changed in app.
                isPhoneRequestingPermission = false
                askedForPermissionOnBehalfOfPhone = true

                // Send 'denied' message to remote phone since it started Activity.
                lifecycleScope.launch {
                    sendMessage(
                        DataMap().apply {
                            putInt(
                                Constants.KEY_COMM_TYPE,
                                Constants.COMM_TYPE_RESPONSE_USER_DENIED_PERMISSION
                            )
                        }
                    )
                }
            }
        }
    }

    private suspend fun sendMessage(dataMap: DataMap) {
        val phoneNodeId = phoneNodeId

        Log.d(TAG, "sendMessage(): $phoneNodeId")
        if (phoneNodeId != null) {
            // Clients are inexpensive to create, so in this case we aren't creating member variables.
            // (They are cached and shared between GoogleApi instances.)
            try {
                Wearable.getMessageClient(this)
                    .sendMessage(phoneNodeId, Constants.MESSAGE_PATH_PHONE, dataMap.toByteArray())
                    .await()

                Log.d(TAG, "Message sent successfully")
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (throwable: Throwable) {
                Log.d(TAG, "Message failed.")
            }
        } else {
            // Unable to retrieve node with proper capability
            phoneInfoPermissionApproved = false
            updatePhoneButtonOnUiThread()
            logToUi(getString(R.string.phone_not_available))
        }
    }

    /*
     * If Phone triggered the wear app for permissions, we open up the permission
     * dialog after inflation.
     */
    private fun launchPermissionDialogForPhone() {
        Log.d(TAG, "launchPermissionDialogForPhone()")
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.BODY_SENSORS)
        }
    }

    private fun updatePhoneButtonOnUiThread() {
        runOnUiThread {
            binding.phoneInfoPermissionButton.setCompoundDrawablesWithIntrinsicBounds(
                if (phoneInfoPermissionApproved) {
                    R.drawable.ic_very_satisfied
                } else {
                    R.drawable.ic_very_dissatisfied
                },
                0,
                0,
                0
            )
        }
    }

    /*
     * Handles all messages for the UI coming on and off the main thread. Not all callbacks happen
     * on the main thread.
     */
    private fun logToUi(message: String) {
        runOnUiThread {
            if (message.isNotEmpty()) {
                Log.d(TAG, message)
                binding.output.text = message
            }
        }
    }

    companion object {
        private const val TAG = "MainWearActivity"

        private const val ASKED_PERMISSION_ON_BEHALF_OF_PHONE = "AskedPermissionOnBehalfOfPhone"

        const val EXTRA_PROMPT_PERMISSION_FROM_PHONE =
            "com.example.android.wearable.runtimepermissions.extra.PROMPT_PERMISSION_FROM_PHONE"
    }
}
