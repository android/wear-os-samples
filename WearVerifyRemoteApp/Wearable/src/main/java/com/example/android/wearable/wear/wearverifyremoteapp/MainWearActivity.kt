/*
 * Copyright (C) 2016 Google Inc. All Rights Reserved.
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
package com.example.android.wearable.wear.wearverifyremoteapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.support.wearable.view.ConfirmationOverlay
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.wear.ambient.AmbientModeSupport
import androidx.wear.phone.interactions.PhoneTypeHelper
import com.example.android.wearable.wear.wearverifyremoteapp.MainWearActivity
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import com.google.android.wearable.intent.RemoteIntent

/**
 * Checks if the phone app is installed on remote device. If it is not, allows user to open app
 * listing on the phone's Play or App Store.
 */
class MainWearActivity : FragmentActivity(), CapabilityClient.OnCapabilityChangedListener {
    // Result from sending RemoteIntent to phone to open app in play/app store.
    private val mResultReceiver: ResultReceiver = object : ResultReceiver(Handler()) {
        override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
            if (resultCode == RemoteIntent.RESULT_OK) {
                ConfirmationOverlay().showOn(this@MainWearActivity)
            } else if (resultCode == RemoteIntent.RESULT_FAILED) {
                ConfirmationOverlay()
                    .setType(ConfirmationOverlay.FAILURE_ANIMATION)
                    .showOn(this@MainWearActivity)
            } else {
                throw IllegalStateException("Unexpected result $resultCode")
            }
        }
    }
    private var mInformationTextView: TextView? = null
    private var mRemoteOpenButton: Button? = null
    private var mAndroidPhoneNodeWithApp: Node? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mInformationTextView = findViewById(R.id.information_text_view)
        mRemoteOpenButton = findViewById(R.id.remote_open_button)
        mInformationTextView!!.setText(CHECKING_MESSAGE)
        mRemoteOpenButton!!.setOnClickListener(View.OnClickListener { openAppInStoreOnPhone() })
    }

    override fun onPause() {
        Log.d(TAG, "onPause()")
        super.onPause()
        Wearable.getCapabilityClient(this).removeListener(this, CAPABILITY_PHONE_APP)
    }

    override fun onResume() {
        Log.d(TAG, "onResume()")
        super.onResume()
        Wearable.getCapabilityClient(this).addListener(this, CAPABILITY_PHONE_APP)
        checkIfPhoneHasApp()
    }

    /*
     * Updates UI when capabilities change (install/uninstall phone app).
     */
    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        Log.d(TAG, "onCapabilityChanged(): $capabilityInfo")
        mAndroidPhoneNodeWithApp = pickBestNodeId(capabilityInfo.nodes)
        verifyNodeAndUpdateUI()
    }

    private fun checkIfPhoneHasApp() {
        Log.d(TAG, "checkIfPhoneHasApp()")
        val capabilityInfoTask = Wearable.getCapabilityClient(this)
            .getCapability(CAPABILITY_PHONE_APP, CapabilityClient.FILTER_ALL)
        capabilityInfoTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Capability request succeeded.")
                val capabilityInfo = task.result
                mAndroidPhoneNodeWithApp = pickBestNodeId(capabilityInfo!!.nodes)
            } else {
                Log.d(TAG, "Capability request failed to return any results.")
            }
            verifyNodeAndUpdateUI()
        }
    }

    private fun verifyNodeAndUpdateUI() {
        if (mAndroidPhoneNodeWithApp != null) {

            // TODO: Add your code to communicate with the phone app via
            // Wear APIs (MessageApi, DataApi, etc.)
            val installMessage = String.format(INSTALLED_MESSAGE, mAndroidPhoneNodeWithApp!!.displayName)
            Log.d(TAG, installMessage)
            mInformationTextView!!.text = installMessage
            mRemoteOpenButton!!.visibility = View.INVISIBLE
        } else {
            Log.d(TAG, MISSING_MESSAGE)
            mInformationTextView!!.text = MISSING_MESSAGE
            mRemoteOpenButton!!.visibility = View.VISIBLE
        }
    }

    private fun openAppInStoreOnPhone() {
        Log.d(TAG, "openAppInStoreOnPhone()")
        val phoneDeviceType = PhoneTypeHelper.getPhoneDeviceType(applicationContext)
        when (phoneDeviceType) {
            PhoneTypeHelper.DEVICE_TYPE_ANDROID -> {
                Log.d(TAG, "\tDEVICE_TYPE_ANDROID")
                // Create Remote Intent to open Play Store listing of app on remote device.
                val intentAndroid = Intent(Intent.ACTION_VIEW)
                    .addCategory(Intent.CATEGORY_BROWSABLE)
                    .setData(Uri.parse(ANDROID_MARKET_APP_URI))
                RemoteIntent.startRemoteActivity(
                    applicationContext,
                    intentAndroid,
                    mResultReceiver
                )
            }
            PhoneTypeHelper.DEVICE_TYPE_IOS -> {
                Log.d(TAG, "\tDEVICE_TYPE_IOS")

                // Create Remote Intent to open App Store listing of app on iPhone.
                val intentIOS = Intent(Intent.ACTION_VIEW)
                    .addCategory(Intent.CATEGORY_BROWSABLE)
                    .setData(Uri.parse(APP_STORE_APP_URI))
                RemoteIntent.startRemoteActivity(
                    applicationContext,
                    intentIOS,
                    mResultReceiver
                )
            }
            PhoneTypeHelper.DEVICE_TYPE_ERROR or PhoneTypeHelper.DEVICE_TYPE_UNKNOWN -> Log.d(
                TAG,
                "\tDEVICE_TYPE_ERROR_UNKNOWN"
            )
        }
    }

    /*
     * There should only ever be one phone in a node set (much less w/ the correct capability), so
     * I am just grabbing the first one (which should be the only one).
     */
    private fun pickBestNodeId(nodes: Set<Node>): Node? {
        Log.d(TAG, "pickBestNodeId(): $nodes")
        var bestNodeId: Node? = null
        // Find a nearby node/phone or pick one arbitrarily. Realistically, there is only one phone.
        for (node in nodes) {
            bestNodeId = node
        }
        return bestNodeId
    }

    companion object {
        private const val TAG = "MainWearActivity"
        private const val WELCOME_MESSAGE = "Welcome to our Wear app!\n\n"
        private const val CHECKING_MESSAGE = """${WELCOME_MESSAGE}Checking for Mobile app...
"""
        private const val MISSING_MESSAGE =
            """${WELCOME_MESSAGE}You are missing the required phone app, please click on the button below to install it on your phone.
"""
        private const val INSTALLED_MESSAGE = """${WELCOME_MESSAGE}Mobile app installed on your %s!

You can now use MessageApi, DataApi, etc."""

        // Name of capability listed in Phone app's wear.xml.
        // IMPORTANT NOTE: This should be named differently than your Wear app's capability.
        private const val CAPABILITY_PHONE_APP = "verify_remote_example_phone_app"

        // Links to install mobile app for both Android (Play Store) and iOS.
        // TODO: Replace with your links/packages.
        private const val ANDROID_MARKET_APP_URI =
            "market://details?id=com.example.android.wearable.wear.wearverifyremoteapp"

        // TODO: Replace with your links/packages.
        private const val APP_STORE_APP_URI = "https://itunes.apple.com/us/app/android-wear/id986496028?mt=8"
    }
}