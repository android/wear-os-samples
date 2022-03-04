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
package com.example.android.wearable.wear.wearverifyremoteapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.core.view.doOnPreDraw
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.wear.phone.interactions.PhoneTypeHelper
import androidx.wear.remote.interactions.RemoteActivityHelper
import androidx.wear.widget.ConfirmationOverlay
import com.example.android.wearable.wear.wearverifyremoteapp.databinding.ActivityMainBinding
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Checks if the phone app is installed on remote device. If it is not, allows user to open app
 * listing on the phone's Play or App Store.
 */
class MainWearActivity : FragmentActivity(), CapabilityClient.OnCapabilityChangedListener {

    private lateinit var binding: ActivityMainBinding

    private lateinit var capabilityClient: CapabilityClient
    private lateinit var nodeClient: NodeClient
    private lateinit var remoteActivityHelper: RemoteActivityHelper

    private var androidPhoneNodeWithApp: Node? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        capabilityClient = Wearable.getCapabilityClient(this)
        nodeClient = Wearable.getNodeClient(this)
        remoteActivityHelper = RemoteActivityHelper(this)

        binding.informationTextView.text = getString(R.string.message_checking)
        binding.remoteOpenButton.setOnClickListener {
            openAppInStoreOnPhone()
        }

        if (resources.configuration.isScreenRound) {
            binding.scrollingContentContainer.doOnPreDraw {
                // Calculate the padding necessary to make the scrolling content fit in a
                // square inscribed on a round
                // screen.
                it.setPadding((it.width / 2.0 * (1.0 - 1.0 / sqrt(2.0))).roundToInt())
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Wearable.getCapabilityClient(this).removeListener(this, CAPABILITY_PHONE_APP)
    }

    override fun onResume() {
        super.onResume()
        Wearable.getCapabilityClient(this).addListener(this, CAPABILITY_PHONE_APP)
        lifecycleScope.launch {
            checkIfPhoneHasApp()
        }
    }

    /*
     * Updates UI when capabilities change (install/uninstall phone app).
     */
    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        Log.d(TAG, "onCapabilityChanged(): $capabilityInfo")
        // There should only ever be one phone in a node set (much less w/ the correct
        // capability), so I am just grabbing the first one (which should be the only one).
        androidPhoneNodeWithApp = capabilityInfo.nodes.firstOrNull()
        updateUi()
    }

    private suspend fun checkIfPhoneHasApp() {
        Log.d(TAG, "checkIfPhoneHasApp()")

        try {
            val capabilityInfo = capabilityClient
                .getCapability(CAPABILITY_PHONE_APP, CapabilityClient.FILTER_ALL)
                .await()

            Log.d(TAG, "Capability request succeeded.")

            withContext(Dispatchers.Main) {
                // There should only ever be one phone in a node set (much less w/ the correct
                // capability), so I am just grabbing the first one (which should be the only one).
                androidPhoneNodeWithApp = capabilityInfo.nodes.firstOrNull()
                updateUi()
            }
        } catch (cancellationException: CancellationException) {
            // Request was cancelled normally
        } catch (throwable: Throwable) {
            Log.d(TAG, "Capability request failed to return any results.")
        }
    }

    private fun updateUi() {
        val androidPhoneNodeWithApp = androidPhoneNodeWithApp

        if (androidPhoneNodeWithApp != null) {
            // TODO: Add your code to communicate with the phone app via
            //       Wear APIs (MessageClient, DataClient, etc.)
            Log.d(TAG, "Installed")
            binding.informationTextView.text =
                getString(R.string.message_installed, androidPhoneNodeWithApp.displayName)
            binding.remoteOpenButton.isInvisible = true
        } else {
            Log.d(TAG, "Missing")
            binding.informationTextView.text = getString(R.string.message_missing)
            binding.remoteOpenButton.isVisible = true
        }
    }

    private fun openAppInStoreOnPhone() {
        Log.d(TAG, "openAppInStoreOnPhone()")

        val intent = when (PhoneTypeHelper.getPhoneDeviceType(applicationContext)) {
            PhoneTypeHelper.DEVICE_TYPE_ANDROID -> {
                Log.d(TAG, "\tDEVICE_TYPE_ANDROID")
                // Create Remote Intent to open Play Store listing of app on remote device.
                Intent(Intent.ACTION_VIEW)
                    .addCategory(Intent.CATEGORY_BROWSABLE)
                    .setData(Uri.parse(ANDROID_MARKET_APP_URI))
            }
            PhoneTypeHelper.DEVICE_TYPE_IOS -> {
                Log.d(TAG, "\tDEVICE_TYPE_IOS")

                // Create Remote Intent to open App Store listing of app on iPhone.
                Intent(Intent.ACTION_VIEW)
                    .addCategory(Intent.CATEGORY_BROWSABLE)
                    .setData(Uri.parse(APP_STORE_APP_URI))
            }
            else -> {
                Log.d(TAG, "\tDEVICE_TYPE_ERROR_UNKNOWN")
                return
            }
        }

        lifecycleScope.launch {
            try {
                remoteActivityHelper.startRemoteActivity(intent).await()

                ConfirmationOverlay().showOn(this@MainWearActivity)
            } catch (cancellationException: CancellationException) {
                // Request was cancelled normally
                throw cancellationException
            } catch (throwable: Throwable) {
                ConfirmationOverlay()
                    .setType(ConfirmationOverlay.FAILURE_ANIMATION)
                    .showOn(this@MainWearActivity)
            }
        }
    }

    companion object {
        private const val TAG = "MainWearActivity"

        // Name of capability listed in Phone app's wear.xml.
        // IMPORTANT NOTE: This should be named differently than your Wear app's capability.
        private const val CAPABILITY_PHONE_APP = "verify_remote_example_phone_app"

        // Links to install mobile app for both Android (Play Store) and iOS.
        // TODO: Replace with your links/packages.
        private const val ANDROID_MARKET_APP_URI =
            "market://details?id=com.example.android.wearable.wear.wearverifyremoteapp"

        // TODO: Replace with your links/packages.
        private const val APP_STORE_APP_URI =
            "https://itunes.apple.com/us/app/android-wear/id986496028?mt=8"
    }
}
