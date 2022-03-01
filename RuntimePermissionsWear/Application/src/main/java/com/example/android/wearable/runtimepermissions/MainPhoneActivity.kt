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
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.android.wearable.runtimepermissions.common.Constants
import com.example.android.wearable.runtimepermissions.databinding.ActivityMainBinding
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Displays data that requires runtime permissions both locally (READ_PHONE_STATE/READ_PHONE_NUMBERS) and
 * remotely on wear (BODY_SENSORS).
 *
 * The class also handles sending back the results of a permission request from a remote wear device
 * when the permission has not been approved yet on the phone (uses EXTRA as trigger). In that case,
 * the IncomingRequestPhoneService launches (or passes a new intent) to this activity, which then launches
 * [PhonePermissionRequestActivity] to inform user of permission request.
 *
 * After the user decides what to do, that activity returns the result here, to handle sending data across and keeps
 * user in app experience.
 */
class MainPhoneActivity :
    AppCompatActivity(),
    CapabilityClient.OnCapabilityChangedListener,
    MessageClient.OnMessageReceivedListener {

    private lateinit var binding: ActivityMainBinding

    /**
     * True if the remote wear body sensors permission is approved.
     */
    private var wearBodySensorsPermissionApproved = false

    /**
     * True if the watch is remotely requesting sensor permissions.
     */
    private var isWearRequestingPhonePermission = false

    /**
     * True if we have handled the watch's remote request for sensor permissions.
     */
    private var askedForPermissionOnBehalfOfWear = false

    /**
     * The set of connected nodes, if any.
     */
    private var wearNodeIdsState = MutableStateFlow<Set<Node>?>(null)

    /**
     * The [ActivityResultLauncher] for informing the user that we are going to make a permission
     * request remotely on the watch.
     */
    private val requestPermissionOnWearLauncher = registerForActivityResult(
        WearPermissionRequestActivity.Companion.RequestPermissionOnWear
    ) { requestMade ->
        if (requestMade) {
            lifecycleScope.launch {
                logToUi(getString(R.string.requested_remote_permission))
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

    /**
     * The [ActivityResultLauncher] for informing the user that we are going to make a permission
     * request on the phone.
     */
    private val requestPermissionOnPhoneLauncher = registerForActivityResult(
        PhonePermissionRequestActivity.Companion.RequestPermission
    ) {
        lifecycleScope.launch {
            sendWearPermissionResults()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate()")
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.wearBodySensorsPermissionButton.setOnClickListener {
            logToUi(getString(R.string.requested_info_from_wear))
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

        binding.phoneInfoPermissionButton.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, phoneSummaryPermission)
                == PackageManager.PERMISSION_GRANTED
            ) {
                logToUi(getPhoneSummary())
            } else {
                requestPermissionOnPhoneLauncher.launch(Unit)
            }
        }

        // Since this is a remote permission, we initialize it to false and then check the remote
        // permission once the GoogleApiClient is connected.
        wearBodySensorsPermissionApproved = false

        // Restore whether we've already asked the user for permission on behalf of the watch.
        askedForPermissionOnBehalfOfWear =
            savedInstanceState?.getBoolean(
            ASKED_PERMISSION_ON_BEHALF_OF_WEAR,
            askedForPermissionOnBehalfOfWear
        ) ?: askedForPermissionOnBehalfOfWear

        checkForRemotePermissionRequest()
    }

    /**
     * Because this wear activity is marked "android:launchMode='singleTop'" in the manifest,
     * we need to allow the permissions dialog to be opened up from the wear app even if the phone app
     * is in the foreground. By overriding onNewIntent, we can cover that use case.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Update the "origination" intent
        setIntent(intent)

        // This must be a new request, mark that we haven't asked for permission on behalf of the
        // phone yet.
        askedForPermissionOnBehalfOfWear = false

        checkForRemotePermissionRequest()
    }

    override fun onResume() {
        Log.d(TAG, "onResume()")
        super.onResume()

        if (ActivityCompat.checkSelfPermission(this, phoneSummaryPermission)
            == PackageManager.PERMISSION_GRANTED
        ) {
            binding.phoneInfoPermissionButton.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_very_satisfied, 0, 0, 0
            )
        }

        // Clients are inexpensive to create, so in this case we aren't creating member variables.
        // (They are cached and shared between GoogleApi instances.)
        Wearable.getMessageClient(this).addListener(this)
        Wearable.getCapabilityClient(this).addListener(
            this, Constants.CAPABILITY_WEAR_APP
        )

        // Initial check of capabilities to find the phone.
        lifecycleScope.launch {
            try {
                val capabilityInfo = Wearable.getCapabilityClient(this@MainPhoneActivity)
                    .getCapability(Constants.CAPABILITY_WEAR_APP, CapabilityClient.FILTER_REACHABLE)
                    .await()

                if (capabilityInfo.name == Constants.CAPABILITY_WEAR_APP) {
                    wearNodeIdsState.value = capabilityInfo.nodes
                }

                Log.d(TAG, "Capability request succeeded.")
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (throwable: Throwable) {
                Log.d(TAG, "Capability request failed to return any results.")
            }
        }
    }

    override fun onPause() {
        Log.d(TAG, "onPause()")
        super.onPause()
        Wearable.getMessageClient(this).removeListener(this)
        Wearable.getCapabilityClient(this).removeListener(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(ASKED_PERMISSION_ON_BEHALF_OF_WEAR, askedForPermissionOnBehalfOfWear)
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        Log.d(TAG, "onCapabilityChanged(): $capabilityInfo")
        wearNodeIdsState.value = capabilityInfo.nodes
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG, "onMessageReceived(): $messageEvent")
        when (messageEvent.path) {
            Constants.MESSAGE_PATH_PHONE -> {
                val dataMap = DataMap.fromByteArray(messageEvent.data)
                when (dataMap.getInt(Constants.KEY_COMM_TYPE, 0)) {
                    Constants.COMM_TYPE_RESPONSE_PERMISSION_REQUIRED -> {
                        wearBodySensorsPermissionApproved = false
                        updateWearButtonOnUiThread()

                        // Because our request for remote data requires a remote permission, we now launch
                        // a splash activity informing the user we need those permissions (along with
                        // other helpful information to approve).
                        requestPermissionOnWearLauncher.launch(Unit)
                    }
                    Constants.COMM_TYPE_RESPONSE_USER_APPROVED_PERMISSION -> {
                        wearBodySensorsPermissionApproved = true
                        updateWearButtonOnUiThread()
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
                        wearBodySensorsPermissionApproved = false
                        updateWearButtonOnUiThread()
                        logToUi(getString(R.string.user_denied_remote_permission))
                    }
                    Constants.COMM_TYPE_RESPONSE_DATA -> {
                        wearBodySensorsPermissionApproved = true
                        val sensorSummary = dataMap.getString(Constants.KEY_PAYLOAD)!!
                        updateWearButtonOnUiThread()
                        logToUi(sensorSummary)
                    }
                    else -> {
                        Log.d(TAG, "Unrecognized communication type received.")
                    }
                }
            }
        }
    }

    /**
     * A helper function to launch the permission dialog on behalf of the phone.
     */
    private fun checkForRemotePermissionRequest() {
        isWearRequestingPhonePermission =
            intent.getBooleanExtra(EXTRA_PROMPT_PERMISSION_FROM_WEAR, false)

        // If we've already asked the user on behalf of the phone, don't ask again
        if (isWearRequestingPhonePermission && !askedForPermissionOnBehalfOfWear) {
            requestPermissionOnPhoneLauncher.launch(Unit)
        }
    }

    private suspend fun sendMessage(dataMap: DataMap) {
        val wearNodeIds = wearNodeIdsState.filterNotNull().first()

        Log.d(TAG, "sendMessage(): $wearNodeIds")

        if (wearNodeIds.isEmpty()) {
            // Unable to retrieve node with proper capability
            wearBodySensorsPermissionApproved = false
            updateWearButtonOnUiThread()
            logToUi(getString(R.string.wear_not_available))
        } else {
            try {
                // Set up an internal scope to send messages in parallel
                coroutineScope {
                    val sentMessageResults = wearNodeIds
                        .map { node ->
                            async {
                                Wearable.getMessageClient(this@MainPhoneActivity)
                                    .sendMessage(
                                        node.id,
                                        Constants.MESSAGE_PATH_WEAR,
                                        dataMap.toByteArray()
                                    )
                                    .await()
                            }
                        }

                    // Wait for all messages to try to send (don't throw upon first failure)
                    sentMessageResults.joinAll()

                    // Now await, rethrowing any exceptions
                    sentMessageResults.awaitAll()
                }
                Log.d(TAG, "Message(s) sent.")
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (throwable: Throwable) {
                // Update the UI if any message failed
                Log.d(TAG, "Sending message failed.")
                updateWearButtonOnUiThread()
                logToUi(getString(R.string.sending_message_failed))
            }
        }
    }

    private fun updateWearButtonOnUiThread() {
        runOnUiThread {
            if (wearBodySensorsPermissionApproved) {
                binding.wearBodySensorsPermissionButton.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_very_satisfied, 0, 0, 0
                )
            } else {
                binding.wearBodySensorsPermissionButton.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_very_dissatisfied, 0, 0, 0
                )
            }
        }
    }

    /*
     * Handles all messages for the UI coming on and off the main thread. Not all callbacks happen
     * on the main thread.
     */
    private fun logToUi(message: String) {
        runOnUiThread {
            binding.output.text = message
        }
    }

    private suspend fun sendWearPermissionResults() {
        Log.d(TAG, "sendWearPermissionResults()")

        if (isWearRequestingPhonePermission) {
            // Resets so this isn't triggered every time permission is changed in app.
            isWearRequestingPhonePermission = false
            askedForPermissionOnBehalfOfWear = true

            sendMessage(
                DataMap().apply {
                    putInt(
                        Constants.KEY_COMM_TYPE,
                        if (ActivityCompat.checkSelfPermission(
                                this@MainPhoneActivity,
                                phoneSummaryPermission
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            Constants.COMM_TYPE_RESPONSE_USER_APPROVED_PERMISSION
                        } else {
                            Constants.COMM_TYPE_RESPONSE_USER_DENIED_PERMISSION
                        }
                    )
                }
            )
        }
    }

    companion object {
        private const val TAG = "MainPhoneActivity"

        private const val ASKED_PERMISSION_ON_BEHALF_OF_WEAR = "AskedPermissionOnBehalfOfWear"

        /*
         * Alerts Activity that the initial request for permissions came from wear, and the Activity
         * needs to send back the results (data or permission rejection).
         */
        const val EXTRA_PROMPT_PERMISSION_FROM_WEAR =
            "com.example.android.wearable.runtimepermissions.extra.PROMPT_PERMISSION_FROM_WEAR"
    }
}
